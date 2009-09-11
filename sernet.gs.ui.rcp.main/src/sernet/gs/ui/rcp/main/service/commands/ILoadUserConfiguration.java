/*******************************************************************************
 * Copyright (c) 2009 Robert Schuster <r.schuster@tarent.de>.
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
 *     Robert Schuster <r.schuster@tarent.de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.service.commands;

import java.util.List;

import sernet.gs.ui.rcp.main.service.crudcommands.LoadUserConfiguration;
import sernet.hui.common.connect.Entity;

/**
 * This interface is needed for the {@link LoadUserConfiguration}
 * class as it is automatically proxied using Spring on the server.
 * 
 * <p>The interface ensures that the methods can be called on the proxy.</p>
 * 
 * @author Robert Schuster <r.schuster@tarent.de>
 *
 */
public interface ILoadUserConfiguration {
	
	List<Entity> getEntities();

	void execute();
}
