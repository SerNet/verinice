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
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import sernet.gs.service.Retriever;
import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.bsi.dialogs.KonsolidatorDialog;
import sernet.gs.ui.rcp.main.bsi.views.BsiModelView;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.model.bsi.BausteinUmsetzung;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.service.commands.task.KonsolidatorCommand;

public class ShowKonsolidatorAction extends ViewAndWindowAction  {

    public static final String ID = "sernet.gs.ui.rcp.main.actions.showkonsolidatoraction"; //$NON-NLS-1$

    private ShowKonsolidatorAction(String label) {
        super(ActionRightIDs.KONSOLIDATOR, label);
        setId(ID);
        setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.KONSOLIDATOR));
        setToolTipText(Messages.ShowKonsolidatorAction_1);
    }

    public ShowKonsolidatorAction(IWorkbenchWindow window, String label) {
        this(label);
        setWindow(window);
    }

    public ShowKonsolidatorAction(IViewSite site, String label) {
        this(label);
        setSite(site);
    }

    /*
     * (non-Javadoc)
     *
     * @see sernet.gs.ui.rcp.main.actions.RightsEnabledAction#doRun()
     */
    @Override
    protected void doRun(IStructuredSelection selection) {
        Activator.inheritVeriniceContextState();

        final List<BausteinUmsetzung> selectedElements = new ArrayList<BausteinUmsetzung>();
        for (Iterator iter = selection.iterator(); iter.hasNext();) {
            Object o = iter.next();
            if (o instanceof BausteinUmsetzung) {
                BausteinUmsetzung baustein = (BausteinUmsetzung) o;
                initParent(baustein);
                selectedElements.add(baustein);
            }
        }

        final KonsolidatorDialog dialog = new KonsolidatorDialog(getShell(), selectedElements);
        if (dialog.open() != Window.OK || dialog.getSource() == null) {
            return;
        }

        if (!KonsolidatorDialog.askConsolidate(getShell())) {
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
                            Activator.inheritVeriniceContextState();
                            monitor.setTaskName(Messages.ShowKonsolidatorAction_2);
                            monitor.beginTask(Messages.ShowKonsolidatorAction_3,
                                    selectedElements.size() + 1);

                            BausteinUmsetzung source = dialog.getSource();

                            try {
                                // change targets on server:
                                KonsolidatorCommand command = new KonsolidatorCommand(
                                        selectedElements, source);
                                command = ServiceFactory.lookupCommandService()
                                        .executeCommand(command);

                                // reload state from server:
                                for (CnATreeElement element : command.getChangedElements()) {
                                    CnAElementFactory.getLoadedModel()
                                            .databaseChildChanged(element);
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
        CnATreeElement parent = Retriever.checkRetrieveElement(withParent.getParent());
        baustein.setParent(parent);
    }

     @Override
    protected void selectionChanged(IStructuredSelection selection) {
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
        if (checkRights()) {
            setEnabled(true);
        }
    }
}
