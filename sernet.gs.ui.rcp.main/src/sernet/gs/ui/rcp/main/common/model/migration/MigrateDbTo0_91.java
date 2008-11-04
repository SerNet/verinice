package sernet.gs.ui.rcp.main.common.model.migration;

import org.apache.log4j.Logger;

import sernet.gs.ui.rcp.main.bsi.model.ITVerbund;
import sernet.gs.ui.rcp.main.bsi.model.SonstigeITKategorie;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.common.model.DbVersion;

public class MigrateDbTo0_91 extends DbMigration {
	
	private DbVersion dbVersion;

	public MigrateDbTo0_91(DbVersion dbVersion) {
		this.dbVersion = dbVersion;
	}

	@Override
	public void run() throws Exception {

			Logger.getLogger(this.getClass()).debug("Updating DB model to V 0.91.");
			ITVerbund verbund = dbVersion.getLoadedModel().getItverbuende().iterator().next();
			for (CnATreeElement child : verbund.getChildren()) {
				if (child instanceof SonstigeITKategorie)
					return;

			}
			SonstigeITKategorie kategorie = new SonstigeITKategorie(verbund);
			verbund.addChild(kategorie);
			dbVersion.getDbHome().save(kategorie);
			dbVersion.getLoadedModel().setDbVersion(0.91D);
			dbVersion.getDbHome().update(dbVersion.getLoadedModel());
	}

}
