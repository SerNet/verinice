/*******************************************************************************
 * Copyright (c) 2019 Daniel Murygin
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
package sernet.verinice.bp.rcp.converter;

import org.eclipse.osgi.util.NLS;

/**
 * 
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "sernet.verinice.bp.rcp.converter.messages"; //$NON-NLS-1$
    public static String ItNetworkConverterAction_ActionTitle;
    public static String ItNetworkConverterAction_ConvertingFinished;
    public static String ItNetworkConverterAction_DialogTitle;
    public static String ItNetworkConverterAction_Error;
    public static String ItNetworkConverterActionDelegate_ConvertingFinished;
    public static String ItNetworkConverterActionDelegate_DialogTitle;
    public static String ItNetworkConverterActionDelegate_Error;
    public static String ItNetworkConverterWizard_ConvertingIsRunning;
    public static String ItNetworkConverterWizard_Error;
    public static String ItNetworkConverterWizard_ErrorInformation;
    public static String ItNetworkConverterWizard_PageTitle;
    public static String ItNetworkConverterWizard_WindowTitle;
    public static String ItNetworkPage_Error;
    public static String ItNetworkPage_Message;
    public static String ItNetworkPage_Title;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
