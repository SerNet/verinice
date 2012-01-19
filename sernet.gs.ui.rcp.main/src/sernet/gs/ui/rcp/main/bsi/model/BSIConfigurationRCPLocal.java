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
package sernet.gs.ui.rcp.main.bsi.model;

import java.io.File;
import java.io.Serializable;
import java.net.MalformedURLException;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Preferences;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.CnAWorkspace;
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;

public class BSIConfigurationRCPLocal implements IBSIConfig, Serializable {
	
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
