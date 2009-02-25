package sernet.gs.ui.rcp.main.service.taskcommands;

import java.io.Serializable;
import java.util.List;

import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.connect.IBaseDao;
import sernet.gs.ui.rcp.main.service.commands.GenericCommand;
import sernet.hui.common.connect.Entity;

public class BulkEditUpdate extends GenericCommand {

	private Class<? extends CnATreeElement> clazz;
	private List<Integer> dbIDs;
	private Entity dialogEntity;

	public BulkEditUpdate(Class<? extends CnATreeElement> clazz, List<Integer> dbIDs, Entity dialogEntity) {
		this.clazz=clazz;
		this.dbIDs = dbIDs;
		this.dialogEntity = dialogEntity;
	}

	public void execute() {
		IBaseDao<? extends CnATreeElement, Serializable> dao = getDaoFactory().getDAO(clazz);
		for (Integer id : dbIDs) {
			CnATreeElement found = dao.findById(id);
			Entity editEntity = found.getEntity();
			editEntity.copyEntity(dialogEntity);
		}
	}

}
