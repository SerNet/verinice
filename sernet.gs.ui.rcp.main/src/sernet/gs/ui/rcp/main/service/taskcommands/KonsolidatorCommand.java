package sernet.gs.ui.rcp.main.service.taskcommands;

import java.io.Serializable;
import java.util.List;

import sernet.gs.ui.rcp.main.bsi.model.BausteinUmsetzung;
import sernet.gs.ui.rcp.main.bsi.model.Konsolidator;
import sernet.gs.ui.rcp.main.connect.IBaseDao;
import sernet.gs.ui.rcp.main.service.commands.GenericCommand;

public class KonsolidatorCommand extends GenericCommand {

	private List<BausteinUmsetzung> selectedElements;
	private BausteinUmsetzung source;

	public KonsolidatorCommand(List<BausteinUmsetzung> selectedElements,
			BausteinUmsetzung source) {
		this.selectedElements = selectedElements;
		this.source = source;
	}

	public void execute() {
		IBaseDao<BausteinUmsetzung, Serializable> dao = getDaoFactory().getDAO(BausteinUmsetzung.class);
		dao.reload(source, source.getDbId());
		
		// for every target:
		for (BausteinUmsetzung target: selectedElements) {
			// do not copy source onto itself:
			if (source.equals(target))
				continue;
			
			dao.reload(target, target.getDbId());
			// set values:
			Konsolidator.konsolidiereBaustein(source, target);
			Konsolidator.konsolidiereMassnahmen(source, target);
		}
		
		// remove elements to make object smaller for transport back to client
		selectedElements = null;
		source = null;
	}


	
	

}
