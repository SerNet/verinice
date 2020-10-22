/*******************************************************************************
 * Copyright (c) 2019 Jochen Kemnade.
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
 ******************************************************************************/
package sernet.gs.ui.rcp.main.actions;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.naming.ConfigurationException;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.bsi.dialogs.PersonBulkEditDialog;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.hui.common.connect.Entity;
import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.PasswordException;
import sernet.verinice.model.bp.elements.BpPerson;
import sernet.verinice.model.bsi.Person;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.PersonIso;
import sernet.verinice.service.commands.CreateConfiguration;
import sernet.verinice.service.commands.LoadConfiguration;
import sernet.verinice.service.commands.task.ConfigurationBulkEditUpdate;

/**
 * Edit multiple accounts at once
 */
public class ShowBulkEditAccountsAction extends ViewAndWindowAction {

    private static final Logger logger = Logger.getLogger(ShowBulkEditAccountsAction.class);

    private List<Integer> dbIDs;

    public static final String ID = "sernet.gs.ui.rcp.main.actions.showbulkeditaccountsaction"; //$NON-NLS-1$

    private static final Set<String> SUPPORTED_ELEMENT_TYPES = Stream
            .of(Person.TYPE_ID, PersonIso.TYPE_ID, BpPerson.TYPE_ID).collect(Collectors.toSet());

    private ShowBulkEditAccountsAction(String label) {
        super(ActionRightIDs.ACCOUNTSETTINGS, label);
        setId(ID);
        setActionDefinitionId(ID);
        setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.ACCOUNTS_BULK));
        setToolTipText(Messages.EditMultipleAccountsTogether);
    }

    public ShowBulkEditAccountsAction(IWorkbenchWindow window, String label) {
        this(label);
        setWindow(window);
    }

    public ShowBulkEditAccountsAction(IViewSite site, String label) {
        this(label);
        setSite(site);
    }

    /*
     * @see sernet.gs.ui.rcp.main.actions.RightsEnabledAction#doRun()
     */
    @Override
    protected void doRun(IStructuredSelection selection) {
        Activator.inheritVeriniceContextState();

        Optional<CnATreeElement> nonWritableElement = findNonWritableElement(selection);
        if (nonWritableElement.isPresent()) {
            MessageDialog.openWarning(getShell(), Messages.ShowBulkEditAction_2,
                    NLS.bind(Messages.ShowBulkEditAction_3, (nonWritableElement.get()).getTitle()));
            setEnabled(false);
            return;
        }

        dbIDs = new ArrayList<>(selection.size());
        readSelection(selection);
        PersonBulkEditDialog dialog = new PersonBulkEditDialog(getShell(),
                Messages.ShowBulkEditAction_14);

        if (dialog.open() != Window.OK) {
            return;
        }

        try {
            // close editors first:
            PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
                    .closeAllEditors(true);

            PlatformUI.getWorkbench().getProgressService()
                    .busyCursorWhile(new IRunnableWithProgress() {
                        @Override
                        public void run(IProgressMonitor monitor)
                                throws InvocationTargetException, InterruptedException {
                            doEdit(dialog, monitor);
                        }
                    });
        } catch (InterruptedException e) {
            ExceptionUtil.log(e, Messages.ShowBulkEditAction_5);
        } catch (Exception e) {
            logger.error("Error on bulk edit", e);
            ExceptionUtil.log(e, Messages.ShowBulkEditAction_6);
        }
    }

    private void doEdit(final PersonBulkEditDialog dialog, IProgressMonitor monitor)
            throws InterruptedException {
        Activator.inheritVeriniceContextState();

        try {
            String pw1 = dialog.getPassword();
            String pw2 = dialog.getPassword2();
            editPersons(dbIDs, dialog.getEntity(), monitor, pw1, pw2);
        } catch (CommandException e) {
            throw new InterruptedException(e.getLocalizedMessage());
        }

        monitor.done();
    }

    private void readSelection(IStructuredSelection selection) {
        for (Iterator<?> iter = selection.iterator(); iter.hasNext();) {
            CnATreeElement cElmt = (CnATreeElement) iter.next();
            LoadConfiguration command = new LoadConfiguration(cElmt);
            try {
                command = ServiceFactory.lookupCommandService().executeCommand(command);
                if (command.getConfiguration() != null) {
                    dbIDs.add(command.getConfiguration().getDbId());
                } else { // no configuration existing for this user up to
                         // here, create new one
                    CreateConfiguration command2 = new CreateConfiguration(cElmt);
                    command2 = ServiceFactory.lookupCommandService().executeCommand(command2);
                    dbIDs.add(command2.getConfiguration().getDbId());
                }
            } catch (CommandException e) {
                logger.error("Error while retrieving configuration", e);
                ExceptionUtil.log(e, Messages.ShowBulkEditAction_6);
            }
        }
    }

    private Optional<CnATreeElement> findNonWritableElement(IStructuredSelection selection) {
        // returns the first selected element for which the current user has no
        // write permission, if any
        Iterator<?> iterator = (selection).iterator();
        while (iterator.hasNext()) {
            Object next = iterator.next();
            if (next instanceof CnATreeElement) {
                CnATreeElement element = (CnATreeElement) next;
                boolean writeallowed = CnAElementHome.getInstance().isWriteAllowed(element);
                if (!writeallowed) {
                    return Optional.of(element);
                }
            }
        }
        return Optional.empty();
    }

    private void editPersons(List<Integer> dbIDs, Entity dialogEntity, IProgressMonitor monitor,
            String newPassword, String newPassword2) throws CommandException {
        monitor.setTaskName(Messages.ShowBulkEditAction_7);
        monitor.beginTask(Messages.ShowBulkEditAction_8, IProgressMonitor.UNKNOWN);

        boolean changePassword = false;
        if (newPassword != null && !newPassword.isEmpty()) {
            if (!newPassword.equals(newPassword2)) {
                throw new PasswordException(Messages.ConfigurationAction_10);
            } else {
                changePassword = true;
            }
        }
        ConfigurationBulkEditUpdate command = new ConfigurationBulkEditUpdate(dbIDs, dialogEntity,
                changePassword, newPassword);

        command = ServiceFactory.lookupCommandService().executeCommand(command);
        if (!command.getFailedUpdates().isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append(Messages.ShowBulkEditAction_15).append(":\n");
            for (String username : command.getFailedUpdates()) {
                sb.append(username).append("\n");
            }
            ExceptionUtil.log(new ConfigurationException(Messages.ShowBulkEditAction_16),
                    Messages.ShowBulkEditAction_16 + "\n" + sb.toString());
        }

    }

    @Override
    protected void selectionChanged(IStructuredSelection selection) {
        boolean selectionEmpty = selection.isEmpty();
        if (selectionEmpty || !(selection.getFirstElement() instanceof CnATreeElement)) {
            setEnabled(false);
            return;
        }

        String firstElementType = ((CnATreeElement) selection.getFirstElement()).getTypeId();
        if (!SUPPORTED_ELEMENT_TYPES.contains(firstElementType)) {
            setEnabled(false);
            return;
        }

        for (Object item : selection.toList()) {
            if (!(item instanceof CnATreeElement)
                    || !((CnATreeElement) item).getTypeId().equals(firstElementType)) {
                setEnabled(false);
                return;
            }
        }

        if (!checkRights()) {
            setEnabled(false);
            return;
        }
        setEnabled(true);
    }
}