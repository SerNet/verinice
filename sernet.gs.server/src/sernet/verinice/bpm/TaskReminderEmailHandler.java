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

import java.util.Map;

import sernet.gs.service.RetrieveInfo;
import sernet.verinice.model.bpm.MissingParameterException;
import sernet.verinice.model.common.CnATreeElement;

/**
 *
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class TaskReminderEmailHandler extends GenericEmailHandler implements IEmailHandler {
    
    private static final String TEMPLATE = "TaskReminder";
    private static final String KEY_NAME = "name";
    
    @Override
    public void addParameter(String type, Map<String, Object> processVariables, String uuidElement,
            Map<String, String> emailParameter) throws MissingParameterException {

        CnATreeElement element = getRemindService().retrieveElement(uuidElement,
                RetrieveInfo.getPropertyInstance());
        if (element == null) {
            throw new MissingParameterException("Obejct was not found, UUID is: " + uuidElement);
        }
        String title = element.getTitle();
        if (isHtml()) {
            title = replaceSpecialChars(title);
        }
        emailParameter.put(TEMPLATE_ELEMENT_TITLE, title);

        String taskTitle = getTaskService().loadTaskTitle(type, processVariables);
        String taskTitleHtml = taskTitle;
        if (isHtml()) {
            taskTitleHtml = replaceSpecialChars(taskTitleHtml);
        }
        emailParameter.put(TEMPLATE_TASK_TITLE, taskTitleHtml);

        String description = getTaskService().loadTaskDescription(type, processVariables);
        if (isHtml()) {
            description = replaceSpecialChars(description);
        }
        emailParameter.put(TEMPLATE_TASK_DESCRIPTION, description);

        emailParameter.put(IRemindService.TEMPLATE_SUBJECT, "verinice task reminder: " + taskTitle);

        fixEncodingOfName(emailParameter);
    }

    private void fixEncodingOfName(Map<String, String> emailParameter) {
        String name = emailParameter.get(KEY_NAME);
        if (name != null) {
            name = replaceSpecialChars(name);
            emailParameter.put(KEY_NAME, name);
        }
    }


    @Override
    public boolean isHtml() {
        return true;
    }

    @Override
    public String getTemplate() {
        return TEMPLATE;
    }

}
