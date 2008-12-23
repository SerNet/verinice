package sernet.gs.ui.rcp.main.service.crudcommands;

import java.io.Serializable;
import java.util.List;

import sernet.gs.ui.rcp.main.bsi.model.BSIModel;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.connect.IBaseDao;
import sernet.gs.ui.rcp.main.service.commands.GenericCommand;

public class LoadElementByType<T> extends GenericCommand {


	private List<T> elements;
	private Class<T> clazz;

	public LoadElementByType(Class<T> type) {
		this.clazz = type;
	}
	
	public void execute() {
		elements = getDaoFactory().getDAO(clazz).findAll();
	}

	public List<T> getElements() {
		return elements;
	}
	
	

}
