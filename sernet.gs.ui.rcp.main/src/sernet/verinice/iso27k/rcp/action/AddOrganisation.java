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

import org.apache.log4j.Logger;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.actions.ActionDelegate;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.bsi.editors.EditorFactory;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.NotSufficientRightsException;
import sernet.hui.common.VeriniceContext;
import sernet.springclient.RightsServiceClient;
import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.interfaces.IInternalServerStartListener;
import sernet.verinice.interfaces.InternalServerEvent;
import sernet.verinice.interfaces.RightEnabledUserInteraction;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Organization;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
// TODO: is there any reason why this shouldnt extend actiondelegate (but exportaction does so) ?
public class AddOrganisation extends ActionDelegate implements IViewActionDelegate, RightEnabledUserInteraction {
	
	private static final Logger LOG = Logger.getLogger(AddOrganisation.class);
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
	 */
	public void init(IViewPart view) {
	}
	
	@Override
	public void init(final IAction action){
	    if(Activator.getDefault().isStandalone()  && !Activator.getDefault().getInternalServer().isRunning()){
	        IInternalServerStartListener listener = new IInternalServerStartListener(){
	            @Override
	            public void statusChanged(InternalServerEvent e) {
	                if(e.isStarted()){
	                    action.setEnabled(checkRights());
	                }
	            }
	        };
	        Activator.getDefault().getInternalServer().addInternalServerStatusListener(listener);
	    } else {
	        action.setEnabled(checkRights());
	    }
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		try {
		    if(checkRights()){
    			CnATreeElement newElement=null;	
    			newElement = CnAElementFactory.getInstance().saveNew(CnAElementFactory.getInstance().getISO27kModel(), Organization.TYPE_ID, null);
    			if (newElement != null) {
    				EditorFactory.getInstance().openEditor(newElement);
    			}
		    } else {
		        throw new NotSufficientRightsException(Messages.getString("Action not allowed for user"));
		    }
		} catch (NotSufficientRightsException e){
            LOG.error("Could not add element", e); //$NON-NLS-1$
            ExceptionUtil.log(e, Messages.getString("AddElement.21")); //$NON-NLS-1$
		} catch (Exception e) {
			LOG.error("Could not add organization", e);
			ExceptionUtil.log(e, "Could not add organization");
		}		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	}

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.RightEnabledUserInteraction#checkRights()
     */
    @Override
    public boolean checkRights() {
            RightsServiceClient service = (RightsServiceClient)VeriniceContext.get(VeriniceContext.RIGHTS_SERVICE);
            return service.isEnabled(getRightID());
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.RightEnabledUserInteraction#getRightID()
     */
    @Override
    public String getRightID() {
       return ActionRightIDs.ADDISMORG;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.RightEnabledUserInteraction#setRightID(java.lang.String)
     */
    @Override
    public void setRightID(String rightID) {
        // DO NOTHING
        
        
    }
	

	

}
