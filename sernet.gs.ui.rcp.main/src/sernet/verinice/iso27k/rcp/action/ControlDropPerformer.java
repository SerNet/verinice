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

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
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
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;
import sernet.verinice.iso27k.rcp.ControlTransformOperation;
import sernet.verinice.iso27k.service.IItem;
import sernet.verinice.model.iso27k.Control;
import sernet.verinice.model.iso27k.Group;
import sernet.verinice.model.iso27k.Threat;
import sernet.verinice.model.iso27k.Vulnerability;

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
        if (LOG.isDebugEnabled()) {
            LOG.debug("performDrop..."); //$NON-NLS-1$
        }
		
		if (!validateDropObjects(target)) {
		    return false;
		}
		
        boolean success = isActive();
        if (isActive()) {
            // because of validateDrop only Groups can be a target
            ControlTransformOperation operation = new ControlTransformOperation((Group) target);
            try {
                IProgressService progressService = PlatformUI.getWorkbench().getProgressService();
                progressService.run(true, true, operation);
                IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
                boolean dontShow = preferenceStore.getBoolean(PreferenceConstants.INFO_CONTROLS_ADDED);
                if (!dontShow) {
                    MessageDialogWithToggle dialog = MessageDialogWithToggle.openInformation(
                            PlatformUI.getWorkbench().getDisplay().getActiveShell(),
                            Messages.getString("ControlDropPerformer.1"),  //$NON-NLS-1$
                            NLS.bind(Messages.getString("ControlDropPerformer.2"), operation.getNumberOfControls(), ((Group) target).getTitle()), //$NON-NLS-1$
                            Messages.getString("ControlDropPerformer.3"),  //$NON-NLS-1$
                            dontShow, 
                            preferenceStore, 
                            PreferenceConstants.INFO_CONTROLS_ADDED);
                    preferenceStore.setValue(PreferenceConstants.INFO_CONTROLS_ADDED, dialog.getToggleState());
                }
            } catch (Exception e) {
                LOG.error("Error while transforming items to controls", e); //$NON-NLS-1$
                ExceptionUtil.log(e, sernet.verinice.iso27k.rcp.action.Messages.getString("ControlDropPerformer.5")); //$NON-NLS-1$
            }
        }
        return success;
    }

   public boolean validateDrop(Object target, int operation, TransferData transferType) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("validateDrop, target: " + target);
        }
        boolean valid = false;
        if(target instanceof Group) {
            valid = Arrays.asList(((Group)target).getChildTypes()).contains(Control.TYPE_ID)
            || Arrays.asList(((Group)target).getChildTypes()).contains(Threat.TYPE_ID)
            || Arrays.asList(((Group)target).getChildTypes()).contains(Vulnerability.TYPE_ID);
        }
        return isActive=valid;
    }

	public boolean validateDropObjects(Object target) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("validateDrop, target: " + target);
		}
		boolean valid = false;
		
		List items = DNDItems.getItems();

        if(items==null || items.isEmpty()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("No items in drag list");
            }
            return isActive=false;
        }
		
		if(target instanceof Group) {
			if (Arrays.asList(((Group)target).getChildTypes()).contains(Control.TYPE_ID)) {
			    valid = isCorrectItemsForGroup(items, IItem.CONTROL);
			}
			if (Arrays.asList(((Group)target).getChildTypes()).contains(Threat.TYPE_ID)) {
			    valid = isCorrectItemsForGroup(items, IItem.THREAT);
			}
			if (Arrays.asList(((Group)target).getChildTypes()).contains(Vulnerability.TYPE_ID)) {
			    valid = isCorrectItemsForGroup(items, IItem.VULNERABILITY);
			}
		}
		return isActive=valid;
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
            if (item.getItems() != null && item.getItems().size()>0) {
                valid = isCorrectItemsForGroup(item.getItems(), type);
            }
            else if(item.getTypeId() != type) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Item did not pass inspection for drop: " + item);
                }
                return false;
            }
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
