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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;

import sernet.gs.ui.rcp.main.bsi.model.EntityResolverFactory;
import sernet.hui.common.VeriniceContext;
import sernet.hui.common.connect.EntityType;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.hui.common.connect.PropertyGroup;
import sernet.hui.common.connect.PropertyOption;
import sernet.hui.common.connect.PropertyType;
import sernet.hui.common.multiselectionlist.IMLPropertyOption;
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
 * <p>
 * Instances of this class are managed by the Spring configuration. In the
 * verinice server the HUITypeFactory instance is managed by Spring as well and
 * gets injected into this class. The verinice client does this step manually.
 * That way both parts of the application behave properly.
 * </p>
 * 
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ $LastChangedBy$
 * 
 */
public class HitroUtil {

	private static final Logger log = Logger.getLogger(HitroUtil.class);

	private HUITypeFactory typeFactory;
	
	private URL url;

	public static HitroUtil getInstance() {
		return (HitroUtil) VeriniceContext.get(VeriniceContext.HITRO_UTIL);
	}

	/** Used from within the server only. */
	public void initForServer() {
		if (typeFactory == null)
			throw new IllegalStateException(
					"type factory instance does not exist yet. This is not expected for the server!");

		log.debug("Initializing server's HitroUI framework");
		EntityResolverFactory.createResolvers(typeFactory);
	}

	/** Used from within the client only. */
	public void initForClient() {
		// For the client no HUITypeFactory instance should be available at this
		// point.
		// If it is, something went wrong (called this method twice?).
		if (typeFactory != null)
			throw new IllegalStateException(
					"Type factory instance already exists. This is not expected for the client!");

		if (url == null)
			throw new IllegalStateException(
					"Property 'url' is not set. This is not expected for the client!");

		log.debug("Initializing client's HitroUI framework");
		initForClientImpl(url);
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
			log.warn("creating type factory for client failed: " + e.getLocalizedMessage());
			
			// When the internal server is used, then it may not be started yet (it is
			// intentionally delayed) and therefore the creation of the type factory
			// fails (which is provided by the server).
			//
			// In such a case we create a HUITypeFactory instance which
			// can initialize itself lazily.
			typeFactory = new DelegatingHUITypeFactory(url);
		}
	}

	public void setTypeFactory(HUITypeFactory typeFactory) {
		// This method must be called only once (by the Spring IoC container).
		if (this.typeFactory != null)
			throw new IllegalStateException("Type factory instance already exists. This method must not be called twice.");

		this.typeFactory = typeFactory;
	}

	public HUITypeFactory getTypeFactory() {
		return typeFactory;
	}

	public void setUrl(URL url) {
		this.url = url;
	}

	public URL getUrl() {
		return url;
	}

	/**
	 * An implementation of {@link HUITypeFactory} which tries to initialize an
	 * instance of that class when its methods are called.
	 * 
	 * <p>The idea is that the initialization will be successful when the
	 * <code>HUITypeFactory</code> instance is really needed.</p>
	 * 
	 */
	static class DelegatingHUITypeFactory extends HUITypeFactory {
		
		private static final Logger log = Logger.getLogger(DelegatingHUITypeFactory.class);
		
		private HUITypeFactory typeFactory;
		
		private URL url;
		
		DelegatingHUITypeFactory(URL url) {
			super();
			
			this.url = url;
		}
		
		private void initDelegate()
		{
			synchronized (url) {
				if (typeFactory != null)
					return;
				
				try {
					typeFactory = HUITypeFactory.createInstance(url);
					
					EntityResolverFactory.createResolvers(typeFactory);
				} catch (DBException e) {
					log.warn("Unable to reach document: " + url);
					
					// TODO rschuster: Provide a message which is informative
					// to the user.
				}
			}
		}
		
		public EntityType getEntityType(String id) {
			initDelegate();
			
			return typeFactory.getEntityType(id);
		}
		
		public Collection<EntityType> getAllEntityTypes() {
			initDelegate();
			
			return typeFactory.getAllEntityTypes();
		}
		
		public List<PropertyType> getURLPropertyTypes() {
			initDelegate();
			
			return typeFactory.getURLPropertyTypes();
		}
		
		public PropertyType readPropertyType(String id) {
			initDelegate();
			
			return typeFactory.readPropertyType(id);
		}
			
		public PropertyGroup readPropertyGroup(String id) {
			initDelegate();
			
			return typeFactory.readPropertyGroup(id);
		}

		public ArrayList getOptionsForPropertyType(String id) {
			initDelegate();
			
			return typeFactory.getOptionsForPropertyType(id);
		}
		
		public PropertyOption getOptionById(String valueId) {
			initDelegate();
			
			return typeFactory.getOptionById(valueId);
		}

		public List<PropertyType> getAllPropertyTypes(String entityTypeID) {
			initDelegate();
			
			return typeFactory.getAllPropertyTypes(entityTypeID);
		}

		public PropertyType getPropertyType(String entityTypeID, String id) {
			initDelegate();
			
			return typeFactory.getPropertyType(entityTypeID, id);
		}

		public boolean isDependency(IMLPropertyOption opt) {
			initDelegate();
			
			return typeFactory.isDependency(opt);
		}
	}

}
