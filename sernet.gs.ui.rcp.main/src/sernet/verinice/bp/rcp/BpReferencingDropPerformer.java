/*******************************************************************************
 * Copyright (c) 2022 Jochen Kemnade <jk{a}sernet{dot}de>.
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
package sernet.verinice.bp.rcp;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.bsi.dnd.transfer.BaseProtectionElementTransfer;
import sernet.gs.ui.rcp.main.bsi.dnd.transfer.BaseProtectionModelingTransfer;
import sernet.gs.ui.rcp.main.bsi.dnd.transfer.VeriniceElementTransfer;
import sernet.gs.ui.rcp.main.bsi.editors.BSIElementEditorInput;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;
import sernet.hui.common.VeriniceContext;
import sernet.hui.common.connect.ITargetObject;
import sernet.springclient.RightsServiceClient;
import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.interfaces.RightEnabledUserInteraction;
import sernet.verinice.iso27k.rcp.action.DropPerformer;
import sernet.verinice.iso27k.rcp.action.MetaDropAdapter;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.rcp.InfoDialogWithShowToggle;
import sernet.verinice.service.bp.exceptions.BpReferencingException;
import sernet.verinice.service.commands.bp.CheckReferencingCommand;
import sernet.verinice.service.commands.bp.ReferencingCommand;

/**
 * This drop performer class starts the referencing process of IT base
 * protection after one or more modules are dragged from one target element to
 * another.
 *
 * @see MetaDropAdapter
 */
public class BpReferencingDropPerformer implements DropPerformer, RightEnabledUserInteraction {

    private static final Logger log = Logger.getLogger(BpReferencingDropPerformer.class);

    private boolean isActive = false;

    /*
     * @see
     * sernet.verinice.iso27k.rcp.action.DropPerformer#performDrop(java.lang.
     * Object, java.lang.Object, org.eclipse.jface.viewers.Viewer)
     */
    @Override
    public boolean performDrop(Object data, Object target, Viewer viewer) {
        try {
            final List<CnATreeElement> draggedModules = getDraggedElements(data);
            if (isValid(draggedModules)) {
                CnATreeElement targetElement = (CnATreeElement) target;
                Set<String> existingModules = getExistingModules(draggedModules, targetElement);
                boolean execute = true;
                if (!existingModules.isEmpty()) {
                    execute = confirmReferencing(existingModules, targetElement);
                }
                if (execute) {
                    startReferencingByProgressService(draggedModules, targetElement);
                    showConfirmationDialog();
                }
            }
            return true;
        } catch (InvocationTargetException | CommandException e) {
            log.error(e);
            showError(e);
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // set interrupt flag
            log.error("InterruptedException occurred while model module and element", e); //$NON-NLS-1$
            showError(e);
            return false;
        }
    }

    private void startReferencingByProgressService(final List<CnATreeElement> draggedModules,
            CnATreeElement target) throws InvocationTargetException, InterruptedException {
        closeEditors();
        PlatformUI.getWorkbench().getProgressService().busyCursorWhile(new IRunnableWithProgress() {
            @Override
            public void run(IProgressMonitor monitor)
                    throws InvocationTargetException, InterruptedException {
                try {
                    monitor.beginTask(getTaskMessage(draggedModules, target),
                            IProgressMonitor.UNKNOWN);
                    addModuleReferences(draggedModules, target);
                    monitor.done();
                } catch (CommandException e) {
                    showError(e);
                }
            }
        });
    }

    private boolean isValid(List<CnATreeElement> draggedModules) {
        if (draggedModules == null || draggedModules.isEmpty()) {
            log.warn("List of dragged modules is empty. Cannot create references."); //$NON-NLS-1$
            return false;
        }
        return true;
    }

    private void showConfirmationDialog() {
        InfoDialogWithShowToggle.openInformation(
                Messages.BpReferencingDropPerformerConfirmationTitle,
                Messages.BpReferencingDropPerformerConfirmation,
                Messages.BpReferencingDropPerformerConfirmationToggleMessage,
                PreferenceConstants.INFO_BP_REFERENCING_CONFIRMATION);
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
                    PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
                            .closeEditor(er.getEditor(true), true);
                }
            } catch (PartInitException e) {
                ExceptionUtil.log(e, Messages.BpReferencingDropPerformer_errorClosingEditors);
            }
        }
    }

    private List<CnATreeElement> getDraggedElements(Object data) {
        List<CnATreeElement> elementList = null;
        if (data instanceof Object[]) {
            elementList = new ArrayList<>(((Object[]) data).length);
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
            return NLS.bind(Messages.BpReferencingDropPerformer_TwoModules,
                    new Object[] { firstModuleTitle, targetTitle });
        } else {
            return NLS.bind(Messages.BpReferencingDropPerformer_MultipleModules,
                    new Object[] { firstModuleTitle, numberMinusOne, targetTitle });
        }
    }

    private String getMessageForOneModule(CnATreeElement module, CnATreeElement target) {
        return NLS.bind(Messages.BpReferencingDropPerformer_OneModule, module.getTitle(),
                target.getTitle());
    }

    /*
     * @see
     * sernet.verinice.iso27k.rcp.action.DropPerformer#validateDrop(java.lang.
     * Object, int, org.eclipse.swt.dnd.TransferData)
     */
    @Override
    public boolean validateDrop(Object rawTarget, int operation, TransferData transferData) {
        if (BaseProtectionModelingTransfer.getInstance().isSupportedType(transferData)) {
            // do not handle elements which are dragged from base protection
            // catalog view
            if (log.isDebugEnabled()) {
                log.debug("Elements dragged from base protection catalog view return false");
            }
            isActive = false;
            return isActive;
        }
        if (!checkRights()) {
            log.debug("ChechRights() failed, return false"); //$NON-NLS-1$
            isActive = false;
        } else {
            if (!getTransfer().isSupportedType(transferData)) {
                log.debug("Unsupported type of TransferData"); //$NON-NLS-1$
            } else {
                isActive = rawTarget instanceof ITargetObject;
            }
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
        Activator.inheritVeriniceContextState();
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

    private boolean confirmReferencing(Set<String> existingModules, CnATreeElement targetElement) {

        String mouleList = existingModules.stream().map(title -> " • " + title)
                .collect(Collectors.joining("\n"));

        String message = NLS.bind(Messages.BpReferencingDropPerformer_ReferencingWarning,
                new Object[] { mouleList });

        return MessageDialog.openQuestion(
                PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
                Messages.BpReferencingDropPerformer_ReferencingWarningTitle, message);
    }

    private void showError(Exception e) {
        final Throwable rootCause = ExceptionUtils.getRootCause(e);
        if (rootCause instanceof BpReferencingException) {
            showReferencingError(rootCause.getMessage());
        } else {
            ExceptionUtil.log(e, Messages.BpReferencingDropPerformer_ReferencingAborted);
        }
    }

    private void showReferencingError(final String causeMessage) {
        final String message = String.join("", causeMessage, " ",
                Messages.BpReferencingDropPerformer_ReferencingAborted, causeMessage);
        Display.getDefault()
                .asyncExec(() -> MessageDialog.openError(Display.getDefault().getActiveShell(),
                        Messages.BpReferencingDropPerformer_ReferencingError, message));
    }

    protected VeriniceElementTransfer getTransfer() {
        return BaseProtectionElementTransfer.getInstance();
    }

    private void addModuleReferences(List<CnATreeElement> draggedModules, CnATreeElement dropTarget)
            throws CommandException {
        ReferencingCommand referencingCommand = new ReferencingCommand(
                draggedModules.stream().map(CnATreeElement::getDbId).collect(Collectors.toSet()),
                dropTarget.getDbId());
        ICommandService commandService = (ICommandService) VeriniceContext
                .get(VeriniceContext.COMMAND_SERVICE);
        commandService.executeCommand(referencingCommand);
        CnAElementFactory.getInstance().reloadBpModelFromDatabase();
    }

    private Set<String> getExistingModules(List<CnATreeElement> draggedModules,
            CnATreeElement dropTarget) throws CommandException {
        CheckReferencingCommand checkReferencingCommand = new CheckReferencingCommand(
                draggedModules.stream().map(CnATreeElement::getDbId).collect(Collectors.toSet()),
                dropTarget.getDbId());
        ICommandService commandService = (ICommandService) VeriniceContext
                .get(VeriniceContext.COMMAND_SERVICE);
        checkReferencingCommand = commandService.executeCommand(checkReferencingCommand);
        return checkReferencingCommand.getExistingModules();
    }

}