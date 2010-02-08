/*******************************************************************************
 * Copyright (c) 2010 Daniel Murygin <dm@sernet.de>.
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Daniel Murygin <dm@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.iso27k.rcp.action;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.progress.IProgressService;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;
import sernet.verinice.iso27k.model.Group;
import sernet.verinice.iso27k.rcp.CnPItems;
import sernet.verinice.iso27k.rcp.CopyOperation;
import sernet.verinice.iso27k.rcp.CutOperation;

/**
 * @author Daniel Murygin <dm@sernet.de>
 *
 */
@SuppressWarnings("unchecked")
public class PasteHandler extends AbstractHandler {

	private static final Logger LOG = Logger.getLogger(PasteHandler.class);
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			Object selection = HandlerUtil.getCurrentSelection(event);
			if(selection instanceof IStructuredSelection) {
				Object sel = ((IStructuredSelection) selection).getFirstElement();			
				if (sel instanceof CnATreeElement) {
					CnATreeElement element = (CnATreeElement) sel;
					if(!CnPItems.getCopyItems().isEmpty()) {
						copy(element,CnPItems.getCopyItems());
					} else if(!CnPItems.getCutItems().isEmpty()) {
						cut(element,CnPItems.getCutItems());
					}
				}
			}
		} catch(Exception e) {
			LOG.error("Error while pasting", e);
			ExceptionUtil.log(e, "Could not paste elements.");
		}
		return null;
	}
	
	private void copy(CnATreeElement sel, List copyList) throws InvocationTargetException, InterruptedException {
		CopyOperation operation = new CopyOperation(sel, copyList);
		IProgressService progressService = PlatformUI.getWorkbench().getProgressService();
		progressService.run(true, true, operation);
		IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
		boolean dontShow = preferenceStore.getBoolean(PreferenceConstants.INFO_ELEMENTS_COPIED);
		if(!dontShow) {
			MessageDialogWithToggle dialog = MessageDialogWithToggle.openInformation(PlatformUI.getWorkbench().getDisplay().getActiveShell(), 
					"Status Information", 
					operation.getNumberOfElements() + " elements copied to group " + sel.getTitle(),
					"Don't show this message again (You can change this in the preferences)",
					dontShow,
					preferenceStore,
					PreferenceConstants.INFO_ELEMENTS_COPIED);
			preferenceStore.setValue(PreferenceConstants.INFO_ELEMENTS_COPIED, dialog.getToggleState());
		}
	}
	
	private void cut(CnATreeElement sel, List cutList) throws InvocationTargetException, InterruptedException {
		CutOperation operation = new CutOperation(sel, cutList);
		IProgressService progressService = PlatformUI.getWorkbench().getProgressService();
		progressService.run(true, true, operation);
		IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
		boolean dontShow = preferenceStore.getBoolean(PreferenceConstants.INFO_ELEMENTS_CUT);
		if(!dontShow) {
			MessageDialogWithToggle dialog = MessageDialogWithToggle.openInformation(PlatformUI.getWorkbench().getDisplay().getActiveShell(), 
					"Status Information", 
					operation.getNumberOfElements() + " elements moved to group " + sel.getTitle(),
					"Don't show this message again (You can change this in the preferences)",
					dontShow,
					preferenceStore,
					PreferenceConstants.INFO_ELEMENTS_CUT);
			preferenceStore.setValue(PreferenceConstants.INFO_ELEMENTS_CUT, dialog.getToggleState());
		}
	}

}
