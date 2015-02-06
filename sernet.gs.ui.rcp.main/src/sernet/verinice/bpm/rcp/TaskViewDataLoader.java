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

import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.eclipse.swt.widgets.Display;

import sernet.gs.service.IThreadCompleteListener;
import sernet.gs.service.RuntimeCommandException;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.IModelLoadListener;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.bpm.KeyMessage;
import sernet.verinice.model.bpm.TaskParameter;
import sernet.verinice.model.bsi.BSIModel;
import sernet.verinice.model.bsi.ITVerbund;
import sernet.verinice.model.common.configuration.Configuration;
import sernet.verinice.model.iso27k.Audit;
import sernet.verinice.model.iso27k.ISO27KModel;
import sernet.verinice.model.iso27k.Organization;
import sernet.verinice.rcp.account.AccountLoader;
import sernet.verinice.service.commands.LoadCnAElementByEntityTypeId;

/**
 * TaskViewDataLoader loads data for task view
 * 
 * @see TaskView
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class TaskViewDataLoader {
    
    private static final Logger LOG = Logger.getLogger(TaskViewDataLoader.class);
    
    private TaskView taskView;

    private IModelLoadListener modelLoadListener;
    private LoadTaskJob job;
    private ExecutorService executer = Executors.newFixedThreadPool(1);

    public TaskViewDataLoader(TaskView taskView) {
        super();
        this.taskView = taskView;
        job = new LoadTaskJob();
        IThreadCompleteListener listener = new RefreshListener(job);
        job.addListener(listener);
    }
    
    void initData() {
        if (CnAElementFactory.isModelLoaded()) {  
            loadGroups();         
            loadAssignees();
            loadProcessTypes();
            loadTaskTypes();
            loadTasks();
        } else if (modelLoadListener == null) {
            // model is not loaded yet: add a listener to load data when it's
            // laoded
            modelLoadListener = new IModelLoadListener() {
                @Override
                public void closed(BSIModel model) {
                    // nothing to do
                }

                @Override
                public void loaded(BSIModel model) {
                    Display.getDefault().syncExec(new Runnable() {
                        @Override
                        public void run() {
                            initData();
                        }
                    });
                }

                @Override
                public void loaded(ISO27KModel model) {
                    // work is done in loaded(BSIModel model)
                }
            };
            CnAElementFactory.getInstance().addLoadListener(modelLoadListener);
        }
    }
    
    public void loadTasks() {
        TaskParameter param = new TaskParameter();        
        param.setUsername(taskView.selectedAssignee);
        if(taskView.selectedAssignee!=null) {           
            param.setAllUser(false);
        } else {
            param.setAllUser(true);
        }       
        if(taskView.selectedGroup!=null) {
            param.setAuditUuid(taskView.selectedGroup.getUuid());
        }       
        if(taskView.selectedProcessType!=null) {
            param.setProcessKey(taskView.selectedProcessType.getKey());
        }      
        if(taskView.selectedTaskType!=null) {
            param.setTaskId(taskView.selectedTaskType.getKey());
        }
        if(taskView.dueDateFrom!=null) {
            param.setDueDateFrom(taskView.dueDateFrom);
        }
        if(taskView.dueDateTo!=null) {
            param.setDueDateTo(taskView.dueDateTo);
        }
        job.setParam(param);
        loadTasksInBackground(job);

        Display.getDefault().syncExec(new Runnable() {
            @Override
            public void run() {
                taskView.getInfoPanel().setText(""); //$NON-NLS-1$
            }
        });
    }
    
    private void loadTasksInBackground(final LoadTaskJob job) {
        taskView.searchButton.setText(Messages.TaskView_19);
        taskView.searchButton.setEnabled(false);
        executer.execute(job);
    }
    
    public void loadGroups()  {
        try {
            taskView.comboModelGroup.clear();
            LoadCnAElementByEntityTypeId command = new LoadCnAElementByEntityTypeId(Organization.TYPE_ID);
            command = taskView.getCommandService().executeCommand(command);
            taskView.comboModelGroup.addAll(command.getElements());
            command = new LoadCnAElementByEntityTypeId(ITVerbund.TYPE_ID_HIBERNATE);      
            command = taskView.getCommandService().executeCommand(command); 
            taskView.comboModelGroup.addAll(command.getElements());
            command = new LoadCnAElementByEntityTypeId(Audit.TYPE_ID);      
            command = taskView.getCommandService().executeCommand(command); 
            taskView.comboModelGroup.addAll(command.getElements());
            taskView.comboModelGroup.sort(TaskView.NSC);
            taskView.comboModelGroup.addNoSelectionObject(Messages.TaskView_21);
            TaskView.getDisplay().syncExec(new Runnable(){
                @Override
                public void run() {
                    taskView.comboGroup.setItems(taskView.comboModelGroup.getLabelArray());
                    selectDefaultGroup();               
                }  
            });
        } catch (CommandException e) {
            // exception is not logged here, but in createPartControl
            throw new RuntimeCommandException("Error while loading organizations, it-verbunds or audits", e); //$NON-NLS-1$
        }
    }
    
    private void selectDefaultGroup() {
        taskView.comboGroup.select(0);
        taskView.comboModelGroup.setSelectedIndex(taskView.comboGroup.getSelectionIndex());
        taskView.selectedGroup = taskView.comboModelGroup.getSelectedObject();
    }
    
    void loadAssignees() {
        taskView.comboModelAccount.clear();
        taskView.comboModelAccount.addAll(AccountLoader.loadAccounts());
        taskView.comboModelAccount.addNoSelectionObject(Messages.TaskView_20);
        TaskView.getDisplay().syncExec(new Runnable(){
            @Override
            public void run() {
                taskView.comboAccount.setItems(taskView.comboModelAccount.getLabelArray());
                selectDefaultAssignee(); 
            }
        });
    }
    
    private void selectDefaultAssignee() {
        String logedInUserName = ServiceFactory.lookupAuthService().getUsername();
        List<Configuration> allAccounts = taskView.comboModelAccount.getObjectList();
        for (Configuration account : allAccounts) {
            if(account!=null) {
                if(logedInUserName.equals(account.getUser())) {             
                    taskView.comboModelAccount.setSelectedObject(account);
                    taskView.comboAccount.select(taskView.comboModelAccount.getSelectedIndex());
                    taskView.selectedAssignee = account.getUser();
                }  
            }
        }       
    }
    
    void loadProcessTypes() {
        taskView.comboModelProcessType.clear();
        // you can use an arbitrary process service here
        Set<KeyMessage> processDefinitionSet =  ServiceFactory.lookupIndividualService().findAllProcessDefinitions();
        taskView.comboModelProcessType.addAll(processDefinitionSet);
        taskView.comboModelProcessType.sort(TaskView.NSC);
        taskView.comboModelProcessType.addNoSelectionObject(Messages.TaskView_23);
        TaskView.getDisplay().syncExec(new Runnable(){
            @Override
            public void run() {
                taskView.comboProcessType.setItems(taskView.comboModelProcessType.getLabelArray());    
                selectDefaultProcessType();
            }  
        });
    }
    
    private void selectDefaultProcessType() {
        taskView.comboProcessType.select(0);
        taskView.comboModelProcessType.setSelectedIndex(taskView.comboProcessType.getSelectionIndex());
        taskView.selectedProcessType = taskView.comboModelProcessType.getSelectedObject();
    }
    
    void loadTaskTypes() {
        TaskView.getDisplay().syncExec(new Runnable(){
            @Override
            public void run() {
                taskView.comboTaskType.setItems(taskView.comboModelTaskType.getLabelArray());      
                selectDefaultTaskType();
            }  
        });
    }
    
    private void selectDefaultTaskType() {
        taskView.comboTaskType.select(0);
        taskView.comboModelTaskType.setSelectedIndex(taskView.comboTaskType.getSelectionIndex());
        taskView.selectedTaskType = taskView.comboModelTaskType.getSelectedObject();
    }

    public void dispose() {
        CnAElementFactory.getInstance().removeLoadListener(modelLoadListener);
        job.removeAllListener();
        shutdownAndAwaitTermination(executer);
    }
    
    private void shutdownAndAwaitTermination(ExecutorService executer) {
        executer.shutdown(); // Disable new tasks from being submitted
        try {
            // Wait a while for existing tasks to terminate
            if (!executer.awaitTermination(30, TimeUnit.SECONDS)) {
                executer.shutdownNow(); // Cancel currently executing tasks
                // Wait a while for tasks to respond to being cancelled
                if (!executer.awaitTermination(30, TimeUnit.SECONDS)) {
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
    
    private final class RefreshListener implements IThreadCompleteListener {
        private final LoadTaskJob job;

        private RefreshListener(LoadTaskJob job) {
            this.job = job;
        }

        @Override
        public void notifyOfThreadComplete(Thread thread) {
            final RefreshTaskView refresh = new RefreshTaskView(job.getTaskList(), taskView.getViewer());
            Display.getDefault().asyncExec(new Runnable() {
                @Override
                public void run() {
                    refresh.refresh();
                    taskView.searchButton.setText(Messages.TaskView_29);
                    taskView.searchButton.setEnabled(true);
                }
            });
        }
    }

}
