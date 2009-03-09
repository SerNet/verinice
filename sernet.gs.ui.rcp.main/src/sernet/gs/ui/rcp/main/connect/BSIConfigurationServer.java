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
