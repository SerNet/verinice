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
package sernet.gs.ui.rcp.main.bsi.actions;

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

import sernet.gs.service.RetrieveInfo;
import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.verinice.iso27k.service.Retriever;
import sernet.verinice.model.bsi.BausteinUmsetzung;
import sernet.verinice.model.bsi.IBSIStrukturElement;
import sernet.verinice.model.bsi.ITVerbund;
import sernet.verinice.model.bsi.risikoanalyse.FinishedRiskAnalysis;
import sernet.verinice.model.bsi.risikoanalyse.GefaehrdungsUmsetzung;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.IISO27kElement;
import sernet.verinice.model.iso27k.IISO27kRoot;
import sernet.verinice.model.iso27k.ImportIsoGroup;

/**
 * Delete items on user request.
 * 
 * @author akoderman[at]sernet[dot]de
 * 
 */
public class DeleteActionDelegate implements IObjectActionDelegate {

    private IWorkbenchPart targetPart;

    public void setActivePart(IAction action, IWorkbenchPart targetPart) {
        this.targetPart = targetPart;
    }

    @SuppressWarnings("unchecked")
    public void run(IAction action) {
        Activator.inheritVeriniceContextState();

        final IStructuredSelection selection = ((IStructuredSelection) targetPart.getSite().getSelectionProvider().getSelection());

        if (!MessageDialog.openQuestion((Shell) targetPart.getAdapter(Shell.class), 
                Messages.DeleteActionDelegate_0, 
                NLS.bind(Messages.DeleteActionDelegate_1, selection.size()))) {
            return;
        }

        // ask twice if IT verbund
        boolean goahead = true;
        Iterator iterator = selection.iterator();
        Object object;
        while (iterator.hasNext()) {
            object = iterator.next();
            if (object instanceof ITVerbund || object instanceof IISO27kRoot) {
                if (!goahead) {
                    return;
                }

                String title = Messages.DeleteActionDelegate_3;
                String message = Messages.DeleteActionDelegate_4;
                if (object instanceof ITVerbund) {
                    title = Messages.DeleteActionDelegate_5;
                    message = NLS.bind(Messages.DeleteActionDelegate_6, ((ITVerbund) object).getTitle());
                }
                if (object instanceof IISO27kRoot) {
                    title = Messages.DeleteActionDelegate_8;
                    message = NLS.bind(Messages.DeleteActionDelegate_9, ((IISO27kRoot) object).getTitle());
                }

                if (!MessageDialog.openQuestion((Shell) targetPart.getAdapter(Shell.class), title, message)) {
                    goahead = false;
                    return;
                }
            }
        }

        try {
            PlatformUI.getWorkbench().getProgressService().busyCursorWhile(new IRunnableWithProgress() {
                public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    Activator.inheritVeriniceContextState();
                    monitor.beginTask(Messages.DeleteActionDelegate_11, selection.size());

                    for (Iterator iter = selection.iterator(); iter.hasNext();) {
                        Object sel = iter.next();

                        if (sel instanceof IBSIStrukturElement || sel instanceof BausteinUmsetzung || sel instanceof FinishedRiskAnalysis || sel instanceof GefaehrdungsUmsetzung || sel instanceof ITVerbund || sel instanceof IISO27kRoot || sel instanceof IISO27kElement || sel instanceof ImportIsoGroup) {

                            // do not delete last ITVerbund:
                            try {
                                if (sel instanceof ITVerbund && CnAElementHome.getInstance().getItverbuende().size() < 2) {
                                    ExceptionUtil.log(new Exception(Messages.DeleteActionDelegate_12), Messages.DeleteActionDelegate_13);
                                    return;
                                }
                            } catch (Exception e) {
                                Logger.getLogger(this.getClass()).debug(e);
                            }

                            CnATreeElement el = (CnATreeElement) sel;
                            try {
                                monitor.setTaskName(NLS.bind(Messages.DeleteActionDelegate_14, el.getTitle()));
                                el.getParent().removeChild(el);
                                CnAElementHome.getInstance().remove(el);
                                monitor.worked(1);

                            } catch (Exception e) {
                                ExceptionUtil.log(e, Messages.DeleteActionDelegate_15);
                            }

                        }
                    }

                    // notify all listeners:
                    CnATreeElement child = (CnATreeElement) selection.iterator().next();
                    CnAElementFactory.getModel(child).databaseChildRemoved(child);
                }
            });
        } catch (InvocationTargetException e) {
            ExceptionUtil.log(e.getCause(), Messages.DeleteActionDelegate_16);
        } catch (InterruptedException e) {
            ExceptionUtil.log(e, Messages.DeleteActionDelegate_17);
        }

    }

    public void selectionChanged(IAction action, ISelection selection) {
        // Realizes that the action to delete an element is greyed out,
        // when there is no right to do so.
        Object sel = ((IStructuredSelection) selection).getFirstElement();
        if (sel instanceof CnATreeElement) {
            boolean b = CnAElementHome.getInstance().isDeleteAllowed((CnATreeElement) sel);

            // Only change state when it is enabled, since we do not want to
            // trash the enablement settings of plugin.xml
            if (action.isEnabled()) {
                action.setEnabled(b);
            }
        }
    }

}
