/*******************************************************************************
 * Copyright (c) 2018 Daniel Murygin.
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
 ******************************************************************************/
package sernet.verinice.bp.rcp.risk.ui;

import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;
import sernet.verinice.model.bp.risk.configuration.RiskConfigurationUpdateResult;
import sernet.verinice.rcp.InfoDialogWithShowToggle;

/**
 * You can use this class to open dialogs which are displayed after a risk
 * configuration has been updated.
 */
public class RiskConfigurationUpdateResultDialog {

    private RiskConfigurationUpdateResultDialog() {
        super();
    }

    public static void openUpdateResultDialog(RiskConfigurationUpdateResult updateResult) {
        String message = createMessage(updateResult);
        InfoDialogWithShowToggle.openInformation(
                Messages.riskConfigurationUpdateResultDialogRiskConfiguration, message,
                Messages.riskConfigurationUpdateResultDialogNotAgain,
                PreferenceConstants.INFO_BP_RISK_CONFIRMATION);
    }

    private static String createMessage(RiskConfigurationUpdateResult updateResult) {
        int changedThreats = updateResult.getNumberOfChangedThreats();
        String message = Messages.riskConfigurationUpdateResultDialogComplete;
        if (changedThreats > 0) {
            message = Messages.bind(
                    Messages.riskConfigurationUpdateResultDialogCompleteThreatsChanged,
                    changedThreats);
        }
        return message;
    }

}
