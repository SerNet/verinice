package sernet.gs.server;

import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.access.BeanFactoryLocator;
import org.springframework.beans.factory.access.BeanFactoryReference;
import org.springframework.beans.factory.access.SingletonBeanFactoryLocator;

import sernet.gs.service.GSServiceException;
import sernet.gs.ui.rcp.main.bsi.model.BSIMassnahmenModel;
import sernet.gs.ui.rcp.main.bsi.model.GSScraperUtil;
import sernet.gs.ui.rcp.main.common.model.HitroUtil;
import sernet.gs.ui.rcp.main.common.model.IProgress;
import sernet.gs.ui.rcp.main.service.ICommandService;
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
public class ServerInitializer {
	
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
		HitroUtil.getInstance().init();
		
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

	
}
