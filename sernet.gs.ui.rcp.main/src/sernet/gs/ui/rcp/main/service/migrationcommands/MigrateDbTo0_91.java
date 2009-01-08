package sernet.gs.ui.rcp.main.service.migrationcommands;

import org.apache.log4j.Logger;

import com.sun.corba.se.spi.legacy.connection.GetEndPointInfoAgainException;

import sernet.gs.ui.rcp.main.bsi.model.BSIModel;
import sernet.gs.ui.rcp.main.bsi.model.ITVerbund;
import sernet.gs.ui.rcp.main.bsi.model.SonstigeITKategorie;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.common.model.DbVersion;
import sernet.gs.ui.rcp.main.common.model.IProgress;
import sernet.gs.ui.rcp.main.service.commands.CommandException;
import sernet.gs.ui.rcp.main.service.commands.RuntimeCommandException;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadBSIModel;
import sernet.gs.ui.rcp.main.service.crudcommands.SaveElement;

public class MigrateDbTo0_91 extends DbMigration {
	

	public void run() throws Exception {
			Logger.getLogger(this.getClass()).debug("Updating DB model to V 0.91.");
			LoadBSIModel command = new LoadBSIModel();
			getCommandService().executeCommand(command);
			BSIModel model = command.getModel();
			
			ITVerbund verbund = model.getItverbuende().iterator().next();
			for (CnATreeElement child : verbund.getChildren()) {
				if (child instanceof SonstigeITKategorie)
					return;

			}
			SonstigeITKategorie kategorie = new SonstigeITKategorie(verbund);
			verbund.addChild(kategorie);
			
			
			SaveElement<SonstigeITKategorie> command2 = new SaveElement<SonstigeITKategorie>(kategorie);
			getCommandService().executeCommand(command2);
			
			model.setDbVersion(getVersion());
			SaveElement<BSIModel> command3 = new SaveElement<BSIModel>(model);
			getCommandService().executeCommand(command3);
			
	}

	@Override
	public double getVersion() {
		return 0.91D;
	}

	public void execute() {
		try {
			run();
		} catch (Exception e) {
			throw new RuntimeCommandException(e);
		}
	}


}
