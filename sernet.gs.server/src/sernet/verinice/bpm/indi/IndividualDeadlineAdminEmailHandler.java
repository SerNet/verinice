/*******************************************************************************
 * Copyright (c) 2013 Daniel Murygin.
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

import sernet.gs.service.RetrieveInfo;
import sernet.verinice.bpm.GenericEmailHandler;
import sernet.verinice.bpm.IEmailHandler;
import sernet.verinice.bpm.IRemindService;
import sernet.verinice.interfaces.bpm.IGenericProcess;
import sernet.verinice.model.bpm.AbortException;
import sernet.verinice.model.bpm.MissingParameterException;
import sernet.verinice.model.common.CnATreeElement;

/**
 * Email handler for task "indi.task.execute" from process "individual-task"
 * defined in file: individual-task.jpdl.xml.
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class IndividualDeadlineAdminEmailHandler extends GenericEmailHandler implements IEmailHandler {

    private static final String TEMPLATE = "IndiDeadlineAdmin"; //$NON-NLS-1$
    
    public static final String TEMPLATE_ASSIGNEE_NAME = "assignee-name";   
    public static final String TEMPLATE_ASSIGNEE_ADDRESS = "assignee-address";

    /**
     * Param uuidElement is always null for this email handler.
     * 
     * (non-Javadoc)
     * @see sernet.verinice.bpm.IEmailHandler#addParameter(java.lang.String, java.util.Map, java.lang.String, java.util.Map)
     */
    @Override
    public void addParameter(String type, Map<String, Object> processVariables, String uuidElement, Map<String, String> emailParameter) throws MissingParameterException {
        CnATreeElement element = getRemindService().retrieveElement(uuidElement, RetrieveInfo.getPropertyInstance());
        if(element==null) {
            throw new MissingParameterException("Obejct was not found, UUID is: " + uuidElement);
        }
        String title = element.getTitle();
        String taskTitle = getTaskService().loadTaskTitle(type, processVariables);String taskDescription = getTaskService().loadTaskDescription(type, processVariables);
        String taskTitleHtml =  taskTitle;
        if(isHtml()) {
            title = replaceSpecialChars(title);
            taskTitleHtml = replaceSpecialChars(taskTitle);
            taskDescription = replaceSpecialChars(taskDescription);
        }
        emailParameter.put(TEMPLATE_TASK_DESCRIPTION, taskDescription);
        emailParameter.put(TEMPLATE_ELEMENT_TITLE, title);
        emailParameter.put(TEMPLATE_TASK_TITLE, taskTitleHtml);
        emailParameter.put(IRemindService.TEMPLATE_SUBJECT, Messages.getString("IndividualDeadlineAdminEmailHandler.1",taskTitle));
        
        String assignee = (String) processVariables.get(IGenericProcess.VAR_ASSIGNEE_NAME);
        Map<String, String> assigneeData = getRemindService().loadUserData(assignee);
        emailParameter.put(TEMPLATE_ASSIGNEE_ADDRESS, assigneeData.get(IRemindService.TEMPLATE_ADDRESS));
        emailParameter.put(TEMPLATE_ASSIGNEE_NAME, assigneeData.get(IRemindService.TEMPLATE_NAME));
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.bpm.GenericEmailHandler#validate(java.util.Map, java.util.Map)
     */
    @Override
    public void validate(Map<String, Object> processVariables, Map<String, String> userParameter) throws AbortException {
        final Date dueDate = (Date) processVariables.get(IGenericProcess.VAR_DUEDATE);
        final Calendar now = Calendar.getInstance();
        if(dueDate.after(now.getTime())) {
            throw new AbortException("Due date is in the future.");
        }
    }

    /* (non-Javadoc)
     * @see sernet.verinice.bpm.IEmailHandler#getTemplate()
     */
    @Override
    public String getTemplate() {
        return TEMPLATE;
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.bpm.GenericEmailHandler#isHtml()
     */
    @Override
    public boolean isHtml() {
        return true;
    }

}
