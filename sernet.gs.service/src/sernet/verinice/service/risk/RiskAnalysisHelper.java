/*******************************************************************************
 * Copyright (c) 2017 Daniel Murygin <dm[at]sernet[dot]de>.
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
 *     Alexander Koderman
 *     Benjamin Weißenfels <bw@sernet.de>
 *     Daniel Murygin <dm[at]sernet[dot]de> 
 ******************************************************************************/
package sernet.verinice.service.risk;

import sernet.verinice.interfaces.CommandException;
import sernet.verinice.model.common.CnATreeElement;

/**
 * Helper interface for executing a risk analysis.
 * 
 * The methods "applyControlsToImpact" and "getRiskColor" were moved from
 * interface RiskAnalysisService to this interface during a refactoring 
 * of the risk analysis service.
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 *
 */
public interface RiskAnalysisHelper {

    String PROP_SCENARIO_METHOD = "incscen_likelihoodmethod";
    String PROP_SCENARIO_THREAT_PROBABILITY = "incscen_threat_likelihood";
    String PROP_SCENARIO_VULN_PROBABILITY = "incscen_vuln_level";

    String PROP_SCENARIO_PROBABILITY = "incscen_likelihood";
    String PROP_SCENARIO_PROBABILITY_WITH_CONTROLS = "incscen_likelihood_wcontrol";
    String PROP_SCENARIO_PROBABILITY_WITH_PLANNED_CONTROLS = "incscen_likelihood_wplancontrol";

    String PROP_SCENARIO_PROBABILITY_WITHOUT_NA_CONTROLS = "incscen_likelihood_without_na_control";

    String PROP_SCENARIO_AFFECTS_C = "scenario_value_method_confidentiality";
    String PROP_SCENARIO_AFFECTS_I = "scenario_value_method_integrity";
    String PROP_SCENARIO_AFFECTS_A = "scenario_value_method_availability";

    String PROP_THREAT_LIKELIHOOD = "threat_likelihood"; //$NON-NLS-1$
    String PROP_THREAT_IMPACT = "threat_impact"; //$NON-NLS-1$

    String PROP_VULNERABILITY_EXPLOITABILITY = "vulnerability_level"; //$NON-NLS-1$

    String PROP_ASSET_RISK_C = "asset_riskvalue_c";
    String PROP_ASSET_RISK_I = "asset_riskvalue_i";
    String PROP_ASSET_RISK_A = "asset_riskvalue_a";
    String PROP_ASSET_CONTROLRISK_C = "asset_riskwcontrolvalue_c";
    String PROP_ASSET_CONTROLRISK_I = "asset_riskwcontrolvalue_i";
    String PROP_ASSET_CONTROLRISK_A = "asset_riskwcontrolvalue_a";
    String PROP_ASSET_PLANCONTROLRISK_C = "asset_riskwplancontrolvalue_c";
    String PROP_ASSET_PLANCONTROLRISK_I = "asset_riskwplancontrolvalue_i";
    String PROP_ASSET_PLANCONTROLRISK_A = "asset_riskwplancontrolvalue_a";

    String PROP_ASSET_WITHOUT_NA_PLANCONTROLRISK_C = "asset_risk_without_na_plancontrolvalue_c";
    String PROP_ASSET_WITHOUT_NA_PLANCONTROLRISK_I = "asset_risk_without_na_plancontrolvalue_i";
    String PROP_ASSET_WITHOUT_NA_PLANCONTROLRISK_A = "asset_risk_without_na_plancontrolvalue_a";

    int RISK_PRE_CONTROLS = 0;
    int RISK_WITH_IMPLEMENTED_CONTROLS = 1;
    int RISK_WITH_ALL_CONTROLS = 2;
    int RISK_WITHOUT_NA_CONTROLS = 3;

    int RISK_COLOR_GREEN = 0;
    int RISK_COLOR_YELLOW = 1;
    int RISK_COLOR_RED = 2;

    /**
     * Apply the controls linked to the given asset to the given business impact
     * values.
     * 
     * @param riskType The type of the risk: RISK_WITH_IMPLEMENTED_CONTROLS, RISK_WITH_ALL_CONTROLS or RISK_WITHOUT_NA_CONTROLS
     * @param asset An asset
     * @param impactC Impact value confidentiality
     * @param impactI Impact value integrity
     * @param impactA Impact value availability
     * @throws CommandException
     */
    Integer[] applyControlsToImpact(int riskType, CnATreeElement asset, Integer impactC, Integer impactI, Integer impactA) ;

    /**
     * Computes if a given risk (given by asset & scenario) is red, yellow or
     * green.
     *
     * @param asset An asset
     * @param scenario A szenario
     * @param riskType 'c', 'i', 'a'
     * @param numOfYellowFields Numer of yellow fields
     * @param probType Probability type: PROP_SCENARIO_PROBABILITY_WITH_CONTROLS or PROP_SCENARIO_PROBABILITY_WITH_PLANNED_CONTROLS
     * @return RISK_COLOR_RED, RISK_COLOR_GREEN or RISK_COLOR_YELLOW
     */
    int getRiskColor(CnATreeElement asset, CnATreeElement scenario, char riskType, int numOfYellowFields, String probType);

}
