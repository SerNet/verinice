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
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.progress.IProgressService;

import sernet.gs.model.Baustein;
import sernet.gs.service.PermissionException;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.bsi.dnd.CopyBausteine;
import sernet.gs.ui.rcp.main.bsi.views.BsiModelView;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;
import sernet.verinice.iso27k.rcp.CnPItems;
import sernet.verinice.iso27k.rcp.CopyTreeElements;
import sernet.verinice.iso27k.rcp.CutOperation;
import sernet.verinice.iso27k.rcp.ISMView;
import sernet.verinice.model.common.CnATreeElement;
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
			IViewPart part = (IViewPart) HandlerUtil.getActivePart(event);
			if (LOG.isDebugEnabled()) {
                LOG.debug("Avtive part: " + part.getViewSite().getId());
            }
			if(selection instanceof IStructuredSelection) {
				CnATreeElement target = getTarget(part.getViewSite().getId(),(IStructuredSelection) selection);
				if (LOG.isDebugEnabled()) {
                    LOG.debug("Target - type: " + target.getTypeId() + ", title:" + target.getTitle());
                }
				if(CnAElementHome.getInstance().isNewChildAllowed(target)) {
					if(!CnPItems.getCopyItems().isEmpty()) {
						copy(target,CnPItems.getCopyItems());
					} else if(!CnPItems.getCutItems().isEmpty()) {
						cut(target,CnPItems.getCutItems());
					}
				} else if (LOG.isDebugEnabled()) {
					LOG.debug("User is not allowed to add elements to this group"); //$NON-NLS-1$
				}
			}		
		} catch(PermissionException e) {
			if (LOG.isDebugEnabled()) {
				LOG.debug(e);
			}
			handlePermissionException(e);
		} catch(Throwable t) {
			if(t.getCause()!=null && t.getCause() instanceof PermissionException) {
				if (LOG.isDebugEnabled()) {
					LOG.debug(t);
				}
				handlePermissionException((PermissionException) t.getCause());
			} else {
				LOG.error("Error while pasting", t); //$NON-NLS-1$
				ExceptionUtil.log(t, Messages.getString("PasteHandler.1")); //$NON-NLS-1$
			}
		}
		return null;
	}

	private void handlePermissionException(PermissionException e) {
		MessageDialog.openError(PlatformUI.getWorkbench().getDisplay().getActiveShell(),
				Messages.getString("PasteHandler.2"), //$NON-NLS-1$
				e.getMessage());
	}
	
	/**
     * @param id
     * @param sel
     * @return
     */
    private CnATreeElement getTarget(String id, IStructuredSelection sel) {
        CnATreeElement target = null;
        if( sel.size()==1 && sel.getFirstElement() instanceof CnATreeElement) {
            target = (CnATreeElement) sel.getFirstElement();
        } else if(ISMView.ID.equals(id)) {
            target = CnAElementFactory.getInstance().getISO27kModel();
        } else if(BsiModelView.ID.equals(id)) {
            target = CnAElementFactory.getLoadedModel();
        }
        return target;
    }

    private void copy(CnATreeElement target, List copyList) throws InvocationTargetException, InterruptedException {
		if(copyList!=null && !copyList.isEmpty()) {
			IProgressRunnable operation = createOperation(target, copyList);
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
	
	

	@SuppressWarnings("restriction")
	private void cut(CnATreeElement target, List cutList) throws InvocationTargetException, InterruptedException {
		if(cutList.get(0) instanceof CnATreeElement && target!=null) {
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
	
	/**
     * @param target
     * @param copyList
     * @return
     */
    private IProgressRunnable createOperation(CnATreeElement target, List copyList) {
        IProgressRunnable operation = null;
        if(copyList!=null && !copyList.isEmpty()) {
            if(copyList.get(0) instanceof CnATreeElement) { 
                operation = new CopyTreeElements(target,copyList);                   
            }
            if(copyList.get(0) instanceof Baustein) {
                operation = new CopyBausteine(target,copyList);
            }
        }
        return operation;
    }

}
