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
package sernet.verinice.iso27k.rcp.action;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.IProgressService;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.bsi.dnd.DNDItems;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;
import sernet.verinice.interfaces.iso27k.IItem;
import sernet.verinice.iso27k.rcp.ControlTransformOperation;
import sernet.verinice.iso27k.service.ItemTransformException;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Control;
import sernet.verinice.model.iso27k.Group;
import sernet.verinice.model.iso27k.Threat;
import sernet.verinice.model.iso27k.Vulnerability;
import sernet.verinice.model.samt.SamtTopic;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.ViewerDropAdapter#performDrop(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	public boolean performDrop(Object data, Object target, Viewer viewer) {
		if (!validateDropObjects(target)) {
			return false;
		}
		boolean success = isActive();
		if (isActive()) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("performDrop..."); //$NON-NLS-1$
			}
			try {
				// because of validateDrop only Groups can be a target
				Group group = (Group) target;
				if(CnAElementHome.getInstance().isNewChildAllowed(group)) {
					ControlTransformOperation operation = new ControlTransformOperation(group);
					IProgressService progressService = PlatformUI.getWorkbench().getProgressService();
					progressService.run(true, true, operation);
					IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
					boolean dontShow = preferenceStore.getBoolean(PreferenceConstants.INFO_CONTROLS_ADDED);
					if (!dontShow) {
						MessageDialogWithToggle dialog = MessageDialogWithToggle.openInformation(PlatformUI.getWorkbench().getDisplay().getActiveShell(), Messages.getString("ControlDropPerformer.1"), //$NON-NLS-1$
								NLS.bind(Messages.getString("ControlDropPerformer.2"), operation.getNumberOfControls(), ((Group) target).getTitle()), //$NON-NLS-1$
								Messages.getString("ControlDropPerformer.3"), //$NON-NLS-1$
								dontShow, preferenceStore, PreferenceConstants.INFO_CONTROLS_ADDED);
						preferenceStore.setValue(PreferenceConstants.INFO_CONTROLS_ADDED, dialog.getToggleState());
					}
				} else if (LOG.isDebugEnabled()) {
					LOG.debug("User is not allowed to add elements to this group"); //$NON-NLS-1$
				}
			 } catch (ItemTransformException e) {
                LOG.error("Error while transforming items to controls", e); //$NON-NLS-1$
                showException(e);
             } catch (InvocationTargetException e) {             
                LOG.error("Error while transforming items to controls", e); //$NON-NLS-1$
                Throwable t = e.getTargetException();
                if(t instanceof ItemTransformException) {
                    showException((ItemTransformException) t);
                } else {
                    ExceptionUtil.log(e, sernet.verinice.iso27k.rcp.action.Messages.getString("ControlDropPerformer.5")); //$NON-NLS-1$
                }
             } catch (Exception e) {             
				LOG.error("Error while transforming items to controls", e); //$NON-NLS-1$
				ExceptionUtil.log(e, sernet.verinice.iso27k.rcp.action.Messages.getString("ControlDropPerformer.5")); //$NON-NLS-1$
			 }
		}
		return success;
	}

    private void showException(ItemTransformException e) {
        final String message = Messages.getString("ControlDropPerformer.0") + e.getMessage(); //$NON-NLS-1$
        MessageDialog.openError(PlatformUI.getWorkbench().getDisplay().getActiveShell(), Messages.getString("ControlDropPerformer.4"), message); //$NON-NLS-1$
    }

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * sernet.verinice.iso27k.rcp.action.DropPerformer#validateDrop(java.lang
	 * .Object, int, org.eclipse.swt.dnd.TransferData)
	 */
	public boolean validateDrop(Object target, int operation, TransferData transferType) {		
		boolean valid = false;
		if (target instanceof Group) {
			List<String> childTypeList = Arrays.asList(((Group) target).getChildTypes());
			valid = childTypeList.contains(Control.TYPE_ID) 
			|| childTypeList.contains(Threat.TYPE_ID) 
			|| childTypeList.contains(Vulnerability.TYPE_ID)
			|| childTypeList.contains(SamtTopic.TYPE_ID);
		}
		if (LOG.isDebugEnabled()) {
			LOG.debug("validateDrop, target: " + target + " result: " + valid); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return isActive = valid;
	}

	/**
	 * @param target
	 * @return
	 */
	public boolean validateDropObjects(Object target) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("validateDrop, target: " + target); //$NON-NLS-1$
		}
		boolean valid = false;

		List items = DNDItems.getItems();

		if (items == null || items.isEmpty()) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("No items in drag list"); //$NON-NLS-1$
			}
			return isActive = false;
		}

		if (target instanceof Group) {
			List<String> childTypeList = Arrays.asList(((Group) target).getChildTypes());
			if(childTypeList.contains(Control.TYPE_ID)) {
				valid = isCorrectItemsForGroup(items, IItem.CONTROL);			
			}
			if(!valid && childTypeList.contains(SamtTopic.TYPE_ID)) {
				valid = isCorrectItemsForGroup(items, IItem.ISA_TOPIC);
			}
			if(!valid && childTypeList.contains(Threat.TYPE_ID)) {
				valid = isCorrectItemsForGroup(items, IItem.THREAT);
			}
			if(!valid && childTypeList.contains(Vulnerability.TYPE_ID)) {
				valid = isCorrectItemsForGroup(items, IItem.VULNERABILITY);
			}
		}
		return isActive = valid;
	}

	/**
	 * @param items
	 * @param control
	 * @return
	 */
	private boolean isCorrectItemsForGroup(Collection<IItem> items, int type) {
		boolean valid = true;
		for (IItem item : items) {
			// only check leaf nodes for type:
			if (item.getItems() != null && item.getItems().size() > 0) {
				valid = isCorrectItemsForGroup(item.getItems(), type);
			} else {
				valid = (item.getTypeId() == type);
			}
		}
		if (LOG.isDebugEnabled()) {
			LOG.debug("isCorrectItemsForGroup result: " + valid); //$NON-NLS-1$
		}
		return valid;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see sernet.verinice.iso27k.rcp.action.DropPerformer#isActive()
	 */
	public boolean isActive() {
		return isActive;
	}

}
