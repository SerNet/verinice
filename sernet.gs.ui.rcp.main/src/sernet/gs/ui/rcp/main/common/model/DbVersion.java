package sernet.gs.ui.rcp.main.common.model;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;

import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.bsi.model.BSIModel;
import sernet.gs.ui.rcp.main.bsi.model.ITVerbund;
import sernet.gs.ui.rcp.main.bsi.model.SonstigeITKategorie;
import sernet.gs.ui.rcp.main.common.model.migration.DbMigration;
import sernet.gs.ui.rcp.main.common.model.migration.MigrateDbTo0_91;
import sernet.gs.ui.rcp.main.common.model.migration.MigrateDbTo0_92;

public class DbVersion {

	private BSIModel loadedModel;
	private CnAElementHome dbHome;
	
	public static final double CURRENT_DB_VERSION = 0.92D;

	public DbVersion(BSIModel loadedModel, CnAElementHome dbHome) {
		this.loadedModel = loadedModel;
		this.dbHome = dbHome;
	}

	public void updateDBVersion(IProgress progress) {
		try {
			if (loadedModel.getDbVersion() < 0.91D) {
				DbMigration migration = new MigrateDbTo0_91(this);
				migration.run(progress);
			}

			 if (loadedModel.getDbVersion() < 0.92D) {
				 DbMigration migration = new MigrateDbTo0_92(this);
				 migration.run(progress);
			 }
			 // TODO migration to 0.93 for roles into mnums

		} catch (Exception e) {
			ExceptionUtil.log(e, "Fehler beim Migrieren der Datenbank!");
		}

	}

	public BSIModel getLoadedModel() {
		return loadedModel;
	}

	public CnAElementHome getDbHome() {
		return dbHome;
	}

}
