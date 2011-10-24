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
package sernet.verinice.bpm.rcp;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import sernet.verinice.interfaces.bpm.ITask;
import sernet.verinice.interfaces.bpm.KeyValue;

/**
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class TaskTreeModel {

    List<ITask> taskList;
    
    Map<KeyValue, List<ITask>> nodeMap;
    
    public TaskTreeModel(List<ITask> taskList) {
        super();
        this.taskList = taskList;
        init();
    } 

    private void init() {
        nodeMap = new HashMap<KeyValue, List<ITask>>();
        for (ITask task : taskList) {
            KeyValue keyValue = new KeyValue(task.getUuidAudit(), task.getAuditTitle());
            List<ITask> taskList = nodeMap.get(keyValue);
            if(taskList==null) {
                taskList = new LinkedList<ITask>();
                nodeMap.put(keyValue, taskList);
            }
            taskList.add(task);
        }
    }
    
    public Object[] getRootElementArray() {   
        Object[] result = getRootElementSet().toArray();
        Arrays.sort(result);
        return result;
    }
    
    public Set<KeyValue> getRootElementSet() {
        return nodeMap.keySet();
    }
    
    public List<ITask> getChildren(KeyValue parent) {
        return nodeMap.get(parent);
    }
    
    public Object[] getChildrenArray(KeyValue parent) {
        List<ITask> children = nodeMap.get(parent);
        Collections.sort(children);
        Object[] taskArray = null;
        if(children==null) {
            taskArray = new ITask[0];
        } else {
            taskArray = children.toArray();
        }
        return taskArray;
    }
}
