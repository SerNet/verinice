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

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 *
 */
public class PerspectiveSwitcher implements IStartup, IPartListener {

	private static final Logger LOG = Logger.getLogger(PerspectiveSwitcher.class);
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IStartup#earlyStartup()
	 */
	public void earlyStartup() {
		Display.getDefault().asyncExec(new Runnable() {
            public void run() {
            	try {
		            PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().addPartListener(new PerspectiveSwitcher());
		        } catch (Exception e) {
		        	LOG.error(e.getMessage(), e);
		        }
            }
        });
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPartListener#partActivated(org.eclipse.ui.IWorkbenchPart)
	 */
	public void partActivated(final IWorkbenchPart part) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("partActivated: " + part.getTitle()); //$NON-NLS-1$
		}
	}
	

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPartListener#partBroughtToTop(org.eclipse.ui.IWorkbenchPart)
	 */
	public void partBroughtToTop(IWorkbenchPart part) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("partBroughtToTop: " + part.getTitle()); //$NON-NLS-1$
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPartListener#partClosed(org.eclipse.ui.IWorkbenchPart)
	 */
	public void partClosed(IWorkbenchPart part) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("partClosed: " + part.getTitle()); //$NON-NLS-1$
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPartListener#partDeactivated(org.eclipse.ui.IWorkbenchPart)
	 */
	public void partDeactivated(IWorkbenchPart part) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("partDeactivated: " + part.getTitle()); //$NON-NLS-1$
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPartListener#partOpened(org.eclipse.ui.IWorkbenchPart)
	 */
	public void partOpened(final IWorkbenchPart part) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("partOpened: " + part.getTitle()); //$NON-NLS-1$
		}
		if (part instanceof IAttachedToPerspective && ((IAttachedToPerspective) part).getPerspectiveId()!=null) {
			final IWorkbenchWindow workbenchWindow = part.getSite().getPage().getWorkbenchWindow();	
			final String attachedPerspectiveId = ((IAttachedToPerspective) part).getPerspectiveId();
			IPerspectiveDescriptor activePerspective = workbenchWindow.getActivePage().getPerspective();
	        if(activePerspective==null || !activePerspective.getId().equals(attachedPerspectiveId)) {        	
    			Display.getCurrent().asyncExec(new Runnable() {
                    public void run() {
                    	// switch perspective        	
                		if(reallySwitch(part.getClass())) {
                        	try {
                        		workbenchWindow.getWorkbench().showPerspective(attachedPerspectiveId,workbenchWindow);
                        	} catch (WorkbenchException e) {
            					LOG.error(Messages.PerspectiveSwitcher_5 + attachedPerspectiveId, e);
            				}
                		}
                    }
    			});
	        }
		}
	}
	
	private boolean reallySwitch(Class<? extends IWorkbenchPart> clazz) {	
		IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();	
		boolean askNot = preferenceStore.getBoolean(PreferenceConstants.getDontAskBeforeSwitch(clazz));
		boolean doSwitch = MessageDialogWithToggle.ALWAYS.equals(preferenceStore.getString(PreferenceConstants.getSwitch(clazz)));
		if(!askNot) {
			MessageDialogWithToggle dialog = MessageDialogWithToggle.openYesNoCancelQuestion(
					PlatformUI.getWorkbench().getDisplay().getActiveShell(), 
					Messages.PerspectiveSwitcher_6, 
					Messages.PerspectiveSwitcher_7,
					Messages.PerspectiveSwitcher_8,
					false,
					Activator.getDefault().getPreferenceStore(),
					PreferenceConstants.getSwitch(clazz));
			doSwitch = (dialog.getReturnCode()==IDialogConstants.OK_ID || dialog.getReturnCode()==IDialogConstants.YES_ID);
			if(dialog.getToggleState()) {
				preferenceStore.setValue(PreferenceConstants.getDontAskBeforeSwitch(clazz), true);
				// workaround because MessageDialogWithToggle dont't set the value if Button with NO_ID is clicked
				if(dialog.getReturnCode()==IDialogConstants.NO_ID) {
					preferenceStore.setValue(PreferenceConstants.getSwitch(clazz), MessageDialogWithToggle.NEVER);
				}
			}
		}
		return doSwitch;
	}

}
