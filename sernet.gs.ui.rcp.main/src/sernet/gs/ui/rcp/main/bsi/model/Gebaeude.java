package sernet.gs.ui.rcp.main.bsi.model;

import java.util.Collection;

import sernet.gs.model.Baustein;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.common.model.ILinkChangeListener;
import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.EntityType;

public class Gebaeude extends CnATreeElement
implements IBSIStrukturElement {
	// ID must correspond to entity definition in XML description
	public static final String TYPE_ID = "gebaeude"; //$NON-NLS-1$
	
	public static final String PROP_NAME = "gebaeude_name"; //$NON-NLS-1$
	public static final String PROP_KUERZEL = "gebaeude_kuerzel"; //$NON-NLS-1$
	public static final String PROP_TAG			= "gebaeude_tag";

	public static final String PROP_ERLAEUTERUNG = "gebaeude_erlaeuterung";

	private static final String PROP_ANZAHL = "gebaeude_anzahl";
	
	private static EntityType entityType;

	
	private final ISchutzbedarfProvider schutzbedarfProvider 
	= new SchutzbedarfAdapter(this);


	private final ILinkChangeListener linkChangeListener
	= new MaximumSchutzbedarfListener(this);

	/**
	 * Create new BSIElement.
	 * @param parent
	 */
	public Gebaeude(CnATreeElement parent) {
		super(parent);
		if (entityType == null )
			entityType = typeFactory.getEntityType(TYPE_ID);
		setEntity(new Entity(TYPE_ID));
		getEntity().createNewProperty(entityType.getPropertyType(PROP_NAME),
				Messages.Gebaeude_2);
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
	
	private Gebaeude() {
		
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
		getEntity().setSimpleValue(entityType.getPropertyType(PROP_NAME), name);
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
