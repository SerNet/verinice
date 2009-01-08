package sernet.gs.ui.rcp.main.service.crudcommands;

import java.io.Serializable;

import sernet.gs.ui.rcp.main.connect.IBaseDao;
import sernet.gs.ui.rcp.main.service.commands.GenericCommand;

public class UpdateElement<T> extends GenericCommand {

	private T element;

	public UpdateElement(T element) {
		this.element = element;
	}

	public void execute() {
		IBaseDao<T, Serializable> dao = (IBaseDao<T, Serializable>) getDaoFactory()
				.getDAO(element.getClass());
		element = dao.merge(element);
	}

	public T getElement() {
		return element;
	}

}
