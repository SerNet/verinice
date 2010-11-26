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
package sernet.gs.ui.rcp.gsimport;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.rtf.RTFEditorKit;

import org.apache.log4j.Logger;

import sernet.gs.reveng.MbBaust;
import sernet.gs.reveng.MbDringlichkeit;
import sernet.gs.reveng.MbDringlichkeitId;
import sernet.gs.reveng.MbDringlichkeitTxt;
import sernet.gs.reveng.MbRolleTxt;
import sernet.gs.reveng.NZielobjekt;
import sernet.gs.reveng.importData.BausteineMassnahmenResult;
import sernet.gs.reveng.importData.GSVampire;
import sernet.gs.reveng.importData.NotizenMassnahmeResult;
import sernet.gs.reveng.importData.ZielobjektTypeResult;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
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
import sernet.verinice.model.common.CnATreeElement;

/**
 * Utility class to convert result sets (from gstool databases) to verinice-objects.
 * 
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public class TransferData {
	
	
	private GSVampire vampire;
	private boolean importRollen;
	private List<MbDringlichkeitTxt> dringlichkeiten;
	private HashMap<String, String> drgMap;

	public TransferData(GSVampire vampire, boolean importRollen) {
		this.vampire = vampire;
		this.importRollen = importRollen ; 
	}

	public void transfer(ITVerbund itverbund, ZielobjektTypeResult result) throws Exception {
		NZielobjekt source = result.zielobjekt;
		itverbund.setTitel(source.getName());
		CnAElementHome.getInstance().update(itverbund);
	}

	public void transfer(CnATreeElement element, ZielobjektTypeResult result) {
			String typeId = element.getTypeId();
			if (typeId.equals(Anwendung.TYPE_ID)) {
				typedTransfer((Anwendung)element, result);
			}
			
			else if (typeId.equals(Client.TYPE_ID)) {
				typedTransfer((Client)element, result);
			} 
		
			else if (typeId.equals(Server.TYPE_ID)) {
				typedTransfer((Server)element, result);
			}
		
			else if (typeId.equals(Person.TYPE_ID)) {
				typedTransfer((Person)element, result);
			
			}

			else if (typeId.equals(TelefonKomponente.TYPE_ID)) {
				typedTransfer((TelefonKomponente)element, result);
			}
			
			else if (typeId.equals(SonstIT.TYPE_ID)) {
				typedTransfer((SonstIT)element, result);
			} 
			
			else if (typeId.equals(NetzKomponente.TYPE_ID)) {
				typedTransfer((NetzKomponente)element, result);
			}
		
			else if (typeId.equals(Gebaeude.TYPE_ID)) {
				typedTransfer((Gebaeude)element, result);
			} 
		
			else if (typeId.equals(Raum.TYPE_ID)) {
				typedTransfer((Raum)element, result);
			}
			
	}

	private void typedTransfer(Anwendung element, ZielobjektTypeResult result) {
		element.setTitel(result.zielobjekt.getName());
		element.setKuerzel(result.zielobjekt.getKuerzel());
		element.setErlaeuterung(result.zielobjekt.getBeschreibung());
		element.setAnzahl(result.zielobjekt.getAnzahl());
		element.setVerarbeiteteInformationen(result.zielobjekt.getAnwBeschrInf());
		element.setProzessBeschreibung(result.zielobjekt.getAnwInf2Beschr());
		element.setProzessWichtigkeit(translateDringlichkeit(result.zielobjekt.getMbDringlichkeit()));
		element.setProzessWichtigkeitBegruendung(result.zielobjekt.getAnwInf1Beschr());
	}

	private String translateDringlichkeit(MbDringlichkeit mbDringlichkeit) {
		if (mbDringlichkeit == null)
			return "";
		
		if (dringlichkeiten == null) {
			dringlichkeiten = vampire.findDringlichkeitAll();
		}
		
		if (drgMap == null) {
			drgMap = new HashMap<String, String>();
			drgMap.put("unterstützend",			Anwendung.PROP_PROZESSBEZUG_UNTERSTUETZEND);
			drgMap.put("wichtig", 				Anwendung.PROP_PROZESSBEZUG_WICHTIG);
			drgMap.put("wesentlich", 			Anwendung.PROP_PROZESSBEZUG_WESENTLICH);
			drgMap.put("hochgradig notwendig", 	Anwendung.PROP_PROZESSBEZUG_HOCHGRADIG);
		}
		
		MbDringlichkeitId drgId = mbDringlichkeit.getId();
		String drgName = "";
		for (MbDringlichkeitTxt dringlichkeit : dringlichkeiten) {
			if (dringlichkeit.getId().getSprId() == 1
					&& dringlichkeit.getId().equals(drgId)) {
				drgName = dringlichkeit.getName();
				return drgMap.get(drgName);
			}
		}
		return "";
	}

	private void typedTransfer(Client element, ZielobjektTypeResult result) {
		element.setTitel(result.zielobjekt.getName());
		element.setKuerzel(result.zielobjekt.getKuerzel());
		element.setErlaeuterung(result.zielobjekt.getBeschreibung());
		element.setAnzahl(result.zielobjekt.getAnzahl());
		}

	private void typedTransfer(Server element, ZielobjektTypeResult result) {
		element.setTitel(result.zielobjekt.getName());
		element.setKuerzel(result.zielobjekt.getKuerzel());
		element.setErlaeuterung(result.zielobjekt.getBeschreibung());
		element.setAnzahl(result.zielobjekt.getAnzahl());
	}

	private void typedTransfer(Person element, ZielobjektTypeResult result) {
		element.setTitel(result.zielobjekt.getName());
		element.setKuerzel(result.zielobjekt.getKuerzel());
		element.setErlaeuterung(result.zielobjekt.getBeschreibung());
		element.setAnzahl(result.zielobjekt.getAnzahl());
		
		if (importRollen) {
			List<MbRolleTxt> rollen = vampire.findRollenByZielobjekt(result.zielobjekt);
			for (MbRolleTxt rolle : rollen) {
				boolean success = element.addRole(rolle.getName());
				if (!success)
					Logger.getLogger(this.getClass()).debug("Rolle konnte nicht übertragen werden: " + 
							rolle.getName());
				else
					Logger.getLogger(this.getClass()).debug("Rolle übertragen: " + rolle.getName() + " für Benutzer " + element.getTitle());
			}
		}
		
	}

	private void typedTransfer(TelefonKomponente element,
			ZielobjektTypeResult result) {
		element.setTitel(result.zielobjekt.getName());
		element.setKuerzel(result.zielobjekt.getKuerzel());
		element.setErlaeuterung(result.zielobjekt.getBeschreibung());
		element.setAnzahl(result.zielobjekt.getAnzahl());
	}

	private void typedTransfer(SonstIT element, ZielobjektTypeResult result) {
		element.setTitel(result.zielobjekt.getName());
		element.setKuerzel(result.zielobjekt.getKuerzel());
		element.setErlaeuterung(result.zielobjekt.getBeschreibung());
		element.setAnzahl(result.zielobjekt.getAnzahl());
	}

	private void typedTransfer(NetzKomponente element,
			ZielobjektTypeResult result) {
		element.setTitel(result.zielobjekt.getName());
		element.setKuerzel(result.zielobjekt.getKuerzel());
		element.setErlaeuterung(result.zielobjekt.getBeschreibung());
		element.setAnzahl(result.zielobjekt.getAnzahl());
	}

	private void typedTransfer(Gebaeude element, ZielobjektTypeResult result) {
		element.setTitel(result.zielobjekt.getName());
		element.setKuerzel(result.zielobjekt.getKuerzel());
		element.setErlaeuterung(result.zielobjekt.getBeschreibung());
		element.setAnzahl(result.zielobjekt.getAnzahl());
	}

	private void typedTransfer(Raum element, ZielobjektTypeResult result) {
		element.setTitel(result.zielobjekt.getName());
		element.setKuerzel(result.zielobjekt.getKuerzel());
		element.setErlaeuterung(result.zielobjekt.getBeschreibung());
		element.setAnzahl(result.zielobjekt.getAnzahl());
	}

	public int translateSchutzbedarf(String name) {
		if (name.equals("normal"))
			return Schutzbedarf.NORMAL;
		if (name.equals("hoch"))
			return Schutzbedarf.HOCH;
		if (name.equals("sehr hoch"))
			return Schutzbedarf.SEHRHOCH;
		return Schutzbedarf.UNDEF;
	}

	/**
	 * @param importTask
	 * @param searchResult
	 * @return
	 */
	public Map<MbBaust, List<BausteineMassnahmenResult>> convertBausteinMap( List<BausteineMassnahmenResult> searchResult) {
		// convert list to map: of bausteine and corresponding massnahmen:
		Map<MbBaust, List<BausteineMassnahmenResult>> bausteineMassnahmenMap = new HashMap<MbBaust, List<BausteineMassnahmenResult>>();
		for (BausteineMassnahmenResult result : searchResult) {
			List<BausteineMassnahmenResult> list = bausteineMassnahmenMap
					.get(result.baustein);
			if (list == null) {
				list = new ArrayList<BausteineMassnahmenResult>();
				bausteineMassnahmenMap.put(result.baustein, list);
			}
			list.add(result);
		}
		return bausteineMassnahmenMap;
	}

	/**
	 * Convert searchResult to map of baustein : list of massnahmen with notes
	 * @param notesResults
	 */
	public Map<MbBaust, List<NotizenMassnahmeResult>> convertZielobjektNotizenMap(List<NotizenMassnahmeResult> searchResult) {
		Map<MbBaust, List<NotizenMassnahmeResult>> bausteineMassnahmenMap = new HashMap<MbBaust, List<NotizenMassnahmeResult>>();
		for (NotizenMassnahmeResult result : searchResult) {
			List<NotizenMassnahmeResult> list = bausteineMassnahmenMap
					.get(result.baustein);
			if (list == null) {
				list = new ArrayList<NotizenMassnahmeResult>();
				bausteineMassnahmenMap.put(result.baustein, list);
			}
			list.add(result);
		}
		return bausteineMassnahmenMap;
	}
	
	public static String getId(MbBaust mbBaust) {
        Pattern pattern = Pattern.compile("(\\d+)\\.0*(\\d+)");

        Matcher match = pattern.matcher(mbBaust.getNr());
        if (match.matches())
            return "B " + match.group(1) + "."
                    + Integer.parseInt(match.group(2));
        return "";
    }
	
	public static BausteineMassnahmenResult findMassnahmenVorlage(
            MassnahmenUmsetzung massnahmenUmsetzung,
            List<BausteineMassnahmenResult> list) {
        for (BausteineMassnahmenResult result : list) {
            if (massnahmenUmsetzung.getKapitelValue()[0] == result.massnahme
                    .getMskId()
                    && massnahmenUmsetzung.getKapitelValue()[1] == result.massnahme
                            .getNr()) {
                return result;
            }
        }
        return null;
    }

    /**
     * @param mnums
     * @param massnahmenNotizen
     */
    public static NotizenMassnahmeResult findMassnahmenVorlage(MassnahmenUmsetzung massnahmenUmsetzung, List<NotizenMassnahmeResult> list) {
        for (NotizenMassnahmeResult result : list) {
            if (massnahmenUmsetzung.getKapitelValue()[0] == result.massnahme
                    .getMskId()
                    && massnahmenUmsetzung.getKapitelValue()[1] == result.massnahme
                            .getNr()) {
                return result;
            }
        }
        return null;
    
    }
    
    public static String convertRtf(String notizText) throws IOException, BadLocationException {
        StringReader reader = new StringReader(notizText);
        RTFEditorKit kit = new RTFEditorKit();
        Document document = kit.createDefaultDocument();
        kit.read(reader, document, 0);

        String plainText = document.getText(0, document.getLength());
        return plainText;
    }

}
