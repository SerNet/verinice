/*******************************************************************************
 * Copyright (c) 2020 Finn Westendorf
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
 ******************************************************************************/
package sernet.verinice.bp.rcp.consolidator;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
    private static final String BUNDLE_NAME = "sernet.verinice.bp.rcp.consolidator.messages"; //$NON-NLS-1$
    public static String consolidator;
    public static String allScopesTitle;
    public static String allScopes;
    public static String dataSelection;
    public static String dataProtection;
    public static String costs;
    public static String kix;
    public static String audit;
    public static String revision;
    public static String selectTheDataToBeConsolidated;
    public static String threat;
    public static String general;
    public static String riskWithout;
    public static String riskWithoudAdditional;
    public static String riskTreatment;
    public static String module;
    public static String riskWithAdditional;
    public static String safeguard;
    public static String implementation;
    public static String requirement;
    public static String selectAll;
    public static String deselectAll;
    public static String title;
    public static String selectModules;
    public static String selectTheModulesToBeConsolidated;
    public static String noMatchingModules;
    public static String scope;
    public static String parent;
    public static String consolidate;
    public static String consolidatorWarning;
    public static String consolidatorFailed;
    public static String DataSelectionPage_PermissionError_LinkedObjects;
    public static String DataSelectionPage_PermissionError_Modules;
    public static String DataSelectionPage_PermissionError_Requirements;
    public static String nonWritableModulesWarning;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
