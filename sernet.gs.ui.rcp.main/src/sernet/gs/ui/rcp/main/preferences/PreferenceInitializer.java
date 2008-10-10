package sernet.gs.ui.rcp.main.preferences;

import java.io.File;
import java.net.URL;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.CnAWorkspace;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();

		store.setDefault(PreferenceConstants.GSACCESS,
				PreferenceConstants.GSACCESS_ZIP);

		store.setDefault(PreferenceConstants.BSIDIR, CnAWorkspace.getInstance().getWorkdir());

		store.setDefault(PreferenceConstants.BSIZIPFILE, CnAWorkspace.getInstance()
				.getWorkdir()
				+ File.separator + "it-grundschutz2006_html_de.zip");

		store.setDefault(PreferenceConstants.OOTEMPLATE, CnAWorkspace
				.getInstance().getWorkdir()
				+ File.separator + "office" + File.separator + "report.ods");

		store.setDefault(PreferenceConstants.OOTEMPLATE_TEXT, CnAWorkspace
				.getInstance().getWorkdir()
				+ File.separator + "office" + File.separator + "report.odt");

		if (System.getProperty("os.name").toLowerCase().matches(".*windows.*")
				|| System.getProperty("os.name").toLowerCase()
						.matches(".*nt.*")) {
			store.setDefault(PreferenceConstants.OODIR, "C:\\Programme");
		} else {
			store.setDefault(PreferenceConstants.OODIR, "/opt");
		}

		store.setDefault(PreferenceConstants.ERRORPOPUPS, true);
		store.setDefault(PreferenceConstants.FIRSTSTART, true);

		store.setDefault(PreferenceConstants.DB_DRIVER,
				PreferenceConstants.DB_DRIVER_DERBY);
		
		store.setDefault(PreferenceConstants.DB_DIALECT,
				PreferenceConstants.DB_DIALECT_derby);
		
		String derbyUrl = PreferenceConstants.DB_URL_DERBY.replace("%s",CnAWorkspace
				.getInstance().getWorkdir() );
		store.setDefault(PreferenceConstants.DB_URL,
				derbyUrl);
		
		store.setDefault(PreferenceConstants.DB_USER, "");
		store.setDefault(PreferenceConstants.DB_PASS, "");

		store.setDefault(PreferenceConstants.GS_DB_URL, "jdbc:jtds:sqlserver://127.0.0.1/BSIDB_V45");
		store.setDefault(PreferenceConstants.GS_DB_USER, "sa");
		store.setDefault(PreferenceConstants.GS_DB_PASS, "");
		
		

	}

}
