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
 *     Robert Schuster <r.schuster@tarent.de> - inject typeFactory by Spring
 ******************************************************************************/
package sernet.gs.ui.rcp.main.common.model;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Assert;
import org.jfree.util.Log;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.bsi.model.EntityResolverFactory;
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;
import sernet.hui.common.VeriniceContext;
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
 * <p>Instances of this class are managed by the Spring configuration.
 * In the verinice server the HUITypeFactory instance is managed by Spring
 * as well and gets injected into this class. The verinice client does this
 * step manually. That way both parts of the application behave properly.
 * </p>
 * 
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ $LastChangedBy$
 * 
 */
public class HitroUtil {
	
	private static final Logger log = Logger.getLogger(HitroUtil.class);
	
	private HUITypeFactory typeFactory;
	
	public static HitroUtil getInstance() {
		return (HitroUtil) VeriniceContext.get(VeriniceContext.HITRO_UTIL);
	}

	/** Used from within the server only. */
	public void initForServer() {
		Assert.isNotNull(getTypeFactory());
		
		Logger.getLogger(HitroUtil.class).debug(
		"Initializing HitroUI framework for server");
		EntityResolverFactory.createResolvers(getTypeFactory());
	}

	/** Used from within the client only. */
	public void initForClient() {
		// For the client no HUITypeFactory instance should be available at this point.
		// If it is, something went wrong (called this method twice?).
		Assert.isTrue(getTypeFactory() == null);
		
		Logger.getLogger(HitroUtil.class).debug(
		"Initializing HitroUI framework for client");
		String server = Activator.getDefault().getPluginPreferences().getString(PreferenceConstants.VNSERVER_URI);
		URL huiConfig;
		try {
			huiConfig = new URL(server + "/GetHitroConfig");
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
		initForClientImpl(huiConfig);
	}

	/** Used for tests only. */
	public void init(String server) {
		URL huiConfig;
		try {
			huiConfig = new URL(server + "/GetHitroConfig");
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
		initForClientImpl(huiConfig);
	}

	private void initForClientImpl(URL url) {
		try {
			typeFactory = HUITypeFactory.createInstance(url);
			
			EntityResolverFactory.createResolvers(typeFactory);
		} catch (DBException e) {
			// The reason for the reason may be that the server is not available
			// (or the URL is wrong). We do not want to prevent the application
			// start because of this (otherwise there would be no possibility
			// for the user to fix the issue).
			log.warn(e.getLocalizedMessage());
		}
	}

	public void setTypeFactory(HUITypeFactory typeFactory) {
		// This method must be called only once (by the Spring IoC container).
		Assert.isTrue(this.typeFactory == null);
		
		this.typeFactory = typeFactory;
	}

	public HUITypeFactory getTypeFactory() {
		return typeFactory;
	}


}
