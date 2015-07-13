/*******************************************************************************
 * Copyright (c) 2010 Alexander Koderman <ak@sernet.de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details.
 *     You should have received a copy of the GNU General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Alexander Koderman <ak@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.gsimport;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Preferences;
import org.hibernate.Hibernate;

import sernet.gs.model.Baustein;
import sernet.gs.model.Gefaehrdung;
import sernet.gs.model.IGSModel;
import sernet.gs.reveng.importData.ESAResult;
import sernet.gs.reveng.importData.GSDBConstants;
import sernet.gs.reveng.importData.GSVampire;
import sernet.gs.reveng.importData.RAGefaehrdungenResult;
import sernet.gs.reveng.importData.RAGefaehrdungsMassnahmenResult;
import sernet.gs.reveng.importData.ZielobjektTypeResult;
import sernet.gs.scraper.GSScraper;
import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.CnAWorkspace;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.OwnGefaehrdungHome;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.RisikoMassnahmeHome;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.RisikoMassnahmenUmsetzungFactory;
import sernet.gs.ui.rcp.main.bsi.views.BSIKatalogInvisibleRoot;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.taskcommands.riskanalysis.AddMassnahmeToGefaherdung;
import sernet.gs.ui.rcp.main.service.taskcommands.riskanalysis.AssociateGefaehrdungsUmsetzung;
import sernet.gs.ui.rcp.main.service.taskcommands.riskanalysis.NegativeEstimateGefaehrdung;
import sernet.gs.ui.rcp.main.service.taskcommands.riskanalysis.SelectRiskTreatment;
import sernet.gs.ui.rcp.main.service.taskcommands.riskanalysis.StartNewRiskAnalysis;
import sernet.snutils.DBException;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.model.bsi.MassnahmenUmsetzung;
import sernet.verinice.model.bsi.risikoanalyse.FinishedRiskAnalysis;
import sernet.verinice.model.bsi.risikoanalyse.FinishedRiskAnalysisLists;
import sernet.verinice.model.bsi.risikoanalyse.GefaehrdungsUmsetzung;
import sernet.verinice.model.bsi.risikoanalyse.OwnGefaehrdung;
import sernet.verinice.model.bsi.risikoanalyse.RisikoMassnahme;
import sernet.verinice.model.bsi.risikoanalyse.RisikoMassnahmenUmsetzung;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.service.commands.LoadCnAElementByExternalID;

/**
 * Import risikoanalysen for existing zielobjekt elements, using the saved GUID
 * from the extId field.
 * 
 * First we ask the GSTOOL database for all zielobjekte, then we try to find
 * matching cnatreeelements in the verinice database. These must have been
 * created by a previous import run from the same GSTOOL database. If a match is
 * found, the "risikoanalyse" subtree will be transferred.
 * 
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ $LastChangedBy$
 *
 */
public class ImportRisikoanalysenTask {

    private IProgress monitor;
    private GSVampire vampire;
    private TransferData transferData;

    private String sourceID;
    private Map<String, Gefaehrdung> allCreatedOwnGefaehrdungen;
    private Map<String, RisikoMassnahme> allCreatedOwnMassnahmen;

    // Map of gefhrdungs-title : gefaehrdungs-object
    private Map<String, Gefaehrdung> allBsiGefaehrdungen = new HashMap<String, Gefaehrdung>();

    private static final Logger LOG = Logger.getLogger(ImportRisikoanalysenTask.class);

    /**
     * @param sourceID
     *            the sourceID of all existing CnaTreeElements (from a previous
     *            GSTOOL import) for which the "risikoanalyse" tree should be
     *            migrated from the same GSTOOL database.
     */
    public ImportRisikoanalysenTask(String sourceID) {
        super();
        this.sourceID = sourceID;
        this.allCreatedOwnGefaehrdungen = new HashMap<String, Gefaehrdung>();
        this.allCreatedOwnMassnahmen = new HashMap<String, RisikoMassnahme>();
    }

    public void execute(int importType, IProgress monitor) throws DBException, CommandException, SQLException, IOException {
        loadGefaehrdungen();
        loadMassnahmen();
        
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(Hibernate.class.getClassLoader());

        Preferences prefs = Activator.getDefault().getPluginPreferences();
        String sourceDbUrl = prefs.getString(PreferenceConstants.GS_DB_URL);
        if (sourceDbUrl.indexOf("odbc") > -1) {
            throw new DBException("Kann nicht direkt aus MDB Datei importieren. Datenbank vorher anhängen in Menü \"Bearbeiten, Einstellungen\".");
        }

        this.monitor = monitor;
        File conf = new File(CnAWorkspace.getInstance().getConfDir() + File.separator + "hibernate-vampire.cfg.xml");
        vampire = new GSVampire(conf.getAbsolutePath());

        transferData = new TransferData(vampire, false);
        importRisikoanalysen();

        // Set back the original context class loader.
        Thread.currentThread().setContextClassLoader(cl);

        CnAElementFactory.getInstance().reloadModelFromDatabase();
    }

    private void loadGefaehrdungen() throws CommandException {
        List<OwnGefaehrdung> allGefaehrdungen = OwnGefaehrdungHome.getInstance().loadAll();
        for (OwnGefaehrdung ownGefaehrdung : allGefaehrdungen) {
            String cacheId = createOwnGefaehrdungsCacheId(ownGefaehrdung);
            this.allCreatedOwnGefaehrdungen.put(cacheId, ownGefaehrdung);
        }
    }
    
    private void loadMassnahmen() throws CommandException {
        List<RisikoMassnahme> allMassnahmen = RisikoMassnahmeHome.getInstance().loadAll();
        for (RisikoMassnahme massnahme : allMassnahmen) {
            String cacheId = createOwnMassnahmeCacheId(massnahme);
            this.allCreatedOwnMassnahmen.put(cacheId, massnahme);
        }
    }

    /**
     * Import risikoanalysen for existing zielobjekt elements, using the saved
     * GUID from the extId field.
     * 
     * First we ask the GSTOOL database for all zielobjekte, then we try to find
     * matching cnatreeelements in the verinice database. These must have been
     * created by a previous import run from the same GSTOOL database. If a
     * match is found, the "risikoanalyse" subtree will be transferred.
     * 
     * @throws CommandException
     * @throws IOException
     * @throws SQLException
     */
    private void importRisikoanalysen() throws CommandException, SQLException, IOException {
        List<ZielobjektTypeResult> allZielobjekte = vampire.findZielobjektTypAll();
        monitor.beginTask("Importiere alle Risikoanalysen für...", allZielobjekte.size());
        for (ZielobjektTypeResult zielobjekt : allZielobjekte) {
            importRisikoanalyse(zielobjekt);
        }
    }

    private void importRisikoanalyse(ZielobjektTypeResult zielobjekt) throws CommandException, SQLException, IOException {
        String name = zielobjekt.zielobjekt.getName();
        monitor.worked(1);
        monitor.subTask(name);

        CnATreeElement element = importEsa(zielobjekt);
        if (element == null) {
            return;
        }

        // Now create the risk analysis object and add all gefaehrdungen to it:
        List<RAGefaehrdungenResult> gefaehrdungenForZielobjekt = vampire.findRAGefaehrdungenForZielobjekt(zielobjekt.zielobjekt);
        if (gefaehrdungenForZielobjekt == null || gefaehrdungenForZielobjekt.size() == 0) {
            LOG.debug("No gefaehrungen found, not creating risk analysis object for " + zielobjekt.zielobjekt.getName());
            return;
        }

        // make sure that list of standard bsi gefaehrdungen and massnahmen as
        // sources for own instances is loaded:
        if (allBsiGefaehrdungen == null || allBsiGefaehrdungen.size() == 0) {
            loadAllBSIGefaehrdungen();
        }

        // create risk analysis object and start adding gefaehrdungen to it:
        StartNewRiskAnalysis command = new StartNewRiskAnalysis(element);
        command = ServiceFactory.lookupCommandService().executeCommand(command);
        FinishedRiskAnalysis newRiskAnalysis = command.getFinishedRiskAnalysis();
        FinishedRiskAnalysisLists finishedRiskLists = command.getFinishedRiskLists();

        alleGefaehrdungen: for (RAGefaehrdungenResult gefaehrdungenResult : gefaehrdungenForZielobjekt) {
            // create new gefaehrdungsumsetzung and save it for this risk
            // analysis:
            // first, create or find matching "gefaehrdung" as a source for this
            // "gefaehrdungsumsetzung":
            Gefaehrdung gefaehrdung = null;
            if (transferData.isUserDefGefaehrdung(gefaehrdungenResult.getGefaehrdung())) {
                OwnGefaehrdung ownGefaehrdung = new OwnGefaehrdung();
                transferData.transferOwnGefaehrdung(ownGefaehrdung, gefaehrdungenResult);
                // avoid doubles, check if gefaehrdung with same name already
                // created:
                String cacheId = createOwnGefaehrdungsCacheId(ownGefaehrdung);
                if (allCreatedOwnGefaehrdungen.containsKey(cacheId)) {
                    // reuse existing owngefaehrdung:
                    gefaehrdung = allCreatedOwnGefaehrdungen.get(cacheId);
                } else {
                    // create and save new owngefaehrdung:
                    OwnGefaehrdungHome.getInstance().save(ownGefaehrdung);
                    allCreatedOwnGefaehrdungen.put(cacheId, ownGefaehrdung);
                    gefaehrdung = ownGefaehrdung;
                }
            } else {
                gefaehrdung = findBsiStandardGefaehrdung(gefaehrdungenResult);
                if (gefaehrdung == null) {
                    LOG.error("Keine passende Standard-Gefährdung gefunden in BSI-Katalog für GSTOOL-Gefährdung: " + gefaehrdungenResult.getGefaehrdungTxt().getName());
                    continue alleGefaehrdungen;
                }
            }

            // attach "newGef" to this risk analysis:
            AssociateGefaehrdungsUmsetzung command2 = new AssociateGefaehrdungsUmsetzung(finishedRiskLists.getDbId(), gefaehrdung, newRiskAnalysis.getDbId(), GSScraper.CATALOG_LANGUAGE_GERMAN);
            command2 = ServiceFactory.lookupCommandService().executeCommand(command2);
            GefaehrdungsUmsetzung gefaehrdungsUmsetzung = command2.getGefaehrdungsUmsetzung();
            transferData.transferRAGefaehrdungsUmsetzung(gefaehrdungsUmsetzung, gefaehrdungenResult);
            CnAElementHome.getInstance().update(gefaehrdungsUmsetzung);

            if(!gefaehrdungsUmsetzung.getOkay()) {
                NegativeEstimateGefaehrdung negativeEstimateGefaehrdungCommand = new NegativeEstimateGefaehrdung(finishedRiskLists.getDbId(), gefaehrdungsUmsetzung, newRiskAnalysis);
                negativeEstimateGefaehrdungCommand = ServiceFactory.lookupCommandService().executeCommand(negativeEstimateGefaehrdungCommand);
                finishedRiskLists = negativeEstimateGefaehrdungCommand.getRaList();
                gefaehrdungsUmsetzung = negativeEstimateGefaehrdungCommand.getGefaehrdungsUmsetzung();
                
                SelectRiskTreatment selectRiskTreatmentCommand = new SelectRiskTreatment(finishedRiskLists.getDbId(), newRiskAnalysis, gefaehrdungsUmsetzung, gefaehrdungsUmsetzung.getAlternative());
                selectRiskTreatmentCommand = ServiceFactory.lookupCommandService().executeCommand(selectRiskTreatmentCommand);
                finishedRiskLists = command.getFinishedRiskLists();
            }
            
            // FIXME fill remaining finishedRiskLists with the above created
            // "gefaehrdung" objects!!
            // finishedRiskLists.setAssociatedGefaehrdungen(); // should be
            // done, is added by the command above!
            // finishedRiskLists.setAllGefaehrdungsUmsetzungen();
            // finishedRiskLists.setNotOKGefaehrdungsUmsetzungen();

            // only if "risikobehandlung" is alternative_a there can be
            // "massnahmen" linked to it:
            if (gefaehrdungenResult.getRisikobehandlungABCD() == GSDBConstants.RABEHAND_A_CHAR) {
                LOG.debug("Loading massnahmen for gefaehrdung " + gefaehrdungsUmsetzung.getTitle());
                List<RAGefaehrdungsMassnahmenResult> ragmResults = vampire.findRAGefaehrdungsMassnahmenForZielobjekt(zielobjekt.zielobjekt, gefaehrdungenResult.getGefaehrdung());
                for (RAGefaehrdungsMassnahmenResult massnahmenResult : ragmResults) {
                    importMassnahme(element, massnahmenResult, gefaehrdungsUmsetzung);
                }
            }
        }

        LOG.debug("Updating newly filled risikoanalyse for zielobjekt " + element.getTitle());
        // update risk analysis with newly created children:
        CnAElementHome.getInstance().update(newRiskAnalysis);
    }

    private CnATreeElement importEsa(ZielobjektTypeResult zielobjekt) throws CommandException {
        CnATreeElement element = null;

        // First transfer the EAS fields into the previously created
        // cnatreeelmt:
        List<ESAResult> esaResult = vampire.findESAByZielobjekt(zielobjekt.zielobjekt);

        if (esaResult == null || esaResult.size() == 0) {
            LOG.debug("No ESA found for zielobjekt" + zielobjekt.zielobjekt.getName());
            return element;
        }
        if (esaResult.size() > 1) {
            LOG.debug("Warning: More than one ESA found for zielobjekt" + zielobjekt.zielobjekt.getName() + " Using first one only.");
        }

        LOG.debug("ESA found for zielobjekt " + zielobjekt.zielobjekt.getName());
        element = findCnaTreeElementByGstoolGuid(zielobjekt.zielobjekt.getGuid());
        if (element == null) {
            LOG.debug("No matching CnaTreeElement to migrate ESA for zielobjekt " + zielobjekt.zielobjekt.getName());
            return element;
        }
        transferData.transferESA(element, esaResult.get(0));
        CnAElementHome.getInstance().update(element);
        return element;
    }

    private void importMassnahme(CnATreeElement element, RAGefaehrdungsMassnahmenResult massnahmenResult, GefaehrdungsUmsetzung gefaehrdungsUmsetzung) throws SQLException, IOException, CommandException {
        RisikoMassnahmenUmsetzung risikoMassnahme = null;
        if (transferData.isUserDefMassnahme(massnahmenResult)) {
            risikoMassnahme = new RisikoMassnahmenUmsetzung(element, gefaehrdungsUmsetzung);
            transferData.transferRAGefaehrdungsMassnahmen(massnahmenResult, gefaehrdungsUmsetzung, risikoMassnahme);
            LOG.debug("Transferred user defined massnahme: " + risikoMassnahme.getTitle());

            RisikoMassnahme newRisikoMassnahme = new RisikoMassnahme();
            newRisikoMassnahme.setNumber(risikoMassnahme.getKapitel());
            newRisikoMassnahme.setName(risikoMassnahme.getName());
            newRisikoMassnahme.setDescription(risikoMassnahme.getDescription());

            if (allCreatedOwnMassnahmen.containsKey(createOwnMassnahmeCacheId(newRisikoMassnahme))) {
                // do nothing, user-defined massnahme was already saved to DB
                // reuse existing massnahme:
                // newRisikoMassnahme =
                // allCreatedOwnMassnahmen.get(createOwnMassnahmeCacheId(newRisikoMassnahme));
            } else {
                // create and save new RisikoMassnahme to database of user
                // defined massnahmen:
                LOG.debug("Saving new user defined massnahe to database.");
                RisikoMassnahmeHome.getInstance().save(newRisikoMassnahme);
                allCreatedOwnMassnahmen.put(createOwnMassnahmeCacheId(newRisikoMassnahme), newRisikoMassnahme);
            }
            
        } else {
            MassnahmenUmsetzung newMnUms = new MassnahmenUmsetzung(gefaehrdungsUmsetzung);
            transferData.transferRAGefaehrdungsMassnahmen(massnahmenResult, gefaehrdungsUmsetzung, newMnUms);
            LOG.debug("Transferred BSI-standard massnahme: " + newMnUms.getTitle());
            risikoMassnahme = RisikoMassnahmenUmsetzungFactory.buildFromMassnahmenUmsetzung(newMnUms, element, null);
         
            LOG.debug("Transferred user defined massnahme: " + risikoMassnahme.getTitle());
        }
        if(risikoMassnahme!=null) {
            AddMassnahmeToGefaherdung command = new AddMassnahmeToGefaherdung(gefaehrdungsUmsetzung, risikoMassnahme);
            command = ServiceFactory.lookupCommandService().executeCommand(command);
            risikoMassnahme = command.getChild();
        }
        
    }

    private Gefaehrdung findBsiStandardGefaehrdung(RAGefaehrdungenResult ragResult) {
        String gefName = ragResult.getGefaehrdungTxt().getName();
        return allBsiGefaehrdungen.get(gefName);
    }

    private void loadAllBSIGefaehrdungen() {
        LOG.debug("Caching all BSI standard Gefaehrdungen from catalog...");
        List<Baustein> bausteine = BSIKatalogInvisibleRoot.getInstance().getBausteine();
        for (Baustein baustein : bausteine) {
            if (baustein.getGefaehrdungen() == null) {
                continue;
            }
            for (Gefaehrdung gefaehrdung : baustein.getGefaehrdungen()) {
                Boolean duplicate = false;
                alleTitel: for (IGSModel element : allBsiGefaehrdungen.values()) {
                    if (element.getId().equals(gefaehrdung.getId())) {
                        duplicate = true;
                        break alleTitel;
                    }
                }
                if (!duplicate) {
                    allBsiGefaehrdungen.put(gefaehrdung.getTitel(), gefaehrdung);
                }
            }
        }

    }

    private String createOwnGefaehrdungsCacheId(OwnGefaehrdung ownGefaehrdung) {
        return ownGefaehrdung.getId() + ownGefaehrdung.getTitel() + ownGefaehrdung.getBeschreibung();
    }

    private String createOwnMassnahmeCacheId(RisikoMassnahme riskMn) {
        return riskMn.getId() + riskMn.getName() + riskMn.getDescription();
    }

    private CnATreeElement findCnaTreeElementByGstoolGuid(String guid) throws CommandException {
        LoadCnAElementByExternalID command = new LoadCnAElementByExternalID(sourceID, guid, false, false);
        command.setProperties(true);
        command = ServiceFactory.lookupCommandService().executeCommand(command);
        List<CnATreeElement> elementList = command.getElements();
        if (elementList == null || elementList.size() == 0) {
            LOG.debug("NOT found: CnaTreeElmt with sourceID " + sourceID + " and extID " + guid + "...");
            return null;
        }
        LOG.debug("Found: CnaTreeElmt with sourceID " + sourceID + " and extID " + guid + "...");
        return elementList.get(0);
    }

}
