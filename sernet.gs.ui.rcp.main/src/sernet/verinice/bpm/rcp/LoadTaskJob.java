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

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;

import sernet.gs.service.NotifyingThread;
import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.verinice.interfaces.bpm.ITask;
import sernet.verinice.interfaces.bpm.ITaskParameter;

final class LoadTaskJob extends NotifyingThread implements IRunnableWithProgress { 
    
    private static final Logger LOG = Logger.getLogger(LoadTaskJob.class);
    
    private ITaskParameter param;       
    private List<ITask> taskList;

    public LoadTaskJob(ITaskParameter param) {
        super();
        this.param = param;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.operation.IRunnableWithProgress#run(org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
        loadTasks();
    }
    
    /* (non-Javadoc)
     * @see sernet.gs.service.NotifyingThread#doRun()
     */
    @Override
    public void doRun() {
        loadTasks();      
    }
    
    public void loadTasks() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Loading tasks...");
        }
        Activator.inheritVeriniceContextState();
        taskList = ServiceFactory.lookupTaskService().getTaskList(param);
        Collections.sort(taskList);  
        if (LOG.isDebugEnabled()) {
            LOG.debug("Tasks loading finished.");
        }
    }
    
    public List<ITask> getTaskList() {
        return taskList;
    }
}