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
package sernet.gs.ui.rcp.main.service.migrationcommands;

import org.apache.log4j.Logger;

import sernet.gs.service.RuntimeCommandException;

/**
 * Nothing to do in this command when updating to 0.98 except updating the version itself.
 * Everything else is done in sernet.gs.server.SchemaCreator
 * and by Hibernate.
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 *  @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 */
@SuppressWarnings("serial")
public class MigrateDbTo0_98 extends DbMigration {

    private transient Logger log = Logger.getLogger(MigrateDbTo0_98.class);

    public Logger getLog() {
        if (log == null) {
            log = Logger.getLogger(MigrateDbTo0_98.class);
        }
        return log;
    }
    
	@Override
	public double getVersion() {
		return 0.98D;
	}

	public void execute() throws RuntimeCommandException {
	    if (getLog().isDebugEnabled()) {
	        getLog().debug("Updating database version to 0.98");
        }
		super.updateVersion();
	}


}
