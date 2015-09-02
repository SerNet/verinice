/*******************************************************************************
 * Copyright (c) 2015 Daniel Murygin.
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
package sernet.verinice.service;

import java.util.HashMap;

import org.apache.log4j.Logger;
import org.springframework.security.context.SecurityContext;
import org.springframework.security.context.SecurityContextHolder;

import sernet.gs.server.security.DummyAuthentication;
import sernet.hui.common.VeriniceContext;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.interfaces.IElementTitleCache;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.service.commands.LoadElementTitles;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class ElementTitleCache implements IElementTitleCache {

    private static final Logger LOG = Logger.getLogger(ElementTitleCache.class);
    
    private HashMap<Integer, String> titleMap = new HashMap<Integer, String>();
    private LoadElementTitles loadScopeTitlesCommand;
    private ICommandService commandService;
    
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IElementTitleCache#get(java.lang.Integer)
     */
    @Override
    public String get(Integer dbId) {
        return titleMap.get(dbId);
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IElementTitleCache#load(java.lang.String[])
     */
    @Override
    public void load(String... typeIds) {
        try {    
            loadScopeTitlesCommand = getCommandService().executeCommand(loadScopeTitlesCommand);
            titleMap.putAll(loadScopeTitlesCommand.getElements());
        } catch (CommandException e) {
            LOG.error("Error while loading titles", e);
        }     
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IElementTitleCache#update(java.lang.Integer, java.lang.String)
     */
    @Override
    public String update(Integer dbId, String title) {
        return titleMap.put(dbId, title);
    }

    public LoadElementTitles getLoadScopeTitlesCommand() {
        return loadScopeTitlesCommand;
    }

    public void setLoadScopeTitlesCommand(LoadElementTitles loadScopeTitlesCommand) {
        this.loadScopeTitlesCommand = loadScopeTitlesCommand;
    }

    protected ICommandService getCommandService() {
        if(commandService==null) {
            commandService = (ICommandService) VeriniceContext.get(VeriniceContext.COMMAND_SERVICE);
        }
        return commandService;
    }

    public void setCommandService(ICommandService commandService) {
        this.commandService = commandService;
    }

}
