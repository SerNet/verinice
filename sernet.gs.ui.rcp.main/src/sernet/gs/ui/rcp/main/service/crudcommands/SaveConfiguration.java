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
package sernet.gs.ui.rcp.main.service.crudcommands;

import java.io.Serializable;
import java.util.List;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

import sernet.gs.ui.rcp.main.common.model.configuration.Configuration;
import sernet.gs.ui.rcp.main.connect.IBaseDao;
import sernet.gs.ui.rcp.main.service.IAuthService;
import sernet.gs.ui.rcp.main.service.ICommandService;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.commands.GenericCommand;
import sernet.gs.ui.rcp.main.service.commands.IAuthAwareCommand;
import sernet.gs.ui.rcp.main.service.commands.UsernameExistsException;
import sernet.gs.ui.rcp.main.service.commands.UsernameExistsRuntimeException;
import sernet.hui.common.connect.Property;

/**
 * Save element of type T to the database using its class to lookup the DAO from
 * the factory.
 * 
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ $LastChangedBy$
 * 
 * @param <T>
 */
public class SaveConfiguration<T extends Configuration> extends GenericCommand implements IAuthAwareCommand {

	private T element;
	private boolean updatePassword;

	private transient IAuthService authService;
	
	private transient IBaseDao<T, Serializable> dao;

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
		// check if username is unique
		checkUsername(element);
		
		if (updatePassword) {
			hashPassword();
		}
		
		element = getDao().merge(element);

		// The roles may have been modified. As such the server needs to throw
		// away its
		
		// cached role data.
		getCommandService().discardRoleMap();
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
			Property usernameProperty = element.getEntity().getProperties(Configuration.PROP_USERNAME).getProperty(0);
			if(usernameProperty!=null && usernameProperty.getPropertyValue()!=null) {
				String username = usernameProperty.getPropertyValue();
				
				DetachedCriteria criteria = DetachedCriteria.forClass(Property.class);
				criteria.add(Restrictions.eq("propertyType", Configuration.PROP_USERNAME));
				criteria.add(Restrictions.eq("propertyValue", username));
				
				List<T> resultList = getDao().findByCriteria(criteria);
				if(resultList!=null && !resultList.isEmpty()) {
					throw new UsernameExistsRuntimeException(username,"Username already exists: " + username);
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
		return (IBaseDao<T, Serializable>) getDaoFactory().getDAO(element.getClass());
	}


}
