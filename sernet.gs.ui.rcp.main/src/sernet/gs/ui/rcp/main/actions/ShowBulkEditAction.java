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
package sernet.gs.ui.rcp.main.actions;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import sernet.gs.service.RetrieveInfo;
import sernet.gs.service.Retriever;
import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.bsi.dialogs.BulkEditDialog;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.EntityType;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.hui.swt.widgets.IHuiControlFactory;
import sernet.verinice.bp.rcp.risk.ui.RiskUiUtils;
import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.model.bp.DeductionImplementationUtil;
import sernet.verinice.model.bp.elements.BpThreat;
import sernet.verinice.model.bp.elements.Safeguard;
import sernet.verinice.model.bpm.TodoViewItem;
import sernet.verinice.model.bsi.DocumentReference;
import sernet.verinice.model.bsi.IBSIModelListener;
import sernet.verinice.model.bsi.MassnahmenUmsetzung;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.IISO27kElement;
import sernet.verinice.service.bp.risk.RiskDeductionUtil;
import sernet.verinice.service.commands.UpdateMultipleElementEntities;

/**
 * Erlaubt das gemeinsame Editieren der Eigenschaften von gleichen,
 * ausgewaehlten Objekten.
 * 
 * @author koderman[at]sernet[dot]de
 * @version $Rev: 39 $ $LastChangedDate: 2007-11-27 12:26:19 +0100 (Di, 27 Nov
 *          2007) $ $LastChangedBy: koderman $
 * 
 */
public class ShowBulkEditAction extends RightsEnabledAction implements ISelectionListener {

    private static final Logger logger = Logger.getLogger(ShowBulkEditAction.class);

    private List<Integer> dbIDs;
    private ArrayList<CnATreeElement> selectedElements;
    private EntityType entType = null;

    public static final String ID = "sernet.gs.ui.rcp.main.actions.showbulkeditaction"; //$NON-NLS-1$
    private final IWorkbenchWindow window;

    public ShowBulkEditAction(IWorkbenchWindow window, String label) {
        super(ActionRightIDs.BULKEDIT, label);
        this.window = window;
        setId(ID);
        setActionDefinitionId(ID);
        setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.CASCADE));
        window.getSelectionService().addSelectionListener(this);
        setToolTipText(Messages.ShowBulkEditAction_1);
    }

    /*
     * (non-Javadoc)
     * 
     * @see sernet.gs.ui.rcp.main.actions.RightsEnabledAction#doRun()
     */
    @Override
    public void doRun() {
        Activator.inheritVeriniceContextState();
        IStructuredSelection selection = (IStructuredSelection) window.getSelectionService()
                .getSelection();
        if (selection == null) {
            return;
        }
        if (!isAllowed(selection)) {
            return;
        }

        dbIDs = new ArrayList<>(selection.size());
        selectedElements = new ArrayList<>();
        entType = null;
        readSelection(selection);
        Map<String, IHuiControlFactory> overrides = RiskUiUtils
                .createHuiControlFactories(selectedElements.get(0));
        BulkEditDialog dialog = new BulkEditDialog(window.getShell(), entType, overrides);

        if (dialog.open() != Window.OK) {
            return;
        }

        final Entity dialogEntity = dialog.getEntity();

        try {
            // close editors first:
            PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
                    .closeAllEditors(true);

            PlatformUI.getWorkbench().getProgressService()
                    .busyCursorWhile(new IRunnableWithProgress() {
                        @Override
                        public void run(IProgressMonitor monitor)
                                throws InvocationTargetException, InterruptedException {
                            doEdit(dialogEntity, monitor);
                        }
                    });
        } catch (InterruptedException e) {
            ExceptionUtil.log(e, Messages.ShowBulkEditAction_5);
        } catch (Exception e) {
            logger.error("Error on bulk edit", e);
            ExceptionUtil.log(e, Messages.ShowBulkEditAction_6);
        }
    }

    private void doEdit(final Entity dialogEntity, IProgressMonitor monitor)
            throws InterruptedException {
        Activator.inheritVeriniceContextState();

        if (!(selectedElements.isEmpty())) {
            // the selected items are of type CnaTreeelement and can be
            // edited right here:
            editElements(selectedElements, dialogEntity, monitor);

        }
        monitor.done();
        refreshListeners();
    }

    private void refreshListeners() {
        boolean isIsoElement = false;
        for (CnATreeElement cnATreeElement : selectedElements) {
            isIsoElement = (cnATreeElement instanceof IISO27kElement);
            if (isIsoElement) {
                break;
            }
        }
        // update once when finished:
        if (CnAElementFactory.getLoadedModel() != null) {
            CnAElementFactory.getLoadedModel()
                    .refreshAllListeners(IBSIModelListener.SOURCE_BULK_EDIT);
        }
        if (isIsoElement) {
            CnAElementFactory.getInstance().getISO27kModel()
                    .refreshAllListeners(IBSIModelListener.SOURCE_BULK_EDIT);
        }
        if (CnAElementFactory.isBpModelLoaded()) {
            CnAElementFactory.getInstance().getBpModel()
                    .refreshAllListeners(IBSIModelListener.SOURCE_BULK_EDIT);
        }
    }

    private void readSelection(IStructuredSelection selection) {
        if (selection.getFirstElement() instanceof TodoViewItem) {
            // prepare list according to selected lightweight todo items:
            for (Iterator<?> iter = selection.iterator(); iter.hasNext();) {
                TodoViewItem item = (TodoViewItem) iter.next();
                dbIDs.add(item.getDbId());
            }
            entType = HUITypeFactory.getInstance().getEntityType(MassnahmenUmsetzung.TYPE_ID);
        } else {
            // prepare list according to selected tree items:
            for (Iterator<?> iter = selection.iterator(); iter.hasNext();) {
                Object o = iter.next();
                CnATreeElement elmt = null;
                if (o instanceof CnATreeElement) {
                    elmt = (CnATreeElement) o;
                } else if (o instanceof DocumentReference) {
                    DocumentReference ref = (DocumentReference) o;
                    elmt = ref.getCnaTreeElement();
                }
                if (elmt == null) {
                    continue;
                }

                entType = HUITypeFactory.getInstance()
                        .getEntityType(elmt.getEntity().getEntityType());
                selectedElements.add(elmt);
                logger.debug("Adding to bulk edit: " + elmt.getTitle()); //$NON-NLS-1$
            }
        }
    }

    private boolean isAllowed(IStructuredSelection selection) {
        // Realizes that the action to delete an element is greyed out,
        // when there is no right to do so.
        Iterator<?> iterator = (selection).iterator();
        while (iterator.hasNext()) {
            Object next = iterator.next();
            if (next instanceof CnATreeElement) {
                boolean writeallowed = CnAElementHome.getInstance()
                        .isWriteAllowed((CnATreeElement) next);
                if (!writeallowed) {
                    MessageDialog.openWarning(window.getShell(), Messages.ShowBulkEditAction_2,
                            NLS.bind(Messages.ShowBulkEditAction_3,
                                    ((CnATreeElement) next).getTitle()));
                    setEnabled(false);
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Action is enabled when only items of the same type are selected.
     */
    @Override
    public void selectionChanged(IWorkbenchPart part, ISelection input) {
        if (input instanceof IStructuredSelection) {
            IStructuredSelection selection = (IStructuredSelection) input;
            boolean selectionEmpty = selection.isEmpty();

            // check for listitems:
            if (!selectionEmpty && selection.getFirstElement() instanceof TodoViewItem) {
                for (Iterator<?> iter = selection.iterator(); iter.hasNext();) {
                    if (!(iter.next() instanceof TodoViewItem)) {
                        setEnabled(false);
                        return;
                    }
                }
                if (checkRights()) {
                    setEnabled(true);
                }
                return;
            }

            // check for document references:
            CnATreeElement elmt = null;
            if (!selectionEmpty && selection.getFirstElement() instanceof DocumentReference) {
                elmt = ((DocumentReference) selection.getFirstElement()).getCnaTreeElement();
            }

            // check for other objects:
            else if (!selectionEmpty && selection.getFirstElement() instanceof CnATreeElement
                    && ((CnATreeElement) selection.getFirstElement()).getEntity() != null) {
                elmt = (CnATreeElement) selection.getFirstElement();
            }

            if (elmt != null) {
                String type = elmt.getEntity().getEntityType();

                for (Iterator<?> iter = selection.iterator(); iter.hasNext();) {
                    Object o = iter.next();
                    if (o instanceof CnATreeElement) {
                        elmt = (CnATreeElement) o;

                    } else if (o instanceof DocumentReference) {
                        DocumentReference ref = (DocumentReference) o;
                        elmt = ref.getCnaTreeElement();
                    }
                }

                if (elmt == null || elmt.getEntity() == null
                        || !elmt.getEntity().getEntityType().equals(type)) {
                    setEnabled(false);
                    return;
                }

                if (checkRights()) {
                    setEnabled(true);
                }
                return;
            }
        }
        setEnabled(false);
    }

    private void editElements(List<CnATreeElement> selectedElements, Entity dialogEntity,
            IProgressMonitor monitor) {
        monitor.setTaskName(Messages.ShowBulkEditAction_9);
        monitor.beginTask(Messages.ShowBulkEditAction_10, selectedElements.size() + 1);
        List<CnATreeElement> elementsToSave = new ArrayList<>(selectedElements.size());
        // for every target:
        for (CnATreeElement elmt : selectedElements) {
            boolean elementIsThreat = elmt instanceof BpThreat;
            if (elementIsThreat) {
                // load linked elements required for risk deduction
                elmt = Retriever.retrieveElement(elmt,
                        RetrieveInfo.getPropertyInstance().setLinksUp(true));
            }
            // set values:
            Entity editEntity = elmt.getEntity();
            editEntity.copyEntity(dialogEntity);
            if (elementIsThreat) {
                RiskDeductionUtil.deduceRisk((BpThreat) elmt);
            }
            elementsToSave.add(elmt);
            elmt.getEntity().trackChange(ServiceFactory.lookupAuthService().getUsername());
            monitor.worked(1);
        }
        try {
            monitor.setTaskName(Messages.ShowBulkEditAction_11);
            monitor.beginTask(Messages.ShowBulkEditAction_12, IProgressMonitor.UNKNOWN);
            UpdateMultipleElementEntities command = new UpdateMultipleElementEntities(
                    elementsToSave);
            command = ServiceFactory.lookupCommandService().executeCommand(command);
            List<CnATreeElement> changedElements = command.getChangedElements();
            for (CnATreeElement cnATreeElement : changedElements) {
                updateRelatedProperties(cnATreeElement);
            }
        } catch (Exception e) {
            logger.error("Error while bulk update", e);
            ExceptionUtil.log(e, Messages.ShowBulkEditAction_13);
        }

    }

    private void updateRelatedProperties(CnATreeElement cnATreeElement) {
        if (cnATreeElement instanceof Safeguard) {
            updateSafeguardRelatedProperties((Safeguard) cnATreeElement);
        }
    }

    private void updateSafeguardRelatedProperties(Safeguard safeguard) {
        CnATreeElement fetchedSafeguard = Retriever.retrieveElement(safeguard,
                new RetrieveInfo().setProperties(true).setLinksUp(true).setLinksUpProperties(true));
        Set<CnALink> linksUp = fetchedSafeguard.getLinksUp();
        for (CnALink cnALink : linksUp) {
            if (DeductionImplementationUtil
                    .isRelevantLinkForImplementationStateDeduction(cnALink)) {
                CnATreeElement requirement = cnALink.getDependant();
                if (DeductionImplementationUtil.isDeductiveImplementationEnabled(requirement)) {
                    // the requirements' implementation status could
                    // have been updated if state deduction is
                    // enabled, so better refresh them (VN-2067)
                    CnAElementFactory.getModel(requirement).childChanged(requirement);
                }
            }
        }
    }
}
