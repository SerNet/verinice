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

import org.eclipse.jface.viewers.TreeViewer;

import sernet.verinice.interfaces.bpm.ITask;

public class RefreshTaskView {
    
    List<ITask> taskList;
    
    TreeViewer viewer;
    
    /**
     * @param taskList
     * @param viewer
     */
    public RefreshTaskView(List<ITask> taskList, TreeViewer viewer) {
        super();
        this.taskList = taskList;
        this.viewer = viewer;
    }

    public void refresh() {
        // Get the content for the viewer, setInput will call getElements in the
        // contentProvider
        try {
            viewer.setInput(taskList);
            if(((TaskContentProvider)viewer.getContentProvider()).getNumberOfGroups()==1) {
                viewer.expandToLevel(2);
            }
            
        } catch (Exception t) {
            TaskView.LOG.error("Error while setting table data", t); //$NON-NLS-1$
        }
    }
}