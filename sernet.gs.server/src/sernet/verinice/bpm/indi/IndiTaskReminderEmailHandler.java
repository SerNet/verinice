/*******************************************************************************
 * Copyright (c) 2014 Daniel Murygin.
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
package sernet.verinice.bpm.indi;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import sernet.verinice.bpm.EmailHandlerFactory;
import sernet.verinice.bpm.TaskReminderEmailHandler;
import sernet.verinice.interfaces.bpm.IGenericProcess;
import sernet.verinice.interfaces.bpm.IIndividualProcess;
import sernet.verinice.model.bpm.AbortException;

/**
 * Email handler for individual workflow defined in
 * individual-task.jpdl.xml.
 * 
 * This handler is created by {@link EmailHandlerFactory}.
 * Task id for this handler is IIndividualProcess.TASK_EXECUTE.
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class IndiTaskReminderEmailHandler extends TaskReminderEmailHandler {

    /**
     * Throws AbortException if reminder date is in the future.
     * 
     * @see sernet.verinice.bpm.GenericEmailHandler#validate(java.util.Map, java.util.Map)
     */
    @Override
    public void validate(Map<String,Object> processVariables, Map<String,String> userParameter) throws AbortException {
        final Date dueDate = (Date) processVariables.get(IGenericProcess.VAR_DUEDATE);
        final int reminderDays = (Integer) processVariables.get(IIndividualProcess.VAR_REMINDER_DAYS);
        final Date reminderDate = getReminderDate(dueDate, reminderDays);
        final Calendar now = Calendar.getInstance();
        if(reminderDate.after(now.getTime())) {
            throw new AbortException("Reminder date is in the future.");
        }
    }
    
    private Date getReminderDate(Date dueDate, int reminderDays) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(dueDate);
        cal.add(Calendar.DAY_OF_MONTH, -1*reminderDays);  
        return cal.getTime();
    }
}
