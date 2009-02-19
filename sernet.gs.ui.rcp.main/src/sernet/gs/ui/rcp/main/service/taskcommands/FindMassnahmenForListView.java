package sernet.gs.ui.rcp.main.service.taskcommands;

import java.io.Serializable;
import java.util.List;

import sernet.gs.ui.rcp.main.bsi.model.MassnahmenUmsetzung;
import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.GefaehrdungsUmsetzung;
import sernet.gs.ui.rcp.main.common.model.HydratorUtil;
import sernet.gs.ui.rcp.main.connect.IBaseDao;
import sernet.gs.ui.rcp.main.service.commands.GenericCommand;

public class FindMassnahmenForListView extends GenericCommand {

	private List<MassnahmenUmsetzung> all;

	public void execute() {
		all = getDaoFactory().getDAO(MassnahmenUmsetzung.class).findAll();
		hydrate(all);
	}

	/**
	 * Initialize lazy loaded field values needed for the view.
	 * 
	 * @param all
	 */
	private void hydrate(List<MassnahmenUmsetzung> all) {
		for (MassnahmenUmsetzung mn : all) {
			HydratorUtil.hydrateEntity(getDaoFactory().getDAO(MassnahmenUmsetzung.class), mn);
			
			mn.getParent();
			if (mn.getParent() instanceof GefaehrdungsUmsetzung)
				mn.getParent().getParent().getParent().getTitel();
			else
				mn.getParent().getParent().getTitel();
			mn.getUmsetzung();
			mn.getUmsetzungBis();
			mn.getUmsetzungDurch();
			mn.getStufe();
			mn.getTitel();
		}
	}

	public List<MassnahmenUmsetzung> getAll() {
		return all;
	}

}
