/*******************************************************************************
 * Copyright (c) 2016 Ruth Motza.
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
 *     Ruth Motza <rm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.rcp.linktable;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.osgi.util.NLS;

/**
 * @author Ruth Motza <rm[at]sernet[dot]de>
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "sernet.verinice.rcp.linktable.messages"; //$NON-NLS-1$
    public static String ExportLinkTableHandler_1;
    public static String ExportLinkTableHandler_2;
    public static String ExportLinkTableHandler_3;
    public static String ExportLinkTableHandler_4;
    public static String ExportLinkTableHandler_5;
    public static String LinkTableEditor_0;
    public static String LinkTableEditor_1;
    public static String LinkTableEditor_2;
    public static String LinkTableHandler_0;
    public static String LinkTableHandler_1;
    public static String LinkTableHandler_2;
    public static String LinkTableHandler_3;
    public static String LinkTableHandler_4;
    public static String LinkTableHandler_5;
    public static String LinkTableUtil_0;
    public static String LinkTableUtil_1;
    public static String LinkTableUtil_2;
    public static String OpenLinkTableHandler_0;
    public static String VeriniceLinkTableEditor_1;
    public static String VeriniceLinkTableEditor_2;
    public static String VeriniceLinkTableEditor_3;
    public static String VeriniceLinkTableEditor_4;
    public static String VeriniceLinkTableEditor_5;
    public static String VeriniceLinkTableEditor_6;
    public static String VeriniceLinkTableEditor_7;
    public static String VeriniceLinkTableEditor_8;
    public static String VeriniceLinkTableUtil_0;
    public static String VeriniceLinkTableUtil_1;
    public static String LinkTableColumn_CnaLink_Property_Title;
    public static String LinkTableColumn_CnaLink_Property_Description;
    public static String LinkTableColumn_CnaLink_Property_C;
    public static String LinkTableColumn_CnaLink_Property_I;
    public static String LinkTableColumn_CnaLink_Property_A;
    public static String LinkTableColumn_CnaLink_Property_C_With_Controls;
    public static String LinkTableColumn_CnaLink_Property_I_With_Controls;
    public static String LinkTableColumn_CnaLink_Property_A_With_Controls;
    public static String LinkTableColumn_CnaLink_Property_Risk_Treatment;
    public static String LinkTableColumn_CnaLink_Property_Unknown;

    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle
            .getBundle(BUNDLE_NAME);

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }

    public static String getString(String key) {
        try {
            return RESOURCE_BUNDLE.getString(key);
        } catch (MissingResourceException e) {
            return '!' + key + '!';
        }
    }
}
