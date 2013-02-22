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
package sernet.gs.ui.rcp.main.common.model;

import java.util.List;

import sernet.verinice.model.common.ChangeLogEntry;

/**
 * Class to check for model changes made by other clients.
 * 
 * This is a simple synchronisation mechanism to use until
 * we have an application server to notify clients of changes. 
 * 
 * @author koderman[at]sernet[dot]de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
// TODO implement notification service for clients
public final class ChangeLogWatcher {

	private static ChangeLogWatcher instance;
	
	private ChangeLogWatcher() {
	}
	
	public static synchronized ChangeLogWatcher getInstance() {
		if (instance == null){
			instance = new ChangeLogWatcher();
		}
		return instance;
	}
	
	
	/**
	 * Get all changes made by another client since our last known
	 * timestamp.
	 * 
	 * @return
	 */
	public List<ChangeLogEntry> getNewChanges() {
		return null;
	}

	/**
	 * Go through transaction log and refresh every object that has been changed
	 * by another client.
	 * @throws Exception 
	 */
	public void updateChanges(Object watchOutFor) throws ObjectDeletedException {
	}
	
	public void refresh(ChangeLogEntry change) {
	}
}
