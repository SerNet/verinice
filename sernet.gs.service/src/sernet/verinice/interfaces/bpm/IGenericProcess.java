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
public interface IGenericProcess {

    String VAR_PROCESS_ID = "PROCESS_ID"; 
    String VAR_UUID = "UUID";    
    String VAR_TYPE_ID = "TYPE";    
    String VAR_OWNER_NAME = "ISA_OWNER_NAME";    
    String VAR_ASSIGNEE_NAME = "ISA_ASSIGNEE_NAME";
    String VAR_TASK_READ_STATUS = "TASK_READ_STATUS";
    String VAR_DUEDATE = "ISA_DUEDATE";   
    String VAR_AUDIT_UUID = "UUID_AUDIT";    
    String VAR_IMPLEMENTATION = "ISA_IMPLEMENTATION";    
    String VAR_PRIORITY = "PRIORITY";
    String VAR_PROPERTY_TYPES = "PROPERTY_TYPES";
    
    String TRANSITION_NOT_IMPLEMENTED = "not implemented";  
    String TRANSITION_IMPLEMENTED = "implemented";
    
    String TASK_UNREAD = "TASK_UNREAD";
}
