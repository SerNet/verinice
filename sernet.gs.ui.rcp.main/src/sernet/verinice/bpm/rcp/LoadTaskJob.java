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


import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import sernet.gs.service.NotifyingThread;
import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.hui.common.VeriniceContext;
import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.interfaces.bpm.ITask;
import sernet.verinice.interfaces.bpm.ITaskParameter;


/**
 * Task loader job for task view.
 * 
 * @see sernet.verinice.bpm.rcp.TaskView
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
final class LoadTaskJob extends NotifyingThread { 
    
    private static final Logger LOG = Logger.getLogger(LoadTaskJob.class);
    
    private ITaskParameter param;       
    private List<ITask> taskList;

    public LoadTaskJob() {
        super();
    }

    public LoadTaskJob(ITaskParameter param) {
        super();
        this.param = param;
    }
    
    /* (non-Javadoc)
     * @see sernet.gs.service.NotifyingThread#doRun()
     */
    @Override
    public void doRun() {
        loadTasks();      
    }
    
    public void loadTasks() {

        Activator.inheritVeriniceContextState();

        if (LOG.isDebugEnabled()) {
            LOG.debug("Loading tasks...");
        }


        if(listAllUserEnabled()){
            taskList = ServiceFactory.lookupTaskService().getTaskList(param);
        } else {
            taskList = ServiceFactory.lookupTaskService().getCurrentUserTaskList(param);
        }

        Collections.sort(taskList);  
        if (LOG.isDebugEnabled()) {
            LOG.debug("Tasks loading finished.");
        }
    }

	private boolean listAllUserEnabled() {
		return ServiceFactory.lookupRightsServiceClient().isEnabled(ActionRightIDs.TASKSHOWALL);
	}
    
    public ITaskParameter getParam() {
        return param;
    }

    public void setParam(ITaskParameter param) {
        this.param = param;
    }

    public List<ITask> getTaskList() {
        return taskList;
    }
}