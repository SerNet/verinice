/*******************************************************************************
 * InsertUpdateCommand.java
 *
 * Copyright (c) 2009 Andreas Becker <andreas.r.becker@rub.de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 *
 * 14.08.2009
 * 
 * @author Andreas Becker
 ******************************************************************************/
package sernet.gs.ui.rcp.main.service.crudcommands;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.jdom.Element;

import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.sync.SyncNamespaceUtil;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.model.bsi.ITVerbund;
import sernet.verinice.model.common.CnATreeElement;

@SuppressWarnings("serial")
public class SyncDeleteCommand extends GenericCommand
{	
	/* Since we are possibly instanciating objects from
	 * verinice business classes without having access to
	 * a tree with categories as parent nodes, we have to
	 * map huientitytype --> category manually:		*/
	
	private String sourceId;
	private Element syncDataElement;
	private List<String> errorList;
	
	private int deleted = 0;
	
	public int getDeleted()
	{
		return deleted;
	}
	
	public SyncDeleteCommand( String sourceId, Element syncDataElement, List<String> errorList )
	{	
		this.sourceId = sourceId;
		this.syncDataElement = syncDataElement;
		this.errorList = errorList;
	}

	/************************************************************
	 * methodName()
	 * 
	 * Search for objects within database, which have previously
	 * been synced from the given sourceId, but not listed any more.
	 * Delete those objects from the database!
	 * 
	 * Be VERY CAREFUL with this command, since it DELETES STUFF!!
	 * This should only be used, if the delete-flag has been explicitely
	 * set (default: false) by the user within the sync process...
	 * 
	 ************************************************************/
	public void execute()
	{
		LoadCnAElementsBySourceID command = new LoadCnAElementsBySourceID( sourceId );
		
		try
		{
			command = ServiceFactory.lookupCommandService().executeCommand( command );
		}
		catch ( CommandException e )
		{
			errorList.add( "Fehler beim Ausführen von LoadCnAElementsBySourceID mit der sourceId = " + sourceId );
			e.printStackTrace();
			return;
		}
		
		List<CnATreeElement> dbElements = command.getElements();
		
		// create a hash map, which contains a token for all
		// extId's which are present in the sync Data:
		HashMap<String,Object> currentExtIds = new HashMap<String, Object>();
		Object exists = new Object();
		
		Iterator iter = syncDataElement.getChildren( "syncObject", SyncNamespaceUtil.DATA_NS ).iterator();
		
		// store a token for the extId of every <syncObject> in the sync data:
		while( iter.hasNext() )
		{
			Element obj = (Element) iter.next();
			currentExtIds.put( obj.getAttributeValue( "extId" ), exists );
			//currentExtIds.put( obj.getAttributeValue( "externalId" ), exists );
		}
		
		// find objects in the db, which have been synched from
		// this sourceId in the past, but missing in the current list:
		allElements: for( CnATreeElement dbElement : dbElements )
		{
			
			if (dbElement instanceof ITVerbund)
				continue allElements;
			
			Object elementExists = currentExtIds.get( dbElement.getExtId() );
			
			if( null == elementExists ) // delete this object from the database:
			{
				RemoveElement cmdRemove = new RemoveElement( dbElement );
				
				try
				{
					cmdRemove = ServiceFactory.lookupCommandService().executeCommand( cmdRemove );
					deleted++;
				}
				catch ( CommandException e )
				{
					errorList.add( "Konnte Objekt ( id=" + dbElement.getId() + ", externalId=" + dbElement.getExtId() + ") nicht löschen." );
					e.printStackTrace();
				}
			}
		}
	}
}
