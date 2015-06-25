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

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Preferences;
import org.hibernate.Hibernate;

import sernet.gs.model.Gefaehrdung;
import sernet.gs.reveng.MbBaust;
import sernet.gs.reveng.importData.ESAResult;
import sernet.gs.reveng.importData.GSDBConstants;
import sernet.gs.reveng.importData.GSVampire;
import sernet.gs.reveng.importData.NotizenMassnahmeResult;
import sernet.gs.reveng.importData.RAGefaehrdungenResult;
import sernet.gs.reveng.importData.RAGefaehrdungsMassnahmenResult;
import sernet.gs.reveng.importData.ZielobjektTypeResult;
import sernet.gs.scraper.GSScraper;
import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.CnAWorkspace;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.GefaehrdungsUmsetzungFactory;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.OwnGefaehrdungHome;
import sernet.gs.ui.rcp.main.bsi.views.BSIKatalogInvisibleRoot;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.taskcommands.ImportNotesForZielobjekt;
import sernet.gs.ui.rcp.main.service.taskcommands.riskanalysis.AssociateGefaehrdungsUmsetzung;
import sernet.gs.ui.rcp.main.service.taskcommands.riskanalysis.StartNewRiskAnalysis;
import sernet.snutils.DBException;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.model.bsi.Client;
import sernet.verinice.model.bsi.MassnahmenUmsetzung;
import sernet.verinice.model.bsi.risikoanalyse.FinishedRiskAnalysis;
import sernet.verinice.model.bsi.risikoanalyse.FinishedRiskAnalysisLists;
import sernet.verinice.model.bsi.risikoanalyse.GefaehrdungsUmsetzung;
import sernet.verinice.model.bsi.risikoanalyse.OwnGefaehrdung;
import sernet.verinice.model.bsi.risikoanalyse.RisikoMassnahmenUmsetzung;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.service.commands.LoadCnAElementByExternalID;

/**
 *   Import risikoanalysen for existing zielobjekt elements, using the saved GUID from the extId field.
     * 
     * First we ask the GSTOOL database for all zielobjekte, then we try to find matching cnatreeelements
     * in the verinice database. These must have been created by a previous import run from the same GSTOOL database.
     * If a match is found, the "risikoanalyse" subtree will be transferred.
     * 
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public class ImportRisikoanalysenTask {
	
	private IProgress monitor;
	private GSVampire vampire;
	private TransferData transferData;
	
	private String sourceID;
    private Map<String, Gefaehrdung> allCreatedOwnGefaehrdungen;
	
	private static final Logger LOG = Logger.getLogger(ImportRisikoanalysenTask.class);

	/**
	
     * 
     * @param sourceID the sourceID of all existing CnaTreeElements (from a previous GSTOOL import) for which
     * the "risikoanalyse" tree should be migrated from the same GSTOOL database.
     * 
     */
    public ImportRisikoanalysenTask(String sourceID) {
        super();
        this.sourceID = sourceID;
        this.allCreatedOwnGefaehrdungen = new HashMap<String, Gefaehrdung>();
    }


    public void execute(int importType, IProgress monitor) throws DBException, CommandException, SQLException, IOException {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(Hibernate.class.getClassLoader());
		
		Preferences prefs = Activator.getDefault().getPluginPreferences();
		String sourceDbUrl = prefs.getString(PreferenceConstants.GS_DB_URL);
		if (sourceDbUrl.indexOf("odbc") > -1) {
			throw new DBException("Kann nicht direkt aus MDB Datei importieren. Datenbank vorher anhängen in Menü \"Bearbeiten, Einstellungen\".");
		}

		this.monitor = monitor;
		File conf = new File(CnAWorkspace.getInstance().getConfDir()
				+ File.separator + "hibernate-vampire.cfg.xml");
		vampire = new GSVampire(conf.getAbsolutePath());
		
		transferData = new TransferData(vampire, false);
		importRisikoanalysen();
		
		// Set back the original context class loader.
		Thread.currentThread().setContextClassLoader(cl);
		
		CnAElementFactory.getInstance().reloadModelFromDatabase();
	}
	

	/**
	 * Import risikoanalysen for existing zielobjekt elements, using the saved GUID from the extId field.
	 * 
	 * First we ask the GSTOOL database for all zielobjekte, then we try to find matching cnatreeelements
	 * in the verinice database. These must have been created by a previous import run from the same GSTOOL database.
	 * If a match is found, the "risikoanalyse" subtree will be transferred.
	 * 
	 * @throws CommandException 
	 * @throws IOException 
	 * @throws SQLException 
	 */
	private void importRisikoanalysen() throws CommandException, SQLException, IOException {

        List<ZielobjektTypeResult> allZielobjekte = vampire.findZielobjektTypAll();
        monitor.beginTask("Importiere alle Risikoanalysen für...", allZielobjekte.size());
        for (ZielobjektTypeResult zielobjekt : allZielobjekte) {
            String name = zielobjekt.zielobjekt.getName();
            monitor.worked(1);
            monitor.subTask(name);
            
            // First transfer the EAS fields into the previously created cnatreeelmt:
            List<ESAResult> esaResult = vampire.findESAByZielobjekt(zielobjekt.zielobjekt);
            
            if (esaResult == null || esaResult.size()==0) {
                LOG.debug("No ESA found for zielobjekt" + zielobjekt.zielobjekt.getName());
                continue;
            }
            if (esaResult.size()>1) {
                LOG.debug("Warning: More than one ESA found for zielobjekt" + zielobjekt.zielobjekt.getName() + " Using first one only.");
            }
            
            LOG.debug("ESA found for zielobjekt " + zielobjekt.zielobjekt.getName());
            CnATreeElement cnaElmt = findCnaTreeElementByGstoolGuid(zielobjekt.zielobjekt.getGuid());
            if (cnaElmt == null) {
                LOG.debug("No matching CnaTreeElement to migrate ESA for zielobjekt " + zielobjekt.zielobjekt.getName());
                continue;
            }
            transferData.transferESA(cnaElmt, esaResult.get(0));

            // Now create the risk analysis object and add all gefaehrdungen to it:
            List<RAGefaehrdungenResult> gefaehrdungenForZielobjekt = vampire.findRAGefaehrdungenForZielobjekt(zielobjekt.zielobjekt);
            if (gefaehrdungenForZielobjekt == null || gefaehrdungenForZielobjekt.size()==0) {
                LOG.debug("No gefaehrungen found, not creating risk analysis object for " + zielobjekt.zielobjekt.getName());
                continue;
            }
            
            // create risk analysis object and start adding gefährdungen to it:
            StartNewRiskAnalysis command = new StartNewRiskAnalysis(cnaElmt);
            command = ServiceFactory.lookupCommandService().executeCommand(command);
            FinishedRiskAnalysis newRiskAnalysis = command.getFinishedRiskAnalysis();
            FinishedRiskAnalysisLists finishedRiskLists = command.getFinishedRiskLists();

            for (RAGefaehrdungenResult ragResult : gefaehrdungenForZielobjekt) {
                // create new gefaehrdungsumsetzung and save it for this risk analysis:
                // first, create or find matching "gefaehrdung" as a source for this "gefaehrdungsumsetzung":
                Gefaehrdung newGef = null;
                if (transferData.isUserDefGefaehrdung(ragResult.getGefaehrdung())) {
                    OwnGefaehrdung ownGefaehrdung = new OwnGefaehrdung();
                    transferData.transferOwnGefaehrdung(ownGefaehrdung, ragResult);
                    // avoid doubles, check if gefaehrdung with same name already created:
                    if (allCreatedOwnGefaehrdungen.keySet().contains(createOwnGefaehrdungsCacheId(ownGefaehrdung))) {
                        // reuse existing owngefaehrdung:
                        newGef = allCreatedOwnGefaehrdungen.get(createOwnGefaehrdungsCacheId(ownGefaehrdung));
                    } else {
                        // create and save new owngefaehrdung:
                        OwnGefaehrdungHome.getInstance().save(ownGefaehrdung);
                        allCreatedOwnGefaehrdungen.put(createOwnGefaehrdungsCacheId(ownGefaehrdung), ownGefaehrdung);
                        newGef = ownGefaehrdung;
                    }
                } else {
                    // FIXME find correct BSI standard gefaehrdung as source:
                    
                }
                
                AssociateGefaehrdungsUmsetzung command2 = new AssociateGefaehrdungsUmsetzung(finishedRiskLists.getDbId(),
                        newGef, newRiskAnalysis.getDbId(), GSScraper.CATALOG_LANGUAGE_GERMAN);
                command2 = ServiceFactory.lookupCommandService().executeCommand(command2);
                GefaehrdungsUmsetzung newGefUms = command2.getGefaehrdungsUmsetzung();
                transferData.transferRAGefaehrdungsUmsetzung(newGefUms, ragResult);
                
                // FIXME fill remaining finishedRiskLists with the above created "gefaehrdung" objects!!
                //finishedRiskLists.setAssociatedGefaehrdungen(); // should be done, is added by the command above!
                //finishedRiskLists.setAllGefaehrdungsUmsetzungen();
                //finishedRiskLists.setNotOKGefaehrdungsUmsetzungen();
                
                // only if "risikobehandlung" is alternative_a there can be "massnahmen" linked to it:
                if (ragResult.getRisikobehandlungABCD() == GSDBConstants.RABEHAND_A_CHAR) {
                    LOG.debug("Loading massnahmen for gefaehrdung " + newGefUms.getTitle());
                    List<RAGefaehrdungsMassnahmenResult> ragmResults = 
                            vampire.findRAGefaehrdungsMassnahmenForZielobjekt(zielobjekt.zielobjekt, 
                                    ragResult.getGefaehrdung());
                    for (RAGefaehrdungsMassnahmenResult ragmResult : ragmResults) {
                        if (transferData.isUserDefMassnahme(ragmResult)) {
                            // FIXME: create and save "Risikomassnahme", check doubles same as with OwnGefaehrdung above
                            RisikoMassnahmenUmsetzung newMnUms = new RisikoMassnahmenUmsetzung(cnaElmt, newGefUms);
                            transferData.transferRAGefaehrdungsMassnahmen(ragmResult, newGefUms, newMnUms);
                            LOG.debug("Transferred user defined massnahme: " + newMnUms.getTitle());
                        } else {
                            MassnahmenUmsetzung newMnUms = new MassnahmenUmsetzung(newGefUms);
                            transferData.transferRAGefaehrdungsMassnahmen(ragmResult, newGefUms, newMnUms);
                            LOG.debug("Transferred BSI-standard massnahme: " + newMnUms.getTitle());
                        }
                    }
                }
            }
            
            LOG.debug("Updating newly filled risikoanalyse for zielobjekt " + cnaElmt.getTitle());
            // update risk analysis with newly created children:
            CnAElementHome.getInstance().update(newRiskAnalysis);
            
        }
	}
	
	private String createOwnGefaehrdungsCacheId(OwnGefaehrdung ownGefaehrdung) {
	    return ownGefaehrdung.getId() + ownGefaehrdung.getTitel() + ownGefaehrdung.getBeschreibung();
	}


    /**
     * @param guid
     * @return
     * @throws CommandException 
     */
    private CnATreeElement findCnaTreeElementByGstoolGuid(String guid) throws CommandException {
        LoadCnAElementByExternalID command = new LoadCnAElementByExternalID(sourceID, guid, false, false);
        command.setProperties(true);
        command = ServiceFactory.lookupCommandService().executeCommand(
                command);
        List<CnATreeElement> elementList = command.getElements();
        if (elementList == null || elementList.size()==0) {
            LOG.debug("NOT found: CnaTreeElmt with sourceID " + sourceID + " and extID " + guid + "...");
            return null;
        }
        LOG.debug("Found: CnaTreeElmt with sourceID " + sourceID + " and extID " + guid + "...");
        return elementList.get(0);
    }

}
