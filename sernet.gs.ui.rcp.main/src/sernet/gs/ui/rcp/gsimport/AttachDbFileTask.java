package sernet.gs.ui.rcp.gsimport;

import java.io.File;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import sernet.gs.reveng.importData.GSVampire;
import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.CnAWorkspace;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;

public class AttachDbFileTask {
	private GSVampire vampire;

	public AttachDbFileTask() {
		File conf = new File(CnAWorkspace.getInstance().getConfDir()
				+ File.separator + "hibernate-vampire.cfg.xml");
		vampire = new GSVampire(conf.getAbsolutePath());
	}
	
	public void attachDBFile(String url, String user, String pass, String fileName, String newDbName) throws SQLException, ClassNotFoundException {
		if (fileName == null || fileName.length() <1)
			return;
		vampire.attachFile(newDbName, fileName, url, user, pass);
	}
	
	
}
