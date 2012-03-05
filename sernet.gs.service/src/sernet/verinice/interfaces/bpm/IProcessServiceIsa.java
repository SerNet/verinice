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

import org.jbpm.pvm.internal.model.ExecutionImpl;

import sernet.verinice.model.iso27k.Audit;
import sernet.verinice.model.iso27k.Control;
import sernet.verinice.model.samt.SamtTopic;

/**
 * Interface to handle ISA processes.
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public interface IProcessServiceIsa extends IProcessServiceGeneric {    
    
    /**
     * Searches for existing process executions for an ISA / SAMT topic.
     * Returns an empty list if no process exists.
     * 
     * @param uuidIsaTopic UUID of a ISA topic
     * @return A list of process executions
     */
    List<ExecutionImpl> findIsaExecution(String uuidIsaTopic);
    
    /**
     * Starts a process for every ISA topic ({@link SamtTopic}) of an
     * Information Security Assessment (ISA) if no process exists for this topic before.
     * 
     * Parameter is an UUID of an audit, since internally an ISA is an {@link Audit}.
     * 
     * @param uuidAudit UUID of an {@link Audit} / ISA
     */
    IProcessStartInformation startProcessForIsa(String uuidAudit);
    
    /**
     * Handles a ISA / SAMT topic and creates a process for this topic if necessary.
     * If a new process is created true is returned, if not false.
     * 
     * @param control A ISA topic
     * @return True if a new process is created, false if not
     */
    void handleSamtTopic(SamtTopic control);
    
    /**
     * Searches for existing process executions for a control.
     * Returns an empty list if no process exists.
     * 
     * @param uuidControl UUID of a control
     * @return A list of process executions
     */
    List<ExecutionImpl> findControlExecution(String uuidControl);
    
    /**
     * Handles a control and creates a process for this control if necessary.
     * If a new process is created true is returned, if not false.
     * 
     * @param control A control
     * @return True if a new process is created, false if not
     */
    void handleControl(Control control); 
    
}

