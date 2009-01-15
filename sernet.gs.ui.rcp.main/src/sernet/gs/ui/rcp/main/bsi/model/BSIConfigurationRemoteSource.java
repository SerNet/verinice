package sernet.gs.ui.rcp.main.bsi.model;

import java.util.Properties;

import sernet.gs.ui.rcp.main.service.IConfiguration;

/**
 * No configuration, use remote scraper instead. 
 * 
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public class BSIConfigurationRemoteSource implements IBSIConfig {


	public String getCacheDir() {
		return null;
	}

	public String getDsPath() {
		return null;
	}

	public String getGsPath() {
		return null;
	}

	public boolean isFromZipFile() {
		return false;
	}

}
