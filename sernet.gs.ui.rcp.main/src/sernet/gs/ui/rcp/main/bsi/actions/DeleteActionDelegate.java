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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.springframework.dao.DataIntegrityViolationException;

import sernet.gs.service.RetrieveInfo;
import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadChildrenForExpansion;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadConfiguration;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadReportElements;
import sernet.gs.ui.rcp.main.service.crudcommands.PrepareObjectWithAccountDataForDeletion;
import sernet.hui.common.VeriniceContext;
import sernet.springclient.RightsServiceClient;
import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.iso27k.service.Retriever;
import sernet.verinice.model.bsi.BausteinUmsetzung;
import sernet.verinice.model.bsi.IBSIStrukturElement;
import sernet.verinice.model.bsi.ITVerbund;
import sernet.verinice.model.bsi.MassnahmenUmsetzung;
import sernet.verinice.model.bsi.Person;
import sernet.verinice.model.bsi.risikoanalyse.FinishedRiskAnalysis;
import sernet.verinice.model.bsi.risikoanalyse.GefaehrdungsUmsetzung;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.IISO27kElement;
import sernet.verinice.model.iso27k.IISO27kGroup;
import sernet.verinice.model.iso27k.IISO27kRoot;
import sernet.verinice.model.iso27k.ImportIsoGroup;
import sernet.verinice.model.iso27k.PersonIso;
import sernet.verinice.service.commands.LoadElementByUuid;

/**
 * Delete items on user request.
 * 
 * @author akoderman[at]sernet[dot]de
 * 
 */
@SuppressWarnings("restriction")
public class DeleteActionDelegate implements IObjectActionDelegate {

    private static final Logger LOG = Logger.getLogger(DeleteActionDelegate.class);
    
    private static final String DEFAULT_ERR_MSG = "Error while deleting element.";

    private IWorkbenchPart targetPart;

    public void setActivePart(IAction action, IWorkbenchPart targetPart) {
        this.targetPart = targetPart;
    }

    public void run(IAction action) {
        try {
            Activator.inheritVeriniceContextState();

            final IStructuredSelection selection = ((IStructuredSelection) targetPart.getSite().getSelectionProvider().getSelection());

            if (!MessageDialog.openQuestion((Shell) targetPart.getAdapter(Shell.class), Messages.DeleteActionDelegate_0, NLS.bind(Messages.DeleteActionDelegate_1, selection.size()))) {
                return;
            }

            // ask twice if IT verbund
            boolean goahead = true;
            final List<CnATreeElement> deleteList = createList(selection.toList());
            Iterator iterator = deleteList.iterator();
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

            PlatformUI.getWorkbench().getProgressService().busyCursorWhile(new IRunnableWithProgress() {
                public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    Object sel = null;
                    try {
                        Activator.inheritVeriniceContextState();
                        monitor.beginTask(Messages.DeleteActionDelegate_11, selection.size());
                        for (Iterator iter = deleteList.iterator(); iter.hasNext();) {
                            sel = iter.next();

                            if (sel instanceof IBSIStrukturElement || sel instanceof BausteinUmsetzung || sel instanceof MassnahmenUmsetzung || sel instanceof FinishedRiskAnalysis || sel instanceof GefaehrdungsUmsetzung || sel instanceof ITVerbund || sel instanceof IISO27kRoot || sel instanceof IISO27kElement || sel instanceof ImportIsoGroup) {

                                // do not delete last ITVerbund:
                                if (sel instanceof ITVerbund && CnAElementHome.getInstance().getItverbuende().size() < 2) {
                                    ExceptionUtil.log(new Exception(Messages.DeleteActionDelegate_12), Messages.DeleteActionDelegate_13);
                                    return;
                                }

                                CnATreeElement el = (CnATreeElement) sel;
                                monitor.setTaskName(NLS.bind(Messages.DeleteActionDelegate_14, el.getTitle()));

                                CnAElementHome.getInstance().remove(el);
                                // notify all listeners:
                                CnAElementFactory.getModel(el).databaseChildRemoved(el);
                                monitor.worked(1);
                            }
                        }
                    } catch (DataIntegrityViolationException dive) {
                        // try solving exception by deleting configuration /
                        // riskanalysis first
                        final CnATreeElement el = (CnATreeElement) sel;
                        Display.getDefault().asyncExec(new Runnable() {

                            @Override
                            public void run() {
                                try {
                                    GenericCommand command = null;
                                    if (el.getClass().getPackage().getName().contains("model.bsi")) {
                                        iterateThroughGroup(el);
                                    }
                                    if (determineConfiguration(el)) {
                                        if (MessageDialog.openConfirm(Display.getDefault().getActiveShell(), Messages.DeleteActionDelegate_0, Messages.DeleteActionDelegate_18)) {
                                            command = new PrepareObjectWithAccountDataForDeletion(el);
                                            command = ServiceFactory.lookupCommandService().executeCommand(command);
                                        } else {
                                            return;
                                        }                                    
                                    }
                                    removeElement(el);
                                } catch (CommandException e) {
                                    LOG.error(DEFAULT_ERR_MSG, e);
                                    ExceptionUtil.log(e, Messages.DeleteActionDelegate_15);
                                } catch (DataIntegrityViolationException de) {
                                    LOG.error(DEFAULT_ERR_MSG, de);
                                } catch (Exception e) {
                                    LOG.error(DEFAULT_ERR_MSG, e);
                                    ExceptionUtil.log(e, Messages.DeleteActionDelegate_15);
                                }
                            }
                        });
                    } catch (Exception e) {
                        LOG.error(DEFAULT_ERR_MSG, e);
                        ExceptionUtil.log(e, Messages.DeleteActionDelegate_15);
                    }
                }
            });
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
    }

    protected List<CnATreeElement> createList(List elementList) {
        List<CnATreeElement> tempList = new ArrayList<CnATreeElement>();
        List<CnATreeElement> insertList = new ArrayList<CnATreeElement>();
        int depth = 0;
        int removed = 0;
        for (Object sel : elementList) {
            if (sel instanceof IBSIStrukturElement || sel instanceof BausteinUmsetzung || sel instanceof MassnahmenUmsetzung || sel instanceof FinishedRiskAnalysis || sel instanceof GefaehrdungsUmsetzung || sel instanceof ITVerbund || sel instanceof IISO27kRoot || sel instanceof IISO27kElement || sel instanceof ImportIsoGroup) {
                createList((CnATreeElement) sel, tempList, insertList, depth, removed);
            }
        }
        return insertList;
    }

    private void createList(CnATreeElement element, List<CnATreeElement> tempList, List<CnATreeElement> insertList, int depth, int removed) {
        if (!tempList.contains(element)) {
            tempList.add(element);
            if (depth == 0) {
                insertList.add(element);
            }
            if (element instanceof IISO27kGroup && element.getChildren() != null) {
                int newDepth = depth++;
                element = Retriever.checkRetrieveChildren(element);
                for (CnATreeElement child : element.getChildren()) {
                    createList(child, tempList, insertList, newDepth, removed);
                }
            }
        } else {
            insertList.remove(element);
            removed++;
        }
    }

    public void selectionChanged(IAction action, ISelection selection) {
        boolean allowed = ((RightsServiceClient) VeriniceContext.get(VeriniceContext.RIGHTS_SERVICE)).isEnabled(ActionRightIDs.DELETEITEM);
        // Realizes that the action to delete an element is greyed out,
        // when there is no right to do so.
        Object sel = ((IStructuredSelection) selection).getFirstElement();
        if (sel instanceof CnATreeElement) {
            CnATreeElement element = (CnATreeElement) sel;
            boolean b = CnAElementHome.getInstance().isDeleteAllowed(element);

            // Only change state when it is enabled, since we do not want to
            // trash the enablement settings of plugin.xml
            if (action.isEnabled()) {
                action.setEnabled(b & allowed);
            }
        }
    }

    private void removeElement(CnATreeElement elementToRemove) {
        try {
            if (!elementToRemove.isChildrenLoaded()) {
                elementToRemove = loadChildren(elementToRemove);
            }
            if (elementToRemove instanceof FinishedRiskAnalysis) {
                RetrieveInfo info = new RetrieveInfo().setParent(true).setChildren(true);
                LoadElementByUuid<FinishedRiskAnalysis> command = new LoadElementByUuid<FinishedRiskAnalysis>(elementToRemove.getUuid(), info);
                command = ServiceFactory.lookupCommandService().executeCommand(command);
                elementToRemove = command.getElement();
            }
            CnAElementHome.getInstance().remove(elementToRemove);
            CnAElementFactory.getModel(elementToRemove).databaseChildRemoved(elementToRemove);
        } catch (Exception e) {
            LOG.error("Error while deleting risk analysis", e);
        }
    }

    private void iterateThroughGroup(CnATreeElement element) throws CommandException {
        if (!element.isChildrenLoaded()) {
            element = loadChildren(element);
        }
        if (canContainRiskAnalysis(element)) {
            freeBSIElement(element);
        }
        for (CnATreeElement child : element.getChildren()) {
            if (canContainRiskAnalysis(child)) {
                freeBSIElement(child);
            } else {
                iterateThroughGroup(child);
            }
        }
    }

    private CnATreeElement loadChildren(CnATreeElement element) throws CommandException {
        LoadChildrenForExpansion command = new LoadChildrenForExpansion(element);
        command = ServiceFactory.lookupCommandService().executeCommand(command);
        element = ((LoadChildrenForExpansion) command).getElementWithChildren();
        element.setChildrenLoaded(true);
        return element;
    }

    private boolean canContainRiskAnalysis(CnATreeElement element) {
        return (element instanceof IBSIStrukturElement);
    }

    private void freeBSIElement(CnATreeElement element) throws CommandException {
        if (element instanceof FinishedRiskAnalysis) {
            removeGefaehrdungen((FinishedRiskAnalysis) element);
            removeElement(element);
        } else {
            if (!element.isChildrenLoaded()) {
                element = loadChildren(element);
            }
            for (CnATreeElement child : element.getChildren()) {
                if (child instanceof FinishedRiskAnalysis) {
                    removeGefaehrdungen((FinishedRiskAnalysis) child);
                    removeElement(child);
                }
            }
        }
    }

    private void removeGefaehrdungen(FinishedRiskAnalysis analysis) throws CommandException {
        RetrieveInfo info = new RetrieveInfo().setParent(true).setChildren(true).setProperties(true);
        LoadElementByUuid<FinishedRiskAnalysis> command = new LoadElementByUuid<FinishedRiskAnalysis>(analysis.getUuid(), info);
        command = ServiceFactory.lookupCommandService().executeCommand(command);
        analysis = command.getElement();
        Set<CnATreeElement> children = analysis.getChildren();
        for (CnATreeElement child : children) {
            if (child instanceof GefaehrdungsUmsetzung) {
                GefaehrdungsUmsetzung gef = (GefaehrdungsUmsetzung) child;
                removeElement(gef);
            }
        }
    }

    private boolean determineConfiguration(CnATreeElement elmt) {
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
}
