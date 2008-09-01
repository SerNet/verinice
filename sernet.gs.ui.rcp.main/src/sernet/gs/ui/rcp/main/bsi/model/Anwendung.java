package sernet.gs.ui.rcp.main.bsi.model;

import sernet.gs.model.Baustein;
import sernet.gs.ui.rcp.main.common.model.CnALink;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.common.model.ILinkChangeListener;
import sernet.gs.ui.rcp.main.ds.model.IDatenschutzElement;
import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.EntityType;
import sernet.hui.common.connect.HUITypeFactory;

public class Anwendung extends CnATreeElement 
	implements IBSIStrukturElement {
	
	private final ISchutzbedarfProvider schutzbedarfProvider 
		= new SchutzbedarfAdapter(this);
	
	
	private final ILinkChangeListener linkChangeListener
		= new MaximumSchutzbedarfListener(this);

	// ID must correspond to entity definition in XML description
	public static final String TYPE_ID = "anwendung"; //$NON-NLS-1$

	public static final String PROP_NAME = "anwendung_name"; //$NON-NLS-1$

	public static final String PROP_KUERZEL = "anwendung_kuerzel"; //$NON-NLS-1$

	public static final String PROP_PERSBEZ = "anwendung_persbez"; //$NON-NLS-1$

	public static final String PROP_BENUTZER = "anwendung_benutzer"; //$NON-NLS-1$
	public static final String PROP_EIGENTUEMER = "anwendung_eigentümer"; //$NON-NLS-1$

	private static EntityType entityType;

	public int getSchicht() {
		return 5;
	}

	public String getKuerzel() {
		return getEntity().getSimpleValue(PROP_KUERZEL);
	}

	/**
	 * Create new BSIElement.
	 * 
	 * @param parent
	 */
	public Anwendung(CnATreeElement parent) {
		super(parent);
		if (entityType == null)
			entityType = typeFactory.getEntityType(TYPE_ID);
		setEntity(new Entity(TYPE_ID));
		getEntity().createNewProperty(entityType.getPropertyType(PROP_NAME),
				Messages.Anwendung_4);
	}

	private Anwendung() {

	}

	@Override
	public String getTitle() {
		return getEntity().getProperties(PROP_NAME).getProperty(0)
				.getPropertyValue();
	}

	@Override
	public String getTypeId() {
		return TYPE_ID;
	}

	@Override
	public boolean canContain(Object obj) {
		if (obj instanceof IDatenschutzElement)
			return true;
		return TreeStructureValidator.canContain(obj);
	}

	@Override
	public ILinkChangeListener getLinkChangeListener() {
		return linkChangeListener;
	}

	@Override
	public ISchutzbedarfProvider getSchutzbedarfProvider() {
		return schutzbedarfProvider;
	}

}
