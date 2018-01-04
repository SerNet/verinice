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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
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
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.interfaces.bpm.KeyMessage;
import sernet.verinice.model.bp.elements.BpModel;
import sernet.verinice.model.bpm.TaskParameter;
import sernet.verinice.model.bsi.BSIModel;
import sernet.verinice.model.bsi.ITVerbund;
import sernet.verinice.model.catalog.CatalogModel;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.configuration.Configuration;
import sernet.verinice.model.iso27k.Audit;
import sernet.verinice.model.iso27k.ISO27KModel;
import sernet.verinice.model.iso27k.Organization;
import sernet.verinice.service.commands.LoadCnAElementByEntityTypeId;
import sernet.verinice.service.commands.LoadVisibleAccounts;

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
    private ICommandService commandService;
    
    private List<CnATreeElement> auditList;
    private List<CnATreeElement> filteredAuditList;
    
    
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

                @Override
                public void loaded(BpModel model) {
                    // work is done in loaded(BSIModel model)
                }

                @Override
                public void loaded(CatalogModel model) {
                    // nothing to do
                }
            };
            CnAElementFactory.getInstance().addLoadListener(modelLoadListener);
        }
    }
    
    public void loadTasks() {
        TaskParameter param = new TaskParameter();        
        param.setUsername(taskView.selectedAssignee);
        if (taskView.selectedAssignee != null) {           
            param.setAllUser(false);
        } else {
            param.setAllUser(true);
        }       
        if (taskView.selectedScope != null) {
            param.setAuditUuid(taskView.selectedScope.getUuid());
        }  
        if (taskView.selectedAudit != null) {
            param.setAuditUuid(taskView.selectedAudit.getUuid());
        } else if (taskView.selectedScope != null 
                && filteredAuditList != null 
                && !filteredAuditList.isEmpty()) {
            param.setGroupIdList(getUuidList(filteredAuditList));
        }
        if (taskView.selectedProcessType != null) {
            param.setProcessKey(taskView.selectedProcessType.getKey());
        }      
        if (taskView.selectedTaskType != null) {
            param.setTaskId(taskView.selectedTaskType.getKey());
        }
        if (taskView.dueDateFrom != null) {
            param.setDueDateFrom(taskView.dueDateFrom);
        }
        if (taskView.dueDateTo != null) {
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
    

    private List<String> getUuidList(List<CnATreeElement> auditList) {
        List<String> uuidList = new ArrayList<String>(auditList.size());
        for (CnATreeElement audit : auditList) {
            uuidList.add(audit.getUuid());
        }
        return uuidList;
    }

    private void loadTasksInBackground(final LoadTaskJob job) {
        taskView.searchButton.setText(Messages.TaskView_19);
        taskView.searchButton.setEnabled(false);
        executer.execute(job);
    }
    
    public void loadGroups()  {
        try {
            taskView.comboModelScope.clear();
            LoadCnAElementByEntityTypeId command = 
                    new LoadCnAElementByEntityTypeId(Organization.TYPE_ID);
            command = taskView.getCommandService().executeCommand(command);
            taskView.comboModelScope.addAll(command.getElements());
            command = new LoadCnAElementByEntityTypeId(ITVerbund.TYPE_ID_HIBERNATE);      
            command = taskView.getCommandService().executeCommand(command); 
            taskView.comboModelScope.addAll(command.getElements());  
            taskView.comboModelScope.sort(TaskView.COMPARATOR_CNA_TREE_ELEMENT);
            taskView.comboModelScope.addNoSelectionObject(Messages.TaskView_21);
            TaskView.getDisplay().syncExec(new Runnable(){
                @Override
                public void run() {
                    taskView.comboScope.setItems(taskView.comboModelScope.getLabelArray());
                    selectDefaultGroup();               
                }  
            });
            loadAudits();
        } catch (CommandException e) {
            // exception is not logged here, but in createPartControl
            throw new RuntimeCommandException("Error while loading "
                    + "organizations, it-verbunds or audits", e); //$NON-NLS-1$
        }
    }
    
    public void loadAudits() {
        try {
            taskView.comboModelAudit.clear();
            LoadCnAElementByEntityTypeId command = 
                    new LoadCnAElementByEntityTypeId(Audit.TYPE_ID);      
            command = taskView.getCommandService().executeCommand(command); 
            auditList = command.getElements();    
            filteredAuditList = filterAudits();
            taskView.comboModelAudit.addAll(filteredAuditList);
            taskView.comboModelAudit.sort(TaskView.COMPARATOR_CNA_TREE_ELEMENT);
            if (filteredAuditList != null && !filteredAuditList.isEmpty()) {
                taskView.comboModelAudit.addNoSelectionObject(Messages.TaskView_21);
            } else {
                taskView.comboModelAudit.addNoSelectionObject(Messages.TaskViewDataLoader_0);
            }
            refreshAudits();
        } catch (CommandException e) {
            // exception is not logged here, but in createPartControl
            throw new RuntimeCommandException("Error while loading organizations,"
                    + " it-verbunds or audits", e); //$NON-NLS-1$
        }
    }

  
    private List<CnATreeElement> filterAudits() {
        List<CnATreeElement> filteredList = auditList;
        if (taskView.selectedScope != null) {
            filteredList = new LinkedList<CnATreeElement>();
            for (CnATreeElement audit : auditList) {
                if (taskView.selectedScope.getDbId().equals(audit.getScopeId())) {
                    filteredList.add(audit);
                }
            }
        } else {
            filteredList = auditList;
        }
        return filteredList;
    }

    private void refreshAudits() {     
        TaskView.getDisplay().syncExec(new Runnable(){
            @Override
            public void run() {
                taskView.comboAudit.setItems(taskView.comboModelAudit.getLabelArray());            
                selectDefaultAudit();
            }  
        });
    }
    
    public void refreshScopes() {     
        TaskView.getDisplay().syncExec(new Runnable(){
            @Override
            public void run() {
                taskView.comboScope.setItems(taskView.comboModelScope.getLabelArray());            
                selectDefaultGroup();
            }  
        });
    }
    
    private void selectDefaultGroup() {
        taskView.comboScope.select(0);
        taskView.comboModelScope.setSelectedIndex(taskView.comboScope.getSelectionIndex());
        taskView.selectedScope = taskView.comboModelScope.getSelectedObject();
    }
    
    private void selectDefaultAudit() {
        taskView.comboAudit.select(0);
        taskView.comboModelAudit.setSelectedIndex(taskView.comboAudit.getSelectionIndex());
        taskView.selectedAudit = taskView.comboModelAudit.getSelectedObject();
    }
    
    void loadAssignees() {
        taskView.comboModelAccount.clear();     
        taskView.comboModelAccount.addAll(loadAccounts());
        taskView.comboModelAccount.sort(TaskView.COMPARATOR_CONFIGURATION);
        taskView.comboModelAccount.addNoSelectionObject(Messages.TaskView_20);
        TaskView.getDisplay().syncExec(new Runnable(){
            @Override
            public void run() {
                taskView.comboAccount.setItems(taskView.comboModelAccount.
                        getLabelArray());
                selectDefaultAssignee(); 
            }
        });
    }
    

    private Collection<Configuration> loadAccounts() {
        try {
            if (taskView.isTaskShowAllEnabled()){
                LoadVisibleAccounts command = new LoadVisibleAccounts();     
                command = getCommandService().executeCommand(command);     
                return command.getAccountList();
            } else {
                String currentUserName = ServiceFactory.lookupAuthService().
                        getUsername();
                Configuration currentUserConfiguration =
                        ServiceFactory.lookupAccountService().
                        getAccountByName(currentUserName);
                return Arrays.asList(new Configuration[]{currentUserConfiguration});
            }
        } catch (CommandException e) {
            LOG.error("Error while loading accounts.", e); //$NON-NLS-1$
            throw new RuntimeException(e);
        }
    }

    private void selectDefaultAssignee() {
        String logedInUserName = ServiceFactory.lookupAuthService().getUsername();
        List<Configuration> allAccounts = taskView.comboModelAccount.getObjectList();
        for (Configuration account : allAccounts) {
            if (account != null && logedInUserName.equals(account.getUser())) {             
                taskView.comboModelAccount.setSelectedObject(account);
                taskView.comboAccount.select(taskView.comboModelAccount.getSelectedIndex());
                taskView.selectedAssignee = account.getUser();
            }  
        }       
    }
    
    void loadProcessTypes() {
        taskView.comboModelProcessType.clear();
        // you can use an arbitrary process service here
        Set<KeyMessage> processDefinitionSet =  ServiceFactory.
                lookupIndividualService().findAllProcessDefinitions();
        taskView.comboModelProcessType.addAll(processDefinitionSet);
        taskView.comboModelProcessType.sort(TaskView.COMPARATOR_KEY_MESSAGE);
        taskView.comboModelProcessType.addNoSelectionObject(Messages.TaskView_23);
        TaskView.getDisplay().syncExec(new Runnable(){
            @Override
            public void run() {
                taskView.comboProcessType.setItems(
                        taskView.comboModelProcessType.getLabelArray());    
                selectDefaultProcessType();
            }  
        });
    }
    
    private void selectDefaultProcessType() {
        taskView.comboProcessType.select(0);
        taskView.comboModelProcessType.setSelectedIndex(
                taskView.comboProcessType.getSelectionIndex());
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
    
    public ICommandService getCommandService() {
        if (commandService == null) {
            commandService = ServiceFactory.lookupCommandService();
        }
        return commandService;
    }
    
    private final class RefreshListener implements IThreadCompleteListener {
        private final LoadTaskJob job;

        private RefreshListener(LoadTaskJob job) {
            this.job = job;
        }

        @Override
        public void notifyOfThreadComplete(Thread thread) {
            final RefreshTaskView refresh = new RefreshTaskView(
                    job.getTaskList(), taskView.getViewer());
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
