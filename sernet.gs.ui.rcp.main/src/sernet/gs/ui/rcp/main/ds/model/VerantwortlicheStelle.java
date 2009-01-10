package sernet.gs.ui.rcp.main.ds.model;

import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.EntityType;

public class VerantwortlicheStelle extends CnATreeElement 
	implements IDatenschutzElement {
	// ID must correspond to entity definition in XML description
	public static final String TYPE_ID = "verantwortlichestelle";
	
	private static EntityType entityType;

	/**
	 * Create new BSIElement.
	 * @param parent
	 */
	public VerantwortlicheStelle(CnATreeElement parent) {
		super(parent);
		if (entityType == null )
			entityType = getTypeFactory().getEntityType(TYPE_ID);
		setEntity(new Entity(TYPE_ID));
	}
	
	private VerantwortlicheStelle() {
		if (entityType == null )
			entityType = getTypeFactory().getEntityType(TYPE_ID);
	}
	
	@Override
	public String getTitel() {
		return entityType.getName();
	}

	@Override
	public String getTypeId() {
		return TYPE_ID;
	}
	
	@Override
	public boolean canContain(Object obj) {
		return false;
	}
	
	


}
