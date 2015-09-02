/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak[at]sernet[dot]de>.
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
 *     Alexander Koderman <ak[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.service.migrationcommands;

import sernet.gs.service.RuntimeCommandException;

/**
 *  Nothing to do when updating to 0.97 except updating the version itself.
 * Everything else is done by Hibernate.
 * 
 * ATTENTION: 
 * 
 * Since DB version 0.97 the inheritance mapping for the CnaTreeelement class hierarchy
 * is moved into a single table. The table BSIModel is no longer used. 
 * If this table is still present from an older DB and still contains a DB version 
 * number IT IS NOT BEING USED and can be removed.
 * 
 * To find out the current DB version you have to check the CnATreeElement table, i.e.:
 * "select * from cnatreeelement where dbVersion is not null;"
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 *  @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 */
@SuppressWarnings("serial")
public class MigrateDbTo0_97 extends DbMigration {

	@Override
	public double getVersion() {
		return 0.97D;
	}

	public void execute() throws RuntimeCommandException {
		super.updateVersion();
	}


}
