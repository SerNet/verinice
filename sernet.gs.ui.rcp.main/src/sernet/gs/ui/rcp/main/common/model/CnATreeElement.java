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
package sernet.gs.ui.rcp.main.common.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.log4j.Logger;

import sernet.gs.ui.rcp.main.bsi.model.BSIModel;
import sernet.gs.ui.rcp.main.bsi.model.BausteinUmsetzung;
import sernet.gs.ui.rcp.main.bsi.model.IBSIModelListener;
import sernet.gs.ui.rcp.main.bsi.model.ISchutzbedarfProvider;
import sernet.gs.ui.rcp.main.bsi.model.LinkKategorie;
import sernet.gs.ui.rcp.main.bsi.model.Schutzbedarf;
import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.EntityType;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.hui.common.connect.PropertyList;

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
public abstract class CnATreeElement implements Serializable, IBSIModelListener {

	public static final String PERSON = "person";
	public static final String BAUSTEIN_UMSETZUNG = "baustein-umsetzung";
	public static final String MASSNAHMEN_UMSETZUNG = "massnahmen-umsetzung";
	public static final String RISIKO_MASSNAHMEN_UMSETZUNG = "risiko-massnahmen-umsetzung";
	public static final String GEFAEHRDUNGS_UMSETZUNG = "gefaehrdungs-umsetzung";
	public static final String GEBAEUDE = "gebaeude";
	public static final String FINISHED_RISK_ANALYSIS = "finished-risk-analysis";
	public static final String ANWENDUNG = "anwendung";
	public static final String CLIENT = "client";
	public static final String SONST_IT = "sonst-it";
	public static final String NETZ_KOMPONENTE = "netz-komponente";
	public static final String RAUM = "raum";
	public static final String TELEFON_KOMPONENTE = "telefon-komponente";
	public static final String SERVER = "server";
	public static final String IT_VERBUND = "it-verbund";
	
	private Integer dbId;

	private String sourceId;
	
	private String objectType;

	public String getSourceId() {
		return sourceId;
	}

	public void setSourceId(String sourceId) {
		this.sourceId = sourceId;
	}

	public String getExternalId() {
		return externalId;
	}

	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}



	private String externalId;
	
	private static final String ENTITY_TITLE = "ENTITY_";

	private CnATreeElement parent;
	
	private Entity entity;

	private transient IBSIModelListener modelChangeListener;

	// bi-directional qualified link list between items:
	private Set<CnALink> linksDown = new HashSet<CnALink>(1);
	private Set<CnALink> linksUp = new HashSet<CnALink>(1);
	private LinkKategorie links = new LinkKategorie(this);

	private Set<CnATreeElement> children;
	
	private Set<Permission> permissions = new HashSet<Permission>();
	
	private boolean childrenLoaded = false;
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof CnATreeElement))
			return false;
		CnATreeElement that = (CnATreeElement) obj;
		return (this.uuid.equals(that.getUuid()));
	}
	
	@Override
	public int hashCode() {
		if (uuid != null)
			return uuid.hashCode();
		return super.hashCode(); // basically only used during migration of old objects (without hashcode)
	}

	public void addChild(CnATreeElement child) {
		if (!children.contains(child) && canContain(child)) {
			children.add(child);
//			Logger.getLogger(this.getClass()).debug("Added child to " + this);
			if (getParent() != null)
				getParent().childAdded(this, child);
			else
				this.childAdded(this, child);
		} else {
			Logger.getLogger(this.getClass()).debug(
					"Element not added. Parent refuses " + child);
		}
	}

	/**
	 * Remove child and notify parents.
	 * 
	 * @param child
	 */
	public void removeChild(CnATreeElement child) {
		if (children.remove(child) && getParent()!=null) {
			getParent().childRemoved(this, child);
		}
	}

	/**
	 * Remove this item from parent and all links from and to this item.
	 * 
	 */
	public void remove() {
		if (getParent() != null)
			getParent().removeChild(this);
		
		CopyOnWriteArrayList<CnALink> list2 = new CopyOnWriteArrayList<CnALink>(
				getLinksDown());
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

	public void childChanged(CnATreeElement category, CnATreeElement child) {
			// child changed:
			getModelChangeListener().childChanged(category, child);
	}
	
	public boolean canContain(Object obj) {
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


	/**
	 * Creates a new CnATreeElement as a child of parent.
	 * Override this constructor in subclasses to set the title.
	 * 
	 * @param parent parent element
	 * @param title title of the element or null if title is unknown
	 */
	public CnATreeElement(CnATreeElement parent, String title) {
        this(parent);
    }
	
	/**
     * Creates a new CnATreeElement as a child of parent.
     * 
     * @param parent parent element
     */
	public CnATreeElement(CnATreeElement parent) {
		this();
		this.parent = parent;
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

	public abstract String getTitle();
	
	public void setTitel(String name) {
		// this method must be overriden
		throw new RuntimeException("Method not implemented in this object");
	}
	
	public String getId() {
		return ENTITY_TITLE + ((getEntity()!=null) ? getEntity().getDbId() : "");
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
		if (subEntityType == null)
			subEntityType = getTypeFactory().getEntityType(getTypeId());
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

	public boolean containsBausteinUmsetzung(String kapitel) {
		for (CnATreeElement elmt : getChildren()) {
			if (elmt instanceof BausteinUmsetzung) {
				BausteinUmsetzung bu = (BausteinUmsetzung) elmt;
				if (bu.getKapitel().equals(kapitel))
					return true;
			}
		}
		return false;
	}

	public Set<CnALink> getLinksDown() {
		return linksDown;
	}

	public void setLinksDown(Set<CnALink> linkDown) {
		this.linksDown = linkDown;
	}

	public Set<CnALink> getLinksUp() {
		return linksUp;
	}

	public void setLinksUp(Set<CnALink> linkUp) {
		this.linksUp = linkUp;
	}

	public LinkKategorie getLinks() {
		return links;
	}

	public void addLinkDown(CnALink link) {
		linksDown.add(link);
	}
	
	public void linkChanged(CnALink old, CnALink link) {
		getModelChangeListener().linkChanged(old, link);
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

	public ISchutzbedarfProvider getSchutzbedarfProvider() {
		return null;
	}

	public boolean isSchutzbedarfProvider() {
		return getSchutzbedarfProvider() != null;
	}

	public boolean isAdditionalMgmtReviewNeeded() {
		if (getEntity()==null)
			return false;
		PropertyList properties = getEntity().getProperties(
				getTypeId() + Schutzbedarf.ERGAENZENDEANALYSE);
		if (properties != null 
				&& properties.getProperties()!=null 
				&& properties.getProperties().size() > 0)
			return Schutzbedarf.isMgmtReviewNeeded(properties.getProperty(0)
					.getPropertyValue());
		else
			return false;
	
	}

	/**
	 * Set change listener to model root if one is present.
	 * 
	 * @return
	 */
	public synchronized IBSIModelListener getModelChangeListener() {
		if (modelChangeListener != null)
			return modelChangeListener;
		
//		BSIModel model = CnAElementFactory.getLoadedModel();
//		if (model != null && ! (model instanceof NullModel)) {
//			modelChangeListener = model;
//		}
//		else {
//			modelChangeListener = new NullListener();
//		}
		
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
		return HitroUtil.getInstance().getTypeFactory();
	}

	public void setLinks(LinkKategorie links) {
		this.links = links;
	}

	public void fireVertraulichkeitChanged(CascadingTransaction ta) {
		if (isSchutzbedarfProvider()) {
			getSchutzbedarfProvider().updateVertraulichkeit(ta);
		}
	}
	public void fireVerfuegbarkeitChanged(CascadingTransaction ta) {
		if (isSchutzbedarfProvider()) {
			getSchutzbedarfProvider().updateVerfuegbarkeit(ta);
		}
	}
	public void fireIntegritaetChanged(CascadingTransaction ta) {
		if (isSchutzbedarfProvider()) {
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
//			Logger.getLogger(this.getClass()).debug("NOT replacing, same instance: " + newElement);
			return;
		}
		
		if (getParent() == null) {
			// replace children of root element:

//			Logger.getLogger(this.getClass()).debug("Replacing children of element " + this);
			this.children = newElement.getChildren();
			this.setChildrenLoaded(true);
			
			return;
		}
		
		else {
//			Logger.getLogger(this.getClass()).debug("Replacing child " + this + "in parent " + getParent());
			getParent().removeChild(this);
//			CnAElementFactory.getLoadedModel().childRemoved(parent, this);
			
			getParent().addChild(newElement);
			newElement.setParent(getParent());
//			CnAElementFactory.getLoadedModel().childAdded(parent, newElement);
		}
		
	}

	public boolean isChildrenLoaded() {
		return childrenLoaded;
	}

	public void  setChildrenLoaded(boolean childrenLoaded) {
		this.childrenLoaded = childrenLoaded;
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
	
	public void refreshAllListeners(Object source) {
		// override this in model classes
	}

}
