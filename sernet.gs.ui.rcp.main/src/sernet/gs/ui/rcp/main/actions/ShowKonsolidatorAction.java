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

import org.eclipse.core.runtime.IProgressMonitor;
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
import sernet.gs.ui.rcp.main.bsi.dialogs.KonsolidatorDialog;
import sernet.gs.ui.rcp.main.bsi.views.BsiModelView;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.taskcommands.KonsolidatorCommand;
import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.IInternalServerStartListener;
import sernet.verinice.interfaces.InternalServerEvent;
import sernet.verinice.iso27k.service.Retriever;
import sernet.verinice.model.bsi.BausteinUmsetzung;
import sernet.verinice.model.common.CnATreeElement;

public class ShowKonsolidatorAction extends RightsEnabledAction implements ISelectionListener {

    public static final String ID = "sernet.gs.ui.rcp.main.actions.showkonsolidatoraction"; //$NON-NLS-1$

    private final IWorkbenchWindow window;

    public ShowKonsolidatorAction(IWorkbenchWindow window, String label) {
        this.window = window;
        setText(label);
        setId(ID);
        setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.KONSOLIDATOR));
        window.getSelectionService().addSelectionListener(this);
        setToolTipText(Messages.ShowKonsolidatorAction_1);
        setRightID(ActionRightIDs.KONSOLIDATOR);
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

        IStructuredSelection selection = (IStructuredSelection) window.getSelectionService().getSelection(BsiModelView.ID);
        if (selection == null) {
            return;
        }
        final List<BausteinUmsetzung> selectedElements = new ArrayList<BausteinUmsetzung>();
        for (Iterator iter = selection.iterator(); iter.hasNext();) {
            Object o = iter.next();
            if (o instanceof BausteinUmsetzung) {
                BausteinUmsetzung baustein = (BausteinUmsetzung) o;
                initParent(baustein);
                selectedElements.add(baustein);
            }
        }

        final KonsolidatorDialog dialog = new KonsolidatorDialog(window.getShell(), selectedElements);
        if (dialog.open() != Window.OK || dialog.getSource() == null) {
            return;
        }

        if (!KonsolidatorDialog.askConsolidate(window.getShell())) {
            return;
        }

        try {
            // close editors first:
            PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().closeAllEditors(true);

            PlatformUI.getWorkbench().getProgressService().busyCursorWhile(new IRunnableWithProgress() {
                public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    Activator.inheritVeriniceContextState();
                    monitor.setTaskName(Messages.ShowKonsolidatorAction_2);
                    monitor.beginTask(Messages.ShowKonsolidatorAction_3, selectedElements.size() + 1);

                    BausteinUmsetzung source = dialog.getSource();

                    try {
                        // change targets on server:
                        KonsolidatorCommand command = new KonsolidatorCommand(selectedElements, source);
                        command = ServiceFactory.lookupCommandService().executeCommand(command);

                        // reload state from server:
                        for (CnATreeElement element : command.getChangedElements()) {
                            CnAElementFactory.getLoadedModel().databaseChildChanged(element);                        
                        }

                    } catch (CommandException e) {
                        ExceptionUtil.log(e, Messages.ShowKonsolidatorAction_4);
                    }

                    monitor.done();
                }
            });
        } catch (InterruptedException e) {
            ExceptionUtil.log(e, Messages.ShowKonsolidatorAction_5);
        } catch (Exception e) {
            ExceptionUtil.log(e, Messages.ShowKonsolidatorAction_6);
        } 
    }

    /**
     * @param baustein
     */
    private void initParent(/* not final */BausteinUmsetzung baustein) {
       CnATreeElement withParent = Retriever.checkRetrieveParent(baustein);
       CnATreeElement parent =  Retriever.checkRetrieveElement(withParent.getParent());
       baustein.setParent(parent);
    }

    /**
     * Action is enabled when only items of the same type are selected.
     * 
     * @see org.eclipse.ui.ISelectionListener#selectionChanged(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
     */
    public void selectionChanged(IWorkbenchPart part, ISelection input) {
        if (input instanceof IStructuredSelection) {
            IStructuredSelection selection = (IStructuredSelection) input;

            if (selection.size() < 2) {
                setEnabled(false);
                return;
            }

            String kapitel = null;
            for (Iterator iter = selection.iterator(); iter.hasNext();) {
                Object o = iter.next();
                if (o instanceof BausteinUmsetzung) {
                    BausteinUmsetzung bst = (BausteinUmsetzung) o;
                    if (kapitel == null) {
                        kapitel = bst.getKapitel();
                    } else {
                        if (!bst.getKapitel().equals(kapitel)) {
                            setEnabled(false);
                            return;
                        }
                    }
                } else {
                    setEnabled(false);
                    return;
                }
            }
            if(checkRights()){
                setEnabled(true);
            }
            return;
        }
        // no structured selection:
        setEnabled(false);
    }

    private void dispose() {
        window.getSelectionService().removeSelectionListener(this);
    }
}
