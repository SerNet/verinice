/*******************************************************************************
 * Copyright (c) 2010 Alexander Koderman.
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
 *     Benjamin Weißenfels <bw@sernet.de> - calculate risk values for planned controls
 *     Daniel Murygin <dm@sernet.de> - Refactoring
 ******************************************************************************/
package sernet.verinice.service.risk;

/**
 * A service to run a ISO/IEC 27005 risk analysis.
 * 
 * The risk values confidentiality (C), integrity (I) and availability (A) is
 * calculated for each Asset (A). The initial risk values (C,I,A) are inherited
 * by an Process (P) or an Asset or is defined directly in the asset.
 * 
 * Scenarios increase each risk value by adding the probability (Pr). Pr is the
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
 * </pre>
 * 
 * Note: Some constants uses planned_control as a sub string in their name.
 * Values which are stored under these constants are actually deducted from all
 * control values.
 * 
 * @author Alexander Koderman
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 * @author Daniel Murygin <dm@sernet.de>
 */
public interface RiskAnalysisService {

    int RISK_WITH_IMPLEMENTED_CONTROLS = 1; 
    int RISK_WITH_ALL_CONTROLS = 2; 
    int RISK_WITHOUT_NA_CONTROLS = 3;
    
    /**
     * Runs a risk on one or more organizations for the given configuration.
     * When null or an empty array of organization ids is passed, 
     * the risk analysis is run on all organizations in the database. 
     * Running a risk analysis on all organizations might be a performance issue.
     * 
     * @param configuration Configuration details for the execution of a risk analysis.
     */
    void runRiskAnalysis(RiskAnalysisConfiguration configuration);

}
