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
 *     Robert Schuster <r.schuster@tarent.de> - do not execute when internal server is used
 ******************************************************************************/
package sernet.gs.ui.rcp.main.service;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Preferences;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;
import sernet.gs.ui.rcp.main.service.taskcommands.GetChangesSince;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.model.common.ChangeLogEntry;
import sernet.verinice.model.common.CnATreeElement;

/**
 * Check transaction log on server for changes and notify listeners. Should be
 * called by a timer or by an event pushed from server.
 * 
 * @author koderman[at]sernet[dot]de
 * @version $Rev$ $LastChangedDate$ $LastChangedBy$
 * 
 */
// TODO server: implement server-side push of events (beacon service)
public class TransactionLogWatcher {
	
	private final Logger log = Logger.getLogger(TransactionLogWatcher.class);
	
	private Date lastChecked = null;

	/** ICommandService instance is injected by Spring. */
	private ICommandService commandService;

	public void checkLog() {
		Activator.inheritVeriniceContextState();

		if (!CnAElementFactory.isModelLoaded() && !CnAElementFactory.isIsoModelLoaded())
		{
			return;
		}

		// No need to do anything when the internal server is used as this
		// means that there is only one user.
		Preferences prefs = Activator.getDefault().getPluginPreferences();
		if (prefs.getString(PreferenceConstants.OPERATION_MODE).equals(PreferenceConstants.OPERATION_MODE_INTERNAL_SERVER)){
			return;
		}

		try {
			GetChangesSince command = new GetChangesSince(lastChecked, ChangeLogEntry.STATION_ID);
			command = commandService.executeCommand(command);

			lastChecked = command.getLastChecked();
			List<ChangeLogEntry> entries = command.getEntries();				
			if(entries==null || entries.isEmpty() ) {
    			if(log.isDebugEnabled()) {
    				log.debug("No changes");
    			}
			} else {
			    Collections.sort(entries);
    			for (ChangeLogEntry changeLogEntry : entries) {
    				Integer elementId = changeLogEntry.getElementId();
    				CnATreeElement changedElement = command.getChangedElements().get(elementId);
    				process(changeLogEntry, changedElement);
    			}
			}

		} catch (CommandException e) {
			log.error("Fehler bei Abfrage des Transaktionslogfiles.", e);
		}
	}

	/**
	 * @param changeLogEntry
	 * @param changedElement
	 */
	private void process(ChangeLogEntry changeLogEntry, CnATreeElement changedElement) {
		int changetype = changeLogEntry.getChange();
		if (log.isInfoEnabled()) {
			log.info("Processing change event type " + changeLogEntry.getChangeDescription() + " from user " + changeLogEntry.getUsername() + " for element " + changeLogEntry.getElementClass() + " / " + changeLogEntry.getElementId());
		}
		
	
		switch (changetype) {
			case ChangeLogEntry.TYPE_UPDATE:
				CnAElementFactory.getModel(changedElement).databaseChildChanged(changedElement);
				break;
	
			case ChangeLogEntry.TYPE_INSERT:
			    if(changedElement!=null) {
			        CnAElementFactory.getModel(changedElement).databaseChildAdded(changedElement);
			        CnAElementFactory.getModel(changedElement).childAdded(changedElement.getParent(), changedElement);
			    }
			    break;
			case ChangeLogEntry.TYPE_DELETE:
				if (changedElement == null) {
					// element no longer retrievable, notify by ID:
					CnAElementFactory.databaseChildRemoved(changeLogEntry);
				} else {
					// element was retrieved before deletion took place, notify
					// using element itself:
					CnAElementFactory.getModel(changedElement).databaseChildRemoved(changedElement);
				}
	
				break;
			case ChangeLogEntry.TYPE_PERMISSION:
				// Changes to the permissions are potentially disruptive (items may
				// be invisible now etc). As such reload everything.
	
				CnAElementFactory.getInstance().reloadModelFromDatabase();
				break;
			default:
			    //ignore (but debug) other change types
				Logger.getLogger(this.getClass()).debug("Unrecognized change type received from server. Ignored, type ID was: " + changetype);
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
