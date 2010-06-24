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
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.progress.IProgressService;

import sernet.gs.model.Baustein;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.bsi.dnd.CopyBausteine;
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;
import sernet.verinice.iso27k.rcp.CnPItems;
import sernet.verinice.iso27k.rcp.CopyTreeElements;
import sernet.verinice.iso27k.rcp.CutOperation;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Group;
import sernet.verinice.rcp.IProgressRunnable;
import sernet.verinice.rcp.InfoDialogWithShowToggle;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
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
				IStructuredSelection sel = ((IStructuredSelection) selection);		
				if(!CnPItems.getCopyItems().isEmpty()) {
					copy(sel,CnPItems.getCopyItems());
				} else if(!CnPItems.getCutItems().isEmpty()) {
					cut(sel,CnPItems.getCutItems());
				}
			}
		} catch(Exception e) {
			LOG.error("Error while pasting", e); //$NON-NLS-1$
			ExceptionUtil.log(e, Messages.getString("PasteHandler.1")); //$NON-NLS-1$
		}
		return null;
	}
	
	private void copy(IStructuredSelection sel, List copyList) throws InvocationTargetException, InterruptedException {
		if(copyList!=null && !copyList.isEmpty()) {
			IProgressRunnable operation = createOperation(sel, copyList);
			if(operation!=null) {
				IProgressService progressService = PlatformUI.getWorkbench().getProgressService();
				progressService.run(true, true, operation);
				InfoDialogWithShowToggle.openInformation(
						Messages.getString("PasteHandler.2"),  //$NON-NLS-1$
						NLS.bind(Messages.getString("PasteHandler.3"), operation.getNumberOfElements()), //$NON-NLS-1$
                        Messages.getString("PasteHandler.0"), //$NON-NLS-1$
						PreferenceConstants.INFO_ELEMENTS_COPIED);
			}
		}
	}
	
	/**
	 * @param sel
	 * @param copyList
	 * @return
	 */
	private IProgressRunnable createOperation(IStructuredSelection sel, List copyList) {
		IProgressRunnable operation = null;
		if(copyList!=null && !copyList.isEmpty()) {
			if(copyList.get(0) instanceof CnATreeElement) { 
				if( sel.size()==1  && sel.getFirstElement() instanceof CnATreeElement) {
					operation = new CopyTreeElements((CnATreeElement)sel.getFirstElement(),copyList);
				} else if( sel.size()>1 ) {
					MessageDialog.openWarning( 
							PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), 
							Messages.getString("PasteHandler.5"),  //$NON-NLS-1$
							Messages.getString("PasteHandler.6")); //$NON-NLS-1$
				}			
			}
			if(copyList.get(0) instanceof Baustein) {
				operation = new CopyBausteine(sel,copyList);
			}
		}
		return operation;
	}

	private void cut(IStructuredSelection sel, List cutList) throws InvocationTargetException, InterruptedException {
		if(cutList.get(0) instanceof CnATreeElement 
			&& sel.size()==1 
			&& sel.getFirstElement() instanceof CnATreeElement) {
			CnATreeElement target = (CnATreeElement)sel.getFirstElement();
			CutOperation operation = new CutOperation(target, cutList);
			IProgressService progressService = PlatformUI.getWorkbench().getProgressService();
			progressService.run(true, true, operation);
			InfoDialogWithShowToggle.openInformation(
					Messages.getString("PasteHandler.7"),  //$NON-NLS-1$
					NLS.bind(Messages.getString("PasteHandler.8"), operation.getNumberOfElements(), target.getTitle()), //$NON-NLS-1$
					Messages.getString("PasteHandler.9"), //$NON-NLS-1$
					PreferenceConstants.INFO_ELEMENTS_CUT);
		}
		
	}

}
