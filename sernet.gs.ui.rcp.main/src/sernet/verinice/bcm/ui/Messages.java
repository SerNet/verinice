/*******************************************************************************
 * Copyright (c) 2023 Urs Zeidler <uz@sernet.de>.
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
 ******************************************************************************/
package sernet.verinice.bcm.ui;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
    private static final String BUNDLE_NAME = Messages.class.getPackageName() + ".messages"; //$NON-NLS-1$
    public static String BiaView_horizontal_layout;
    public static String BiaView_ISM_Mtpd;
    public static String BiaView_linkWithEditorTooltip;
    public static String BiaView_loadJobText;
    public static String BiaView_MinMtpdPrefix;
    public static String BiaView_MOGS_Mtpd;
    public static String BiaView_MtpdPrefix;
    public static String BiaView_noValue;
    public static String BiaView_radial_layout;
    public static String BiaView_RPOPrefix;
    public static String BiaView_RtoPrefix;
    public static String BiaView_saveTooltip;
    public static String BiaView_spring_layout;
    public static String BiaView_tree_Layout;
    public static String BiaView_vertical_layout;
    public static String BiaView_viewModeTooltip;
    public static String BiaViewPreferences_color_parallel;
    public static String BiaViewPreferences_color_pre_after;
    public static String BiaViewPreferences_color_resource;
    public static String BiaViewPreferences_color_selected;
    public static String BiaViewPreferences_title;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
