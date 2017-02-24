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
import sernet.verinice.model.iso27k.AssetValueAdapter;
import sernet.verinice.model.iso27k.Control;
import sernet.verinice.model.iso27k.IncidentScenario;
import sernet.verinice.model.iso27k.Threat;
import sernet.verinice.model.iso27k.Vulnerability;

/**
 * Service implementation to run a ISO/IEC 27005 risk analysis.
 *  
 * This implementation needs a IGraphService and a IBaseDao<CnALink, Serializable>
 * to run.
 *  
 * @author koderman@sernet.de
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 */
public class RiskAnalysisServiceImpl implements RiskAnalysisService {
    
    private static final transient Logger LOG = Logger.getLogger(RiskAnalysisServiceImpl.class);
    private static final Logger LOG_RUNTIME = Logger.getLogger(RiskAnalysisServiceImpl.class.getName() + ".runtime");
    
    private IGraphService graphService;  
    private IBaseDao<CnALink, Serializable> cnaLinkDao;  
    private RiskAnalysisHelper riskAnalysisHelper = new RiskAnalysisHelperImpl();
    
    /**
     * The service needs a VeriniceGraph and a IBaseDao<CnALink, Serializable>
     * to run.
     */
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
    

    private void runRiskAnalysis(VeriniceGraph graph) {
        // update asset values (business impact, CIA):
        // done on every save, no need to do it here
        Set<CnATreeElement> scenarios = graph.getElements(IncidentScenario.TYPE_ID);
        if (LOG.isInfoEnabled()) {
            LOG.info("Number of scenarios: " + scenarios.size());
        }
        for (CnATreeElement scenario : scenarios) {
            if(LOG.isDebugEnabled()){
                LOG.debug("Determine Probability for Scenario:\t" 
                        + scenario.getTitle());
            }
            determineProbability(graph, (IncidentScenario) scenario);
        }
     
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

        // determine risk originating from scenarios for all linked assets:
        for (CnATreeElement scenario : scenarios) {
            if(LOG.isDebugEnabled()){
                LOG.debug("Determine Risk for Scenario:\t" + scenario.getTitle());
            }            
            try {
                determineRisks(graph, (IncidentScenario) scenario);
            } catch (CommandException e) {
                LOG.error("Error while determine risk", e);
            }
        }
    }

    /**
     * Determine probability for this scenario, based on threat and
     * vulnerability.
     *
     * @param scenario
     */
    private void determineProbability(VeriniceGraph graph, IncidentScenario scenario) {
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
            deductControlFromSzenario(scenario, control);
        }
    }

    /** 
     * Determine risks for linked assets from this scenario.
     *
     * @param scenario
     * @throws CommandException
     */
    private void determineRisks(VeriniceGraph graph, IncidentScenario scenario) throws CommandException {
        Set<Edge> edgesToAsset = graph.getEdgesByElementType(scenario, Asset.TYPE_ID);
      
        for (Edge edge : edgesToAsset) {
            determineRisk(scenario, edge);
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

    private void determineRisk(IncidentScenario scenario, Edge edgeToAsset) throws CommandException {
        Asset asset = (Asset) edgeToAsset.getTarget();
        AssetValueAdapter valueAdapter = new AssetValueAdapter(asset);

        // reset risk values for link:
        edgeToAsset.setRiskConfidentiality(0);
        edgeToAsset.setRiskIntegrity(0);
        edgeToAsset.setRiskAvailability(0);

        // get reduced impact of asset:
        Integer[] impactWithImplementedControlsCIA = riskAnalysisHelper.applyControlsToImpact(
                RiskAnalysisService.RISK_WITH_IMPLEMENTED_CONTROLS, asset,
                valueAdapter.getVertraulichkeit(), valueAdapter.getIntegritaet(),
                valueAdapter.getVerfuegbarkeit());

        Integer[] impactWithAllControlsCIA = riskAnalysisHelper.applyControlsToImpact(
                RiskAnalysisService.RISK_WITH_ALL_CONTROLS, asset,
                valueAdapter.getVertraulichkeit(), valueAdapter.getIntegritaet(),
                valueAdapter.getVerfuegbarkeit());

        Integer[] impactWithAllPlannedControlsCIA = riskAnalysisHelper.applyControlsToImpact(
                RiskAnalysisService.RISK_WITHOUT_NA_CONTROLS, asset,
                valueAdapter.getVertraulichkeit(), valueAdapter.getIntegritaet(),
                valueAdapter.getVerfuegbarkeit());

        if (scenario.getNumericProperty(IncidentScenario.PROP_SCENARIO_AFFECTS_C) == 1) {
            // increase total asset risk by this combination's risk, saving
            // this individual combination's risk in the link between the
            // two objects:
            // without any controls:
            int risk = valueAdapter.getVertraulichkeit()
                    + scenario.getNumericProperty(IncidentScenario.PROP_SCENARIO_PROBABILITY);
            int riskImplControls = impactWithImplementedControlsCIA[0]
                    + scenario.getNumericProperty(IncidentScenario.PROP_SCENARIO_PROBABILITY_WITH_CONTROLS);
            int riskAllControls = impactWithAllControlsCIA[0]
                    + scenario.getNumericProperty(IncidentScenario.PROP_SCENARIO_PROBABILITY_WITH_PLANNED_CONTROLS);
            int riskPlannedControls = impactWithAllPlannedControlsCIA[0]
                    + scenario.getNumericProperty(IncidentScenario.PROP_SCENARIO_PROBABILITY_WITHOUT_NA_CONTROLS);

            edgeToAsset.setRiskConfidentiality(risk);
            edgeToAsset.setRiskConfidentialityWithControls(riskImplControls);

            asset.setNumericProperty(Asset.ASSET_RISK_C,
                    asset.getNumericProperty(Asset.ASSET_RISK_C) + risk);
            asset.setNumericProperty(Asset.ASSET_CONTROLRISK_C,
                    asset.getNumericProperty(Asset.ASSET_CONTROLRISK_C) + riskImplControls);
            asset.setNumericProperty(Asset.ASSET_PLANCONTROLRISK_C,
                    asset.getNumericProperty(Asset.ASSET_PLANCONTROLRISK_C) + riskAllControls);
            asset.setNumericProperty(Asset.ASSET_WITHOUT_NA_PLANCONTROLRISK_C,
                    asset.getNumericProperty(Asset.ASSET_WITHOUT_NA_PLANCONTROLRISK_C)
                            + riskPlannedControls);
        }

        if (scenario.getNumericProperty(IncidentScenario.PROP_SCENARIO_AFFECTS_I) == 1) {
            // increase total asset risk by this combination's risk
            int risk = valueAdapter.getIntegritaet()
                    + scenario.getNumericProperty(IncidentScenario.PROP_SCENARIO_PROBABILITY);
            int riskImplControls = impactWithImplementedControlsCIA[1]
                    + scenario.getNumericProperty(IncidentScenario.PROP_SCENARIO_PROBABILITY_WITH_CONTROLS);
            int riskAllControls = impactWithAllControlsCIA[1]
                    + scenario.getNumericProperty(IncidentScenario.PROP_SCENARIO_PROBABILITY_WITH_PLANNED_CONTROLS);
            int riskPlannedControls = impactWithAllPlannedControlsCIA[1]
                    + scenario.getNumericProperty(IncidentScenario.PROP_SCENARIO_PROBABILITY_WITHOUT_NA_CONTROLS);

            edgeToAsset.setRiskIntegrity(risk);
            edgeToAsset.setRiskIntegrityWithControls(riskImplControls);

            asset.setNumericProperty(Asset.ASSET_RISK_I,
                    asset.getNumericProperty(Asset.ASSET_RISK_I) + risk);
            asset.setNumericProperty(Asset.ASSET_CONTROLRISK_I,
                    asset.getNumericProperty(Asset.ASSET_CONTROLRISK_I) + riskImplControls);
            asset.setNumericProperty(Asset.ASSET_PLANCONTROLRISK_I,
                    asset.getNumericProperty(Asset.ASSET_PLANCONTROLRISK_I) + riskAllControls);
            asset.setNumericProperty(Asset.ASSET_WITHOUT_NA_PLANCONTROLRISK_I,
                    asset.getNumericProperty(Asset.ASSET_WITHOUT_NA_PLANCONTROLRISK_I)
                            + riskPlannedControls);

        }

        if (scenario.getNumericProperty(IncidentScenario.PROP_SCENARIO_AFFECTS_A) == 1) {
            // increase total asset risk by this combination's risk
            int risk = valueAdapter.getVerfuegbarkeit()
                    + scenario.getNumericProperty(IncidentScenario.PROP_SCENARIO_PROBABILITY);
            int riskImplControls = impactWithImplementedControlsCIA[2]
                    + scenario.getNumericProperty(IncidentScenario.PROP_SCENARIO_PROBABILITY_WITH_CONTROLS);
            int riskAllControls = impactWithAllControlsCIA[2]
                    + scenario.getNumericProperty(IncidentScenario.PROP_SCENARIO_PROBABILITY_WITH_PLANNED_CONTROLS);
            int riskPlannedControls = impactWithAllPlannedControlsCIA[2]
                    + scenario.getNumericProperty(IncidentScenario.PROP_SCENARIO_PROBABILITY_WITHOUT_NA_CONTROLS);

            edgeToAsset.setRiskAvailability(risk);
            edgeToAsset.setRiskAvailabilityWithControls(riskImplControls);

            asset.setNumericProperty(Asset.ASSET_RISK_A,
                    asset.getNumericProperty(Asset.ASSET_RISK_A) + risk);
            asset.setNumericProperty(Asset.ASSET_CONTROLRISK_A,
                    asset.getNumericProperty(Asset.ASSET_CONTROLRISK_A) + riskImplControls);
            asset.setNumericProperty(Asset.ASSET_PLANCONTROLRISK_A,
                    asset.getNumericProperty(Asset.ASSET_PLANCONTROLRISK_A) + riskAllControls);
            asset.setNumericProperty(Asset.ASSET_WITHOUT_NA_PLANCONTROLRISK_A,
                    asset.getNumericProperty(Asset.ASSET_WITHOUT_NA_PLANCONTROLRISK_A)
                            + riskPlannedControls);
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

    private void deductControlFromSzenario(IncidentScenario scenario, CnATreeElement control) {
        int controlEffect = control.getNumericProperty(Control.PROP_CONTROL_EFFECT_P);
        int probAfterControl;
        // risk with planned controls
        probAfterControl = scenario.getNumericProperty(
                IncidentScenario.PROP_SCENARIO_PROBABILITY_WITH_PLANNED_CONTROLS) - controlEffect;
        scenario.setNumericProperty(IncidentScenario.PROP_SCENARIO_PROBABILITY_WITH_PLANNED_CONTROLS,
                probAfterControl < 0 ? 0 : probAfterControl);
        if (Control.isImplemented(control.getEntity())) {
            // risk with implemented controls
            probAfterControl = scenario.getNumericProperty(IncidentScenario.PROP_SCENARIO_PROBABILITY_WITH_CONTROLS)
                    - controlEffect;
            scenario.setNumericProperty(IncidentScenario.PROP_SCENARIO_PROBABILITY_WITH_CONTROLS,
                    probAfterControl < 0 ? 0 : probAfterControl);
        }

        if (Control.isPlanned(control.getEntity())) {
            probAfterControl = scenario.getNumericProperty(
                    IncidentScenario.PROP_SCENARIO_PROBABILITY_WITHOUT_NA_CONTROLS) - controlEffect;
            scenario.setNumericProperty(IncidentScenario.PROP_SCENARIO_PROBABILITY_WITHOUT_NA_CONTROLS,
                    probAfterControl < 0 ? 0 : probAfterControl);
        }
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

    public RiskAnalysisHelper getRiskAnalysisHelper() {
        return riskAnalysisHelper;
    }

    public void setRiskAnalysisHelper(RiskAnalysisHelper riskAnalysisHelper) {
        this.riskAnalysisHelper = riskAnalysisHelper;
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
