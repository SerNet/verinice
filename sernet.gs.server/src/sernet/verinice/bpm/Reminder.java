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

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jbpm.api.ExecutionService;
import org.jbpm.api.ProcessEngine;
import org.jbpm.api.listener.EventListener;
import org.jbpm.api.listener.EventListenerExecution;
import org.springframework.security.context.SecurityContext;
import org.springframework.security.context.SecurityContextHolder;

import sernet.gs.server.security.DummyAuthentication;
import sernet.gs.service.ServerInitializer;
import sernet.hui.common.VeriniceContext;
import sernet.verinice.interfaces.bpm.IGenericProcess;

/**
 * jBPM EventListener which sends email reminder to the assignee
 * of an task if notify is called by jBPM engine.
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
public class Reminder implements EventListener  {

    private static final Logger LOG = Logger.getLogger(Reminder.class);
    
    private DummyAuthentication authentication = new DummyAuthentication(); 
    
    private String taskType;

    public void sendEmail(String taskType, String recipient, String uuid) {
        ServerInitializer.inheritVeriniceContextState();
        doSendEmail(Collections.<String, Object> emptyMap(), taskType, recipient, uuid);
    }
    
    public void sendEmail(String executionId, String taskType, String recipient, String uuid) {
        ServerInitializer.inheritVeriniceContextState();
        doSendEmail(loadVariablesForProcess(executionId), taskType, recipient, uuid);
    }
    
    public void sendEmailWithoutElement(String executionId, String taskType, String recipient) {
        ServerInitializer.inheritVeriniceContextState();
        doSendEmail(loadVariablesForProcess(executionId), taskType, recipient, null);
    }
   
    private void doSendEmail(Map<String, Object> variables, String taskType, String recipient, String uuid) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("sendEmail called (taskType: " + taskType + ", recipient: " + recipient + ", uuid: " + uuid + ")...");           
        }
        
        if(!validate(taskType,recipient)) {
            return;
        }      
        
        // NotificationJob can not do a real login
        // authentication is a fake instance to run secured commands and dao actions
        // without a login
        boolean dummyAuthAdded = false;
        SecurityContext ctx = SecurityContextHolder.getContext(); 
        try {                    
            if(ctx.getAuthentication()==null) {
                ctx.setAuthentication(authentication);
                dummyAuthAdded = true;
            }
                 
            IEmailHandler handler = EmailHandlerFactory.getHandler(taskType);
            if(handler==null) {
                LOG.error("No email handler found for task: " + taskType + ". Can not send email."); 
                return;
            }
            handler.send(recipient, taskType, variables, uuid);
        } finally {
            if(dummyAuthAdded) {
                ctx.setAuthentication(null);
                dummyAuthAdded = false;
            }
        }
    }
    
    private boolean validate(String taskType, String assignee) {
        if(taskType==null) {
            LOG.error("Task type is null. Can not send email.");
            return false;
        }
        if(assignee==null) {
            LOG.error("Assignee type is null. Can not send email.");
            return false;
        }
        if(EmailHandlerFactory.getHandler(taskType)==null) {
            LOG.error("No email handler is registered for task type: " + taskType +". Can not send email.");
            return false;
        }
        return true;
    }

    /* (non-Javadoc)
     * @see org.jbpm.api.listener.EventListener#notify(org.jbpm.api.listener.EventListenerExecution)
     */
    @Override
    public void notify(EventListenerExecution execution) throws Exception {
        Map<String, Object> variables = (Map<String, Object>)execution.getVariables();        
        String recipient = getRecipient(variables);
        String uuid = (String) variables.get(IGenericProcess.VAR_UUID);
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("Notify, taskType: " + taskType + ", assignee: " + recipient + ", uuid: " + uuid);
        }
        
        doSendEmail(variables, taskType, recipient, uuid);   
    }

    /**
     * Returns the user name of the recipient
     * of the reminder email.
     * 
     * In this class the value of process variable
     * IGenericProcess.VAR_ASSIGNEE_NAME is returned.
     * 
     * Extend {@link Reminder} and override this method
     * to change the recipient
     * 
     * @param variables Process variables
     * @return User name of the recipient
     */
    protected String getRecipient(Map<String, Object> variables) {
        return (String) variables.get(IGenericProcess.VAR_ASSIGNEE_NAME);
    }
    
    private Map<String, Object> loadVariablesForProcess(String executionId) {
        Set<String> varNameSet = getExecutionService().getVariableNames(executionId);
        Map<String, Object> varMap = Collections.emptyMap();
        if(varNameSet!=null && !varNameSet.isEmpty()) {
            varMap = getExecutionService().getVariables(executionId,varNameSet);
        }
        return varMap;
    }

    private ExecutionService getExecutionService() {
        return getProcessEngine().getExecutionService();
    }

    protected ProcessEngine getProcessEngine() {
        return (ProcessEngine) VeriniceContext.get(VeriniceContext.JBPM_PROCESS_ENGINE);
    }

}
