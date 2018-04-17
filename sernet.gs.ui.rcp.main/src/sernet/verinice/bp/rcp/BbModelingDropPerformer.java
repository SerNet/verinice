/*******************************************************************************
 * Copyright (c) 2017 Daniel Murygin <dm{a}sernet{dot}de>.
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
 * Daniel Murygin <dm{a}sernet{dot}de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.bp.rcp;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.bsi.dnd.transfer.BaseProtectionModelingTransfer;
import sernet.gs.ui.rcp.main.bsi.dnd.transfer.VeriniceElementTransfer;
import sernet.gs.ui.rcp.main.bsi.editors.BSIElementEditorInput;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;
import sernet.hui.common.VeriniceContext;
import sernet.springclient.RightsServiceClient;
import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.interfaces.RightEnabledUserInteraction;
import sernet.verinice.iso27k.rcp.action.DropPerformer;
import sernet.verinice.iso27k.rcp.action.MetaDropAdapter;
import sernet.verinice.model.bp.elements.Application;
import sernet.verinice.model.bp.elements.BusinessProcess;
import sernet.verinice.model.bp.elements.Device;
import sernet.verinice.model.bp.elements.IcsSystem;
import sernet.verinice.model.bp.elements.ItNetwork;
import sernet.verinice.model.bp.elements.ItSystem;
import sernet.verinice.model.bp.elements.Network;
import sernet.verinice.model.bp.elements.Room;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.rcp.InfoDialogWithShowToggle;
import sernet.verinice.rcp.catalog.CatalogDragListener;
import sernet.verinice.service.bp.exceptions.BpModelingException;
import sernet.verinice.service.bp.exceptions.GroupNotFoundInScopeException;
import sernet.verinice.service.commands.bp.ModelCommand;

/**
 * This drop performer class starts the modeling process
 * of IT base protection after one or more modules
 * are dragged from sernet.verinice.rcp.catalog.CatalogView
 * and dropped on an element in BaseProtectionView.
 *
 * @see CatalogDragListener
 * @see MetaDropAdapter
 * @author Daniel Murygin <dm{a}sernet{dot}de>
 */
@SuppressWarnings("restriction")
public class BbModelingDropPerformer implements DropPerformer, RightEnabledUserInteraction {

    private static final Logger log = Logger.getLogger(BbModelingDropPerformer.class);
    private static final List<String> supportedDropTypeIds;
    static {
        supportedDropTypeIds = new ArrayList<>(9);
        supportedDropTypeIds.add(ItNetwork.TYPE_ID);
        supportedDropTypeIds.add(Application.TYPE_ID);
        supportedDropTypeIds.add(BusinessProcess.TYPE_ID);
        supportedDropTypeIds.add(Device.TYPE_ID);
        supportedDropTypeIds.add(IcsSystem.TYPE_ID);
        supportedDropTypeIds.add(ItSystem.TYPE_ID);
        supportedDropTypeIds.add(Network.TYPE_ID);
        supportedDropTypeIds.add(Room.TYPE_ID);
    }

    private ModelCommand modelCommand;

    private ICommandService commandService;

    private boolean isActive = false;
    private CnATreeElement targetElement = null;

    /*
     * (non-Javadoc)
     *
     * @see
     * sernet.verinice.iso27k.rcp.action.DropPerformer#performDrop(java.lang.
     * Object, java.lang.Object, org.eclipse.jface.viewers.Viewer)
     */
    @Override
    public boolean performDrop(Object data, Object target, Viewer viewer) {
        try {
            final List<CnATreeElement> draggedModules = getDraggedElements(data);
            if (log.isDebugEnabled()) {
                logParameter(draggedModules, targetElement);
            }
            if (draggedModules == null || draggedModules.isEmpty()) {
                log.warn("List of dragged modules is empty. Can not model element."); //$NON-NLS-1$
                return false;
            }
            startModelingByProgressService(draggedModules);
            showConfirmationDialog();
            return true;
        } catch (InvocationTargetException e) {
            log.error(e);
            showError(e, Messages.BbModelingDropPerformer_Error0);
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // set interrupt flag
            log.error("InterruptedException occurred while model module and element", e); //$NON-NLS-1$
            showError(e, Messages.BbModelingDropPerformer_Error1);
            return false;
        }
    }

    private void showConfirmationDialog() {
        InfoDialogWithShowToggle.openInformation(Messages.BbModelingDropPerformerConfirmationTitle,
                getConfirmationDialogMessage(),
                Messages.BbModelingDropPerformerConfirmationToggleMessage,
                PreferenceConstants.INFO_BP_MODELING_CONFIRMATION);
    }

    private String getConfirmationDialogMessage() {
        String message = Messages.BbModelingDropPerformerConfirmationNoProceeding;
        String proceedingLabel = null;
        if (modelCommand != null) {
            proceedingLabel = modelCommand.getProceedingLable();
        }
        if (proceedingLabel != null && !proceedingLabel.isEmpty()) {
            message = NLS.bind(Messages.BbModelingDropPerformerConfirmation, proceedingLabel);
        }
        return message;
    }

    private void startModelingByProgressService(final List<CnATreeElement> draggedModules)
            throws InvocationTargetException, InterruptedException {
        closeEditors();
        PlatformUI.getWorkbench().getProgressService()
                .busyCursorWhile(new IRunnableWithProgress() {
                    @Override
                    public void run(IProgressMonitor monitor)
                            throws InvocationTargetException, InterruptedException {
                        try {
                            monitor.beginTask(getTaskMessage(draggedModules, targetElement),
                                    IProgressMonitor.UNKNOWN);
                            modelModulesAndElement(draggedModules, targetElement);
                            monitor.done();
                        } catch (CommandException e) {
                            showError(e, Messages.BbModelingDropPerformer_Error0);
                        }
                    }
                });
    }

    private void modelModulesAndElement(List<CnATreeElement> draggedModules, CnATreeElement element)
            throws CommandException {
        Set<String> compendiumUuids = new HashSet<>();
        for (CnATreeElement module : draggedModules) {
            compendiumUuids.add(module.getUuid());
        }
        List<String> targetUuids = new LinkedList<>();
        targetUuids.add(element.getUuid());

        modelCommand = new ModelCommand(compendiumUuids, targetUuids);
        modelCommand = getCommandService().executeCommand(modelCommand);
        CnAElementFactory.getInstance().reloadAllModelsFromDatabase();
    }

    private void closeEditors() {
        IEditorReference[] editorReferences = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                .getActivePage().getEditorReferences();
        for (IEditorReference er : editorReferences) {
            try {
                if (er.isPinned() || er.isDirty()) {
                    continue;
                }
                if (er.getEditorInput() instanceof BSIElementEditorInput) {
                    PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().closeEditor(er.getEditor(true), true);
                }
            } catch (PartInitException e) {
                ExceptionUtil.log(e, "Error closing editors");
            }
        }
    }

    private List<CnATreeElement> getDraggedElements(Object data) {
        List<CnATreeElement> elementList = null;
        if (data instanceof Object[]) {
            elementList = new LinkedList<>();
            for (Object o : (Object[]) data) {
                if (o instanceof CnATreeElement) {
                    elementList.add((CnATreeElement) o);
                }
            }
        } else {
            elementList = Collections.emptyList();
        }
        return elementList;
    }

    protected String getTaskMessage(List<CnATreeElement> draggedModules,
            CnATreeElement targetElement) {
        if (draggedModules == null || draggedModules.isEmpty()) {
            return (Messages.BbModelingDropPerformer_NoModules);
        }
        int number = draggedModules.size();
        if (number == 1) {
            return getMessageForOneModule(draggedModules.get(0), targetElement);
        } else {
            return getMessageForMoreThanOneModule(draggedModules, targetElement);
        }
    }

    private String getMessageForMoreThanOneModule(List<CnATreeElement> draggedModules,
            CnATreeElement targetElement) {
        String firstModuleTitle = draggedModules.get(0).getTitle();
        String targetTitle = targetElement.getTitle();
        int numberMinusOne = draggedModules.size() - 1;
        if (numberMinusOne == 1) {
            return NLS.bind(Messages.BbModelingDropPerformer_TwoModules,
                    new Object[] { targetTitle, firstModuleTitle });
        } else {
            return NLS.bind(Messages.BbModelingDropPerformer_MultipleModules,
                    new Object[] { targetTitle, firstModuleTitle, numberMinusOne });
        }
    }

    private String getMessageForOneModule(CnATreeElement module, CnATreeElement target) {
        return NLS.bind(Messages.BbModelingDropPerformer_OneModule, target.getTitle(),
                module.getTitle());
    }

    /*
     * @see
     * sernet.verinice.iso27k.rcp.action.DropPerformer#validateDrop(java.lang.
     * Object, int, org.eclipse.swt.dnd.TransferData)
     */
    @Override
    public boolean validateDrop(Object rawTarget, int operation, TransferData transferData) {
        if (!checkRights()) {
            log.debug("ChechRights() failed, return false"); //$NON-NLS-1$
            isActive = false;
        } else {
            if (!getTransfer().isSupportedType(transferData)) {
                log.debug("Unsupported type of TransferData"); //$NON-NLS-1$
                this.targetElement = null;
            } else {
                this.targetElement = getTargetElement(rawTarget);
            }
            isActive = isTargetElement();
        }
        return isActive;
    }

    /*
     * @see sernet.verinice.iso27k.rcp.action.DropPerformer#isActive()
     */
    @Override
    public boolean isActive() {
        return isActive;
    }

    /*
     * @see sernet.verinice.interfaces.RightEnabledUserInteraction#checkRights()
     */
    @Override
    public boolean checkRights() {
        RightsServiceClient service = (RightsServiceClient) VeriniceContext
                .get(VeriniceContext.RIGHTS_SERVICE);
        return service.isEnabled(getRightID());
    }

    /*
     * @see sernet.verinice.interfaces.RightEnabledUserInteraction#getRightID()
     */
    @Override
    public String getRightID() {
        return ActionRightIDs.BASEPROTECTIONMODELING;
    }

    private CnATreeElement getTargetElement(Object target) {
        if (log.isDebugEnabled()) {
            log.debug("Target: " + target); //$NON-NLS-1$
        }
        CnATreeElement element = null;
        if (target instanceof CnATreeElement) {
            element = (CnATreeElement) target;
            if (!supportedDropTypeIds.contains(element.getTypeId())) {
                if (log.isDebugEnabled()) {
                    log.debug("Unsupported type of target element: " + element.getTypeId()); //$NON-NLS-1$
                }
                element = null;
            }
        }
        return element;
    }

    protected boolean isTargetElement() {
        return this.targetElement != null;
    }

    private void showError(Exception e, String message) {
        final Throwable rootCause = ExceptionUtils.getRootCause(e);
        if (rootCause instanceof GroupNotFoundInScopeException
                || rootCause instanceof BpModelingException) {
            showModelingError(rootCause);
        } else {
            ExceptionUtil.log(e, message);
        }
    }

    private void showModelingError(final Throwable rootCause) {
        final String message = NLS.bind(Messages.BbModelingDropPerformer_ModelingAborted,
                rootCause.getMessage());
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                MessageDialog.openError(Display.getDefault().getActiveShell(),
                        Messages.BbModelingDropPerformerModelingError, message);
            }
        });
    }

    protected VeriniceElementTransfer getTransfer() {
        return BaseProtectionModelingTransfer.getInstance();
    }

    private void logParameter(List<CnATreeElement> draggedElements,
            CnATreeElement targetElementParam) {
        log.debug("Module(s):"); //$NON-NLS-1$
        for (CnATreeElement module : draggedElements) {
            log.debug(module);
        }
        log.debug("is/are modeled with: " + targetElementParam + "..."); //$NON-NLS-1$ //$NON-NLS-2$
    }

    private ICommandService getCommandService() {
        if (commandService == null) {
            commandService = createCommandService();
        }
        return commandService;
    }

    private ICommandService createCommandService() {
        return (ICommandService) VeriniceContext.get(VeriniceContext.COMMAND_SERVICE);
    }

}
