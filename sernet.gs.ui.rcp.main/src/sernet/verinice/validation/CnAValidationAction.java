/*******************************************************************************
 * Copyright (c) 2012 Sebastian Hagedorn <sh@sernet.de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details.
 *     You should have received a copy of the GNU General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Sebastian Hagedorn <sh@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.validation;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;

import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.hui.common.VeriniceContext;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.interfaces.RightEnabledUserInteraction;
import sernet.verinice.interfaces.validation.IValidationService;
import sernet.verinice.model.bp.elements.ItNetwork;
import sernet.verinice.model.bsi.ITVerbund;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Organization;
import sernet.verinice.rcp.RightsEnabledActionDelegate;

public class CnAValidationAction extends RightsEnabledActionDelegate implements RightEnabledUserInteraction {

    private static transient Logger LOG = Logger.getLogger(CnAValidationAction.class);
    
    private List<Object> rootObjects;
    
    private IValidationService validationService;

    /* (non-Javadoc)
     * @see sernet.verinice.rcp.RightsEnabledActionDelegate#doRun(org.eclipse.jface.action.IAction)
     */
    @Override
    public void doRun(IAction action) {
        try {
            for (Object rootObject : rootObjects) {
                if (rootObject instanceof Organization || rootObject instanceof ITVerbund || rootObject instanceof ItNetwork) {
                    CnATreeElement cnATreeElement = (CnATreeElement) rootObject;
                    int scopeID = cnATreeElement.getScopeId();
                    final CreateValidationsJob validationJob = new CreateValidationsJob(scopeID);
                    IProgressService progressService = PlatformUI.getWorkbench().getProgressService();
                    progressService.run(true, true, validationJob);
                    CnAElementFactory.getModel(cnATreeElement).validationAdded(scopeID);
                }
            }
        } catch (Exception e) {
            LOG.error("Error while executing validation action", e);
        }
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.RightEnabledUserInteraction#getRightID()
     */
    @Override
    public String getRightID() {
        return ActionRightIDs.CNAVALIDATION;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.actions.ActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
     */
    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        if (isServerRunning()) {
            action.setEnabled(checkRights());
        }
        if(selection instanceof ITreeSelection) {
            ITreeSelection treeSelection = (ITreeSelection) selection;
            rootObjects = treeSelection.toList();
        }
    }
    
    protected HUITypeFactory getTypeFactory() {
        return (HUITypeFactory) VeriniceContext.get(VeriniceContext.HUI_TYPE_FACTORY);
    }

    public IValidationService getValidationService() {
        if(validationService == null){
            validationService = (IValidationService)VeriniceContext.get(VeriniceContext.VALIDATION_SERVICE);
        }
        return validationService;
    }
    
    
    public void setRootObjects(List<Object> rootObjects) {
        this.rootObjects = rootObjects;
    }
}
