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

	public RefreshElement(T element) {
		this.element = element;
	}
	
	public void execute() {
		IBaseDao<T, Serializable> dao = (IBaseDao<T, Serializable>) getDaoFactory().getDAO(element.getClass());
		Integer id = getId(element);
//		dao.reload(element, id);
		dao.refresh(element);
		HydratorUtil.hydrateElement(dao, element, true);
	}

	private Integer getId(T element2) {
		if (element2 instanceof CnATreeElement) {
			CnATreeElement elmt = (CnATreeElement) element2;
			return elmt.getDbId();
		}
		
//		if (element2 instanceof Entity) {
//			Entity elmt = (Entity) element2;
//			return elmt.getDbId();
//		}
//		
//		if (element2 instanceof Property) {
//			Property prop = (Property) element2;
//			return prop.getDbId();
//		}
//		
//		if (element2 instanceof PropertyList) {
//			PropertyList list = (PropertyList) element2;
//			return list.getDbId();
//		}
		
		return null;
		
	}

	public T getElement() {
		return element;
	}

}
