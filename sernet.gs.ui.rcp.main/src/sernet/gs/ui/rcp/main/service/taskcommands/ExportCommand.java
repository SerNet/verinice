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

import java.io.Serializable;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import sernet.gs.ui.rcp.main.common.model.HydratorUtil;
import sernet.gs.ui.rcp.main.connect.RetrieveInfo;
import sernet.hui.common.connect.EntityType;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.hui.common.connect.PropertyList;
import sernet.hui.common.connect.PropertyType;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.bsi.BausteinUmsetzung;
import sernet.verinice.model.common.CnATreeElement;

/**
 * Creates an XML representation of the given list of
 * CnATreeElements.
 * 
 * @author <andreas[at]becker[dot]name>
 */
public class ExportCommand extends GenericCommand
{
	private static final long serialVersionUID = 821504393526786830L;
	
	// TODO: Use NamespaceUtil, when available!
	public static HashMap<String, String> syncNamespaces = new HashMap<String, String>();
	
	// Blacklist of object types that should not be exported as an object:
	private static HashMap<String, String> blacklist = new HashMap<String, String>();
	
	/*+++
	 * List of already-exported objects' IDs, to
	 * prevent multiple inclusion of a single object
	 * in <syncData>, e.g. due to explicitly selecting
	 * a father element and one of its successors:
	 *++++++++++++++++++++++++++++++++++++++++++++++++*/
	private HashMap<String, String> exportedObjectIDs = new HashMap<String, String>();
	
	static
	{
		blacklist.put("it-verbund", new String());
		blacklist.put("raeume-kategorie", new String());
		blacklist.put("nk-kategorie", new String());
		blacklist.put("server-kategorie", new String());
		blacklist.put("personen-kategorie", new String());
		blacklist.put("gebaeude-kategorie", new String());
		blacklist.put("clients-kategorie", new String());
		blacklist.put("anwendungen-kategorie", new String());
		blacklist.put("nk-kategorie", new String());
		blacklist.put("sonstige-it-kategorie", new String());
		blacklist.put("tk-kategorie", new String());
		
		syncNamespaces.put("sync", "http://www.sernet.de/sync/sync");
		syncNamespaces.put("data", "http://www.sernet.de/sync/data");
		syncNamespaces.put("map", "http://www.sernet.de/sync/mapping");
	}
	
	private List<CnATreeElement> elements;
	private String sourceId;
	private HashMap<String,String> entityTypesToBeExported;
	private Document exportDocument;

	public ExportCommand( List<CnATreeElement> elements, String sourceId )
	{
		this.elements = elements;
		this.sourceId = sourceId;
	}
	
	public ExportCommand( List<CnATreeElement> elements, String sourceId, HashMap<String,String> entityTypesToBeExported )
	{
		this( elements, sourceId );
		this.entityTypesToBeExported = entityTypesToBeExported; 
	}
	
	public void execute()
	{
		exportedObjectIDs = new HashMap<String, String>();
		
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
		
		exportDocument.appendChild(syncRequest);
		
		List<Element> syncObjects = new LinkedList<Element>();
		
		String timestamp = Long.toString(Calendar.getInstance().getTimeInMillis());
		
		/*+++++
		 * Add one <syncObject> element for each
		 * given CnATreeElement to <syncData>: 
		 *+++++++++++++++++++++++++++++++++++++++*/
		for( CnATreeElement element : elements )
		{
			syncObjects.addAll(export(element, timestamp));
		}
		
		ListIterator<Element> iter = syncObjects.listIterator();
		
		while( iter.hasNext() )
		{
			syncData.appendChild(iter.next());
		}
		
		/*+++++
		 * Add Sync Mapping, which in our case describes
		 * the identity map, i.e. it maps every object type
		 * and every attribute type to itself. 
		 *+++++++++++++++++++++++++++++++++++++++++++++++++*/
		
		Collection<EntityType> entityTypes = HUITypeFactory.getInstance().getAllEntityTypes();
		Iterator<EntityType> entityTypesIter = entityTypes.iterator();
		
		while( entityTypesIter.hasNext() )
		{
			EntityType entityType = entityTypesIter.next();
			
			if(!(entityType.getId().equals("itverbund"))) //$NON-NLS-1$
			{
				// Add <mapObjectType> element for this entity type to <syncMapping>:
				Element mapObjectType = exportDocument.createElementNS(syncNamespaces.get("map"), "mapObjectType");
				syncMapping.appendChild(mapObjectType);
				mapObjectType.setAttribute("extId", entityType.getId());
				mapObjectType.setAttribute("intId", entityType.getId());
				
				List<PropertyType> propertyTypes = entityType.getPropertyTypes();
				Iterator<PropertyType> propertyTypesIter = propertyTypes.iterator();
				
				while( propertyTypesIter.hasNext() )
				{
					PropertyType propertyType = propertyTypesIter.next();
					
					// Add <mapAttributeType> for this property type to current <mapObjectType>:
					Element mapAttributeType = exportDocument.createElementNS(syncNamespaces.get("map"), "mapAttributeType");
					mapObjectType.appendChild(mapAttributeType);
					mapAttributeType.setAttribute("extId", propertyType.getId());
					mapAttributeType.setAttribute("intId", propertyType.getId());
				}
			}
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
	private List<Element> export( CnATreeElement cnATreeElement, String timestamp )
	{		
		hydrate( cnATreeElement );
		List<Element> syncObjects = new LinkedList<Element>();
		
		/*++++++++++
		 * Export the given CnATreeElement, iff it is NOT blacklisted (i.e. an IT network
		 * or category element) AND, if we should restrict the exported objects to certain
		 * entity types, this element's entity type IS allowed:
		 *++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/
		
		if ((blacklist.get(cnATreeElement.getObjectType()) == null)
				&& (exportedObjectIDs.get( cnATreeElement.getId()) == null )
				&& (entityTypesToBeExported == null || entityTypesToBeExported.get(cnATreeElement.getObjectType()) != null) )
		{
			Element syncObject = exportDocument.createElementNS(syncNamespaces.get("data"), "syncObject");
			syncObject.setAttribute("extId", cnATreeElement.getId());
			syncObject.setAttribute("extObjectType", cnATreeElement.getObjectType());
			syncObject.setAttribute("timestamp", timestamp);

			/*+++++
			 * Retrieve all properties from the entity:
			 *+++++++++++++++++++++++++++++++++++++++++*/
			
			Map<String, PropertyList> properties = cnATreeElement.getEntity().getTypedPropertyLists();

			for( String s : properties.keySet() )
			{
				String propertyValue = cnATreeElement.getEntity().getSimpleValue(s);

				if( propertyValue != null )
				{
					// Add <syncAttribute> to this <syncObject>:
					Element syncAttribute = exportDocument.createElementNS(syncNamespaces.get("data"), "syncAttribute");
					syncAttribute.setAttribute("name", s);
					syncAttribute.setAttribute("value", propertyValue);
					syncObject.appendChild(syncAttribute);
				}			
			}

			syncObjects.add( syncObject );
			exportedObjectIDs.put( cnATreeElement.getId(), new String() );
		}
		
		/*++++
		 * Handle children recursively:
		 *+++++++++++++++++++++++++++++*/
		Set<CnATreeElement> children = cnATreeElement.getChildren();
		
		for( CnATreeElement child : children )
		{
			syncObjects.addAll( export(child, timestamp) );
		}
		
		return syncObjects;
	}
	
	/*************************************************
	 * Hydrate {@code element}, including all of its
	 * successor elements and properties.
	 * 
	 * @param element
	 *************************************************/
	private void hydrate(CnATreeElement element)
	{
		if (element == null)
			return;
		
		IBaseDao<? extends CnATreeElement, Serializable> dao = getDaoFactory().getDAOforTypedElement(element);
		//element = dao.retrieve(element.getDbId(), RetrieveInfo.getPropertyChildrenInstance());
		
		HydratorUtil.hydrateElement( dao, element, true);
		HydratorUtil.hydrateEntity( dao, element);
		
		Set<CnATreeElement> children = element.getChildren();
		for (CnATreeElement child : children)
		{
			if (child instanceof BausteinUmsetzung)
			{
				// next element:
				continue;
			}
			
			hydrate(child);
		}
	}
	
	/* Getters and Setters: */
	
	public Document getExportDocument()
	{
		return exportDocument;
	}
}
