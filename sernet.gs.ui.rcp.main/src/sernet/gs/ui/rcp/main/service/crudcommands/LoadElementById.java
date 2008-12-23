package sernet.gs.ui.rcp.main.service.crudcommands;

import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.service.commands.GenericCommand;

public class LoadElementById extends GenericCommand {

	private Class<? extends CnATreeElement> clazz;
	private int id;
	private CnATreeElement found;

	public LoadElementById(Class<? extends CnATreeElement> clazz, int id) {
		this.clazz= clazz;
		this.id = id;
	}

	public void execute() {
		found = getDaoFactory().getDAO(clazz).findById(id);
	}

	public CnATreeElement getFound() {
		return found;
	}
	

}
