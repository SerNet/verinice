/*******************************************************************************
 * Copyright (c) 2013 Sebastian Hagedorn <sh@sernet.de>.
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
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.ICachedCommand;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Asset;
import sernet.verinice.model.iso27k.ProtectionRequirementsValueAdapter;
import sernet.verinice.model.iso27k.IncidentScenario;
import sernet.verinice.model.iso27k.Process;
import sernet.verinice.service.commands.LoadElementByUuid;
import sernet.verinice.service.risk.RiskAnalysisHelper;
import sernet.verinice.service.risk.RiskAnalysisHelperImpl;

/**
 *
 */
public class LoadReportRemainingRiskWithImplControls extends GenericCommand implements ICachedCommand {
    
    private int rootObject;
    private int toleratedC, toleratedI, toleratedA;
    
    private transient Logger log = Logger.getLogger(LoadReportRemainingRiskWithImplControls.class);

    public static final String[] COLUMNS = new String[] { 
        "color_name",
        "confidentiality",
        "integrity",
        "availability",
        "sortID"
    };

    public static final String PROP_ORG_RISKACCEPT_C = "org_riskaccept_confid";
    public static final String PROP_ORG_RISKACCEPT_I = "org_riskaccept_integ";
    public static final String PROP_ORG_RISKACCEPT_A = "org_riskaccept_avail";
    
    private boolean resultInjectedFromCache = false;
    
    List<List<String>> results = null;
    
    public LoadReportRemainingRiskWithImplControls(int root, int tolC, int tolI, int tolA){
        this.rootObject = root;
        this.toleratedC = tolC;
        this.toleratedI = tolI;
        this.toleratedA = tolA;
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICommand#execute()
     */
    @Override
    public void execute() {
        if(!resultInjectedFromCache){
            //load all processes of root
            try{
                LoadReportElements processLoader = new LoadReportElements(Process.TYPE_ID, rootObject);
                List<CnATreeElement> processes = getCommandService().executeCommand(processLoader).getElements();
                List<CnATreeElement> assets = null;
                List<CnATreeElement> scenarios = null;
                ProtectionRequirementsValueAdapter valueAdapter = null;
                RiskAnalysisHelperImpl raService = null;
                results = new ArrayList<List<String>>(0);

                int cRedCount = 0;
                int cYellowCount = 0;
                int cGreenCount = 0;
                int iRedCount = 0;
                int iYellowCount = 0;
                int iGreenCount = 0;
                int aRedCount = 0;
                int aYellowCount = 0;
                int aGreenCount = 0;
                for(CnATreeElement process : processes){
                    //load all to process linked assets
                    LoadReportLinkedElements assetLoader = new LoadReportLinkedElements(Asset.TYPE_ID, process.getDbId(), true, false);
                    assets = getCommandService().executeCommand(assetLoader).getElements();
                    for(CnATreeElement asset : assets){
                        valueAdapter = new ProtectionRequirementsValueAdapter(asset);
                        raService = new RiskAnalysisHelperImpl();

                        // reload asset
                        LoadElementByUuid<CnATreeElement> assetReloader = new LoadElementByUuid<CnATreeElement>(asset.getUuid(), new RetrieveInfo().setLinksDown(true).setLinksUp(true).setLinksDownProperties(true).setLinksUpProperties(true));
                        asset = getCommandService().executeCommand(assetReloader).getElement();
                        int impactC = valueAdapter.getConfidentiality();
                        int impactI = valueAdapter.getIntegrity();
                        int impactA = valueAdapter.getAvailability();
                        Integer[] reducedImpact = raService.applyControlsToImpact(RiskAnalysisHelper.RISK_WITH_IMPLEMENTED_CONTROLS, asset, impactC, impactI, impactA);
                        if(reducedImpact != null){
                            impactC = reducedImpact[0];
                            impactI = reducedImpact[1];
                            impactA = reducedImpact[2];
                        }

                        // load scenarios for each asset
                        LoadReportLinkedElements scenarioLoader = new LoadReportLinkedElements(IncidentScenario.TYPE_ID, asset.getDbId());
                        scenarios = getCommandService().executeCommand(scenarioLoader).getElements();
                        for(CnATreeElement scenario : scenarios){
                            int probability = scenario.getNumericProperty(RiskAnalysisHelper.PROP_SCENARIO_PROBABILITY_WITH_CONTROLS);
                            boolean isCrelevant = scenario.getEntity().getProperties(RiskAnalysisHelper.PROP_SCENARIO_AFFECTS_C).getProperty(0).getPropertyValue().equals("1");
                            boolean isIrelevant = scenario.getEntity().getProperties(RiskAnalysisHelper.PROP_SCENARIO_AFFECTS_I).getProperty(0).getPropertyValue().equals("1");
                            boolean isArelevant = scenario.getEntity().getProperties(RiskAnalysisHelper.PROP_SCENARIO_AFFECTS_A).getProperty(0).getPropertyValue().equals("1");
                            if(isCrelevant){
                                if((probability + impactC) > toleratedC){
                                    cRedCount++;
                                } else if((probability + impactC + 2) < toleratedC){
                                    cGreenCount++;
                                } else {
                                    cYellowCount++;
                                }
                            }
                            if(isIrelevant){
                                if((probability + impactI) > toleratedI){
                                    iRedCount++;
                                } else if((probability + impactI + 3) < toleratedI){
                                    iGreenCount++;
                                } else {
                                    iYellowCount++;
                                }
                            }
                            if(isArelevant){
                                if((probability + impactA) > toleratedA){
                                    aRedCount++;
                                } else if((probability + impactA + 2) < toleratedA){
                                    aGreenCount++;
                                } else {
                                    aYellowCount++;
                                }
                            }

                        }
                    }
                }
                // prepare results
                List<String> result = new ArrayList<String>(0);
                result.add("Confidentiality");
                result.add(String.valueOf(cRedCount));
                result.add(String.valueOf(cYellowCount));
                result.add(String.valueOf(cGreenCount));
                result.add(String.valueOf(0));
                results.add(result);
                
                result = new ArrayList<String>(0);
                result.add("Integrity");
                result.add(String.valueOf(iRedCount));
                result.add(String.valueOf(iYellowCount));
                result.add(String.valueOf(iGreenCount));
                result.add(String.valueOf(1));
                results.add(result);

                result = new ArrayList<String>(0);
                result.add("Availability");
                result.add(String.valueOf(aRedCount));
                result.add(String.valueOf(aYellowCount));
                result.add(String.valueOf(aGreenCount));
                result.add(String.valueOf(2));
                results.add(result);
                
                
            } catch (CommandException e){
                getLog().error("Error while executing command", e);
            }
        }
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICachedCommand#getCacheID()
     */
    @Override
    public String getCacheID() {
        StringBuilder cacheID = new StringBuilder();
        cacheID.append(this.getClass().getSimpleName());
        cacheID.append(String.valueOf(rootObject));
        cacheID.append(String.valueOf(toleratedC));
        cacheID.append(String.valueOf(toleratedI));
        cacheID.append(String.valueOf(toleratedA));
        return cacheID.toString();
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICachedCommand#injectCacheResult(java.lang.Object)
     */
    @Override
    public void injectCacheResult(Object result) {
        if(result instanceof List<?>){
            this.results = (List<List<String>>) result;
            resultInjectedFromCache = true;
        }
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICachedCommand#getCacheableResult()
     */
    @Override
    public Object getCacheableResult() {
        return results;
    }
    
    public List<List<String>> getResults(){
        return results;
    }
    
    private Logger getLog(){
        if(log == null){
            log = Logger.getLogger(LoadReportRemainingRiskWithImplControls.class);
        }
        return log;
    }

}
