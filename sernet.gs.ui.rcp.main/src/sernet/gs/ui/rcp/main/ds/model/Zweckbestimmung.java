package sernet.gs.ui.rcp.main.ds.model;

import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.EntityType;

public class Zweckbestimmung extends CnATreeElement 
	implements IDatenschutzElement {
	// ID must correspond to entity definition in XML description
	public static final String TYPE_ID = "zweckbestimmung";
	

	/**
	 * Create new BSIElement.
	 * @param parent
	 */
	public Zweckbestimmung(CnATreeElement parent) {
		super(parent);
		setEntity(new Entity(TYPE_ID));
	}
	
	Zweckbestimmung() {
	}
	
	@Override
	public String getTitel() {
		return getEntityType().getName();
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
