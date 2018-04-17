/*******************************************************************************
 * Copyright (c) 2015 Alexander Koderman <ak@sernet.de>.
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

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Preferences;

import sernet.gs.model.Baustein;
import sernet.gs.model.Gefaehrdung;
import sernet.gs.model.IGSModel;
import sernet.gs.reveng.importData.GSDBConstants;
import sernet.gs.reveng.importData.GSVampire;
import sernet.gs.reveng.importData.RAGefaehrdungenResult;
import sernet.gs.reveng.importData.RAGefaehrdungsMassnahmenResult;
import sernet.gs.reveng.importData.ZielobjektTypeResult;
import sernet.gs.scraper.GSScraper;
import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.OwnGefaehrdungHome;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.RisikoMassnahmeHome;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.RisikoMassnahmenUmsetzungFactory;
import sernet.gs.ui.rcp.main.bsi.views.BSIKatalogInvisibleRoot;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
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
import sernet.verinice.service.commands.risk.AddMassnahmeToGefaherdung;
import sernet.verinice.service.commands.risk.AssociateGefaehrdungsUmsetzung;
import sernet.verinice.service.commands.risk.NegativeEstimateGefaehrdung;
import sernet.verinice.service.commands.risk.SelectRiskTreatment;
import sernet.verinice.service.commands.risk.StartNewRiskAnalysis;
import sernet.verinice.service.gstoolimport.TransferData;
import sernet.verinice.service.parser.GSScraperUtil;

/**
 * Import Risikoanalysen (RA) for existing zielobjekt elements. In GSTOOL a RA is called
 * Ergaenzende Sicherheitsanalyse (ESA).
 * 
 * First we ask the GSTOOL database for all zielobjekte with RAs, then we try to find
 * matching cnatreeelements in the verinice database. These must have been
 * created by a previous import run from the same GSTOOL database. If a match is
 * found, the RA subtree will be transferred.
 * 
 * @author koderman@sernet.de
 * @author dm@sernet.de
 *
 */
public class ImportRisikoanalysenTask extends AbstractGstoolImportTask {

    private static final Logger LOG = Logger.getLogger(ImportRisikoanalysenTask.class);
    
    private IProgress monitor;
    private TransferData transferData;
    private int numberOfRAs;
    
    private String sourceID;
    private FinishedRiskAnalysis riskAnalysis;
    private FinishedRiskAnalysisLists riskAnalysisLists;
    private Map<String, Gefaehrdung> allCreatedOwnGefaehrdungen;
    private Map<String, RisikoMassnahme> allCreatedOwnMassnahmen;

    // Map of gefhrdungs-title : gefaehrdungs-object
    private Map<String, Gefaehrdung> allBsiGefaehrdungen = new HashMap<String, Gefaehrdung>();

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

    public void executeTask(int importType, IProgress monitor) throws Exception {
        loadGefaehrdungen();
        loadMassnahmen();
       
        Preferences prefs = Activator.getDefault().getPluginPreferences();
        String sourceDbUrl = prefs.getString(PreferenceConstants.GS_DB_URL);
        if (sourceDbUrl.indexOf("odbc") > -1) {
            throw new DBException("Kann nicht direkt aus MDB-Datei importieren. Datenbank vorher anhängen in Menü \"Bearbeiten, Einstellungen\".");
        }

        this.monitor = monitor;

        transferData = new TransferData(getGstoolDao(), false);
        importRisikoanalysen();
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
        List<ZielobjektTypeResult> allZielobjekte = getGstoolDao().findZielobjektWithRA();
        numberOfRAs = allZielobjekte.size();
        monitor.beginTask("Importiere Risikoanalysen...", numberOfRAs);
        int i = 1;
        for (ZielobjektTypeResult zielobjekt : allZielobjekte) {
            importRisikoanalyse(zielobjekt, i);
            i++;
        }
    }

    private void importRisikoanalyse(ZielobjektTypeResult zielobjekt, int i) throws CommandException, SQLException, IOException {
        String name = zielobjekt.zielobjekt.getName();
        monitor.worked(1);
        monitor.subTask(i + "/" + numberOfRAs + " - Zielobjekt: " + name);

        CnATreeElement element = findCnaTreeElementByGstoolGuid(zielobjekt.zielobjekt.getGuid());
        if (element == null) {
            return;
        }

        // Now create the risk analysis object and add all gefaehrdungen to it:
        List<RAGefaehrdungenResult> gefaehrdungenForZielobjekt = getGstoolDao().findRAGefaehrdungenForZielobjekt(zielobjekt.zielobjekt);
        if (gefaehrdungenForZielobjekt == null || gefaehrdungenForZielobjekt.size() == 0) {
            // TODO adjust this to ensure not importing empty riskanalysises
            // if element.risikoanalyse.entbehrlich = true, then skip
            if(LOG.isDebugEnabled()){
                LOG.debug("No gefaehrungen found, not creating risk analysis object for " + zielobjekt.zielobjekt.getName());
            }
            return;
        }

        // make sure that list of standard bsi gefaehrdungen and massnahmen as
        // sources for own instances is loaded:
        if (allBsiGefaehrdungen == null || allBsiGefaehrdungen.size() == 0) {
            loadAllBSIGefaehrdungen();
        }

        // create risk analysis object
        StartNewRiskAnalysis startRiskAnalysis = new StartNewRiskAnalysis(element);
        startRiskAnalysis = ServiceFactory.lookupCommandService().executeCommand(startRiskAnalysis);
        riskAnalysis = startRiskAnalysis.getFinishedRiskAnalysis();
        riskAnalysisLists = startRiskAnalysis.getFinishedRiskLists();

        importGefaehrdungen(zielobjekt, element, gefaehrdungenForZielobjekt);
        
        // update risk analysis with newly created children:
        CnAElementHome.getInstance().update(riskAnalysis);
    }
    


    private void importGefaehrdungen(ZielobjektTypeResult zielobjekt, CnATreeElement element, List<RAGefaehrdungenResult> gefaehrdungenForZielobjekt) throws SQLException, IOException, CommandException {
        for (RAGefaehrdungenResult gefaehrdungenResult : gefaehrdungenForZielobjekt) {           
            Gefaehrdung gefaehrdung = createGefaehrdung(gefaehrdungenResult);        
            if (gefaehrdung == null) {
                continue;
            }

            // attach "newGef" to this risk analysis:
            GefaehrdungsUmsetzung gefaehrdungsUmsetzung = addGefaehrdungToRiskAnalysis(gefaehrdungenResult, gefaehrdung);
            if(gefaehrdungsUmsetzung == null){
                continue;
            }
            if(!gefaehrdungsUmsetzung.getOkay()) {
                gefaehrdungsUmsetzung = setNegativeEstimated(gefaehrdungsUmsetzung);
            }
            
            // only if "risikobehandlung" is alternative_a there can be
            // "massnahmen" linked to it:
            if (gefaehrdungenResult.getRisikobehandlungABCD() == GSDBConstants.RA_BEHAND_A_REDUKTION) {
                if(LOG.isDebugEnabled()){
                    LOG.debug("Loading massnahmen for gefaehrdung " + gefaehrdungsUmsetzung.getTitle());
                }
                List<RAGefaehrdungsMassnahmenResult> ragmResults = getGstoolDao().findRAGefaehrdungsMassnahmenForZielobjekt(zielobjekt.zielobjekt, gefaehrdungenResult.getGefaehrdung());
                for (RAGefaehrdungsMassnahmenResult massnahmenResult : ragmResults) {
                    importMassnahme(element, massnahmenResult, gefaehrdungsUmsetzung);
                }
            }
        }
    }
    
    private Gefaehrdung createGefaehrdung(RAGefaehrdungenResult gefaehrdungenResult) throws SQLException, IOException, CommandException {
        // create new gefaehrdungsumsetzung and save it for this risk
        // analysis:
        // first, create or find matching "gefaehrdung" as a source for this
        // "gefaehrdungsumsetzung":
        Gefaehrdung gefaehrdung = null;
        if (TransferData.isUserDefGefaehrdung(gefaehrdungenResult.getGefaehrdung())) {
            OwnGefaehrdung ownGefaehrdung = new OwnGefaehrdung();
            TransferData.transferOwnGefaehrdung(ownGefaehrdung, gefaehrdungenResult);
            // avoid doubles, check if gefaehrdung with same name already
            // created:
            String cacheId = createOwnGefaehrdungsCacheId(ownGefaehrdung);
            if (allCreatedOwnGefaehrdungen.containsKey(cacheId)) {
                // reuse existing owngefaehrdung:
                gefaehrdung = allCreatedOwnGefaehrdungen.get(cacheId);
            } else {
                // create and save new owngefaehrdung:
                ownGefaehrdung = OwnGefaehrdungHome.getInstance().save(ownGefaehrdung);
                allCreatedOwnGefaehrdungen.put(cacheId, ownGefaehrdung);
                gefaehrdung = ownGefaehrdung;
            }
        } else {
            gefaehrdung = findBsiStandardGefaehrdung(gefaehrdungenResult);
            if (gefaehrdung == null) {
                LOG.error("Keine passende Standard-Gefährdung gefunden in BSI-Katalog für GSTOOL-Gefährdung: " + gefaehrdungenResult.getGefaehrdungTxt().getName());
            }          
        }
        return gefaehrdung;
    }
    
    private GefaehrdungsUmsetzung addGefaehrdungToRiskAnalysis(RAGefaehrdungenResult gefaehrdungenResult, Gefaehrdung gefaehrdung) throws CommandException, SQLException, IOException {
        // attach "newGef" to this risk analysis:
        AssociateGefaehrdungsUmsetzung associateGefaehrdung = new AssociateGefaehrdungsUmsetzung(
                riskAnalysisLists.getDbId(), 
                gefaehrdung, 
                riskAnalysis.getDbId(), 
                GSScraper.CATALOG_LANGUAGE_GERMAN);
        associateGefaehrdung = ServiceFactory.lookupCommandService().executeCommand(associateGefaehrdung);
        GefaehrdungsUmsetzung gefaehrdungsUmsetzung = associateGefaehrdung.getGefaehrdungsUmsetzung();
        TransferData.transferRAGefaehrdungsUmsetzung(gefaehrdungsUmsetzung, gefaehrdungenResult);
        gefaehrdungsUmsetzung.setExtId(GSVampire.generateGefaehrdungsUmsetzungExtid(String.valueOf(gefaehrdungenResult.getGefaehrdung().getId().getGefId()), String.valueOf(gefaehrdungenResult.getZielobjekt().getId().getZobId()), gefaehrdungenResult.getGefaehrdung().getGuid(), gefaehrdungenResult.getZielobjekt().getGuid()));
        CnAElementHome.getInstance().update(gefaehrdungsUmsetzung);
        assert (gefaehrdungsUmsetzung.getExtId() != null);
        return gefaehrdungsUmsetzung;
    }

    private GefaehrdungsUmsetzung setNegativeEstimated(GefaehrdungsUmsetzung gefaehrdungsUmsetzung) throws CommandException {
        NegativeEstimateGefaehrdung negativeEstimateGefaehrdungCommand = new NegativeEstimateGefaehrdung(riskAnalysisLists.getDbId(), gefaehrdungsUmsetzung, riskAnalysis);
        negativeEstimateGefaehrdungCommand = ServiceFactory.lookupCommandService().executeCommand(negativeEstimateGefaehrdungCommand);
        riskAnalysisLists = negativeEstimateGefaehrdungCommand.getRaList();
        gefaehrdungsUmsetzung = negativeEstimateGefaehrdungCommand.getGefaehrdungsUmsetzung();
        
        SelectRiskTreatment selectRiskTreatmentCommand = new SelectRiskTreatment(riskAnalysisLists.getDbId(), riskAnalysis, gefaehrdungsUmsetzung, gefaehrdungsUmsetzung.getAlternative());
        selectRiskTreatmentCommand = ServiceFactory.lookupCommandService().executeCommand(selectRiskTreatmentCommand);
        riskAnalysisLists = selectRiskTreatmentCommand.getFinishedRiskLists();
        return gefaehrdungsUmsetzung;
    }
    
    
    private void importMassnahme(CnATreeElement element, RAGefaehrdungsMassnahmenResult massnahmenResult, GefaehrdungsUmsetzung gefaehrdungsUmsetzung) throws SQLException, IOException, CommandException {
        RisikoMassnahmenUmsetzung risikoMassnahme = null;
        if (TransferData.isUserDefMassnahme(massnahmenResult)) {
            risikoMassnahme = importUserMassnahme(element, massnahmenResult, gefaehrdungsUmsetzung);          
        } else {
            risikoMassnahme = importBsiMassnahme(element, massnahmenResult, gefaehrdungsUmsetzung);          
        }
        if(risikoMassnahme!=null) {
            AddMassnahmeToGefaherdung command = new AddMassnahmeToGefaherdung(gefaehrdungsUmsetzung, risikoMassnahme);
            command = ServiceFactory.lookupCommandService().executeCommand(command);
            risikoMassnahme = command.getChild();
        }

    }

    private RisikoMassnahmenUmsetzung importUserMassnahme(CnATreeElement element, RAGefaehrdungsMassnahmenResult massnahmenResult, GefaehrdungsUmsetzung gefaehrdungsUmsetzung) throws SQLException, IOException, CommandException {
        RisikoMassnahmenUmsetzung risikoMassnahme;
        risikoMassnahme = new RisikoMassnahmenUmsetzung(element, gefaehrdungsUmsetzung);
        TransferData.transferRAGefaehrdungsMassnahmen(massnahmenResult, risikoMassnahme);
        if(LOG.isDebugEnabled()){
            LOG.debug("Transferred user defined massnahme: " + risikoMassnahme.getTitle());
        }
        RisikoMassnahme newRisikoMassnahme = new RisikoMassnahme();
        newRisikoMassnahme.setNumber(risikoMassnahme.getKapitel());
        newRisikoMassnahme.setName(risikoMassnahme.getName());

        newRisikoMassnahme.setDescription(TransferData.convertClobToStringEncodingSave(massnahmenResult.getMassnahmeTxt().getBeschreibung(), GSScraperUtil.getInstance().getModel().getEncoding()));

        String key = createOwnMassnahmeCacheId(newRisikoMassnahme);
        if (!allCreatedOwnMassnahmen.containsKey(key)) {
            // create and save new RisikoMassnahme to database of user
            // defined massnahmen:
            if(LOG.isDebugEnabled()){
                LOG.debug("Saving new user defined massnahme to database.");
            }
            RisikoMassnahmeHome.getInstance().save(newRisikoMassnahme);
            allCreatedOwnMassnahmen.put(key, newRisikoMassnahme);
        }
        if(LOG.isDebugEnabled()){
            LOG.debug("Transferred user defined massnahme: " + risikoMassnahme.getTitle());
        }
        return risikoMassnahme;
    }
    
    private RisikoMassnahmenUmsetzung importBsiMassnahme(CnATreeElement element, RAGefaehrdungsMassnahmenResult massnahmenResult, GefaehrdungsUmsetzung gefaehrdungsUmsetzung) throws SQLException, IOException {
        RisikoMassnahmenUmsetzung risikoMassnahme;
        MassnahmenUmsetzung newMnUms = new MassnahmenUmsetzung(gefaehrdungsUmsetzung);
        transferData.transferRAGefaehrdungsMassnahmen(massnahmenResult, newMnUms);
        if(LOG.isDebugEnabled()){
            LOG.debug("Transferred BSI-standard massnahme: " + newMnUms.getTitle());
        }
        risikoMassnahme = RisikoMassnahmenUmsetzungFactory.buildFromMassnahmenUmsetzung(newMnUms, element, null);
        return risikoMassnahme;
    }

    private Gefaehrdung findBsiStandardGefaehrdung(RAGefaehrdungenResult ragResult) {
        String gefName = ragResult.getGefaehrdungTxt().getName();
        return allBsiGefaehrdungen.get(gefName);
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

    private void loadAllBSIGefaehrdungen() {
        if(LOG.isDebugEnabled()){
            LOG.debug("Caching all BSI standard Gefaehrdungen from catalog...");
        }
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
            if (LOG.isDebugEnabled()) {
                LOG.debug("NOT found: CnaTreeElmt with sourceID " + sourceID + " and extID " + guid + "...");
            }
            return null;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Found: CnaTreeElmt with sourceID " + sourceID + " and extID " + guid + "...");
        }
        return elementList.get(0);
    }

}
