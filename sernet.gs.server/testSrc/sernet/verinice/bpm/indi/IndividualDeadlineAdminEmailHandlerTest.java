/*******************************************************************************
 * Copyright (c) 2019 Jochen Kemnade.
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
package sernet.verinice.bpm.indi;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import sernet.gs.service.RetrieveInfo;
import sernet.gs.service.ServerInitializer;
import sernet.hui.common.VeriniceContext;
import sernet.hui.common.VeriniceContext.State;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.snutils.DBException;
import sernet.verinice.bpm.IRemindService;
import sernet.verinice.bpm.TaskServiceDummy;
import sernet.verinice.interfaces.bpm.IGenericProcess;
import sernet.verinice.interfaces.bpm.ITaskService;
import sernet.verinice.model.bpm.MissingParameterException;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Asset;

public class IndividualDeadlineAdminEmailHandlerTest {

    @Test
    public void umlautsInNameAreEscaped() throws DBException {
        Map<String, Object> veriniceObjects = new HashMap<>();
        RemindServiceMock remindService = new RemindServiceMock();
        veriniceObjects.put(VeriniceContext.REMIND_SERVICE, remindService);
        HUITypeFactory huiTypeFactory = HUITypeFactory
                .createInstance(IndividualDeadlineAdminEmailHandlerTest.class
                        .getResource("/" + HUITypeFactory.HUI_CONFIGURATION_FILE));
        veriniceObjects.put(VeriniceContext.HUI_TYPE_FACTORY, huiTypeFactory);
        ITaskService taskService = new TaskServiceDummy() {
            @Override
            public String loadTaskDescription(String taskId, Map<String, Object> varMap,
                    boolean isHtml) {
                return "<b>Foo</b>";
            }
        };
        veriniceObjects.put(VeriniceContext.TASK_SERVICE, taskService);

        State state = new VeriniceContext.State();
        state.setMap(veriniceObjects);
        new ServerInitializer().setWorkObjects(state);

        IndividualDeadlineAdminEmailHandler handler = new IndividualDeadlineAdminEmailHandler();
        Map<String, Object> processVariables = new HashMap<>();
        processVariables.put(IGenericProcess.VAR_DUEDATE, new Date());
        handler.send(null, null, processVariables, null);

        Assert.assertEquals("M&uuml;ller", remindService.model
                .get(IndividualDeadlineAdminEmailHandler.TEMPLATE_ASSIGNEE_NAME));

        Assert.assertEquals("<b>Foo</b>", remindService.model.get("taskDescription"));

    }

    private static class RemindServiceMock implements IRemindService {

        private Map<String, String> model;

        @Override
        public Map<String, String> loadUserData(String name) throws MissingParameterException {
            Map<String, String> userData = new HashMap<>();
            userData.put(IRemindService.TEMPLATE_NAME, "Müller");
            userData.put(IRemindService.TEMPLATE_ADDRESS, "Herr");
            return userData;
        }

        @Override
        public void sendEmail(Map<String, String> model, boolean html) {
            this.model = model;

        }

        @Override
        public CnATreeElement retrieveElement(String uuid, RetrieveInfo ri) {
            return new Asset();
        }

    }

}