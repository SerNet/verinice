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
package sernet.verinice.rcp.risk;

import org.eclipse.osgi.util.NLS;

/**
 *
 *
 * @author Daniel Murygin <dm{a}sernet{dot}de>
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "sernet.verinice.rcp.risk.messages"; //$NON-NLS-1$
    public static String OrganizationPage_ErrorMessage;
    public static String OrganizationPage_WizardMessage;
    public static String OrganizationPage_WizardTitle;
    public static String RiskAnalysisAction_ErrorDialogMessage; 
    public static String RiskAnalysisAction_ErrorDialogTitle; 
    public static String RiskAnalysisAction_FinishDialogMessage; 
    public static String RiskAnalysisAction_FinishDialogTitle;
    public static String RiskAnalysisAction_Text; 
    public static String RiskAnalysisIsoWizard_ErrorMessage;
    public static String RiskAnalysisIsoWizard_IsRunningMessage;
    public static String RiskAnalysisIsoWizard_IsRunningTaskMessage;
    public static String RiskAnalysisIsoWizard_WindowTitle;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
