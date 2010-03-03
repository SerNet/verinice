/*******************************************************************************
 * Copyright (c) 2010 Daniel Murygin <dm[at]sernet[dot]de>.
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
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.rcp;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import sernet.gs.ui.rcp.main.Activator;

/**
 * Opens an information and a toggle "Don't show this information again".
 * 
 * Toggle state is stored in the preference store if store and preference key is not null.
 * 
 * If toggle state is true dialog is not shown if InfoDialogWithShowToggle.openInformation is called.
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class InfoDialogWithShowToggle extends MessageDialogWithToggle {

	/**
	 * @param parentShell
	 * @param dialogTitle
	 * @param image
	 * @param message
	 * @param dialogImageType
	 * @param dialogButtonLabels
	 * @param defaultIndex
	 * @param toggleMessage
	 * @param toggleState
	 * 
	 * @see MessageDialogWithToggle
	 */
	public InfoDialogWithShowToggle(Shell parentShell, String dialogTitle, Image image, String message, int dialogImageType, String[] dialogButtonLabels, int defaultIndex, String toggleMessage, boolean toggleState) {
		super(parentShell, dialogTitle, image, message, dialogImageType, dialogButtonLabels, defaultIndex, toggleMessage, toggleState);
	}
	
	public static InfoDialogWithShowToggle openInformation(
            String title, 
            String message, 
            String toggleMessage,
            String key) {
		return InfoDialogWithShowToggle.openInformation(
				PlatformUI.getWorkbench().getDisplay().getActiveShell(), 
				title, 
				message, 
				toggleMessage,
				Activator.getDefault().getPreferenceStore(), 
				key);
	}
	
	public static InfoDialogWithShowToggle openInformation(
			Shell parent,
            String title, 
            String message, 
            String toggleMessage, 
            IPreferenceStore store, 
            String key) {
		InfoDialogWithShowToggle dialog = new InfoDialogWithShowToggle(
				parent,
                title, 
                null, // accept the default window icon
                message, 
                INFORMATION,
                new String[] { IDialogConstants.OK_LABEL }, 
                0, // ok is thedefault
                toggleMessage, 
                false);
        dialog.setPrefStore(store);
        dialog.setPrefKey(key);
        boolean open = true;
        if(dialog.getPrefStore() != null 
           && dialog.getPrefKey() != null
           && dialog.getPrefStore().contains(dialog.getPrefKey()) ) {
        	open = !dialog.getPrefStore().getBoolean(dialog.getPrefKey());
        }
        if(open) {
        	dialog.open();
        }
        return dialog;
    }
	
	 /**
     * @see org.eclipse.jface.dialogs.Dialog#buttonPressed(int)
     */
    protected void buttonPressed(int buttonId) {
        super.buttonPressed(buttonId);

        if (getToggleState() && getPrefStore() != null && getPrefKey() != null) {
        	getPrefStore().setValue(getPrefKey(), true);
  
        }
    }

}
