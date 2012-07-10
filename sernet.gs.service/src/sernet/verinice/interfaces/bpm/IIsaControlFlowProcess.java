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

/**
 *
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public interface IIsaControlFlowProcess extends IGenericProcess {

    String KEY = "isa-control-flow";
    
    String TASK_ASSIGN = "TASK_ASSIGN";
    
    String TASK_EXECUTE = "TASK_EXECUTE";
    
    String TASK_AUDIT = "TASK_AUDIT";
    
    String DEADLINE_PASSED = "DEADLINE_PASSED";
    
    String NOT_RESPONSIBLE = "NOT_RESPONSIBLE";
    
    String VAR_DEADLINE_COMMENT = "ISA_DEADLINE_COMMENT";
    
    String VAR_ASSIGN_DUEDATE = "ISA_ASSIGN_DUEDATE";
    
    String VAR_AUDITOR_NAME = "ICF_AUDITOR_NAME";
    
}
