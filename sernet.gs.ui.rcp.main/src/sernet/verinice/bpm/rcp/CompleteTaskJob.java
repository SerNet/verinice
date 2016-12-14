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


import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.swt.widgets.Shell;

import sernet.gs.service.NotifyingThread;
import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.verinice.bpm.CompleteHandlerRegistry;
import sernet.verinice.bpm.ICompleteClientHandler;
import sernet.verinice.interfaces.bpm.ITaskService;
import sernet.verinice.model.bpm.TaskInformation;


/**
 * Job / thread to complete tasks in background.
 * Jobs are started in view "Tasks".
 * 
 * @see sernet.verinice.bpm.rcp.TaskView
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
final class CompleteTaskJob extends NotifyingThread { 
    
    private static final Logger LOG = Logger.getLogger(CompleteTaskJob.class);
   
    private TaskInformation task;
    private String outcomeId;
    private Shell shell;
    
    public CompleteTaskJob() {
        super();
    }

    public CompleteTaskJob(TaskInformation task, String outcomeId, Shell shell) {
        super();
        this.task = task;
        this.outcomeId = outcomeId;
        this.shell = shell;
    }


    /* (non-Javadoc)
     * @see sernet.gs.service.NotifyingThread#doRun()
     */
    @Override
    public void doRun() {
        completeTask();      
    }
    
    public void completeTask() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Completing task...");
        }
        Activator.inheritVeriniceContextState();
        String type = task.getType();
        ICompleteClientHandler handler = CompleteHandlerRegistry.getHandler(new StringBuilder(type).append(".").append(outcomeId).toString()); //$NON-NLS-1$
        Map<String, Object> parameter = null;
        if(handler!=null) {
            handler.setShell(getShell());
            parameter = handler.execute(task);          
        }
            
        if (outcomeId == null) {
            getTaskService().completeTask(task.getId());
        } else {
            getTaskService().completeTask(task.getId(), outcomeId, parameter);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Task completing finished.");
        }
    }
    
    private ITaskService getTaskService() {
        return ServiceFactory.lookupTaskService();
    }
    
    private Shell getShell() {
        return shell;
    }

}