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

import org.apache.log4j.Logger;

import sernet.gs.ui.rcp.main.connect.BSIConfigurationServer;
import sernet.gs.ui.rcp.main.service.IConfiguration;
import sernet.hui.common.VeriniceContext;

public class GSScraperUtil {
	
	/** Resource injected by Spring (so far, only on the server).
	 */
	IConfiguration configuration;
	
	BSIMassnahmenModel model;

	public IConfiguration getConfiguration() {
		return configuration;
	}

	public void setConfiguration(IConfiguration configuration) {
		this.configuration = configuration;
	}

	private GSScraperUtil() {
		Logger.getLogger(GSScraperUtil.class).debug(
		"Initializing GS catalogues service...");
	}

	public static GSScraperUtil getInstance() {
		return (GSScraperUtil) VeriniceContext.get(VeriniceContext.GS_SCRAPER_UTIL);
	}

	/** Initialization method for the client. */
	public void initForClient() {
		Logger.getLogger(this.getClass()).debug("Initializing client Grundschutz scraper...");
		BSIConfigurationRemoteSource config = new BSIConfigurationRemoteSource();
		model = new BSIMassnahmenModel(config);
	}

	/** Initialization method for the server. */
	public void initForServer() {
		Logger.getLogger(this.getClass()).debug("Initializing server Grundschutz scraper...");
		BSIConfigurationServer config = new BSIConfigurationServer(configuration.getProperties());
		model = new BSIMassnahmenModel(config);
	}
	
	public BSIMassnahmenModel getModel()
	{
		return model;
	}

}
