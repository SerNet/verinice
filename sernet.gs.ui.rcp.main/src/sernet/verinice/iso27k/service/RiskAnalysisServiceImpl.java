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

import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import sernet.gs.service.RetrieveInfo;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadPolymorphicCnAElementById;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Asset;
import sernet.verinice.model.iso27k.AssetValueAdapter;
import sernet.verinice.model.iso27k.Control;
import sernet.verinice.model.iso27k.IncidentScenario;
import sernet.verinice.model.iso27k.Organization;
import sernet.verinice.model.iso27k.Threat;
import sernet.verinice.model.iso27k.Vulnerability;

/**
 * Perform risk analysis according to ISO 27005.
 * 
 * Dokumentation:
 * http://www.verinice.org/priv/mediawiki-1.6.12/index.php/Benutzerdokumentation#Anhang:_Beschreibung_der_Methode_zum_Risk_Assessment_in_verinice
 * 
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ $LastChangedBy$
 */
public class RiskAnalysisServiceImpl implements IRiskAnalysisService {
    
    private static transient Logger log = Logger.getLogger(RiskAnalysisServiceImpl.class);

    /*
     * (non-Javadoc)
     * 
     * @see
     * sernet.verinice.iso27k.service.IRiskAnalysisService#determineProbability
     * (sernet.verinice.model.iso27k.IncidentScenario)
     */
    @Override
    public void determineProbability(IncidentScenario scenario) {
        // get values from linked threat & vuln, only if automatic mode is activated:
        if (scenario.getNumericProperty(PROP_SCENARIO_METHOD) == 1) {
            // only calculate if threat AND vulnerability is linked to scenario:
            Map<CnATreeElement, CnALink> threats = CnALink.getLinkedElements(scenario, Threat.TYPE_ID);
            Map<CnATreeElement, CnALink> vulns = CnALink.getLinkedElements(scenario, Vulnerability.TYPE_ID);
            
            if (threats.size()>0 && vulns.size()>0) {
                int threatImpact = 0;
                for (CnATreeElement threat : threats.keySet()) {
                    //use higher value of likelihood or impact:
                    int level1 = threat.getNumericProperty(PROP_THREAT_LIKELIHOOD);
                    int level2 = threat.getNumericProperty(PROP_THREAT_IMPACT);
                    int level = (level1 > level2) ? level1 : level2;
                    threatImpact = (level > threatImpact) ? level : threatImpact;
                }
                
                int exploitability = 0;
                for (CnATreeElement vuln : vulns.keySet()) {
                    int level = vuln.getNumericProperty(PROP_VULNERABILITY_EXPLOITABILITY);
                    exploitability = (level > exploitability) ? level : exploitability;
                }
                
                // set values to highest found:
                scenario.setNumericProperty(PROP_SCENARIO_THREAT_PROBABILITY, threatImpact);
                scenario.setNumericProperty(PROP_SCENARIO_VULN_PROBABILITY, exploitability);
            }
        }

        // calculate probability:
        scenario.setNumericProperty(PROP_SCENARIO_PROBABILITY, 0);
        int myThreat = scenario.getNumericProperty(PROP_SCENARIO_THREAT_PROBABILITY);
        int myVuln = scenario.getNumericProperty(PROP_SCENARIO_VULN_PROBABILITY);
        scenario.setNumericProperty(PROP_SCENARIO_PROBABILITY, myThreat + myVuln);
        
        // now determine probability after all applied controls:
        Map<CnATreeElement, CnALink> linkedControlMap = CnALink.getLinkedElements(scenario, Control.TYPE_ID);
        // init probability values to value without controls:
        scenario.setNumericProperty(PROP_SCENARIO_PROBABILITY_WITH_CONTROLS, scenario.getNumericProperty(PROP_SCENARIO_PROBABILITY));
        scenario.setNumericProperty(PROP_SCENARIO_PROBABILITY_WITH_PLANNED_CONTROLS, scenario.getNumericProperty(PROP_SCENARIO_PROBABILITY)); 
        
        // deduct controls from probability:
        for (CnATreeElement control : linkedControlMap.keySet()) {
            int controlEffect = control.getNumericProperty(PROP_CONTROL_EFFECT_P);
            int probAfterControl =0;
            // risk with planned controls
            probAfterControl = scenario.getNumericProperty(PROP_SCENARIO_PROBABILITY_WITH_PLANNED_CONTROLS)-controlEffect;
            scenario.setNumericProperty(PROP_SCENARIO_PROBABILITY_WITH_PLANNED_CONTROLS, 
                    probAfterControl < 0 ? 0 : probAfterControl);
            if (Control.isImplemented(control.getEntity())) {          
                // risk with implemented controls
                probAfterControl = scenario.getNumericProperty(PROP_SCENARIO_PROBABILITY_WITH_CONTROLS)-controlEffect;
                scenario.setNumericProperty(PROP_SCENARIO_PROBABILITY_WITH_CONTROLS, 
                        probAfterControl < 0 ? 0 : probAfterControl);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * sernet.verinice.iso27k.service.IRiskAnalysisService#determineRisks(sernet
     * .verinice.model.iso27k.IncidentScenario)
     */
    @Override
    public void determineRisks(IncidentScenario scenario) {
        Map<CnATreeElement, CnALink> linksForAssets = CnALink.getLinkedElements(scenario, Asset.TYPE_ID);
        for (Entry<CnATreeElement, CnALink> entry : linksForAssets.entrySet()){    
            CnATreeElement asset = entry.getKey();
            AssetValueAdapter valueAdapter = new AssetValueAdapter(asset);

            // reset risk values for link:
            linksForAssets.get(asset).setRiskConfidentiality(0);
            linksForAssets.get(asset).setRiskIntegrity(0);
            linksForAssets.get(asset).setRiskAvailability(0);

            // get reduced impact of asset:
            Integer[] impactWithImplementedControlsCIA = applyControlsToImpact(IRiskAnalysisService.RISK_WITH_IMPLEMENTED_CONTROLS, asset, 
                    valueAdapter.getVertraulichkeit(), valueAdapter.getIntegritaet(), valueAdapter.getVerfuegbarkeit());
            
            Integer[] impactWithAllControlsCIA = applyControlsToImpact(IRiskAnalysisService.RISK_WITH_ALL_CONTROLS, asset, 
                    valueAdapter.getVertraulichkeit(), valueAdapter.getIntegritaet(), valueAdapter.getVerfuegbarkeit());
            
            
            if (scenario.getNumericProperty(PROP_SCENARIO_AFFECTS_C)==1) {
                // increase total asset risk by this combination's risk, saving this individual combination's risk in the link between the two objects:
                // without any controls:
                int risk = valueAdapter.getVertraulichkeit() + scenario.getNumericProperty(PROP_SCENARIO_PROBABILITY);
                int riskImplControls = impactWithImplementedControlsCIA[0] + scenario.getNumericProperty(PROP_SCENARIO_PROBABILITY_WITH_CONTROLS);
                int riskAllControls = impactWithAllControlsCIA[0] + scenario.getNumericProperty(PROP_SCENARIO_PROBABILITY_WITH_PLANNED_CONTROLS);
                
                linksForAssets.get(asset).setRiskConfidentiality(risk);
                asset.setNumericProperty(PROP_ASSET_RISK_C,            asset.getNumericProperty(PROP_ASSET_RISK_C) + risk);
                asset.setNumericProperty(PROP_ASSET_CONTROLRISK_C,     asset.getNumericProperty(PROP_ASSET_CONTROLRISK_C) + riskImplControls);
                asset.setNumericProperty(PROP_ASSET_PLANCONTROLRISK_C, asset.getNumericProperty(PROP_ASSET_PLANCONTROLRISK_C) + riskAllControls);
            }
            
            if (scenario.getNumericProperty(PROP_SCENARIO_AFFECTS_I)==1) {
                // increase total asset risk by this combination's risk
                int risk = valueAdapter.getIntegritaet() + scenario.getNumericProperty(PROP_SCENARIO_PROBABILITY);
                int riskImplControls = impactWithImplementedControlsCIA[1] + scenario.getNumericProperty(PROP_SCENARIO_PROBABILITY_WITH_CONTROLS);
                int riskAllControls = impactWithAllControlsCIA[1] + scenario.getNumericProperty(PROP_SCENARIO_PROBABILITY_WITH_PLANNED_CONTROLS);
              
                linksForAssets.get(asset).setRiskIntegrity(risk);
                asset.setNumericProperty(PROP_ASSET_RISK_I, asset.getNumericProperty(PROP_ASSET_RISK_I) + risk);
                asset.setNumericProperty(PROP_ASSET_CONTROLRISK_I,     asset.getNumericProperty(PROP_ASSET_CONTROLRISK_I) + riskImplControls);
                asset.setNumericProperty(PROP_ASSET_PLANCONTROLRISK_I, asset.getNumericProperty(PROP_ASSET_PLANCONTROLRISK_I) + riskAllControls);
            }    
            
            if (scenario.getNumericProperty(PROP_SCENARIO_AFFECTS_A)==1) {
                // increase total asset risk by this combination's risk
                int risk = valueAdapter.getVerfuegbarkeit() + scenario.getNumericProperty(PROP_SCENARIO_PROBABILITY);
                int riskImplControls = impactWithImplementedControlsCIA[2] + scenario.getNumericProperty(PROP_SCENARIO_PROBABILITY_WITH_CONTROLS);
                int riskAllControls = impactWithAllControlsCIA[2] + scenario.getNumericProperty(PROP_SCENARIO_PROBABILITY_WITH_PLANNED_CONTROLS);
              
                linksForAssets.get(asset).setRiskAvailability(risk);
                asset.setNumericProperty(PROP_ASSET_RISK_A, asset.getNumericProperty(PROP_ASSET_RISK_A) + risk);
                asset.setNumericProperty(PROP_ASSET_CONTROLRISK_A,     asset.getNumericProperty(PROP_ASSET_CONTROLRISK_A) + riskImplControls);
                asset.setNumericProperty(PROP_ASSET_PLANCONTROLRISK_A, asset.getNumericProperty(PROP_ASSET_PLANCONTROLRISK_A) + riskAllControls);
            }                
        }
    }

    /* (non-Javadoc)
     * @see sernet.verinice.iso27k.service.IRiskAnalysisService#resetRisks(sernet.verinice.model.iso27k.Asset)
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
    public Integer[] applyControlsToImpact(int riskType, CnATreeElement asset, Integer impactC, Integer impactI, Integer impactA)  {
        if (riskType == RISK_PRE_CONTROLS){
            return null; // do nothing
        }
        asset = Retriever.checkRetrieveLinks(asset, true);
        
        Map<CnATreeElement, CnALink> linkedElements = CnALink.getLinkedElements(asset, Control.TYPE_ID);
        
        Integer impactC0 = Integer.valueOf(impactC.intValue());
        Integer impactI0 = Integer.valueOf(impactI.intValue());
        Integer impactA0 = Integer.valueOf(impactA.intValue());
        
        
        switch (riskType) {
        case RISK_WITH_IMPLEMENTED_CONTROLS:
            for (CnATreeElement control : linkedElements.keySet()) {
                control = Retriever.checkRetrieveElement(control);
                if (Control.isImplemented(control.getEntity())) {
                    impactC0 -= control.getNumericProperty(IRiskAnalysisService.PROP_CONTROL_EFFECT_C);
                    impactI0 -= control.getNumericProperty(IRiskAnalysisService.PROP_CONTROL_EFFECT_I);
                    impactA0 -= control.getNumericProperty(IRiskAnalysisService.PROP_CONTROL_EFFECT_A);
                } 
            }
            break;
        case RISK_WITH_ALL_CONTROLS:
            for (CnATreeElement control : linkedElements.keySet()) {
                impactC0 -= control.getNumericProperty(IRiskAnalysisService.PROP_CONTROL_EFFECT_C);
                impactI0 -= control.getNumericProperty(IRiskAnalysisService.PROP_CONTROL_EFFECT_I);
                impactA0 -= control.getNumericProperty(IRiskAnalysisService.PROP_CONTROL_EFFECT_A);
            }
            break;
        default: // do nothing
            break;
        }      
        
        impactC0 = (impactC0.intValue() < 0) ? Integer.valueOf(0) : impactC0;
        impactI0 = (impactI0.intValue() < 0) ? Integer.valueOf(0) : impactI0;
        impactA0 = (impactA0.intValue() < 0) ? Integer.valueOf(0) : impactA0;
        
        return new Integer[] {impactC0, impactI0, impactA0};
        
    }

    /**
     * computes if a given risk (given by asset & scenario) is red, yellow or green
     */
    @Override
    public int getRiskColor(CnATreeElement asset, CnATreeElement scenario, char riskType, int numOfYellowFields, String probType){
        AssetValueAdapter valueAdapter = new AssetValueAdapter(asset);
        
        int probability = scenario.getNumericProperty(probType);
        int riskControlState = 0;
        if (probType.equals(IRiskAnalysisService.PROP_SCENARIO_PROBABILITY_WITH_CONTROLS)){
            riskControlState = IRiskAnalysisService.RISK_WITH_IMPLEMENTED_CONTROLS;
        } else if(probType.equals(IRiskAnalysisService.PROP_SCENARIO_PROBABILITY_WITH_PLANNED_CONTROLS)){
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
        switch(riskType) {
        case 'c':
            riskColour = (getRiskColor(riskC, getTolerableRisks(asset, 'c'), numOfYellowFields));
            break;
        case 'i':
            riskColour = (getRiskColor(riskI, getTolerableRisks(asset, 'i'), numOfYellowFields));
            break;
        case 'a':
            riskColour = (getRiskColor(riskA, getTolerableRisks(asset, 'a'), numOfYellowFields));
            break;
        default: // do nothing
            break;
        }
        return riskColour;
    }
    
    private int getTolerableRisks(CnATreeElement elmt, char riskType){
        LoadPolymorphicCnAElementById rootLoader = new LoadPolymorphicCnAElementById(new Integer[]{elmt.getScopeId()});
        try {
            elmt = ServiceFactory.lookupCommandService().executeCommand(rootLoader).getElements().get(0);
            elmt = Retriever.retrieveElement(elmt, new RetrieveInfo().setProperties(true));
        } catch (CommandException e) {
            log.error("Error while executing command");
        }
        if(elmt instanceof Organization){
            switch(riskType){
            case 'c':
                return elmt.getNumericProperty("org_riskaccept_confid");
            case 'i':
                return elmt.getNumericProperty("org_riskaccept_integ");
            case 'a': 
                return elmt.getNumericProperty("org_riskaccept_avail");
            }
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
