package sernet.gs.server;

import java.util.Properties;

import sernet.gs.ui.rcp.main.service.IConfiguration;

public class ServerConfiguration implements IConfiguration {
	
	private Properties properties;

	public Properties getProperties() {
		return properties;
	}
	
	public void setProperties(Properties properties) {
		this.properties = properties;
	}

}
