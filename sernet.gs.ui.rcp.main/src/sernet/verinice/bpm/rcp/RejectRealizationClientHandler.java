/*******************************************************************************
 * Copyright (c) 2016 Viktor Schmidt.
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
 *     Viktor Schmidt <vschmidt[at]ckc[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.bpm.rcp;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.hui.common.VeriniceContext;
import sernet.verinice.bpm.ICompleteClientHandler;
import sernet.verinice.interfaces.bpm.IIndividualProcess;
import sernet.verinice.interfaces.bpm.ITask;
import sernet.verinice.interfaces.bpm.ITaskService;
import sernet.verinice.interfaces.bpm.IndividualServiceParameter;
import sernet.verinice.model.bsi.Person;
import sernet.verinice.rcp.NonModalWizardDialog;

/**
 * @author Viktor Schmidt <vschmidt[at]ckc[dot]de>
 */
public class RejectRealizationClientHandler implements ICompleteClientHandler {
    private static final Logger LOG = Logger.getLogger(RejectRealizationClientHandler.class);

    private Shell shell;

    private int dialogStatus;

    /*
     * (non-Javadoc)
     * 
     * @see sernet.verinice.bpm.ICompleteClientHandler#execute()
     */
    @Override
    public Map<String, Object> execute(final ITask task) {
        Map<String, Object> parameter = new HashMap<String, Object>();
        try {
            final IndividualProcessWizard wizard = new IndividualProcessWizard(Collections.singletonList(task.getUuid()), task.getElementTitle(), task.getElementType());
            // TODO: check if isGrundschutzElement?
            wizard.setPersonTypeId(Person.TYPE_ID);
            Display.getDefault().syncExec(new Runnable() {
                @Override
                public void run() {
                    WizardDialog wizardDialog = new NonModalWizardDialog(shell, wizard);
                    wizardDialog.create();

                    IndividualServiceParameter individualServiceParameter = getIndividualServiceParameter(task.getId());
                    wizard.setTemplateForRejectedRealization(individualServiceParameter);
                    dialogStatus = wizardDialog.open();
                }
            });

            if (dialogStatus == Window.OK) {
                wizard.saveTemplate();
                parameter.putAll(ServiceFactory.lookupIndividualService().createParameterMap(wizard.getParameter()));
            } else {
                throw new CompletionAbortedException("Canceled by user.");
            }
        } catch (CompletionAbortedException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("Error while apply for extension.", e);
        }
        return parameter;
    }

    private IndividualServiceParameter getIndividualServiceParameter(String taskId) {
        Map<String, Object> taskVariables = getTaskService().getVariables(taskId);
        final IndividualServiceParameter individualServiceParameter = new IndividualServiceParameter();
        individualServiceParameter.setDueDate((Date) taskVariables.get(IIndividualProcess.VAR_DUEDATE));
        individualServiceParameter.setReminderPeriodDays((Integer) taskVariables.get(IIndividualProcess.VAR_REMINDER_DAYS));
        individualServiceParameter.setAssignee((String) taskVariables.get(IIndividualProcess.VAR_ASSIGNEE_NAME));
        individualServiceParameter.setAssigneeRelationId((String) taskVariables.get(IIndividualProcess.VAR_RELATION_ID));
        individualServiceParameter.setProperties((Set<String>) taskVariables.get(IIndividualProcess.VAR_PROPERTY_TYPES));
        individualServiceParameter.setTitle((String) taskVariables.get(IIndividualProcess.VAR_TITLE));
        individualServiceParameter.setDescription((String) taskVariables.get(IIndividualProcess.VAR_DESCRIPTION));
        individualServiceParameter.setWithAReleaseProcess((boolean) taskVariables.get(IIndividualProcess.VAR_IS_WITH_RELEASE_PROCESS));
        return individualServiceParameter;
    }

    /*
     * (non-Javadoc)
     * 
     * @see sernet.verinice.bpm.ICompleteClientHandler#setShell(org.eclipse.swt.
     * widgets.Shell)
     */
    @Override
    public void setShell(Shell shell) {
        this.shell = shell;
    }

    private ITaskService getTaskService() {
        return (ITaskService) VeriniceContext.get(VeriniceContext.TASK_SERVICE);
    }
}
