package sernet.gs.ui.rcp.main.service.crudcommands;

import java.io.Serializable;

import sernet.gs.ui.rcp.main.common.model.CnALink;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.common.model.HydratorUtil;
import sernet.gs.ui.rcp.main.connect.IBaseDao;
import sernet.gs.ui.rcp.main.service.commands.GenericCommand;
import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.Property;
import sernet.hui.common.connect.PropertyList;

public class RefreshElement<T extends CnATreeElement> extends GenericCommand {

	private T element;
	private boolean includeCollections;

	public RefreshElement(T element, boolean includeCollections) {
		this.element = element;
		this.includeCollections = includeCollections;
	}
	
	public RefreshElement(T element) {
		this(element, false);
	}
	
	public void execute() {
		IBaseDao dao =  getDaoFactory().getDAOForObject(element);
		Integer id = getId(element);
		element = (T) dao.findById(id);
		HydratorUtil.hydrateElement(dao, element, includeCollections);
	}

	private Integer getId(T element2) {
		if (element2 instanceof CnATreeElement) {
			CnATreeElement elmt = (CnATreeElement) element2;
			return elmt.getDbId();
		}
		
		return null;
		
	}

	public T getElement() {
		return element;
	}

}
