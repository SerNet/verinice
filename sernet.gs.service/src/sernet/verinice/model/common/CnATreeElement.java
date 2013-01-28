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
package sernet.verinice.model.common;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.log4j.Logger;

import sernet.hui.common.VeriniceContext;
import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.EntityType;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.hui.common.connect.ITypedElement;
import sernet.hui.common.connect.PropertyList;
import sernet.verinice.model.bsi.Attachment;
import sernet.verinice.model.bsi.BSIModel;
import sernet.verinice.model.bsi.BausteinUmsetzung;
import sernet.verinice.model.bsi.IBSIModelListener;
import sernet.verinice.model.bsi.ISchutzbedarfProvider;
import sernet.verinice.model.bsi.LinkKategorie;
import sernet.verinice.model.bsi.Schutzbedarf;
import sernet.verinice.model.iso27k.IISO27kGroup;
import sernet.verinice.model.iso27k.InheritLogger;
import sernet.verinice.model.validation.CnAValidation;

/**
 * This is the base class for all model classes of this application.
 * 
 * Implements all methods to take care of the model hierarchy and references to
 * other model objects.
 * 
 * Uses the Hitro-UI (HUI) framework: Actual attribute values will be saved in
 * an <code>HUIEntity</code> object and saved as defined in the corresponding
 * HUI XML file for this application: SNCA.xml
 * 
 * @author koderman[at]sernet[dot]de
 * 
 */
@SuppressWarnings("serial")
public abstract class CnATreeElement implements Serializable, IBSIModelListener, ITypedElement {

    private transient Logger log = Logger.getLogger(CnATreeElement.class); // NOPMD by dm on 07.02.12 12:36
    
    private static final InheritLogger LOG_INHERIT = InheritLogger.getLogger(CnATreeElement.class);

    private Logger getLog() {
        if (log == null) {
            log = Logger.getLogger(CnATreeElement.class);
        }
        return log;
    }
    
	private Integer dbId;
	
	private Integer scopeId;
	
	private String extId;

	private String sourceId;
	
	private String objectType;
	
	private String iconPath;
	
	public int getNumericProperty(String propertyTypeId) {
	    PropertyList properties = getEntity().getProperties(propertyTypeId);
	    if (properties == null || properties.getProperties().size()==0){
	        return 0;
	    } else {
	        return properties.getProperty(0).getNumericPropertyValue();
	    }
	}
	
	public void setNumericProperty(String propTypeId, int value) {
	    EntityType type = getTypeFactory().getEntityType(getEntity().getEntityType());
        getEntity().setSimpleValue(type.getPropertyType(propTypeId), Integer.toString(value));
	}

	public void setextId(String extId)
	{
	    this.extId = extId;
	}
	
	private static final String ENTITY_TITLE = "ENTITY_";

	private Integer parentId;
	
	private CnATreeElement parent;
	
	private Entity entity;

	private transient IBSIModelListener modelChangeListener;

	// bi-directional qualified link list between items:
	
	/**
	 * dependant in linksDown is this {@link CnATreeElement}
	 */
	private Set<CnALink> linksDown = new HashSet<CnALink>(1);
	
	/**
	 * dependency in linksUp is this {@link CnATreeElement}
	 */
	private Set<CnALink> linksUp = new HashSet<CnALink>(1);
	
	private LinkKategorie links = new LinkKategorie(this);

	private Set<CnATreeElement> children;
	
	private Set<Permission> permissions = new HashSet<Permission>();
	
	private boolean childrenLoaded = false;
	
	private Set<Attachment> files = new HashSet<Attachment>(1);
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj){
			return true;
		}
		if (!(obj instanceof CnATreeElement)){
			return false;
		}
		CnATreeElement that = (CnATreeElement) obj;
		boolean result = false;
		try {
		    result = this.getUuid().equals(that.getUuid());
        } catch (Exception e) {
            getLog().error("Error in equals, this uuid: " + this.getUuid(), e);
        }
		return result;
	}
	
	@Override
	public int hashCode() {
		if (getUuid() != null){
			return getUuid().hashCode();
		}
		return super.hashCode(); // basically only used during migration of old objects (without hashcode)
	}

	public void addChild(CnATreeElement child) {
		if (!children.contains(child) && canContain(child)) {
			children.add(child);
			if (getParent() != null) {			    
				try {
                    getParent().childAdded(this, child);
                } catch (Exception e) {
                    getLog().error("Error while adding child", e);
                }
			} else { 
				this.childAdded(this, child);
			}
		} else if (getLog().isDebugEnabled()) {
		    getLog().debug("Element not added. Parent refuses " + child);
        }
	}

	/**
	 * Remove child and notify parents.
	 * 
	 * @param child
	 */
	public void removeChild(CnATreeElement child) {
	    try {
    	    if (children.remove(child)) {	    
    	        childRemoved(this, child);	    
    		}
	    } catch(Exception e) {
            getLog().error("Error while removing child", e);
        }
	}

	/**
	 * Remove this item from parent and all links from and to this item.
	 * 
	 */
	public void remove() {
		if (getParent() != null) {
			getParent().removeChild(this);
		}
		
		CopyOnWriteArrayList<CnALink> list2 = new CopyOnWriteArrayList<CnALink>(getLinksDown());
		for (CnALink link : list2) {
			link.remove();
		}
		
		list2 = new CopyOnWriteArrayList<CnALink>(getLinksUp());
		for (CnALink link : list2) {
			link.remove();
		}
	}

	public CnATreeElement[] getChildrenAsArray() {
		return (CnATreeElement[]) children.toArray(new CnATreeElement[children.size()]);
	}

	public void childAdded(CnATreeElement category, CnATreeElement child) {
			getModelChangeListener().childAdded(category, child);
	}
	
	/**
	 * Propagate event upwards.
	 * 
	 * @param category
	 * @param child
	 */
	public void childRemoved(CnATreeElement category, CnATreeElement child) {
			getModelChangeListener().childRemoved(category, child);
	}

	public void childChanged(CnATreeElement child) {
			// child changed:
			getModelChangeListener().childChanged(child);
	}
	
	public boolean canContain(Object obj) { // NOPMD by dm on 07.02.12 12:39
		return false;
	}

	public void setChildren(Set<CnATreeElement> children) {
		this.children = children;
	}

	public Set<CnATreeElement> getChildren() {
		return children;
	}



	private String uuid;

	private transient EntityType subEntityType;


	public CnATreeElement(CnATreeElement parent) {
		this();
		this.parent = parent;
		if(parent!=null && this.getScopeId()==null) {
		    this.setScopeId(parent.getScopeId());
		}
	}

	protected CnATreeElement() {
		if (this.uuid == null) {
			UUID randomUUID = java.util.UUID.randomUUID();
			uuid = randomUUID.toString();
		}
		
		children = new HashSet<CnATreeElement>();
	}

	public CnATreeElement getParent() {
		return parent;
	}

	/**
     * @return the parentId
     */
    public Integer getParentId() {
        return parentId;
    }

    /**
     * @param parentId the parentId to set
     */
    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    public abstract String getTitle();
	
	public void setTitel(String name) { // NOPMD by dm on 07.02.12 12:38
		// override this method
	}
	
	public String getId() {
		if (getEntity() == null){
			return Entity.TITLE + getUuid();
		} else {
			return getEntity().getId();
		}
	}

	/**
     * @return the scopeId
     */
    public Integer getScopeId() {
        return scopeId;
    }

    /**
     * @param scopeId the scopeId to set
     */
    public void setScopeId(Integer scopeId) {
        this.scopeId = scopeId;
    }

    public abstract String getTypeId();

	public String getObjectType() {
		return objectType;
	}

	public void setObjectType(String objectType) {
		this.objectType = objectType;
	}

	public Entity getEntity() {
		return entity;
	}
	
	public EntityType getEntityType() {
		if (subEntityType == null) {
		    if (getLog().isDebugEnabled()) {
		        getLog().debug("type-factory: " + getTypeFactory());
            }
			subEntityType = getTypeFactory().getEntityType(getTypeId());
			if (getLog().isDebugEnabled()) {
                getLog().debug("type: " + getTypeId() + ", subEntityType: " + subEntityType);
            }
		}
		return subEntityType;
	}

	public void setEntity(Entity newEntity) {
		entity = newEntity;
	}

	public Integer getDbId() {
		return dbId;
	}

	public void setDbId(Integer dbId) {
		this.dbId = dbId;
	}
	
	public void setSimpleProperty(String typeId, String value) {
		EntityType entityType = getTypeFactory().getEntityType(getTypeId());
		getEntity().setSimpleValue(entityType.getPropertyType(typeId), value);
	}
	
	public void setParent(CnATreeElement parent) {
        this.parent = parent;
    }

	public void setParentAndScope(CnATreeElement parent) {
		setParent(parent);
		if(parent!=null && parent.getScopeId()!=null) {
		    this.setScopeId(parent.getScopeId());
		}	
	}

	public boolean containsBausteinUmsetzung(String kapitel) {
		for (CnATreeElement elmt : getChildren()) {
			if (elmt instanceof BausteinUmsetzung) {
				BausteinUmsetzung bu = (BausteinUmsetzung) elmt;
				if (bu.getKapitel().equals(kapitel)){
					return true;
				}
			}
		}
		return false;
	}

	
	/**
     * dependant in linksDown is this {@link CnATreeElement}
     *
     * @return
     */
	public Set<CnALink> getLinksDown() {
		return linksDown;
	}

	public void setLinksDown(Set<CnALink> linkDown) {
		this.linksDown = linkDown;
	}

	/**
     * dependency in linksUp is this {@link CnATreeElement}
     *
     * @return
     */
	public Set<CnALink> getLinksUp() {
		return linksUp;
	}

	public void setLinksUp(Set<CnALink> linkUp) {
		this.linksUp = linkUp;
	}

	public LinkKategorie getLinks() {
		return links;
	}
	
	public String getIconPath() {
        return iconPath;
    }

    public void setIconPath(String iconPath) {
        this.iconPath = iconPath;
    }

    /**
     * dependant in linksDown is this {@link CnATreeElement}
     */
	public void addLinkDown(CnALink link) {
		linksDown.add(link);
	}
	
	public void linkChanged(CnALink old, CnALink link, Object source) {
		getModelChangeListener().linkChanged(old, link, source);
	}
	
	public void linkRemoved(CnALink link) {
		getModelChangeListener().linkRemoved(link);
	}
	
	public void linkAdded(CnALink link) {
		getModelChangeListener().linkAdded(link);
	}

	public boolean removeLinkDown(CnALink link) {
		return linksDown.remove(link);
	}
	
	/**
     * dependency in linksUp is this {@link CnATreeElement}
     */
	public void addLinkUp(CnALink link) {
		linksUp.add(link);
	}
	
	/**
	 * @deprecated Es soll stattdessen {@link #modelRefresh(Object)} verwendet werden
	 */
	public void modelRefresh() {
		modelRefresh(null);
	}

	public void modelRefresh(Object source) {
		getModelChangeListener().modelRefresh(null);
	}

	public boolean removeLinkUp(CnALink link) {
		return linksUp.remove(link);
	}

	public ILinkChangeListener getLinkChangeListener() {
		return new ILinkChangeListener() {
			public void determineIntegritaet(CascadingTransaction ta)
					throws TransactionAbortedException {
				// do nothing
				
			}

			public void determineVerfuegbarkeit(CascadingTransaction ta)
					throws TransactionAbortedException {
				// do nothing
				
			}

			public void determineVertraulichkeit(CascadingTransaction ta)
					throws TransactionAbortedException {
				// do nothing
				
			}
		};
	}

	public ISchutzbedarfProvider getSchutzbedarfProvider() { // NOPMD by dm on 07.02.12 12:38
		return null;
	}

	public boolean isSchutzbedarfProvider() {
		return getSchutzbedarfProvider() != null;
	}

	public boolean isAdditionalMgmtReviewNeeded() {
		if (getEntity()==null){
			return false;
		}
		PropertyList properties = getEntity().getProperties(
				getTypeId() + Schutzbedarf.ERGAENZENDEANALYSE);
		if (properties != null 
				&& properties.getProperties()!=null 
				&& properties.getProperties().size() > 0){
			return Schutzbedarf.isMgmtReviewNeeded(properties.getProperty(0)
					.getPropertyValue());
		} else {
			return false;
		}
	}

	/**
	 * Set change listener to model root if one is present.
	 * 
	 * @return
	 */
	public synchronized IBSIModelListener getModelChangeListener() {
		if (modelChangeListener != null){
			return modelChangeListener;
		}
		
		modelChangeListener = new NullListener();
		return modelChangeListener;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	protected HUITypeFactory getTypeFactory() {
		return (HUITypeFactory) VeriniceContext.get(VeriniceContext.HUI_TYPE_FACTORY);
	}

	public void setLinks(LinkKategorie links) {
		this.links = links;
	}
	
	/**
	 * Returns a child-group of this this element
	 * which can contain elements of type childTypeId
	 * 
	 * @param childTypeId a type id
	 * @return a child-group of this this element
	 */
	public CnATreeElement getGroup(String childTypeId) {
        CnATreeElement group = null;
        for (CnATreeElement cnATreeElement : getChildren()) {
            if(cnATreeElement instanceof IISO27kGroup && 
                // true if: group can contain childTypeId
                // or group.typeId == childTypeId
                ((Arrays.binarySearch(((IISO27kGroup)cnATreeElement).getChildTypes(), childTypeId)>-1
                   || ((IISO27kGroup)cnATreeElement).getTypeId().equals(childTypeId)))) {
                    group = (CnATreeElement) cnATreeElement;
                    break;
            }
        }
        return group;
    }

	public void fireVertraulichkeitChanged(CascadingTransaction ta) {
		if (isSchutzbedarfProvider()) {
		    if(LOG_INHERIT.isInfo()) {
	            LOG_INHERIT.info(this.getTypeId() + " is provider, update confidentiality");
	        }
			getSchutzbedarfProvider().updateVertraulichkeit(ta);
		}
	}
	public void fireVerfuegbarkeitChanged(CascadingTransaction ta) {
		if (isSchutzbedarfProvider()) {
            if(LOG_INHERIT.isInfo()) {
                LOG_INHERIT.info(this.getTypeId() + " is provider, update availability");
            }
			getSchutzbedarfProvider().updateVerfuegbarkeit(ta);
		}
	}
	public void fireIntegritaetChanged(CascadingTransaction ta) {
		if (isSchutzbedarfProvider()) {
            if(LOG_INHERIT.isInfo()) {
                LOG_INHERIT.info(this.getTypeId() + " is provider, update integrity of: " + this.getTitle());
            }
			getSchutzbedarfProvider().updateIntegritaet(ta);
		}
	}

	/**
	 * Replace a displayed item in tree with another one. Used to replace 
	 * displaed objects with reloaded ones from thje database.
	 * 
	 * @param newElement
	 */
	public void replace(CnATreeElement newElement) {
		if (this == newElement) {
		    if (getLog().isDebugEnabled()) {
		        getLog().debug("NOT replacing, same instance: " + newElement);
            }
			return;
		}		
		if (getParent() == null) {
			// replace children of root element:
		    if (getLog().isDebugEnabled()) {
		        getLog().debug("Replacing children of element " + this);
            }
			this.children = newElement.getChildren();
			this.setChildrenLoaded(true);
			
			return;
		} else {
		    if (getLog().isDebugEnabled()) {
		        getLog().debug("Replacing child " + this + "in parent " + getParent());
            }
			getParent().removeChild(this);		
			getParent().addChild(newElement);
			newElement.setParentAndScope(getParent());
		}
		
	}

	public boolean isChildrenLoaded() {
		return childrenLoaded;
	}

	public void  setChildrenLoaded(boolean childrenLoaded) {
		this.childrenLoaded = childrenLoaded;
	}
	
	public Set<Attachment> getFiles() {
        return files;
    }

    public void setFiles(Set<Attachment> files) {
        this.files = files;
    }

    public void databaseChildAdded(CnATreeElement child) {
		getModelChangeListener().databaseChildAdded(child);
	}
	
	public void databaseChildChanged(CnATreeElement child) {
		getModelChangeListener().databaseChildChanged(child);
	}
	
	public void databaseChildRemoved(CnATreeElement child) {
		getModelChangeListener().databaseChildRemoved(child);
	}

	public void databaseChildRemoved(ChangeLogEntry entry) {
		getModelChangeListener().databaseChildRemoved(entry);
	}
	
	public void modelReload(BSIModel newModel) {
		getModelChangeListener().modelReload(newModel);
	}

	public Set<Permission> getPermissions() {
		return permissions;
	}

	public void setPermissions(Set<Permission> permissions) {
		this.permissions = permissions;
	}

	public void addPermission(Permission permission) {
		permissions.add(permission);
	}
	
	public boolean removePermission(Permission permission) {
		return permissions.remove(permission);
	}
	
	public void refreshAllListeners(Object source) { // NOPMD by dm on 07.02.12 12:39
		// override this in model classes
	}
	
	public String getExtId()
    {
        return extId;
    }

    public void setExtId(String extId)
    {
        this.extId = extId;
    }
    
    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
	    return new StringBuilder("uuid: ").append(getUuid()).toString();
	}
	
    @Override
    public void validationAdded(Integer scopeId){};
    
    @Override
    public void validationRemoved(Integer scopeId){};
    
    @Override
    public void validationChanged(CnAValidation oldValidation, CnAValidation newValidation){};

}
