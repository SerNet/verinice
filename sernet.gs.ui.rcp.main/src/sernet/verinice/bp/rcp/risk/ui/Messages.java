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

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "sernet.verinice.bp.rcp.risk.ui.messages"; //$NON-NLS-1$

    public static String riskConfigurationUpdateResultDialogCompleteThreatsChanged;
    public static String riskConfigurationUpdateResultDialogComplete;
    public static String riskConfigurationUpdateResultDialogNotAgain;
    public static String riskConfigurationUpdateResultDialogRiskConfiguration;
    public static String riskConfigurationMatrixUsage;
    public static String riskConfigurationMatrixFrequencyAxis;
    public static String riskConfigurationMatrixImpactAxis;

    public static String RiskValuesConfigurator_chooseColor;

    public static String errorUniqueFrequencyLabels;
    public static String errorUniqueImpactLabels;
    public static String errorUniqueRiskCategoryLabels;

    public static String warningRiskPropertyExceedance;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
