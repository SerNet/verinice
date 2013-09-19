/*******************************************************************************
 * Copyright (c) 2013 Julia Haas.
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
 *     Julia Haas <jh[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.preferences;

import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import sernet.gs.ui.rcp.main.Activator;

/**
 * @author Julia Haas <jh[at]sernet[dot]de>
 *
 */

public class FolderPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
    
    public FolderPreferencePage() {
        super(GRID);
        setPreferenceStore(Activator.getDefault().getPreferenceStore());
    }

    /**
     * Creates the field editors. Field editors are abstractions of the common
     * GUI blocks needed to manipulate various types of preferences. Each field
     * editor knows how to save and restore itself.
     */
    @Override
    public void createFieldEditors() {
        Composite fep = getFieldEditorParent();
        
       
        addField(new DirectoryFieldEditor(PreferenceConstants.DEFAULT_FOLDER_EXPORT, Messages.getString("FolderPreferencePage.0"), fep));//$NON-NLS-1$
        addField(new DirectoryFieldEditor(PreferenceConstants.DEFAULT_FOLDER_IMPORT, Messages.getString("FolderPreferencePage.1"), fep)); //$NON-NLS-1$
        addField(new DirectoryFieldEditor(PreferenceConstants.DEFAULT_FOLDER_REPORT, Messages.getString("FolderPreferencePage.2"), fep)); //$NON-NLS-1$
        
       
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent event) {
        super.propertyChange(event);
        if (event.getProperty().equals(FieldEditor.VALUE)) {
            checkState();
        }
    }

    @Override
    protected void checkState() {
        super.checkState();
        if (!isValid()) {
            return;
        }

    }

   @Override
    public void init(IWorkbench workbench) {
        }
}