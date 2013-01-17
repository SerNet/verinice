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

import sernet.verinice.model.common.CnATreeElement;

/**
 * Interface to handle jBPM processes.
 * jBPM is an open source workflow engine: http://www.jboss.org/jbpm/
 * 
 * This interface defines generic methods with no dependencies to verinice classes like
 * {@link CnATreeElement}.
 * 
 * Extend this interface when you create a new process service for verinice. 
 * See {@link IProcessServiceIsa} for an example.
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public interface IProcessServiceGeneric {
   
    /**
     * Returns the latest process definition id
     * for a process definition key if any.
     * 
     * If there is no process definition found null is returned
     * 
     * @param processDefinitionKey  a process definition key
     * @return latest process definition id or null
     */
    String findProcessDefinitionId(String processDefinitionKey);
    
    /**
     * @param processDefinitionKey
     * @param variables
     */
    void startProcess(String processDefinitionKey, Map<java.lang.String, ?> variables);

    /**
     * Deletes a process including sub-processes with process id.
     * The process id is column id_ (not db-id) of table jbpm4_execution. 
     * 
     * @param id a process id
     */
    void deleteProcess(String id);

    /**
     * Returns true id this process service is really active.
     * Used to determine if it is dummy implementation.
     * 
     * @return true id this process is really active
     */
    boolean isActive();
    
}