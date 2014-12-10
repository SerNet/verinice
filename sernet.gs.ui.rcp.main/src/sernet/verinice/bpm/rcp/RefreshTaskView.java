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

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.TableViewer;

import sernet.verinice.interfaces.bpm.ITask;

/**
 * This class refresh the task table in task view after task loading job is done.
 *
 * @see sernet.verinice.bpm.rcp.TaskView
 * @see sernet.verinice.bpm.rcp.LoadTaskJob
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class RefreshTaskView {
    
    private static final Logger LOG = Logger.getLogger(RefreshTaskView.class);
    
    List<ITask> taskList;
    
    TableViewer viewer;
    
    /**
     * @param taskList
     * @param viewer
     */
    public RefreshTaskView(List<ITask> taskList, TableViewer viewer) {
        super();
        this.taskList = taskList;
        this.viewer = viewer;
    }

    public void refresh() {
        // Get the content for the viewer, setInput will call getElements in the
        // contentProvider
        try {
            viewer.setInput(taskList);                     
        } catch (Exception t) {
            LOG.error("Error while setting table data", t); //$NON-NLS-1$
        }
    }
}