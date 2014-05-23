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
public interface IIndividualProcess extends IGenericProcess {
    
    String KEY = "individual-task";
    
    String VAR_TITLE = "INDI_TITLE";   
    String VAR_DESCRIPTION = "INDI_DESCRIPTION";
    String VAR_RELATION_ID = "INDI_RELATION_ID";   
    String VAR_REMINDER_DATE = "INDI_REMINDER_DATE";
    
    String TASK_ASSIGN = "indi.task.assign";  
    String TASK_EXECUTE = "indi.task.execute";    
    String TASK_CHECK = "indi.task.check";   
    String TASK_DEADLINE = "indi.task.assign.deadline"; 
    String TASK_NOT_RESPOSIBLE = "indi.task.assign.nr";
    
    String TRANS_COMPLETE = "indi.trans.complete";
    String TRANS_ASSIGNED = "indi.trans.assigned";
    String TRANS_ACCEPT = "indi.trans.accept";
}
