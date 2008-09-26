package sernet.gs.ui.rcp.main.common.model;

import java.io.File;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.log4j.Logger;

import com.sun.star.document.LinkUpdateModes;

import sernet.gs.ui.rcp.main.CnAWorkspace;
import sernet.gs.ui.rcp.main.bsi.model.BausteinUmsetzung;
import sernet.gs.ui.rcp.main.bsi.model.ISchutzbedarfProvider;
import sernet.gs.ui.rcp.main.bsi.model.LinkKategorie;
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
public abstract class CnATreeElement {

	private static String huiConfig = null;

	private Integer dbId;
	
	private static final String ENTITY_TITLE = "ENTITY_";

	private CnATreeElement parent;

	private Entity entity;

	public static HUITypeFactory typeFactory;

	// bi-directional qualified link list between items:
	private Set<CnALink> linksDown = new HashSet<CnALink>(5);
	private Set<CnALink> linksUp = new HashSet<CnALink>(5);
	private LinkKategorie links = new LinkKategorie(this);

	private Set<CnATreeElement> children;

	public void addChild(CnATreeElement child) {
		if (!children.contains(child) && canContain(child)) {
			children.add(child);
			if (getParent() != null)
				getParent().childAdded(this, child);
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
		if (getParent() != null)
			getParent().childAdded(category, child);
	}

	/**
	 * Propagate event upstairs.
	 * 
	 * @param category
	 * @param child
	 */
	public void childRemoved(CnATreeElement category, CnATreeElement child) {
		if (getParent() != null)
			getParent().childRemoved(category, child);
	}

	public void childChanged(CnATreeElement category, CnATreeElement child) {
		if (getParent() != null) {
			// child changed:
			getParent().childChanged(category, child);
		}
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

	private final IEntityChangedListener entityChangeListener = new IEntityChangedListener() {

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
	};

	private EntityType entityType;

	public CnATreeElement(CnATreeElement parent) {
		this();
		this.parent = parent;
	}

	
		
	

	protected CnATreeElement() {
		children = new HashSet<CnATreeElement>();
		if (typeFactory == null) {
			try {
				Logger.getLogger(this.getClass()).debug(
						"Initializing Hitro-UI framework...");
				huiConfig = String.format("%s%sconf%sSNCA.xml", CnAWorkspace
						.getInstance().getWorkdir(), File.separator,
						File.separator);
				huiConfig = (new File(huiConfig)).toURI().toString();
				Logger.getLogger(this.getClass()).debug("Getting type definition from: " + huiConfig);
				HUITypeFactory.initialize(huiConfig);
				typeFactory = HUITypeFactory.getInstance();
				Logger.getLogger(this.getClass()).debug("HUI initialized.");
			} catch (DBException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public void entityChanged() {
		if (getParent() != null)
			getParent().childChanged(parent, this);
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

	public void setEntity(Entity newEntity) {
		if (entity != null) {
			entity.removeListener(entityChangeListener);
			if(isSchutzbedarfProvider())
				entity.removeListener(getSchutzbedarfProvider().getChangeListener());
		}
		entity = newEntity;

		if (entity != null) {
			entity.addChangeListener(entityChangeListener);
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
	}
	
	public void removeLinkDown(CnALink link) {
		linksDown.remove(link);
		childChanged(parent, this);
	}

	public void addLinkUp(CnALink link) {
		linksUp.add(link);
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

	public EntityType getEntityType() {
		return entityType;
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

}
