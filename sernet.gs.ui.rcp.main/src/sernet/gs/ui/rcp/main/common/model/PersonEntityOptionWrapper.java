package sernet.gs.ui.rcp.main.common.model;

import sernet.gs.ui.rcp.main.bsi.model.Person;
import sernet.hui.common.connect.Entity;
import sernet.hui.common.multiselectionlist.IContextMenuListener;
import sernet.hui.common.multiselectionlist.IMLPropertyOption;

public class PersonEntityOptionWrapper implements IMLPropertyOption {

	private Person person;

	public PersonEntityOptionWrapper(Person entity) {
		this.person = entity;
	}

	public IContextMenuListener getContextMenuListener() {
		return null;
	}

	public String getId() {
		return person.getEntity().getDbId().toString();
	}

	public String getName() {
		return person.getTitel();
	}

}
