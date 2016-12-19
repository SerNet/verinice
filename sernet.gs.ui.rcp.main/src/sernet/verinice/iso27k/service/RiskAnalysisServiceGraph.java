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

import sernet.gs.service.RetrieveInfo;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.graph.Edge;
import sernet.verinice.interfaces.graph.VeriniceGraph;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Asset;
import sernet.verinice.model.iso27k.AssetValueAdapter;
import sernet.verinice.model.iso27k.Control;
import sernet.verinice.model.iso27k.IncidentScenario;
import sernet.verinice.model.iso27k.Organization;
import sernet.verinice.model.iso27k.Threat;
import sernet.verinice.model.iso27k.Vulnerability;
import sernet.verinice.service.commands.RetrieveCnATreeElement;

/**
 * Performs risk analysis according to ISO 27005.
 *  
 * @author koderman@sernet.de
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 */
public class RiskAnalysisServiceGraph implements IRiskAnalysisService {
    
    private static final transient Logger LOG = Logger.getLogger(RiskAnalysisServiceGraph.class);

    private VeriniceGraph graph;
    private IBaseDao<CnALink, Serializable> cnaLinkDao;
    
    /**
     * @param graph
     * @param cnaLinkDao2 
     */
    public RiskAnalysisServiceGraph(VeriniceGraph graph, IBaseDao<CnALink,Serializable> cnaLinkDao) {
        this.graph = graph;
        this.cnaLinkDao = cnaLinkDao;
    }

    /*
     * @see
     * sernet.verinice.iso27k.service.IRiskAnalysisService#determineProbability
     * (sernet.verinice.model.iso27k.IncidentScenario)
     */
    @Override
    public void determineProbability(IncidentScenario scenario) {
        // get values from linked threat & vuln, only if automatic mode is activated:
        if (scenario.getNumericProperty(PROP_SCENARIO_METHOD) == 1) {
            getProbabilityFromThreatAndVulnerability(scenario);
        }

        // calculate probability:
        scenario.setNumericProperty(PROP_SCENARIO_PROBABILITY, 0);
        int myThreat = scenario.getNumericProperty(PROP_SCENARIO_THREAT_PROBABILITY);
        int myVuln = scenario.getNumericProperty(PROP_SCENARIO_VULN_PROBABILITY);
        scenario.setNumericProperty(PROP_SCENARIO_PROBABILITY, myThreat + myVuln);

        // now determine probability after all applied controls:
        // init probability values to value without controls:
        scenario.setNumericProperty(PROP_SCENARIO_PROBABILITY_WITH_CONTROLS, scenario.getNumericProperty(PROP_SCENARIO_PROBABILITY));
        scenario.setNumericProperty(PROP_SCENARIO_PROBABILITY_WITH_PLANNED_CONTROLS, scenario.getNumericProperty(PROP_SCENARIO_PROBABILITY));
        scenario.setNumericProperty(PROP_SCENARIO_PROBABILITY_WITHOUT_NA_CONTROLS, scenario.getNumericProperty(PROP_SCENARIO_PROBABILITY));

        Set<CnATreeElement> controlSet = graph.getLinkTargetsByElementType(scenario, Control.TYPE_ID);
        // deduct controls from probability:
        for (CnATreeElement control : controlSet) {
            deductControlFromSzenario(scenario, control);
        }
    }

    /*
     * @see
     * sernet.verinice.iso27k.service.IRiskAnalysisService#determineRisks(sernet
     * .verinice.model.iso27k.IncidentScenario)
     */
    @Override
    public void determineRisks(IncidentScenario scenario) {
        Set<Edge> edgesToAsset = graph.getEdgesByElementType(scenario, Asset.TYPE_ID);
      
        for (Edge edge : edgesToAsset) {
            determineRisk(scenario, edge);
        }  
    
        // Update cnalinks
        for (Edge edge : edgesToAsset) {
            saveLink(edge);
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

    private void determineRisk(IncidentScenario scenario, Edge edgeToAsset) {
        Asset asset = (Asset) edgeToAsset.getTarget();
        AssetValueAdapter valueAdapter = new AssetValueAdapter(asset);

        // reset risk values for link:
        edgeToAsset.setRiskConfidentiality(0);
        edgeToAsset.setRiskIntegrity(0);
        edgeToAsset.setRiskAvailability(0);

        // get reduced impact of asset:
        Integer[] impactWithImplementedControlsCIA = applyControlsToImpact(
                IRiskAnalysisService.RISK_WITH_IMPLEMENTED_CONTROLS, asset,
                valueAdapter.getVertraulichkeit(), valueAdapter.getIntegritaet(),
                valueAdapter.getVerfuegbarkeit());

        Integer[] impactWithAllControlsCIA = applyControlsToImpact(
                IRiskAnalysisService.RISK_WITH_ALL_CONTROLS, asset,
                valueAdapter.getVertraulichkeit(), valueAdapter.getIntegritaet(),
                valueAdapter.getVerfuegbarkeit());

        Integer[] impactWithAllPlannedControlsCIA = applyControlsToImpact(
                IRiskAnalysisService.RISK_WITHOUT_NA_CONTROLS, asset,
                valueAdapter.getVertraulichkeit(), valueAdapter.getIntegritaet(),
                valueAdapter.getVerfuegbarkeit());

        if (scenario.getNumericProperty(PROP_SCENARIO_AFFECTS_C) == 1) {
            // increase total asset risk by this combination's risk, saving
            // this individual combination's risk in the link between the
            // two objects:
            // without any controls:
            int risk = valueAdapter.getVertraulichkeit()
                    + scenario.getNumericProperty(PROP_SCENARIO_PROBABILITY);
            int riskImplControls = impactWithImplementedControlsCIA[0]
                    + scenario.getNumericProperty(PROP_SCENARIO_PROBABILITY_WITH_CONTROLS);
            int riskAllControls = impactWithAllControlsCIA[0]
                    + scenario.getNumericProperty(PROP_SCENARIO_PROBABILITY_WITH_PLANNED_CONTROLS);
            int riskPlannedControls = impactWithAllPlannedControlsCIA[0]
                    + scenario.getNumericProperty(PROP_SCENARIO_PROBABILITY_WITHOUT_NA_CONTROLS);

            edgeToAsset.setRiskConfidentiality(risk);
            edgeToAsset.setRiskConfidentialityWithControls(riskImplControls);

            asset.setNumericProperty(PROP_ASSET_RISK_C,
                    asset.getNumericProperty(PROP_ASSET_RISK_C) + risk);
            asset.setNumericProperty(PROP_ASSET_CONTROLRISK_C,
                    asset.getNumericProperty(PROP_ASSET_CONTROLRISK_C) + riskImplControls);
            asset.setNumericProperty(PROP_ASSET_PLANCONTROLRISK_C,
                    asset.getNumericProperty(PROP_ASSET_PLANCONTROLRISK_C) + riskAllControls);
            asset.setNumericProperty(PROP_ASSET_WITHOUT_NA_PLANCONTROLRISK_C,
                    asset.getNumericProperty(PROP_ASSET_WITHOUT_NA_PLANCONTROLRISK_C)
                            + riskPlannedControls);
        }

        if (scenario.getNumericProperty(PROP_SCENARIO_AFFECTS_I) == 1) {
            // increase total asset risk by this combination's risk
            int risk = valueAdapter.getIntegritaet()
                    + scenario.getNumericProperty(PROP_SCENARIO_PROBABILITY);
            int riskImplControls = impactWithImplementedControlsCIA[1]
                    + scenario.getNumericProperty(PROP_SCENARIO_PROBABILITY_WITH_CONTROLS);
            int riskAllControls = impactWithAllControlsCIA[1]
                    + scenario.getNumericProperty(PROP_SCENARIO_PROBABILITY_WITH_PLANNED_CONTROLS);
            int riskPlannedControls = impactWithAllPlannedControlsCIA[1]
                    + scenario.getNumericProperty(PROP_SCENARIO_PROBABILITY_WITHOUT_NA_CONTROLS);

            edgeToAsset.setRiskIntegrity(risk);
            edgeToAsset.setRiskIntegrityWithControls(riskImplControls);

            asset.setNumericProperty(PROP_ASSET_RISK_I,
                    asset.getNumericProperty(PROP_ASSET_RISK_I) + risk);
            asset.setNumericProperty(PROP_ASSET_CONTROLRISK_I,
                    asset.getNumericProperty(PROP_ASSET_CONTROLRISK_I) + riskImplControls);
            asset.setNumericProperty(PROP_ASSET_PLANCONTROLRISK_I,
                    asset.getNumericProperty(PROP_ASSET_PLANCONTROLRISK_I) + riskAllControls);
            asset.setNumericProperty(PROP_ASSET_WITHOUT_NA_PLANCONTROLRISK_I,
                    asset.getNumericProperty(PROP_ASSET_WITHOUT_NA_PLANCONTROLRISK_I)
                            + riskPlannedControls);

        }

        if (scenario.getNumericProperty(PROP_SCENARIO_AFFECTS_A) == 1) {
            // increase total asset risk by this combination's risk
            int risk = valueAdapter.getVerfuegbarkeit()
                    + scenario.getNumericProperty(PROP_SCENARIO_PROBABILITY);
            int riskImplControls = impactWithImplementedControlsCIA[2]
                    + scenario.getNumericProperty(PROP_SCENARIO_PROBABILITY_WITH_CONTROLS);
            int riskAllControls = impactWithAllControlsCIA[2]
                    + scenario.getNumericProperty(PROP_SCENARIO_PROBABILITY_WITH_PLANNED_CONTROLS);
            int riskPlannedControls = impactWithAllPlannedControlsCIA[2]
                    + scenario.getNumericProperty(PROP_SCENARIO_PROBABILITY_WITHOUT_NA_CONTROLS);

            edgeToAsset.setRiskAvailability(risk);
            edgeToAsset.setRiskAvailabilityWithControls(riskImplControls);

            asset.setNumericProperty(PROP_ASSET_RISK_A,
                    asset.getNumericProperty(PROP_ASSET_RISK_A) + risk);
            asset.setNumericProperty(PROP_ASSET_CONTROLRISK_A,
                    asset.getNumericProperty(PROP_ASSET_CONTROLRISK_A) + riskImplControls);
            asset.setNumericProperty(PROP_ASSET_PLANCONTROLRISK_A,
                    asset.getNumericProperty(PROP_ASSET_PLANCONTROLRISK_A) + riskAllControls);
            asset.setNumericProperty(PROP_ASSET_WITHOUT_NA_PLANCONTROLRISK_A,
                    asset.getNumericProperty(PROP_ASSET_WITHOUT_NA_PLANCONTROLRISK_A)
                            + riskPlannedControls);
        }
    }

   

    /*
     * (non-Javadoc)
     *
     * @see
     * sernet.verinice.iso27k.service.IRiskAnalysisService#resetRisks(sernet
     * .verinice.model.iso27k.Asset)
     */
    @Override
    public void resetRisks(Asset asset) {
        asset.setNumericProperty(PROP_ASSET_RISK_C, 0);
        asset.setNumericProperty(PROP_ASSET_RISK_I, 0);
        asset.setNumericProperty(PROP_ASSET_RISK_A, 0);
        asset.setNumericProperty(PROP_ASSET_CONTROLRISK_C, 0);
        asset.setNumericProperty(PROP_ASSET_CONTROLRISK_I, 0);
        asset.setNumericProperty(PROP_ASSET_CONTROLRISK_A, 0);
        asset.setNumericProperty(PROP_ASSET_PLANCONTROLRISK_C, 0);
        asset.setNumericProperty(PROP_ASSET_PLANCONTROLRISK_I, 0);
        asset.setNumericProperty(PROP_ASSET_PLANCONTROLRISK_A, 0);
        asset.setNumericProperty(PROP_ASSET_WITHOUT_NA_PLANCONTROLRISK_C, 0);
        asset.setNumericProperty(PROP_ASSET_WITHOUT_NA_PLANCONTROLRISK_I, 0);
        asset.setNumericProperty(PROP_ASSET_WITHOUT_NA_PLANCONTROLRISK_A, 0);
    }

  
    
    /**
     * Reduce impact levels by all controls applied to this asset.
     * 
     * @param asset
     * @param impactC
     * @param impactI
     * @param impactA
     * @throws CommandException 
     */
    @Override
    public Integer[] applyControlsToImpact(int riskType, CnATreeElement rawElement, Integer impactC, Integer impactI, Integer impactA)  {
        if (riskType == RISK_PRE_CONTROLS){
            return null; // do nothing
        }
        Set<CnATreeElement> controlSet = graph.getLinkTargetsByElementType(rawElement, Control.TYPE_ID);       
        
        Integer impactC0 = Integer.valueOf(impactC.intValue());
        Integer impactI0 = Integer.valueOf(impactI.intValue());
        Integer impactA0 = Integer.valueOf(impactA.intValue());
        
        
        switch (riskType) {
        case RISK_WITH_IMPLEMENTED_CONTROLS:
            for (CnATreeElement control : controlSet) {
                control = Retriever.checkRetrieveElement(control);
                if (Control.isImplemented(control.getEntity())) {
                    impactC0 -= control.getNumericProperty(IRiskAnalysisService.PROP_CONTROL_EFFECT_C);
                    impactI0 -= control.getNumericProperty(IRiskAnalysisService.PROP_CONTROL_EFFECT_I);
                    impactA0 -= control.getNumericProperty(IRiskAnalysisService.PROP_CONTROL_EFFECT_A);
                } 
            }
            break;
        case RISK_WITH_ALL_CONTROLS:
            for (CnATreeElement control : controlSet) {
                control = Retriever.checkRetrieveElement(control);
                impactC0 -= control.getNumericProperty(IRiskAnalysisService.PROP_CONTROL_EFFECT_C);
                impactI0 -= control.getNumericProperty(IRiskAnalysisService.PROP_CONTROL_EFFECT_I);
                impactA0 -= control.getNumericProperty(IRiskAnalysisService.PROP_CONTROL_EFFECT_A);
            }
            break;
        case RISK_WITHOUT_NA_CONTROLS:
            for (CnATreeElement control : controlSet) {
                control = Retriever.checkRetrieveElement(control);
                if (Control.isPlanned(control.getEntity())) {
                    impactC0 -= control.getNumericProperty(IRiskAnalysisService.PROP_CONTROL_EFFECT_C);
                    impactI0 -= control.getNumericProperty(IRiskAnalysisService.PROP_CONTROL_EFFECT_I);
                    impactA0 -= control.getNumericProperty(IRiskAnalysisService.PROP_CONTROL_EFFECT_A);
                }
            }
            break;
        default: // do nothing
            break;
        }

        impactC0 = (impactC0.intValue() < 0) ? Integer.valueOf(0) : impactC0;
        impactI0 = (impactI0.intValue() < 0) ? Integer.valueOf(0) : impactI0;
        impactA0 = (impactA0.intValue() < 0) ? Integer.valueOf(0) : impactA0;

        return new Integer[] { impactC0, impactI0, impactA0 };

    }

    /**
     * computes if a given risk (given by asset & scenario) is red, yellow or
     * green
     */
    @Override
    public int getRiskColor(CnATreeElement asset, CnATreeElement scenario, char riskType, int numOfYellowFields, String probType) {
        AssetValueAdapter valueAdapter = new AssetValueAdapter(asset);

        int probability = scenario.getNumericProperty(probType);
        int riskControlState;
        if (probType.equals(IRiskAnalysisService.PROP_SCENARIO_PROBABILITY_WITH_CONTROLS)) {
            riskControlState = IRiskAnalysisService.RISK_WITH_IMPLEMENTED_CONTROLS;
        } else if (probType.equals(IRiskAnalysisService.PROP_SCENARIO_PROBABILITY_WITH_PLANNED_CONTROLS)) {
            riskControlState = IRiskAnalysisService.RISK_WITH_ALL_CONTROLS;
        } else {
            riskControlState = IRiskAnalysisService.RISK_PRE_CONTROLS;
        }

        int impactC = valueAdapter.getVertraulichkeit();
        int impactI = valueAdapter.getIntegritaet();
        int impactA = valueAdapter.getVerfuegbarkeit();
        Integer[] reducedImpact = applyControlsToImpact(riskControlState, asset, impactC, impactI, impactA);
        if (reducedImpact != null) {
            impactC = reducedImpact[0];
            impactI = reducedImpact[1];
            impactA = reducedImpact[2];
        }

        // prob. / impact:
        int riskC = probability + impactC;
        int riskI = probability + impactI;
        int riskA = probability + impactA;

        int riskColour = 0;
        // risk values:
        switch (riskType) {
        case 'c':
            riskColour = getRiskColor(riskC, getTolerableRisks(asset, 'c'), numOfYellowFields);
            break;
        case 'i':
            riskColour = getRiskColor(riskI, getTolerableRisks(asset, 'i'), numOfYellowFields);
            break;
        case 'a':
            riskColour = getRiskColor(riskA, getTolerableRisks(asset, 'a'), numOfYellowFields);
            break;
        default: // do nothing
            break;
        }
        return riskColour;
    }
    
    private void getProbabilityFromThreatAndVulnerability(IncidentScenario scenario) {
        // only calculate if threat AND vulnerability is linked to scenario:     
        Set<CnATreeElement> threatSet = graph.getLinkTargetsByElementType(scenario, Threat.TYPE_ID);
        Set<CnATreeElement> vulnerabilitySet = graph.getLinkTargetsByElementType(scenario, Vulnerability.TYPE_ID);
         
        if (threatSet.size()>0 && vulnerabilitySet.size()>0) {
            int threatImpact = 0;
            for (CnATreeElement threat : threatSet) {
                //use higher value of likelihood or impact:
                int level1 = threat.getNumericProperty(PROP_THREAT_LIKELIHOOD);
                int level2 = threat.getNumericProperty(PROP_THREAT_IMPACT);
                int level = (level1 > level2) ? level1 : level2;
                threatImpact = (level > threatImpact) ? level : threatImpact;
            }
            
            int exploitability = 0;
            for (CnATreeElement vuln : vulnerabilitySet) {
                int level = vuln.getNumericProperty(PROP_VULNERABILITY_EXPLOITABILITY);
                exploitability = (level > exploitability) ? level : exploitability;
            }
            
            // set values to highest found:
            scenario.setNumericProperty(PROP_SCENARIO_THREAT_PROBABILITY, threatImpact);
            scenario.setNumericProperty(PROP_SCENARIO_VULN_PROBABILITY, exploitability);
        }
    }

    private void deductControlFromSzenario(IncidentScenario scenario, CnATreeElement control) {
        int controlEffect = control.getNumericProperty(PROP_CONTROL_EFFECT_P);
        int probAfterControl;
        // risk with planned controls
        probAfterControl = scenario.getNumericProperty(
                PROP_SCENARIO_PROBABILITY_WITH_PLANNED_CONTROLS) - controlEffect;
        scenario.setNumericProperty(PROP_SCENARIO_PROBABILITY_WITH_PLANNED_CONTROLS,
                probAfterControl < 0 ? 0 : probAfterControl);
        if (Control.isImplemented(control.getEntity())) {
            // risk with implemented controls
            probAfterControl = scenario.getNumericProperty(PROP_SCENARIO_PROBABILITY_WITH_CONTROLS)
                    - controlEffect;
            scenario.setNumericProperty(PROP_SCENARIO_PROBABILITY_WITH_CONTROLS,
                    probAfterControl < 0 ? 0 : probAfterControl);
        }

        if (Control.isPlanned(control.getEntity())) {
            probAfterControl = scenario.getNumericProperty(
                    PROP_SCENARIO_PROBABILITY_WITHOUT_NA_CONTROLS) - controlEffect;
            scenario.setNumericProperty(PROP_SCENARIO_PROBABILITY_WITHOUT_NA_CONTROLS,
                    probAfterControl < 0 ? 0 : probAfterControl);
        }
    }
    
    private int getTolerableRisks(CnATreeElement elmt, char riskType) {
        try {
            RetrieveCnATreeElement command = new RetrieveCnATreeElement(Organization.TYPE_ID,
                    elmt.getScopeId(), RetrieveInfo.getPropertyInstance());
            CnATreeElement organization = ServiceFactory.lookupCommandService()
                    .executeCommand(command).getElement();
            if (organization instanceof Organization) {
                switch (riskType) {
                case 'c':
                    return organization.getNumericProperty(Organization.PROP_RISKACCEPT_CONFID);
                case 'i':
                    return organization.getNumericProperty(Organization.PROP_RISKACCEPT_INTEG);
                case 'a':
                    return organization.getNumericProperty(Organization.PROP_RISKACCEPT_AVAIL);
                default:
                    return 0;
                }
            }
        } catch (CommandException e) {
            LOG.error("Error while getting tolerable risk", e);
        }
        return 0;
    }
    
    private int getRiskColor(int risk, int tolerableRisk, int numOfYellowFields){
        if(risk > tolerableRisk){
            return IRiskAnalysisService.RISK_COLOR_RED;
        } else if(risk < tolerableRisk-numOfYellowFields+1){
            return IRiskAnalysisService.RISK_COLOR_GREEN;
        } else {
            return IRiskAnalysisService.RISK_COLOR_YELLOW;
        }
    }
    
    public String getColorString(int colorValue){
        switch(colorValue){
            case IRiskAnalysisService.RISK_COLOR_GREEN:
                return "green";
            case IRiskAnalysisService.RISK_COLOR_YELLOW:
                return "yellow";
            case IRiskAnalysisService.RISK_COLOR_RED:
                return "red";
            default: 
                return "noColourDefined";
        }
    }

}
