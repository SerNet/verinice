package sernet.gs.ui.rcp.main.service.crudcommands;

import java.io.Serializable;

import sernet.gs.ui.rcp.main.connect.IBaseDao;
import sernet.gs.ui.rcp.main.service.commands.GenericCommand;

public class UpdateElement<T> extends GenericCommand {

	private T element;
	private boolean fireupdates;

	public UpdateElement(T element, boolean fireUpdates) {
		this.element = element;
		this.fireupdates = fireUpdates;
	}

	public void execute() {
		IBaseDao dao =  getDaoFactory()	.getDAOForObject(element);
		element = (T) dao.merge(element, fireupdates);
	}

	public T getElement() {
		return element;
	}

}
