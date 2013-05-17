/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak[at]sernet[dot]de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *     You should have received a copy of the GNU Lesser General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Alexander Koderman <ak[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.preferences;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.internal.Workbench;

import sernet.gs.ui.rcp.main.Activator;

/**
 * Preference page to switch between client / server settings.
 * 
 * @author akoderman[at]sernet[dot]de
 * 
 */
public class ClientServerPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	private RadioGroupFieldEditor operationMode;
	private StringFieldEditor serverURI;
	
	private String oldServerMode, newServerMode; 

	public ClientServerPreferencePage() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription(Messages.getString("ClientServerPreferencePage.0")); //$NON-NLS-1$
	}

	/**
	 * Creates the field editors. Field editors are abstractions of the common
	 * GUI blocks needed to manipulate various types of preferences. Each field
	 * editor knows how to save and restore itself.
	 */
	@Override
	public void createFieldEditors() {
		createRadioGroup();

		serverURI = new StringFieldEditor(PreferenceConstants.VNSERVER_URI, Messages.getString("ClientServerPreferencePage.1"), getFieldEditorParent()); //$NON-NLS-1$
		addField(serverURI);

	}

	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible) {
			boolean standalone = getPreferenceStore().getString(PreferenceConstants.OPERATION_MODE).equals(PreferenceConstants.OPERATION_MODE_INTERNAL_SERVER);

			serverURI.setEnabled(!standalone, getFieldEditorParent());
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
	    super.propertyChange(event);
	    if (event.getProperty().equals(FieldEditor.VALUE) && event.getSource() == operationMode ) {
	        newServerMode = (String) event.getNewValue();
	        boolean servermode = newServerMode.equals(PreferenceConstants.OPERATION_MODE_REMOTE_SERVER);
	        serverURI.setEnabled(servermode, getFieldEditorParent());
	    }
	}

	/**
     * @param event
     * @return
     */
    private boolean propertyChanged() {       
        if(newServerMode!=null) {
            return !(newServerMode.equals(oldServerMode));
        }
        return oldServerMode!=null;
    }

    private void createRadioGroup() {
		operationMode = new RadioGroupFieldEditor(PreferenceConstants.OPERATION_MODE, Messages.getString("ClientServerPreferencePage.4"), 1, new String[][] { { Messages.getString("ClientServerPreferencePage.5"), PreferenceConstants.OPERATION_MODE_INTERNAL_SERVER }, { Messages.getString("ClientServerPreferencePage.6"), PreferenceConstants.OPERATION_MODE_REMOTE_SERVER } }, getFieldEditorParent()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		addField(operationMode);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	    oldServerMode = getPreferenceStore().getString(PreferenceConstants.OPERATION_MODE);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#performOk()
	 */
	@Override
	public boolean performOk() {
	    boolean returnValue = super.performOk();
	    if(propertyChanged()) {
	        MessageDialog mDialog = new MessageDialog(
	                Display.getDefault().getActiveShell(), 
	                Messages.getString("ClientServerPreferencePage.2"), //$NON-NLS-1$
	                null,
	                Messages.getString("ClientServerPreferencePage.3"), //$NON-NLS-1$
	                MessageDialog.QUESTION, 
	                new String[] { Messages.getString("ClientServerPreferencePage.7"), Messages.getString("ClientServerPreferencePage.8") }, 1); //$NON-NLS-1$ //$NON-NLS-2$
	        int result = mDialog.open();
    	    if(result==1) {
                Workbench.getInstance().restart();
            }
	    }
	    return returnValue;
	}

}
