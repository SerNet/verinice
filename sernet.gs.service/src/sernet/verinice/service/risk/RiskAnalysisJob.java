/*******************************************************************************
 * Copyright (c) 2017 Daniel Murygin <dm{a}sernet{dot}de>.
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
 *     Daniel Murygin <dm{a}sernet{dot}de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.service.risk;

import java.io.Serializable;
import java.util.Set;

import org.apache.log4j.Logger;

import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.graph.Edge;
import sernet.verinice.interfaces.graph.VeriniceGraph;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Asset;
import sernet.verinice.model.iso27k.Control;
import sernet.verinice.model.iso27k.IncidentScenario;
import sernet.verinice.model.iso27k.Threat;
import sernet.verinice.model.iso27k.Vulnerability;

/**
 * A job which runs a risk analysis on a verinice graph. 
 * The verinice graph contains all elements which are analyzed.
 * 
 * This Job is created and executed by class RiskAnalysisServiceImpl.
 * RiskAnalysisServiceImpl is configured as a singleton. In contrast to this
 * a new instance of RiskAnalysisJob is created for every execution of
 * a risk analysis.
 *
 * @author Daniel Murygin <dm{a}sernet{dot}de>
 */
public class RiskAnalysisJob {

    private static final Logger LOG = Logger.getLogger(RiskAnalysisJob.class);

    private static final RiskCalculator RISK_CALCULATOR_DEFAULT = new RiskAdder();
    
    /**
     * A verinice graph with all elements which are analyzed
     */
    private VeriniceGraph graph;
    
    private RiskCalculator riskCalculator;
    
    private IBaseDao<CnALink, Serializable> cnaLinkDao;
    
    public RiskAnalysisJob(VeriniceGraph graph, IBaseDao<CnALink, Serializable> cnaLinkDao) {
        super();
        this.graph = graph;
        this.cnaLinkDao = cnaLinkDao;
    }

    /**
     * Runs a risk analysis by analyzing the risk of
     * scenarios and assets.
     */
    public void runRiskAnalysis() {
        resetRiskValuesOfAssets();
        setProbabilityOfScenarios();
        reduceRiskOfScenarios();
        analyseRiskOfAssets();
    }
    
    /**
     * Resets the risk values of all assets.
     */
    private void resetRiskValuesOfAssets() {
        Set<CnATreeElement> assets = graph.getElements(Asset.TYPE_ID);
        if (LOG.isInfoEnabled()) {
            LOG.info("Number of assets: " + assets.size());
        }
        for (CnATreeElement asset : assets) {
            if(LOG.isDebugEnabled()){
                LOG.debug("Resetting Risk for Asset: " + asset.getTitle());
            }
            resetRisks((Asset) asset);
        }
    }

    /**
     * Sets the probability of all incident scenarios.
     */
    private void setProbabilityOfScenarios() {
        Set<CnATreeElement> scenarios = graph.getElements(IncidentScenario.TYPE_ID);
        if (LOG.isInfoEnabled()) {
            LOG.info("Number of scenarios: " + scenarios.size());
        }
        for (CnATreeElement scenario : scenarios) {
            setProbabilityOfScenario((IncidentScenario) scenario);          
        }
    }

    /**
     * Sets the probability of an incident scenario.
     * 
     * The probability of the scenario is set by addition of
     * the probability of the threat and the vulnerability which are
     * linked to the scenario.
     * 
     * @param scenario A single incident scenario which is analyzed
     */
    private void setProbabilityOfScenario(IncidentScenario scenario) {
        // get values from linked threat & vuln, only if automatic mode is
        // activated:
        if (scenario.getNumericProperty(IncidentScenario.PROP_SCENARIO_METHOD) == 1) {
            getProbabilityFromThreatAndVulnerability(graph, scenario);
        }

        // calculate probability:
        scenario.setNumericProperty(IncidentScenario.PROP_SCENARIO_PROBABILITY, 0);
        int myThreat = scenario
                .getNumericProperty(IncidentScenario.PROP_SCENARIO_THREAT_PROBABILITY);
        int myVuln = scenario.getNumericProperty(IncidentScenario.PROP_SCENARIO_VULN_PROBABILITY);
        scenario.setNumericProperty(IncidentScenario.PROP_SCENARIO_PROBABILITY, myThreat + myVuln);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Scenario: " + scenario.getTitle() + ", probability set: "
                    + scenario.getNumericProperty(IncidentScenario.PROP_SCENARIO_PROBABILITY));
        }
    }
    
    /**
     * Reduces the probability of all incident scenarios by subtracting
     * the effect of all linked and implemented controls.
     */
    private void reduceRiskOfScenarios() {
        Set<CnATreeElement> scenarios = graph.getElements(IncidentScenario.TYPE_ID);
        for (CnATreeElement scenario : scenarios) {
            reduceRiskOfSzenario((IncidentScenario) scenario);          
        }
    }
    
    /**
     * Reduces the probability of a scenario by subtracting
     * the effect of all linked and implemented controls.
     *
     * @param scenario A single incident scenario
     */
    private void reduceRiskOfSzenario(IncidentScenario scenario) {    
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
    
    private void reduceRiskOfSzenarioWithControl(IncidentScenario scenario,
            CnATreeElement control) {
        int controlEffect = control.getNumericProperty(Control.PROP_CONTROL_EFFECT_P);
        int probAfterControl;

        // Risk with all controls
        probAfterControl = scenario.getNumericProperty(
                IncidentScenario.PROP_SCENARIO_PROBABILITY_WITH_PLANNED_CONTROLS) - controlEffect;
        scenario.setNumericProperty(
                IncidentScenario.PROP_SCENARIO_PROBABILITY_WITH_PLANNED_CONTROLS,
                positiveOrZero(probAfterControl));

        // Risk with implemented controls
        if (Control.isImplemented(control.getEntity())) {

            probAfterControl = scenario.getNumericProperty(
                    IncidentScenario.PROP_SCENARIO_PROBABILITY_WITH_CONTROLS) - controlEffect;
            scenario.setNumericProperty(IncidentScenario.PROP_SCENARIO_PROBABILITY_WITH_CONTROLS,
                    positiveOrZero(probAfterControl));
        }

        // Risk with planned implemented controls
        if (Control.isPlanned(control.getEntity())) {
            probAfterControl = scenario.getNumericProperty(
                    IncidentScenario.PROP_SCENARIO_PROBABILITY_WITHOUT_NA_CONTROLS) - controlEffect;
            scenario.setNumericProperty(
                    IncidentScenario.PROP_SCENARIO_PROBABILITY_WITHOUT_NA_CONTROLS,
                    positiveOrZero(probAfterControl));
        }
    }

    /**
     * Returns the risk for a given business impact of an asset and
     * a given probability of occurrence for a incident scenario.
     * 
     * @param businessImpact A business impact of an asset
     * @param probability The probability of occurrence for a incident scenario
     * @return A risk value for the given parameters
     */
    protected int calculateRisk(int businessImpact, int probability) {
        return getRiskCalculator().calculateRiskFromBusinessImpactAndProbability(businessImpact, probability);
    }
    
    /**
     * Analyses the risk of all assets which are linked to incident scenarios
     * in a given verinice graph.
     * 
     * @throws CommandException 
     */
    private void analyseRiskOfAssets() {
        Set<CnATreeElement> scenarios = graph.getElements(IncidentScenario.TYPE_ID);
        // determine risk originating from scenarios for all linked assets:
        for (CnATreeElement scenario : scenarios) {
            if(LOG.isDebugEnabled()){
                LOG.debug("Determine Risk for Scenario: " + scenario.getTitle());
            }                
            analyseRiskOfAssets((IncidentScenario) scenario);       
        }
    }

    /** 
     * Analyses the risk of all assets which are linked to a given incident scenario.
     *
     * @param graph A verinice graph with all elements which are analyzed
     * @param scenario A single incident scenario
     * @throws CommandException
     */
    private void analyseRiskOfAssets(IncidentScenario scenario)  {
        Set<Edge> edgesToAsset = graph.getEdgesByElementType(scenario, Asset.TYPE_ID);
      
        for (Edge edge : edgesToAsset) {
            analyseRiskOfAsset(graph, scenario, edge);
        }  
        if (LOG.isInfoEnabled()) {
            LOG.info("Number of links from scenario to assets: " + edgesToAsset.size());
        }
        long time = RiskAnalysisServiceImpl.initRuntime();
        // Update cnalinks
        for (Edge edge : edgesToAsset) {
            saveLink(edge);
        }
        RiskAnalysisServiceImpl.logRuntime("Updating links from scenarios to assets runtime : ", time);
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

    /**
     * Abbreviations used in this method to shorten in names of variables: -
     * business impact: bi - confidentiality: C
     * 
     * @param scenario
     * @param edgeToAsset
     * @param assetWithReducedCIAValues
     */
    private void analyseRiskCOfAsset(IncidentScenario scenario, Edge edgeToAsset,
            Asset assetWithReducedCIAValues) {
        Asset asset = (Asset) edgeToAsset.getTarget();

        // Without controls:
        int biC = asset.getNumericProperty(Asset.ASSET_VALUE_CONFIDENTIALITY);
        int probability = scenario.getNumericProperty(IncidentScenario.PROP_SCENARIO_PROBABILITY);
        int riskC = calculateRisk(biC, probability);
        asset.setNumericProperty(Asset.ASSET_RISK_C,
                positiveOrZero(asset.getNumericProperty(Asset.ASSET_RISK_C) + riskC));
        edgeToAsset.setRiskConfidentiality(positiveOrZero(riskC));

        // With implemented controls
        int biWithImplementedControlsC = assetWithReducedCIAValues
                .getNumericProperty(Asset.ASSET_CONTROLRISK_C);
        int probabilityWithImplementedControls = scenario
                .getNumericProperty(IncidentScenario.PROP_SCENARIO_PROBABILITY_WITH_CONTROLS);
        int riskWithImplementedControlsC = calculateRisk(biWithImplementedControlsC,
                probabilityWithImplementedControls);
        asset.setNumericProperty(Asset.ASSET_CONTROLRISK_C,
                positiveOrZero(asset.getNumericProperty(Asset.ASSET_CONTROLRISK_C)
                        + riskWithImplementedControlsC));
        edgeToAsset
                .setRiskConfidentialityWithControls(positiveOrZero(riskWithImplementedControlsC));

        // With all controls
        int biWithAllControlsC = assetWithReducedCIAValues
                .getNumericProperty(Asset.ASSET_PLANCONTROLRISK_C);
        int probabilityWithAllControls = scenario.getNumericProperty(
                IncidentScenario.PROP_SCENARIO_PROBABILITY_WITH_PLANNED_CONTROLS);
        int riskWithAllControlsC = calculateRisk(biWithAllControlsC, probabilityWithAllControls);
        asset.setNumericProperty(Asset.ASSET_PLANCONTROLRISK_C, positiveOrZero(
                asset.getNumericProperty(Asset.ASSET_PLANCONTROLRISK_C) + riskWithAllControlsC));

        // With planned controls
        int biWithPlannedControlsC = assetWithReducedCIAValues
                .getNumericProperty(Asset.ASSET_WITHOUT_NA_PLANCONTROLRISK_C);
        int probabilityWithPlannedControls = scenario
                .getNumericProperty(IncidentScenario.PROP_SCENARIO_PROBABILITY_WITHOUT_NA_CONTROLS);
        int riskWithPlannedControlsC = calculateRisk(biWithPlannedControlsC,
                probabilityWithPlannedControls);
        asset.setNumericProperty(Asset.ASSET_WITHOUT_NA_PLANCONTROLRISK_C,
                positiveOrZero(asset.getNumericProperty(Asset.ASSET_WITHOUT_NA_PLANCONTROLRISK_C)
                        + riskWithPlannedControlsC));
    }
    
    /**
     * Abbreviations used in this method to shorten in names of variables:
     *  - Business impact: bi
     *  - Integrity: I
     * 
     * @param scenario
     * @param edgeToAsset
     * @param assetWithReducedCIAValues
     */
    private void analyseRiskIOfAsset(IncidentScenario scenario, Edge edgeToAsset,
            Asset assetWithReducedCIAValues) {
        Asset asset = (Asset) edgeToAsset.getTarget();

        // Without controls:
        int biI = asset.getNumericProperty(Asset.ASSET_VALUE_INTEGRITY);
        int probability = scenario.getNumericProperty(IncidentScenario.PROP_SCENARIO_PROBABILITY);
        int riskI = calculateRisk(biI, probability);
        asset.setNumericProperty(Asset.ASSET_RISK_I,
                positiveOrZero(asset.getNumericProperty(Asset.ASSET_RISK_I) + riskI));
        edgeToAsset.setRiskIntegrity(positiveOrZero(riskI));

        // With implemented controls
        int biWithImplementedControlsI = assetWithReducedCIAValues
                .getNumericProperty(Asset.ASSET_CONTROLRISK_I);
        int probabilityWithImplementedControls = scenario
                .getNumericProperty(IncidentScenario.PROP_SCENARIO_PROBABILITY_WITH_CONTROLS);
        int riskWithImplementedControlsI = calculateRisk(biWithImplementedControlsI,
                probabilityWithImplementedControls);
        asset.setNumericProperty(Asset.ASSET_CONTROLRISK_I,
                positiveOrZero(asset.getNumericProperty(Asset.ASSET_CONTROLRISK_I) + riskWithImplementedControlsI));
        edgeToAsset.setRiskIntegrityWithControls(positiveOrZero(riskWithImplementedControlsI));

        // With all controls
        int biWithAllControlsI = assetWithReducedCIAValues
                .getNumericProperty(Asset.ASSET_PLANCONTROLRISK_I);
        int probabilityWithAllControls = scenario.getNumericProperty(
                IncidentScenario.PROP_SCENARIO_PROBABILITY_WITH_PLANNED_CONTROLS);
        int riskWithAllControlsI = calculateRisk(biWithAllControlsI, probabilityWithAllControls);
        asset.setNumericProperty(Asset.ASSET_PLANCONTROLRISK_I,
                positiveOrZero(asset.getNumericProperty(Asset.ASSET_PLANCONTROLRISK_I) + riskWithAllControlsI));

        // With planned controls
        int biWithPlannedControlsI = assetWithReducedCIAValues
                .getNumericProperty(Asset.ASSET_WITHOUT_NA_PLANCONTROLRISK_I);
        int probabilityWithPlannedControls = scenario
                .getNumericProperty(IncidentScenario.PROP_SCENARIO_PROBABILITY_WITHOUT_NA_CONTROLS);
        int riskWithPlannedControlsI = calculateRisk(biWithPlannedControlsI,
                probabilityWithPlannedControls);
        asset.setNumericProperty(Asset.ASSET_WITHOUT_NA_PLANCONTROLRISK_I,
                positiveOrZero(asset.getNumericProperty(Asset.ASSET_WITHOUT_NA_PLANCONTROLRISK_I)
                        + riskWithPlannedControlsI));
    }
    
    /**
     * Abbreviations used in this method to shorten in names of variables:
     *  - Business impact: bi
     *  - Availability: A
     * 
     * @param scenario
     * @param edgeToAsset
     * @param assetWithReducedCIAValues
     */
    private void analyseRiskAOfAsset(IncidentScenario scenario, Edge edgeToAsset,
            Asset assetWithReducedCIAValues) {
        Asset asset = (Asset) edgeToAsset.getTarget();

        // Without controls:
        int biA = asset.getNumericProperty(Asset.ASSET_VALUE_AVAILABILITY);
        int probability = scenario.getNumericProperty(IncidentScenario.PROP_SCENARIO_PROBABILITY);
        int riskA = calculateRisk(biA, probability);
        asset.setNumericProperty(Asset.ASSET_RISK_A,
                positiveOrZero(asset.getNumericProperty(Asset.ASSET_RISK_A) + riskA));
        edgeToAsset.setRiskAvailability(positiveOrZero(riskA));

        // With implemented controls
        int biWithImplementedControlsA = assetWithReducedCIAValues
                .getNumericProperty(Asset.ASSET_CONTROLRISK_A);
        int probabilityWithImplementedControls = scenario
                .getNumericProperty(IncidentScenario.PROP_SCENARIO_PROBABILITY_WITH_CONTROLS);
        int riskWithImplementedControlsA = calculateRisk(biWithImplementedControlsA,
                probabilityWithImplementedControls);
        asset.setNumericProperty(Asset.ASSET_CONTROLRISK_A,
                positiveOrZero(asset.getNumericProperty(Asset.ASSET_CONTROLRISK_A) + riskWithImplementedControlsA));
        edgeToAsset.setRiskAvailabilityWithControls(positiveOrZero(riskWithImplementedControlsA));

        // With all controls
        int biWithAllControlsA = assetWithReducedCIAValues
                .getNumericProperty(Asset.ASSET_PLANCONTROLRISK_A);
        int probabilityWithAllControls = scenario.getNumericProperty(
                IncidentScenario.PROP_SCENARIO_PROBABILITY_WITH_PLANNED_CONTROLS);
        int riskWithAllControlsA = calculateRisk(biWithAllControlsA, probabilityWithAllControls);
        asset.setNumericProperty(Asset.ASSET_PLANCONTROLRISK_A,
                positiveOrZero(asset.getNumericProperty(Asset.ASSET_PLANCONTROLRISK_A) + riskWithAllControlsA));

        // With planned controls
        int biWithPlannedControlsA = assetWithReducedCIAValues
                .getNumericProperty(Asset.ASSET_WITHOUT_NA_PLANCONTROLRISK_A);
        int probabilityWithPlannedControls = scenario
                .getNumericProperty(IncidentScenario.PROP_SCENARIO_PROBABILITY_WITHOUT_NA_CONTROLS);
        int riskWithPlannedControlsA = calculateRisk(biWithPlannedControlsA,
                probabilityWithPlannedControls);
        asset.setNumericProperty(Asset.ASSET_WITHOUT_NA_PLANCONTROLRISK_A,
                positiveOrZero(asset.getNumericProperty(Asset.ASSET_WITHOUT_NA_PLANCONTROLRISK_A)
                        + riskWithPlannedControlsA));
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
        int controlEffectC = control.getNumericProperty(Control.PROP_EFFECTIVENESS_CONFIDENTIALITY);
        int controlEffectI = control.getNumericProperty(Control.PROP_EFFECTIVENESS_INTEGRITY);
        int controlEffectA = control.getNumericProperty(Control.PROP_EFFECTIVENESS_AVAILABILITY);
        
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
    
    /**
     * @param n
     *            A positive or negative number
     * @return The given number if n is positive or 0 if n is 0 or negative
     */
    private int positiveOrZero(int n) {
        return n < 0 ? 0 : n;
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
    
    public RiskCalculator getRiskCalculator() {
        if(riskCalculator==null) {
            riskCalculator = RISK_CALCULATOR_DEFAULT;
        }
        return riskCalculator;
    }

    public void setRiskCalculator(RiskCalculator riskCalculator) {
        this.riskCalculator = riskCalculator;
    }

    public IBaseDao<CnALink, Serializable> getCnaLinkDao() {
        return cnaLinkDao;
    }

    public void setCnaLinkDao(IBaseDao<CnALink, Serializable> cnaLinkDao) {
        this.cnaLinkDao = cnaLinkDao;
    }
}
