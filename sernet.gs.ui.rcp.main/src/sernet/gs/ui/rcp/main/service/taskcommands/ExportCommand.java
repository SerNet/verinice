/*******************************************************************************
 * Copyright (c) 2010 Andreas Becker <andreas[at]becker[dot]name>.
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
 *     Andreas Becker <andreas[at]becker[dot]name> - initial API and implementation
 ******************************************************************************/

package sernet.gs.ui.rcp.main.service.taskcommands;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.service.commands.GenericCommand;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.hui.common.connect.PropertyType;

/**
 * Creates an XML representation of the given list of
 * CnATreeElements.
 * 
 * NOTE: This has not been tested yet! Work in progress!
 * 
 * @author <andreas[at]becker[dot]name>
 */
public class ExportCommand extends GenericCommand
{
	// TODO: Use NamespaceUtil, when available!
	public static HashMap<String, String> syncNamespaces = new HashMap<String, String>();
	
	static
	{
		syncNamespaces.put("sync", "http://www.sernet.de/sync/sync");
		syncNamespaces.put("data", "http://www.sernet.de/sync/data");
		syncNamespaces.put("map", "http://www.sernet.de/sync/mapping");
	}
	
	private Set<CnATreeElement> elements;
	private String sourceId;
	private Document exportDocument;

	/**
	 * @param elements
	 */
	public ExportCommand( Set<CnATreeElement> elements, String sourceId )
	{
		this.elements = elements;
		this.sourceId = sourceId;
	}
	
	@Override
	public void execute()
	{
		/*+++++
		 * Create empty DOM-tree from scratch:
		 *++++++++++++++++++++++++++++++++++++*/
		try
		{
			this.exportDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		}
		catch (ParserConfigurationException e)
		{
			throw new RuntimeException(e);
		}
		
		Element syncRequest = exportDocument.createElementNS(syncNamespaces.get("sync"), "syncRequest");
		syncRequest.setAttribute("sourceId", sourceId);
		Element syncData = exportDocument.createElementNS(syncNamespaces.get("data"), "syncData");
		Element syncMapping = exportDocument.createElementNS(syncNamespaces.get("map"), "syncMapping");
		syncRequest.appendChild(syncData);
		syncRequest.appendChild(syncMapping);
		
		List<Element> syncObjects = new LinkedList<Element>();
		
		/*+++++
		 * Process all CnATreeElements:
		 *+++++++++++++++++++++++++++++*/
		for( CnATreeElement element : elements )
		{
			syncObjects.addAll( export(element) );
		}
		
		ListIterator<Element> iter = syncObjects.listIterator();
		
		while( iter.hasNext() )
		{
			syncData.appendChild(iter.next());
		}
	}

	/**
	 * Export (i.e. "create XML representation of" the given cnATreeElement
	 * and its successors. For this, child elements are exported recursively.
	 * All elements that have been processed are returned as a list of
	 * {@code syncObject}s with their respective attributes, represented
	 * as {@code syncAttribute}s.
	 * 
	 * @param cnATreeElement
	 * @return List<Element>
	 */
	private List<Element> export( CnATreeElement cnATreeElement )
	{
		List<Element> syncObjects = new LinkedList<Element>();
		
		Element syncObject = exportDocument.createElementNS(syncNamespaces.get("data"), "syncObject");
		syncObject.setAttribute("extId", cnATreeElement.getId());
		syncObject.setAttribute("extObjectType", cnATreeElement.getObjectType());
		
		/*+++++
		 * Retrieve all properties from the entity:
		 *+++++++++++++++++++++++++++++++++++++++++*/
		List<PropertyType> possibleProperties = HUITypeFactory.getInstance().getEntityType(cnATreeElement.getObjectType()).getPropertyTypes();
		ListIterator<PropertyType> iter = possibleProperties.listIterator();
		
		while(iter.hasNext())
		{
			PropertyType propertyType = iter.next();
			String propertyValue = cnATreeElement.getEntity().getSimpleValue(propertyType.getId());
			
			if( propertyValue != null )
			{
				// Add <syncAttribute> to this <syncObject>:
				Element syncAttribute = exportDocument.createElementNS(syncNamespaces.get("data"), "syncAttribute");
				syncAttribute.setAttribute("name", propertyType.getId());
				syncAttribute.setAttribute("value", propertyValue);
				syncObject.appendChild(syncAttribute);
			}
		}
		
		syncObjects.add( syncObject );
		
		/*++++
		 * Handle children recursively:
		 *+++++++++++++++++++++++++++++*/
		Set<CnATreeElement> children = cnATreeElement.getChildren();
		
		for( CnATreeElement child : children )
		{
			syncObjects.addAll( export(child) );
		}
		
		return syncObjects;
	}
	
	/* Getters and Setters: */
	
	public Document getExportDocument()
	{
		return exportDocument;
	}
}
