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
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.bsi.dialogs.AccessControlEditDialog;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.crudcommands.UpdatePermissions;
import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.IInternalServerStartListener;
import sernet.verinice.interfaces.InternalServerEvent;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.Permission;

/**
 * {@link Action} that creates a dialog to modify the access rights of a
 * {@link CnATreeElement}.
 * 
 * @author Robert Schuster <r.schuster@tarent.de>
 * 
 */
public class ShowAccessControlEditAction extends RightsEnabledAction implements ISelectionListener {

    private static final Logger LOG = Logger.getLogger(ShowAccessControlEditAction.class);
    
    public static final String ID = "sernet.gs.ui.rcp.main.actions.showaccesscontroleditaction"; //$NON-NLS-1$
    private final IWorkbenchWindow window;
    
    private List<CnATreeElement> elements = new ArrayList<CnATreeElement>();
    private Set<Permission> permissionSet;
    private boolean isOverride;
    private boolean isUpdateChildren;

    public ShowAccessControlEditAction(IWorkbenchWindow window, String label) {
        this.window = window;
        setText(label);
        setId(ID);
        setActionDefinitionId(ID);
        setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.SECURITY));
        setToolTipText(Messages.ShowAccessControlEditAction_1);
        window.getSelectionService().addSelectionListener(this);
        setRightID(ActionRightIDs.ACCESSCONTROL);
        if(Activator.getDefault().isStandalone()  && !Activator.getDefault().getInternalServer().isRunning()){
            IInternalServerStartListener listener = new IInternalServerStartListener(){
                @Override
                public void statusChanged(InternalServerEvent e) {
                    if(e.isStarted()){
                        setEnabled(checkRights());
                    }
                }

            };
            Activator.getDefault().getInternalServer().addInternalServerStatusListener(listener);
        } else {
            setEnabled(checkRights());
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    public void run() {
        Activator.inheritVeriniceContextState();
        IStructuredSelection selection = (IStructuredSelection) window.getSelectionService().getSelection();
        if (selection == null || selection.size() < 1) {
            return;
        }
        
        final AccessControlEditDialog dialog = new AccessControlEditDialog(window.getShell(), selection);
        if (dialog.open() != Window.OK) {
            return;
        }
        elements = dialog.getElements();
        permissionSet = dialog.getPermissionSet();
        isOverride = dialog.isOverride();
        isUpdateChildren = dialog.isUpdateChildren();
        
        try {
            PlatformUI.getWorkbench().getProgressService().busyCursorWhile(new IRunnableWithProgress() {
                @Override
                public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    try {
                        monitor.beginTask(Messages.ShowAccessControlEditAction_0, IProgressMonitor.UNKNOWN);
                        Activator.inheritVeriniceContextState();
                        updatePermissions();
                    } catch (CommandException e) {
                        LOG.error("Error while setting access rights.", e); //$NON-NLS-1$
                        throw new RuntimeException("Error while setting access rights.", e); //$NON-NLS-1$
                    } finally {
                        if(monitor!=null) {
                            monitor.done();
                        }
                    }
                }
            }); 
        } catch (Exception e) {
            LOG.error("Error while setting access rights.", e); //$NON-NLS-1$
            ExceptionUtil.log(e, "Error while setting access rights."); //$NON-NLS-1$
        }       
    }
    
    private void updatePermissions() throws CommandException {
        for (CnATreeElement element : elements) {
            UpdatePermissions up = new UpdatePermissions(element, permissionSet, isUpdateChildren, isOverride);
            ServiceFactory.lookupCommandService().executeCommand(up);       
        }
    }

    public void dispose() {
        window.getSelectionService().removeSelectionListener(this);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.ISelectionListener#selectionChanged(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
     */
    @Override
    public void selectionChanged(IWorkbenchPart part, ISelection selection) {
        // Conditions for availability of this action:
        // - Database connection must be open (Implicitly assumes that login
        // credentials have
        // been transferred and that the server can be queried. This is
        // neccessary since this
        // method will be called before the server connection is enabled.)
        // - permission handling is needed by IAuthService implementation
        // - user has administrator privileges
        boolean b = ((IStructuredSelection) selection).getFirstElement() instanceof CnATreeElement
        && CnAElementHome.getInstance().isOpen();
        setEnabled(b && checkRights());
    }

}
