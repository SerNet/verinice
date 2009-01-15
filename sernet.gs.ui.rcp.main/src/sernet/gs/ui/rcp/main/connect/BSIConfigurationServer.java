package sernet.gs.ui.rcp.main.connect;

import java.io.File;
import java.net.URL;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.access.BeanFactoryLocator;
import org.springframework.beans.factory.access.BeanFactoryReference;
import org.springframework.beans.factory.access.SingletonBeanFactoryLocator;

import sernet.gs.ui.rcp.main.bsi.model.IBSIConfig;
import sernet.gs.ui.rcp.main.service.ICommandService;
import sernet.gs.ui.rcp.main.service.IConfiguration;
import sernet.springclient.SpringClientPlugin;

public class BSIConfigurationServer implements IBSIConfig {

	private Properties properties;

	public BSIConfigurationServer(Properties properties2) {
		properties = properties2;
	}

	public String getCacheDir() {
		String property = System
				.getProperty("java.io.tmpdir")+ File.separator + "gscache";
		Logger.getLogger(this.getClass()).debug("Setting temp dir to " + property);
		return property;
	}

	public String getDsPath() {
		String property = properties.getProperty("datenschutzBaustein");
		URL resource = getClass().getClassLoader().getResource(property);
		return resource.getPath();
	}

	public String getGsPath() {
		String property = properties.getProperty("grundschutzKataloge");
		URL resource = getClass().getClassLoader().getResource(property);
		return resource.getPath();
	}

	public boolean isFromZipFile() {
		return true;
	}

}
