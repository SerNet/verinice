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
package sernet.verinice.web;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.apache.log4j.Logger;
import org.richfaces.component.html.HtmlExtendedDataTable;
import org.richfaces.model.selection.Selection;
import org.richfaces.model.selection.SimpleSelection;

import sernet.gs.web.Util;
import sernet.hui.common.VeriniceContext;
import sernet.verinice.interfaces.bpm.ITask;
import sernet.verinice.interfaces.bpm.ITaskParameter;
import sernet.verinice.interfaces.bpm.ITaskService;
import sernet.verinice.model.bpm.TaskParameter;
import sernet.verinice.model.iso27k.Audit;
import sernet.verinice.model.samt.SamtTopic;
/**
 * JSF managed bean for view and edit Tasks, template: todo/task.xhtml
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class TaskBean {

    private static final Logger LOG = Logger.getLogger(TaskBean.class);
    
    public static final String BOUNDLE_NAME = "sernet.verinice.web.TaskMessages"; //$NON-NLS-1$

    private EditBean editBean;
    
    List<Audit> auditList;
    
    Audit selectedAudit;
    
    String selectedAuditName;
    
    Map<String, Audit> nameAuditMap;
    
    List<ITask> taskList;
    
    ITask selectedTask;
    
    private String outcomeId;
    
    private boolean showRead = true;
    
    private boolean showUnread = true;
    
    private HtmlExtendedDataTable table;
    
    private Selection selection = new SimpleSelection();
    
    /**
     * @return
     */
    public List<ITask> loadTasks() {  
        if (LOG.isDebugEnabled()) {
            LOG.debug("loadTasks called..."); //$NON-NLS-1$
        }
        ITaskParameter parameter = new TaskParameter();
        parameter.setRead(getShowRead());
        parameter.setUnread(getShowUnread());  
        if(selectedAudit!=null) {
            parameter.setAuditUuid(selectedAudit.getUuid());
        }
        taskList = getTaskService().getTaskList(parameter);
        Collections.sort(taskList);
        if (LOG.isDebugEnabled()) {
            LOG.debug("loadTasks finished"); //$NON-NLS-1$
        }
        return taskList;
    }
    
    public void openTask() {
    	if (LOG.isDebugEnabled()) {
            LOG.debug("openTask() called ..."); //$NON-NLS-1$
        }
    	readSelectionFromTable();
    	doOpenTask();
    }
    
    private void readSelectionFromTable() {
    	Iterator<Object> iterator = getSelection().getKeys();
        while (iterator.hasNext()) {
            Object key = iterator.next();
            table.setRowKey(key);
            if (table.isRowAvailable()) {
                setSelectedTask( (ITask) table.getRowData());
            }
        }
    }
    
    private void doOpenTask() {  
        try {         
            getTaskService().markAsRead(getSelectedTask().getId());
            getSelectedTask().setIsRead(true);
            getSelectedTask().setStyle(ITask.STYLE_READ);
            
            getEditBean().setUuid(getSelectedTask().getUuid());
            getEditBean().setTitle(getSelectedTask().getControlTitle());
            getEditBean().setTypeId(getSelectedTask().getType());
            getEditBean().addNoLabelType(SamtTopic.PROP_DESC);
            setOutcomeId(null);
            getEditBean().init();
            getEditBean().clearActionHandler();
            getEditBean().addActionHandler(new SaveAndNextHandler());
            getEditBean().addActionHandler(new OpenNextHandler());
            
            getLinkBean().setSelectedLink(null);
            getLinkBean().setSelectedLinkTargetName(null);
            getLinkBean().setSelectedLinkType(null);
        } catch (Throwable t) {
            LOG.error("Error while opening task", t); //$NON-NLS-1$
        }
    }
    
    public void saveAndOpenNext() {
        getEditBean().save();
        openNext();
    }
    
    public void openNext() {
    	int i = 0;
    	boolean isNext = false;
    	for (Iterator<ITask> iterator = getTaskList().iterator(); iterator.hasNext();) {
    		ITask task = iterator.next();
    		if(task!=null && getSelectedTask()!=null && task.equals(getSelectedTask())) {
    		    if(iterator.hasNext()) {
    		        isNext = true;
        			setSelectedTask(iterator.next());
        			SimpleSelection selection = new SimpleSelection();
        			selection.addKey(Integer.valueOf(i+1));
        			getTable().setSelection(selection);
    		    }
    		}
    		i++;
		}
    	if(isNext) {
    	    doOpenTask();
    	}
    }
    
    public void completeTask() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("completeTask() called ..."); //$NON-NLS-1$
        }
        if(getSelectedTask()!=null) {
            getTaskService().completeTask(getSelectedTask().getId(),getOutcomeId());
            getTaskList().remove(getSelectedTask());
            setSelectedTask(null);
            setSelection(null);
            getEditBean().clear();
            Util.addInfo("complete", Util.getMessage(TaskBean.BOUNDLE_NAME, "taskCompleted"));   //$NON-NLS-1$ //$NON-NLS-2$
        }
    }
    
    public void completeAllTask() { 
        int n = 0;
        for (ITask task : getTaskList()) {        
            getTaskService().completeTask(task.getId());
            if (LOG.isDebugEnabled()) {
                LOG.debug("Task completed, id: " + task.getId()); //$NON-NLS-1$
            }
            n++;
        }
        getTaskList().clear();
        this.taskList = loadTasks();
        setSelectedTask(null);
        setSelection(null);
        getEditBean().clear();
        Util.addInfo("complete", Util.getMessage(TaskBean.BOUNDLE_NAME, "allTaskCompleted", new Object[]{Integer.valueOf(n)}));   //$NON-NLS-1$ //$NON-NLS-2$
    }
    
    public void selectAudit() {
        selectedAudit = nameAuditMap.get(selectedAuditName);
        loadTasks();
    }
    
    public List<String> getAuditNameList() {
        if(nameAuditMap==null) {
            nameAuditMap = createNameAuditMap();
        }
        return new ArrayList<String>(nameAuditMap.keySet());
    }
    
    private Map<String, Audit> createNameAuditMap() {    
        List<Audit> auditList = getAuditList();
        nameAuditMap = new Hashtable<String, Audit>(auditList.size());
        for (Audit audit : auditList) {
            if(audit!=null) {
                String name = getUniqueName(audit.getTitle(),0);
                nameAuditMap.put(name,audit);
            }
        }
        return nameAuditMap;
    }
    
    String getUniqueName(String name, int n) {
        if(nameAuditMap.containsKey(name)) {
            n++;
            name = new StringBuilder(name).append(" (").append(n).append(")").toString(); //$NON-NLS-1$ //$NON-NLS-2$
            return getUniqueName(name, n);
        } else {
            return name;
        }
    }
    
    public LinkBean getLinkBean() {
        return getEditBean().getLinkBean();
    }

    public EditBean getEditBean() {
        return editBean;
    }

    public void setEditBean(EditBean editBean) {
        this.editBean = editBean;
    }

    public List<Audit> getAuditList() {
        if(auditList==null) {
            auditList = getTaskService().getAuditList();
        }
        return auditList;
    }

    public void setAuditList(List<Audit> auditList) {
        this.auditList = auditList;
    }

    public List<ITask> getTaskList() {
        if(this.taskList==null) {
            this.taskList = loadTasks();
        }
        return taskList;
    }

    public void setTaskList(List<ITask> taskList) {     
        this.taskList = taskList;
    }

    public Audit getSelectedAudit() {
        return selectedAudit;
    }

    public void setSelectedAudit(Audit selectedAudit) {
        this.selectedAudit = selectedAudit;
    }

    public String getSelectedAuditName() {
        return selectedAuditName;
    }

    public void setSelectedAuditName(String selectedAuditName) {
        this.selectedAuditName = selectedAuditName;
    }

    public ITask getSelectedTask() {
        return selectedTask;
    }

    public void setSelectedTask(ITask selectedTask) {
        this.selectedTask = selectedTask;
    }

    public String getOutcomeId() {
        return outcomeId;
    }

    public void setOutcomeId(String outcomeId) {
        this.outcomeId = outcomeId;
    }

    public boolean getShowRead() {
        return showRead;
    }

    public void setShowRead(boolean showRead) {
        this.showRead = showRead;
    }

    public boolean getShowUnread() {
        return showUnread;
    }

    public void setShowUnread(boolean showUnread) {
        this.showUnread = showUnread;
    }

    public HtmlExtendedDataTable getTable() {
        return table;
    }

    public void setTable(HtmlExtendedDataTable table) {
        this.table = table;
    }

    public Selection getSelection() {
        return selection;
    }

    public void setSelection(Selection selection) {
        this.selection = selection;
    }

    public TimeZone getTimeZone() {
        return TimeZone.getDefault();
    }
    
    private ITaskService getTaskService() {
        return (ITaskService) VeriniceContext.get(VeriniceContext.TASK_SERVICE);
    }
    
    public void english() {
        Util.english();
    }
    
    public void german() {
        Util.german();
    }
    


    public class SaveAndNextHandler implements IActionHandler {
    
        @Override
        public void execute() {
            saveAndOpenNext();  
        }
    
        @Override
        public String getLabel() {
            return Messages.getString("TaskBean.8"); //$NON-NLS-1$
        }
    
        @Override
        public void setLabel(String label) {}      
    }
    
    public class OpenNextHandler implements IActionHandler {
        
        @Override
        public void execute() {
            openNext();  
        }
    
        @Override
        public String getLabel() {
            return Messages.getString("TaskBean.9"); //$NON-NLS-1$
        }
    
        @Override
        public void setLabel(String label) {}      
    }

}
