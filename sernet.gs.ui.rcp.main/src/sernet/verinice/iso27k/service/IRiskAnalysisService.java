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

import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Asset;
import sernet.verinice.model.iso27k.IncidentScenario;

/**
 * Dokumentation:
 * http://www.verinice.org/priv/mediawiki-1.6.12/index.php/Benutzerdokumentation#Anhang:_Beschreibung_der_Methode_zum_Risk_Assessment_in_verinice
 * 
 * 
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public interface IRiskAnalysisService {
    
    public static final String PROP_SCENARIO_METHOD = "incscen_likelihoodmethod";
    public static final String PROP_SCENARIO_THREAT_PROBABILITY = "incscen_threat_likelihood";
    public static final String PROP_SCENARIO_VULN_PROBABILITY = "incscen_vuln_level";
    
    public static final String PROP_SCENARIO_PROBABILITY = "incscen_likelihood";
    public static final String PROP_SCENARIO_PROBABILITY_WITH_CONTROLS = "incscen_likelihood_wcontrol";
    public static final String PROP_SCENARIO_PROBABILITY_WITH_PLANNED_CONTROLS = "incscen_likelihood_wplancontrol";
    
    public static final String PROP_SCENARIO_AFFECTS_C = "scenario_value_method_confidentiality";
    public static final String PROP_SCENARIO_AFFECTS_I = "scenario_value_method_integrity";
    public static final String PROP_SCENARIO_AFFECTS_A = "scenario_value_method_availability";

    public static final String PROP_THREAT_LIKELIHOOD = "threat_likelihood"; //$NON-NLS-1$
    public static final String PROP_THREAT_IMPACT = "threat_impact"; //$NON-NLS-1$
    
    public static final String PROP_VULNERABILITY_EXPLOITABILITY = "vulnerability_level"; //$NON-NLS-1$

    public static final String PROP_ASSET_RISK_C ="asset_riskvalue_c";
    public static final String PROP_ASSET_RISK_I ="asset_riskvalue_i";
    public static final String PROP_ASSET_RISK_A ="asset_riskvalue_a";
    public static final String PROP_ASSET_CONTROLRISK_C ="asset_riskwcontrolvalue_c";
    public static final String PROP_ASSET_CONTROLRISK_I ="asset_riskwcontrolvalue_i";
    public static final String PROP_ASSET_CONTROLRISK_A ="asset_riskwcontrolvalue_a";
    public static final String PROP_ASSET_PLANCONTROLRISK_C ="asset_riskwplancontrolvalue_c";
    public static final String PROP_ASSET_PLANCONTROLRISK_I ="asset_riskwplancontrolvalue_i";
    public static final String PROP_ASSET_PLANCONTROLRISK_A ="asset_riskwplancontrolvalue_a";
    
    public static final String PROP_CONTROL_EFFECT_C ="control_effectiveness_confidentiality";
    public static final String PROP_CONTROL_EFFECT_I ="control_effectiveness_integrity";
    public static final String PROP_CONTROL_EFFECT_A ="control_effectiveness_availability";
    public static final String PROP_CONTROL_EFFECT_P ="control_eff_probability";
    
    public static final int RISK_PRE_CONTROLS = 0;
    public static final int RISK_WITH_IMPLEMENTED_CONTROLS = 1;
    public static final int RISK_WITH_ALL_CONTROLS = 2;
    

    /**
     * Determine probability for this scenario, based on threat and vulnerability.
     * @param scenario
     */
    void determineProbability(IncidentScenario scenario);

    /**
     * Determine risks for linked assets from this scenario.
     * 
     * @param scenario
     */
    void determineRisks(IncidentScenario scenario);
    
    /**
     * Reset risk calculation, remove all calculated risk values.
     * 
     * @param asset
     */
    void resetRisks(Asset asset);

    /**
     * Apply the controls linked to the given asset to the given business impact values.
     *  
     * @param riskType
     * @param asset
     * @param impactC
     * @param impactI
     * @param impactA
     * @throws CommandException
     */
    public Integer[] applyControlsToImpact(int riskType, CnATreeElement asset, Integer impactC, Integer impactI, Integer impactA) throws CommandException;

}


