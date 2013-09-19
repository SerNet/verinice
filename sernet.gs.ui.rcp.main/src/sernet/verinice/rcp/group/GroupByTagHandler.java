/*******************************************************************************
 * Copyright (c) 2012 Daniel Murygin.
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
package sernet.verinice.rcp.group;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.progress.IProgressService;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.rcp.IllegalSelectionException;
import sernet.verinice.rcp.RightsEnabledHandler;
import sernet.verinice.service.commands.GroupByTags;
import sernet.verinice.service.commands.LoadTagsOfGroupElements;

/**
 * This handler groups child elements of a group.
 * 
 * If a child has a tag a group with the same name as the tag is created
 * and the element is moved to this group.
 * 
 * If there is already a group with the name of tag this group is used.
 * 
 * If a child has multiple tags the first tag is used after sorting the tags
 * lexicographically.
 * 
 * This handler is activated for all ISO-27000 groups configured in plugin.xml.
 * 
 * @see GroupByTags
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class GroupByTagHandler extends RightsEnabledHandler  {

    private static final Logger LOG = Logger.getLogger(GroupByTagHandler.class);

    private CnATreeElement group;
    
    private Set<String> allTags;
    
    public GroupByTagHandler() {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
     */
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Group by tags called..."); //$NON-NLS-1$
        }
        try {
            List<CnATreeElement> selectedElements = getSelectedElements(event);
            if(selectedElements==null || selectedElements.size()!=1) {
                return null;
            }
            group = selectedElements.get(0);
            
            allTags = loadTagSet(group);
            if(allTags==null || allTags.isEmpty()) {
                MessageDialog.openInformation(Display.getCurrent().getActiveShell(), Messages.GroupByTagHandler_1, Messages.GroupByTagHandler_2);
                return null;
            }
            
            GroupByTagDialog dialog = openGroupByTagDialog();
            if( dialog.open() == Dialog.OK ) {
                Set<String> tags = dialog.getTagsSelected();
                if(tags!=null && !tags.isEmpty()) {
                    groupByTags(tags);
                }           
            }
        } catch (Exception e) {
            LOG.error("Error grouping elements.", e); //$NON-NLS-1$
            MessageDialog.openError(HandlerUtil.getActiveShell(event), Messages.GroupByTagHandler_3, Messages.GroupByTagHandler_4);
        }
        return null;
    }
    
    /**
     * @param group A group with ISO 27000 elements
     * @return Sorted set with all tags of the children of the group
     * @throws InterruptedException 
     * @throws InvocationTargetException 
     */
    private Set<String> loadTagSet(CnATreeElement group) throws InvocationTargetException, InterruptedException  {
        if(group==null) {
            return Collections.emptySet();
        }
        LoadTagsOperation operation = new LoadTagsOperation();
        IProgressService progressService = PlatformUI.getWorkbench().getProgressService();
        progressService.busyCursorWhile(operation);
        return operation.getTagSet();    
     }

    private void groupByTags(Set<String> tags) throws InvocationTargetException, InterruptedException {
        GroupByTagsOperation operation = new GroupByTagsOperation(group,tags);
        IProgressService progressService = PlatformUI.getWorkbench().getProgressService();
        progressService.busyCursorWhile(operation);
    }

    private GroupByTagDialog openGroupByTagDialog() {      
        return new GroupByTagDialog(Display.getCurrent().getActiveShell(), allTags);             
    }
    
    /**
     * Returns a list with selected tree elements from an event. If there is an
     * element selected which is not a CnATreeElement a
     * {@link IllegalSelectionException} is thrown.
     * 
     * @param event A ExecutionEvent
     * @return A list with selected {@link CnATreeElement}s
     * @throws IllegalSelectionException
     */
    @SuppressWarnings("unchecked")
    private List<CnATreeElement> getSelectedElements(ExecutionEvent event) {
        List<CnATreeElement> elements = Collections.emptyList();
        ISelection selection = HandlerUtil.getCurrentSelection(event);
        if (selection instanceof StructuredSelection) {
            IStructuredSelection structuredSelection = (IStructuredSelection) selection;
            try {
                elements = structuredSelection.toList();
            } catch (ClassCastException e) {
                LOG.warn("One of the selected object is not a CnATreeElement. Will not return any selected object."); //$NON-NLS-1$
                if (LOG.isDebugEnabled()) {
                    LOG.debug("stackstrace: ", e); //$NON-NLS-1$
                }
                throw new IllegalSelectionException("Wrong object selected."); //$NON-NLS-1$
            }
        }
        return elements;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.RightEnabledUserInteraction#getRightID()
     */
    @Override
    public String getRightID() {
        return ActionRightIDs.GROUP_BY_TAG;
    }

    
    class LoadTagsOperation implements IRunnableWithProgress {

        private Set<String> tagSet;
        
        /* (non-Javadoc)
         * @see org.eclipse.jface.operation.IRunnableWithProgress#run(org.eclipse.core.runtime.IProgressMonitor)
         */
        @Override
        public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
            monitor.beginTask(Messages.GroupByTagHandler_0, IProgressMonitor.UNKNOWN);
            Activator.inheritVeriniceContextState();
            LoadTagsOfGroupElements command = new LoadTagsOfGroupElements(group.getUuid());
            try {
                command = ServiceFactory.lookupCommandService().executeCommand(command);
                tagSet = command.getTagSet();
            } catch (CommandException ex) {
                LOG.error("Error while loading tags", ex); //$NON-NLS-1$
                MessageDialog.openInformation(Display.getCurrent().getActiveShell(), Messages.GroupByTagHandler_9, Messages.GroupByTagHandler_10);               
            } finally {
                if(monitor!=null) {
                    monitor.done();
                }
            }
        }

        public Set<String> getTagSet() {
            return tagSet;
        }
        
    }
    
    static class GroupByTagsOperation implements IRunnableWithProgress {

        private CnATreeElement group;
        private Set<String> tags;
        

        public GroupByTagsOperation(CnATreeElement group, Set<String> tags) {
            super();
            this.group = group;
            this.tags = tags;
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.operation.IRunnableWithProgress#run(org.eclipse.core.runtime.IProgressMonitor)
         */
        @Override
        public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
            monitor.beginTask(Messages.GroupByTagHandler_8, IProgressMonitor.UNKNOWN);
            Activator.inheritVeriniceContextState();
            GroupByTags command = new GroupByTags(group.getUuid(), tags);
            try {
                ServiceFactory.lookupCommandService().executeCommand(command);
                CnAElementFactory.getInstance().reloadModelFromDatabase();
            } catch (CommandException ex) {
                LOG.error("Error while grouping by tags", ex); //$NON-NLS-1$
                MessageDialog.openInformation(Display.getCurrent().getActiveShell(), Messages.GroupByTagHandler_9, Messages.GroupByTagHandler_10);               
            } finally {
                if(monitor!=null) {
                    monitor.done();
                }
            }
        }
        
    }
}
