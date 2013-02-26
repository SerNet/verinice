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
 ******************************************************************************/
package sernet.verinice.service.commands;

import java.io.Serializable;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

import sernet.gs.service.RetrieveInfo;
import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.Property;
import sernet.hui.common.connect.PropertyList;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IAuthAwareCommand;
import sernet.verinice.interfaces.IAuthService;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.common.configuration.Configuration;

/**
 * Save element of type T to the database using its class to lookup the DAO from
 * the factory.
 * 
 * @author koderman[at]sernet[dot]de
 * @version $Rev$ $LastChangedDate$ $LastChangedBy$
 * 
 * @param <T>
 */
public class SaveConfiguration<T extends Configuration> extends GenericCommand implements IAuthAwareCommand {

	private transient Logger log = Logger.getLogger(SaveConfiguration.class);

    public Logger getLog() {
        if (log == null) {
            log = Logger.getLogger(SaveConfiguration.class);
        }
        return log;
    }
	
	private T element;
	private boolean updatePassword;

	private transient IAuthService authService;
	
	private transient IBaseDao<T, Serializable> dao;
	
	private transient IBaseDao<Entity, Serializable> entityDao;

	/**
	 * Save a configuration item
	 * 
	 * @param element
	 *            item to save / update
	 * @param updatePassword
	 *            was the password newly entered and needs to be hashed?
	 */
	public SaveConfiguration(T element, boolean updatePassword) {
		this.element = element;
		this.updatePassword = updatePassword;
	}

	
	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.service.commands.ICommand#execute()
	 */
	public void execute() {
		try {
			// check if username is unique
			checkUsername(element);
			
			if (updatePassword) {
				hashPassword();
			}
			
			element = getDao().merge(element);
	
			// The roles may have been modified. As such the server needs to throw
			// away its
			// cached role data.
			getCommandService().discardUserData();
		} catch(UsernameExistsRuntimeException uere) {
			// already logged in checkUsername
			throw uere;
		} catch(RuntimeException re) {
			getLog().error("Runtime Error while saving configuration", re);
			throw re;
		} catch(Exception re) {
			getLog().error("Error while saving configuration", re);
			throw new RuntimeException(re);
		}
	}

	/**
	 * Checks if the username in a {@link Configuration} is unique in the database.
	 * Throws {@link UsernameExistsRuntimeException} if username is not available.
	 * If username is not set or null no exception is thrown
	 * 
	 * @param element a {@link Configuration}
	 * @throws UsernameExistsRuntimeException if username is not available
	 */
	private void checkUsername(T element) throws UsernameExistsRuntimeException {
		if(element!=null && element.getEntity()!=null && element.getEntity().getProperties(Configuration.PROP_USERNAME)!=null) {		
			PropertyList usernamePropertyList = element.getEntity().getProperties(Configuration.PROP_USERNAME);
			Property usernameProperty = usernamePropertyList.getProperty(0);
			if(usernameProperty!=null && usernameProperty.getPropertyValue()!=null) {
				String username = usernameProperty.getPropertyValue();
				if(getAuthService().getAdminUsername().equals(username)) {
				    if (getLog().isDebugEnabled()) {
                        getLog().debug("Username is admin name: " + username);
                    }
				    throw new UsernameExistsRuntimeException(username,"Username already exists: " + username);
				}
				
				DetachedCriteria criteria = DetachedCriteria.forClass(Property.class);
				criteria.add(Restrictions.eq("propertyType", Configuration.PROP_USERNAME));
				criteria.add(Restrictions.like("propertyValue", username));
				
				List resultList = getDao().findByCriteria(criteria);
				if(resultList!=null && !resultList.isEmpty()) {
					// save only if this is really the same user object:
					boolean doubleUsername = false;
					
					// reload entity because in some causes new entities alredy exists in db 
					getEntityDao().findByUuid(element.getEntity().getUuid(), new RetrieveInfo());
					usernamePropertyList = element.getEntity().getProperties(Configuration.PROP_USERNAME);
					
					checkDoubles: for (Object t : resultList) {
						Property foundProperty = (Property) t;
						
						if ( (usernamePropertyList==null || usernamePropertyList.getDbId() == null) // current object was never saved, found name must be double
								|| !usernamePropertyList.getDbId().equals(foundProperty.getDbId()) ) { // current dbId doesn't match found username, is double
							doubleUsername = true;
							break checkDoubles;
						}
					}
					if (doubleUsername) {
						if (getLog().isDebugEnabled()) {
							getLog().debug("Username exists: " + username);
						}
						throw new UsernameExistsRuntimeException(username,"Username already exists: " + username);
					}
				}
			}
		}
	}


	private void hashPassword() {
		Property passProperty = element.getEntity().getProperties(Configuration.PROP_PASSWORD).getProperty(0);
		Property userProperty = element.getEntity().getProperties(Configuration.PROP_USERNAME).getProperty(0);

		String hash = getAuthService().hashPassword(userProperty.getPropertyValue(), passProperty.getPropertyValue());
		passProperty.setPropertyValue(hash, false);
	}

	public T getElement() {
		return element;
	}

	public IAuthService getAuthService() {
		return this.authService;
	}
	
	public IBaseDao<Entity, Serializable> getEntityDao() {
		if (entityDao == null) {
			entityDao = createEntityDao();
		}
		return entityDao;
	}

	private IBaseDao<Entity, Serializable> createEntityDao() {
		return (IBaseDao<Entity, Serializable>) getDaoFactory().getDAO(Entity.class);
	}

	public void setAuthService(IAuthService service) {
		this.authService = service;
	}
	
	public IBaseDao<T, Serializable> getDao() {
		if (dao == null) {
			dao = createDao();
		}
		return dao;
	}

	private IBaseDao<T, Serializable> createDao() {
		return (IBaseDao<T, Serializable>) getDaoFactory().getDAO(element.getTypeId());
	}


}
