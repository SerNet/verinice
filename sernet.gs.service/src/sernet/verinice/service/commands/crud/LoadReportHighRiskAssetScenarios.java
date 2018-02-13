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
import java.util.List;

import org.apache.log4j.Logger;

import sernet.gs.service.RetrieveInfo;
import sernet.gs.service.Retriever;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.ICachedCommand;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Asset;
import sernet.verinice.model.iso27k.ProtectionRequirementsValueAdapter;
import sernet.verinice.model.iso27k.IncidentScenario;
import sernet.verinice.model.iso27k.Process;
import sernet.verinice.service.risk.RiskAnalysisHelper;
import sernet.verinice.service.risk.RiskAnalysisHelperImpl;

/**
 *
 */
@SuppressWarnings("serial")
public class LoadReportHighRiskAssetScenarios extends GenericCommand implements ICachedCommand {

    private transient Logger log;
    
    private Integer root;
    
    private Integer riskType;
    
    private List<List<String>> results;
    
    private boolean resultInjectedFromCache = false;
    
    public static final String[] COLUMNS = new String[]{
                                    "assetName",
                                    "scenarioName",
                                    "processName",
                                    "riskC",
                                    "riskI",
                                    "riskA",
                                    "colourValue"
    };
    
    public LoadReportHighRiskAssetScenarios(Integer root, Integer riskType){
        this.root = root;
        this.riskType = riskType;
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICommand#execute()
     */
    @Override
    public void execute() {
        final int fourYellowFields = 4;
        final int threeYellowFields = 3;
        if(!resultInjectedFromCache){
            results = new ArrayList<List<String>>(0);
            try{
                LoadReportElements processLoader = new LoadReportElements(Process.TYPE_ID, root, true);
                processLoader = getCommandService().executeCommand(processLoader);
                for(CnATreeElement pr : processLoader.getElements()){
                    LoadReportLinkedElements assetLoader = new LoadReportLinkedElements(Asset.TYPE_ID, pr.getDbId());
                    assetLoader = getCommandService().executeCommand(assetLoader);
                    for(CnATreeElement ae : assetLoader.getElements()){
                        Asset asset = (Asset)Retriever.retrieveElement(ae, new RetrieveInfo().setProperties(true));
                        LoadReportLinkedElements scenarioLoader = new LoadReportLinkedElements(IncidentScenario.TYPE_ID, asset.getDbId());
                        scenarioLoader = getCommandService().executeCommand(scenarioLoader);
                        for(CnATreeElement se : scenarioLoader.getElements()){
                            IncidentScenario scenario = (IncidentScenario)Retriever.retrieveElement(se, new RetrieveInfo().setProperties(true));
                            ProtectionRequirementsValueAdapter valueAdapter = new ProtectionRequirementsValueAdapter(asset);
                            RiskAnalysisHelperImpl raService = new RiskAnalysisHelperImpl();

                            Integer impactC = valueAdapter.getConfidentiality();
                            Integer impactI = valueAdapter.getIntegrity();
                            Integer impactA = valueAdapter.getAvailability();
                            Integer[] reducedImpact = raService.applyControlsToImpact(riskType, (CnATreeElement)asset, impactC, impactI, impactA);
                            if (reducedImpact != null) {
                                impactC = reducedImpact[0];
                                impactI = reducedImpact[1];
                                impactA = reducedImpact[2];
                            }

                            boolean isCRelevant = false;
                            boolean isIRelevant = false;
                            boolean isARelevant = false;

                            isCRelevant = scenario.getEntity().getProperties("scenario_value_method_confidentiality").getProperty(0).getPropertyValue().equals("1");
                            isIRelevant = scenario.getEntity().getProperties("scenario_value_method_integrity").getProperty(0).getPropertyValue().equals("1");
                            isARelevant = scenario.getEntity().getProperties("scenario_value_method_availability").getProperty(0).getPropertyValue().equals("1");

                            String scenProbType = "";
                            scenProbType = getScenPropType();

                            Integer probability = scenario.getNumericProperty(scenProbType);            
                            Integer riskC = (isCRelevant) ? impactC + probability : Integer.valueOf(0);
                            Integer riskI = (isIRelevant) ? impactI + probability : Integer.valueOf(0);
                            Integer riskA = (isARelevant) ? impactA + probability : Integer.valueOf(0);

                            char[] cia = new char[]{'c', 'i', 'a'};
                            boolean isRedRisk = false;
                            boolean isYellowRisk = false;
                            for(char c : cia){
                                int yellowFields = 0;
                                if(c == 'i'){
                                    yellowFields = fourYellowFields;
                                } else {
                                    yellowFields = threeYellowFields;
                                }
                                int riskColor = raService.getRiskColor(asset, scenario, c, yellowFields, scenProbType);
                                if(!isRedRisk && riskColor == RiskAnalysisHelper.RISK_COLOR_RED){
                                    isRedRisk = true;
                                    break;
                                } else if(!isYellowRisk && riskColor == RiskAnalysisHelper.RISK_COLOR_YELLOW){
                                    isYellowRisk = true;
                                }

                            }

                            if(isRedRisk || isYellowRisk){  
                                ArrayList<String> row = new ArrayList<String>(0);
                                row.add(asset.getTitle());
                                row.add(scenario.getTitle());
                                row.add(pr.getTitle());
                                row.add(String.valueOf(riskC));
                                row.add(String.valueOf(riskI));
                                row.add(String.valueOf(riskA));
                                if(isRedRisk){
                                    row.add("red");
                                } else if(isYellowRisk){
                                    row.add("yellow");
                                }
                                results.add(row);
                            }
                            scenario = null;
                            se = null;
                        }
                        scenarioLoader = null;
                        ae = null;
                        asset = null;
                    }
                }
            } catch (CommandException e){
                getLog().error("Error while executing command", e);
            }
        }
    }

    private String getScenPropType() {
        String scenProbType;
        switch(riskType){
        case RiskAnalysisHelper.RISK_PRE_CONTROLS:
            scenProbType = RiskAnalysisHelper.PROP_SCENARIO_PROBABILITY;
            break;
        case RiskAnalysisHelper.RISK_WITH_ALL_CONTROLS:
            scenProbType = RiskAnalysisHelper.PROP_SCENARIO_PROBABILITY_WITH_PLANNED_CONTROLS;
            break;
        case RiskAnalysisHelper.RISK_WITH_IMPLEMENTED_CONTROLS:
            scenProbType = RiskAnalysisHelper.PROP_SCENARIO_PROBABILITY_WITH_CONTROLS;
            break;
        default:
            scenProbType = RiskAnalysisHelper.PROP_SCENARIO_PROBABILITY;
            break;
        }
        return scenProbType;
    }

    public List<List<String>> getResults() {
        return results;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICachedCommand#getCacheID()
     */
    @Override
    public String getCacheID() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getClass().getCanonicalName());
        sb.append(String.valueOf(root));
        sb.append(String.valueOf(riskType));
        return sb.toString();
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICachedCommand#injectCacheResult(java.lang.Object)
     */
    @Override
    public void injectCacheResult(Object result) {
        this.results = (ArrayList<List<String>>)result;
        resultInjectedFromCache = true;
        
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICachedCommand#getCacheableResult()
     */
    @Override
    public Object getCacheableResult() {
        return results;
    }
    
    private Logger getLog(){
        if(log == null){
            log = Logger.getLogger(LoadReportHighRiskAssetScenarios.class);
        }
        return log;
    }

}
