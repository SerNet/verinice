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
 *     Benjamin Weißenfels <bw@sernet.de> - calculate risk values for planned controls
 ******************************************************************************/
package sernet.verinice.iso27k.service;

import java.util.ResourceBundle.Control;

import sernet.verinice.interfaces.CommandException;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Asset;
import sernet.verinice.model.iso27k.IncidentScenario;

/**
 * 
 * 
 * The risk values Confidentality (C), Integrity (I) and Availability (A) is
 * calculated for each Asset (A). The initial risk values (C,I,A) are inherited
 * by an Process (P) or an Asset or is defined directly in the asset.
 * 
 * Scenarios increase each risk value by adding the probalitiy (Pr). Pr is the
 * sum of the Threat (T) or Vulnerability (V). All controls (C) which are
 * connected with an asset or a scenario decrease the risk values. Controls
 * which are directly linked with an asset decreases only the specific risk
 * values (C, I, A). Controls which are linked with the scenario decrease only
 * the Pr value of the scenario. The resulting Pr value added to each risk value
 * of the linked asset.
 * 
 * <pre>
 *                 P 
 *                 |
 *         -C,I,A  |
 *         *-------A (C,I,A)
 *         |       |
 *         C-------* -- resulting RiskValue for Asset (C,I,A)
 *         |       |
 *         *-------S 
 *         -Pr    / \
 *               T   V
 * 
 * </pre>
 * 
 * The resulting risk values are stored in the asset properties. There are 4
 * kinds of this risk values (C, I, A):
 * 
 * <pre>
 * 1. {@link #RISK_PRE_CONTROLS}: All linked controls are not taken into account.
 * 2. {@link #RISK_WITH_ALL_CONTROLS}: All linked controls are taken into account.
 * 3. {@link #RISK_WITH_IMPLEMENTED_CONTROLS}: Only controls which have status "Implemented"
 * 4. {@link #RISK_WITHOUT_NA_CONTROLS}: This are planned controls, which means all controls which not carry status "N.a" or for which the method
 * {@linkplain sernet.verinice.model.iso27k.Control#isPlanned(sernet.hui.common.connect.Entity)} returns true.
 * 
 * </pre>
 * 
 * Note: Some constants uses planned_control as a sub string in their name.
 * Values which are stored under these constants are actually dedcuted from all
 * control values.
 * 
 * @author koderman@sernet.de
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 *
 */
public interface IRiskAnalysisService {

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

    String PROP_CONTROL_EFFECT_C = "control_effectiveness_confidentiality";
    String PROP_CONTROL_EFFECT_I = "control_effectiveness_integrity";
    String PROP_CONTROL_EFFECT_A = "control_effectiveness_availability";
    String PROP_CONTROL_EFFECT_P = "control_eff_probability";

    int RISK_PRE_CONTROLS = 0;
    int RISK_WITH_IMPLEMENTED_CONTROLS = 1;
    int RISK_WITH_ALL_CONTROLS = 2;
    int RISK_WITHOUT_NA_CONTROLS = 3;

    int RISK_COLOR_GREEN = 0;
    int RISK_COLOR_YELLOW = 1;
    int RISK_COLOR_RED = 2;

    /**
     * Determine probability for this scenario, based on threat and
     * vulnerability.
     * 
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
     * Apply the controls linked to the given asset to the given business impact
     * values.
     * 
     * @param riskType
     * @param asset
     * @param impactC
     * @param impactI
     * @param impactA
     * @throws CommandException
     */
    Integer[] applyControlsToImpact(int riskType, CnATreeElement asset, Integer impactC, Integer impactI, Integer impactA) throws CommandException;

    int getRiskColor(CnATreeElement asset, CnATreeElement scenario, char riskType, int numOfYellowFields, String probType);

}
