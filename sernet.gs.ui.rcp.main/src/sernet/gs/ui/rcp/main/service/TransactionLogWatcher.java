/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak@sernet.de>.
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
 *     Alexander Koderman <ak@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.service;

import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import sernet.gs.ui.rcp.main.common.model.ChangeLogEntry;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.service.commands.CommandException;
import sernet.gs.ui.rcp.main.service.taskcommands.GetChangesSince;

/**
 * Check transaction log on server for changes and notify listeners. Should be
 * called by a timer or by an event pushed from server.
 * 
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ $LastChangedBy$
 * 
 */
// TODO server: implement server-side push of events (beacon service)
public class TransactionLogWatcher {
	private Date lastChecked = null;
	
	/** ICommandService instance is injected by Spring. */
	private ICommandService commandService;

	public void checkLog() {
		if (!CnAElementFactory.isModelLoaded())
			return;

//		Logger.getLogger(this.getClass()).debug("Checking transaction log...");
		
		try {
			GetChangesSince command = new GetChangesSince(lastChecked,
					ChangeLogEntry.STATION_ID);
			command = commandService.executeCommand(
					command);

			lastChecked = command.getLastChecked();
			List<ChangeLogEntry> entries = command.getEntries();
			for (ChangeLogEntry changeLogEntry : entries) {
				Integer elementId = changeLogEntry.getElementId();
				CnATreeElement changedElement = command.getChangedElements().get(elementId);
				process(changeLogEntry, changedElement);
			}

		} catch (CommandException e) {
			Logger.getLogger(this.getClass()).error(
					"Fehler bei Abfrage des Transaktionslogfiles.", e);
		}
	}

	/**
	 * @param changeLogEntry
	 * @param changedElement 
	 */
	private void process(ChangeLogEntry changeLogEntry, CnATreeElement changedElement) {
		int changetype = changeLogEntry.getChange();
		Logger.getLogger(this.getClass()).debug("Processing change event from user " 
				+ changeLogEntry.getUsername() + " for element " + changeLogEntry.getElementClass() 
				+ " / " + changeLogEntry.getElementId());

		switch (changetype) {
		case ChangeLogEntry.TYPE_UPDATE:
			CnAElementFactory.getLoadedModel().databaseChildChanged(
					changedElement);
			break;

		case ChangeLogEntry.TYPE_INSERT:
			CnAElementFactory.getLoadedModel().databaseChildAdded(
					changedElement);

			break;
		case ChangeLogEntry.TYPE_DELETE:
			if (changedElement == null) {
				// element no longer retrievable, notify by ID:
				CnAElementFactory.getLoadedModel().databaseChildRemoved(
						changeLogEntry);
			}
			else {
				// element was retrieved before deletion took place, notify using element itself:
				CnAElementFactory.getLoadedModel().databaseChildRemoved(
						changedElement);
			}

			break;

		default:
			Logger.getLogger(this.getClass()).debug("Unrecognized change type received from server.");
			break;
		}
	}

	public void setCommandService(ICommandService commandService) {
		this.commandService = commandService;
	}

	public ICommandService getCommandService() {
		return commandService;
	}

}
