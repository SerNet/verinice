package sernet.gs.ui.rcp.main.bsi.model;

import java.util.Collection;

import sernet.gs.model.Baustein;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.common.model.ILinkChangeListener;
import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.EntityType;

public class Raum extends CnATreeElement 
	implements IBSIStrukturElement {
	
	// ID must correspond to entity definition in XML description
	
	public static final String TYPE_ID = "raum"; //$NON-NLS-1$
	public static final String PROP_NAME = "raum_name"; //$NON-NLS-1$
	public static final String PROP_KUERZEL = "raum_kuerzel"; //$NON-NLS-1$
	public static final String PROP_TAG			= "raum_tag"; //$NON-NLS-1$
	public static final String PROP_ERLAEUTERUNG = "raum_erlaeuterung"; //$NON-NLS-1$
	private static final String PROP_ANZAHL = "raum_anzahl"; //$NON-NLS-1$
	

	private  ISchutzbedarfProvider schutzbedarfProvider 
	= new SchutzbedarfAdapter(this);


	private  ILinkChangeListener linkChangeListener
	= new MaximumSchutzbedarfListener(this);

	/**
	 * Create new BSIElement.
	 * @param parent
	 */
	public Raum(CnATreeElement parent) {
		super(parent);
		setEntity(new Entity(TYPE_ID));
		getEntity().createNewProperty(getEntityType().getPropertyType(PROP_NAME), 
				"Neuer Raum");
	}
	
	public String getKuerzel() {
		return getEntity().getSimpleValue(PROP_KUERZEL);
	}
	public Collection<? extends String> getTags() {
		return TagHelper.getTags(getEntity().getSimpleValue(PROP_TAG));
	}
	
	public int getSchicht() {
		return 2;
	}
	
	private Raum() {
		
	}
	
	@Override
	public String getTitel() {
		return getEntity().getProperties(PROP_NAME).getProperty(0).getPropertyValue();
	}

	@Override
	public String getTypeId() {
		return TYPE_ID;
	}
	
	@Override
	public boolean canContain(Object obj) {
		return CnaStructureHelper.canContain(obj);
	}
	
	public void setTitel(String name) {
		getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_NAME), name);
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
		getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_ERLAEUTERUNG), name);
	}
	
	public void setKuerzel(String name) {
		getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_KUERZEL), name);
	}

	public void setAnzahl(int anzahl) {
		getEntity().setSimpleValue(getEntityType().getPropertyType(PROP_ANZAHL), Integer.toString(anzahl));
	}
}
