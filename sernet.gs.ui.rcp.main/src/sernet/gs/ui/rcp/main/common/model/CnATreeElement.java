package sernet.gs.ui.rcp.main.common.model;

import java.io.File;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.log4j.Logger;

import com.sun.star.document.LinkUpdateModes;

import sernet.gs.ui.rcp.main.CnAWorkspace;
import sernet.gs.ui.rcp.main.bsi.model.BSIModel;
import sernet.gs.ui.rcp.main.bsi.model.BausteinUmsetzung;
import sernet.gs.ui.rcp.main.bsi.model.EntityResolverFactory;
import sernet.gs.ui.rcp.main.bsi.model.IBSIModelListener;
import sernet.gs.ui.rcp.main.bsi.model.ISchutzbedarfProvider;
import sernet.gs.ui.rcp.main.bsi.model.LinkKategorie;
import sernet.gs.ui.rcp.main.bsi.model.Person;
import sernet.gs.ui.rcp.main.bsi.model.Schutzbedarf;
import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.EntityType;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.hui.common.connect.IEntityChangedListener;
import sernet.hui.common.connect.Property;
import sernet.hui.common.connect.PropertyChangedEvent;
import sernet.hui.common.connect.PropertyList;
import sernet.hui.common.multiselectionlist.IMLPropertyOption;
import sernet.hui.common.multiselectionlist.IMLPropertyType;
import sernet.snutils.DBException;

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
 * @author koderman@sernet.de
 * 
 */
public abstract class CnATreeElement implements Serializable, IBSIModelListener, IEntityChangedListener {

	private Integer dbId;
	
	private static final String ENTITY_TITLE = "ENTITY_";

	private CnATreeElement parent;

	private Entity entity;

	private IBSIModelListener modelChangeListener;

	// bi-directional qualified link list between items:
	private Set<CnALink> linksDown = new HashSet<CnALink>(5);
	private Set<CnALink> linksUp = new HashSet<CnALink>(5);
	private LinkKategorie links = new LinkKategorie(this);

	private Set<CnATreeElement> children;
	
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
		return super.hashCode(); // basicaly only used during migration of old objects (without hashcode)
	}

	public void addChild(CnATreeElement child) {
		if (!children.contains(child) && canContain(child)) {
			children.add(child);
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
		if (children.remove(child)) {
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
		return (CnATreeElement[]) children.toArray(new CnATreeElement[children
				.size()]);
	}

	public void childAdded(CnATreeElement category, CnATreeElement child) {
			getModelChangeListener().childAdded(category, child);
	}
	
	/**
	 * Propagate event upstairs.
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


	public CnATreeElement(CnATreeElement parent) {
		this();
		this.parent = parent;
	}

	public void dependencyChanged(IMLPropertyType type,
			IMLPropertyOption opt) {
		entityChanged();
	}

	public void propertyChanged(PropertyChangedEvent evt) {
		// TODO move this to schutzbedarfprovider as new entitychangelistener
		entityChanged();
	}

	public void selectionChanged(IMLPropertyType type, IMLPropertyOption opt) {
		entityChanged();
	}
		
	

	protected CnATreeElement() {
		if (this.uuid == null) {
			UUID randomUUID = java.util.UUID.randomUUID();
			uuid = randomUUID.toString();
		}
		
		children = new HashSet<CnATreeElement>();
	}

	public void entityChanged() {
			getModelChangeListener().childChanged(parent, this);
	}

	public CnATreeElement getParent() {
		return parent;
	}

	public abstract String getTitel();

	public String getId() {
		return ENTITY_TITLE + getEntity().getDbId();
	}

	public abstract String getTypeId();

	public Entity getEntity() {
		return entity;
	}
	
	public EntityType getEntityType() {
		if (subEntityType == null)
			subEntityType = getTypeFactory().getEntityType(getTypeId());
		return subEntityType;
	}

	public void setEntity(Entity newEntity) {
		if (entity != null) {
			entity.removeListener(this);
			if(isSchutzbedarfProvider())
				entity.removeListener(getSchutzbedarfProvider().getChangeListener());
		}
		entity = newEntity;

		if (entity != null) {
			entity.addChangeListener(this);
			if (isSchutzbedarfProvider())
				entity.addChangeListener(getSchutzbedarfProvider().getChangeListener());
		}
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
		childChanged(parent, this);
		linkChanged(link);
	}
	
	public void linkChanged(CnALink link) {
			getModelChangeListener().linkChanged(link);
	}

	public void removeLinkDown(CnALink link) {
		linksDown.remove(link);
		childChanged(parent, this);
		linkChanged(link);
	}

	public void addLinkUp(CnALink link) {
		linksUp.add(link);
	}
	
	public void modelRefresh() {
		getModelChangeListener().modelRefresh();
	}

	public void removeLinkUp(CnALink link) {
		linksUp.remove(link);
	}

	public ILinkChangeListener getLinkChangeListener() {
		return new ILinkChangeListener() {
			public void integritaetChanged() {
				// default: do nothing
			}

			public void verfuegbarkeitChanged() {
				// default: do nothing
			}

			public void vertraulichkeitChanged() {
				// default: do nothing
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
		
		BSIModel model = CnAElementFactory.getLoadedModel();
		if (model != null && ! (model instanceof NullModel)) {
			modelChangeListener = model;
		}
		else {
			modelChangeListener = new NullListener();
		}
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

	


}
