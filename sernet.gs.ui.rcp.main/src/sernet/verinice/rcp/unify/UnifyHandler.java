/*******************************************************************************
 * Copyright (c) 2012 Daniel Murygin.
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
 * 
 * Contributors:
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.rcp.unify;

import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.handlers.HandlerUtil;

import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.ControlGroup;
import sernet.verinice.rcp.IllegalSelectionException;
import sernet.verinice.rcp.RightsEnabledHandler;

/**
 * 
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class UnifyHandler extends RightsEnabledHandler  {

    private static final Logger LOG = Logger.getLogger(UnifyHandler.class);

    public UnifyHandler() {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.
     * ExecutionEvent)
     */
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Unify called...");
        }
        try {
            openUnifyWizard(event);
        } catch (IllegalSelectionException e) {
            LOG.warn("Wrong selection. Can not unify elements"); //$NON-NLS-1$
            if (LOG.isDebugEnabled()) {
                LOG.debug("stackstrace: ", e);
            }
            MessageDialog.openError(HandlerUtil.getActiveShell(event), "Error", e.getMessage() + " Please select two control groups.");
        } catch (Exception e) {
            LOG.error("Error while unifying elements.", e); //$NON-NLS-1$
            MessageDialog.openError(HandlerUtil.getActiveShell(event), "Error", "Error while unifying elements.");
        }
        return null;
    }

    /**
     * @param event
     */
    private void openUnifyWizard(ExecutionEvent event) {
        List<CnATreeElement> selectedElements = getSelectedElements(event);
        validateElements(selectedElements);
        UnifyWizard wizard = new UnifyWizard(selectedElements);
        WizardDialog dialog = new WizardDialog(HandlerUtil.getActiveShell(event), wizard);
        dialog.create();
        dialog.open();
    }

    /**
     * Checks if the selection is valid for unfiy action. If not a
     * {@link IllegalSelectionException} is thrown.
     * 
     * List passin this method consists of exactly two {@link CnATreeElement}s.
     * 
     * @param selectedElements
     *            A list of selected tree elements
     * @throws IllegalSelectionException
     */
    private void validateElements(List<CnATreeElement> selectedElements) {
        if (selectedElements == null || selectedElements.isEmpty()) {
            throw new IllegalSelectionException("No element selected.");
        }
        if (selectedElements.size() < 2) {
            throw new IllegalSelectionException("Only one element selected.");
        }
        if (selectedElements.size() > 2) {
            throw new IllegalSelectionException("More than two elements selected.");
        }
        for (CnATreeElement element : selectedElements) {
            if (!ControlGroup.TYPE_ID.equals(element.getTypeId())) {
                throw new IllegalSelectionException("Wrong element selected.");
            }
        }
    }

    /**
     * Returns a list with selected tree elements from an event. If there is an
     * element selected which is not a CnATreeElement a
     * {@link IllegalSelectionException} is thrown.
     * 
     * @param event
     *            A ExecutionEvent
     * @return A list with selected {@link CnATreeElement}s
     * @throws IllegalSelectionException
     */
    @SuppressWarnings("unchecked")
    private List<CnATreeElement> getSelectedElements(ExecutionEvent event) {
        List<CnATreeElement> elements = Collections.emptyList();
        ISelection selection = HandlerUtil.getCurrentSelection(event);
        if (selection instanceof StructuredSelection) {
            IStructuredSelection structuredSelection = (IStructuredSelection) selection;
            try {
                elements = structuredSelection.toList();
            } catch (ClassCastException e) {
                LOG.warn("One of the selected element is not a CnATreeElement. Will not return any selected element.");
                if (LOG.isDebugEnabled()) {
                    LOG.debug("stackstrace: ", e);
                }
                throw new IllegalSelectionException("Wrong element selected.");
            }
        }
        return elements;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.RightEnabledUserInteraction#getRightID()
     */
    @Override
    public String getRightID() {
        return ActionRightIDs.UNIFY;
    }

}
