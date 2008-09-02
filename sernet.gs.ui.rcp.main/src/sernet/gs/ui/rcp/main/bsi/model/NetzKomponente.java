package sernet.gs.ui.rcp.main.bsi.model;

import sernet.gs.model.Baustein;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.EntityType;

public class NetzKomponente extends CnATreeElement 
	implements IBSIStrukturElement {
	
	// ID must correspond to entity definition in XML description
	public static final String TYPE_ID = "netzkomponente"; //$NON-NLS-1$
	public static final String PROP_NAME = "netzkomponente_name"; //$NON-NLS-1$
	public static final String PROP_KUERZEL = "netzkomponente_kuerzel"; //$NON-NLS-1$
	
	private static EntityType entityType;
	
	public String getKuerzel() {
		return getEntity().getSimpleValue(PROP_KUERZEL);
	}

	/**
	 * Create new BSIElement.
	 * @param parent
	 */
	public NetzKomponente(CnATreeElement parent) {
		super(parent);
		if (entityType == null )
			entityType = typeFactory.getEntityType(TYPE_ID);
		setEntity(new Entity(TYPE_ID));
		getEntity().createNewProperty(entityType.getPropertyType(PROP_NAME), 
				Messages.NetzKomponente_2);
	}
	
	public int getSchicht() {
		return 4;
	}
	
	private NetzKomponente() {
		
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
		return CnaStructureHelper.canContain(obj);
	}
	
	



}
