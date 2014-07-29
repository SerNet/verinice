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

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;

import sernet.gs.ui.rcp.main.ImageCache;
import sernet.verinice.interfaces.bpm.ITaskParameter;
import sernet.verinice.model.bpm.TaskParameter;

/**
 * 
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class TaskFilterAction extends Action {

    private static final Logger LOG = Logger.getLogger(TaskFilterAction.class);

    private Shell shell;

    TreeViewer viewer;
    
    Action myTaskAction;
    
    boolean onlyMyTask;
    
    boolean onlyMyTaskEnabled;
    
    String processKey;
    
    String taskId;

    /**
     * 
     */
    public TaskFilterAction(Shell shell, TreeViewer viewer, Action myTaskAction) {
        super(Messages.TaskFilterAction_0, SWT.NONE);
        this.shell = shell;
        this.viewer = viewer;
        this.myTaskAction = myTaskAction;
        setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.FILTER));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    public void run() {
        TaskFilterDialog dialog = new TaskFilterDialog(this.shell,processKey,taskId, !myTaskAction.isChecked());
        dialog.setAllTasksEnabled(isOnlyMyTaskEnabled());
        if (dialog.open() == InputDialog.OK) {
            ITaskParameter parameter = new TaskParameter();
            parameter.setAllUser(dialog.isAllTasks());
            myTaskAction.setChecked(!dialog.isAllTasks());
            processKey = dialog.getProcessKey();
            parameter.setProcessKey(processKey);
            taskId = dialog.getTypeId();
            parameter.setTaskId(taskId);
            final LoadTaskJob job = new LoadTaskJob(parameter);
            final IProgressService progressService = PlatformUI.getWorkbench().getProgressService();
            Display.getDefault().syncExec(new Runnable() {
                public void run() {
                    try {
                        progressService.run(true, true, job);
                    } catch (Exception t) {
                        LOG.error("Error while loading tasks.", t); //$NON-NLS-1$
                    }
                }
            });

            RefreshTaskView refresh = new RefreshTaskView(job.getTaskList(), viewer);
            refresh.refresh();
        }
    }

    public String getProcessKey() {
        return processKey;
    }

    public String getTaskId() {
        return taskId;
    }

    public boolean isOnlyMyTaskEnabled() {
        return onlyMyTaskEnabled;
    }

    public void setOnlyMyTaskEnabled(boolean onlyMyTaskEnabled) {
        this.onlyMyTaskEnabled = onlyMyTaskEnabled;
    }

    
}
