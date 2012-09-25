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

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 *
 */
public interface IIsaExecutionProcess extends IGenericProcess {
    
    String KEY = "isa-execution";
    
    String VAR_WRITE_PERMISSION = "ISA_WRITE_PERMISSION";
    
    String TRANSITION_IS_WRITE_PERMISSION = "is write permission";
    String TRANSITION_NO_WRITE_PERMISSION = "no write permission";
    
    String DEFAULT_OWNER_NAME = "admin";
    
    // see https://docs.jboss.org/jbpm/v4/devguide/html_single/#timer 
    String DEFAULT_DUEDATE = "10 business days";

    String TASK_SET_ASSIGNEE = "isa.task.setAssignee";
    
    String TASK_CHECK_IMPLEMENTATION = "isa.task.checkImplementation";

    String TASK_ESCALATE = "isa.task.escalate";
    
    String TASK_IMPLEMENT = "isa.task.implement";
    
    String TASK_WRITE_PERMISSION = "isa.task.setWritePermission";

    String TRANS_COMPLETE = "isa.transition.complete";

    String TRANS_ACCEPT = "isa.transition.accepted";

    String TRANS_ESCALATE = "isa.transition.escalate";


   

    
}
