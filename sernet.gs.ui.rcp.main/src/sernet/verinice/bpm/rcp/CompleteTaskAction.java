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
package sernet.verinice.bpm.rcp;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;

import sernet.gs.service.IThreadCompleteListener;
import sernet.hui.common.VeriniceContext;
import sernet.verinice.interfaces.bpm.IIndividualProcess;
import sernet.verinice.interfaces.bpm.ITaskService;
import sernet.verinice.model.bpm.TaskInformation;

/**
 * GUI action which completes task. Tasks are completed concurrently by
 * {@link ExecutorService}.
 * 
 * Instances of this action are created on demand in {@link TaskView} after the
 * user selects a task in the view.
 * 
 * To complete a task this action creates a {@link CompleteTaskJob} and executes
 * it by {@link ExecutorService}.
 * 
 * @see TaskView
 * @see CompleteTaskJob
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
final class CompleteTaskAction extends Action {

    private static final Logger LOG = Logger.getLogger(CompleteTaskAction.class);

    private final TaskView taskView;
    final String id = TaskView.class.getName() + ".complete"; //$NON-NLS-1$
    String outcomeId;

    private ExecutorService executer;

    public CompleteTaskAction(TaskView taskView, String outcomeId) {
        super();
        this.taskView = taskView;
        this.outcomeId = outcomeId;
        setId(id + "." + outcomeId); //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    public void run() {
        try {
            executer = Executors.newFixedThreadPool(2);
            List<TaskInformation> taskList = taskView.getSelectedTasks();
            for (TaskInformation task : taskList) {
                if (IIndividualProcess.TRANS_ACCEPT.equals(outcomeId) && task.isWithAReleaseProcess()) {
                    getTaskService().saveChangedElementPropertiesToCnATreeElement(task.getId(), task.getUuid());
                    taskView.closeEditorForElement(task.getUuid());
                }
                completeTask(task, outcomeId);
            }
            this.setEnabled(false);
            executer.shutdown();
        } catch (Exception t) {
            LOG.error("Error while completing tasks.", t); //$NON-NLS-1$
            shutdownAndAwaitTermination();
            this.taskView.showError(Messages.CompleteTaskAction_6, Messages.CompleteTaskAction_7);
        }
    }

    protected void completeTask(final TaskInformation task, String outcomeId) {
        CompleteTaskJob job = new CompleteTaskJob(task, outcomeId, taskView.getSite().getShell());
        job.addListener(new IThreadCompleteListener() {
            @Override
            public void notifyOfThreadComplete(Thread thread) {
                taskView.removeTask(task);
            }
        });
        executer.execute(job);
    }

    private void shutdownAndAwaitTermination() {
        executer.shutdown(); // Disable new tasks from being submitted
        try {
            // Wait a while for existing tasks to terminate
            if (!executer.awaitTermination(10, TimeUnit.SECONDS)) {
                executer.shutdownNow(); // Cancel currently executing tasks
                // Wait a while for tasks to respond to being cancelled
                if (!executer.awaitTermination(10, TimeUnit.SECONDS)) {
                    LOG.error("Task loader (ExecutorService) shutdown failed."); //$NON-NLS-1$
                }
            }
        } catch (InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            executer.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }

    private ITaskService getTaskService() {
        return (ITaskService) VeriniceContext.get(VeriniceContext.TASK_SERVICE);
    }
}