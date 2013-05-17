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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Preferences;
import org.hibernate.Hibernate;
import org.hibernate.exception.SQLGrammarException;

import sernet.gs.reveng.MSchutzbedarfkategTxt;
import sernet.gs.reveng.MbBaust;
import sernet.gs.reveng.MbZeiteinheitenTxt;
import sernet.gs.reveng.ModZobjBst;
import sernet.gs.reveng.ModZobjBstMass;
import sernet.gs.reveng.NZielobjekt;
import sernet.gs.reveng.NZobSb;
import sernet.gs.reveng.importData.BausteineMassnahmenResult;
import sernet.gs.reveng.importData.GSVampire;
import sernet.gs.reveng.importData.ZielobjektTypeResult;
import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.CnAWorkspace;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.bsi.model.BSIConfigurationRCPLocal;
import sernet.gs.ui.rcp.main.bsi.model.CnAElementBuilder;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.taskcommands.ImportCreateBausteinReferences;
import sernet.gs.ui.rcp.main.service.taskcommands.ImportCreateBausteine;
import sernet.gs.ui.rcp.main.service.taskcommands.ImportTransferSchutzbedarf;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.model.bsi.BausteinUmsetzung;
import sernet.verinice.model.bsi.ITVerbund;
import sernet.verinice.model.bsi.MassnahmenUmsetzung;
import sernet.verinice.model.bsi.NetzKomponente;
import sernet.verinice.model.bsi.Person;
import sernet.verinice.model.bsi.Schutzbedarf;
import sernet.verinice.model.common.CnATreeElement;

import com.heatonresearch.datamover.DataMover;
import com.heatonresearch.datamover.db.Database;
import com.heatonresearch.datamover.db.DerbyDatabase;
import com.heatonresearch.datamover.db.MDBFileDatabase;

/**
 * Import GSTOOL(tm) databases using the GSVampire. Maps GStool-database objects
 * to Verinice-Objects and fields.
 * 
 * @author koderman[at]sernet[dot]de
 * @version $Rev$ $LastChangedDate$ $LastChangedBy$
 * 
 */
public class ImportTask {

    private static final Logger LOG = Logger.getLogger(ImportTask.class);

    public static final int TYPE_SQLSERVER = 1;
    public static final int TYPE_MDB = 2;

    private IProgress monitor;
    private GSVampire vampire;
    private TransferData transferData;

    private List<MbZeiteinheitenTxt> zeiten;
    private Map<ModZobjBstMass, MassnahmenUmsetzung> alleMassnahmen;
    private Map<NZielobjekt, CnATreeElement> alleZielobjekte = new HashMap<NZielobjekt, CnATreeElement>();
    private List<Person> allePersonen = new ArrayList<Person>();
    private boolean importBausteine;
    private boolean massnahmenPersonen;
    private boolean bausteinPersonen;
    private boolean zielObjekteZielobjekte;
    private boolean schutzbedarf;
    private boolean importRollen;
    private boolean kosten;
    private boolean importUmsetzung;

    private Map<MbBaust, BausteinUmsetzung> alleBausteineToBausteinUmsetzungMap;
    private Map<MbBaust, ModZobjBst> alleBausteineToZoBstMap;

    public ImportTask(boolean bausteine, boolean massnahmenPersonen, boolean zielObjekteZielobjekte, boolean schutzbedarf, boolean importRollen, boolean kosten, boolean umsetzung, boolean bausteinPersonen) {
        this.importBausteine = bausteine;
        this.massnahmenPersonen = massnahmenPersonen;
        this.zielObjekteZielobjekte = zielObjekteZielobjekte;
        this.schutzbedarf = schutzbedarf;
        this.importRollen = importRollen;
        this.kosten = kosten;
        this.importUmsetzung = umsetzung;
        this.bausteinPersonen = bausteinPersonen;

        this.alleBausteineToBausteinUmsetzungMap = new HashMap<MbBaust, BausteinUmsetzung>();
        this.alleBausteineToZoBstMap = new HashMap<MbBaust, ModZobjBst>();
        this.alleMassnahmen = new HashMap<ModZobjBstMass, MassnahmenUmsetzung>();
    }

    public void execute(int importType, IProgress monitor) throws Exception {
        // On this thread Hibernate will access Antlr in order to create a
        // lexer.
        // Hibernate will provide the name of a Hibernate-based class to Antlr.
        // Antlr
        // will try to load that class. In an OSGi-environment this will
        // miserably
        // fail since the Antlr bundle's classloader has no access to the
        // Hibernate
        // bundle's classes. However Antlr will use the context classloader if
        // it
        // finds one. For this reason we initialize the context classloader with
        // a classloader from a Hibernate class. This classloader is able to
        // resolve
        // Hibernate classes and can be used successfully by Antlr to access.
        try{ 
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            ClassLoader classLoader = Hibernate.class.getClassLoader();
            Thread.currentThread().setContextClassLoader(classLoader);

            Preferences prefs = Activator.getDefault().getPluginPreferences();
            String sourceDbUrl = prefs.getString(PreferenceConstants.GS_DB_URL);
            if (sourceDbUrl.indexOf("odbc") > -1) {
                copyMDBToTempDB(sourceDbUrl);
            }

            this.monitor = monitor;
            File conf = new File(CnAWorkspace.getInstance().getConfDir() + File.separator + "hibernate-vampire.cfg.xml");
            vampire = new GSVampire(conf.getAbsolutePath());

            zeiten = vampire.findZeiteinheitenTxtAll();

            transferData = new TransferData(vampire, importRollen);
            importZielobjekte();

            // Set back the original context class loader.
            Thread.currentThread().setContextClassLoader(cl);

            CnAElementFactory.getInstance().reloadModelFromDatabase();
        } catch (GSImportException e){
            ExceptionUtil.log(e, e.getMessage());
        }
    }

    private void copyMDBToTempDB(String sourceDbUrl) {
        Database source = new MDBFileDatabase();
        Database target = new DerbyDatabase();
        try {
            // copy contents of MDB file to temporary derby db:
            String tempDbUrl = CnAWorkspace.getInstance().createTempImportDbUrl();

            DataMover mover = new DataMover();

            source.connect(PreferenceConstants.GS_DB_DRIVER_ODBC, sourceDbUrl);
            target.connect(PreferenceConstants.DB_DRIVER_DERBY, tempDbUrl);

            mover.setSource(source);
            mover.setTarget(target);
            mover.exportDatabse();
        } catch (Exception e) {
            LOG.error("Error: ", e);
            ExceptionUtil.log(e, "Fehler beim Import aus MDB Datei über temporäre Derby-DB.");
        } finally {
            try {
                source.close();
                target.close();
            } catch (Exception e) {
                LOG.debug("Konnte temporäre Import DB nicht schließen.", e);
            }
        }

    }

    public boolean delete(File dir) {
        if (dir.isDirectory()) {
            String[] subdirs = dir.list();
            for (int i = 0; i < subdirs.length; i++) {
                boolean success = delete(new File(dir, subdirs[i]));
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    }

    private void importZielobjekte() throws Exception {
        // generate a sourceId for objects created by this import:
        final int maxUuidLength = 6;
        final String defaultSubTaskDescription = "Schreibe alle Objekte in Verinice-Datenbank...";
        String sourceId = UUID.randomUUID().toString().substring(0, maxUuidLength);

        List<ZielobjektTypeResult> zielobjekte;
        try {
            zielobjekte = vampire.findZielobjektTypAll();
        } catch(SQLGrammarException e){
            SQLGrammarException sqlException = (SQLGrammarException) e;
            // wrong db version has columns missing, i.e. "GEF_ID":
            if (sqlException.getSQLException().getMessage().indexOf("GEF_OK") > -1) {
                ExceptionUtil.log(sqlException.getSQLException(), "Fehler beim Laden der Zielobjekte. Möglicherweise falsche Datenbankversion des GSTOOL? " + "\nEs wird nur der Import der aktuellen Version (4.7) des GSTOOL unterstützt.");
            }
            throw e;
        } catch (Exception e) {
            ExceptionUtil.log(e, "Fehler beim Laden der Zielobjekte");
            throw e;
        }
        if (this.importBausteine) {
            monitor.beginTask("Lese Zielobjekte, Bausteine und Massnahmen...", zielobjekte.size());
        } else {
            monitor.beginTask("Lese Zielobjekte...", zielobjekte.size());
        }

        // create all found ITVerbund first
        List<ITVerbund> neueVerbuende = new ArrayList<ITVerbund>();
        for (ZielobjektTypeResult result : zielobjekte) {
            try{
                if (ITVerbund.TYPE_ID.equals(ImportZielobjektTypUtil.translateZielobjektType(result.type, result.subtype))) {
                    ITVerbund itverbund = (ITVerbund) CnAElementFactory.getInstance().saveNew(CnAElementFactory.getLoadedModel(), ITVerbund.TYPE_ID, null);
                    neueVerbuende.add(itverbund);
                    monitor.worked(1);

                    // save element for later:
                    alleZielobjekte.put(result.zielobjekt, itverbund);

                    transferData.transfer(itverbund, result);
                    createBausteine(sourceId, itverbund, result.zielobjekt);
                }
            } catch (GSImportException e){
                throw e;
            }
        }

        // create all Zielobjekte in first ITVerbund,
        // TODO tag them with every ITVerbund the've been in
        for (ZielobjektTypeResult result : zielobjekte) {
            String typeId = ImportZielobjektTypUtil.translateZielobjektType(result.type, result.subtype);
            if (LOG.isDebugEnabled()) {
                LOG.debug("GSTOOL type id " + result.type + " : " + result.subtype + " was translated to: " + typeId);
            }
            CnATreeElement element = null;
            if(neueVerbuende.size() > 0){
                element = CnAElementBuilder.getInstance().buildAndSave(neueVerbuende.get(0), typeId);
            }
            if (element != null) {
                // save element for later:
                alleZielobjekte.put(result.zielobjekt, element);

                // aditionally save persons:
                if (element instanceof Person) {
                    allePersonen.add((Person) element);
                }

                transferData.transfer(element, result);

                monitor.subTask(element.getTitle());
                createBausteine(sourceId, element, result.zielobjekt);

                CnAElementHome.getInstance().update(element);
                monitor.worked(1);
            }
        }

        monitor.subTask(defaultSubTaskDescription);

        importMassnahmenVerknuepfungen();
        monitor.subTask(defaultSubTaskDescription);

        importBausteinPersonVerknuepfungen();
        monitor.subTask(defaultSubTaskDescription);

        importZielobjektVerknuepfungen();
        monitor.subTask(defaultSubTaskDescription);

        importSchutzbedarf();
        monitor.subTask(defaultSubTaskDescription);

        monitor.beginTask("Lese Bausteinreferenzen...", zielobjekte.size());
        for (NZielobjekt zielobjekt : alleZielobjekte.keySet()) {
            CnATreeElement element = alleZielobjekte.get(zielobjekt);
            monitor.subTask(element.getTitle());
            if (!element.getTypeId().equals(ITVerbund.TYPE_ID)) {
                createBausteinReferences(sourceId, element, zielobjekt);
            }
            monitor.worked(1);
        }
        monitor.done();
    }

    /**
     * @param sourceId
     * @param element
     * @param zielobjekt
     * @throws CommandException
     */
    private void createBausteinReferences(String sourceId, CnATreeElement element, NZielobjekt zielobjekt) throws CommandException {
        if (!importBausteine) {
            return;
        }

        List<BausteineMassnahmenResult> findBausteinMassnahmenByZielobjekt = vampire.findBausteinMassnahmenByZielobjekt(zielobjekt);

        Map<MbBaust, List<BausteineMassnahmenResult>> bausteineMassnahmenMap = transferData.convertBausteinMap(findBausteinMassnahmenByZielobjekt);

        this.monitor.subTask(Messages.Import_Task_1 + zielobjekt.getName());

        ImportCreateBausteinReferences command;
        ServiceFactory.lookupAuthService();
        if (!ServiceFactory.isPermissionHandlingNeeded()) {
            command = new ImportCreateBausteinReferences(sourceId, element, bausteineMassnahmenMap, new BSIConfigurationRCPLocal());
        } else {
            command = new ImportCreateBausteinReferences(sourceId, element, bausteineMassnahmenMap);
        }
        ServiceFactory.lookupCommandService().executeCommand(command);
    }

    private void importSchutzbedarf() throws Exception {
        if (!schutzbedarf) {
            return;
        }

        monitor.beginTask("Importiere Schutzbedarf für alle Zielobjekte...", alleZielobjekte.size());
        Set<Entry<NZielobjekt, CnATreeElement>> alleZielobjekteEntries = alleZielobjekte.entrySet();

        for (Entry<NZielobjekt, CnATreeElement> entry : alleZielobjekteEntries) {
            List<NZobSb> internalSchutzbedarf = vampire.findSchutzbedarfByZielobjekt(entry.getKey());
            for (NZobSb schubeda : internalSchutzbedarf) {
                CnATreeElement element = entry.getValue();

                MSchutzbedarfkategTxt vertr = vampire.findSchutzbedarfNameForId(schubeda.getZsbVertrSbkId());
                MSchutzbedarfkategTxt verfu = vampire.findSchutzbedarfNameForId(schubeda.getZsbVerfuSbkId());
                MSchutzbedarfkategTxt integ = vampire.findSchutzbedarfNameForId(schubeda.getZsbIntegSbkId());

                int vertraulichkeit = (vertr != null) ? transferData.translateSchutzbedarf(vertr.getName()) : Schutzbedarf.UNDEF;

                int verfuegbarkeit = (verfu != null) ? transferData.translateSchutzbedarf(verfu.getName()) : Schutzbedarf.UNDEF;

                int integritaet = (integ != null) ? transferData.translateSchutzbedarf(integ.getName()) : Schutzbedarf.UNDEF;

                String vertrBegruendung = schubeda.getZsbVertrBegr();
                String verfuBegruendung = schubeda.getZsbVerfuBegr();
                String integBegruendung = schubeda.getZsbIntegBegr();

                Short isPersonenbezogen = schubeda.getZsbPersDaten();
                if (isPersonenbezogen == null) {
                    isPersonenbezogen = 0;
                }

                ImportTransferSchutzbedarf command = new ImportTransferSchutzbedarf(element, vertraulichkeit, verfuegbarkeit, integritaet, vertrBegruendung, verfuBegruendung, integBegruendung, isPersonenbezogen);
                command = ServiceFactory.lookupCommandService().executeCommand(command);
            }
        }

    }

    private void importZielobjektVerknuepfungen() {
        if (!this.zielObjekteZielobjekte) {
            return;
        }

        monitor.beginTask("Importiere Verknüpfungen von Zielobjekten...", alleZielobjekte.size());
        Set<NZielobjekt> allElements = alleZielobjekte.keySet();
        for (NZielobjekt zielobjekt : allElements) {
            monitor.worked(1);
            CnATreeElement dependant = alleZielobjekte.get(zielobjekt);
            List<NZielobjekt> dependencies = vampire.findLinksByZielobjekt(zielobjekt);
            for (NZielobjekt dependency : dependencies) {
                monitor.subTask(dependant.getTitle());
                CnATreeElement dependencyElement = findZielobjektFor(dependency);
                if (dependencyElement == null) {
                    LOG.debug("Kein Ziel gefunden für Verknüpfung " + dependency.getName());
                    continue;
                }
                LOG.debug("Neue Verknüpfung von " + dependant.getTitle() + " zu " + dependencyElement.getTitle());

                // verinice models dependencies DOWN, not UP as the gstool.
                // therefore we need to turn things around, except for persons,
                // networks and itverbund
                // (look at it in the tree and it will make sense):
                CnATreeElement from;
                CnATreeElement to;
                if (dependencyElement instanceof Person || dependencyElement instanceof NetzKomponente || dependant instanceof ITVerbund) {
                    from = dependant;
                    to = dependencyElement;
                } else {
                    from = dependencyElement;
                    to = dependant;
                }

                try {
                    CnAElementHome.getInstance().createLink(from, to);
                } catch (Exception e) {
                    LOG.debug("Saving link failed."); //$NON-NLS-1$
                }
            }
        }
    }

    private CnATreeElement findZielobjektFor(NZielobjekt dependency) {
        for (NZielobjekt zielobjekt : alleZielobjekte.keySet()) {
            if (zielobjekt.getId().equals(dependency.getId())) {
                return alleZielobjekte.get(zielobjekt);
            }
        }
        return null;
    }

    private void importMassnahmenVerknuepfungen() {
        if (!this.massnahmenPersonen || !this.importBausteine) {
            return;
        }

        monitor.beginTask("Verknüpfe verantwortliche Ansprechpartner mit Massnahmen...", alleMassnahmen.size());
        for (ModZobjBstMass obm : alleMassnahmen.keySet()) {
            monitor.worked(1);
            monitor.subTask(alleMassnahmen.get(obm).getTitle());
            // transferiere individuell verknüpfte verantowrtliche in massnahmen
            // (TAB "Verantwortlich" im GSTOOL):
            Set<NZielobjekt> personenSrc = vampire.findVerantowrtlicheMitarbeiterForMassnahme(obm.getId());
            if (personenSrc != null && personenSrc.size() > 0) {
                List<Person> dependencies = findPersonen(personenSrc);
                if (dependencies.size() != personenSrc.size()) {
                    LOG.debug("ACHTUNG: Es wurde mindestens eine Person für die zu verknüpfenden Verantwortlichen nicht gefunden.");
                }
                MassnahmenUmsetzung dependantMassnahme = alleMassnahmen.get(obm);
                for (Person personToLink : dependencies) {
                    LOG.debug("Verknüpfe Massnahme " + dependantMassnahme.getTitle() + " mit Person " + personToLink.getTitle());
                    dependantMassnahme.addUmsetzungDurch(personToLink);
                }
            }
        }
    }

    private void importBausteinPersonVerknuepfungen() {
        if (!this.bausteinPersonen || !this.importBausteine) {
            return;
        }

        monitor.beginTask("Verknüpfe befragte Personen mit Bausteinen...", alleBausteineToBausteinUmsetzungMap.size());
        Set<MbBaust> keySet = alleBausteineToBausteinUmsetzungMap.keySet();
        for (MbBaust mbBaust : keySet) {
            monitor.worked(1);
            BausteinUmsetzung bausteinUmsetzung = alleBausteineToBausteinUmsetzungMap.get(mbBaust);
            if (bausteinUmsetzung != null) {

                NZielobjekt interviewer = alleBausteineToZoBstMap.get(mbBaust).getNZielobjektByFkZbZ2();
                if (interviewer != null) {
                    HashSet<NZielobjekt> set = new HashSet<NZielobjekt>();
                    set.add(interviewer);
                    List<Person> personen = findPersonen(set);
                    if (personen != null && personen.size() > 0) {
                        LOG.debug("Befragung für Baustein " + bausteinUmsetzung.getTitle() + " durchgeführt von " + personen.get(0));
                        bausteinUmsetzung.addBefragungDurch(personen.get(0));
                    }
                }

                Set<NZielobjekt> befragteMitarbeiter = vampire.findBefragteMitarbeiterForBaustein(alleBausteineToZoBstMap.get(mbBaust).getId());
                if (befragteMitarbeiter != null && befragteMitarbeiter.size() > 0) {
                    List<Person> dependencies = findPersonen(befragteMitarbeiter);
                    if (dependencies.size() != befragteMitarbeiter.size()) {
                        LOG.debug("ACHTUNG: Es wurde mindestens eine Person für die " + "zu verknüpfenden Interviewpartner nicht gefunden.");
                    }
                    monitor.subTask(bausteinUmsetzung.getTitle());
                    for (Person personToLink : dependencies) {
                        LOG.debug("Verknüpfe Baustein " + bausteinUmsetzung.getTitle() + " mit befragter Person " + personToLink.getTitle());
                        bausteinUmsetzung.addBefragtePersonDurch(personToLink);
                    }
                }
            }
        }
    }

    private List<Person> findPersonen(Set<NZielobjekt> personen) {
        List<Person> result = new ArrayList<Person>();
        alleZielobjekte: for (NZielobjekt nzielobjekt : personen) {
            for (Person person : allePersonen) {
                if (person.getKuerzel().equals(nzielobjekt.getKuerzel()) && person.getErlaeuterung().equals(nzielobjekt.getBeschreibung())) {
                    result.add(person);
                    continue alleZielobjekte;
                }
            }
        }
        return result;
    }

    private void createBausteine(String sourceId, CnATreeElement element, NZielobjekt zielobjekt) throws CommandException {
        if (!importBausteine) {
            return;
        }

        List<BausteineMassnahmenResult> findBausteinMassnahmenByZielobjekt = vampire.findBausteinMassnahmenByZielobjekt(zielobjekt);

        Map<MbBaust, List<BausteineMassnahmenResult>> bausteineMassnahmenMap = transferData.convertBausteinMap(findBausteinMassnahmenByZielobjekt);

        this.monitor.subTask("Erstelle " + zielobjekt.getName() + " mit " + bausteineMassnahmenMap.keySet().size() + " Bausteinen und " + getAnzahlMassnahmen(bausteineMassnahmenMap) + " Massnahmen...");

        ImportCreateBausteine command;
        ServiceFactory.lookupAuthService();
        if (!ServiceFactory.isPermissionHandlingNeeded()) {
            command = new ImportCreateBausteine(sourceId, element, bausteineMassnahmenMap, zeiten, kosten, importUmsetzung, new BSIConfigurationRCPLocal());
        } else {
            command = new ImportCreateBausteine(sourceId, element, bausteineMassnahmenMap, zeiten, kosten, importUmsetzung);
        }

        command = ServiceFactory.lookupCommandService().executeCommand(command);

        if (command.getAlleBausteineToBausteinUmsetzungMap() != null) {
            this.alleBausteineToBausteinUmsetzungMap.putAll(command.getAlleBausteineToBausteinUmsetzungMap());
        }

        if (command.getAlleBausteineToZoBstMap() != null) {
            this.alleBausteineToZoBstMap.putAll(command.getAlleBausteineToZoBstMap());
        }

        if (command.getAlleMassnahmen() != null) {
            this.alleMassnahmen.putAll(command.getAlleMassnahmen());
        }

    }

    private int getAnzahlMassnahmen(Map<MbBaust, List<BausteineMassnahmenResult>> bausteineMassnahmenMap) {
        Set<MbBaust> keys = bausteineMassnahmenMap.keySet();
        int result = 0;
        for (MbBaust baust : keys) {
            result += bausteineMassnahmenMap.get(baust).size();
        }
        return result;
    }
}
