package sernet.gs.ui.rcp.main.service.crudcommands;

import java.io.Serializable;
import java.util.List;

import sernet.gs.ui.rcp.main.bsi.model.BSIModel;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.connect.IBaseDao;
import sernet.gs.ui.rcp.main.service.commands.GenericCommand;

public class LoadBSIModel extends GenericCommand {


	private BSIModel model;

	public LoadBSIModel() {
	}
	
	public void execute() {
		List<BSIModel> models = getDaoFactory().getDAO(BSIModel.class).findAll();
		if (models != null && models.size()>0)
			model = models.get(0);
	}

	public BSIModel getModel() {
		return model;
	}
	
	

}
