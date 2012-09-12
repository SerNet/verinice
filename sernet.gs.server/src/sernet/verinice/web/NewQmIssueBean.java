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
package sernet.verinice.web;

import java.util.Hashtable;
import java.util.Map;

import  javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

import org.apache.log4j.Logger;
import org.primefaces.context.RequestContext;

import sernet.gs.web.Util;
import sernet.hui.common.VeriniceContext;
import sernet.verinice.interfaces.bpm.IIsaQmProcess;
import sernet.verinice.interfaces.bpm.ITask;
import sernet.verinice.interfaces.bpm.ITaskService;
/**
 *
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
@ManagedBean(name=NewQmIssueBean.NAME)
@SessionScoped
public class NewQmIssueBean implements ICompleteWebHandler {

    private static final Logger LOG = Logger.getLogger(NewQmIssueBean.class);
    
    public static final String NAME = "newQmBean";

    private String description;
    
    private String priority;
    
    private String dummy = "";
    
    private ITask task;
    
    private String outcomeId;
    
    
    /* (non-Javadoc)
     * @see sernet.verinice.web.ICompleteWebHandler#execute(sernet.verinice.interfaces.bpm.ITask)
     */
    @Override
    public void execute(ITask selectedTask, String outcomeId) {
       this.task = selectedTask;
       this.outcomeId = outcomeId;
       priority = ITask.PRIO_NORMAL;
       RequestContext context = RequestContext.getCurrentInstance(); 
       context.execute("newQmBean.show();");  
    }
    
    public void complete() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("completeTask() called ..."); //$NON-NLS-1$
        }
        if(this.task !=null) {        
            getTaskService().completeTask(task.getId(),outcomeId,getParameter());
            RequestContext context = RequestContext.getCurrentInstance(); 
            context.execute("newQmBean.hide();");
            Util.addInfo("complete", Util.getMessage(TaskBean.BOUNDLE_NAME, "taskCompleted"));   //$NON-NLS-1$ //$NON-NLS-2$        
         }
    }
    
    private Map<String, Object> getParameter() {
        Map<String, Object> parameter = new Hashtable<String, Object>();
        parameter.put(IIsaQmProcess.VAR_FEEDBACK, getDescription());
        parameter.put(IIsaQmProcess.VAR_QM_PRIORITY, getPriority());      
        return parameter;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getDummy() {
        return dummy;
    }

    public void setDummy(String dummy) {
        this.dummy = dummy;
    }
    
    private ITaskService getTaskService() {
        return (ITaskService) VeriniceContext.get(VeriniceContext.TASK_SERVICE);
    }
    
}
