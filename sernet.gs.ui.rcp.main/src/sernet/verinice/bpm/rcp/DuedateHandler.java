/*******************************************************************************
 * Copyright (c) 2014 Daniel Murygin.
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
package sernet.verinice.bpm.rcp;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import sernet.hui.common.VeriniceContext;
import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.interfaces.bpm.ITask;
import sernet.verinice.interfaces.bpm.ITaskService;
import sernet.verinice.rcp.DateSelectDialog;
import sernet.verinice.rcp.RightsEnabledHandler;


/**
 * Sets the duedate of one or more selected tasks in {@link TaskView}.
 * This handler is configured in plugin.xml
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class DuedateHandler extends RightsEnabledHandler {

    private static final Logger LOG = Logger.getLogger(DuedateHandler.class);
    
    Set<String> taskIdSet;
    Date oldDate;
    
    private Shell shell;
    
    /* (non-Javadoc)
     * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
     */
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        try {
            ISelection selection = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage().getSelection();
            if (selection != null && selection instanceof IStructuredSelection) {             
                    taskIdSet = getSelectedTasks(selection);
                    Date duedate = getDate();
                    getTaskService().setDuedate(taskIdSet, duedate);
                    TaskChangeRegistry.tasksAdded();                                                 
            }
        } catch(Exception e) {
            LOG.error("Error while assigning user to task.", e);
        }
        return null;
    }

    private Date getDate() {
        final DateSelectDialog typeDialog = new DateSelectDialog(shell,oldDate);
        if (typeDialog.open() == Window.OK) { 
            return typeDialog.getDate();
        } else {
            throw new CompletionAbortedException("Canceled by user.");
        }
    }

    private Set<String> getSelectedTasks(ISelection selection) {
        Set<String> taskIdSet = new HashSet<String>();
        for (Iterator iterator = ((IStructuredSelection)selection).iterator(); iterator.hasNext();) {
            ITask task = (ITask) iterator.next();
            taskIdSet.add(task.getId());
            oldDate = task.getDueDate();
        }
        return taskIdSet;
    }
    
    private ITaskService getTaskService() {
        return (ITaskService) VeriniceContext.get(VeriniceContext.TASK_SERVICE);
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.RightEnabledUserInteraction#getRightID()
     */
    @Override
    public String getRightID() {
        return ActionRightIDs.TASKCHANGEDUEDATE;
    }

}
