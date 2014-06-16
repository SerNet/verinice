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

import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;

import sernet.gs.service.RetrieveInfo;
import sernet.gs.service.ServerInitializer;
import sernet.hui.common.VeriniceContext;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.interfaces.bpm.ITaskService;
import sernet.verinice.model.bpm.AbortException;
import sernet.verinice.model.bpm.MissingParameterException;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.service.commands.LoadElementByUuid;

/**
 * Email handler for use in JBPM workflows.
 * You have to override methods addParameter(), getTemplate()
 * in your implementation.
 * 
 * Email handler is called by {@link Reminder} and is configured
 * GenericEmailHandler sends email by a {@link IRemindService}.
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public abstract class GenericEmailHandler implements IEmailHandler {
    
    private static final Logger LOG = Logger.getLogger(GenericEmailHandler.class);
    
    // template path without lang code "_en" and file extension ".vm"
    private static final String TEMPLATE_BASE_PATH = "sernet/verinice/bpm/"; //$NON-NLS-1$
    protected static final String TEMPLATE_EXTENSION = ".vm"; //$NON-NLS-1$
    
    protected static final String TEMPLATE_TASK_TITLE = "taskTitle";
    protected static final String TEMPLATE_TASK_DESCRIPTION = "taskDescription";    
    protected static final String TEMPLATE_ELEMENT_TITLE = "elementTitle";
    
    private IRemindService remindService;
    
    private ICommandService commandService;
    
    /**
     * Loads user based data calls abstract method addParameter
     * ans sends email by {@link IRemindService}.
     * 
     * @see sernet.verinice.bpm.IEmailHandler#send(java.lang.String, java.lang.String)
     */
    @Override
    public void send(String assignee, String type, Map<String, Object> processVariables, String uuid) {
        try { 
            ServerInitializer.inheritVeriniceContextState();        
            Map<String , String> userParameter = getRemindService().loadUserData(assignee);
            userParameter.put(IRemindService.TEMPLATE_PATH, getTemplatePath());
            addParameter(type, processVariables, uuid, userParameter);  
            validate(processVariables, userParameter); // throws AbortException if it fails
            getRemindService().sendEmail(userParameter, isHtml());
        } catch(AbortException e) {
            LOG.info("Execution aborted: " + e.getMessage());
            if (LOG.isDebugEnabled()) {
                LOG.error("stacktrace: ", e);
            }         
        } catch(MissingParameterException e) {
            LOG.error("Email can not be send: " + e.getMessage());
            if (LOG.isDebugEnabled()) {
                LOG.error("stacktrace: ", e);
            }         
        } catch(Exception e) {
            LOG.error("Error while sending email", e);
        }
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.bpm.IEmailHandler#isHtml()
     */
    @Override
    public boolean isHtml() {
        return false;
    }
    
    /**
     * Returns the bundle/jar relative path to the velocity email template.
     * First a localized template is search by the default locale of the java vm.
     * If localized template is not found default/english template is returned.
     * 
     * Localized template path: <TEMPLATE_BASE_PATH>_<LANG_CODE>.vm
     * Default template path: <TEMPLATE_BASE_PATH>.vm
     * 
     * @return bundle/jar relative path to the velocity email template
     */
    protected String getTemplatePath() {      
        String langCode = Locale.getDefault().getLanguage();
        String path = TEMPLATE_BASE_PATH + getTemplate() + "_" + langCode + TEMPLATE_EXTENSION; //$NON-NLS-1$
        if(this.getClass().getClassLoader().getResource(path)==null) {
            path = TEMPLATE_BASE_PATH + getTemplate() + TEMPLATE_EXTENSION;
        }
        return path;
    }
    
    protected String replaceSpecialChars(String description) {
        String result = description.replace("ä", "&auml;");
        result = result.replace("Ä", "&Auml;");
        result = result.replace("ü", "&uuml;");
        result = result.replace("Ü", "&Uuml;");
        result = result.replace("ö", "&ouml;");
        result = result.replace("Ö", "&Ouml;");
        result = result.replace("ß", "&szlig;");
        result = result.replace("€", "&euro;");
        return result;
    }
    
    protected CnATreeElement retrieveElement(String uuid, RetrieveInfo ri) throws CommandException {
        LoadElementByUuid<CnATreeElement> command = new LoadElementByUuid<CnATreeElement>(uuid, ri);
        command = getCommandService().executeCommand(command);
        return command.getElement();
    }
    
    protected IRemindService getRemindService() {
        if(remindService==null) {
            remindService = (IRemindService) VeriniceContext.get(VeriniceContext.REMIND_SERVICE);
        }
        return remindService;
    }
    
    protected ICommandService getCommandService() {
        if(commandService==null) {
            commandService = (ICommandService) VeriniceContext.get(VeriniceContext.COMMAND_SERVICE);
        }
        return commandService;
    }
    
    protected ITaskService getTaskService() {
        return (ITaskService) VeriniceContext.get(VeriniceContext.TASK_SERVICE);
    }
    
    @Override
    public void validate(Map<String, Object> processVariables, Map<String, String> userParameter) throws AbortException {
        // nothing means validate success
    }

}
