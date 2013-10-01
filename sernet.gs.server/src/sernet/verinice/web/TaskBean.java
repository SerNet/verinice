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
import java.util.Arrays;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.apache.log4j.Logger;

import sernet.gs.service.RetrieveInfo;
import sernet.gs.service.TimeFormatter;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.web.Util;
import sernet.hui.common.VeriniceContext;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.bpm.ITask;
import sernet.verinice.interfaces.bpm.ITaskParameter;
import sernet.verinice.interfaces.bpm.ITaskService;
import sernet.verinice.model.bpm.TaskParameter;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Audit;
import sernet.verinice.model.samt.SamtTopic;
import sernet.verinice.service.commands.LoadElementByUuid;
/**
 * JSF managed bean for view and edit Tasks, template: todo/task.xhtml
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class TaskBean {

    private static final Logger LOG = Logger.getLogger(TaskBean.class);
    
    public static final String BOUNDLE_NAME = "sernet.verinice.web.TaskMessages"; //$NON-NLS-1$
    
    public static final int MAX_TITLE_LENGTH = 100;

    private EditBean editBean;
    
    private List<CnATreeElement> auditList;
    
    private CnATreeElement selectedAudit;
    
    private String selectedAuditName;
    
    private Map<String, CnATreeElement> nameAuditMap;
    
    private List<ITask> taskList;
    
    private ITask selectedTask;
    
    private String outcomeId;
    
    private boolean showRead = true;
    
    private boolean showUnread = true;
    
    
    public List<ITask> loadTasks() {  
        long start = 0;
        if (LOG.isDebugEnabled()) {
            start = System.currentTimeMillis();
            LOG.debug("loadTasks() called ..."); //$NON-NLS-1$
        }
        
        ITaskParameter parameter = new TaskParameter();
        parameter.setRead(getShowRead());
        parameter.setUnread(getShowUnread());  
        if(selectedAudit!=null) {
            parameter.setAuditUuid(selectedAudit.getUuid());
        }
        taskList = getTaskService().getTaskList(parameter);
        Collections.sort(taskList);
        for (ITask task : taskList) {
            String controlTitle = task.getControlTitle();
            if(controlTitle!=null && controlTitle.length()>MAX_TITLE_LENGTH) {
                task.setControlTitle(controlTitle.substring(0, MAX_TITLE_LENGTH-1) + "...");
            }
        }
        if (LOG.isDebugEnabled()) {
            long duration = System.currentTimeMillis() - start;
            LOG.debug("loadTasks() finished in: " + TimeFormatter.getHumanRedableTime(duration)); //$NON-NLS-1$
        }
        return taskList;
    }
    
    public void openTask() {
        long start = 0;
    	if (LOG.isDebugEnabled()) {
    	    start = System.currentTimeMillis();
            LOG.debug("openTask() called ..."); //$NON-NLS-1$
        }
    	doOpenTask();
    	if (LOG.isDebugEnabled()) {
            long duration = System.currentTimeMillis() - start;
            LOG.debug("openTask() finished in: " + TimeFormatter.getHumanRedableTime(duration)); //$NON-NLS-1$
        }
    }
    
    private void doOpenTask() {  
        try {         
            getTaskService().markAsRead(getSelectedTask().getId());
            getSelectedTask().setIsRead(true);
            getSelectedTask().addStyle(ITask.STYLE_READ);
            
            getEditBean().setSaveMessage(Util.getMessage(TaskBean.BOUNDLE_NAME, "elementSaved"));
            getEditBean().setVisibleTags(Arrays.asList(EditBean.TAG_WEB));
            if(getSelectedTask().getProperties()!=null) {
                getEditBean().setVisiblePropertyIds(getSelectedTask().getProperties());
            }
            getEditBean().setSaveButtonHidden(false);
            getEditBean().setUuid(getSelectedTask().getUuid());
            String title = getSelectedTask().getControlTitle();
            if(title.length()>MAX_TITLE_LENGTH) {
                title = title.substring(0, MAX_TITLE_LENGTH-1) + "...";
            }
            getEditBean().setTitle(title);
            getEditBean().setTypeId(getSelectedTask().getElementType());
            getEditBean().addNoLabelType(SamtTopic.PROP_DESC);
            setOutcomeId(null);
            getEditBean().init();
            getEditBean().clearActionHandler();
            getEditBean().addActionHandler(new SaveAndNextHandler());
            getEditBean().addActionHandler(new OpenNextHandler());
            
            getLinkBean().setSelectedLink(null);
            getLinkBean().setSelectedLinkTargetName(null);
            getLinkBean().setSelectedLinkType(null);
        } catch (Exception t) {
            LOG.error("Error while opening task", t); //$NON-NLS-1$
        }
    }
    
    public void saveAndOpenNext() {
        getEditBean().save();
        openNext();
    }
    
    public void openNext() {
    	boolean isNext = false;
    	for (Iterator<ITask> iterator = getTaskList().iterator(); iterator.hasNext();) {
    		ITask task = iterator.next();
    		if(task!=null && getSelectedTask()!=null && task.equals(getSelectedTask()) && iterator.hasNext()) {
    		    setSelectedTask(iterator.next());
    		    isNext = true;
    		    break;
    		}
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
            ICompleteWebHandler handler = CompleteWebHandlerRegistry.getHandler(getSelectedTask().getType() + "." + getOutcomeId());
            if(handler!=null) {
                handler.execute(getSelectedTask(), getOutcomeId());
            } else {
                getTaskService().completeTask(getSelectedTask().getId(),getOutcomeId());
                getTaskList().remove(getSelectedTask());
                setSelectedTask(null);
                getEditBean().clear();
                Util.addInfo("complete", Util.getMessage(TaskBean.BOUNDLE_NAME, "taskCompleted"));   //$NON-NLS-1$ //$NON-NLS-2$
            }
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
    
    private Map<String, CnATreeElement> createNameAuditMap() {    
        List<CnATreeElement> audits = getAuditList();
        nameAuditMap = new Hashtable<String, CnATreeElement>(audits.size());
        for (CnATreeElement audit : audits) {
            if(audit!=null) {
                String name = getUniqueName(audit.getTitle(),0);
                nameAuditMap.put(name,audit);
            }
        }
        return nameAuditMap;
    }
    
    String getUniqueName(String name, int n) {
        int n0 = n;
        String name0 = name;
        if(nameAuditMap.containsKey(name0)) {
            n0++;
            name0 = new StringBuilder(name0).append(" (").append(n).append(")").toString(); //$NON-NLS-1$ //$NON-NLS-2$
            return getUniqueName(name0, n0);
        } else {
            return name0;
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

    public List<CnATreeElement> getAuditList() {
        if(auditList==null) {        
            try {
                loadAuditList();
            } catch (CommandException e) {
                LOG.error("Error while loading audit list.", e);
            }
        }
        return auditList;
    }

    public void setAuditList(List<CnATreeElement> auditList) {
        this.auditList = auditList;
    }
    
    private void loadAuditList() throws CommandException {
        List<String> uuidAuditList = getTaskService().getElementList();
        auditList = new ArrayList<CnATreeElement>(uuidAuditList.size());
        for (String uuid : uuidAuditList) {
            LoadElementByUuid<CnATreeElement> command = new LoadElementByUuid<CnATreeElement>(uuid, RetrieveInfo.getPropertyInstance());
            command = ServiceFactory.lookupCommandService().executeCommand(command);
            auditList.add(command.getElement());              
        }
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

    public CnATreeElement getSelectedAudit() {
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
        @Override
        public String getIcon() { return null; }
        @Override
        public void setIcon(String path) {}
        @Override
        public void addElementListeners(IElementListener elementListener) {}     
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
        @Override
        public String getIcon() { return null; }
        @Override
        public void setIcon(String path) {}
        @Override
        public void addElementListeners(IElementListener elementListener) {}
    }

}
