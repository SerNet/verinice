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

    public static final String DBID = "dbid";
    public static final String UUID = "uuid";
    public static final String PARENT_ID = "parent-id";
    public static final String SCOPE_ID = "scope-id";
    
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
	
    /**
     * <p>
     * Modeling templates in the BSI IT baseline security allow to create and
     * maintain the modules, safeguards and objects at freely definable, ideally
     * at central location, and to use them in other points of application.
     * </p>
     * <p>
     * Each {@link CnATreeElement}, representing a Object or Module, can be
     * marked as modeling template or each Safeguard as central
     * ({@link TemplateType#TEMPLATE}).
     * </p>
     * <p>
     * Copy & paste a modeling template means using those to model a new
     * {@link CnATreeElement} which implements all underlying elements
     * ({@link TemplateType#IMPLEMENTATION}) that belong to the modeling
     * template.
     * </p>
     * <p>
     * Example:
     * </p>
     * <p>
     * A Modeling template in the BSI IT baseline:
     * </p>
     * <p>
     * <b>IT Network 1</b> <ui>
     * <li>Object (marked as modeling template,
     * {@link TemplateType#TEMPLATE})</li> <ui>
     * <li>Module 1 (marked as modeling template,
     * {@link TemplateType#TEMPLATE})</li> <ui>
     * <li>Safeguard 1 (marked as central, {@link TemplateType#TEMPLATE})</li>
     * <li>Safeguard 2</li> </ui>
     * <li>Module 2</li> <ui>
     * <li>Safeguard 1</li>
     * <li>Safeguard 2 (marked as central, {@link TemplateType#TEMPLATE})</li>
     * </ui>
     * <li>Module 3...</li> </ui> </ui>
     * </p>
     * <p>
     * After applying (implementing) this modeling template:
     * </p>
     * <p>
     * <b>IT Network 2</b> <ui>
     * <li>Object (marked as implementation,
     * {@link TemplateType#IMPLEMENTATION})</li> <ui>
     * <li>Module 1 (marked as implementation,
     * {@link TemplateType#IMPLEMENTATION})</li> <ui>
     * <li>Safeguard 1 (marked as central implementation,
     * {@link TemplateType#IMPLEMENTATION})</li>
     * <li>Safeguard 2</li> </ui>
     * <li>Module 2</li> <ui>
     * <li>Safeguard 1</li>
     * <li>Safeguard 2 (marked as central implementation,
     * {@link TemplateType#IMPLEMENTATION})</li> </ui>
     * <li>Module 3...</li> </ui> </ui>
     * </p>
     * 
     * @author Viktor Schmidt <vschmidt[at]ckc[dot]de>
     * @see sernet.gs.server.DeleteOrphanTemplateRelationsJob
     */
	public enum TemplateType { NONE, TEMPLATE, IMPLEMENTATION }
	public String templateTypeValue = TemplateType.NONE.name();
	
    /**
     * Holds references to {@link TemplateType#TEMPLATE}s which are implemented
     * in this {@link CnATreeElement}.
     * 
     * @see TemplateType
     * @see sernet.gs.server.DeleteOrphanTemplateRelationsJob
     */
    private Set<String> implementedTemplateUuids = new HashSet<String>();

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
		removeCnLinks();
	}

    /**
     * Remove all links from and to this item.
     * 
     */
    public void removeCnLinks() {
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
		return children.toArray(new CnATreeElement[children.size()]);
	}

	@Override
    public void childAdded(CnATreeElement category, CnATreeElement child) {
			getModelChangeListener().childAdded(category, child);
	}
	
	/**
	 * Propagate event upwards.
	 * 
	 * @param category
	 * @param child
	 */
	@Override
    public void childRemoved(CnATreeElement category, CnATreeElement child) {
			getModelChangeListener().childRemoved(category, child);
	}

	@Override
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
		inherit(parent);
	}

	protected CnATreeElement() {
		if (this.uuid == null) {
			UUID randomUUID = java.util.UUID.randomUUID();
			uuid = randomUUID.toString();
		}
		
		children = new HashSet<CnATreeElement>();
	}
	
    protected void inherit(CnATreeElement parent) {
        if (parent != null) {
            inheritScopeId(parent);
        }
    }

    protected void inheritIcon(CnATreeElement parent) {
        if (this.getIconPath() == null) {
            this.setIconPath(parent.getIconPath());
        }
    }

    protected void inheritScopeId(CnATreeElement parent) {
        if (this.getScopeId() == null) {
            this.setScopeId(parent.getScopeId());
        }
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

    @Override
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
	
	public String getPropertyValue(String typeId) {
        return getEntity().getPropertyValue(typeId);
    }
	
	public void setPropertyValue(String typeId, String value) {
        getEntity().setPropertyValue(typeId, value);
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
     * </p>
     * Might also been called getLinksToDependencies();
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
     * </p>
     * Might also been called getLinksToDependants();
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
	
	@Override
    public void linkChanged(CnALink old, CnALink link, Object source) {
		getModelChangeListener().linkChanged(old, link, source);
	}
	
	@Override
    public void linkRemoved(CnALink link) {
		getModelChangeListener().linkRemoved(link);
	}
	
	@Override
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
	@Deprecated
    @Override
    public void modelRefresh() {
		modelRefresh(null);
	}

	@Override
    public void modelRefresh(Object source) {
		getModelChangeListener().modelRefresh(null);
	}

	public boolean removeLinkUp(CnALink link) {
		return linksUp.remove(link);
	}

	public ILinkChangeListener getLinkChangeListener() {
		return new ILinkChangeListener() {
			@Override
            public void determineIntegritaet(CascadingTransaction ta)
					throws TransactionAbortedException {
				// do nothing
				
			}

			@Override
            public void determineVerfuegbarkeit(CascadingTransaction ta)
					throws TransactionAbortedException {
				// do nothing
				
			}

			@Override
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
                    group = cnATreeElement;
                    break;
            }
        }
        return group;
    }
	
	/**
	 * Checks if a propertyId is the id of a static property which are
	 * defined for every element: CnATreeElement.DBID, .PARENT_ID, .SCOPE_ID, .UUID
	 * 
	 * @param propertyId The id of a "static" property.
	 * @return Return true if the id is the id of a "static" property
	 */
	public static boolean isStaticProperty(String propertyId) {
	        return(CnATreeElement.PARENT_ID.equals(propertyId)
	            || CnATreeElement.SCOPE_ID.equals(propertyId)
	            || CnATreeElement.DBID.equals(propertyId)
	            || CnATreeElement.UUID.equals(propertyId));
	    }
	
	/**
	 * Returns properties which are defined for every element. This method is a addition
	 * to retrieve these values by property keys the same way as the properties
	 * which are saved as dynamic properties
	 * 
	 * @param element A CnATreeElement
	 * @param propertyId The id of a "static" property: 
	 *     CnATreeElement.DBID, .PARENT_ID, .SCOPE_ID, .UUID
	 * @return The value of the property or null if no value exists
	 */
	public static String getStaticProperty(CnATreeElement element, String propertyId) {
        String value = null;
        if(CnATreeElement.SCOPE_ID.equals(propertyId)) {
            value = String.valueOf(element.getScopeId());
        }
        if(CnATreeElement.DBID.equals(propertyId)) {
            value = String.valueOf(element.getDbId());
        }
        if(CnATreeElement.PARENT_ID.equals(propertyId)) {
            value = String.valueOf(element.getParentId());
        }
        if(CnATreeElement.UUID.equals(propertyId)) {
            value = element.getUuid();
        }
        return value;
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

    @Override
    public void databaseChildAdded(CnATreeElement child) {
		getModelChangeListener().databaseChildAdded(child);
	}
	
	@Override
    public void databaseChildChanged(CnATreeElement child) {
		getModelChangeListener().databaseChildChanged(child);
	}
	
	@Override
    public void databaseChildRemoved(CnATreeElement child) {
		getModelChangeListener().databaseChildRemoved(child);
	}

	@Override
    public void databaseChildRemoved(ChangeLogEntry entry) {
		getModelChangeListener().databaseChildRemoved(entry);
	}
	
	@Override
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
    
    /**
     * Returns the TemplateTypeValue of this {@link CnATreeElement}.
     * 
     * @return the TemplateType
     * 
     * @see TemplateType
     */
    public TemplateType getTemplateType() {
        return getTemplateTypeValue() == null? TemplateType.NONE : TemplateType.valueOf(getTemplateTypeValue());
    }

    /**
     * Sets the TemplateTypeValue of this {@link CnATreeElement}.
     * 
     * @param templateType
     *            the templateType to set
     * 
     * @see TemplateType
     */
    public void setTemplateType(TemplateType templateType) {
        this.templateTypeValue = templateType.name();
    }

    /**
     * Returns the TemplateTypeValue of this {@link CnATreeElement}.
     * 
     * @return the templateTypeValue
     * 
     * @see TemplateType
     */
    public String getTemplateTypeValue() {
        return templateTypeValue;
    }

    /**
     * Sets the TemplateTypeValue of this {@link CnATreeElement}.
     * 
     * @param templateTypeValue
     *            the templateTypeValue to set
     * 
     * @see TemplateType
     */
    public void setTemplateTypeValue(String templateTypeValue) {
        this.templateTypeValue = templateTypeValue;
    }

    /**
     * @return true if this {@link CnATreeElement} is a
     *         {@link TemplateType#TEMPLATE}, false otherwise.
     * @see TemplateType
     */
    public boolean isTemplate() {
        return TemplateType.TEMPLATE.equals(this.getTemplateType());
    }

    /**
     * @return true if this {@link CnATreeElement} is a
     *         {@link TemplateType#IMPLEMENTATION}, false otherwise.
     * @see TemplateType
     */
    public boolean isImplementation() {
        return TemplateType.IMPLEMENTATION.equals(this.getTemplateType());
    }
    
    /**
     * @return true if this {@link CnATreeElement} is a
     *         {@link TemplateType#TEMPLATE} or
     *         {@link TemplateType#IMPLEMENTATION}, false otherwise.
     * @see TemplateType
     */
    public boolean isTemplateOrImplementation() {
        return TemplateType.TEMPLATE.equals(this.getTemplateType()) || TemplateType.IMPLEMENTATION.equals(this.getTemplateType());
    }

    /**
     * @return all references to {@link TemplateType#TEMPLATE}s which are
     *         implemented in this {@link CnATreeElement}.
     * @see TemplateType
     * @see sernet.gs.server.DeleteOrphanTemplateRelationsJob
     */
    public Set<String> getImplementedTemplateUuids() {
        return implementedTemplateUuids;
    }

    /**
     * Set new references to {@link TemplateType#TEMPLATE} which are implemented
     * in this {@link CnATreeElement}
     * 
     * @param implementedTemplateUuids
     *            the UUIds of {@link TemplateType#TEMPLATE}s
     * @see TemplateType
     * @see sernet.gs.server.DeleteOrphanTemplateRelationsJob
     */
    public void setImplementedTemplateUuids(Set<String> implementedTemplateUuids) {
        this.implementedTemplateUuids = implementedTemplateUuids;
    }
}
