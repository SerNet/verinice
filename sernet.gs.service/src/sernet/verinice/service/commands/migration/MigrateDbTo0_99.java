/*******************************************************************************
 * Copyright (c) 2011 Daniel Murygin <dm[at]sernet[dot]de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *     You should have received a copy of the GNU Lesser General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *    Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.service.commands.migration;

import org.apache.log4j.Logger;

import sernet.gs.service.RuntimeCommandException;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.service.commands.AddDefaultGroups;
import sernet.verinice.service.commands.UpdateScopeId;

/**
 * New column scope_id is added in version 0.99.
 * Columns contains the scope or org-id of the element.
 * This command sets the scope id for all existing elements.
 * 
 * In the end is updates the version itself.
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 */
@SuppressWarnings({ "serial", "restriction" })
public class MigrateDbTo0_99 extends DbMigration {

    private static final Logger log = Logger.getLogger(MigrateDbTo0_99.class);

	@Override
	public double getVersion() {
		return 0.99D;
	}

	public void execute() throws RuntimeCommandException {
	    if (log.isDebugEnabled()) {
	        log.debug("Updating database version to 0.99");
        }
	    UpdateScopeId updateScopeId = new UpdateScopeId();
	    try {
	        getCommandService().executeCommand(updateScopeId);
        } catch (CommandException e) {
            log.error("Error while updating scope id.", e);
            throw new RuntimeCommandException(e);
        }
	    
	    AddDefaultGroups addDefaultGroups = new AddDefaultGroups();
        try {
            getCommandService().executeCommand(addDefaultGroups);
        } catch (CommandException e) {
            log.error("Error while adding default groups.", e);
            throw new RuntimeCommandException(e);
        }
	    
		super.updateVersion();
	}


}
