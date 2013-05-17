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
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.ICachedCommand;
import sernet.verinice.iso27k.service.IRiskAnalysisService;
import sernet.verinice.iso27k.service.RiskAnalysisServiceImpl;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Asset;
import sernet.verinice.model.iso27k.IncidentScenario;

/**
 * Loads all yellow & red risks to a given scenarioGroup
 */
public class LoadReportNotGreenScenarios extends GenericCommand implements ICachedCommand{
    
    private transient Logger log = Logger.getLogger(LoadReportNotGreenScenarios.class);
    
    public static final String[] COLUMNS = new String[]{"SCENARIO_NAME",
                                                  "RISK_COLOUR",
                                                  "SCENARIO_DBID"};
    
    private Integer rootElmt;
    
    private List<ArrayList<String>> results;
    
    private int[] numOfYellowFields;
    
    private boolean resultInjectedFromCache = false;
    
    private String probabilityType;
    
    public LoadReportNotGreenScenarios(Integer root, int[] yellowFields, String probType){
        this.rootElmt = root;
        this.numOfYellowFields = (yellowFields != null) ? yellowFields.clone() : null;
        results = new ArrayList<ArrayList<String>>(0);
        this.probabilityType = probType;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICommand#execute()
     */
    @Override
    public void execute() {
        if(!resultInjectedFromCache){
            try{
                HashMap<String, Integer> seenScenarios = new HashMap<String, Integer>(0);
                RiskAnalysisServiceImpl raService = new RiskAnalysisServiceImpl();
                LoadReportElements scenarioLoader = new LoadReportElements(IncidentScenario.TYPE_ID, rootElmt, false);
                scenarioLoader = getCommandService().executeCommand(scenarioLoader);
                for(CnATreeElement e : scenarioLoader.getElements()){
                    if(e instanceof IncidentScenario && !seenScenarios.containsKey(e.getUuid())){
                        LoadReportLinkedElements assetLoader = new LoadReportLinkedElements(Asset.TYPE_ID, e.getDbId(), false, true);
                        assetLoader = getCommandService().executeCommand(assetLoader);
                        int riskColour = 0;
                        for(CnATreeElement a : assetLoader.getElements()){
                            a = (CnATreeElement)assetLoader.getDaoFactory().getDAO(CnATreeElement.class).initializeAndUnproxy(a);
                            char[] riskTypes = new char[]{'c', 'i', 'a'};
                            for(int i = 0; i < riskTypes.length; i++){
                                int tc = raService.getRiskColor(a, e, riskTypes[i], numOfYellowFields[i], probabilityType);
                                if(riskColour == 0){ // case of first iteration
                                    riskColour = tc;
                                } else if(riskColour != IRiskAnalysisService.RISK_COLOR_GREEN && tc != IRiskAnalysisService.RISK_COLOR_GREEN){
                                    riskColour = tc;
                                }
                                if(riskColour == IRiskAnalysisService.RISK_COLOR_RED){
                                    break;
                                }

                            }
                            if(riskColour == IRiskAnalysisService.RISK_COLOR_RED){
                                break;
                            }
                        }
                        if(riskColour != 0){
                            ArrayList<String> result = new ArrayList<String>(0);
                            result.add(e.getTitle());
                            result.add(riskColour == IRiskAnalysisService.RISK_COLOR_RED ? "red" : "yellow");
                            result.add(e.getDbId().toString());
                            results.add(result);
                            seenScenarios.put(e.getUuid(), 1);
                        }
                    }
                }

            } catch (CommandException e){
                getLog().error("Error while executing command", e);
            }
        }
    }
    
    private Logger getLog(){
        if(log == null){
            log = Logger.getLogger(LoadReportNotGreenScenarios.class);
        }
        return log;
    }

    public List<ArrayList<String>> getResult() {
        return results;
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
