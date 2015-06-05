/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak[at]sernet[dot]de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *     You should have received a copy of the GNU Lesser General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Alexander Koderman <ak[at]sernet[dot]de> - initial API and implementation
 *     Robert Schuster <r.schuster@tarent.de> - inject typeFactory by Spring
 ******************************************************************************/
package sernet.hui.common.connect;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import sernet.hui.common.VeriniceContext;
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
 * @author koderman[at]sernet[dot]de
 * @author Rober Schuster
 * @version $Rev$ $LastChangedDate$ $LastChangedBy$
 * 
 */
public class HitroUtil {

	private static final Logger LOG = Logger.getLogger(HitroUtil.class);

	private HUITypeFactory typeFactory;
	
	private URL url;
	
	private IEntityResolverFactory resolverFactory;

	public static HitroUtil getInstance() {
		return (HitroUtil) VeriniceContext.get(VeriniceContext.HITRO_UTIL);
	}

	/** Used from within the server only. */
	public void initForServer() {
		if (typeFactory == null){
			throw new IllegalStateException(
					"type factory instance does not exist yet. This is not expected for the server!");
		}
		LOG.debug("Initializing server's HitroUI framework");
		resolverFactory.createResolvers(typeFactory);
	}

	/** Used from within the client only. */
	public void initForClient() {
		// For the client no HUITypeFactory instance should be available at this
		// point.
		// If it is, something went wrong (called this method twice?).
		if (typeFactory != null){
			throw new IllegalStateException(
					"Type factory instance already exists. This is not expected for the client!");
		}
		if (url == null){
			throw new IllegalStateException(
					"Property 'url' is not set. This is not expected for the client!");
		}
		LOG.debug("Initializing client's HitroUI framework");
		initForClientImpl(url, resolverFactory);
	}

	/** Used for tests only. */
	public void init(String server, IEntityResolverFactory resolverFactory) {
		URL huiConfig;
		try {
			huiConfig = new URL(server + "/GetHitroConfig");
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
		initForClientImpl(huiConfig, resolverFactory);
	}

	private void initForClientImpl(URL url, IEntityResolverFactory resolverFactory) {
		// When the internal server is used, then it may not be started yet (it is
		// intentionally delayed) and therefore the creation of the type factory
		// fails (which is provided by the server).
		//
		// We create a HUITypeFactory instance which
		// can initialize itself lazily.
		typeFactory = new DelegatingHUITypeFactory(url, resolverFactory);
	}

	public void setTypeFactory(HUITypeFactory typeFactory) {
		// This method must be called only once (by the Spring IoC container).
		if (this.typeFactory != null){
			throw new IllegalStateException("Type factory instance already exists. This method must not be called twice.");
		}
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
	
	public void setEntityResolverFactory(IEntityResolverFactory resolverFactory)
	{
		this.resolverFactory = resolverFactory;
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
		
		private static final Logger LOG_0 = Logger.getLogger(DelegatingHUITypeFactory.class);
		
		// dont't use typeFactory directly, use getTypeFactory() instead !
		private HUITypeFactory typeFactory;
		
		private URL url;
		
		private IEntityResolverFactory resolverFactory;
		
		DelegatingHUITypeFactory(URL url, IEntityResolverFactory resolverFactory) {
			super();
			this.url = url;
			this.resolverFactory = resolverFactory;
		}
		
		private void initDelegate()
		{
			synchronized (url) {
				if (typeFactory != null){
					return;
				}
				try {
					typeFactory = HUITypeFactory.createInstance(url);
					
					resolverFactory.createResolvers(typeFactory);
				} catch (DBException e) {
					LOG_0.error("Unable to reach document: " + url, e);
					
					// TODO rschuster: Provide a message which is informative
					// to the user.
				}
			}
		}
		
		private HUITypeFactory getTypeFactory() {
		    if(typeFactory==null) {
		        initDelegate();
		    }
		    return typeFactory;
		}
		
		@Override
        public EntityType getEntityType(String id) {
			return getTypeFactory().getEntityType(id);
		}
		
		@Override
        public Collection<EntityType> getAllEntityTypes() {
			return getTypeFactory().getAllEntityTypes();
		}
		
		@Override
        public List<PropertyType> getURLPropertyTypes() {
			return getTypeFactory().getURLPropertyTypes();
		}

		@Override
        public PropertyType getPropertyType(String entityTypeID, String id) {
			return getTypeFactory().getPropertyType(entityTypeID, id);
		}

		
		@Override
        public HuiRelation getRelation(String typeId) {
			return getTypeFactory().getRelation(typeId);
		}
		
		/* (non-Javadoc)
		 * @see sernet.hui.common.connect.HUITypeFactory#getPossibleRelations(java.lang.String, java.lang.String)
		 */
		@Override
		public Set<HuiRelation> getPossibleRelations(String fromEntityTypeID, String toEntityTypeID) {
		    return getTypeFactory().getPossibleRelations(fromEntityTypeID, toEntityTypeID);
		}
		
		/* (non-Javadoc)
		 * @see sernet.hui.common.connect.HUITypeFactory#getPossibleRelationsTo(java.lang.String)
		 */
		@Override
		public Set<HuiRelation> getPossibleRelationsTo(String toEntityTypeID) {
		    return getTypeFactory().getPossibleRelationsTo(toEntityTypeID);
		}
		
		/* (non-Javadoc)
		 * @see sernet.hui.common.connect.HUITypeFactory#getAllTags()
		 */
		@Override
		public Set<String> getAllTags() {
		    return getTypeFactory().getAllTags();
		}
		
		/* (non-Javadoc)
		 * @see sernet.hui.common.connect.HUITypeFactory#getPossibleRelationsFrom(java.lang.String)
		 */
		@Override
		public Set<HuiRelation> getPossibleRelationsFrom(String fromEntityTypeID) {
		    return getTypeFactory().getPossibleRelationsFrom(fromEntityTypeID);
		}
		
		
		/* (non-Javadoc)
		 * @see sernet.hui.common.connect.HUITypeFactory#getMessage(java.lang.String)
		 */
		@Override
		public String getMessage(String key) {
		    return getTypeFactory().getMessage(key);
		}
	}

}
