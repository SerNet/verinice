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
package sernet.verinice.bpm;

import java.util.Map;

import sernet.verinice.interfaces.bpm.IGenericProcess;

/**
 * Unlike {@link Reminder} this reminder sends an email
 * to the creator of a process if notify is called by jBPM engine.
 * 
 * See jBPM developers guide for timer and event listener
 * documentation.
 * 
 * This Reminder is referenced for example in individual-task.jpdl.xml
 * (see task: "indi.task.execute").
 * 
 * http://docs.jboss.com/jbpm/v4/devguide/html_single/#timer
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class ProcessCreaterReminder extends Reminder {

    /**
     * Returns the user name of the recipient
     * of the reminder email.
     * 
     * In this class the value of process variable
     * IGenericProcess.VAR_OWNER_NAME is returned.
     * 
     * @param variables Process variables
     * @return User name of the recipient
     */
    @Override
    protected String getRecipient(Map<String, Object> variables) {
        return (String) variables.get(IGenericProcess.VAR_OWNER_NAME);
    }
}
