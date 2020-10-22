/*******************************************************************************
 * Copyright (c) 2009 Robert Schuster <r.schuster@tarent.de>.
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
 *     Robert Schuster <r.schuster@tarent.de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.actions;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.bsi.dialogs.AccessControlEditDialog;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.interfaces.ApplicationRoles;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.IAuthService;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.Permission;
import sernet.verinice.service.commands.UpdatePermissions;

/**
 * {@link Action} that creates a dialog to modify the access rights of a
 * {@link CnATreeElement}.
 *
 * @author Robert Schuster <r.schuster@tarent.de>
 *
 */
public class ShowAccessControlEditAction extends ViewAndWindowAction {

    private static final String ERROR_MESSAGE = "Error while setting access rights.";

    private static final Logger LOG = Logger.getLogger(ShowAccessControlEditAction.class);

    public static final String ID = "sernet.gs.ui.rcp.main.actions.showaccesscontroleditaction"; //$NON-NLS-1$
    private List<CnATreeElement> elements = new ArrayList<CnATreeElement>();
    private Set<Permission> permissionSetAdd;
    private Set<Permission> permissionSetRemove;
    private boolean isOverride;
    private boolean isUpdateChildren;

    private ShowAccessControlEditAction(String label) {
        super(ActionRightIDs.ACCESSCONTROL, label);
        setId(ID);
        setActionDefinitionId(ID);
        setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.SECURITY));
        setToolTipText(Messages.ShowAccessControlEditAction_1);
    }

    public ShowAccessControlEditAction(IWorkbenchWindow window, String label) {
        this(label);
        setWindow(window);
    }

    public ShowAccessControlEditAction(IViewSite site, String label) {
        this(label);
        setSite(site);
    }

    /*
     * (non-Javadoc)
     *
     * @see sernet.gs.ui.rcp.main.actions.RightsEnabledAction#doRun()
     */
    @Override
    protected void doRun(IStructuredSelection structuredSelection) {
        Activator.inheritVeriniceContextState();
        if (structuredSelection == null || structuredSelection.isEmpty()) {
            return;
        }

        final AccessControlEditDialog dialog = new AccessControlEditDialog(getShell(), structuredSelection);
        if (dialog.open() != Window.OK) {
            return;
        }
        elements = dialog.getElements();
        permissionSetAdd = dialog.getPermissionSetAdd();
        permissionSetRemove = dialog.getPermissionSetRemove();
        isOverride = dialog.isOverride();
        isUpdateChildren = dialog.isUpdateChildren();

        try {
            PlatformUI.getWorkbench().getProgressService()
                    .busyCursorWhile(new IRunnableWithProgress() {
                        @Override
                        public void run(IProgressMonitor monitor)
                                throws InvocationTargetException, InterruptedException {
                            try {
                                monitor.beginTask(Messages.ShowAccessControlEditAction_0,
                                        IProgressMonitor.UNKNOWN);
                                Activator.inheritVeriniceContextState();
                                updatePermissions();
                            } catch (CommandException e) {
                                LOG.error(ERROR_MESSAGE, e); // $NON-NLS-1$
                                throw new RuntimeException(ERROR_MESSAGE, e); // $NON-NLS-1$
                            } finally {
                                if (monitor != null) {
                                    monitor.done();
                                }
                            }
                        }
                    });
        } catch (Exception e) {
            LOG.error(ERROR_MESSAGE, e); // $NON-NLS-1$
            ExceptionUtil.log(e, ERROR_MESSAGE); // $NON-NLS-1$
        }
    }

    private void updatePermissions() throws CommandException {
        for (CnATreeElement element : elements) {
            UpdatePermissions up = new UpdatePermissions(element, permissionSetAdd,
                    permissionSetRemove, isUpdateChildren, isOverride);
            ServiceFactory.lookupCommandService().executeCommand(up);
        }
    }

    @Override
    protected void selectionChanged(IStructuredSelection selection) {
        boolean statisfiedConditions = selection
                .getFirstElement() instanceof CnATreeElement
                && CnAElementHome.getInstance().isOpen();

        if (!statisfiedConditions) {
            setEnabled(false);
            return;
        }
        if (!checkRights()) {
            setEnabled(false);
            return;
        }

        boolean isLocalAdmin = getAuthService()
                .currentUserHasRole(new String[] { ApplicationRoles.ROLE_LOCAL_ADMIN });
        if (isLocalAdmin) {
            boolean isWriteAllowed = isWriteAllowed(selection);
            setEnabled(isLocalAdmin && isWriteAllowed);
        } else {
            setEnabled(true);
        }
    }

    private boolean isWriteAllowed(ISelection selection) {
        boolean isWriteAllowed = false;
        IStructuredSelection sel = (IStructuredSelection) selection;
        for (Iterator<?> iter = sel.iterator(); iter.hasNext();) {
            Object o = iter.next();
            if (o instanceof CnATreeElement) {
                CnATreeElement element = (CnATreeElement) o;
                isWriteAllowed = CnAElementHome.getInstance().isWriteAllowed(element);
                if (!isWriteAllowed) {
                    break;
                }
            }
        }
        return isWriteAllowed;
    }

    private IAuthService getAuthService() {
        return ServiceFactory.lookupAuthService();
    }
}
