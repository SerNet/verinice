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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.xml.bind.DataBindingException;
import javax.xml.bind.JAXB;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.namespace.QName;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.Status;

import org.apache.log4j.Logger;

import sernet.gs.service.RetrieveInfo;
import sernet.gs.service.VeriniceCharset;
import sernet.gs.ui.rcp.main.common.model.HydratorUtil;
import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.EntityType;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.hui.common.connect.Property;
import sernet.hui.common.connect.PropertyList;
import sernet.hui.common.connect.PropertyType;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.bsi.BausteinUmsetzung;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import de.sernet.sync.data.SyncData;
import de.sernet.sync.data.SyncLink;
import de.sernet.sync.data.SyncObject;
import de.sernet.sync.data.SyncObject.SyncAttribute;
import de.sernet.sync.mapping.SyncMapping;
import de.sernet.sync.mapping.SyncMapping.MapObjectType;
import de.sernet.sync.mapping.SyncMapping.MapObjectType.MapAttributeType;
import de.sernet.sync.sync.SyncRequest;

/**
 * Creates an XML representation of the given list of
 * CnATreeElements.
 * 
 * @author <andreas[at]becker[dot]name>
 */
@SuppressWarnings("serial")
public class ExportCommand extends GenericCommand
{
    private transient Logger log = Logger.getLogger(ExportCommand.class);

    public Logger getLog() {
        if (log == null) {
            log = Logger.getLogger(ExportCommand.class);
        }
        return log;
    }
    
    private transient CacheManager manager = null;
    private String cacheId = null;
    private transient Cache cache = null;

    transient private IBaseDao<CnATreeElement, Serializable> dao;
    
	/*+++
	 * List of already-exported objects' IDs, to
	 * prevent multiple inclusion of a single object
	 * in <syncData>, e.g. due to explicitly selecting
	 * a father element and one of its successors:
	 *++++++++++++++++++++++++++++++++++++++++++++++++*/
	private HashMap<String, String> exportedObjectIDs = new HashMap<String, String>();
	
	private transient List<SyncObject> orphaneList;

	private String sourceId;
	private List<CnATreeElement> elements;
	private Set<CnALink> linkSet;
	
	private HashMap<String,String> entityTypesToBeExported;
	
	private byte[] result;
	
	public ExportCommand( List<CnATreeElement> elements, String sourceId )
	{
		this.elements = elements;
		this.sourceId = sourceId;
		this.linkSet = new HashSet<CnALink>();
	}
	
	public ExportCommand( List<CnATreeElement> elements, String sourceId, HashMap<String,String> entityTypesToBeExported )
	{
		this( elements, sourceId );
		this.entityTypesToBeExported = entityTypesToBeExported;
        this.linkSet = new HashSet<CnALink>();
	}
	
	public void execute()
	{
	    getCache().removeAll();
		String timestamp = Long.toString(Calendar.getInstance().getTimeInMillis());
		exportedObjectIDs = new HashMap<String, String>();
		
		SyncData sd = new SyncData();
		
		SyncMapping sm = new SyncMapping();
		List<MapObjectType> mapObjectTypeList = sm.getMapObjectType();
		
		SyncRequest sr = new SyncRequest();
		sr.setSourceId(sourceId);
		sr.setSyncData(sd);
		sr.setSyncMapping(sm);
		
		/** 
		 * A list for objects whose parent object is not in the exported set.
		 * 
		 * The orphanes are added last as top-level elements to the SyncData
		 * object.
		 */
		orphaneList = new ArrayList<SyncObject>();

		/*+++++
		 * Add one <syncObject> element for each
		 * given CnATreeElement to <syncData>: 
		 *+++++++++++++++++++++++++++++++++++++++*/
		Set<EntityType> exportedEntityTypes = new HashSet<EntityType>();
		Set<String> exportedTypes = new HashSet<String>();
		for( CnATreeElement element : elements ) {	    
			export(sd.getSyncObject(), element, timestamp, exportedEntityTypes, exportedTypes);
		}
		sd.getSyncObject().addAll(orphaneList);
		
		for(CnALink link : linkSet) {
		    export(sd.getSyncLink(),link);
		}
		
		/* Adds SyncMapping for all EntityTypes that have been exported. This
		 * is going to be an identity mapping.
		 */
		for (EntityType entityType : exportedEntityTypes)
		{
			if(entityType!=null) {
    			// Add <mapObjectType> element for this entity type to <syncMapping>:
    			MapObjectType mapObjectType = new MapObjectType();
    			
    			mapObjectType.setIntId(entityType.getId());
    			mapObjectType.setExtId(entityType.getId());
    			
    			List<MapAttributeType> mapAttributeTypes = mapObjectType.getMapAttributeType();
    			for (PropertyType propertyType : entityType.getAllPropertyTypes())
    			{
    				// Add <mapAttributeType> for this property type to current <mapObjectType>:
    				MapAttributeType mapAttributeType = new MapAttributeType();
    				
    				mapAttributeType.setExtId(propertyType.getId());
    				mapAttributeType.setIntId(propertyType.getId());
    				
    				mapAttributeTypes.add(mapAttributeType);
    			}
    			
    			mapObjectTypeList.add(mapObjectType);
			}
		}
		
		// For all exported objects that had no entity type (e.g. category objects)
		// we create a simple 1-to-1 mapping using their type id.
		for (String typeId : exportedTypes)
		{
			MapObjectType mapObjectType = new MapObjectType();
			mapObjectType.setIntId(typeId);
			mapObjectType.setExtId(typeId);
			
			mapObjectTypeList.add(mapObjectType);
		}
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		marshal(sr, bos);
		
		result = bos.toByteArray();
	}
	
	private void marshal( Object jaxbObject, OutputStream os ) {
        try {       
            JAXBContext context = JAXBContext.newInstance( jaxbObject.getClass() );
            Marshaller m = context.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,true);
            m.setProperty(Marshaller.JAXB_ENCODING,VeriniceCharset.CHARSET_UTF_8.name());
            m.marshal(jaxbObject, os);
        } catch (JAXBException e) {
            throw new DataBindingException(e);
        } 
    }

    /**
	 * Export (i.e. "create XML representation of" the given cnATreeElement
	 * and its successors. For this, child elements are exported recursively.
	 * All elements that have been processed are returned as a list of
	 * {@code syncObject}s with their respective attributes, represented
	 * as {@code syncAttribute}s.
	 * 
	 * @param element
	 * @return List<Element>
	 */
	private void export(List<SyncObject> list, CnATreeElement element, String timestamp, Set<EntityType> exportedEntityTypes, Set<String> exportedTypes)
	{		
	    element = hydrate( element );
		
		List<SyncObject> childList = null;
		
		/*++++++++++
		 * Export the given CnATreeElement, if it is NOT blacklisted (i.e. an IT network
		 * or category element) AND, if we should restrict the exported objects to certain
		 * entity types, this element's entity type IS allowed:
		 *++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/
		
		String typeId = element.getTypeId();
		if ((exportedObjectIDs.get( element.getId()) == null )
				&& (entityTypesToBeExported == null || entityTypesToBeExported.get(typeId) != null) )
		{
			SyncObject syncObject = new SyncObject();
			syncObject.setExtId(element.getId());
			syncObject.setExtObjectType(typeId);

			List<SyncAttribute> attributes = syncObject.getSyncAttribute();
			
			/*+++++
			 * Retrieve all properties from the entity:
			 *+++++++++++++++++++++++++++++++++++++++++*/
			
			Entity entity = element.getEntity();
			// Category instance may have no Entity attached to it
			// For those we do not store any property values.
			if (entity != null) {
				Map<String, PropertyList> properties = entity.getTypedPropertyLists();

				for (String s : properties.keySet()) {
					
					SyncAttribute syncAttribute = new SyncAttribute();
					// Add <syncAttribute> to this <syncObject>:
					syncAttribute.setName(s);
					
					int noOfValues = entity.exportProperties(s, syncAttribute.getValue());

					// Only if any value for the attribute could be found the whole
					// attribute instance is being added to the SyncObject's attribute
					// list.
					if (noOfValues > 0) {
						attributes.add(syncAttribute);
					}
				}
				
				// Add the elements EntityType to the set of exported EntityTypes in order to
				// use it for the mapping generation later on.
				exportedEntityTypes.add(element.getEntityType());
			}
			else
			{
				// Instance has no EntityType. This is fine but still some mapping
				// information is needed. We save the typeId for later then.
				exportedTypes.add(typeId);
			}

			// add links to linkSet
			saveLinks(element);
            		
			list.add(syncObject);
			childList = syncObject.getChildren();
			exportedObjectIDs.put( element.getId(), new String() );
		}
		
		/*++++
		 * Handle children recursively:
		 *+++++++++++++++++++++++++++++*/
		Set<CnATreeElement> children = element.getChildren();
		
		List<SyncObject> targetList = (childList == null ? orphaneList : childList);
		
		for( CnATreeElement child : children )
		{
			export(targetList, child, timestamp, exportedEntityTypes, exportedTypes);
		}
	}
	
	/**
     * @param syncLink
     * @param link
     */
    private void export(List<SyncLink> syncLinkList, CnALink link) {
        SyncLink syncLink = new SyncLink();
        syncLink.setDependant(link.getDependant().getId());
        syncLink.setDependency(link.getDependency().getId());
        syncLink.setRelationId(link.getRelationId());
        if(link.getComment()!=null && !link.getComment().isEmpty()) {
            syncLink.setComment(link.getComment());
        }
        syncLinkList.add(syncLink);     
    }


    private void saveLinks(CnATreeElement element) {
        try {
            linkSet.addAll(element.getLinksDown());
            linkSet.addAll(element.getLinksUp());
        } catch (Exception e) {
            getLog().error("error while getting links of element: " + element.getTitle() + "(" + element.getTypeId() + "), UUID: " + element.getUuid(), e);
        }
    }
    
    private IBaseDao<CnATreeElement, Serializable> getDao() {
        if(dao==null) {
            dao = createDao();
        }
        return dao;
    }
    
    private IBaseDao<CnATreeElement, Serializable> createDao() {
        return getDaoFactory().getDAO(CnATreeElement.class);
    }

    /*************************************************
	 * Hydrate {@code element}, including all of its
	 * successor elements and properties.
	 * 
	 * @param element
	 *************************************************/
	private CnATreeElement hydrate(CnATreeElement element)
	{ 
		if (element == null)
			return element;
		
		Element cachedElement = getCache().get(element.getUuid());
		if(cachedElement!=null) {
		    element = (CnATreeElement) cachedElement.getValue();
		    if (getLog().isDebugEnabled()) {
		        getLog().debug("Element from cache: " + element.getTitle());
            }
		    return element;
		}
		
		
		//IBaseDao<? extends CnATreeElement, Serializable> dao = getDaoFactory().getDAOforTypedElement(element);
		RetrieveInfo ri = RetrieveInfo.getPropertyChildrenInstance();
		ri.setLinksDown(true);
		ri.setLinksUp(true);
		element = getDao().retrieve(element.getDbId(), ri);
		
		getCache().put(new Element(element.getUuid(), element));
		
		if (getLog().isDebugEnabled()) {
		    getLog().debug("Hydrating element: " + element.getTitle() + ", UUID: " + element.getUuid());
        }
		
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
		
		return element;
	}

	public byte[] getResult() {
		return result; 
	}
	
	protected void finalize() throws Throwable {
	    CacheManager.getInstance().shutdown();
	    manager=null;
	};
	
	private Cache getCache() { 	
	    if(manager==null || Status.STATUS_SHUTDOWN.equals(manager.getStatus()) || cache==null || !Status.STATUS_ALIVE.equals(cache.getStatus())) {
	        cache = createCache();
	    } else {
	        cache = manager.getCache(cacheId);
	    }
	    return cache;
 	}
	
	private Cache createCache() {
	    cacheId = UUID.randomUUID().toString();
        manager = CacheManager.create();
        cache = new Cache(cacheId, 20000, false, false, 600, 500);
        manager.addCache(cache);
        return cache;
	}
	
}
