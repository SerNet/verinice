/*******************************************************************************
 * Copyright (c) 2013 Daniel Murygin.
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
package sernet.gs.ui.rcp.main.bsi.actions;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.springframework.dao.DataIntegrityViolationException;

import sernet.gs.service.RetrieveInfo;
import sernet.gs.service.Retriever;
import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.bsi.editors.BSIElementEditorInput;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.model.bp.DeductionImplementationUtil;
import sernet.verinice.model.bp.IBpElement;
import sernet.verinice.model.bp.elements.BpModel;
import sernet.verinice.model.bp.elements.Safeguard;
import sernet.verinice.model.bsi.ITVerbund;
import sernet.verinice.model.bsi.Person;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.IISO27kElement;
import sernet.verinice.model.iso27k.IISO27kGroup;
import sernet.verinice.model.iso27k.Organization;
import sernet.verinice.model.iso27k.PersonIso;
import sernet.verinice.rcp.RightsEnabledHandler;
import sernet.verinice.service.commands.LoadConfiguration;
import sernet.verinice.service.commands.crud.LoadReportElements;
import sernet.verinice.service.commands.crud.PrepareObjectWithAccountDataForDeletion;

/**
 *
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class DeleteHandler extends RightsEnabledHandler {

    private static final Logger LOG = Logger.getLogger(DeleteHandler.class);

    protected static final String DEFAULT_ERR_MSG = "Error while deleting element.";

    /*
     * 
     * @see
     * org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.
     * ExecutionEvent)
     */
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        final IStructuredSelection selection = (IStructuredSelection) HandlerUtil
                .getCurrentSelection(event);
        return execute(selection);
    }

    public Object execute(final IStructuredSelection selection) {
        changeSelection(selection);
        try {
            Activator.inheritVeriniceContextState();

            if (!MessageDialog.openQuestion(Display.getCurrent().getActiveShell(),
                    Messages.DeleteActionDelegate_0,
                    NLS.bind(Messages.DeleteActionDelegate_1, selection.size()))) {
                return null;
            }

            final List<CnATreeElement> deleteList = createList(selection.toList());

            closeOpenEditors(deleteList);

            if (!deleteList.isEmpty() && deleteList.get(0) instanceof IISO27kElement) {
                doDeleteIso(deleteList);
            } else {
                doDelete(deleteList);
            }
        } catch (InvocationTargetException e) {
            LOG.error(DEFAULT_ERR_MSG, e);
            ExceptionUtil.log(e.getCause(), Messages.DeleteActionDelegate_16);
        } catch (InterruptedException e) {
            LOG.error(DEFAULT_ERR_MSG, e);
            ExceptionUtil.log(e, Messages.DeleteActionDelegate_17);
        } catch (Exception e) {
            LOG.error(DEFAULT_ERR_MSG, e);
            ExceptionUtil.log(e, Messages.DeleteActionDelegate_17);
        }
        return null;
    }

    /**
     * closes editors of elements that are going to be deleted
     */
    private void closeOpenEditors(List<CnATreeElement> deleteList) throws PartInitException {
        Set<IEditorReference> editorsToCloseSet = getRelevantEditors(deleteList);
        List<IEditorReference> editorsToCloseList = new ArrayList<>(editorsToCloseSet.size());
        editorsToCloseList.addAll(editorsToCloseSet);
        PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
                .closeEditors((IEditorReference[]) editorsToCloseList
                        .toArray(new IEditorReference[editorsToCloseList.size()]), true);
    }

    /**
     * iterates all elements that are going to be deleted, checks if an editor
     * for this element is currently open (special handling for
     * {@link ITVerbund} and {@link Organization}) and returns a set of editors
     * that needs to be closed because their content is going to be deleted
     * 
     * @param deleteList
     * @throws PartInitException
     */
    private Set<IEditorReference> getRelevantEditors(List<CnATreeElement> deleteList)
            throws PartInitException {
        Set<IEditorReference> closeableEditors = new HashSet<>();
        for (CnATreeElement elementToDelete : deleteList) {
            if (!elementToDelete.isScope()) {
                Optional<IEditorReference> reference = findOpenEditor(elementToDelete);
                reference.ifPresent(closeableEditors::add);
                if (Safeguard.isSafeguard(elementToDelete)) {
                    addEditorsForLinkedRequirements(elementToDelete, closeableEditors);
                }
            } else { // if element is a scope, add all editors that shows
                     // children of that scope
                closeableEditors.addAll(findOpenEditors(elementToDelete.getScopeId()));
            }
        }
        return closeableEditors;
    }

    private void addEditorsForLinkedRequirements(CnATreeElement elementToDelete,
            Set<IEditorReference> closeableEditors) throws PartInitException {
        CnATreeElement safeguard = elementToDelete;
        if (!Retriever.areLinksInitizialized(safeguard, true)) {
            RetrieveInfo retrieveInfo = new RetrieveInfo().setLinksUp(true)
                    .setLinksUpProperties(true);
            safeguard = Retriever.retrieveElement(safeguard, retrieveInfo);
        }
        for (CnALink link : safeguard.getLinksUp()) {
            if (DeductionImplementationUtil.isRelevantLinkForImplementationStateDeduction(link)) {
                CnATreeElement requirement = link.getDependant();
                if (DeductionImplementationUtil.isDeductiveImplementationEnabled(requirement)) {
                    Optional<IEditorReference> editor = findOpenEditor(requirement);
                    editor.ifPresent(closeableEditors::add);

                }
            }
        }
    }

    /**
     * returns all editors that shows elements with a given scope id
     */
    private Set<IEditorReference> findOpenEditors(int scopeId) throws PartInitException {
        Set<IEditorReference> closeableEditors = new HashSet<>();
        for (IEditorReference editorReference : PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                .getActivePage().getEditorReferences()) {
            Object o = editorReference.getEditorInput();
            if (o instanceof BSIElementEditorInput) {
                BSIElementEditorInput bsiElementEditorInput = (BSIElementEditorInput) o;
                if (scopeId == bsiElementEditorInput.getCnAElement().getScopeId()) {
                    closeableEditors.add(editorReference);
                }
            }
        }
        return closeableEditors;
    }

    /**
     * returns {@link IEditorReference} of given {@link CnATreeElement} if
     * element is currently opened by an editor
     */
    private Optional<IEditorReference> findOpenEditor(CnATreeElement element)
            throws PartInitException {
        for (IEditorReference editorReference : PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                .getActivePage().getEditorReferences()) {
            Object o = editorReference.getEditorInput();
            if (o instanceof BSIElementEditorInput) {
                BSIElementEditorInput bsiElementEditorInput = (BSIElementEditorInput) o;
                if (element.equals(bsiElementEditorInput.getCnAElement())) {
                    return Optional.of(editorReference);
                }
            }
        }
        return Optional.empty();

    }

    protected void doDelete(final List<CnATreeElement> deleteList)
            throws InvocationTargetException, InterruptedException {
        PlatformUI.getWorkbench().getProgressService()
                .busyCursorWhile(new DeleteElements(deleteList));
    }

    protected void doDeleteIso(final List<CnATreeElement> deleteList)
            throws InvocationTargetException, InterruptedException {
        PlatformUI.getWorkbench().getProgressService().busyCursorWhile(new IRunnableWithProgress() {
            @Override
            public void run(IProgressMonitor monitor)
                    throws InvocationTargetException, InterruptedException {
                Object sel = null;
                try {
                    Activator.inheritVeriniceContextState();
                    monitor.beginTask(Messages.DeleteActionDelegate_11, deleteList.size());
                    monitor.setTaskName(Messages.DeleteActionDelegate_14);
                    removeElements(deleteList);
                } catch (DataIntegrityViolationException dive) {
                    deleteElementWithAccountAsync((CnATreeElement) sel);
                } catch (Exception e) {
                    LOG.error(DEFAULT_ERR_MSG, e);
                    ExceptionUtil.log(e, Messages.DeleteActionDelegate_15);
                }
            }
        });
    }

    protected List<CnATreeElement> createList(List<CnATreeElement> elementList) {
        List<CnATreeElement> tempList = new ArrayList<>();
        List<CnATreeElement> insertList = new ArrayList<>();
        if (elementList.size() > 1) {
            for (CnATreeElement element : elementList) {
                createList(element, tempList, insertList);
            }
        } else {
            // add last element
            insertList.add(elementList.get(0));
        }
        return insertList;
    }

    private void createList(CnATreeElement element, List<CnATreeElement> tempList,
            List<CnATreeElement> insertList) {
        if (!tempList.contains(element)) {
            tempList.add(element);
            insertList.add(element);
            if (element instanceof IISO27kGroup && element.getChildren() != null) {
                element = Retriever.checkRetrieveChildren(element);
                for (CnATreeElement child : element.getChildren()) {
                    createList(child, tempList, insertList);
                }
            }
        } else {
            insertList.remove(element);
        }
    }

    protected static void deleteElementWithAccountAsync(final CnATreeElement element) {
        Display.getDefault().asyncExec(() -> {
            try {
                deleteElementWithAccount(element);
            } catch (CommandException e1) {
                LOG.error(DEFAULT_ERR_MSG, e1);
                ExceptionUtil.log(e1, Messages.DeleteActionDelegate_15);
            } catch (DataIntegrityViolationException de) {
                LOG.error(DEFAULT_ERR_MSG, de);
            } catch (Exception e2) {
                LOG.error(DEFAULT_ERR_MSG, e2);
                ExceptionUtil.log(e2, Messages.DeleteActionDelegate_15);
            }
        });
    }

    protected static void deleteElementWithAccount(final CnATreeElement element)
            throws CommandException {
        GenericCommand command = null;
        if (loadConfiguration(element)) {
            if (MessageDialog.openConfirm(Display.getDefault().getActiveShell(),
                    Messages.DeleteActionDelegate_0, Messages.DeleteActionDelegate_18)) {
                command = new PrepareObjectWithAccountDataForDeletion(element);
                ServiceFactory.lookupCommandService().executeCommand(command);
            } else {
                return;
            }
        }
        removeElements(Set.of(element));
    }

    protected static void removeElements(Collection<CnATreeElement> elementsToRemove)
            throws CommandException {
        CnAElementHome.getInstance().remove(elementsToRemove);
        elementsToRemove.forEach(elementToRemove -> {
            CnAElementFactory.getModel(elementToRemove).databaseChildRemoved(elementToRemove);
            CnAElementFactory.getInstance().getCatalogModel().databaseChildRemoved(elementToRemove);
        });
    }

    protected static boolean loadConfiguration(CnATreeElement elmt) {
        String[] types = new String[] { Person.TYPE_ID, PersonIso.TYPE_ID };
        ICommandService service = ServiceFactory.lookupCommandService();
        for (String type : types) {
            try {
                LoadReportElements command = new LoadReportElements(type, elmt.getDbId());
                command = service.executeCommand(command);
                for (CnATreeElement person : command.getElements()) {
                    LoadConfiguration command2 = new LoadConfiguration(person);
                    command2 = service.executeCommand(command2);
                    if (command2.getConfiguration() != null) {
                        return true;
                    }
                }
            } catch (CommandException e) {
                LOG.error("Error determing existence of configuration objects", e);
            }
        }

        return false;
    }

    private void changeSelection(ISelection selection) {
        boolean allowed = checkRights();
        boolean isWriteAllowed = true;

        // Realizes that the action to delete an element is greyed out,
        // when there is no right to do so.
        Object sel = ((IStructuredSelection) selection).getFirstElement();
        if (allowed && sel instanceof CnATreeElement) {
            CnATreeElement element = (CnATreeElement) sel;
            isWriteAllowed = CnAElementHome.getInstance().isDeleteAllowed(element);
        }

        // Only change state when it is enabled, since we do not want to
        // trash the enablement settings of plugin.xml
        if (this.isEnabled()) {
            this.setEnabled(isWriteAllowed && allowed);
        }
    }

    /*
     * 
     * @see sernet.verinice.interfaces.RightEnabledUserInteraction#getRightID()
     */
    @Override
    public String getRightID() {
        return ActionRightIDs.DELETEITEM;
    }

    private static final class DeleteElements implements IRunnableWithProgress {
        private final List<CnATreeElement> deleteList;

        private DeleteElements(List<CnATreeElement> deleteList) {
            this.deleteList = deleteList;
        }

        @Override
        public void run(IProgressMonitor monitor)
                throws InvocationTargetException, InterruptedException {
            CnATreeElement sel = null;
            try {
                Activator.inheritVeriniceContextState();
                monitor.beginTask(Messages.DeleteActionDelegate_14, IProgressMonitor.UNKNOWN);
                boolean reloadBpModel = deleteList.stream().anyMatch(IBpElement.class::isInstance);
                removeElements(deleteList);
                if (reloadBpModel) {
                    BpModel bpModel = CnAElementFactory.getInstance().getBpModel();
                    bpModel.modelReload(bpModel);
                }
            } catch (DataIntegrityViolationException dive) {
                deleteElementWithAccountAsync((CnATreeElement) sel);
            } catch (Exception e) {
                LOG.error(DEFAULT_ERR_MSG, e);
                ExceptionUtil.log(e, Messages.DeleteActionDelegate_15);
            } finally {
                if (monitor != null) {
                    monitor.done();
                }
            }
        }
    }

}