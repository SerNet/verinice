/*******************************************************************************
 * Copyright (c) 2020 Finn Westendorf
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
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package sernet.verinice.bp.rcp.consolidator;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IViewSite;

import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.actions.ViewAndWindowAction;
import sernet.verinice.bp.rcp.Messages;
import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.model.bp.groups.BpRequirementGroup;
import sernet.verinice.rcp.NonModalWizardDialog;

/**
 * ConsolidatorAction starts the {@link ConsolidatorWizard}.
 * <p>
 * This action is only enabled when started from a single module, as that will
 * be used as source for the consolidator.
 */
public class ConsolidatorAction extends ViewAndWindowAction {

    private static final Logger logger = Logger.getLogger(ConsolidatorAction.class);

    BpRequirementGroup selectedModule;

    public ConsolidatorAction(IViewSite viewSite) {
        super(ActionRightIDs.CONSOLIDATOR_MODBP, Messages.BaseProtectionView_Consolidator);
        setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.KONSOLIDATOR));
        setSite(viewSite);
    }

    @Override
    protected void doRun(IStructuredSelection structuredSelection) {
        if (getShell() == null || selectedModule == null) {
            logger.error("The shell or the selected module was null.");
            return;
        }
        new NonModalWizardDialog(getShell(), new ConsolidatorWizard(selectedModule)).open();
    }

    @Override
    protected void selectionChanged(IStructuredSelection selection) {
        setEnabled(selection.size() == 1 && selection.getFirstElement() instanceof BpRequirementGroup);
        if (isEnabled()) {
            selectedModule = (BpRequirementGroup) selection.getFirstElement();
            if (logger.isDebugEnabled()) {
                logger.debug("Selected module: " + selectedModule);
            }
        }
    }
}