package sernet.gs.ui.rcp.main.service.crudcommands;

import java.io.Serializable;

import sernet.gs.ui.rcp.main.connect.IBaseDao;
import sernet.gs.ui.rcp.main.service.commands.GenericCommand;

public class RemoveElement<T> extends GenericCommand {

	private T element;

	public RemoveElement(T element) {
		this.element = element;
	}
	
	public void execute() {
		IBaseDao<T, Serializable> dao = (IBaseDao<T, Serializable>) getDaoFactory().getDAO(element.getClass());
		dao.delete(element);
	}

}
