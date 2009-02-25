package sernet.gs.ui.rcp.main.service.crudcommands;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.common.model.HydratorUtil;
import sernet.gs.ui.rcp.main.connect.IBaseDao;
import sernet.gs.ui.rcp.main.service.commands.GenericCommand;

public class LoadCnAElementsByIds<T extends CnATreeElement> extends GenericCommand {

	private Class<T> clazz;
	private List<Integer> dbIDs;
	private ArrayList<T> foundItems;


	public LoadCnAElementsByIds(Class<T> clazz, List<Integer> dbIDs) {
		this.clazz = clazz;
		this.dbIDs = dbIDs;
	}

	public void execute() {
		IBaseDao<T, Serializable> dao = getDaoFactory().getDAO(clazz);
		foundItems = new ArrayList<T>();
		for (Integer id : dbIDs) {
			T found = dao.findById(id);
			foundItems.add(found);
			HydratorUtil.hydrateElement(dao, found, false);
		}
	}

	public ArrayList<T> getFoundItems() {
		return foundItems;
	}

	
	

}
