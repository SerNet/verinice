/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak@sernet.de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details.
 *     You should have received a copy of the GNU General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Alexander Koderman <ak@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.bsi.model;

import java.util.Properties;

import org.apache.log4j.Logger;

import sernet.gs.ui.rcp.main.common.model.HitroUtil;
import sernet.gs.ui.rcp.main.connect.BSIConfigurationServer;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.WhereAmIUtil;

public class GSScraperUtil {

	private static GSScraperUtil instance = new GSScraperUtil();

	private GSScraperUtil() {
		// singleton
	}

	public static GSScraperUtil getInstance() {
		return instance;
	}

	public void init() {
		Logger.getLogger(HitroUtil.class).debug(
				"Initializing GS catalogues service...");

		if (WhereAmIUtil.runningOnClient()) {
			if (ServiceFactory.isUsingRemoteService())
				initFromRemoteServer();
			else
				initFromWorkspace();
		}
	}

	private void initFromRemoteServer() {
		BSIConfigurationRemoteSource config = new BSIConfigurationRemoteSource();
		BSIMassnahmenModel.setConfig(config);
	}

	/**
	 * Get configuration for Grundschutz parsing from server configuration file. 
	 * @param properties 
	 */
	private void initFromServerConfiguration(Properties properties) {
		BSIConfigurationServer config = new BSIConfigurationServer(properties);
		BSIMassnahmenModel.setConfig(config);
	}

	/**
	 * Get config for parsing from RCP preference store.
	 */
	private void initFromWorkspace() {
		BSIConfigurationRCPLocal config = new BSIConfigurationRCPLocal();
		BSIMassnahmenModel.setConfig(config);
	}

	public void init(Properties properties) {
		initFromServerConfiguration(properties);
	}

}
