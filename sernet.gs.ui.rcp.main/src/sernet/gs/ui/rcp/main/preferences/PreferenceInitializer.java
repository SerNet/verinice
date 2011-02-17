/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak[at]sernet[dot]de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *     You should have received a copy of the GNU Lesser General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Alexander Koderman <ak[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.preferences;

import java.io.File;

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
				+ File.separator + "it-grundschutz2008_html_de_zip.zip"); //$NON-NLS-1$

		store.setDefault(PreferenceConstants.OOTEMPLATE, CnAWorkspace
				.getInstance().getWorkdir()
				+ File.separator + "office" + File.separator + "report.ods"); //$NON-NLS-1$ //$NON-NLS-2$

		store.setDefault(PreferenceConstants.OOTEMPLATE_TEXT, CnAWorkspace
				.getInstance().getWorkdir()
				+ File.separator + "office" + File.separator + "report.odt"); //$NON-NLS-1$ //$NON-NLS-2$

		if (System.getProperty("os.name").toLowerCase().matches(".*windows.*") //$NON-NLS-1$ //$NON-NLS-2$
				|| System.getProperty("os.name").toLowerCase() //$NON-NLS-1$
						.matches(".*nt.*")) { //$NON-NLS-1$
			store.setDefault(PreferenceConstants.OODIR, "C:\\Programme"); //$NON-NLS-1$
		} else {
			store.setDefault(PreferenceConstants.OODIR, "/opt"); //$NON-NLS-1$
		}

		store.setDefault(PreferenceConstants.ERRORPOPUPS, true);
		store.setDefault(PreferenceConstants.FIRSTSTART, true);
		store.setDefault(PreferenceConstants.INPUTHINTS, true);

		store.setDefault(PreferenceConstants.DB_DRIVER,
				PreferenceConstants.DB_DRIVER_DERBY);
		
		store.setDefault(PreferenceConstants.DB_DIALECT,
				PreferenceConstants.DB_DIALECT_derby);
		
		String derbyUrl = PreferenceConstants.DB_URL_DERBY.replace("%s",CnAWorkspace //$NON-NLS-1$
				.getInstance().getWorkdir().replaceAll("\\\\", "/") );
		store.setDefault(PreferenceConstants.DB_URL,
				derbyUrl);
		
		store.setDefault(PreferenceConstants.DB_USER, ""); //$NON-NLS-1$
		store.setDefault(PreferenceConstants.DB_PASS, ""); //$NON-NLS-1$

		store.setDefault(PreferenceConstants.GS_DB_URL, PreferenceConstants.GS_DB_URL_LOCALHOST); //$NON-NLS-1$
		store.setDefault(PreferenceConstants.GS_DB_USER, PreferenceConstants.GS_DB_USER_DEFAULT); //$NON-NLS-1$
		store.setDefault(PreferenceConstants.GS_DB_PASS, ""); //$NON-NLS-1$
		
		store.setDefault(PreferenceConstants.OPERATION_MODE, PreferenceConstants.OPERATION_MODE_INTERNAL_SERVER);
		
		store.setDefault(PreferenceConstants.VNSERVER_URI, PreferenceConstants.VNSERVER_URI_DEFAULT);
		
		store.setDefault(PreferenceConstants.HUI_TAGS, "Risk");
		store.setDefault(PreferenceConstants.HUI_TAGS_STRICT, false);
		

	}

}
