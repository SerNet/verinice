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

import java.util.LinkedList;
import java.util.List;

import sernet.verinice.interfaces.bpm.ITask;
import sernet.verinice.interfaces.bpm.ITaskListener;

/**
 *
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public abstract class TaskChangeRegistry {

    private static List<ITaskListener> taskChangeListeners;
   
    public static void addTaskChangeListener(ITaskListener listener) {
        getTaskChangeListeners().add(listener);
    }
    
    public static void removeTaskChangeListener(ITaskListener listener) {
        getTaskChangeListeners().remove(listener);
    }
    
    public static void tasksAdded() {
        for (ITaskListener listener : getTaskChangeListeners()) {
            listener.newTasks();
        }
    }
    
    public static void tasksAdded(List<ITask>  newTasks) {
        for (ITaskListener listener : getTaskChangeListeners()) {
            listener.newTasks(newTasks);
        }
    }
    
    private static List<ITaskListener> getTaskChangeListeners() {
        if(taskChangeListeners==null) {
            taskChangeListeners = new LinkedList<ITaskListener>();
        }
        return taskChangeListeners;     
    }
    
}
