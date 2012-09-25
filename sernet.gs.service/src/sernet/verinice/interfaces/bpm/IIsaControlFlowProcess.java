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
    
    String VAR_DEADLINE_COMMENT = "ISA_DEADLINE_COMMENT";   
    String VAR_ASSIGN_DUEDATE = "ISA_ASSIGN_DUEDATE";   
    String VAR_AUDITOR_NAME = "ICF_AUDITOR_NAME";   
    String VAR_REMINDER_PERIOD = "ICF_REMINDER_PERIOD";
    
    String TASK_ASSIGN = "icf.task.assign";   
    String TASK_EXECUTE = "icf.task.execute";   
    String TASK_ASSIGN_DEADLINE = "icf.task.assign.deadline";
    String TASK_COMMENT = "icf.task.comment";
    String TASK_ASSIGN_NOT_RESPONSIBLE = "icf.task.assign.nr";
    String TASK_OBTAIN_ADVISE = "icf.task.obtainAdvise";
    String TASK_CHECK = "icf.task.check";
    String TASK_ASSIGN_AUDITOR = "icf.task.assignAuditor";
    
    String TRANS_ERROR = "icf.trans.error";
    String TRANS_ASSIGNED = "icf.trans.assigned";
    String TRANS_CHECK = "icf.trans.check";
    String TRANS_COMPLETE = "icf.trans.complete";
    String TRANS_FINISH = "icf.trans.finish";
    String TRANS_OK = "icf.trans.ok";
    String TRANS_WAIT = "icf.trans.wait";
    
    String REMINDER_FIXED = "REMINDER_FIXED";   
    String REMINDER_NOT_CHANGED = "REMINDER_NOT_CHANGED";

    String AUDIT_STARTS = "AUDIT_STARTS";   
    String DEADLINE_PASSED = "DEADLINE_PASSED";    
    String NOT_RESPONSIBLE = "NOT_RESPONSIBLE";
    
}
