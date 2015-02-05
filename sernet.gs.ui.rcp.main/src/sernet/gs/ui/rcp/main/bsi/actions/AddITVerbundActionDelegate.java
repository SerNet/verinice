/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak[at]sernet[dot]de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *     You should have received a copy of the GNU Lesser General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Alexander Koderman <ak[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.bsi.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.bsi.editors.EditorFactory;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.interfaces.IInternalServerStartListener;
import sernet.verinice.interfaces.InternalServerEvent;
import sernet.verinice.interfaces.RightEnabledUserInteraction;
import sernet.verinice.model.bsi.ITVerbund;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.rcp.RightsEnabledActionDelegate;

public class AddITVerbundActionDelegate extends RightsEnabledActionDelegate implements IViewActionDelegate, RightEnabledUserInteraction  {
    
    /* (non-Javadoc)
     * @see
     * org.eclipse.ui.IObjectActionDelegate#setActivePart(org.eclipse.jface.
     * action.IAction, org.eclipse.ui.IWorkbenchPart)
     */
    public void setActivePart(IAction action, IWorkbenchPart targetPart) {
    }

    /* (non-Javadoc)
     * @see sernet.verinice.rcp.RightsEnabledActionDelegate#doRun(org.eclipse.jface.action.IAction)
     */
    @Override
    public void doRun(IAction action) {
        try {
            CnATreeElement newElement = null;
            newElement = CnAElementFactory.getInstance().saveNew(CnAElementFactory.getLoadedModel(), ITVerbund.TYPE_ID, null, false);
            if (newElement != null) {
                EditorFactory.getInstance().openEditor(newElement);
            }
        } catch (Exception e) {
            ExceptionUtil.log(e, Messages.AddITVerbundActionDelegate_0);
        }
    }

	@Override
	public void selectionChanged(final IAction action, ISelection arg1) {
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

	@Override
	public void init(IViewPart arg0) {
		
	}

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.RightEnabledUserInteraction#getRightID()
     */
    @Override
    public String getRightID() {
        return ActionRightIDs.ADDITVERBUND;
    }


}
