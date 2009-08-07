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

import org.apache.log4j.Logger;

import sernet.gs.ui.rcp.main.bsi.model.GSScraperUtil;
import sernet.gs.ui.rcp.main.common.model.IProgress;
import sernet.gs.ui.rcp.main.service.HibernateCommandService;
import sernet.hui.common.VeriniceContext;

/**
 * Initialize environemnt on Verinice server on startup.
 * 
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public class ServerInitializer {
	
	private ServerConfiguration configuration;
	
	private VeriniceContext.State workObjects;
	
	private HibernateCommandService hibernateCommandService;
	
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
		// After this we can use the getInstance() methods from HitroUtil and
		// GSScraperUtil
		VeriniceContext.setState(workObjects);
		
		// The work objects in the HibernateCommandService can only be set
		// at this point because otherwise we would have a circular dependency
		// in the Spring configuration (= commandService needs workObjects
		// and vice versa)
		hibernateCommandService.setWorkObjects(workObjects);
		
		GSScraperUtil gsScraperUtil = GSScraperUtil.getInstance();
		// initialize grundschutz scraper:
		try {
			gsScraperUtil.getModel().loadBausteine(nullMonitor);
		} catch (Exception e) {
			Logger.getLogger(this.getClass()).error("Error while loading Grundschutzkataloge", e);
		}
	}

	public ServerConfiguration getConfiguration() {
		return configuration;
	}

	public void setConfiguration(ServerConfiguration configuration) {
		this.configuration = configuration;
	}

	public void setWorkObjects(VeriniceContext.State workObjects) {
		this.workObjects = workObjects;
	}

	public VeriniceContext.State getWorkObjects() {
		return workObjects;
	}

	public void setHibernateCommandService(HibernateCommandService hibernateCommandService) {
		this.hibernateCommandService = hibernateCommandService;
	}

	public HibernateCommandService getHibernateCommandService() {
		return hibernateCommandService;
	}

}
