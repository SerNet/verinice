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

import java.util.HashSet;
import java.util.Set;

import org.hibernate.dialect.function.CastFunction;

import sernet.hui.common.connect.ITypedElement;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Asset;
import sernet.verinice.model.iso27k.AssetValueAdapter;
import sernet.verinice.model.iso27k.Control;
import sernet.verinice.model.iso27k.IncidentScenario;
import sernet.verinice.model.iso27k.Threat;
import sernet.verinice.model.iso27k.Vulnerability;

/**
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ $LastChangedBy$
 * 
 */
public class RiskAnalysisServiceImpl implements IRiskAnalysisService {

    public static final String PROP_SCENARIO_METHOD = "incscen_likelihoodmethod";
    public static final String PROP_SCENARIO_PROBABILITY = "incscen_likelihood";
    public static final String PROP_SCENARIO_PROBABILITY_WITH_CONTROLS = "incscen_likelihood_wcontrol";
    public static final String PROP_SCENARIO_AFFECTS_C = "scenario_value_method_confidentiality";
    public static final String PROP_SCENARIO_AFFECTS_I = "scenario_value_method_integrity";
    public static final String PROP_SCENARIO_AFFECTS_A = "scenario_value_method_availability";

    public static final String PROP_THREAT_LIKELIHOOD = "threat_likelihood"; //$NON-NLS-1$
    
    public static final String PROP_VULNERABILITY_EXPLOITABILITY = "vulnerability_level"; //$NON-NLS-1$

    public static final String PROP_ASSET_RISK_C ="asset_riskvalue_c";
    public static final String PROP_ASSET_RISK_I ="asset_riskvalue_i";
    public static final String PROP_ASSET_RISK_A ="asset_riskvalue_a";
    public static final String PROP_ASSET_CONTROLRISK_C ="asset_riskwcontrolvalue_c";
    public static final String PROP_ASSET_CONTROLRISK_I ="asset_riskwcontrolvalue_i";
    public static final String PROP_ASSET_CONTROLRISK_A ="asset_riskwcontrolvalue_a";
    
    public static final String PROP_CONTROL_EFFECT_C ="control_effectiveness_confidentiality";
    public static final String PROP_CONTROL_EFFECT_I ="control_effectiveness_integrity";
    public static final String PROP_CONTROL_EFFECT_A ="control_effectiveness_availability";
    public static final String PROP_CONTROL_EFFECT_P ="control_eff_probability";
  
    /*
     * (non-Javadoc)
     * 
     * @see
     * sernet.verinice.iso27k.service.IRiskAnalysisService#determineProbability
     * (sernet.verinice.model.iso27k.IncidentScenario)
     */
    @Override
    public void determineProbability(IncidentScenario scenario) {
        // only if automatic is on:
        if (scenario.getNumericProperty(PROP_SCENARIO_METHOD) == 1) {
            // init value:
            scenario.setNumericProperty(PROP_SCENARIO_PROBABILITY, 0);
            // calculate threat + vulnerability, use max combination if more than
            // one pair:
            Set<CnATreeElement> threats = CnALink.getLinkedElements(scenario, Threat.TYPE_ID);
            Set<CnATreeElement> vulns = CnALink.getLinkedElements(scenario, Vulnerability.TYPE_ID);
            
            int likelihood = 0;
            for (CnATreeElement elmt : threats) {
                Threat threat = (Threat) elmt;
                int level = threat.getNumericProperty(PROP_THREAT_LIKELIHOOD);
                if (level > likelihood)
                    likelihood = level;
            }
            
            int exploitability = 0;
            for (CnATreeElement elmt : vulns) {
                Vulnerability vuln = (Vulnerability) elmt;
                int level = vuln.getNumericProperty(PROP_VULNERABILITY_EXPLOITABILITY);
                if (level > exploitability)
                    exploitability = level;
            }
            scenario.setNumericProperty(PROP_SCENARIO_PROBABILITY, likelihood + exploitability);
        }
        
        // now determine probability after controls:
        Set<CnATreeElement> elmts = CnALink.getLinkedElements(scenario, Control.TYPE_ID);
        for (CnATreeElement elmt : elmts) {
            Control control = (Control) elmt;
            int controlEffect = control.getNumericProperty(PROP_CONTROL_EFFECT_P);
            int probAfterControl = scenario.getNumericProperty(PROP_SCENARIO_PROBABILITY)-controlEffect;
            scenario.setNumericProperty(PROP_SCENARIO_PROBABILITY_WITH_CONTROLS, 
                    probAfterControl < 0 ? 0 : probAfterControl);
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
        Set<CnATreeElement> elements = CnALink.getLinkedElements(scenario, Asset.TYPE_ID);
        for (CnATreeElement elmt : elements) {
            Asset asset = (Asset) elmt;
            AssetValueAdapter valueAdapter = new AssetValueAdapter(asset);
            
            if (scenario.getNumericProperty(PROP_SCENARIO_AFFECTS_C)==1) {
                int risk = valueAdapter.getVertraulichkeit() + scenario.getNumericProperty(PROP_SCENARIO_PROBABILITY);
                asset.setNumericProperty(PROP_ASSET_RISK_C, risk);
                
                // risk with controls:
                int reducedRisk = valueAdapter.getVertraulichkeit() + scenario.getNumericProperty(PROP_SCENARIO_PROBABILITY_WITH_CONTROLS);
                
                Set<CnATreeElement> assetControls = CnALink.getLinkedElements(asset, Control.TYPE_ID);
                for (CnATreeElement controlElmt : assetControls) {
                    Control control = (Control) controlElmt;
                    reducedRisk = reducedRisk - control.getNumericProperty(PROP_CONTROL_EFFECT_C);
                }
                asset.setNumericProperty(PROP_ASSET_CONTROLRISK_C, reducedRisk < 0 ? 0 : reducedRisk);
            }
            
            if (scenario.getNumericProperty(PROP_SCENARIO_AFFECTS_I)==1) {
                int risk = valueAdapter.getIntegritaet() + scenario.getNumericProperty(PROP_SCENARIO_PROBABILITY);
                asset.setNumericProperty(PROP_ASSET_RISK_I, risk);
                
                // risk with controls:
                int reducedRisk = valueAdapter.getIntegritaet() + scenario.getNumericProperty(PROP_SCENARIO_PROBABILITY_WITH_CONTROLS);
                
                Set<CnATreeElement> assetControls = CnALink.getLinkedElements(asset, Control.TYPE_ID);
                for (CnATreeElement controlElmt : assetControls) {
                    Control control = (Control) controlElmt;
                    reducedRisk = reducedRisk - control.getNumericProperty(PROP_CONTROL_EFFECT_I);
                }
                asset.setNumericProperty(PROP_ASSET_CONTROLRISK_I, reducedRisk < 0 ? 0 : reducedRisk);
            
            }
            
            if (scenario.getNumericProperty(PROP_SCENARIO_AFFECTS_A)==1) {
                int risk = valueAdapter.getVerfuegbarkeit() + scenario.getNumericProperty(PROP_SCENARIO_PROBABILITY);
                asset.setNumericProperty(PROP_ASSET_RISK_A, risk);
                
                // risk with controls:
                int reducedRisk = valueAdapter.getVerfuegbarkeit() + scenario.getNumericProperty(PROP_SCENARIO_PROBABILITY_WITH_CONTROLS);
                
                Set<CnATreeElement> assetControls = CnALink.getLinkedElements(asset, Control.TYPE_ID);
                for (CnATreeElement controlElmt : assetControls) {
                    Control control = (Control) controlElmt;
                    reducedRisk = reducedRisk - control.getNumericProperty(PROP_CONTROL_EFFECT_A);
                }
                asset.setNumericProperty(PROP_ASSET_CONTROLRISK_A, reducedRisk < 0 ? 0 : reducedRisk);
            }
        }
    }

}
