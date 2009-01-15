package sernet.gs.ui.rcp.main.service.grundschutzparser;

import java.io.IOException;
import java.util.List;

import sernet.gs.model.Baustein;
import sernet.gs.service.GSServiceException;
import sernet.gs.ui.rcp.main.bsi.model.BSIMassnahmenModel;
import sernet.gs.ui.rcp.main.common.model.IProgress;
import sernet.gs.ui.rcp.main.service.commands.GenericCommand;
import sernet.gs.ui.rcp.main.service.commands.RuntimeCommandException;

public class LoadBausteine extends GenericCommand {

	private List<Baustein> bausteine;

	public void execute() {
		try {
			bausteine = BSIMassnahmenModel.loadBausteine(new IProgress() {

				public void beginTask(String name, int totalWork) {
					// TODO Auto-generated method stub
					
				}

				public void done() {
					// TODO Auto-generated method stub
					
				}

				public void setTaskName(String string) {
					// TODO Auto-generated method stub
					
				}

				public void subTask(String string) {
					// TODO Auto-generated method stub
					
				}

				public void worked(int work) {
					// TODO Auto-generated method stub
					
				}
				
			});
		} catch (Exception e) {
			throw new RuntimeCommandException(e);
		}
	}

	public List<Baustein> getBausteine() {
		return bausteine;
	}

}
