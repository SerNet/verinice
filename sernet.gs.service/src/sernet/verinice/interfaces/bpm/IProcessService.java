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
package sernet.verinice.interfaces.bpm;

import java.util.List;
import java.util.Map;

import org.jbpm.pvm.internal.model.ExecutionImpl;

import sernet.verinice.model.iso27k.Audit;
import sernet.verinice.model.iso27k.Control;
import sernet.verinice.model.samt.SamtTopic;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 *
 */
public interface IProcessService {

    /**
     * @param processDefinitionKey
     * @param variables
     */
    void startProcess(String processDefinitionKey, Map<java.lang.String,?> variables);
    
    /**
     * Starts a process for every Isa-Topic ({@link SamtTopic}) of an
     * Information Security Assessment (ISA) if no process exists for this topic before.
     * 
     * Parameter is an UUID of an audit, since internally an ISA is an {@link Audit}.
     * 
     * @param uuidAudit UUID of an {@link Audit} / ISA
     */
    IProcessStartInformation startProcessForIsa(final String uuidAudit);
    
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
     * @param uuidControl
     * @return
     */
    List<ExecutionImpl> findControlExecution(final String uuidControl);
    
    /**
     * @param uuidSamtTopic
     * @return
     */
    List<ExecutionImpl> findIsaExecution(final String uuidSamtTopic);
    
    /**
     * @param control
     */
    void handleControl(Control control);

    /**
     * Handles a SAMT / ISA topic and creates an process if necessary.
     * If a new process is created true is returned, if not false.
     * 
     * @param control
     * @return true if a new process is created
     */
    void handleSamtTopic(SamtTopic control);
    
    /**
     * Deletes a process including sub-processes with process id.
     * The process id is column id_ (not db-id) of table jbpm4_execution. 
     * 
     * @param id a process id
     */
    void deleteProcess(String id);
    
    /**
     * Returns true id this process is really active.
     * Used to determine if it is dummy implementation.
     * 
     * @return true id this process is really active
     */
    boolean isActive();
}

