/*******************************************************************************
 * Copyright (c) 2013 Daniel Murygin.
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
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.fei.rcp;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;


/**
 * Preference page for file element import.
 * Page is referenced in plugin.xml.
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class FileElementImportPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    public FileElementImportPreferencePage() {
        super(GRID);
        // sernet.gs.ui.rcp.main.Activator is used because of class InfoDialogWithShowToggle
        setPreferenceStore(sernet.gs.ui.rcp.main.Activator.getDefault().getPreferenceStore());
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
     */
    @Override
    protected void createFieldEditors() {
        BooleanFieldEditor editorDnd = new BooleanFieldEditor(PreferenceConstants.FEI_DND_CONFIRM, Messages.FileElementImportPreferencePage_0, getFieldEditorParent()); 
        addField(editorDnd); 
        BooleanFieldEditor editorResult = new BooleanFieldEditor(PreferenceConstants.FEI_SHOW_RESULT, Messages.FileElementImportPreferencePage_1, getFieldEditorParent()); 
        addField(editorResult);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
     */
    @Override
    public void init(IWorkbench arg0) {  
    }

}
