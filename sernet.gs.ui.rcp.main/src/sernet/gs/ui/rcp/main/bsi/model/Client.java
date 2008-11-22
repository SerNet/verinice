package sernet.gs.ui.rcp.main.bsi.model;

import java.util.Collection;

import sernet.gs.model.Baustein;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.common.model.ILinkChangeListener;
import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.EntityType;

public class Client extends CnATreeElement 
	implements IBSIStrukturElement {
	
	// ID must correspond to entity definition in XML description
	public static final String TYPE_ID = "client"; //$NON-NLS-1$
	public static final String PROP_NAME = "client_name"; //$NON-NLS-1$
	public static final String PROP_KUERZEL = "client_kuerzel"; //$NON-NLS-1$
	@Deprecated
	public static final String P_ADMIN_OLD = "client_admin"; //$NON-NLS-1$
	@Deprecated
	public static final String P_ANWENDER_OLD = "client_anwender"; //$NON-NLS-1$
	public static final String PROP_TAG			= "client_tag";
	public static final String PROP_ERLAEUTERUNG = "client_erlaeuterung";
	private static final String PROP_ANZAHL = "client_anzahl";
	
	private static EntityType entityType;

	private final ISchutzbedarfProvider schutzbedarfProvider 
	= new SchutzbedarfAdapter(this);


private final ILinkChangeListener linkChangeListener
	= new MaximumSchutzbedarfListener(this);
	/**
	 * Create new BSIElement.
	 * @param parent
	 */
	public Client(CnATreeElement parent) {
		super(parent);
		if (entityType == null )
			entityType = typeFactory.getEntityType(TYPE_ID);
		setEntity(new Entity(TYPE_ID));
		getEntity().createNewProperty(entityType.getPropertyType(PROP_NAME), Messages.Client_2);
	}
	public Collection<? extends String> getTags() {
		return TagHelper.getTags(getEntity().getSimpleValue(PROP_TAG));
	}
	
	public String getKuerzel() {
		return getEntity().getSimpleValue(PROP_KUERZEL);
	}
	
	public int getSchicht() {
		return 3;
	}
	
	private Client() {
		
	}
	
	@Override
	public String getTitel() {
		return getEntity().getProperties(PROP_NAME).getProperty(0).getPropertyValue();
	}
	
	public void setTitel(String name) {
		getEntity().setSimpleValue(entityType.getPropertyType(PROP_NAME), name);
	}

	@Override
	public String getTypeId() {
		return TYPE_ID;
	}
	
	@Override
	public boolean canContain(Object obj) {
		return CnaStructureHelper.canContain(obj);
	}
	
	
	@Override
	public ILinkChangeListener getLinkChangeListener() {
		return linkChangeListener;
	}

	@Override
	public ISchutzbedarfProvider getSchutzbedarfProvider() {
		return schutzbedarfProvider;
	}
	public void setErlaeuterung(String name) {
		getEntity().setSimpleValue(entityType.getPropertyType(PROP_ERLAEUTERUNG), name);
	}
	
	public void setKuerzel(String name) {
		getEntity().setSimpleValue(entityType.getPropertyType(PROP_KUERZEL), name);
	}
	
	public void setAnzahl(int anzahl) {
		getEntity().setSimpleValue(entityType.getPropertyType(PROP_ANZAHL), Integer.toString(anzahl));
	}

}
