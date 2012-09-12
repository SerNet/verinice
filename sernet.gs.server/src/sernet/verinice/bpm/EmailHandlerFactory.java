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
package sernet.verinice.bpm;

import java.util.Hashtable;
import java.util.Map;

import sernet.verinice.bpm.isam.AuditEmailHandler;
import sernet.verinice.bpm.isam.DeadlineEmailHandler;
import sernet.verinice.bpm.isam.NotResponsibleEmailHandler;
import sernet.verinice.bpm.qm.IssueFixedEmailHandler;
import sernet.verinice.bpm.qm.IssueNotFixedEmailHandler;
import sernet.verinice.interfaces.bpm.IIndividualProcess;
import sernet.verinice.interfaces.bpm.IIsaControlFlowProcess;

/**
 *
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class EmailHandlerFactory {

    private static final Map<String, IEmailHandler> handlerMap;
    
    static {
        handlerMap = new Hashtable<String, IEmailHandler>();
        // task reminder
        IEmailHandler taskReminder = new TaskReminderEmailHandler();
        handlerMap.put(IIsaControlFlowProcess.TASK_ASSIGN, taskReminder);
        handlerMap.put(IIsaControlFlowProcess.TASK_EXECUTE, taskReminder);
        handlerMap.put(IIndividualProcess.TASK_EXECUTE, taskReminder);
        handlerMap.put(IIndividualProcess.TASK_ASSIGN, taskReminder);
        // special reminder
        handlerMap.put(IIsaControlFlowProcess.DEADLINE_PASSED, new DeadlineEmailHandler());
        handlerMap.put(IIsaControlFlowProcess.NOT_RESPONSIBLE, new NotResponsibleEmailHandler());
        handlerMap.put(IIsaControlFlowProcess.AUDIT_STARTS, new AuditEmailHandler());
        handlerMap.put(IIsaControlFlowProcess.REMINDER_FIXED, new IssueFixedEmailHandler());
        handlerMap.put(IIsaControlFlowProcess.REMINDER_NOT_CHANGED, new IssueNotFixedEmailHandler());      
    }
    
    /**
     * @param taskId
     * @return
     */
    public static IEmailHandler getHandler(String taskId) {
        return handlerMap.get(taskId);
    }

}
