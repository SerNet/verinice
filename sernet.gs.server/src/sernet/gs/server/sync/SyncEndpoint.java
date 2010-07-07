/*******************************************************************************
 * SyncEndpoint.java
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
 * 18.07.2009
 * 
 * @author Andreas Becker
 ******************************************************************************/
package sernet.gs.server.sync;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.springframework.ws.server.endpoint.AbstractJDomPayloadEndpoint;

import sernet.gs.server.ServerInitializer;
import sernet.gs.service.RuntimeCommandException;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.crudcommands.SyncDeleteCommand;
import sernet.gs.ui.rcp.main.service.crudcommands.SyncInsertUpdateCommand;
import sernet.gs.ui.rcp.main.sync.InternalErrorException;
import sernet.gs.ui.rcp.main.sync.InvalidRequestException;
import sernet.gs.ui.rcp.main.sync.SyncNamespaceUtil;
import sernet.verinice.interfaces.CommandException;

public class SyncEndpoint extends AbstractJDomPayloadEndpoint
{
	/*********************************************
	 * invokeInternal()
	 * 
	 * this method ist called, whenever an incoming
	 * request has been mapped to this endpoint.
	 * 
	 * @param requestElement
	 * @return Element to be the response payload
	 * @throws Exception
	 ********************************************/
	protected Element invokeInternal(Element requestElement) throws InternalErrorException, InvalidRequestException
	{
		try
		{
			// retreive <syncData> and <syncMapping> from request payload:
			Element syncDataElement = requestElement.getChild( "syncData", SyncNamespaceUtil.DATA_NS );
			Element syncMappingElement = requestElement.getChild( "syncMapping", SyncNamespaceUtil.MAPPING_NS );
			
			if( null == syncDataElement || null == syncMappingElement )
				throw new InvalidRequestException( "Konnte syncData und/oder syncMapping nicht finden!" );
			
			// default sync-flags, according to sync.xsd:
			boolean insert = true;
			boolean update = true; 
			boolean delete = false;

			// retreive flags from request payload:
			if( null != requestElement.getAttributeValue( "insert" ) )
				insert = Boolean.valueOf( requestElement.getAttributeValue( "insert" ) );
			if( null != requestElement.getAttributeValue( "update" ) )
				update = Boolean.valueOf( requestElement.getAttributeValue( "update" ) );
			if( null != requestElement.getAttributeValue( "delete" ) )
				delete = Boolean.valueOf( requestElement.getAttributeValue( "delete" ) );

			// we will count, how many objects have been inserted, updated or deleted:
			int inserted=0, updated=0, deleted=0;
			List<String> errorList = new ArrayList<String>();

			String sourceId = requestElement.getAttributeValue( "sourceId" );

			/*****************************************
			 * INSERT & UPDATE:
			 *****************************************/
			
			SyncInsertUpdateCommand cmdInsertUpdate = new SyncInsertUpdateCommand( sourceId, syncDataElement, syncMappingElement, insert, update, errorList );
			
			// editiert 
			ServerInitializer.inheritVeriniceContextState();
			// e 
			
			cmdInsertUpdate = ServiceFactory.lookupCommandService().executeCommand( cmdInsertUpdate );
			
			inserted += cmdInsertUpdate.getInserted();
			updated += cmdInsertUpdate.getUpdated();
			
			/*****************************************
			 * DELETE:
			 *****************************************/
			
			if( delete )
			{
				SyncDeleteCommand cmdDelete = new SyncDeleteCommand( sourceId, syncDataElement, errorList );
				
				
				
				cmdDelete = ServiceFactory.lookupCommandService().executeCommand( cmdDelete );
				
				deleted += cmdDelete.getDeleted();
			}
		 
			/*****************************************
			 * Build response element, send to client:
			 *****************************************/
			
			Element responseElement = new Element( "syncResponse", SyncNamespaceUtil.SYNC_NS );
			Element replyMsgElement = new Element( "replyMessage", SyncNamespaceUtil.SYNC_NS );
			
			if( errorList.isEmpty() )
				replyMsgElement.setText( "Synchronisation erfolgreich." );
			else
			{
				for( String s : errorList )
				{
					replyMsgElement.setText( replyMsgElement.getText() + s + '\n' );
				}
			}
			
			responseElement.addContent( replyMsgElement );
			responseElement.addContent( new Element( "inserted", SyncNamespaceUtil.SYNC_NS ).setText( new Integer( inserted  ).toString() ) );
			responseElement.addContent( new Element( "updated", SyncNamespaceUtil.SYNC_NS ).setText( new Integer( updated  ).toString() ) );
			responseElement.addContent( new Element( "deleted", SyncNamespaceUtil.SYNC_NS ).setText( new Integer( deleted  ).toString() ) );
			
			return responseElement;
		}
		catch (RuntimeCommandException e) {
			Logger.getLogger( this.getClass() ).error( "Laufzeit-Fehler", e );
		}
		catch( CommandException e ){
			Logger.getLogger( this.getClass() ).error( "Laufzeit-Fehler", e );
		}
		
		return null;
	}
}
