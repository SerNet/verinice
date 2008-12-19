package sernet.gs.ui.rcp.gsimport;

import java.io.File;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import sernet.gs.reveng.importData.BackupFileLocation;
import sernet.gs.reveng.importData.GSVampire;
import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.CnAWorkspace;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;

public class RestoreDbTask {
	private GSVampire vampire;

	public RestoreDbTask() {
		File conf = new File(CnAWorkspace.getInstance().getConfDir()
				+ File.separator + "hibernate-vampire.cfg.xml");
		vampire = new GSVampire(conf.getAbsolutePath());
	}
	
	public void restoreDBFile(String urlString, String userString,
			String passString, String fileName, String newDbName, String toDir) throws SQLException, ClassNotFoundException {
		BackupFileLocation fileNames = vampire.getBackupFileNames(newDbName, fileName, urlString, userString, passString);
		vampire.restoreBackupFile(newDbName, fileName, urlString, userString, passString,
				fileNames.getMdfLogicalName(), toDir + "\\" + newDbName + ".mdf",
				fileNames.getLdfLogicalName(), toDir + "\\" + newDbName + ".ldf");
		
	}
	
	
}
