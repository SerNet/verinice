package sernet.gs.ui.rcp.main.service.crudcommands;

import java.io.Serializable;

import sernet.gs.ui.rcp.main.connect.IBaseDao;
import sernet.gs.ui.rcp.main.service.commands.GenericCommand;

public class RefreshElement<T> extends GenericCommand {

	private T element;

	public RefreshElement(T element) {
		this.element = element;
	}
	
	public void execute() {
		IBaseDao<T, Serializable> dao = (IBaseDao<T, Serializable>) getDaoFactory().getDAO(element.getClass());
		dao.refresh(element);
	}

}
