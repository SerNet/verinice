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
 *     Alexander Koderman - initial API and implementation
 *     Benjamin Weißenfels <bw[at]sernet[dot]de>
 *     Daniel Murygin <dm[at]sernet[dot]de>
 ******************************************************************************/
package sernet.verinice.iso27k.service;

import java.io.Serializable;
import java.util.Set;

import org.apache.log4j.Logger;

import sernet.gs.service.TimeFormatter;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.graph.Edge;
import sernet.verinice.interfaces.graph.GraphElementLoader;
import sernet.verinice.interfaces.graph.IGraphElementLoader;
import sernet.verinice.interfaces.graph.IGraphService;
import sernet.verinice.interfaces.graph.VeriniceGraph;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Asset;
import sernet.verinice.model.iso27k.Control;
import sernet.verinice.model.iso27k.IncidentScenario;
import sernet.verinice.model.iso27k.Threat;
import sernet.verinice.model.iso27k.Vulnerability;

/**
 * Service implementation to run a ISO/IEC 27005 risk analysis.
 *  
 * This implementation loads data by IGraphService and a CnALink dao
 * to save links.
 *  
 * @author Alexander Koderman
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class RiskAnalysisServiceImpl implements RiskAnalysisService {
    
    private static final transient Logger LOG = Logger.getLogger(RiskAnalysisServiceImpl.class);
    private static final Logger LOG_RUNTIME = Logger.getLogger(RiskAnalysisServiceImpl.class.getName() + ".runtime");
    
    private IGraphService graphService;  
    private IBaseDao<CnALink, Serializable> cnaLinkDao;
    
    public RiskAnalysisServiceImpl() {
        super();
    }
    
    public RiskAnalysisServiceImpl(IGraphService graphService, IBaseDao<CnALink,Serializable> cnaLinkDao) {
        this.graphService = graphService;
        this.cnaLinkDao = cnaLinkDao;
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.iso27k.service.RiskAnalysisService#runRiskAnalysis(java.lang.Long[])
     */
    @Override
    public void runRiskAnalysis(Integer... organizationIds) {
        long time = initRuntime();   
        
        VeriniceGraph graph = loadGraph(organizationIds);              
        runRiskAnalysis(graph);
        
        logRuntime("runRiskAnalysis() runtime : ", time);
    }
    

    /**
     * Runs a risk analysis by analyzing the risk of
     * scenarios and assets.
     * 
     * @param graph A verinice graph with all elements which are analyzed
     */
    private void runRiskAnalysis(VeriniceGraph graph) {
        resetRiskValuesOfAssets(graph);
        analyseRiskOfScenarios(graph);
        analyseRiskOfAssets(graph);
    }

    /**
     * Resets the risk values of all assets in a given 
     * verinice graph.
     *
     * @param graph A verinice graph with all elements which are analyzed
     */
    private void resetRiskValuesOfAssets(VeriniceGraph graph) {
        Set<CnATreeElement> assets = graph.getElements(Asset.TYPE_ID);
        if (LOG.isInfoEnabled()) {
            LOG.info("Number of assets: " + assets.size());
        }
        for (CnATreeElement asset : assets) {
            if(LOG.isDebugEnabled()){
                LOG.debug("Resetting Risk for Asset:\t" + asset.getTitle());
            }
            resetRisks((Asset) asset);
        }
    }

    /**
     * Analyses the risk of all incident scenarios in a given 
     * verinice graph.
     *      
     * @param graph A verinice graph with all elements which are analyzed
     */
    private void analyseRiskOfScenarios(VeriniceGraph graph) {
        Set<CnATreeElement> scenarios = graph.getElements(IncidentScenario.TYPE_ID);
        if (LOG.isInfoEnabled()) {
            LOG.info("Number of scenarios: " + scenarios.size());
        }
        for (CnATreeElement scenario : scenarios) {
            if(LOG.isDebugEnabled()){
                LOG.debug("Determine Probability for Scenario:\t" 
                        + scenario.getTitle());
            }
            analyseScenario(graph, (IncidentScenario) scenario);
        }
    }

    /**
     * Analyses the risk of an incident scenario.
     * 
     * 1. The probability of the scenario is set by addition of
     *    the probability of the threat and the vulnerability.
     * 2. The reduced probability of the scenario is set by subtracting
     *    the effect of all linked controls irrespective of whether or not
     *    the controls are implemented.
     * 3. The reduced probability of the scenario is set by subtracting
     *    the effect of all linked and implemented controls.
     *
     * @param graph A verinice graph with all elements which are analyzed
     * @param scenario A single incident scenario which is analyzed
     */
    private void analyseScenario(VeriniceGraph graph, IncidentScenario scenario) {
        // get values from linked threat & vuln, only if automatic mode is activated:
        if (scenario.getNumericProperty(IncidentScenario.PROP_SCENARIO_METHOD) == 1) {
            getProbabilityFromThreatAndVulnerability(graph, scenario);
        }

        // calculate probability:
        scenario.setNumericProperty(IncidentScenario.PROP_SCENARIO_PROBABILITY, 0);
        int myThreat = scenario.getNumericProperty(IncidentScenario.PROP_SCENARIO_THREAT_PROBABILITY);
        int myVuln = scenario.getNumericProperty(IncidentScenario.PROP_SCENARIO_VULN_PROBABILITY);
        scenario.setNumericProperty(IncidentScenario.PROP_SCENARIO_PROBABILITY, myThreat + myVuln);

        // now determine probability after all applied controls:
        // init probability values to value without controls:
        scenario.setNumericProperty(IncidentScenario.PROP_SCENARIO_PROBABILITY_WITH_CONTROLS, scenario.getNumericProperty(IncidentScenario.PROP_SCENARIO_PROBABILITY));
        scenario.setNumericProperty(IncidentScenario.PROP_SCENARIO_PROBABILITY_WITH_PLANNED_CONTROLS, scenario.getNumericProperty(IncidentScenario.PROP_SCENARIO_PROBABILITY));
        scenario.setNumericProperty(IncidentScenario.PROP_SCENARIO_PROBABILITY_WITHOUT_NA_CONTROLS, scenario.getNumericProperty(IncidentScenario.PROP_SCENARIO_PROBABILITY));

        Set<CnATreeElement> controlSet = graph.getLinkTargetsByElementType(scenario, Control.TYPE_ID);
        // deduct controls from probability:
        for (CnATreeElement control : controlSet) {
            reduceRiskOfSzenarioWithControl(scenario, control);
        }
    }
    
    private void reduceRiskOfSzenarioWithControl(IncidentScenario scenario, CnATreeElement control) {
        int controlEffect = control.getNumericProperty(Control.PROP_CONTROL_EFFECT_P);
        int probAfterControl;
        
        // Risk with all controls
        probAfterControl = scenario.getNumericProperty(
                IncidentScenario.PROP_SCENARIO_PROBABILITY_WITH_PLANNED_CONTROLS) - controlEffect;
        scenario.setNumericProperty(IncidentScenario.PROP_SCENARIO_PROBABILITY_WITH_PLANNED_CONTROLS,
                probAfterControl < 0 ? 0 : probAfterControl);
        
        // Risk with implemented controls
        if (Control.isImplemented(control.getEntity())) {
            
            probAfterControl = scenario.getNumericProperty(IncidentScenario.PROP_SCENARIO_PROBABILITY_WITH_CONTROLS)
                    - controlEffect;
            scenario.setNumericProperty(IncidentScenario.PROP_SCENARIO_PROBABILITY_WITH_CONTROLS,
                    probAfterControl < 0 ? 0 : probAfterControl);
        }

        // Risk with planned implemented controls
        if (Control.isPlanned(control.getEntity())) {
            probAfterControl = scenario.getNumericProperty(
                    IncidentScenario.PROP_SCENARIO_PROBABILITY_WITHOUT_NA_CONTROLS) - controlEffect;
            scenario.setNumericProperty(IncidentScenario.PROP_SCENARIO_PROBABILITY_WITHOUT_NA_CONTROLS,
                    probAfterControl < 0 ? 0 : probAfterControl);
        }
    }
    
    /**
     * Analyses the risk of all assets which are linked to incident scenarios
     * in a given verinice graph.
     * 
     * @param graph A verinice graph with all elements which are analyzed
     * @throws CommandException 
     */
    private void analyseRiskOfAssets(VeriniceGraph graph) {
        Set<CnATreeElement> scenarios = graph.getElements(IncidentScenario.TYPE_ID);
        if (LOG.isInfoEnabled()) {
            LOG.info("Number of scenarios: " + scenarios.size());
        }
        // determine risk originating from scenarios for all linked assets:
        for (CnATreeElement scenario : scenarios) {
            if(LOG.isDebugEnabled()){
                LOG.debug("Determine Risk for Scenario:\t" + scenario.getTitle());
            }                
            analyseRiskOfAssets(graph, (IncidentScenario) scenario);       
        }
    }

    /** 
     * Analyses the risk of all assets which are linked to a given incident scenario.
     *
     * @param graph A verinice graph with all elements which are analyzed
     * @param scenario A single incident scenario
     * @throws CommandException
     */
    private void analyseRiskOfAssets(VeriniceGraph graph, IncidentScenario scenario)  {
        Set<Edge> edgesToAsset = graph.getEdgesByElementType(scenario, Asset.TYPE_ID);
      
        for (Edge edge : edgesToAsset) {
            analyseRiskOfAsset(graph, scenario, edge);
        }  
        if (LOG.isInfoEnabled()) {
            LOG.info("Number of links from scenarios to assets: " + edgesToAsset.size());
        }
        long time = initRuntime();
        // Update cnalinks
        for (Edge edge : edgesToAsset) {
            saveLink(edge);
        }
        logRuntime("Updating links from scenarios to assets runtime : ", time);
    }
    
    /**
     * Analyses the risk of a single asset which is linked to an incident scenario.
     * 
     * @param scenario An incident scenario
     * @param edgeToAsset An edge that connects an incident scenario with an asset
     */
    private void analyseRiskOfAsset(VeriniceGraph graph, IncidentScenario scenario, Edge edgeToAsset)  {
        Asset asset = (Asset) edgeToAsset.getTarget();
        Asset assetWithReducedCIAValues = reduceCIAValues(graph,asset);
        
        resetRisks(edgeToAsset);
        
        if (scenarioAffectsConfidentiality(scenario)) {
            analyseRiskCOfAsset(scenario, edgeToAsset, assetWithReducedCIAValues);
        }
        
        if (scenarioAffectsIntegrity(scenario)) {
            analyseRiskIOfAsset(scenario, edgeToAsset, assetWithReducedCIAValues);
        }
        
        if (scenarioAffectsAvailability(scenario)) {
            analyseRiskAOfAsset(scenario, edgeToAsset, assetWithReducedCIAValues);
        }
    }

    private void analyseRiskCOfAsset(IncidentScenario scenario, Edge edgeToAsset, Asset assetWithReducedCIAValues) {
        Asset asset = (Asset) edgeToAsset.getTarget();
        int assetValueC = asset.getEntity().getNumericValue(Asset.ASSET_VALUE_CONFIDENTIALITY);
        
        // Without controls:
        int riskValueC = assetValueC + scenario.getNumericProperty(IncidentScenario.PROP_SCENARIO_PROBABILITY);
        asset.setNumericProperty(Asset.ASSET_RISK_C,
                asset.getNumericProperty(Asset.ASSET_RISK_C) + riskValueC);
        edgeToAsset.setRiskConfidentiality(riskValueC);
        
        // With implemented controls
        int assetValueImplementedControlsC = assetWithReducedCIAValues.getNumericProperty(Asset.ASSET_CONTROLRISK_C);        
        int riskImplControlsC = assetValueImplementedControlsC + scenario.getNumericProperty(IncidentScenario.PROP_SCENARIO_PROBABILITY_WITH_CONTROLS);
        asset.setNumericProperty(Asset.ASSET_CONTROLRISK_C,
                asset.getNumericProperty(Asset.ASSET_CONTROLRISK_C) + riskImplControlsC);
        edgeToAsset.setRiskConfidentialityWithControls(riskImplControlsC);
                    
        // With all controls          
        int assetValueAllControlsC = assetWithReducedCIAValues.getNumericProperty(Asset.ASSET_PLANCONTROLRISK_C);        
        int riskAllControlsC = assetValueAllControlsC
                + scenario.getNumericProperty(IncidentScenario.PROP_SCENARIO_PROBABILITY_WITH_PLANNED_CONTROLS);
        asset.setNumericProperty(Asset.ASSET_PLANCONTROLRISK_C,
                asset.getNumericProperty(Asset.ASSET_PLANCONTROLRISK_C) + riskAllControlsC);
        
        // With planned controls
        int assetValuePlannedControlsC = assetWithReducedCIAValues.getNumericProperty(Asset.ASSET_WITHOUT_NA_PLANCONTROLRISK_C);   
        int riskPlannedControlsC = assetValuePlannedControlsC
                + scenario.getNumericProperty(IncidentScenario.PROP_SCENARIO_PROBABILITY_WITHOUT_NA_CONTROLS);
        asset.setNumericProperty(Asset.ASSET_WITHOUT_NA_PLANCONTROLRISK_C,
                asset.getNumericProperty(Asset.ASSET_WITHOUT_NA_PLANCONTROLRISK_C)
                        + riskPlannedControlsC);
    }
    
    private void analyseRiskIOfAsset(IncidentScenario scenario, Edge edgeToAsset, 
            Asset assetWithReducedCIAValues) {
        Asset asset = (Asset) edgeToAsset.getTarget();
        int assetValueI = asset.getEntity().getNumericValue(Asset.ASSET_VALUE_INTEGRITY);
        
        // Without controls:
        int riskValueI = assetValueI + scenario.getNumericProperty(IncidentScenario.PROP_SCENARIO_PROBABILITY);
        asset.setNumericProperty(Asset.ASSET_RISK_I,
                asset.getNumericProperty(Asset.ASSET_RISK_I) + riskValueI);
        edgeToAsset.setRiskIntegrity(riskValueI);
        
        // With implemented controls
        int assetValueImplementedControlsI = assetWithReducedCIAValues.getNumericProperty(Asset.ASSET_CONTROLRISK_I);        
        int riskImplControlsI = assetValueImplementedControlsI + scenario.getNumericProperty(IncidentScenario.PROP_SCENARIO_PROBABILITY_WITH_CONTROLS);
        asset.setNumericProperty(Asset.ASSET_CONTROLRISK_I,
                asset.getNumericProperty(Asset.ASSET_CONTROLRISK_I) + riskImplControlsI);
        edgeToAsset.setRiskIntegrityWithControls(riskImplControlsI);
                
        // With all controls          
        int assetValueAllControlsI = assetWithReducedCIAValues.getNumericProperty(Asset.ASSET_PLANCONTROLRISK_I);        
        int riskAllControlsI = assetValueAllControlsI
                + scenario.getNumericProperty(IncidentScenario.PROP_SCENARIO_PROBABILITY_WITH_PLANNED_CONTROLS);
        asset.setNumericProperty(Asset.ASSET_PLANCONTROLRISK_I,
                asset.getNumericProperty(Asset.ASSET_PLANCONTROLRISK_I) + riskAllControlsI);
        
        // With planned controls
        int assetValuePlannedControlsI = assetWithReducedCIAValues.getNumericProperty(Asset.ASSET_WITHOUT_NA_PLANCONTROLRISK_I);   
        int riskPlannedControlsI = assetValuePlannedControlsI
                + scenario.getNumericProperty(IncidentScenario.PROP_SCENARIO_PROBABILITY_WITHOUT_NA_CONTROLS);
        asset.setNumericProperty(Asset.ASSET_WITHOUT_NA_PLANCONTROLRISK_I,
                asset.getNumericProperty(Asset.ASSET_WITHOUT_NA_PLANCONTROLRISK_I)
                        + riskPlannedControlsI);
    }
    
    private void analyseRiskAOfAsset(IncidentScenario scenario, Edge edgeToAsset,
            Asset assetWithReducedCIAValues) {
        Asset asset = (Asset) edgeToAsset.getTarget();
        int assetValueA = asset.getEntity().getNumericValue(Asset.ASSET_VALUE_AVAILABILITY);
        
        // Without controls:
        int riskValueA = assetValueA + scenario.getNumericProperty(IncidentScenario.PROP_SCENARIO_PROBABILITY);
        asset.setNumericProperty(Asset.ASSET_RISK_A,
                asset.getNumericProperty(Asset.ASSET_RISK_A) + riskValueA);
        edgeToAsset.setRiskAvailability(riskValueA);
        
        // With implemented controls
        int assetValueImplementedControlsA = assetWithReducedCIAValues.getNumericProperty(Asset.ASSET_CONTROLRISK_A);        
        int riskImplControlsA = assetValueImplementedControlsA + scenario.getNumericProperty(IncidentScenario.PROP_SCENARIO_PROBABILITY_WITH_CONTROLS);
        asset.setNumericProperty(Asset.ASSET_CONTROLRISK_A,
                asset.getNumericProperty(Asset.ASSET_CONTROLRISK_A) + riskImplControlsA);
        edgeToAsset.setRiskAvailabilityWithControls(riskImplControlsA);
                
        // With all controls          
        int assetValueAllControlsA = assetWithReducedCIAValues.getNumericProperty(Asset.ASSET_PLANCONTROLRISK_A);        
        int riskAllControlsA = assetValueAllControlsA
                + scenario.getNumericProperty(IncidentScenario.PROP_SCENARIO_PROBABILITY_WITH_PLANNED_CONTROLS);
        asset.setNumericProperty(Asset.ASSET_PLANCONTROLRISK_A,
                asset.getNumericProperty(Asset.ASSET_PLANCONTROLRISK_A) + riskAllControlsA);
        
        // With planned controls
        int assetValuePlannedControlsA = assetWithReducedCIAValues.getNumericProperty(Asset.ASSET_WITHOUT_NA_PLANCONTROLRISK_A);   
        int riskPlannedControlsA = assetValuePlannedControlsA
                + scenario.getNumericProperty(IncidentScenario.PROP_SCENARIO_PROBABILITY_WITHOUT_NA_CONTROLS);
        asset.setNumericProperty(Asset.ASSET_WITHOUT_NA_PLANCONTROLRISK_A,
                asset.getNumericProperty(Asset.ASSET_WITHOUT_NA_PLANCONTROLRISK_A)
                        + riskPlannedControlsA);
    }
      
    /**
     * Reduce the CIA values of an given asset with the effect of all controls
     * which are linked to this asset.
     * Returns a copy of the given asset with reduced values.
     * 
     * @param graph A verinice graph with all elements which are analyzed
     * @param asset An asset with CIA values
     * @return A copy of the given asset with reduced CIA values.
     */
    private Asset reduceCIAValues(VeriniceGraph graph, Asset asset) {  
        Asset assetWithReducedCIAValues = new Asset();
        assetWithReducedCIAValues.getEntity().copyEntity(asset.getEntity());
        
        // Reset risk values to prevent summation when running risk analysis multiple times
        assetWithReducedCIAValues.setNumericProperty(Asset.ASSET_PLANCONTROLRISK_C,asset.getNumericProperty(Asset.ASSET_VALUE_CONFIDENTIALITY));
        assetWithReducedCIAValues.setNumericProperty(Asset.ASSET_PLANCONTROLRISK_I,asset.getNumericProperty(Asset.ASSET_VALUE_INTEGRITY));
        assetWithReducedCIAValues.setNumericProperty(Asset.ASSET_PLANCONTROLRISK_A,asset.getNumericProperty(Asset.ASSET_VALUE_AVAILABILITY));
        
        assetWithReducedCIAValues.setNumericProperty(Asset.ASSET_WITHOUT_NA_PLANCONTROLRISK_C,asset.getNumericProperty(Asset.ASSET_VALUE_CONFIDENTIALITY));
        assetWithReducedCIAValues.setNumericProperty(Asset.ASSET_WITHOUT_NA_PLANCONTROLRISK_I,asset.getNumericProperty(Asset.ASSET_VALUE_INTEGRITY));
        assetWithReducedCIAValues.setNumericProperty(Asset.ASSET_WITHOUT_NA_PLANCONTROLRISK_A,asset.getNumericProperty(Asset.ASSET_VALUE_AVAILABILITY));
        
        assetWithReducedCIAValues.setNumericProperty(Asset.ASSET_CONTROLRISK_C,asset.getNumericProperty(Asset.ASSET_VALUE_CONFIDENTIALITY));
        assetWithReducedCIAValues.setNumericProperty(Asset.ASSET_CONTROLRISK_I,asset.getNumericProperty(Asset.ASSET_VALUE_INTEGRITY));
        assetWithReducedCIAValues.setNumericProperty(Asset.ASSET_CONTROLRISK_A,asset.getNumericProperty(Asset.ASSET_VALUE_AVAILABILITY)); 
        
        Set<CnATreeElement> linkedControls = graph.getLinkTargetsByElementType(asset, Control.TYPE_ID);    
        for (CnATreeElement control : linkedControls) {
            reduceCIAValues(assetWithReducedCIAValues, control);
        }  
        return assetWithReducedCIAValues;
    }
    
    /**
     * Reduce the CIA values of a given asset with the effect of a given control.
     * 
     * @param asset An asset with CIA values
     * @param control A control which reduces the CIA values
     */
    private void reduceCIAValues(Asset asset, CnATreeElement control) {
        int controlEffectC = control.getNumericProperty(RiskAnalysisHelper.PROP_CONTROL_EFFECT_C);
        int controlEffectI = control.getNumericProperty(RiskAnalysisHelper.PROP_CONTROL_EFFECT_I);
        int controlEffectA = control.getNumericProperty(RiskAnalysisHelper.PROP_CONTROL_EFFECT_A);
        
        // Reduce regardless of implementation status
        asset.setNumericProperty(Asset.ASSET_PLANCONTROLRISK_C,
                asset.getNumericProperty(Asset.ASSET_PLANCONTROLRISK_C) - controlEffectC);
        asset.setNumericProperty(Asset.ASSET_PLANCONTROLRISK_I,
                asset.getNumericProperty(Asset.ASSET_PLANCONTROLRISK_I) - controlEffectI);
        asset.setNumericProperty(Asset.ASSET_PLANCONTROLRISK_A,
                asset.getNumericProperty(Asset.ASSET_PLANCONTROLRISK_A) - controlEffectA);
        
        // Reduce if implementation status is "planned"
        if (Control.isPlanned(control.getEntity())) {
            asset.setNumericProperty(Asset.ASSET_WITHOUT_NA_PLANCONTROLRISK_C,
                    asset.getNumericProperty(Asset.ASSET_WITHOUT_NA_PLANCONTROLRISK_C)
                            - controlEffectC);
            asset.setNumericProperty(Asset.ASSET_WITHOUT_NA_PLANCONTROLRISK_I,
                    asset.getNumericProperty(Asset.ASSET_WITHOUT_NA_PLANCONTROLRISK_I)
                            - controlEffectI);
            asset.setNumericProperty(Asset.ASSET_WITHOUT_NA_PLANCONTROLRISK_A,
                    asset.getNumericProperty(Asset.ASSET_WITHOUT_NA_PLANCONTROLRISK_A)
                            - controlEffectA);
        }
        
        // Reduce if implementation status is "implemented"
        if (Control.isImplemented(control.getEntity())) {
            asset.setNumericProperty(Asset.ASSET_CONTROLRISK_C,
                    asset.getNumericProperty(Asset.ASSET_CONTROLRISK_C) - controlEffectC);
            asset.setNumericProperty(Asset.ASSET_CONTROLRISK_I,
                    asset.getNumericProperty(Asset.ASSET_CONTROLRISK_I) - controlEffectI);
            asset.setNumericProperty(Asset.ASSET_CONTROLRISK_A,
                    asset.getNumericProperty(Asset.ASSET_CONTROLRISK_A) - controlEffectA);
        }
        
    }

    private void saveLink(Edge edge) {
        CnALink link = new CnALink(edge.getSource(), edge.getTarget(), edge.getType(), edge.getDescription());
        link.setRiskConfidentiality(edge.getRiskConfidentiality());
        link.setRiskIntegrity(edge.getRiskIntegrity());
        link.setRiskAvailability(edge.getRiskAvailability());
        link.setRiskConfidentialityWithControls(edge.getRiskConfidentialityWithControls());
        link.setRiskIntegrityWithControls(edge.getRiskIntegrityWithControls());
        link.setRiskAvailabilityWithControls(edge.getRiskAvailabilityWithControls());
        link.setRiskTreatment(edge.getRiskTreatment());
        cnaLinkDao.merge(link);
        
    }

    /**
     * Reset risk calculation, remove all calculated risk values in an asset.
     * 
     * @param asset
     */
    private void resetRisks(Asset asset) {
        asset.setNumericProperty(Asset.ASSET_RISK_C, 0);
        asset.setNumericProperty(Asset.ASSET_RISK_I, 0);
        asset.setNumericProperty(Asset.ASSET_RISK_A, 0);
        asset.setNumericProperty(Asset.ASSET_CONTROLRISK_C, 0);
        asset.setNumericProperty(Asset.ASSET_CONTROLRISK_I, 0);
        asset.setNumericProperty(Asset.ASSET_CONTROLRISK_A, 0);
        asset.setNumericProperty(Asset.ASSET_PLANCONTROLRISK_C, 0);
        asset.setNumericProperty(Asset.ASSET_PLANCONTROLRISK_I, 0);
        asset.setNumericProperty(Asset.ASSET_PLANCONTROLRISK_A, 0);
        asset.setNumericProperty(Asset.ASSET_WITHOUT_NA_PLANCONTROLRISK_C, 0);
        asset.setNumericProperty(Asset.ASSET_WITHOUT_NA_PLANCONTROLRISK_I, 0);
        asset.setNumericProperty(Asset.ASSET_WITHOUT_NA_PLANCONTROLRISK_A, 0);
    }
    
    private void resetRisks(Edge edgeToAsset) {
        edgeToAsset.setRiskConfidentiality(0);
        edgeToAsset.setRiskConfidentialityWithControls(0);
        
        edgeToAsset.setRiskIntegrity(0);
        edgeToAsset.setRiskIntegrityWithControls(0);
                
        edgeToAsset.setRiskAvailability(0);
        edgeToAsset.setRiskAvailabilityWithControls(0);
    }

 
    private void getProbabilityFromThreatAndVulnerability(VeriniceGraph graph, IncidentScenario scenario) {
        // only calculate if threat AND vulnerability is linked to scenario:     
        Set<CnATreeElement> threatSet = graph.getLinkTargetsByElementType(scenario, Threat.TYPE_ID);
        Set<CnATreeElement> vulnerabilitySet = graph.getLinkTargetsByElementType(scenario, Vulnerability.TYPE_ID);
         
        if (!threatSet.isEmpty() && !vulnerabilitySet.isEmpty()) {
            int threatImpact = 0;
            for (CnATreeElement threat : threatSet) {
                //use higher value of likelihood or impact:
                int level1 = threat.getNumericProperty(Threat.PROP_THREAT_LIKELIHOOD);
                int level2 = threat.getNumericProperty(Threat.PROP_THREAT_IMPACT);
                int level = (level1 > level2) ? level1 : level2;
                threatImpact = (level > threatImpact) ? level : threatImpact;
            }
            
            int exploitability = 0;
            for (CnATreeElement vuln : vulnerabilitySet) {
                int level = vuln.getNumericProperty(Vulnerability.PROP_VULNERABILITY_EXPLOITABILITY);
                exploitability = (level > exploitability) ? level : exploitability;
            }
            
            // set values to highest found:
            scenario.setNumericProperty(IncidentScenario.PROP_SCENARIO_THREAT_PROBABILITY, threatImpact);
            scenario.setNumericProperty(IncidentScenario.PROP_SCENARIO_VULN_PROBABILITY, exploitability);
        }
    }
    
    private boolean scenarioAffectsConfidentiality(IncidentScenario scenario) {
        return scenario.getNumericProperty(IncidentScenario.PROP_SCENARIO_AFFECTS_C) == 1;
    }

    private boolean scenarioAffectsIntegrity(IncidentScenario scenario) {
        return scenario.getNumericProperty(IncidentScenario.PROP_SCENARIO_AFFECTS_I) == 1;
    }
    
    private boolean scenarioAffectsAvailability(IncidentScenario scenario) {
        return scenario.getNumericProperty(IncidentScenario.PROP_SCENARIO_AFFECTS_A) == 1;
    }
   
    private VeriniceGraph loadGraph(Integer[] scopeIds) { 
        IGraphElementLoader loader = new GraphElementLoader();
        if(scopeIds!=null) {
            loader.setScopeIds(scopeIds);
        }
        loader.setTypeIds(new String[]{Asset.TYPE_ID, IncidentScenario.TYPE_ID, Control.TYPE_ID, Threat.TYPE_ID, Vulnerability.TYPE_ID});
        getGraphService().setLoader(loader);
        return getGraphService().create() ;          
    }

    public IBaseDao<CnALink, Serializable> getCnaLinkDao() {
        return cnaLinkDao;
    }

    public void setCnaLinkDao(IBaseDao<CnALink, Serializable> cnaLinkDao) {
        this.cnaLinkDao = cnaLinkDao;
    }

    public IGraphService getGraphService() {
        return graphService;
    }

    public void setGraphService(IGraphService graphService) {
        this.graphService = graphService;
    }
    
    private long initRuntime() {
        long time = 0;
        if (LOG_RUNTIME.isDebugEnabled()) {
            time = System.currentTimeMillis();
        }
        return time;
    }
    
    private void logRuntime(String message, long starttime) {
        LOG_RUNTIME.debug(message + TimeFormatter.getHumanRedableTime(System.currentTimeMillis()-starttime));
    }

}
