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
public interface IIsaQmProcess extends IGenericProcess {

    String KEY = "isa-quality-management";
    
    String TASK_IQM_SET_ASSIGNEE = "iqm.task.setAssignee";
    
    String TASK_IQM_CHECK = "iqm.task.check";
    
    String TRANS_IQM_SET_ASSIGNEE = "iqm.trans.setAssignee";

    String TRANS_IQM_FIX = "iqm.trans.fix";
    
    String VAR_FEEDBACK = "IQM_FEEDBACK";
    
    String VAR_IQM_ASSIGNEE = "IQM_ASSIGNEE";
    
    String VAR_QM_PRIORITY = "IQM_PRIORITY";
    
    String VAR_IQM_REVIEW = "IQM_REVIEW";
    
}
