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

import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Asset;
import sernet.verinice.model.iso27k.AssetValueAdapter;
import sernet.verinice.model.iso27k.Control;
import sernet.verinice.model.iso27k.IControl;
import sernet.verinice.model.iso27k.IncidentScenario;
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
                    int level = level1 > level2 ? level1 : level2;
                    if (level > threatImpact)
                        threatImpact = level;
                }
                
                int exploitability = 0;
                for (CnATreeElement vuln : vulns.keySet()) {
                    int level = vuln.getNumericProperty(PROP_VULNERABILITY_EXPLOITABILITY);
                    if (level > exploitability)
                        exploitability = level;
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
            String optionValue = control.getEntity().getOptionValue(IControl.PROP_IMPL);
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
        for (CnATreeElement asset : linksForAssets.keySet()) {
            AssetValueAdapter valueAdapter = new AssetValueAdapter(asset);

            // reset risk values for link:
            linksForAssets.get(asset).setRiskConfidentiality(0);
            linksForAssets.get(asset).setRiskIntegrity(0);
            linksForAssets.get(asset).setRiskAvailability(0);
            
            if (scenario.getNumericProperty(PROP_SCENARIO_AFFECTS_C)==1) {
                // increase total asset risk by this combination's risk, saving this individual combination's risk in the link between the two objects:
                int risk = valueAdapter.getVertraulichkeit() + scenario.getNumericProperty(PROP_SCENARIO_PROBABILITY);
                linksForAssets.get(asset).setRiskConfidentiality(risk);
                asset.setNumericProperty(PROP_ASSET_RISK_C, asset.getNumericProperty(PROP_ASSET_RISK_C) + risk);
                
                // now take planned / implemented controls for the scenario into account:
                int reducedRisk = valueAdapter.getVertraulichkeit() + scenario.getNumericProperty(PROP_SCENARIO_PROBABILITY_WITH_CONTROLS);
                asset.setNumericProperty(PROP_ASSET_CONTROLRISK_C, asset.getNumericProperty(PROP_ASSET_CONTROLRISK_C) + reducedRisk);
                
                int reducedRiskPlanned = valueAdapter.getVertraulichkeit() + scenario.getNumericProperty(PROP_SCENARIO_PROBABILITY_WITH_PLANNED_CONTROLS);
                asset.setNumericProperty(PROP_ASSET_PLANCONTROLRISK_C, asset.getNumericProperty(PROP_ASSET_PLANCONTROLRISK_C) + reducedRiskPlanned);
            }
            
            if (scenario.getNumericProperty(PROP_SCENARIO_AFFECTS_I)==1) {
                // increase total asset risk by this combination's risk
                int risk = valueAdapter.getIntegritaet() + scenario.getNumericProperty(PROP_SCENARIO_PROBABILITY);
                linksForAssets.get(asset).setRiskIntegrity(risk);
                asset.setNumericProperty(PROP_ASSET_RISK_I, asset.getNumericProperty(PROP_ASSET_RISK_I) + risk);
                
                // now take planned / implemented controls for the scenario into account:
                int reducedRisk = valueAdapter.getIntegritaet() + scenario.getNumericProperty(PROP_SCENARIO_PROBABILITY_WITH_CONTROLS);
                asset.setNumericProperty(PROP_ASSET_CONTROLRISK_I, asset.getNumericProperty(PROP_ASSET_CONTROLRISK_I) + reducedRisk);
                
                int reducedRiskPlanned = valueAdapter.getIntegritaet() + scenario.getNumericProperty(PROP_SCENARIO_PROBABILITY_WITH_PLANNED_CONTROLS);
                asset.setNumericProperty(PROP_ASSET_PLANCONTROLRISK_I, asset.getNumericProperty(PROP_ASSET_PLANCONTROLRISK_I) + reducedRiskPlanned);
            }
       
            if (scenario.getNumericProperty(PROP_SCENARIO_AFFECTS_A)==1) {
                // increase total asset risk by this combination's risk
                int risk = valueAdapter.getVerfuegbarkeit() + scenario.getNumericProperty(PROP_SCENARIO_PROBABILITY);
                linksForAssets.get(asset).setRiskAvailability(risk);
                asset.setNumericProperty(PROP_ASSET_RISK_A, asset.getNumericProperty(PROP_ASSET_RISK_A) + risk);
                
                // now take planned / implemented controls for the scenario into account:
                int reducedRisk = valueAdapter.getVerfuegbarkeit() + scenario.getNumericProperty(PROP_SCENARIO_PROBABILITY_WITH_CONTROLS);
                asset.setNumericProperty(PROP_ASSET_CONTROLRISK_A, asset.getNumericProperty(PROP_ASSET_CONTROLRISK_A) + reducedRisk);
                
                int reducedRiskPlanned = valueAdapter.getVerfuegbarkeit() + scenario.getNumericProperty(PROP_SCENARIO_PROBABILITY_WITH_PLANNED_CONTROLS);
                asset.setNumericProperty(PROP_ASSET_PLANCONTROLRISK_A, asset.getNumericProperty(PROP_ASSET_PLANCONTROLRISK_A) + reducedRiskPlanned);
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

    /* (non-Javadoc)
     * @see sernet.verinice.iso27k.service.IRiskAnalysisService#applyControls(sernet.verinice.model.iso27k.Asset)
     */
    @Override
    public void applyControls(Asset asset) {
        Map<CnATreeElement, CnALink> assetControls = CnALink.getLinkedElements(asset, Control.TYPE_ID);
        for (CnATreeElement control : assetControls.keySet()) {
            
            // calculate reduced risk if control *were* implemented:
            int reducedRisk = asset.getNumericProperty(PROP_ASSET_PLANCONTROLRISK_C) - control.getNumericProperty(PROP_CONTROL_EFFECT_C);
            asset.setNumericProperty(PROP_ASSET_PLANCONTROLRISK_C, reducedRisk < 0 ? 0 : reducedRisk);

            reducedRisk = asset.getNumericProperty(PROP_ASSET_PLANCONTROLRISK_I) - control.getNumericProperty(PROP_CONTROL_EFFECT_I);
            asset.setNumericProperty(PROP_ASSET_PLANCONTROLRISK_I, reducedRisk < 0 ? 0 : reducedRisk);
            
            reducedRisk = asset.getNumericProperty(PROP_ASSET_PLANCONTROLRISK_A) - control.getNumericProperty(PROP_CONTROL_EFFECT_A);
            asset.setNumericProperty(PROP_ASSET_PLANCONTROLRISK_A, reducedRisk < 0 ? 0 : reducedRisk);
            
            // calculate reduced risk if control is implemented:
            if (Control.isImplemented(control.getEntity())) {
                reducedRisk = asset.getNumericProperty(PROP_ASSET_CONTROLRISK_C) - control.getNumericProperty(PROP_CONTROL_EFFECT_C);
                asset.setNumericProperty(PROP_ASSET_CONTROLRISK_C, reducedRisk < 0 ? 0 : reducedRisk);
                
                reducedRisk = asset.getNumericProperty(PROP_ASSET_CONTROLRISK_I) - control.getNumericProperty(PROP_CONTROL_EFFECT_I);
                asset.setNumericProperty(PROP_ASSET_CONTROLRISK_I, reducedRisk < 0 ? 0 : reducedRisk);
                
                reducedRisk = asset.getNumericProperty(PROP_ASSET_CONTROLRISK_A) - control.getNumericProperty(PROP_CONTROL_EFFECT_A);
                asset.setNumericProperty(PROP_ASSET_CONTROLRISK_A, reducedRisk < 0 ? 0 : reducedRisk);
            } 
        }
        
    }


}
