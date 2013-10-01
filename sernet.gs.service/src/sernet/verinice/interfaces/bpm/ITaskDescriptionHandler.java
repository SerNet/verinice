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

/**
 * Instances of ITaskDescriptionHandler loads titles and descriptions of jBPM tasks.
 * Task-Description-Handlers are configured in veriniceserver-jbpm.xml.
 * 
 * Task-Description-Handlers are an optional configuration feature. If no
 * Task-Description-Handler is configured for a task 
 * sernet.verinice.bpm.DefaultTaskDescriptionHandler is used to load title and description.
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public interface ITaskDescriptionHandler {

    /**
     * @param taskId a jBPM task-id
     * @param processVars jBPM process variables
     * @return The description of a task
     */
    String loadDescription(String taskId, Map<String, Object> processVars);

    /**
     * @param taskId a jBPM task-id
     * @param processVars jBPM process variables
     * @return The title of a task
     */
    String loadTitle(String taskId, Map<String, Object> processVars);
    
}
