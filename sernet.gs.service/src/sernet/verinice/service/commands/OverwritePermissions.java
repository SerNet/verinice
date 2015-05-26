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
package sernet.verinice.service.commands;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import sernet.gs.service.RetrieveInfo;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.interfaces.IPostProcessor;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.Permission;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public abstract class OverwritePermissions implements IPostProcessor, Serializable {

    private transient Logger log = Logger.getLogger(OverwritePermissions.class);

    private String uuidPermissionParent;
    
    public Logger getLog() {
        if (log == null) {
            log = Logger.getLogger(OverwritePermissions.class);
        }
        return log;
    }
    
    Set<Permission> permissions;
    
    public OverwritePermissions(String uuid) {
        super();
        this.uuidPermissionParent = uuid;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IPostProcessor#process(java.util.List, java.util.Map)
     */
    @Override
    public void process(List<String> copyUuidList, Map<String, String> sourceDestMap) {
        try {
            loadPermissions();
            for (String uuidSource : copyUuidList) {
                String uuidDest = sourceDestMap.get(uuidSource);
                overwritePermissions(uuidDest);
            }
        } catch (CommandException e) {
            getLog().error("Error while overwriting permissions", e);
        }
    }

    private void loadPermissions() throws CommandException {
        RetrieveInfo ri  = new RetrieveInfo();
        ri.setPermissions(true);
        LoadElementByUuid<CnATreeElement> loadCommand = new LoadElementByUuid<CnATreeElement>(uuidPermissionParent, ri);
        loadCommand = getCommandService().executeCommand(loadCommand);
        permissions = loadCommand.getElement().getPermissions();
    }

    private void overwritePermissions(String uuid) throws CommandException {      
        UpdatePermissions updatePermissions = new UpdatePermissions(uuid, permissions, true, true);
        updatePermissions = getCommandService().executeCommand(updatePermissions);       
    }

    protected abstract ICommandService getCommandService();

}
