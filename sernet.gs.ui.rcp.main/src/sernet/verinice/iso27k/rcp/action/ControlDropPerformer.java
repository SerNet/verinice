/*******************************************************************************
 * Copyright (c) 2010 Daniel Murygin <dm@sernet.de>.
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
 * You should have received a copy of the GNU General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Daniel Murygin <dm@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.iso27k.rcp.action;

import java.util.Arrays;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.IProgressService;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;
import sernet.verinice.iso27k.model.Control;
import sernet.verinice.iso27k.model.Group;
import sernet.verinice.iso27k.rcp.ControlTransformOperation;

/**
 * @author Daniel Murygin <dm@sernet.de>
 */
public class ControlDropPerformer implements DropPerformer {

	private boolean isActive;
	
	/**
	 * @param view 
	 * @param viewer
	 */
	public ControlDropPerformer(ViewPart view) {
	}

	private static final Logger LOG = Logger.getLogger(ControlDropPerformer.class);
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ViewerDropAdapter#performDrop(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	public boolean performDrop(Object data, Object target, Viewer viewer) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("performDrop...");
		}
		boolean success = isActive();
		if(isActive()) {
			// because of validateDrop only Groups can be a target
			ControlTransformOperation operation = new ControlTransformOperation((Group) target);
			try {
				IProgressService progressService = PlatformUI.getWorkbench().getProgressService();
				progressService.run(true, true, operation);
				IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
				boolean dontShow = preferenceStore.getBoolean(PreferenceConstants.INFO_CONTROLS_ADDED);
				if(!dontShow) {
					MessageDialogWithToggle dialog = MessageDialogWithToggle.openInformation(PlatformUI.getWorkbench().getDisplay().getActiveShell(), 
							"Status Information", 
							operation.getNumberOfControls() + " controls added to group " + ((Group) target).getTitle(),
							"Don't show this message again (You can change this in the preferences)",
							dontShow,
							preferenceStore,
							PreferenceConstants.INFO_CONTROLS_ADDED);
					preferenceStore.setValue(PreferenceConstants.INFO_CONTROLS_ADDED, dialog.getToggleState());
				}
			} catch (Exception e) {
				LOG.error("Error while transforming items to controls", e);
				ExceptionUtil.log(e, "Error while transforming items to controls");
			}	
		}
		return success;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ViewerDropAdapter#validateDrop(java.lang.Object, int, org.eclipse.swt.dnd.TransferData)
	 */
	@SuppressWarnings("unchecked")
	public boolean validateDrop(Object target, int operation, TransferData transferType) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("validateDrop, target: " + target);
		}
		boolean valid = false;
		if(target instanceof Group) {
			valid = Arrays.asList(((Group)target).getChildTypes()).contains(Control.TYPE_ID);
		}
		return isActive=valid;
	}

	/* (non-Javadoc)
	 * @see sernet.verinice.iso27k.rcp.action.DropPerformer#isActive()
	 */
	public boolean isActive() {
		return isActive;
	}

}
