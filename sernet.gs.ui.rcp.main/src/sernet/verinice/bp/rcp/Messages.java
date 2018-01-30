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
package sernet.verinice.bp.rcp;

import org.eclipse.osgi.util.NLS;

/**
 * @author Daniel Murygin <dm{a}sernet{dot}de>
 */
@SuppressWarnings("restriction")
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "sernet.verinice.bp.rcp.messages"; //$NON-NLS-1$
    public static String GsCatalogModelingDropPerformer_exception_dialog_message;
    public static String GsCatalogModelingDropPerformer_exception_dialog_title;
    public static String GsCatalogModelingDropPerformer_finished_dialog_message;
    public static String GsCatalogModelingDropPerformer_finished_dialog_title;
    public static String GsCatalogModelingDropPerformer_finished_dialog_toggle_message;
    public static String GsCatalogModelingDropPerformer_transform_error_message;
    public static String BaseProtectionView_AccessControl;
    public static String BaseProtectionView_BulkEdit;
    public static String BaseProtectionView_CollapseAll;
    public static String BaseProtectionView_CollapseChildren;
    public static String BaseProtectionView_ErrorCreating;
    public static String BaseProtectionView_ExpandAll;
    public static String BaseProtectionView_ExpandChildren;
    public static String BaseProtectionView_LinkWithEditor;
    public static String BaseProtectionView_Loading_1;
    public static String BaseProtectionView_Loading_2;
    public static String BbModelingDropPerformer_Error0;
    public static String BbModelingDropPerformer_Error1;
    public static String BbModelingDropPerformerModelingError;
    public static String BbModelingDropPerformer_TwoModules;
    public static String BbModelingDropPerformer_ModelingAborted;
    public static String BbModelingDropPerformer_MultipleModules;
    public static String BbModelingDropPerformer_NoModules;
    public static String BbModelingDropPerformer_OneModule;
    public static String BbModelingDropPerformerConfirmationNoProceeding;
    public static String BbModelingDropPerformerConfirmation;
    public static String BbModelingDropPerformerConfirmationTitle;
    public static String BbModelingDropPerformerConfirmationToggleMessage;
    
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
