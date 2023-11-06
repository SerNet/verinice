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

import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;

public class BiaViewPreferences extends FieldEditorPreferencePage
implements IWorkbenchPreferencePage {

    /**
     * Create the preference page.
     */
    public BiaViewPreferences() {
        super(GRID);
        setDescription(Messages.BiaViewPreferences_title);
        setPreferenceStore(Activator.getDefault().getPreferenceStore());
    }

     /**
     * Initialize the preference page.
     */
    public void init(IWorkbench workbench) {
        // do nothing
    }
    
    @Override
    protected void createFieldEditors() {
        addField(new ColorFieldEditor(PreferenceConstants.BIA_VIEW_LINK_COLOR_2,
                Messages.BiaViewPreferences_color_pre_after,
                getFieldEditorParent()));
        addField(new ColorFieldEditor(PreferenceConstants.BIA_VIEW_LINK_COLOR_1,
                Messages.BiaViewPreferences_color_parallel,
                getFieldEditorParent()));
        addField(new ColorFieldEditor(PreferenceConstants.BIA_VIEW_LINK_COLOR_3,
                Messages.BiaViewPreferences_color_resource,
                getFieldEditorParent()));
        addField(new ColorFieldEditor(PreferenceConstants.BIA_VIEW_SELECTED_COLOR,
                Messages.BiaViewPreferences_color_selected,
                getFieldEditorParent()));
        
    }

}
