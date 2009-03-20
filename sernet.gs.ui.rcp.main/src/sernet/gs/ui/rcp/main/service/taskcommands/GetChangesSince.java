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
package sernet.gs.ui.rcp.main.service.taskcommands;

import java.io.Serializable;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sernet.gs.ui.rcp.main.common.model.ChangeLogEntry;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.common.model.HydratorUtil;
import sernet.gs.ui.rcp.main.connect.IBaseDao;
import sernet.gs.ui.rcp.main.service.commands.CommandException;
import sernet.gs.ui.rcp.main.service.commands.GenericCommand;
import sernet.gs.ui.rcp.main.service.commands.RuntimeCommandException;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadPolymorphicCnAElementById;
import sernet.hui.common.connect.Entity;

/**
 * Get list of changes from transaction log.
 * 
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public class GetChangesSince extends GenericCommand {

	private static final String QUERY = "from ChangeLogEntry entry " + 
		"where entry.changetime > ? " + 
		"and entry.stationId != ?";
	
	
	private Date lastChecked;
	private String stationId;

	private List<ChangeLogEntry> entries;
	private Map<Integer, CnATreeElement> changedElements;

	/**
	 * New query for changes.
	 * 
	 * @param lastChecked only get changes after this timestamp
	 * @param stationId filter out / remove changes for this client (self)
	 */
	public GetChangesSince(Date lastChecked, String stationId) {
		this.stationId = stationId;
		this.lastChecked = lastChecked;
	}

	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.service.commands.ICommand#execute()
	 */
	public void execute() {
		/* save date now before query is executed, to ensure that overlapping new entries made 
		 * by another thread will definitely be included in next query:
		 * (the alternative would be to save the date after the query and possibly
		 * loose changes made between execution of the query and setting of the date)
		 */
		Date now = GregorianCalendar.getInstance().getTime();
		
		// client has never checked the log, start from now:
		if (lastChecked == null) {
			lastChecked = now;
		}
		
		IBaseDao<ChangeLogEntry, Serializable> dao = getDaoFactory().getDAO(ChangeLogEntry.class);
		entries = dao.findByQuery(QUERY, new Object[] {lastChecked, stationId});
		lastChecked = now;
	
		try { if entries' elementid are null, IDs will be null (check insert bausteinumsetzung, see screenshots)
			hydrateChangedItems(entries);
		} catch (CommandException e) {
			throw new RuntimeCommandException(e);
		}
	}

	/**
	 * @param entries2
	 * @throws CommandException 
	 */
	private void hydrateChangedItems(List<ChangeLogEntry> entries2) throws CommandException {
		if (entries2.size()<1)
			return;
		
		Integer[] IDs = new Integer[entries2.size()];
		changedElements = new HashMap<Integer, CnATreeElement>(entries2.size());
		
		int i=0;
		for (ChangeLogEntry logEntry : entries2) {
			IDs[i] = logEntry.getElementId();
			++i;
		}
		
		LoadPolymorphicCnAElementById command = new LoadPolymorphicCnAElementById(IDs);
		command = getCommandService().executeCommand(command);
		
		List<CnATreeElement> elements = command.getElements();
		for (CnATreeElement elmt : elements) {
			changedElements.put(elmt.getDbId(), elmt);
		}
	}

	public Date getLastChecked() {
		return lastChecked;
	}

	public List<ChangeLogEntry> getEntries() {
		return entries;
	}

	public Map<Integer, CnATreeElement> getChangedElements() {
		return changedElements;
	}


}
