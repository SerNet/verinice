/*******************************************************************************
 * Copyright (c) 2012 Sebastian Hagedorn <sh@sernet.de>.
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
 *     Sebastian Hagedorn <sh@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.service.commands.crud;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import sernet.gs.service.RetrieveInfo;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.ICachedCommand;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Asset;
import sernet.verinice.model.iso27k.Audit;
import sernet.verinice.model.iso27k.IncidentScenario;
import sernet.verinice.model.iso27k.IncidentScenarioGroup;
import sernet.verinice.model.iso27k.Organization;
import sernet.verinice.model.iso27k.Process;
import sernet.verinice.service.commands.LoadElementByUuid;
import sernet.verinice.service.risk.RiskAnalysisHelper;
import sernet.verinice.service.risk.RiskAnalysisHelperImpl;

/**
 * command categorizes all scenario groups of a rootElmt to red or yellow if
 * they contain a scenario which leads to a risk which is categorized that way
 */
public class LoadReportRedYellowScenarioGroups extends GenericCommand implements ICachedCommand {

    private static transient Logger log = Logger.getLogger(LoadReportRedYellowScenarioGroups.class);

    private Integer rootElmt;

    private List<ColoredScenarioGroup> results;

    private int[] numOfYellowFields;

    private String scenarioProbType;

    public static final String[] COLUMNS = new String[] { "GROUPTITLE", "RISKCOLOUR", "GROUPDBID" };

    private boolean resultInjectedFromCache = false;

    private HashMap<String, Integer> scenarioGroups;

    public LoadReportRedYellowScenarioGroups(Integer root, int[] numOfYellowFields, String probType) {
        this.rootElmt = root;
        results = new ArrayList<ColoredScenarioGroup>(0);
        this.numOfYellowFields = (numOfYellowFields != null) ? numOfYellowFields.clone() : null;
        this.scenarioProbType = probType;
        this.scenarioGroups = new HashMap<String, Integer>();
    }

    /*
     * (non-Javadoc)
     * 
     * @see sernet.verinice.interfaces.ICommand#execute()
     */
    @Override
    public void execute() {
        if (!resultInjectedFromCache) {
            try {
                LoadPolymorphicCnAElementById command = new LoadPolymorphicCnAElementById(new Integer[] { rootElmt });
                command = getCommandService().executeCommand(command);
                CnATreeElement root = command.getElements().get(0);
                List<Process> processList = new ArrayList<Process>(0);

                // determine processes
                if (root instanceof Organization || root instanceof Audit) {
                    processList.addAll(getProcesses(root));
                } else if (root instanceof Process) {
                    processList.add((Process) root);
                }
                // iterate all processes
                for (Process p : processList) {
                    List<CnATreeElement> assets = loadLinkedProcesses(p);

                    // iterate assets linked to process
                    for (CnATreeElement asset : assets) {
                        List<CnATreeElement> scenarios = loadLinkedIncidentSzenarios(asset);

                        // iterate scenarios linked to asset
                        for (CnATreeElement scenario : scenarios) {
                            if (affectsCIA(scenario)) {

                                scenario = (IncidentScenario) getDaoFactory().getDAO(IncidentScenario.TYPE_ID).initializeAndUnproxy(scenario);
                                CnATreeElement parent = loadScenarioParent(scenario);

                                if (!isScenarioGroupRoot(parent, scenario)) {

                                    int riskColor = getRiskColour(asset, scenario);
                                    setRiskColourForParentScenarioGroup(parent.getUuid(), riskColor);
                                }
                            }
                        }
                    }
                }                
                
                generateResult();
            } catch (CommandException e) {
                log.error("Error while executing command", e);
            }
        }
    }

    private void generateResult() throws CommandException  {
     
        for (Entry<String, Integer> entry : scenarioGroups.entrySet()) {
            ColoredScenarioGroup coloredScenarioGroup = new ColoredScenarioGroup();
            LoadElementByUuid<IncidentScenarioGroup> groupLoader = new LoadElementByUuid<IncidentScenarioGroup>(IncidentScenarioGroup.TYPE_ID, entry.getKey(), new RetrieveInfo().setChildren(true).setProperties(true));
            groupLoader = getCommandService().executeCommand(groupLoader);
            coloredScenarioGroup.title = groupLoader.getElement().getTitle();
            
            switch (entry.getValue().intValue()) {
            case RiskAnalysisHelper.RISK_COLOR_GREEN:
                coloredScenarioGroup.color = "2green";
                break;
            case RiskAnalysisHelper.RISK_COLOR_YELLOW:
                coloredScenarioGroup.color = "1yellow";
                break;
            case RiskAnalysisHelper.RISK_COLOR_RED:
                coloredScenarioGroup.color = "0red";
                break;
            default:
                coloredScenarioGroup.color = "";
            }
            
            coloredScenarioGroup.databaseId = groupLoader.getElement().getDbId().toString();
            results.add(coloredScenarioGroup);
        }        
    }
    
   public class ColoredScenarioGroup
   {
       String title;
       String color;
       String databaseId;
   }
    
    

    private void setRiskColourForParentScenarioGroup(String parentUuid, int riskColor) {
        if (!scenarioGroups.containsKey(parentUuid)) {
            scenarioGroups.put(parentUuid, riskColor);

        } else {
            if (!(scenarioGroups.get(parentUuid).intValue() == RiskAnalysisHelper.RISK_COLOR_RED)) {
                if (!(scenarioGroups.get(parentUuid).intValue() == RiskAnalysisHelper.RISK_COLOR_YELLOW)) {
                    scenarioGroups.put(parentUuid, riskColor); // previous
                                                               // value
                                                               // green
                } else {// previous value yellow, if
                        // red ==> save
                    if (riskColor == RiskAnalysisHelper.RISK_COLOR_RED) {
                        scenarioGroups.put(parentUuid, riskColor);
                    }
                }
            } // if previous == red, done!
        }

    }

    private boolean isScenarioGroupRoot(CnATreeElement parent, CnATreeElement scenario) {
        return !(parent instanceof IncidentScenarioGroup && parent.getParent().getDbId().intValue() != scenario.getScopeId().intValue());
    }

    private CnATreeElement loadScenarioParent(CnATreeElement scenario) throws CommandException {
        LoadElementByUuid<CnATreeElement> scenarioReloader = new LoadElementByUuid<CnATreeElement>(IncidentScenario.TYPE_ID, scenario.getUuid(), new RetrieveInfo().setProperties(true).setParent(true));
        scenario = getCommandService().executeCommand(scenarioReloader).getElement();
        CnATreeElement parent = scenario.getParent();
        return (CnATreeElement) getDaoFactory().getDAO(IncidentScenarioGroup.TYPE_ID).initializeAndUnproxy(parent);
    }

    private List<CnATreeElement> loadLinkedIncidentSzenarios(CnATreeElement asset) throws CommandException {
        LoadReportLinkedElements command = new LoadReportLinkedElements(IncidentScenario.TYPE_ID, asset.getDbId());
        command = getCommandService().executeCommand(command);
        return command.getElements();
    }

    private List<CnATreeElement> loadLinkedProcesses(Process p) throws CommandException {
        LoadReportLinkedElements command = new LoadReportLinkedElements(Asset.TYPE_ID, p.getDbId(), true, false);
        command = getCommandService().executeCommand(command);
        return command.getElements();
    }

    /**
     * returns risk colour for a risk based on a scenario & an asset
     * 
     * @param asset
     * @param scenario
     * @return
     */
    private int getRiskColour(CnATreeElement asset, CnATreeElement scenario) {
        char[] riskTypes = new char[] { 'c', 'i', 'a' };
        for (int i = 0; i < riskTypes.length; i++) {
            if (isColouredRisk(asset, scenario, RiskAnalysisHelper.RISK_COLOR_RED, riskTypes[i])) {
                return RiskAnalysisHelper.RISK_COLOR_RED;
            } else if (isColouredRisk(asset, scenario, RiskAnalysisHelper.RISK_COLOR_YELLOW, riskTypes[i])) {
                return RiskAnalysisHelper.RISK_COLOR_YELLOW;
            }
        }
        return RiskAnalysisHelper.RISK_COLOR_GREEN;
    }

    /**
     * determines if a risk (given by asset & scenario) is categorized in the
     * specified color, risk type chooses the numOfYellowFields (currently CIA :
     * 343)
     * 
     * @param asset
     * @param scenario
     * @param riskColour
     * @param riskType
     * @return
     */
    private boolean isColouredRisk(CnATreeElement asset, CnATreeElement scenario, int riskColour, char riskType) {
        RiskAnalysisHelperImpl raService = new RiskAnalysisHelperImpl();
        int yellowNum = 0;
        switch (riskType) {
        case 'c':
            yellowNum = numOfYellowFields[0];
            break;
        case 'i':
            yellowNum = numOfYellowFields[1];
            break;
        case 'a':
            yellowNum = numOfYellowFields[2];
            break;
        default:
            break;
        }
        if (raService.getRiskColor(asset, scenario, riskType, yellowNum, scenarioProbType) == riskColour) {
            return true;
        }
        return false;
    }

    /**
     * Determines if a scenario has at least one of the affect check boxes
     * checked. (is relevant for group categorization)
     */
    private boolean affectsCIA(CnATreeElement scenario) {
        String[] sArr = new String[] { RiskAnalysisHelper.PROP_SCENARIO_AFFECTS_C, RiskAnalysisHelper.PROP_SCENARIO_AFFECTS_I, RiskAnalysisHelper.PROP_SCENARIO_AFFECTS_A };
        for (String s : Arrays.asList(sArr)) {
            if (scenario.getEntity().getProperties(s).getProperty(0).getPropertyValue().equals("1")) {
                return true;
            }
        }
        return false;
    }

    /**
     * returns all processes belonging to a given scope
     * 
     * @param elmt
     * @return
     * @throws CommandException
     */
    private List<Process> getProcesses(CnATreeElement elmt) throws CommandException {
        List<Process> list = new ArrayList<Process>(0);
        boolean useScopeId = false;
        if (elmt instanceof Organization) {
            useScopeId = true;
        }
        LoadReportElements command = new LoadReportElements(Process.TYPE_ID, elmt.getDbId(), useScopeId);
        command = getCommandService().executeCommand(command);
        for (CnATreeElement e : command.getElements()) {
            if (e instanceof Process) {
                list.add((Process) e);
            }
        }
        return list;
    }

    public List<ColoredScenarioGroup> getResults() {
        return results;
    }

    private Logger getLog() {
        if (log == null) {
            log = Logger.getLogger(LoadReportRedYellowScenarioGroups.class);
        }
        return log;
    }

    /*
     * (non-Javadoc)
     * 
     * @see sernet.verinice.interfaces.ICachedCommand#getCacheID()
     */
    @Override
    public String getCacheID() {
        StringBuilder cacheID = new StringBuilder();
        cacheID.append(this.getClass().getSimpleName());
        cacheID.append(String.valueOf(rootElmt));
        for (int i : numOfYellowFields) {
            cacheID.append(String.valueOf(i));
        }
        return cacheID.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * sernet.verinice.interfaces.ICachedCommand#injectCacheResult(java.lang
     * .Object)
     */
    @Override
    public void injectCacheResult(Object result) {
        this.results = (ArrayList<ColoredScenarioGroup>) result;
        resultInjectedFromCache = true;
        if (getLog().isDebugEnabled()) {
            getLog().debug("Result in " + this.getClass().getCanonicalName() + " injected from cache");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see sernet.verinice.interfaces.ICachedCommand#getCacheableResult()
     */
    @Override
    public Object getCacheableResult() {
        return this.results;
    }

}
