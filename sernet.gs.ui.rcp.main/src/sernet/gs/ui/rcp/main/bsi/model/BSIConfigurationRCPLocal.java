package sernet.gs.ui.rcp.main.bsi.model;

import java.io.File;
import java.net.MalformedURLException;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Preferences;

import sernet.gs.scraper.IGSSource;
import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.CnAWorkspace;
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;

public class BSIConfigurationRCPLocal implements IBSIConfig {
	
	private String gsPath;
	private String dsPath;
	private boolean fromZipFile;
	private String cacheDir;

	public BSIConfigurationRCPLocal() {
		
		gsPath = null;
		Preferences preferences = Activator.getDefault().getPluginPreferences();
		fromZipFile = preferences.getString(
				PreferenceConstants.GSACCESS).equals(
				PreferenceConstants.GSACCESS_ZIP);

		if (fromZipFile) {
			gsPath = preferences.getString(PreferenceConstants.BSIZIPFILE);
		} else {
			gsPath = preferences.getString(PreferenceConstants.BSIDIR);
			try {
				gsPath = (new File(gsPath)).toURI().toURL().toString();
			} catch (MalformedURLException e) {
				Logger.getLogger(this.getClass()).debug(e);
			}
		}
		
		dsPath = preferences.getString(PreferenceConstants.DSZIPFILE);
		
		cacheDir = CnAWorkspace.getInstance().getWorkdir() + File.separator + "gscache";
	}

	public String getDsPath() {
		return dsPath;
	}

	public String getGsPath() {
		return gsPath;
	}

	public boolean isFromZipFile() {
		return fromZipFile;
	}

	public String getCacheDir() {
		return cacheDir;
	}

}
