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

import sernet.gs.service.RetrieveInfo;
import sernet.hui.common.VeriniceContext;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.service.commands.LoadElementByUuid;

/**
 *
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public abstract class GenericEmailHandler implements IEmailHandler {
    
    // template path without lang code "_en" and file extension ".vm"
    private static final String TEMPLATE_BASE_PATH = "sernet/verinice/bpm/"; //$NON-NLS-1$
    protected static final String TEMPLATE_EXTENSION = ".vm"; //$NON-NLS-1$
    
    private IRemindService remindService;
    
    private ICommandService commandService;
    
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
}
