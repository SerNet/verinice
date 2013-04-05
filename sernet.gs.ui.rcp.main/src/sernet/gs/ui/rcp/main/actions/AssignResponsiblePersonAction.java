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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
import sernet.verinice.interfaces.IInternalServerStartListener;
import sernet.verinice.interfaces.InternalServerEvent;
import sernet.verinice.model.bsi.MassnahmenUmsetzung;
import sernet.verinice.model.common.CnATreeElement;

/**
 * @author Julia Haas <jh[at]sernet[dot]de>
 * 
 */
public class AssignResponsiblePersonAction extends RightsEnabledAction implements ISelectionListener {

    public static final String ID = "sernet.gs.ui.rcp.main.actions.assignresponsiblepersonaction"; //$NON-NLS-1$

    private static final Logger LOG = Logger.getLogger(AssignResponsiblePersonAction.class);
    private final IWorkbenchWindow window;
    private boolean serverIsRunning = true;
    private static final String DEFAULT_ERR_MSG = "Error while creating relation.";

    public AssignResponsiblePersonAction(IWorkbenchWindow window, String label) {
        this.window = window;
        setText(label);
        setId(ID);
        setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.PERSON));
        window.getSelectionService().addSelectionListener(this);
        setToolTipText("");
        setRightID(ActionRightIDs.RELATIONS);
        if (Activator.getDefault().isStandalone() && !Activator.getDefault().getInternalServer().isRunning()) {
            serverIsRunning = false;
            IInternalServerStartListener listener = new IInternalServerStartListener() {
                @Override
                public void statusChanged(InternalServerEvent e) {
                    if (e.isStarted()) {
                        serverIsRunning = true;
                        setEnabled(checkRights());
                    }
                }
            };
            Activator.getDefault().getInternalServer().addInternalServerStatusListener(listener);
        } else {
            setEnabled(checkRights());
        }
    }

    @Override
    public void run() {
        Activator.inheritVeriniceContextState();
        final IStructuredSelection selection = (IStructuredSelection) window.getSelectionService().getSelection(BsiModelView.ID);
        if (selection == null) {
            return;
        }
        final List<CnATreeElement> elementList = createList(selection.toList());
      
        try {
            PlatformUI.getWorkbench().getProgressService().busyCursorWhile(new IRunnableWithProgress() {
                @SuppressWarnings("restriction")
                public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    Object sel = null;
                    try {
                        Activator.inheritVeriniceContextState();
                        monitor.beginTask(Messages.AssignResponsiblePersonAction_2, selection.size());

                        for (Iterator iter = elementList.iterator(); iter.hasNext();) {
                            sel = iter.next();
                            MassnahmenUmsetzung massnahme = (MassnahmenUmsetzung) sel;
                            monitor.setTaskName(NLS.bind(Messages.AssignResponsiblePersonAction_3, massnahme.getTitle()));
                            AssignResponsiblePersonCommand command = new AssignResponsiblePersonCommand(massnahme);
                            ServiceFactory.lookupCommandService().executeCommand(command);
                            monitor.worked(1);
                        }
                        showInfoMessage(elementList.size());
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

  

    protected List<CnATreeElement> createList(List elementList) {
    
        List<CnATreeElement> insertList = new ArrayList<CnATreeElement>();
        for (Object sel : elementList) {
            if (sel instanceof MassnahmenUmsetzung) {
                insertList.add((CnATreeElement)sel);
             
            }
        }
        return insertList;
    }
    
    private void showInfoMessage(final Integer anzahl){
        Display.getDefault().asyncExec(new Runnable() {
             @Override
             public void run() {
                 // code der in der GUI laufen soll 
                 MessageDialog.openInformation(window.getShell(), "Info", anzahl + " " + Messages.AssignResponsiblePersonAction_2);
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
        // TODO Auto-generated method stub
        if (serverIsRunning) {
            setEnabled(checkRights());
            if (input instanceof IStructuredSelection) {
                IStructuredSelection selection = (IStructuredSelection) input;
                for (Iterator iter = selection.iterator(); iter.hasNext();) {
                    Object element = iter.next();
                    if (!(element instanceof CnATreeElement)) {
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