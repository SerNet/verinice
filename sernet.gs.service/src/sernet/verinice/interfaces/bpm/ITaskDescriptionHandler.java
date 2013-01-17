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
package sernet.verinice.interfaces.bpm;

import java.util.Map;

import org.jbpm.api.task.Task;

/**
 * A ITaskDescriptionHandler loads a description of a jBPM task
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public interface ITaskDescriptionHandler {

    /**
     * @param task a jBPM task
     * @param varMap 
     * @return The description of a task
     */
    String loadDescription(String taskId, Map<String, Object> varMap);

    /**
     * @param task a jBPM task
     * @param varMap 
     * @return The title of a task
     */
    String loadTitle(String taskId, Map<String, Object> varMap);
    
}
