/*******************************************************************************
 * Copyright (c) 2016 Viktor Schmidt.
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
 *     Viktor Schmidt <vschmidt[at]ckc[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.web;

import java.util.Hashtable;
import java.util.Map;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;

import org.apache.log4j.Logger;
import org.primefaces.context.RequestContext;

import sernet.gs.web.Util;
import sernet.hui.common.VeriniceContext;
import sernet.verinice.interfaces.bpm.IIndividualProcess;
import sernet.verinice.interfaces.bpm.ITask;
import sernet.verinice.interfaces.bpm.ITaskService;

/**
 * @author Viktor Schmidt <vschmidt[at]ckc[dot]de>
 */
@ManagedBean(name = IndiRejectRealizationBean.NAME)
@SessionScoped
public class IndiRejectRealizationBean implements ICompleteWebHandler {

    private static final Logger LOG = Logger.getLogger(IndiRejectRealizationBean.class);

    public static final String NAME = "indiRejectRealizationBean";

    private String description;

    private ITask task;

    private String outcomeId;
    
    @ManagedProperty(value = "#{task}")
    private TaskBean taskBean;

    /*
     * (non-Javadoc)
     * 
     * @see sernet.verinice.web.ICompleteWebHandler#execute(sernet.verinice.
     * interfaces.bpm.ITask)
     */
    @Override
    public void execute(ITask selectedTask, String outcomeId) {
        this.task = selectedTask;
        this.outcomeId = outcomeId;
        String oldDescription = (String) getTaskService().getVariables(task.getId()).get(IIndividualProcess.VAR_DESCRIPTION);
        this.description = oldDescription;
        RequestContext context = RequestContext.getCurrentInstance();
        context.execute("PF('indiRejectRealizationBean').show();");
    }

    public void complete() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("completeTask() called ..."); //$NON-NLS-1$
        }
        if (this.task != null) {
            getTaskService().completeTask(task.getId(), outcomeId, getParameter());
            RequestContext context = RequestContext.getCurrentInstance();
            context.execute("PF('indiRejectRealizationBean').hide();");
            Util.addInfo("complete", Util.getMessage(TaskBean.BOUNDLE_NAME, "taskCompleted")); //$NON-NLS-1$ //$NON-NLS-2$

            if (taskBean != null) {
                taskBean.resetSelectedTask();
            }
        }
    }

    private Map<String, Object> getParameter() {
        Map<String, Object> parameter = new Hashtable<String, Object>();
        parameter.put(IIndividualProcess.VAR_DESCRIPTION, getDescription());
        parameter.put(IIndividualProcess.TRANS_DECLINE, Boolean.TRUE);
        return parameter;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    private ITaskService getTaskService() {
        return (ITaskService) VeriniceContext.get(VeriniceContext.TASK_SERVICE);
    }

    public TaskBean getTaskBean() {
        return taskBean;
    }

    public void setTaskBean(TaskBean taskBean) {
        this.taskBean = taskBean;
    }
}
