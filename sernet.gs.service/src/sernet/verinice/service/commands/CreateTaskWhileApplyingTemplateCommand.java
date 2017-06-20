/*******************************************************************************  
 * Copyright (c) 2017 Viktor Schmidt.  
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
package sernet.verinice.service.commands;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.log4j.Logger;

import sernet.gs.service.ServerInitializer;
import sernet.hui.common.VeriniceContext;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IAccountService;
import sernet.verinice.interfaces.IAuthService;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.IConfigurationService;
import sernet.verinice.interfaces.bpm.IIndividualService;
import sernet.verinice.interfaces.bpm.IndividualServiceParameter;
import sernet.verinice.model.bsi.MassnahmenUmsetzung;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.CnATreeElement.TemplateType;
import sernet.verinice.model.common.Permission;

/**
 * This command creates a task for modeling template master
 * ({@link /sernet.gs.server/WebContent/WEB-INF/veriniceserver-plain.properties})
 * each time an user copies a modeling template ({@link TemplateType#TEMPLATE},
 * {@link CopyCommand}). Skips creating task, if logged in user is the modeling
 * template master.
 * 
 * @see TemplateType
 * @see CopyCommand
 * 
 * @author Viktor Schmidt <vschmidt[at]ckc[dot]de>
 */
@SuppressWarnings("serial")
public class CreateTaskWhileApplyingTemplateCommand extends GenericCommand {

    private transient Logger log = Logger.getLogger(CreateTaskWhileApplyingTemplateCommand.class);

    public Logger getLog() {
        if (log == null) {
            log = Logger.getLogger(CreateTaskWhileApplyingTemplateCommand.class);
        }
        return log;
    }
    
    private CnATreeElement selectedModelingTemplate;
    private CnATreeElement applyToElement;
    
    private static HashMap<Integer, String> titleMap = new HashMap<>();

    public CreateTaskWhileApplyingTemplateCommand(CnATreeElement selectedModelingTemplate, CnATreeElement applyToElement) {
        super();
        this.selectedModelingTemplate = selectedModelingTemplate;
        this.applyToElement = applyToElement;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICommand#execute()
     */
    @Override
    public void execute() { 
        createTaskWhileApplyingTemplateCommand();
    }

    private void createTaskWhileApplyingTemplateCommand() {
        if (selectedModelingTemplate.isTemplate() && !MassnahmenUmsetzung.TYPE_ID.equals(selectedModelingTemplate.getTypeId())) {
            ServerInitializer.inheritVeriniceContextState();
            String modelingTemplateMaster = modelingTemplateMaster();
            String username = ((IAuthService) VeriniceContext.get(VeriniceContext.AUTH_SERVICE)).getUsername();

            if (!modelingTemplateMaster.equals(username)) {
                IIndividualService individualService = (IIndividualService) VeriniceContext.get(VeriniceContext.INDIVIDUAL_SERVICE);
                IndividualServiceParameter parameter = getIndividualServiceParameter(selectedModelingTemplate, applyToElement, modelingTemplateMaster);
                individualService.startProcess(parameter);
            }
        }
    }

    private String modelingTemplateMaster() {
        String defaultTemplateMaster = PropertyLoader.getModelingTemplateMaster();
        if (defaultTemplateMaster == null) {
            getLog().error("Error while parsing property " + PropertyLoader.MODELING_TEMPLATE_MASTER);
            defaultTemplateMaster = ((IAuthService) VeriniceContext.get(VeriniceContext.AUTH_SERVICE)).getAdminUsername();
        }
        return defaultTemplateMaster;
    }
    
    private IndividualServiceParameter getIndividualServiceParameter(final CnATreeElement copyElement, final CnATreeElement newElement, String modelingTemplateMaster) {
        IndividualServiceParameter parameter = new IndividualServiceParameter();
        parameter.setUuid(newElement.getUuid());
        parameter.setTypeId(newElement.getTypeId());
        parameter.setAssignee(modelingTemplateMaster);
        parameter.setTitle(Messages.getString("CopyCommand.TaskTitleWhenUsingTemplate"));
        String newElementScopeTitle = getScopeTitle(newElement);
        parameter.setDescription(Messages.getString("CopyCommand.TaskDescriptionWhenUsingTemplate", copyElement.getTitle(), newElement.getParent().getTitle(), newElementScopeTitle));

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, 14);
        parameter.setDueDate(cal.getTime());
        parameter.setReminderPeriodDays(7);
        parameter.setProperties(new HashSet<>(Arrays.asList(newElement.getEntityType().getAllPropertyTypeIds())));
        parameter.setWithAReleaseProcess(false);
        return parameter;
    }
    
    private String getScopeTitle(final CnATreeElement copyElement) {
        String scopeTitle = "";
        try {
            if (!titleMap.containsKey(copyElement.getScopeId())) {
                scopeTitle = loadElementsTitles(copyElement);
            } else {
                scopeTitle = titleMap.get(copyElement.getScopeId());
            }
        } catch (CommandException e) {
            log.error("Error while getting element properties", e);
        }
        return scopeTitle;
    }

    private String loadElementsTitles(CnATreeElement element) throws CommandException {
        LoadElementTitles scopeCommand;
        scopeCommand = new LoadElementTitles();
        scopeCommand = getCommandService().executeCommand(scopeCommand);
        titleMap = scopeCommand.getElements();
        return titleMap.get(element.getScopeId());
    }

    protected IBaseDao<Permission, Serializable> getDao() {
        return getDaoFactory().getDAO(Permission.class);
    }
    
    protected IAccountService getAccountService() {
        return (IAccountService) VeriniceContext.get(VeriniceContext.ACCOUNT_SERVICE);
    }
    
    protected IConfigurationService getConfigurationService() {
        return (IConfigurationService) VeriniceContext.get(VeriniceContext.CONFIGURATION_SERVICE);
    }
}
