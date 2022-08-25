/*******************************************************************************
 * Copyright (c) 2022 Urs Zeidler.
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
 ******************************************************************************/
package sernet.verinice.bpm;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.springframework.core.io.Resource;

import sernet.gs.service.RetrieveInfo;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.snutils.DBException;
import sernet.verinice.interfaces.bpm.ITask;
import sernet.verinice.interfaces.bpm.ITaskParameter;
import sernet.verinice.interfaces.bpm.ITaskService;
import sernet.verinice.model.bpm.MissingParameterException;
import sernet.verinice.model.common.CnATreeElement;

public class TaskReminderEmailHandlerTest {

    @Test
    public void testContentHtmlQuoting() {
        // we need to extend the code we want to test
        // to not deal with any DI spring stuff
        // like a mock
        TaskReminderEmailHandler taskReminderEmailHandler = new TaskReminderEmailHandler() {

            // first mock
            @Override
            protected IRemindService getRemindService() {
                return new IRemindService() {
                    @Override
                    public void sendEmail(Map<String, String> model, boolean html) {
                    }

                    @Override
                    public CnATreeElement retrieveElement(String uuid, RetrieveInfo ri) {
                        // our element the task is about
                        return new CnATreeElement() {
                            @Override
                            protected HUITypeFactory getTypeFactory() {
                                return new HUITypeFactory() {
                                    @Override
                                    public String getMessage(String key) {
                                        return "<b>Title should be quoted</b>";
                                    }
                                };

                            }

                            @Override
                            public String getTypeId() {
                                return "no id";
                            }
                        };
                    }

                    @Override
                    public Map<String, String> loadUserData(String name)
                            throws MissingParameterException {
                        return Map.of("user", "userdata");
                    }
                };
            }

            // second mock provides the task
            @Override
            protected ITaskService getTaskService() {
                return new ITaskService() {

                    @Override
                    public void updateChangedElementProperties(String taskId,
                            Map<String, String> changedElementProperties) {
                    }

                    @Override
                    public void setVariables(String taskId, Map<String, Object> param) {
                    }

                    @Override
                    public void setDuedate(Set<String> taskIdset, Date duedate) {
                    }

                    @Override
                    public void setAssigneeVar(Set<String> taskIdSet, String username) {
                    }

                    @Override
                    public void setAssignee(Set<String> taskIdSet, String username) {
                    }

                    @Override
                    public void saveChangedElementPropertiesToCnATreeElement(String taskId,
                            String uuid) {
                    }

                    @Override
                    public void markAsRead(String taskId) {
                    }

                    @Override
                    public String loadTaskTitle(String taskId, Map<String, Object> varMap) {
                        return "<b>My title</b>";
                    }

                    @Override
                    public String loadTaskDescription(String taskId, Map<String, Object> varMap) {
                        return "<b>My description</b>";
                    }

                    @Override
                    public Map<String, String> loadChangedElementProperties(String taskId) {
                        return Map.of("test Property", "my poperty");
                    }

                    @Override
                    public boolean isActive() {
                        return false;
                    }

                    @Override
                    public Map<String, Object> getVariables(String taskId) {
                        return Map.of("test", "my variable");
                    }

                    @Override
                    public Set<String> getTaskReminderBlacklist() {
                        return null;
                    }

                    @Override
                    public List<ITask> getTaskList(ITaskParameter parameter) {
                        return Collections.emptyList();
                    }

                    @Override
                    public List<String> getElementList() {
                        return Collections.emptyList();
                    }

                    @Override
                    public List<ITask> getCurrentUserTaskList(ITaskParameter parameter) {
                        return Collections.emptyList();
                    }

                    @Override
                    public List<ITask> getCurrentUserTaskList() {
                        return Collections.emptyList();
                    }

                    @Override
                    public ITask findTask(String taskId) {
                        return null;
                    }

                    @Override
                    public void completeTask(String taskId, String outcomeId,
                            Map<String, Object> parameter) {
                    }

                    @Override
                    public void completeTask(String taskId, Map<String, Object> parameter) {
                    }

                    @Override
                    public void completeTask(String taskId, String outcomeId) {
                    }

                    @Override
                    public void completeTask(String taskId) {
                    }

                    @Override
                    public void cancelTask(String taskId) {
                    }

                };
            }

            // we only use the relevant part of the org code
            // getRemindService().sendEmail(userParameter, isHtml());
            @Override
            public void send(String assignee, String type, Map<String, Object> processVariables,
                    String uuid) {
                Map<String, String> userParameter = new HashMap<>();
                try {
                    addParameter(type, processVariables, uuid, userParameter);
                    processVariables.putAll(userParameter);
                } catch (MissingParameterException e) {
                    throw new RuntimeException(e);
                }
            }
        };

        Map<String, Object> pv = new HashMap<String, Object>();
        taskReminderEmailHandler.send("", "", pv, null);

        assertEquals("&lt;b&gt;Title should be quoted&lt;/b&gt;", pv.get("elementTitle"));
        assertEquals("&lt;b&gt;My title&lt;/b&gt;", pv.get("taskTitle"));
        assertEquals("verinice task reminder: <b>My title</b>", pv.get("subject"));
        assertEquals("<b>My description</b>", pv.get("taskDescription"));
    }

}
