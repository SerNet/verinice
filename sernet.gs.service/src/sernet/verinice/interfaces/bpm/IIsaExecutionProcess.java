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
public interface IIsaExecutionProcess extends IExecutionProcess {
    
    String KEY = "isa-execution";

    String VAR_OWNER_NAME = "ISA_OWNER_NAME";
    String VAR_ASSIGNEE_NAME = "ISA_ASSIGNEE_NAME";
    String VAR_IMPLEMENTATION = "ISA_IMPLEMENTATION";
    String VAR_DUEDATE = "ISA_DUEDATE";
    String VAR_AUDIT_UUID = "UUID_AUDIT";
    String VAR_WRITE_PERMISSION = "ISA_WRITE_PERMISSION";
    
    String TRANSITION_IS_WRITE_PERMISSION = "is write permission";
    String TRANSITION_NO_WRITE_PERMISSION = "no write permission";
    String TRANSITION_NOT_IMPLEMENTED = "not implemented";
    String TRANSITION_IMPLEMENTED = "implemented";
    
    String DEFAULT_OWNER_NAME = "admin";
    
    // see https://docs.jboss.org/jbpm/v4/devguide/html_single/#timer 
    String DEFAULT_DUEDATE = "10 business days";

   

    
}
