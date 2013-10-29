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
package sernet.gs.ui.rcp.main.service.crudcommands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import sernet.gs.service.RetrieveInfo;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.ICachedCommand;
import sernet.verinice.interfaces.IDAOFactory;
import sernet.verinice.iso27k.service.IRiskAnalysisService;
import sernet.verinice.iso27k.service.RiskAnalysisServiceImpl;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Asset;
import sernet.verinice.model.iso27k.Audit;
import sernet.verinice.model.iso27k.IncidentScenario;
import sernet.verinice.model.iso27k.IncidentScenarioGroup;
import sernet.verinice.model.iso27k.Organization;
import sernet.verinice.model.iso27k.Process;
import sernet.verinice.service.commands.LoadElementByUuid;


/**
 *  command categorizes all scenario groups of a rootElmt to red or yellow if they contain a scenario which leads
 *  to a risk which is categorized that way
 */
public class LoadReportRedYellowScenarioGroups extends GenericCommand implements ICachedCommand{
    
    private static transient Logger log = Logger.getLogger(LoadReportRedYellowScenarioGroups.class); 

    private Integer rootElmt;
    
    private List<ArrayList<String>> results;
    
    private int[] numOfYellowFields;
    
    private String scenarioProbType;
    
    public static final String[] COLUMNS = new String[] { 
        "GROUPTITLE",
        "RISKCOLOUR",
        "GROUPDBID"
        };
    
    private boolean resultInjectedFromCache = false;

    public LoadReportRedYellowScenarioGroups(Integer root, int[] numOfYellowFields, String probType){
        this.rootElmt = root;
        results = new ArrayList<ArrayList<String>>(0);
        this.numOfYellowFields = (numOfYellowFields != null) ? numOfYellowFields.clone() : null;
        this.scenarioProbType = probType;
    }
    
    
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICommand#execute()
     */
    @Override
    public void execute() {
        if(!resultInjectedFromCache){
            HashMap<String, Integer> scenarioGroupColorMap = new HashMap<String, Integer>(0);
            try{
                LoadPolymorphicCnAElementById command = new LoadPolymorphicCnAElementById(new Integer[] {rootElmt});
                command = getCommandService().executeCommand(command);
                CnATreeElement root = command.getElements().get(0);
                List<Process> processList = new ArrayList<Process>(0);

                // determine processes
                if(root instanceof Organization || root instanceof Audit){
                    processList.addAll(getProcesses(root));
                } else if(root instanceof Process){
                    processList.add((Process)root);
                }
                // iterate all processes
                for(Process p : processList){
                    LoadReportLinkedElements cmnd2 = new LoadReportLinkedElements(Asset.TYPE_ID, p.getDbId(), true, false);
                    cmnd2 = getCommandService().executeCommand(cmnd2);
                    List<CnATreeElement> assets = cmnd2.getElements();
                    // iterate assets linked to process
                    for (CnATreeElement asset : assets) {
                        
                        LoadReportLinkedElements cmnd3 = new LoadReportLinkedElements(IncidentScenario.TYPE_ID, asset.getDbId());
                        cmnd3 = getCommandService().executeCommand(cmnd3);
                        List<CnATreeElement> scenarios = cmnd3.getElements();
                        // iterate scenarios linked to asset
                        for (CnATreeElement scenario : scenarios) {
                            if(affectsCIA(scenario)){
                                scenario = (IncidentScenario)cmnd3.getDaoFactory().getDAO(IncidentScenario.TYPE_ID).initializeAndUnproxy(scenario);
                                // reload Scenario with parent && properties
                                LoadElementByUuid<CnATreeElement> scenarioReloader = new LoadElementByUuid<CnATreeElement>(IncidentScenario.TYPE_ID,
                                        scenario.getUuid(), new RetrieveInfo().setProperties(true).setParent(true));
                                scenario = getCommandService().executeCommand(scenarioReloader).getElement();
                                CnATreeElement parent = scenario.getParent();
                                parent = (CnATreeElement)cmnd3.getDaoFactory().getDAO(IncidentScenarioGroup.TYPE_ID).initializeAndUnproxy(parent);
                                if(parent instanceof IncidentScenarioGroup && 
                                        parent.getParent().getDbId().intValue() != scenario.getScopeId().intValue() // avoid rootScenGroup
                                        ){
                                    String parentUuid = scenario.getParent().getUuid();
                                    int riskColor = getRiskColour(asset, scenario);
                                    // if risk is yellow or red, put parent on map and mark all parents red also, if current is red
                                    if(!scenarioGroupColorMap.containsKey(parentUuid)){
                                        scenarioGroupColorMap.put(parentUuid, riskColor);
                                        if(riskColor == IRiskAnalysisService.RISK_COLOR_RED){
                                            scenarioGroupColorMap = (HashMap<String, Integer>)markParents(parent, scenarioGroupColorMap, cmnd3.getDaoFactory());
                                        }
                                    } else {
                                        if(!(scenarioGroupColorMap.get(parentUuid).intValue() == IRiskAnalysisService.RISK_COLOR_RED)){
                                            if(!(scenarioGroupColorMap.get(parentUuid).intValue() == IRiskAnalysisService.RISK_COLOR_YELLOW)){
                                                scenarioGroupColorMap.put(parentUuid, riskColor); // previous value green
                                            } else {// previous value yellow, if red ==> save
                                                if(riskColor == IRiskAnalysisService.RISK_COLOR_RED ){
                                                    scenarioGroupColorMap.put(parentUuid, riskColor); 
                                                }
                                            }
                                        } // if previous == red, done!
                                    }
                                }
                            }
                        }
                    }
                }
                // generate result
                for(Entry<String, Integer> entry : scenarioGroupColorMap.entrySet()){
                    LoadElementByUuid<IncidentScenarioGroup> groupLoader = new LoadElementByUuid<IncidentScenarioGroup>(IncidentScenarioGroup.TYPE_ID, entry.getKey(), 
                            new RetrieveInfo().setChildren(true).setProperties(true));
                    groupLoader = getCommandService().executeCommand(groupLoader);
                    ArrayList<String> result = new ArrayList<String>(0);
                    result.add(groupLoader.getElement().getTitle());
                    String colourValue = "";
                    switch(entry.getValue().intValue()){
                    case IRiskAnalysisService.RISK_COLOR_GREEN:
                        colourValue = "2green";
                        break;
                    case IRiskAnalysisService.RISK_COLOR_YELLOW:
                        colourValue = "1yellow";
                        break;
                    case IRiskAnalysisService.RISK_COLOR_RED:
                        colourValue = "0red";
                        break;
                    default:
                        break;
                    }
                    result.add(colourValue);
                    result.add(groupLoader.getElement().getDbId().toString());
                    results.add(result);
                }
            } catch (CommandException e){
                log.error("Error while executing command", e);
            }
        }
    }
    
    /**
     * if a child is marked red, its parent is marked red also
     * @param element
     * @param currentMap
     * @param daoFactory
     * @return
     */
    private Map<String, Integer> markParents(CnATreeElement element, Map<String, Integer> currentMap, IDAOFactory daoFactory){
        while(!(element instanceof Organization)){
            if(element instanceof IncidentScenarioGroup){
                currentMap.put(element.getUuid(), IRiskAnalysisService.RISK_COLOR_RED);
            }
            element = element.getParent();
            element = daoFactory.getDAO(CnATreeElement.class).initializeAndUnproxy(element);
        }
        return currentMap;
    }
    
    /**
     * returns risk colour for a risk based on a scenario & an asset
     * @param asset
     * @param scenario
     * @return
     */
    private int getRiskColour(CnATreeElement asset, CnATreeElement scenario){
        char[] riskTypes = new char[]{'c', 'i', 'a'};
        for(int i = 0; i < riskTypes.length; i++){
            if(isColouredRisk(asset, scenario, IRiskAnalysisService.RISK_COLOR_RED, riskTypes[i])){
                return IRiskAnalysisService.RISK_COLOR_RED;
            } else if(isColouredRisk(asset, scenario, IRiskAnalysisService.RISK_COLOR_YELLOW, riskTypes[i])){
                return IRiskAnalysisService.RISK_COLOR_YELLOW;
            }
        }
        return IRiskAnalysisService.RISK_COLOR_GREEN;
    }

    /**
     * determins if a risk (given by asset & scenario) is categorized in the specified colour,
     * risktype chooses the numOfYellowFields (currently CIA : 343)
     * @param asset
     * @param scenario
     * @param riskColour
     * @param riskType
     * @return
     */
    private boolean isColouredRisk(CnATreeElement asset, CnATreeElement scenario, int riskColour, char riskType){
        RiskAnalysisServiceImpl raService = new RiskAnalysisServiceImpl();
        int yellowNum = 0;
        switch(riskType){
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
        if(raService.getRiskColor(asset, scenario, riskType, yellowNum, scenarioProbType) == riskColour){
            return true;
        }
        return false;
    }
    
    /**
     * determins if a scenario has at least one of the affect checkboxes checked (is relevant for group categorization)
     * @param scenario
     * @return
     */
    private boolean affectsCIA(CnATreeElement scenario){
        String[] sArr = new String[]{IRiskAnalysisService.PROP_SCENARIO_AFFECTS_C,
                IRiskAnalysisService.PROP_SCENARIO_AFFECTS_I,
                IRiskAnalysisService.PROP_SCENARIO_AFFECTS_A};
        for(String s : Arrays.asList(sArr)){
            if(scenario.getEntity().getProperties(s).getProperty(0).getPropertyValue().equals("1")){
                return true;
            }
        }
        return false;
    }
                
    /**
     * returns all processes belonging to a given scope
     * @param elmt
     * @return
     * @throws CommandException
     */
    private List<Process> getProcesses(CnATreeElement elmt) throws CommandException{
        List<Process> list = new ArrayList<Process>(0);
        boolean useScopeId = false;
        if(elmt instanceof Organization){
            useScopeId = true;
        }
        LoadReportElements command = new LoadReportElements(Process.TYPE_ID, elmt.getDbId(), useScopeId);
        command = getCommandService().executeCommand(command);
        for(CnATreeElement e : command.getElements()){
            if(e instanceof Process){
                list.add((Process)e);
            }
        }
        return list;
    }


    public List<ArrayList<String>> getResults() {
        return results;
    }
    
    private Logger getLog(){
        if(log == null){
            log = Logger.getLogger(LoadReportRedYellowScenarioGroups.class);
        }
        return log;
    }


    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICachedCommand#getCacheID()
     */
    @Override
    public String getCacheID() {
        StringBuilder cacheID = new StringBuilder();
        cacheID.append(this.getClass().getSimpleName());
        cacheID.append(String.valueOf(rootElmt));
        for(int i : numOfYellowFields){
            cacheID.append(String.valueOf(i));
        }
        return cacheID.toString();
    }


    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICachedCommand#injectCacheResult(java.lang.Object)
     */
    @Override
    public void injectCacheResult(Object result) {
        this.results = (ArrayList<ArrayList<String>>)result;
        resultInjectedFromCache = true;
        if(getLog().isDebugEnabled()){
            getLog().debug("Result in " + this.getClass().getCanonicalName() + " injected from cache");
        }
    }


    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICachedCommand#getCacheableResult()
     */
    @Override
    public Object getCacheableResult() {
        return this.results;
    }


    
}
