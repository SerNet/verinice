package sernet.gs.ui.rcp.main.service.crudcommands;

import java.io.Serializable;

import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.common.model.HydratorUtil;
import sernet.gs.ui.rcp.main.connect.IBaseDao;
import sernet.gs.ui.rcp.main.service.commands.GenericCommand;

public class LoadCnAElementById extends GenericCommand {

	private Class<? extends CnATreeElement> clazz;
	private int id;
	private CnATreeElement found;

	public LoadCnAElementById(Class<? extends CnATreeElement> clazz, int id) {
		this.clazz= clazz;
		this.id = id;
	}

	public void execute() {
		IBaseDao<? extends CnATreeElement, Serializable> dao = getDaoFactory().getDAO(clazz);
		found = dao.findById(id);
		HydratorUtil.hydrateElement(dao, found, false);
	}

	public CnATreeElement getFound() {
		return found;
	}
	

}
