/*******************************************************************************
 * Copyright (c) 2015 Daniel Murygin <dm@sernet.de>.
 *
 * This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     Daniel Murygin <dm@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.service.commands;

import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import de.sernet.sync.risk.Risk;
import de.sernet.sync.risk.SyncControl;
import de.sernet.sync.risk.SyncRiskAnalysis;
import de.sernet.sync.risk.SyncScenario;
import de.sernet.sync.risk.SyncScenarioList;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.model.bsi.risikoanalyse.FinishedRiskAnalysis;
import sernet.verinice.model.bsi.risikoanalyse.FinishedRiskAnalysisLists;
import sernet.verinice.model.bsi.risikoanalyse.GefaehrdungsUmsetzung;
import sernet.verinice.model.bsi.risikoanalyse.OwnGefaehrdung;
import sernet.verinice.model.bsi.risikoanalyse.RisikoMassnahme;
import sernet.verinice.model.common.CnATreeElement;

/**
 * Exports a set of risk analysises from IT Baseline Protection to
 * XML object Risk. RiskAnalysisExporter is called during VNA export
 * in command ExportCommand.
 * 
 * Risk schema definition:
 * sernet/verinice/service/sync/risk.xsd
 * 
 * @author Daniel Murygin <dm@sernet.de>
 */
public class RiskAnalysisExporter {

    private static final Logger LOG = Logger.getLogger(RiskAnalysisExporter.class);
    
    private Risk risk;
    private Set<Integer> riskAnalysisIdSet;
    private ICommandService commandService;
    
    public RiskAnalysisExporter() {
        super();
    }

    public void run() {
        risk = new Risk();
        for (Integer id : riskAnalysisIdSet) {      
            try {
                exportRiskAnalysis(id);
                exportOwnSzenarios();
                exportOwnControls();
            } catch (Exception e) {
                LOG.error("Error while exporting risk analysis", e);
            }
        }
    }

    private void exportRiskAnalysis(Integer id) throws CommandException {
        CnATreeElement riskAnalysis = loadRiskAnalysis(id);
        FinishedRiskAnalysisLists riskAnalysisList = loadRiskAnalysisList(id);
        SyncRiskAnalysis syncRiskAnalysis = new SyncRiskAnalysis();
        syncRiskAnalysis.setExtId(ExportFactory.createExtId(riskAnalysis));
        
        List<GefaehrdungsUmsetzung> scenarios = riskAnalysisList.getAssociatedGefaehrdungen();
        SyncScenarioList syncScenarioList = new SyncScenarioList();
        syncRiskAnalysis.setScenarios(syncScenarioList);
        addExtIdList(scenarios, syncScenarioList.getExtId());
        
        scenarios = riskAnalysisList.getAllGefaehrdungsUmsetzungen();
        SyncScenarioList syncScenarioListNotTreated = new SyncScenarioList();
        syncRiskAnalysis.setScenariosNotTreated(syncScenarioListNotTreated);
        addExtIdList(scenarios, syncScenarioListNotTreated.getExtId());
        
        scenarios = riskAnalysisList.getNotOKGefaehrdungsUmsetzungen();
        SyncScenarioList syncScenarioListReduction = new SyncScenarioList();
        syncRiskAnalysis.setScenariosReduction(syncScenarioListReduction);
        addExtIdList(scenarios, syncScenarioListReduction.getExtId());
        
        risk.getAnalysis().add(syncRiskAnalysis);      
    }
    
    private void exportOwnSzenarios() throws CommandException {
        LoadGenericElementByType<OwnGefaehrdung> command = new LoadGenericElementByType<OwnGefaehrdung>(OwnGefaehrdung.class);
        command = commandService.executeCommand(command);
        List<OwnGefaehrdung> scenarioList = command.getElements();
        for (OwnGefaehrdung scenario : scenarioList) {
            SyncScenario syncScenario = new SyncScenario();
            syncScenario.setDescription(scenario.getBeschreibung());
            syncScenario.setName(scenario.getTitel());
            syncScenario.setNumber(scenario.getId());
            syncScenario.setUuid(scenario.getUuid()); 
            if(scenario.getOwnkategorie()!=null && !scenario.getOwnkategorie().isEmpty()) {
                syncScenario.setCategory(scenario.getOwnkategorie());
            }
            risk.getScenario().add(syncScenario);      
        }
    }
    
    private void exportOwnControls() throws CommandException {
        LoadGenericElementByType<RisikoMassnahme> command = new LoadGenericElementByType<RisikoMassnahme>(RisikoMassnahme.class);
        command = commandService.executeCommand(command);
        List<RisikoMassnahme> controlList = command.getElements();
        for (RisikoMassnahme control : controlList) {
            SyncControl syncControl = new SyncControl();
            syncControl.setDescription(control.getDescription());
            syncControl.setName(control.getName());
            syncControl.setNumber(control.getNumber());
            syncControl.setUuid(control.getUuid());  
            risk.getControl().add(syncControl);      
        }
    }

    private FinishedRiskAnalysisLists loadRiskAnalysisList(Integer id) throws CommandException {
        FindRiskAnalysisListsByParentID command = new FindRiskAnalysisListsByParentID(id);
        command = getCommandService().executeCommand(command);
        return command.getFoundLists();
    }

    private CnATreeElement loadRiskAnalysis(Integer id) throws CommandException {
        RetrieveCnATreeElement retrieveElement = new RetrieveCnATreeElement(FinishedRiskAnalysis.TYPE_ID, id);
        retrieveElement = getCommandService().executeCommand(retrieveElement);
        return retrieveElement.getElement();
    }

    private static void addExtIdList(List<GefaehrdungsUmsetzung> scenarios, List<String> extIdList) {
        for (GefaehrdungsUmsetzung gefaehrdungsUmsetzung : scenarios) {
            extIdList.add(ExportFactory.createExtId(gefaehrdungsUmsetzung));
        }
    }
    
    public Set<Integer> getRiskAnalysisIdSet() {
        return riskAnalysisIdSet;
    }

    public void setRiskAnalysisIdSet(Set<Integer> riskAnalysisIdSet) {
        this.riskAnalysisIdSet = riskAnalysisIdSet;
    }

    public Risk getRisk() {
        return risk;
    }

    public void setRisk(Risk risk) {
        this.risk = risk;
    }

    public ICommandService getCommandService() {
        return commandService;
    }

    public void setCommandService(ICommandService commandService) {
        this.commandService = commandService;
    }
}
