/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak[at]sernet[dot]de>.
 * This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *     You should have received a copy of the GNU Lesser General Public
 * License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     Alexander Koderman <ak[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.service.gstoolimport;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.sql.Clob;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.rtf.RTFEditorKit;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import sernet.gs.reveng.MbBaust;
import sernet.gs.reveng.MbDringlichkeit;
import sernet.gs.reveng.MbDringlichkeitId;
import sernet.gs.reveng.MbDringlichkeitTxt;
import sernet.gs.reveng.MbDringlichkeitTxtId;
import sernet.gs.reveng.MbGefaehr;
import sernet.gs.reveng.MbRolleTxt;
import sernet.gs.reveng.MsUnj;
import sernet.gs.reveng.NZielobjekt;
import sernet.gs.reveng.importData.BausteineMassnahmenResult;
import sernet.gs.reveng.importData.ESAResult;
import sernet.gs.reveng.importData.GSDBConstants;
import sernet.gs.reveng.importData.GSVampire;
import sernet.gs.reveng.importData.NotizenMassnahmeResult;
import sernet.gs.reveng.importData.RAGefaehrdungenResult;
import sernet.gs.reveng.importData.RAGefaehrdungsMassnahmenResult;
import sernet.gs.reveng.importData.ZielobjektTypeResult;
import sernet.hui.common.VeriniceContext;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.model.bsi.Anwendung;
import sernet.verinice.model.bsi.Client;
import sernet.verinice.model.bsi.Gebaeude;
import sernet.verinice.model.bsi.ITVerbund;
import sernet.verinice.model.bsi.MassnahmenUmsetzung;
import sernet.verinice.model.bsi.NetzKomponente;
import sernet.verinice.model.bsi.Person;
import sernet.verinice.model.bsi.Raum;
import sernet.verinice.model.bsi.Schutzbedarf;
import sernet.verinice.model.bsi.Server;
import sernet.verinice.model.bsi.SonstIT;
import sernet.verinice.model.bsi.TelefonKomponente;
import sernet.verinice.model.bsi.risikoanalyse.GefaehrdungsUmsetzung;
import sernet.verinice.model.bsi.risikoanalyse.OwnGefaehrdung;
import sernet.verinice.model.common.ChangeLogEntry;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.service.commands.UpdateElement;
import sernet.verinice.service.parser.GSScraperUtil;

/**
 * Utility class to convert result sets (from gstool databases) to
 * verinice-objects.
 *
 * @author koderman@sernet.de
 * @author sh@sernet.de - added ESA handling
 * @version $Rev$ $LastChangedDate$ $LastChangedBy$
 *
 */
public class TransferData {

    private static final Logger LOG = Logger.getLogger(TransferData.class);

    private static final Pattern PATTERN_BAUSTEIN_NUMMER = Pattern.compile("(\\d+)\\.0*(\\d+)");

    private static final Pattern PATTERN_URL = Pattern.compile("(\\\\.*\\\\.*\\\\)(.*)(\\.html)");

    private static final char KEIN_SIEGEL = '-';

    private final GSVampire vampire;
    private final boolean importRollen;
    private List<MbDringlichkeitTxt> dringlichkeiten;
    private Map<String, String> drgMap;

    private static final Map<String, String> typeIdESAEntscheidungBisMap = new HashMap<>(8);
    private static final Map<String, String> typeIdESAEntscheidungAmMap = new HashMap<>(8);
    private static final Map<String, String> typeIdESAEntscheidungDurchMap = new HashMap<>(8);
    static {
        typeIdESAEntscheidungBisMap.put(Raum.TYPE_ID, Raum.PROP_ESA_ENTSCHEIDUNG_BIS);
        typeIdESAEntscheidungBisMap.put(Anwendung.TYPE_ID, Anwendung.PROP_ESA_ENTSCHEIDUNG_BIS);
        typeIdESAEntscheidungBisMap.put(Client.TYPE_ID, Client.PROP_ESA_ENTSCHEIDUNG_BIS);
        typeIdESAEntscheidungBisMap.put(Gebaeude.TYPE_ID, Gebaeude.PROP_ESA_ENTSCHEIDUNG_BIS);
        typeIdESAEntscheidungBisMap.put(NetzKomponente.TYPE_ID,
                NetzKomponente.PROP_ESA_ENTSCHEIDUNG_BIS);
        typeIdESAEntscheidungBisMap.put(Server.TYPE_ID, Server.PROP_ESA_ENTSCHEIDUNG_BIS);
        typeIdESAEntscheidungBisMap.put(SonstIT.TYPE_ID, SonstIT.PROP_ESA_ENTSCHEIDUNG_BIS);
        typeIdESAEntscheidungBisMap.put(TelefonKomponente.TYPE_ID,
                TelefonKomponente.PROP_ESA_ENTSCHEIDUNG_BIS);
        typeIdESAEntscheidungAmMap.put(Raum.TYPE_ID, Raum.PROP_ESA_ENTSCHEIDUNG_AM);
        typeIdESAEntscheidungAmMap.put(Anwendung.TYPE_ID, Anwendung.PROP_ESA_ENTSCHEIDUNG_AM);
        typeIdESAEntscheidungAmMap.put(Client.TYPE_ID, Client.PROP_ESA_ENTSCHEIDUNG_AM);
        typeIdESAEntscheidungAmMap.put(Gebaeude.TYPE_ID, Gebaeude.PROP_ESA_ENTSCHEIDUNG_AM);
        typeIdESAEntscheidungAmMap.put(NetzKomponente.TYPE_ID,
                NetzKomponente.PROP_ESA_ENTSCHEIDUNG_AM);
        typeIdESAEntscheidungAmMap.put(Server.TYPE_ID, Server.PROP_ESA_ENTSCHEIDUNG_AM);
        typeIdESAEntscheidungAmMap.put(SonstIT.TYPE_ID, SonstIT.PROP_ESA_ENTSCHEIDUNG_AM);
        typeIdESAEntscheidungAmMap.put(TelefonKomponente.TYPE_ID,
                TelefonKomponente.PROP_ESA_ENTSCHEIDUNG_AM);
        typeIdESAEntscheidungDurchMap.put(Raum.TYPE_ID, Raum.PROP_ESA_ENTSCHEIDUNG_DURCH);
        typeIdESAEntscheidungDurchMap.put(Anwendung.TYPE_ID, Anwendung.PROP_ESA_ENTSCHEIDUNG_DURCH);
        typeIdESAEntscheidungDurchMap.put(Client.TYPE_ID, Client.PROP_ESA_ENTSCHEIDUNG_DURCH);
        typeIdESAEntscheidungDurchMap.put(Gebaeude.TYPE_ID, Gebaeude.PROP_ESA_ENTSCHEIDUNG_DURCH);
        typeIdESAEntscheidungDurchMap.put(NetzKomponente.TYPE_ID,
                NetzKomponente.PROP_ESA_ENTSCHEIDUNG_DURCH);
        typeIdESAEntscheidungDurchMap.put(Server.TYPE_ID, Server.PROP_ESA_ENTSCHEIDUNG_DURCH);
        typeIdESAEntscheidungDurchMap.put(SonstIT.TYPE_ID, SonstIT.PROP_ESA_ENTSCHEIDUNG_DURCH);
        typeIdESAEntscheidungDurchMap.put(TelefonKomponente.TYPE_ID,
                TelefonKomponente.PROP_ESA_ENTSCHEIDUNG_DURCH);
    }

    public TransferData(GSVampire vampire, boolean importRollen) {
        this.vampire = vampire;
        this.importRollen = importRollen;
    }

    public static void transfer(ITVerbund itverbund, ZielobjektTypeResult result)
            throws CommandException {
        NZielobjekt source = result.zielobjekt;
        itverbund.setTitel(source.getName());
        itverbund.setExtId(result.zielobjekt.getGuid());

        UpdateElement<ITVerbund> command = new UpdateElement<>(itverbund, true,
                ChangeLogEntry.STATION_ID);
        getCommandService().executeCommand(command);
    }

    public void transfer(CnATreeElement element, ZielobjektTypeResult result) {
        String typeId = element.getTypeId();
        if (typeId.equals(Anwendung.TYPE_ID)) {
            typedTransfer((Anwendung) element, result);
        }

        else if (typeId.equals(Client.TYPE_ID)) {
            typedTransfer((Client) element, result);
        }

        else if (typeId.equals(Server.TYPE_ID)) {
            typedTransfer((Server) element, result);
        }

        else if (typeId.equals(Person.TYPE_ID)) {
            typedTransfer((Person) element, result);

        }

        else if (typeId.equals(TelefonKomponente.TYPE_ID)) {
            typedTransfer((TelefonKomponente) element, result);
        }

        else if (typeId.equals(SonstIT.TYPE_ID)) {
            typedTransfer((SonstIT) element, result);
        }

        else if (typeId.equals(NetzKomponente.TYPE_ID)) {
            typedTransfer((NetzKomponente) element, result);
        }

        else if (typeId.equals(Gebaeude.TYPE_ID)) {
            typedTransfer((Gebaeude) element, result);
        }

        else if (typeId.equals(Raum.TYPE_ID)) {
            typedTransfer((Raum) element, result);
        }

        // use GSTOOL guid as extId:
        element.setExtId(result.zielobjekt.getGuid());
    }

    /**
     * Transfer fields for "Ergänzende Sicherheitsanalyse" from GSTOOL to
     * existing Zielobjekt.
     *
     * @param target
     * @param esa
     */
    public static void transferESA(CnATreeElement target, ESAResult esa) {
        // Zielobjekt-erg.sich.analyse
        // risikoanalyse J/N nZobEsa esamsunj
        // begründung bes. einsatz J/N Esaeinsatz 0 nein, 1 ja
        // begründung nicht mit bst. J/N esamodellierung
        // Begründung-text esabegründung
        // entscheidung entscheiddurch oder

        setEsaTrue(target, esa.getEinsatz(), esa.getModellierung());
        setRATrueFalse(target, esa.getUnj());

        String begruendung = "";
        if (esa.getEntscheidungDurch() != null && esa.getEntscheidungDurch().length() > 0) {
            begruendung += "Entscheidung durch: " + esa.getEntscheidungDurch() + "\n";
        }
        begruendung += esa.getBegruendung();
        setEsaBegruendung(target, begruendung);

        setESAEntscheidung(target, esa.getZmiName(), esa.getEntscheidungBis(),
                esa.getEntscheidungAm());
    }

    private static CnATreeElement setESAEntscheidung(CnATreeElement target,
            String entscheidungDurch, Date entscheidungBis, Date entscheidungAm) {
        setESAEntscheidungDurch(target, entscheidungDurch);
        setESAEntscheidungAm(target, entscheidungAm);
        setESAEntscheidungBis(target, entscheidungBis);
        return target;
    }

    private static CnATreeElement setESAEntscheidungBis(CnATreeElement target,
            Date entscheidungBis) {
        if (entscheidungBis != null
                && typeIdESAEntscheidungBisMap.containsKey(target.getTypeId())) {
            target.setSimpleProperty(typeIdESAEntscheidungBisMap.get(target.getTypeId()),
                    String.valueOf(entscheidungBis.getTime()));
        }
        return target;
    }

    private static CnATreeElement setESAEntscheidungAm(CnATreeElement target, Date entscheidungAm) {
        if (entscheidungAm != null && typeIdESAEntscheidungAmMap.containsKey(target.getTypeId())) {
            target.setSimpleProperty(typeIdESAEntscheidungAmMap.get(target.getTypeId()),
                    String.valueOf(entscheidungAm.getTime()));
        }
        return target;
    }

    private static CnATreeElement setESAEntscheidungDurch(CnATreeElement target,
            String entscheidungDurch) {
        if (StringUtils.isNotEmpty(entscheidungDurch)
                && typeIdESAEntscheidungDurchMap.containsKey(target.getTypeId())) {
            target.setSimpleProperty(typeIdESAEntscheidungDurchMap.get(target.getTypeId()),
                    entscheidungDurch);
        }
        return target;
    }

    /**
     * @param target
     * @param begruendung
     */
    private static void setEsaBegruendung(CnATreeElement target, String begruendung) {

        if (target.getTypeId().equals(Raum.TYPE_ID)) {
            target.setSimpleProperty("raum_risikoanalyse_begruendung", begruendung);
        }
        if (target.getTypeId().equals(Anwendung.TYPE_ID)) {
            target.setSimpleProperty("anwendung_risikoanalyse_begruendung", begruendung);
        }
        if (target.getTypeId().equals(Client.TYPE_ID)) {
            target.setSimpleProperty("client_risikoanalyse_begruendung", begruendung);
        }
        if (target.getTypeId().equals(Gebaeude.TYPE_ID)) {
            target.setSimpleProperty("gebaeude_risikoanalyse_begruendung", begruendung);
        }
        if (target.getTypeId().equals(NetzKomponente.TYPE_ID)) {
            target.setSimpleProperty("nkkomponente_risikoanalyse_begruendung", begruendung);
        }
        if (target.getTypeId().equals(Server.TYPE_ID)) {
            target.setSimpleProperty("server_risikoanalyse_begruendung", begruendung);
        }
        if (target.getTypeId().equals(SonstIT.TYPE_ID)) {
            target.setSimpleProperty("sonstit_risikoanalyse_begruendung", begruendung);
        }
        if (target.getTypeId().equals(TelefonKomponente.TYPE_ID)) {
            target.setSimpleProperty("tkkomponente_risikoanalyse_begruendung", begruendung);
        }

    }

    private static void setRATrueFalse(CnATreeElement target, byte unj) {
        if (unj == GSDBConstants.UNJ_UNBEARBEITET) {
            return;
        }
        if (unj == GSDBConstants.UNJ_JA) {
            setRATrue(target);
        } else if (unj == GSDBConstants.UNJ_NEIN) {
            setRAFalse(target);
        }
    }

    private static void setRATrue(CnATreeElement target) {
        if (target.getTypeId().equals(Raum.TYPE_ID)) {
            target.setSimpleProperty("raum_risikoanalyse", "raum_risikoanalyse_noetig");
        }
        if (target.getTypeId().equals(Anwendung.TYPE_ID)) {
            target.setSimpleProperty("anwendung_risikoanalyse", "anwendung_risikoanalyse_noetig");
        }
        if (target.getTypeId().equals(Client.TYPE_ID)) {
            target.setSimpleProperty("client_risikoanalyse", "client_risikoanalyse_noetig");
        }
        if (target.getTypeId().equals(Gebaeude.TYPE_ID)) {
            target.setSimpleProperty("gebaeude_risikoanalyse", "gebaeude_risikoanalyse_noetig");
        }
        if (target.getTypeId().equals(NetzKomponente.TYPE_ID)) {
            target.setSimpleProperty("nkkomponente_risikoanalyse",
                    "nkkomponente_risikoanalyse_noetig");
        }
        if (target.getTypeId().equals(Server.TYPE_ID)) {
            target.setSimpleProperty("server_risikoanalyse", "server_risikoanalyse_noetig");
        }
        if (target.getTypeId().equals(SonstIT.TYPE_ID)) {
            target.setSimpleProperty("sonstit_risikoanalyse", "sonstit_risikoanalyse_noetig");
        }
        if (target.getTypeId().equals(TelefonKomponente.TYPE_ID)) {
            target.setSimpleProperty("tkkomponente_risikoanalyse",
                    "tkkomponente_risikoanalyse_noetig");
        }
    }

    private static void setRAFalse(CnATreeElement target) {
        if (target.getTypeId().equals(Raum.TYPE_ID)) {
            target.setSimpleProperty("raum_risikoanalyse", "raum_risikoanalyse_unnoetig");
        }
        if (target.getTypeId().equals(Anwendung.TYPE_ID)) {
            target.setSimpleProperty("anwendung_risikoanalyse", "anwendung_risikoanalyse_unnoetig");
        }
        if (target.getTypeId().equals(Client.TYPE_ID)) {
            target.setSimpleProperty("client_risikoanalyse", "client_risikoanalyse_unnoetig");
        }
        if (target.getTypeId().equals(Gebaeude.TYPE_ID)) {
            target.setSimpleProperty("gebaeude_risikoanalyse", "gebaeude_risikoanalyse_unnoetig");
        }
        if (target.getTypeId().equals(NetzKomponente.TYPE_ID)) {
            target.setSimpleProperty("nkkomponente_risikoanalyse",
                    "nkkomponente_risikoanalyse_unnoetig");
        }
        if (target.getTypeId().equals(Server.TYPE_ID)) {
            target.setSimpleProperty("server_risikoanalyse", "server_risikoanalyse_unnoetig");
        }
        if (target.getTypeId().equals(SonstIT.TYPE_ID)) {
            target.setSimpleProperty("sonstit_risikoanalyse", "sonstit_risikoanalyse_unnoetig");
        }
        if (target.getTypeId().equals(TelefonKomponente.TYPE_ID)) {
            target.setSimpleProperty("tkkomponente_risikoanalyse",
                    "tkkomponente_risikoanalyse_unnoetig");
        }
    }

    private static void setEsaTrue(CnATreeElement target, byte besondererEinsatz,
            byte nichtModellierbar) {
        // one of the reasons has to be given, if not do nothing:
        if (besondererEinsatz == 0 && nichtModellierbar == 0) {
            return;
        }
        if (target.getTypeId().equals(Raum.TYPE_ID)) {
            target.setSimpleProperty("raum_ergaenzendeanalyse", "raum_ergaenzendeanalyse_modell");
        }
        if (target.getTypeId().equals(Anwendung.TYPE_ID)) {
            target.setSimpleProperty("anwendung_ergaenzendeanalyse",
                    "anwendung_ergaenzendeanalyse_modell");
        }
        if (target.getTypeId().equals(Client.TYPE_ID)) {
            target.setSimpleProperty("client_ergaenzendeanalyse",
                    "client_ergaenzendeanalyse_modell");
        }
        if (target.getTypeId().equals(Gebaeude.TYPE_ID)) {
            target.setSimpleProperty("gebaeude_ergaenzendeanalyse",
                    "gebaeude_ergaenzendeanalyse_modell");
        }
        if (target.getTypeId().equals(NetzKomponente.TYPE_ID)) {
            target.setSimpleProperty("nkkomponente_ergaenzendeanalyse",
                    "nkkomponente_ergaenzendeanalyse_modell");
        }
        if (target.getTypeId().equals(Server.TYPE_ID)) {
            target.setSimpleProperty("server_ergaenzendeanalyse",
                    "server_ergaenzendeanalyse_modell");
        }
        if (target.getTypeId().equals(SonstIT.TYPE_ID)) {
            target.setSimpleProperty("sonstit_ergaenzendeanalyse",
                    "sonstit_ergaenzendeanalyse_modell");
        }
        if (target.getTypeId().equals(TelefonKomponente.TYPE_ID)) {
            target.setSimpleProperty("tkkomponente_ergaenzendeanalyse",
                    "tkkomponente_ergaenzendeanalyse_modell");
        }
    }

    /**
     * Transfer "gefaehrdungen" to existing "gefaehrdungsumsetzung" object in a
     * "risikoanalyse" parent.
     *
     * @param gefaehrdungen
     * @param risikoanalyse
     * @throws IOException
     * @throws SQLException
     */
    public static void transferRAGefaehrdungsUmsetzung(GefaehrdungsUmsetzung gefaehrdungsUmsetzung,
            RAGefaehrdungenResult ragResult) throws IOException {
        // gefährdungsbewertung:

        // vollständigkeit J/N
        // mechanismenstärke J/N
        // zuverlässigkeit J/N
        transferGefaehrdungsBewertung(gefaehrdungsUmsetzung,
                ragResult.getRzg().getMsUnjByZgVollstaUnjId(),
                ragResult.getRzg().getMsUnjByZgStaerkeUnjId(),
                ragResult.getRzg().getMsUnjByZgZuverlaUnjId());

        transferGefaehrdungsBewertungTxt(gefaehrdungsUmsetzung,
                // vollst begr
                // mechan begr
                // zuverl begr
                ragResult.getRzg().getZgVollstaBegr(), ragResult.getRzg().getZgStaerkeBegr(),
                ragResult.getRzg().getZgZuverlaBegr(),
                // unterschrift liegt vor J/N
                ragResult.getRzg().getMsUnjByZgUnterUnjId(),
                // Risikobehandlung begründung
                ragResult.getRzg().getZgRabBegr());

        // these dates are currently not transferred
        // durchf. Von
        // ragResult.getRzg().getZgDatumVon();
        // durchf. Bis
        // ragResult.getRzg().getZgDatumBis();

        // risikobehandlung A-D
        gefaehrdungsUmsetzung.setAlternative(String.valueOf(ragResult.getRisikobehandlungABCD()));

        // ausreichender schutz J/N
        if (ragResult.getRzg().getMsUnjByZgOkUnjId().getUnjId() == GSDBConstants.UNJ_JA) {
            gefaehrdungsUmsetzung.setSimpleProperty("gefaehrdungsumsetzung_okay",
                    "gefaehrdungsumsetzung_okay_yes");
        } else if (ragResult.getRzg().getMsUnjByZgOkUnjId().getUnjId() == GSDBConstants.UNJ_NEIN) {
            gefaehrdungsUmsetzung.setSimpleProperty("gefaehrdungsumsetzung_okay",
                    "gefaehrdungsumsetzung_okay_no");
        }

        // we skip these: (from GSTOOL GUI, columns unknown, could be some of
        // the four listed below:)
        // entscheider (link person)
        // datum der entscheidung

        //
        // Ben.def.gs gefährdung
        // --------------------
        // nr
        // katalog
        // typ (bendef) RaZobGef.MyesnoByZgIndivYesId.yesId - wrong it's
        // apparently mbgef.userdef
        // bezeichnung
        // version
        // gef.txt

        String gefNr = translateGefaehrdungsNr(ragResult.getGefaehrdung());
        gefaehrdungsUmsetzung.setSimpleProperty("gefaehrdungsumsetzung_id", gefNr);

        gefaehrdungsUmsetzung.setDescription(
                convertClobToStringEncodingSave(ragResult.getGefaehrdungTxt().getBeschreibung(),
                        GSScraperUtil.getInstance().getModel().getEncoding()));

        gefaehrdungsUmsetzung.setTitel(ragResult.getGefaehrdungTxt().getName());
        String url = transferUrl(ragResult.getGefaehrdung().getLink());
        gefaehrdungsUmsetzung.setUrl(url);
    }

    private static String transferUrl(String url) {
        Matcher matcher = PATTERN_URL.matcher(url);
        if (matcher.find()) {
            url = matcher.group(2);
        }
        return url;
    }

    private static void transferGefaehrdungsBewertungTxt(GefaehrdungsUmsetzung gefUms,
            String zgVollstaBegr, String zgStaerkeBegr, String zgZuverlaBegr,
            MsUnj unterschriftLiegtVor, String begruendungRisikobehandlung) {
        StringBuilder sb = new StringBuilder();

        if (zgVollstaBegr != null && zgVollstaBegr.length() > 0) {
            sb.append("Bewertung ver Vollständigkeit:\n");
            sb.append(zgVollstaBegr);
        }
        if (zgStaerkeBegr != null && zgStaerkeBegr.length() > 0) {
            sb.append("\n\nBewertung der Mechanismenstärke:\n");
            sb.append(zgStaerkeBegr);

        }
        if (zgZuverlaBegr != null && zgZuverlaBegr.length() > 0) {
            sb.append("\n\nBewertung der Zuverlässigkeit:\n");
            sb.append(zgZuverlaBegr);
        }
        sb.append("\n\n");

        if (unterschriftLiegtVor.getUnjId() == GSDBConstants.UNJ_JA) {
            sb.append("Unterschrift liegt vor.\n\n");
        }
        if (unterschriftLiegtVor.getUnjId() == GSDBConstants.UNJ_NEIN) {
            sb.append("Unterschrift liegt nicht vor.\n\n");
        }

        if (begruendungRisikobehandlung != null && begruendungRisikobehandlung.length() > 0) {
            sb.append("Begründung der Risikobehandlung:\n" + begruendungRisikobehandlung);
        }

        sb.append("\n\n" + gefUms.getEntity().getSimpleValue("gefaehrdungsumsetzung_erlaeuterung"));

        gefUms.setSimpleProperty("gefaehrdungsumsetzung_erlaeuterung", sb.toString());

    }

    private static void transferGefaehrdungsBewertung(GefaehrdungsUmsetzung gefUms,
            MsUnj msUnjByZgVollstaUnjId, MsUnj msUnjByZgStaerkeUnjId, MsUnj msUnjByZgZuverlaUnjId) {
        if (msUnjByZgStaerkeUnjId.getUnjId() == GSDBConstants.UNJ_JA) {
            gefUms.setSimpleProperty("gefaehrdungsumsetzung_mechanismenstaerke",
                    "gefaehrdungsumsetzung_mechanismenstaerke_ja");
        }
        if (msUnjByZgStaerkeUnjId.getUnjId() == GSDBConstants.UNJ_NEIN) {
            gefUms.setSimpleProperty("gefaehrdungsumsetzung_mechanismenstaerke",
                    "gefaehrdungsumsetzung_mechanismenstaerke_nein");
        }

        if (msUnjByZgVollstaUnjId.getUnjId() == GSDBConstants.UNJ_JA) {
            gefUms.setSimpleProperty("gefaehrdungsumsetzung_vollstaendigkeit",
                    "gefaehrdungsumsetzung_vollstaendigkeit_ja");
        }
        if (msUnjByZgVollstaUnjId.getUnjId() == GSDBConstants.UNJ_NEIN) {
            gefUms.setSimpleProperty("gefaehrdungsumsetzung_vollstaendigkeit",
                    "gefaehrdungsumsetzung_vollstaendigkeit_nein");
        }

        if (msUnjByZgZuverlaUnjId.getUnjId() == GSDBConstants.UNJ_JA) {
            gefUms.setSimpleProperty("gefaehrdungsumsetzung_zuverlaessigkeit",
                    "gefaehrdungsumsetzung_zuverlaessigkeit_ja");
        }
        if (msUnjByZgZuverlaUnjId.getUnjId() == GSDBConstants.UNJ_NEIN) {
            gefUms.setSimpleProperty("gefaehrdungsumsetzung_zuverlaessigkeit",
                    "gefaehrdungsumsetzung_zuverlaessigkeit_nein");
        }
    }

    public static boolean isUserDefGefaehrdung(MbGefaehr gefaehrdung) {
        return gefaehrdung.getUserdef() == GSDBConstants.USERDEF_YES;
    }

    /**
     * @param gefaehrdung
     * @return
     */
    private static String translateGefaehrdungsNr(MbGefaehr gefaehrdung) {
        // this is how the displayed "number" has to be determined:
        if (gefaehrdung.getUserdef() == GSDBConstants.USERDEF_YES) {
            return "bG " + gefaehrdung.getGfkId() + "." + gefaehrdung.getNr();
        } else {
            return "G " + gefaehrdung.getGfkId() + "." + gefaehrdung.getNr();
        }
    }

    public static String convertClobToStringEncodingSave(Clob clob, String encoding)
            throws IOException {
        try (Reader reader = clob.getCharacterStream();
                InputStream in = new ByteArrayInputStream(IOUtils.toByteArray(reader, encoding))) {
            return IOUtils.toString(in, encoding);
        } catch (SQLException e) {
            LOG.error("Error while converting clob to String", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Transfer all data from one "massnahme" from gstool to existing
     * "gefaehrdung" underneath a risk analysis in verinice.
     * 
     * @param result
     * @param gefaehrdung
     *
     * @throws IOException
     */
    public static void transferRAGefaehrdungsMassnahmen(RAGefaehrdungsMassnahmenResult ragmResult,
            MassnahmenUmsetzung massnahmenUmsetzung) throws IOException {
        // Ben.def.gs massnahme
        // -------------------
        // katalog
        // typ (bendef)
        // nr
        // bezeichnung
        // version
        // maßnahmentext

        String massnahmeNr = translateMassnahmenNr(ragmResult);
        massnahmenUmsetzung.setSimpleProperty("mnums_id", massnahmeNr);
        massnahmenUmsetzung.setName(ragmResult.getMassnahmeTxt().getName());
        massnahmenUmsetzung.setDescription(
                convertClobToStringEncodingSave(ragmResult.getMassnahmeTxt().getBeschreibung(),
                        GSScraperUtil.getInstance().getModel().getEncoding()));
        massnahmenUmsetzung.setErlaeuterung(ragmResult.getMzbm().getUmsBeschr());
        massnahmenUmsetzung.setUrl(transferUrl(ragmResult.getMassnahme().getLink()));
        char siegel = convertToChar(ragmResult.getSiegelTxt().getKurzname());
        if (siegel != KEIN_SIEGEL) {
            massnahmenUmsetzung.setStufe(siegel);
        }
        MassnahmenFactory massnahmenFactory = new MassnahmenFactory();
        massnahmenUmsetzung = massnahmenFactory.transferUmsetzungWithDate(massnahmenUmsetzung,
                ragmResult.getUmsTxt().getName(), ragmResult.getMzbm().getUmsDatBis());
        massnahmenFactory.transferRevision(massnahmenUmsetzung, ragmResult.getMzbm().getRevDat(),
                ragmResult.getMzbm().getRevDatNext(), ragmResult.getMzbm().getRevBeschr());

        // may be necessary for user defined bausteine:

        // Massnahme-umsetzung
        // -------------------
        // nr
        // bezeichnung
        // bautein nr (rB 99.10)
        // baustein name
        // priorität
        // erforderlich ab A, b, c...
        // Umsetzung J,n,...
        // Lebenszyklusphase

    }

    /**
     * Convert ">G<", A, B, C, W, "-", "---" to a char ">G<" is converted to G
     *
     * @param kurzname
     * @return
     */
    private static char convertToChar(String kurzname) {
        char result = KEIN_SIEGEL;
        if (kurzname != null && !kurzname.isEmpty()) {
            if (kurzname.length() > 1) {
                result = kurzname.toCharArray()[1];
            } else {
                result = kurzname.toCharArray()[0];
            }
        }
        return result;
    }

    public static boolean isUserDefMassnahme(RAGefaehrdungsMassnahmenResult ragmResult) {
        return ragmResult.getMassnahme().getUserdef() == GSDBConstants.USERDEF_YES;
    }

    /**
     * @param ragmResult
     * @return
     */
    private static String translateMassnahmenNr(RAGefaehrdungsMassnahmenResult ragmResult) {
        if (ragmResult.getMassnahme().getUserdef() == GSDBConstants.USERDEF_YES) {
            return "bM " + ragmResult.getMassnahme().getMskId() + "."
                    + ragmResult.getMassnahme().getNr();
        } else {
            return "M " + ragmResult.getMassnahme().getMskId() + "."
                    + ragmResult.getMassnahme().getNr();
        }
    }

    private void typedTransfer(Anwendung element, ZielobjektTypeResult result) {
        element.setTitel(result.zielobjekt.getName());
        element.setKuerzel(result.zielobjekt.getKuerzel());
        element.setErlaeuterung(result.zielobjekt.getBeschreibung());
        element.setAnzahl(result.zielobjekt.getAnzahl());
        element.setVerarbeiteteInformationen(result.zielobjekt.getAnwBeschrInf());
        element.setProzessBeschreibung(result.zielobjekt.getAnwInf2Beschr());
        element.setProzessWichtigkeit(
                translateDringlichkeit(result.zielobjekt.getMbDringlichkeit()));
        element.setProzessWichtigkeitBegruendung(result.zielobjekt.getAnwInf1Beschr());
    }

    private String translateDringlichkeit(MbDringlichkeit mbDringlichkeit) {
        if (mbDringlichkeit == null) {
            return "";
        }
        if (dringlichkeiten == null) {
            dringlichkeiten = vampire.findDringlichkeitAll();
        }

        if (drgMap == null) {
            Map<String, String> m = new HashMap<>(4);
            m.put("unterstützend", Anwendung.PROP_PROZESSBEZUG_UNTERSTUETZEND);
            m.put("wichtig", Anwendung.PROP_PROZESSBEZUG_WICHTIG);
            m.put("wesentlich", Anwendung.PROP_PROZESSBEZUG_WESENTLICH);
            m.put("hochgradig notwendig", Anwendung.PROP_PROZESSBEZUG_HOCHGRADIG);
            drgMap = Collections.unmodifiableMap(m);
        }

        MbDringlichkeitId drgId = mbDringlichkeit.getId();
        for (MbDringlichkeitTxt dringlichkeit : dringlichkeiten) {
            MbDringlichkeitTxtId dringlichkeitId = dringlichkeit.getId();
            if (dringlichkeitId.getSprId() == 1
                    && dringlichkeitId.getDrgId().equals(drgId.getDrgId())
                    && dringlichkeitId.getDrgImpId().equals(drgId.getDrgImpId())) {
                return drgMap.get(dringlichkeit.getName());
            }
        }
        return "";
    }

    private static void typedTransfer(Client element, ZielobjektTypeResult result) {
        element.setTitel(result.zielobjekt.getName());
        element.setKuerzel(result.zielobjekt.getKuerzel());
        element.setErlaeuterung(result.zielobjekt.getBeschreibung());
        element.setAnzahl(result.zielobjekt.getAnzahl());
    }

    private static void typedTransfer(Server element, ZielobjektTypeResult result) {
        element.setTitel(result.zielobjekt.getName());
        element.setKuerzel(result.zielobjekt.getKuerzel());
        element.setErlaeuterung(result.zielobjekt.getBeschreibung());
        element.setAnzahl(result.zielobjekt.getAnzahl());
    }

    private void typedTransfer(Person element, ZielobjektTypeResult result) {
        element.getEntity().setSimpleValue(element.getEntityType().getPropertyType(Person.P_NAME),
                result.zielobjekt.getName());
        element.setKuerzel(result.zielobjekt.getKuerzel());
        element.setErlaeuterung(result.zielobjekt.getBeschreibung());
        element.setAnzahl(result.zielobjekt.getAnzahl());
        element.getEntity().setSimpleValue(element.getEntityType().getPropertyType(Person.P_PHONE),
                result.zielobjekt.getTelefon());
        element.getEntity().setSimpleValue(
                element.getEntityType().getPropertyType(Person.P_ORGEINHEIT),
                result.zielobjekt.getAbteilung());

        if (importRollen) {
            List<MbRolleTxt> rollen = vampire.findRollenByZielobjekt(result.zielobjekt);
            for (MbRolleTxt rolle : rollen) {
                boolean success = element.addRole(rolle.getName());
                if (!success) {
                    Logger.getLogger(this.getClass())
                            .debug("Rolle konnte nicht übertragen werden: " + rolle.getName());
                } else {
                    Logger.getLogger(this.getClass()).debug("Rolle übertragen: " + rolle.getName()
                            + " für Benutzer " + element.getTitle());
                }
            }
        }

    }

    private static void typedTransfer(TelefonKomponente element, ZielobjektTypeResult result) {
        element.setTitel(result.zielobjekt.getName());
        element.setKuerzel(result.zielobjekt.getKuerzel());
        element.setErlaeuterung(result.zielobjekt.getBeschreibung());
        element.setAnzahl(result.zielobjekt.getAnzahl());
    }

    private static void typedTransfer(SonstIT element, ZielobjektTypeResult result) {
        element.setTitel(result.zielobjekt.getName());
        element.setKuerzel(result.zielobjekt.getKuerzel());
        element.setErlaeuterung(result.zielobjekt.getBeschreibung());
        element.setAnzahl(result.zielobjekt.getAnzahl());
    }

    private static void typedTransfer(NetzKomponente element, ZielobjektTypeResult result) {
        element.setTitel(result.zielobjekt.getName());
        element.setKuerzel(result.zielobjekt.getKuerzel());
        element.setErlaeuterung(result.zielobjekt.getBeschreibung());
        element.setAnzahl(result.zielobjekt.getAnzahl());
    }

    private static void typedTransfer(Gebaeude element, ZielobjektTypeResult result) {
        element.setTitel(result.zielobjekt.getName());
        element.setKuerzel(result.zielobjekt.getKuerzel());
        element.setErlaeuterung(result.zielobjekt.getBeschreibung());
        element.setAnzahl(result.zielobjekt.getAnzahl());
    }

    private static void typedTransfer(Raum element, ZielobjektTypeResult result) {
        element.setTitel(result.zielobjekt.getName());
        element.setKuerzel(result.zielobjekt.getKuerzel());
        element.setErlaeuterung(result.zielobjekt.getBeschreibung());
        element.setAnzahl(result.zielobjekt.getAnzahl());
    }

    public static int translateSchutzbedarf(String name) {
        if (name.equals("normal")) {
            return Schutzbedarf.NORMAL;
        }
        if (name.equals("hoch")) {
            return Schutzbedarf.HOCH;
        }
        if (name.equals("sehr hoch")) {
            return Schutzbedarf.SEHRHOCH;
        }
        return Schutzbedarf.UNDEF;
    }

    /**
     * @param importTask
     * @param searchResult
     * @return
     */
    public static Map<MbBaust, List<BausteineMassnahmenResult>> convertBausteinMap(
            List<BausteineMassnahmenResult> searchResult) {
        // convert list to map: of bausteine and corresponding massnahmen:
        Map<MbBaust, List<BausteineMassnahmenResult>> bausteineMassnahmenMap = new HashMap<>();
        for (BausteineMassnahmenResult result : searchResult) {
            List<BausteineMassnahmenResult> list = bausteineMassnahmenMap.get(result.baustein);
            if (list == null) {
                list = new ArrayList<>();
                bausteineMassnahmenMap.put(result.baustein, list);
            }
            list.add(result);
        }
        return bausteineMassnahmenMap;
    }

    /**
     * Convert searchResult to map of baustein : list of massnahmen with notes
     *
     * @param notesResults
     */
    public static Map<MbBaust, List<NotizenMassnahmeResult>> convertZielobjektNotizenMap(
            List<NotizenMassnahmeResult> searchResult) {
        Map<MbBaust, List<NotizenMassnahmeResult>> bausteineMassnahmenMap = new HashMap<>();
        for (NotizenMassnahmeResult result : searchResult) {
            List<NotizenMassnahmeResult> list = bausteineMassnahmenMap.get(result.baustein);
            if (list == null) {
                list = new ArrayList<>();
                bausteineMassnahmenMap.put(result.baustein, list);
            }
            list.add(result);
        }
        return bausteineMassnahmenMap;
    }

    public static String getId(MbBaust mbBaust) {

        Matcher match = PATTERN_BAUSTEIN_NUMMER.matcher(mbBaust.getNr());
        if (match.matches()) {
            return "B " + match.group(1) + "." + Integer.parseInt(match.group(2));
        }
        // TODO AK if none found return ben.def.baustein number
        return "";
    }

    public static BausteineMassnahmenResult findMassnahmenVorlageBaustein(
            MassnahmenUmsetzung massnahmenUmsetzung, List<BausteineMassnahmenResult> list) {
        for (BausteineMassnahmenResult result : list) {
            if (massnahmenUmsetzung.getKapitelValue()[0] == result.massnahme.getMskId()
                    && massnahmenUmsetzung.getKapitelValue()[1] == result.massnahme.getNr()) {
                return result;
            }
        }
        return null;
    }

    /**
     * @param mnums
     * @param massnahmenNotizen
     */
    public static List<NotizenMassnahmeResult> findMassnahmenVorlageNotiz(
            MassnahmenUmsetzung massnahmenUmsetzung, List<NotizenMassnahmeResult> list) {
        List<NotizenMassnahmeResult> resultList = new ArrayList<>();

        for (NotizenMassnahmeResult result : list) {
            if (result.massnahme != null
                    && massnahmenUmsetzung.getKapitelValue()[0] == result.massnahme.getMskId()
                    && massnahmenUmsetzung.getKapitelValue()[1] == result.massnahme.getNr()) {
                resultList.add(result);
            }
        }
        return resultList;

    }

    public static String convertRtf(String notizText) throws IOException, BadLocationException {
        StringReader reader = new StringReader(notizText);
        RTFEditorKit kit = new RTFEditorKit();
        Document document = kit.createDefaultDocument();
        kit.read(reader, document, 0);
        // return plaintext
        return document.getText(0, document.getLength());
    }

    /**
     * Find notes that are connected to a baustein directly.
     * 
     * @param massnahmenNotizen
     *
     * @return
     */
    public static List<NotizenMassnahmeResult> findBausteinVorlageNotiz(
            List<NotizenMassnahmeResult> list) {

        List<NotizenMassnahmeResult> resultList = new ArrayList<>();

        for (NotizenMassnahmeResult result : list) {
            if (result.massnahme == null) {
                resultList.add(result);
            }
        }
        return resultList;

    }

    /**
     * @param ownGefaehrdung
     * @param ragResult
     * @throws IOException
     */
    public static void transferOwnGefaehrdung(OwnGefaehrdung ownGefaehrdung,
            RAGefaehrdungenResult ragResult) throws IOException {
        String gefNr = translateGefaehrdungsNr(ragResult.getGefaehrdung());
        ownGefaehrdung.setId(gefNr);
        ownGefaehrdung.setExtId(GSVampire.generateGefaehrdungsUmsetzungExtid(
                String.valueOf(ragResult.getGefaehrdung().getId().getGefId()),
                String.valueOf(ragResult.getZielobjekt().getId().getZobId()),
                ragResult.getGefaehrdung().getGuid(), ragResult.getZielobjekt().getGuid()));
        ownGefaehrdung.setTitel(ragResult.getGefaehrdungTxt().getName());
        ownGefaehrdung.setBeschreibung(
                convertClobToStringEncodingSave(ragResult.getGefaehrdungTxt().getBeschreibung(),
                        GSScraperUtil.getInstance().getModel().getEncoding()));
    }

    public static String createBausteineMassnahmenResultIdentifier(
            BausteineMassnahmenResult bausteineMassnahmenResult) {
        StringBuilder sb = new StringBuilder();
        sb.append(bausteineMassnahmenResult.baustein.getId().getBauId());
        sb.append(bausteineMassnahmenResult.massnahme.getId().getMasId());
        sb.append(bausteineMassnahmenResult.obm.hashCode());
        sb.append(bausteineMassnahmenResult.umstxt.getId());
        sb.append(bausteineMassnahmenResult.zoBst.getId().hashCode());
        return sb.toString();
    }

    public static ICommandService getCommandService() {
        return (ICommandService) VeriniceContext.get(VeriniceContext.COMMAND_SERVICE);
    }
}
