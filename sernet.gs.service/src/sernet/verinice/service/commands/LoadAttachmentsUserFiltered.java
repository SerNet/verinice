/*******************************************************************************
 * Copyright (c) 2013 Sebastian Hagedorn <sh@sernet.de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details.
 *     You should have received a copy of the GNU General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Sebastian Hagedorn <sh@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.service.commands;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.model.bsi.Attachment;
import sernet.verinice.model.common.configuration.Configuration;

/**
 *
 */
public class LoadAttachmentsUserFiltered extends GenericCommand {
    
    private static final long serialVersionUID = 20140530;

    private static final Logger LOG = Logger.getLogger(LoadAttachmentsUserFiltered.class);
    
    private Integer id;
    
    private List<Attachment> result;
    
    public LoadAttachmentsUserFiltered(Integer id){
        this.id = id;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICommand#execute()
     */
    @Override
    public void execute() {
        try{
            Set<String> roles = null;
            LoadAttachments command = null;
            if(id == null){
                LoadCurrentUserConfiguration lcuc = new LoadCurrentUserConfiguration();
                lcuc = getCommandService().executeCommand(lcuc);
                Configuration c = lcuc.getConfiguration();
                boolean isAdminUser = false;
                boolean isScopeOnly = false;
                Integer scopeId = -1;
                if(c != null){
                    roles = c.getRoles();
                    isAdminUser = c.isAdminUser();
                    isScopeOnly = c.isScopeOnly();
                    scopeId = c.getPerson().getScopeId();
                }

                if(roles == null){
                    roles = new HashSet<String>(0);
                }
                command = new LoadAttachments(id, roles.toArray(new String[roles.size()]), isAdminUser, isScopeOnly, scopeId);
            } else {
                command = new LoadAttachments(id);
            }
            command = getCommandService().executeCommand(command);
            result = command.getAttachmentList();
        } catch(Exception e){
            LOG.error("Error loading attachments filtered by user", e);
        }
    }
    
    public List<Attachment> getResult(){
        if(result == null){
            return new ArrayList<Attachment>(0);
        }
        return this.result;
    }
}
