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
 *     Daniel Murygin <dm{a}sernet{dot}de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.rcp.dataprotection;

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;

import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Organization;
import sernet.verinice.rcp.RightsEnabledActionDelegate;
import sernet.verinice.service.commands.dataprotection.migration.MigrateDataProtectionCommand;

/**
 * The action to start a data protection migration.
 */
public class MigrateDataProtectionActionDelegate extends RightsEnabledActionDelegate
        implements IWorkbenchWindowActionDelegate {

    private final class RunMigrationCommand implements IRunnableWithProgress {
        private final Set<CnATreeElement> selectedElementSet;
        private MigrateDataProtectionCommand migrateDataProtectionCommand;

        private RunMigrationCommand(Set<CnATreeElement> selectedElementSet) {
            this.selectedElementSet = selectedElementSet;
        }

        @Override
        public void run(IProgressMonitor monitor)
                throws InvocationTargetException, InterruptedException {
            monitor.beginTask("Migrate the organizations to the new dataprotection",
                    IProgressMonitor.UNKNOWN);
            MigrateDataProtectionCommand command = new MigrateDataProtectionCommand(
                    selectedElementSet);

            try {
                migrateDataProtectionCommand = ServiceFactory.lookupCommandService()
                        .executeCommand(command);
                monitor.beginTask("Refreshing data ....", IProgressMonitor.UNKNOWN);
                CnAElementFactory.getInstance().reloadIsoModelFromDatabase();
            } catch (CommandException e) {
                LOG.error("Error while migrating dataprotection", e);
            }
            monitor.done();
        }
    }

    private static final Logger LOG = Logger.getLogger(MigrateDataProtectionActionDelegate.class);

    private CnATreeElement selectedOrganization;

    private Shell shell;

    @Override
    public void doRun(IAction action) {
        try {
            PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().closeAllEditors(true /* ask save */);
            MigrateDataProtectionDialog dialog = new MigrateDataProtectionDialog(getShell());
            dialog.setSelectedElement(selectedOrganization);
            if (dialog.open() == Window.OK) {
                Set<CnATreeElement> selectedElementSet = dialog.getSelectedElementSet();
                RunMigrationCommand iRunnableWithProgressImplementation =
                        new RunMigrationCommand(selectedElementSet);
                PlatformUI.getWorkbench().getProgressService()
                        .busyCursorWhile(iRunnableWithProgressImplementation);

                Set<CnATreeElement> processes = iRunnableWithProgressImplementation.migrateDataProtectionCommand
                        .getProcesses();
                Set<CnATreeElement> controls = iRunnableWithProgressImplementation.migrateDataProtectionCommand
                        .getControls();
                Set<CnATreeElement> missedControls = iRunnableWithProgressImplementation.migrateDataProtectionCommand
                        .getMissedControls();

                displayFinishedDialog(selectedElementSet, processes, controls, missedControls);
            }
        } catch (Exception e) {
            LOG.error("Error running the dataprotection migration.", e);
            MessageDialog.openError(getShell(), "Error while migrating dataprotection",
                    "An error occours");
        }
    }

    private void displayFinishedDialog(Set<CnATreeElement> organizations,
            Set<CnATreeElement> processes, Set<CnATreeElement> controls,
            Set<CnATreeElement> missedControls) {
        StringBuilder sb = new StringBuilder();
        for (CnATreeElement cnATreeElement : missedControls) {
            sb.append(cnATreeElement.getTitle()).append("\n");
        }

        StringBuilder sb1 = new StringBuilder();
        for (CnATreeElement org : organizations) {
            sb1.append(org.getTitle()).append(" ");
        }

        ScrollableMultilineDialog multilineDialog = new ScrollableMultilineDialog(getShell(),
                "The " + sb1.toString() + " are migrated.\n" + processes.size() + " processes and "
                        + controls.size()
                        + " controls\n" + "Missed controls: " + missedControls.size() + "\n"
                        + sb.toString());
        multilineDialog.open();
    }

    @Override
    public String getRightID() {// TODO: right for migrating
        return ActionRightIDs.RISKANALYSIS;
    }

    @Override
    public void init(IWorkbenchWindow window) {
        try {
            shell = window.getShell();
        } catch (Exception e) {
            LOG.error("Error creating ActionDelegate", e); //$NON-NLS-1$
        }
    }

    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        if (selection instanceof ITreeSelection) {
            selectedOrganization = null;
            ITreeSelection selectionCurrent = (ITreeSelection) selection;
            for (Iterator<?> iter = selectionCurrent.iterator(); iter.hasNext();) {
                Object selectedObject = iter.next();
                if (isOrganization(selectedObject)) {
                    selectedOrganization = (CnATreeElement) selectedObject;
                }
            }
        }
    }
    private boolean isOrganization(Object element) {
        return element instanceof Organization;
    }

    private Shell getShell() {
        return shell;
    }
}
