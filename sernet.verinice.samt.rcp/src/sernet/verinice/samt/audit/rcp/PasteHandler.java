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
package sernet.verinice.samt.audit.rcp;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.progress.IProgressService;

import sernet.gs.service.RetrieveInfo;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;
import sernet.verinice.iso27k.rcp.CnPItems;
import sernet.verinice.iso27k.service.PasteService;
import sernet.verinice.iso27k.service.Retriever;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.rcp.IProgressRunnable;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 *
 */
@SuppressWarnings({ "unchecked", "restriction" })
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
			CnATreeElement groupToAdd = null,elementToLink = null;
			GenericElementView elementView = null;
			if(part instanceof GenericElementView) {
			    elementView = (GenericElementView) part;
			    groupToAdd = elementView.getGroupToAdd();
			    elementToLink = elementView.getElementToLink();
			}
			if(selection instanceof IStructuredSelection) {
			    groupToAdd = Retriever.retrieveElement(groupToAdd, RetrieveInfo.getPropertyChildrenInstance().setParent(true));
				IProgressRunnable operation = createOperation(groupToAdd,elementToLink);
	            if(operation!=null) {
	                IProgressService progressService = PlatformUI.getWorkbench().getProgressService();
	                progressService.run(true, true, operation);
	                operation.openInformation(); 
	                if(elementView!=null) {
                        elementView.reload();
                    }
	            }
			}
		} catch(Exception e) {
			LOG.error("Error while pasting", e); //$NON-NLS-1$
			ExceptionUtil.log(e, "Could not paste elements."); 
		}
		return null;
	}
	
	/**
	 * 
	 * 
     * @param element
	 * @param elementToLink 
     * @param copyList
     * @return
     */
    private IProgressRunnable createOperation(CnATreeElement element, CnATreeElement elementToLink) {
        IProgressRunnable operation = null;
        List copyList = CnPItems.getCopyItems();
        List cutList = CnPItems.getCutItems();         
        if(copyList!=null && !copyList.isEmpty() && CnPItems.getCopyItems().get(0) instanceof CnATreeElement) { 
            PasteService task = new AuditCopyService(element, elementToLink, copyList);
            operation = new PasteOperation(task,"{0} elements copied to group {1}",PreferenceConstants.INFO_ELEMENTS_COPIED) ;
        } else if(cutList!=null && cutList.size()>0 && cutList.get(0) instanceof CnATreeElement) { 
            PasteService task = new AuditCutService(element, elementToLink, cutList);
            operation = new PasteOperation(task,"{0} elements moved to group {1}",PreferenceConstants.INFO_ELEMENTS_CUT) ;
        }
        return operation;
    }
    

}
