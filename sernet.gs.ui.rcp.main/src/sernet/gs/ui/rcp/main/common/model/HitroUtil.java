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
package sernet.gs.ui.rcp.main.common.model;

import java.io.File;
import java.io.InputStream;
import java.net.URL;

import org.apache.log4j.Logger;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.CnAWorkspace;
import sernet.gs.ui.rcp.main.bsi.model.EntityResolverFactory;
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.WhereAmIUtil;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.snutils.DBException;

/**
 * Returns HitroUI type factory using configuration available in RCP client's
 * workspace or server.
 * 
 * The user editable HitroUI (or "HUI") configuration determines the fields
 * ("properties") of all business objects available in the application.
 * 
 * It is important that all clients work with the same HUI representation,
 * otherwise some fields may be missing on one client.
 * 
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ $LastChangedBy$
 * 
 */
public class HitroUtil {
	private HUITypeFactory typeFactory;
	private static HitroUtil instance = new HitroUtil();

	/**
	 * Create HitroUI Configuration from XML file in workspace. (On RCP rich
	 * client)
	 */
	private void initHitroUIFromWorkspace() {
		try {

			String huiConfig = String
					.format("%s%sconf%sSNCA.xml", CnAWorkspace.getInstance()
							.getWorkdir(), File.separator, File.separator);
			huiConfig = (new File(huiConfig)).toURI().toString();
			initHitroUI(huiConfig);
		} catch (DBException e) {
			throw new RuntimeException(e);
		}
	}

	private void initHitroUIFromClasspath() {
		try {
			URL resource = this.getClass().getClassLoader().getResource(
					"sernet/gs/server/SNCA.xml");
			String huiConfig = resource != null ? resource.toString() : "";
			initHitroUI(huiConfig);
		} catch (DBException e) {
			throw new RuntimeException(e);
		}
	}

	private void initHitroUI(String huiConfig) throws DBException {
		Logger.getLogger(HitroUtil.class).debug(
				"Getting type definition from: " + huiConfig);
		HUITypeFactory.initialize(huiConfig);
		typeFactory = HUITypeFactory.getInstance();
		
		EntityResolverFactory.createResolvers(typeFactory);
		
		Logger.getLogger(HitroUtil.class).debug("HUI initialized.");
	}

	private HitroUtil() {
	}

	private void initHitroUIFromServer(String uri) {
		try {
			initHitroUI(uri);
		} catch (DBException e) {
			throw new RuntimeException(e);
		}
	}
	
	private void initHitroUIFromServer() {
		String server = Activator.getDefault().getPluginPreferences().getString(PreferenceConstants.VNSERVER_URI);
		String huiConfig = server + "/GetHitroConfig";
		initHitroUIFromServer(huiConfig);
	}

	public static HitroUtil getInstance() {
		return instance;
	}

	public HUITypeFactory getTypeFactory() {
		return typeFactory;
	}

	public void init() {
		Logger.getLogger(HitroUtil.class).debug(
				"Initializing Hitro-UI framework...");

		if (WhereAmIUtil.runningOnClient())
			if (ServiceFactory.isUsingRemoteService())
				initHitroUIFromServer(); // init from server using HTTP uri
			else
				initHitroUIFromWorkspace(); // init from workspace folder:
		// workspace/conf/SNCA.xml
		else
			initHitroUIFromClasspath(); // init from classpath, i.e. on tomcat:
		// WEB-INF/classes/SNCA.xml

	}
	
	public void init(String server) {
		String huiConfig = server + "/GetHitroConfig";
		initHitroUIFromServer(huiConfig);
	}
}
