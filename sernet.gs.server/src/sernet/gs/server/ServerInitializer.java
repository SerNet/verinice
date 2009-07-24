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
package sernet.gs.server;

import java.io.File;

import javax.servlet.ServletConfig;

import org.apache.log4j.Logger;
import org.springframework.web.context.ServletConfigAware;

import sernet.gs.ui.rcp.main.bsi.model.BSIMassnahmenModel;
import sernet.gs.ui.rcp.main.bsi.model.GSScraperUtil;
import sernet.gs.ui.rcp.main.common.model.HitroUtil;
import sernet.gs.ui.rcp.main.common.model.IProgress;
import sernet.gs.ui.rcp.main.service.IConfiguration;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.WhereAmIUtil;

/**
 * Initialize environemnt on Verinice server on startup.
 * 
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public class ServerInitializer implements ServletConfigAware {
	
	ServletConfig servletConfig;
	
	public IConfiguration configuration;
	
	private IProgress nullMonitor = new IProgress() {
		public void beginTask(String name, int totalWork) {
		}

		public void done() {
		}

		public void setTaskName(String string) {
		}

		public void subTask(String string) {
		}

		public void worked(int work) {
		}
		
	};

	public void initialize() {
		Logger.getLogger(this.getClass()).debug("Initializing server context...");
		// basic Verinice client setup:
		ServiceFactory.setService(ServiceFactory.LOCAL);
		
		// tell me where to find HitroUI configuration and other stuff:
		WhereAmIUtil.setLocation(WhereAmIUtil.LOCATION_SERVER);
		
		// initialize HitroUI type factory:
		Logger.getLogger(this.getClass()).debug("Initializing server HitroUI types...");
		HitroUtil.getInstance().init(new File(servletConfig.getServletContext().getContextPath(), "WEB-INF" + File.separator + "SNCA.xml"));
		
		// initialize grundschutz scraper:
		Logger.getLogger(this.getClass()).debug("Initializing server Grundschutz scraper...");
		GSScraperUtil.getInstance().init(configuration.getProperties());
		try {
			BSIMassnahmenModel.loadBausteine(nullMonitor);
		} catch (Exception e) {
			Logger.getLogger(this.getClass()).error("Error while loading Grundschutzkataloge", e);
		}
	}

	public  IConfiguration getConfiguration() {
		return configuration;
	}

	public void setConfiguration(IConfiguration configuration) {
		this.configuration = configuration;
	}

	@Override
	public void setServletConfig(ServletConfig servletConfig) {
		this.servletConfig = servletConfig;
	}

	

	
}
