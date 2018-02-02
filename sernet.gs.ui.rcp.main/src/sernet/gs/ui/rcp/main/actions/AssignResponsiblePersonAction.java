/*******************************************************************************
 * Copyright (c) 2013 Julia Haas.
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
 *     Julia Haas <jh[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.actions;

/**
 * @author Julia Haas <jh[at]sernet[dot]de>
 *
 */

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.bsi.views.BsiModelView;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.model.bsi.MassnahmenUmsetzung;
import sernet.verinice.service.commands.AssignResponsiblePersonCommand;

/**
 * @author Julia Haas <jh[at]sernet[dot]de>
 * 
 */
public class AssignResponsiblePersonAction extends RightsEnabledAction implements ISelectionListener {

    public static final String ID = "sernet.gs.ui.rcp.main.actions.assignresponsiblepersonaction"; //$NON-NLS-1$

    private static final Logger LOG = Logger.getLogger(AssignResponsiblePersonAction.class);
    private final IWorkbenchWindow window;
    private static final String DEFAULT_ERR_MSG = "Error while creating relation.";

    public AssignResponsiblePersonAction(IWorkbenchWindow window, String label) {
        super(ActionRightIDs.RELATIONS, label);
        this.window = window;
        setId(ID);
        setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.PERSON));
        window.getSelectionService().addSelectionListener(this);
        setToolTipText("");
    }

    /* (non-Javadoc)
     * @see sernet.gs.ui.rcp.main.actions.RightsEnabledAction#doRun()
     */
    @Override
    public void doRun() {
        Activator.inheritVeriniceContextState();
        final IStructuredSelection selection = (IStructuredSelection) window.getSelectionService().getSelection(BsiModelView.ID);
        if (selection == null) {
            return;
        }

        try {
            PlatformUI.getWorkbench().getProgressService().busyCursorWhile(new IRunnableWithProgress() {
                @Override
                @SuppressWarnings("restriction")
                public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    try {
                        Activator.inheritVeriniceContextState();

                        AssignResponsiblePersonCommand command = new AssignResponsiblePersonCommand(selection.toList());
                        command = ServiceFactory.lookupCommandService().executeCommand(command);
                        if(command.getchanedElements().size() > 0){
                            showInfoMessage(command.getchanedElements().size(), selection.toList().size());
                        }
                        else{
                            showMessage();
                        }
                        if(command.getlinkedElements().size() > 0){
                            showAnotherInfoMessage(command.getlinkedElements().size());
                        }
                    } catch (Exception e) {
                        LOG.error("Error while command", e);
                    }
                }
            });
        } catch (InvocationTargetException e) {
            LOG.error(DEFAULT_ERR_MSG, e);

        } catch (InterruptedException e) {
            LOG.error(DEFAULT_ERR_MSG, e);
        }
    }
    
    private void showInfoMessage(final Integer differenz, final Integer summe) {
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                MessageDialog.openInformation(window.getShell(), "Info", NLS.bind(Messages.AssignResponsiblePersonAction_1, differenz, summe));//$NON-NLS-1$
            }
        });
    }

   private void showAnotherInfoMessage(final Integer anzahl) {
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                MessageDialog.openInformation(window.getShell(), "Info", anzahl + " " + Messages.AssignResponsiblePersonAction_2);
            }
        });
    }
   
   private void showMessage() {
       Display.getDefault().asyncExec(new Runnable() {
           @Override
           public void run() {
               MessageDialog.openInformation(window.getShell(), "Info", Messages.AssignResponsiblePersonAction_3);
           }
       });
   }

    
    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.ISelectionListener#selectionChanged(org.eclipse.ui.
     * IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
     */
    @Override
    public void selectionChanged(IWorkbenchPart part, ISelection input) {
        if (isServerRunning()) {
            setEnabled(checkRights());
            if (input instanceof IStructuredSelection) {
                IStructuredSelection selection = (IStructuredSelection) input;
                for (Iterator iter = selection.iterator(); iter.hasNext();) {
                    Object element = iter.next();
                    if (!(element instanceof MassnahmenUmsetzung)) {
                        setEnabled(false);
                        return;
                    }
                }
                if (checkRights()) {
                    setEnabled(true);
                }
                return;
            }
            setEnabled(false);
        }
    }

}