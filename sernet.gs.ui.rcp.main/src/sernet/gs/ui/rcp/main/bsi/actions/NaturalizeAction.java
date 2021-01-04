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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PlatformUI;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.actions.RightsEnabledAction;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.service.commands.NaturalizeCommand;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 * 
 */
public class NaturalizeAction extends RightsEnabledAction implements ISelectionChangedListener {

    private static final Logger LOG = Logger.getLogger(NaturalizeAction.class);

    public static final String ID = "sernet.gs.ui.rcp.main.actions.NaturalizeAction"; //$NON-NLS-1$

    public static final String TYPE_ID = "naturalizeAction";

    private List<CnATreeElement> selectedElementList = new LinkedList<>();

    private ICommandService commandService;

    public NaturalizeAction(IViewSite site) {
        super(ActionRightIDs.NATURALIZE, Messages.NaturalizeAction_0);
        setId(ID);
        setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.NOALIENS));
        setToolTipText(Messages.NaturalizeAction_1);
        site.getSelectionProvider().addSelectionChangedListener(this);
    }

    /*
     * @see sernet.gs.ui.rcp.main.actions.RightsEnabledAction#doRun()
     */
    @Override
    public void doRun() {
        final int maxChangedElements = 10;
        try {
            PlatformUI.getWorkbench().getProgressService()
                    .busyCursorWhile(new IRunnableWithProgress() {
                        @Override
                        public void run(IProgressMonitor monitor)
                                throws InvocationTargetException, InterruptedException {
                            try {
                                Activator.inheritVeriniceContextState();
                                if (selectedElementList != null && !selectedElementList.isEmpty()) {
                                    Set<String> uuidSet = new HashSet<>(selectedElementList.size());
                                    Map<String, CnATreeElement> uuidElementMap = new HashMap<>(
                                            selectedElementList.size());

                                    for (CnATreeElement element : selectedElementList) {
                                        if (element != null && element.getSourceId() != null) {
                                            uuidSet.add(element.getUuid());
                                            uuidElementMap.put(element.getUuid(), element);
                                        }
                                    }
                                    NaturalizeCommand command = new NaturalizeCommand(uuidSet);
                                    command = getCommandService().executeCommand(command);
                                    List<CnATreeElement> changedElements = command
                                            .getChangedElements();

                                    if (changedElements != null) {
                                        if (changedElements.size() < maxChangedElements) {
                                            for (CnATreeElement elementFromServer : changedElements) {
                                                CnATreeElement element = uuidElementMap
                                                        .get(elementFromServer.getUuid());
                                                element.setSourceId(
                                                        elementFromServer.getSourceId());
                                                element.setExtId(elementFromServer.getExtId());
                                                CnAElementFactory.getModel(element)
                                                        .childChanged(element);
                                            }
                                        } else {
                                            CnAElementFactory.getInstance()
                                                    .reloadAllModelsFromDatabase();
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

    @Override
    public void selectionChanged(SelectionChangedEvent event) {
        boolean enabled = false;
        selectedElementList.clear();
        for (Iterator<?> iterator = event.getStructuredSelection().iterator(); iterator
                .hasNext();) {
            Object o = iterator.next();
            if (o instanceof CnATreeElement
                    && CnAElementHome.getInstance().isWriteAllowed((CnATreeElement) o)) {
                selectedElementList.add((CnATreeElement) o);
                enabled = true;
            } else {
                selectedElementList.clear();
                enabled = false;
                break;
            }
        }
        if (enabled && checkRights()) {
            this.setEnabled(enabled);
        }
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