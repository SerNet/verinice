/*******************************************************************************
 * Copyright (c) 2009 Andreas Becker <andreas.r.becker@rub.de>.
 * Copyright (c) 2010 Robert Schuster <r.schuster@tarent.de>
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
 *     Andreas Becker <andreas.r.becker[at]rub[dot]de> - initial API and implementation
 *     Robert Schuster <r.schuster[a]tarent[dot]de> - conversion to verinice command
 ******************************************************************************/
package sernet.gs.ui.rcp.main.sync.commands;

import java.util.ArrayList;
import java.util.List;

import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import de.sernet.sync.data.SyncData;
import de.sernet.sync.mapping.SyncMapping;
import de.sernet.sync.sync.SyncRequest;

@SuppressWarnings("serial")
public class SyncCommand extends GenericCommand
{
	private String sourceId;
	
	private boolean insert, update, delete;
	
	private SyncData syncData;
	
	private SyncMapping syncMapping;
	
	private int inserted, updated, deleted;
	
	private List<String> errors = new ArrayList<String>();
	
	public SyncCommand(SyncRequest sr)
	{
		this.sourceId = sr.getSourceId();
		
		this.insert = sr.isInsert();
		this.update = sr.isUpdate();
		this.delete = sr.isDelete();
		
		this.syncData = sr.getSyncData();
		this.syncMapping = sr.getSyncMapping();
	}
	
	@Override
	public void execute()
	{
		SyncInsertUpdateCommand cmdInsertUpdate =
			new SyncInsertUpdateCommand(sourceId,
					syncData, syncMapping,
					insert, update, errors);

		try {
			cmdInsertUpdate = getCommandService().executeCommand(cmdInsertUpdate);
		} catch (CommandException e) {
			errors.add("Insert/Update failed.");
			return;
		}
		
		inserted += cmdInsertUpdate.getInserted();
		updated += cmdInsertUpdate.getUpdated();

		if( delete )
		{
			SyncDeleteCommand cmdDelete = new SyncDeleteCommand( sourceId, syncData, errors);
			
			try {
				cmdDelete = getCommandService().executeCommand(cmdDelete);
			} catch (CommandException e) {
				errors.add("Delete failed.");
				return;
			}
			
			deleted += cmdDelete.getDeleted();
		}

	}

	public int getInserted()
	{
		return inserted;
	}
	
	public int getUpdated()
	{
		return updated;
	}
	
	public int getDeleted()
	{
		return deleted;
	}
	
	public List<String> getErrors()
	{
		return errors;
	}
	
}
