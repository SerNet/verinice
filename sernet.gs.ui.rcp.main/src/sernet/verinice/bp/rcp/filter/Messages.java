/*******************************************************************************
 * Copyright (c) 2018 Jochen Kemnade.
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
package sernet.verinice.bp.rcp.filter;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
    private static final String BUNDLE_NAME = "sernet.verinice.bp.rcp.filter.messages"; //$NON-NLS-1$
    public static String BaseProtectionFilterDialog_Title;
    public static String BaseProtectionFilterDialog_ImplementationState;
    public static String BaseProtectionFilterDialog_IntroText;
    public static String BaseProtectionFilterDialog_Objects;
    public static String BaseProtectionFilterDialog_Tags;
    public static String BaseProtectionFilterDialog_Qualifier;
    public static String BaseProtectionFilterDialog_Apply_Tag_Filter_To_IT_Networks;
    public static String BaseProtectionFilterDialog_Hide_Empty_Groups;
    public static String BaseProtectionFilterDialog_Clear;

    public static String ImplementationState_YES;
    public static String ImplementationState_NO;
    public static String ImplementationState_PARTIALLY;
    public static String ImplementationState_NOT_APPLICABLE;

    public static String SecurityLevel_BASIC;
    public static String SecurityLevel_STANDARD;
    public static String SecurityLevel_HIGH;

    public static String BaseProtectionFilterDialog_Property_Value_Null;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
