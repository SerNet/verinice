/*******************************************************************************
 * Copyright (c) 2010 Daniel Murygin.
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
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.bsi.actions;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.iso27k.service.commands.NaturalizeCommand;
import sernet.verinice.model.common.CnATreeElement;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 * 
 */
public class NaturalizeAction extends Action implements ISelectionListener {

    private static final Logger LOG = Logger.getLogger(NaturalizeAction.class);
    
    public static final String ID = "sernet.gs.ui.rcp.main.actions.NaturalizeAction"; //$NON-NLS-1$

    List<CnATreeElement> selectedElementList = new LinkedList<CnATreeElement>();

    ICommandService commandService;

    public NaturalizeAction(IWorkbenchWindow window) {
        setText(Messages.NaturalizeAction_0);
        setId(ID);
        setActionDefinitionId(ID);
        setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.NOALIENS));
        setToolTipText(Messages.NaturalizeAction_1);
        window.getSelectionService().addSelectionListener(this);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.action.Action#run()
     */
    public void run() {
        try {
            PlatformUI.getWorkbench().getProgressService().busyCursorWhile(new IRunnableWithProgress() {
                public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    try {
                        Activator.inheritVeriniceContextState();
                        if(selectedElementList!=null && !selectedElementList.isEmpty()) {
                            Set<String> uuidSet = new HashSet<String>(selectedElementList.size());
                            for (CnATreeElement element : selectedElementList) {
                                if(element!=null && element.getSourceId()!=null) {
                                    uuidSet.add(element.getUuid()); 
                                }
                            }
                            NaturalizeCommand command = new NaturalizeCommand(uuidSet);
                            command = getCommandService().executeCommand(command);
                            List<CnATreeElement> changedElements = command.getChangedElements();
                                               
                            if(changedElements!=null) {
                                if(changedElements.size()<10) {
                                    for (CnATreeElement element : changedElements) {
                                        CnAElementFactory.getModel(element).childChanged(element.getParent(), element);
                                    }
                                } else {
                                    CnAElementFactory.getInstance().reloadModelFromDatabase();
                                }
                            }
                            
                        }
                    } catch (CommandException e) {
                        LOG.error("Error while naturalizing element", e);
                        throw new RuntimeException("Error while naturalizing element", e);
                    }
                }
            });
        } catch (Exception e) {
            LOG.error("Error while naturalizing element", e);
            ExceptionUtil.log(e, Messages.NaturalizeAction_2);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @seeorg.eclipse.ui.ISelectionListener#selectionChanged(org.eclipse.ui.
     * IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
     */
    @Override
    public void selectionChanged(IWorkbenchPart part, ISelection selection) {
        boolean enabled = false;
        selectedElementList.clear();
        for (Iterator iterator = ((IStructuredSelection) selection).iterator(); iterator.hasNext();) {
            Object o = iterator.next();
            if (o instanceof CnATreeElement) {
                selectedElementList.add((CnATreeElement) o);
                enabled = true;
            } else {
                selectedElementList.clear();
                enabled = false;
                break;
            }
        }
        this.setEnabled(enabled);
    }

    public ICommandService getCommandService() {
        if (commandService == null) {
            commandService = createCommandServive();
        }
        return commandService;
    }

    private ICommandService createCommandServive() {
        return ServiceFactory.lookupCommandService();
    }

}
