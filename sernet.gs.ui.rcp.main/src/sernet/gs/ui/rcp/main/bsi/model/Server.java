package sernet.gs.ui.rcp.main.bsi.model;

import sernet.gs.model.Baustein;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.common.model.ILinkChangeListener;
import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.EntityType;

public class Server extends CnATreeElement 
	implements IBSIStrukturElement {
	
	// ID must correspond to entity definition in XML description
	public static final String TYPE_ID = "server"; //$NON-NLS-1$
	public static final String PROP_NAME = "server_name"; //$NON-NLS-1$
	public static final String PROP_KUERZEL = "server_kuerzel"; //$NON-NLS-1$
	public static final String P_ADMIN = "server_admin";
	
	private final ISchutzbedarfProvider schutzbedarfProvider 
	= new SchutzbedarfAdapter(this);


	private final ILinkChangeListener linkChangeListener
	= new MaximumSchutzbedarfListener(this);

	public static final String P_ANWENDER = "server_anwender";
				private static EntityType entityType;

	/**
	 * Create new BSIElement.
	 * @param parent
	 */
	public Server(CnATreeElement parent) {
		super(parent);
		if (entityType == null )
			entityType = typeFactory.getEntityType(TYPE_ID);
		setEntity(new Entity(TYPE_ID));
		getEntity().createNewProperty(entityType.getPropertyType(PROP_NAME), 
				Messages.Server_2);
	}
	
	public String getKuerzel() {
		return getEntity().getSimpleValue(PROP_KUERZEL);
	}
	
	public int getSchicht() {
		return 3;
	}
	
	private Server() {
		
	}
	
	@Override
	public String getTitle() {
		return getEntity().getProperties(PROP_NAME).getProperty(0).getPropertyValue();
	}

	@Override
	public String getTypeId() {
		return TYPE_ID;
	}
	
	@Override
	public boolean canContain(Object obj) {
		if (obj instanceof BausteinUmsetzung)
			return true;
		if (obj instanceof Baustein)
			return true;
		if (obj instanceof LinkKategorie)
			return true;
		return false;
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
