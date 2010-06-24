/*******************************************************************************
 * Copyright (c) 2009 Daniel Murygin <dm[at]sernet[dot]de>.
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
 *     Daniel <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.connect;

import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import sernet.gs.common.ApplicationRoles;
import sernet.gs.common.SecurityException;
import sernet.verinice.interfaces.IAuthService;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.Permission;
import sernet.verinice.model.common.configuration.Configuration;

/**
 * Extends {@link HibernateBaseDao} to check write and delete authorization for {@link CnATreeElement}s.
 * Use this for CnATreeElement-Daos in Spring configuration
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class CnATElementDao extends HibernateBaseDao<CnATreeElement, Integer> {

	private final Logger log = Logger.getLogger(CnATElementDao.class);

	private IAuthService authService;

	private IBaseDao<Configuration, Integer> configurationDao;

	private IBaseDao<Permission, Integer> permissionDao;

	
	private static HashMap<String, String[]> roleMap = new HashMap<String, String[]>();

	/**
	 * @param type
	 */
	public CnATElementDao(Class<CnATreeElement> type) {
		super(type);
	}
	
	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.connect.HibernateBaseDao#delete(java.lang.Object)
	 */
	@Override
	public void delete(CnATreeElement entity) {
		checkRights(entity);
		super.delete(entity);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * sernet.gs.ui.rcp.main.connect.HibernateBaseDao#merge(java.lang.Object)
	 */
	@Override
	public CnATreeElement merge(CnATreeElement entity) {
		return super.merge(entity);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * sernet.gs.ui.rcp.main.connect.HibernateBaseDao#merge(java.lang.Object,
	 * boolean)
	 */
	@Override
	public CnATreeElement merge(CnATreeElement entity, boolean fireChange) {
		// check rights only while updating
		if(entity.getDbId()!=null) {
			checkRights(entity);
		}
		return super.merge(entity, fireChange);
	}

	/**
	 * @param entity
	 */
	protected void checkRights(CnATreeElement entity) /*throws SecurityException*/ { 
		if (getAuthService().isPermissionHandlingNeeded() && !hasAdminRole(authService.getRoles())) {
			final String username = getAuthService().getUsername();
			if (log.isDebugEnabled()) {
				log.debug("Username: " + username);
			}
			String[] roleArray = getDynamicRoles(username);
					
			
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < roleArray.length; i++) {
				sb.append("'").append(roleArray[i]).append("'");
				if(i<roleArray.length-1) {
					sb.append(",");
				}			
			}
			String roleParam = sb.toString();
			
			sb = new StringBuilder();
			sb.append("select p.dbId from Permission p where p.cnaTreeElement.dbId = ? and p.role in (");
			// workaraound, because adding roles as ? param does not work
			sb.append(roleParam);
			sb.append(") and p.writeAllowed = ?");
			String hql = sb.toString();
			
			Object[] params = new Object[]{entity.getDbId(),Boolean.TRUE};
			if (log.isDebugEnabled()) {
				log.debug("checkRights, hql: " + hql);
				log.debug("checkRights, entity db-id: " + entity.getDbId() );
			}
			List<Integer> idList = getPermissionDao().findByQuery(hql, params);
			if (log.isDebugEnabled()) {
				log.debug("checkRights, permission ids: ");
				for (Integer integer : idList) {
					log.debug(integer);
				}
			}
			if(idList==null | idList.isEmpty()) {
				final String message = "User: " + username + " has no right to write CnATreeElement with id: " + entity.getDbId();
				log.warn(message);
				throw new SecurityException(message);
			}
		}
	}

	private boolean hasAdminRole(String[] roles) {
		for (String r : roles) {
			if (ApplicationRoles.ROLE_ADMIN.equals(r))
				return true;
		}

		return false;
	}

	private String[] getDynamicRoles(String user) {
		String[] result = roleMap.get(user);
		if (result == null) {
			List<Configuration> configurations = getConfigurationDao().findAll();

			for (Configuration c : configurations) {
				if (user.equals(c.getUser()) && result == null) {
					result = c.getRoles().toArray(new String[c.getRoles().size()]);
					configurations.clear();

					// Put result into map and save asking the DB next time.
					roleMap.put(user, result);

					// TODO: Whenever an admin modifies the roles the roleMap
					// should be cleared.
					// We could introduce a special command just for this.

					return result;
				}
			}

			// This should not happen because the login was done using an
			// existing username
			// and if that does not exist any more something must have gone
			// wrong.
			throw new IllegalStateException();

		}

		return result;
	}

	public void setAuthService(IAuthService authService) {
		this.authService = authService;
	}

	public IAuthService getAuthService() {
		return authService;
	}

	public void setConfigurationDao(IBaseDao<Configuration, Integer> configurationDao) {
		this.configurationDao = configurationDao;
	}

	public IBaseDao<Configuration, Integer> getConfigurationDao() {
		return configurationDao;
	}

	public void setPermissionDao(IBaseDao<Permission, Integer> permissionDao) {
		this.permissionDao = permissionDao;
	}

	public IBaseDao<Permission, Integer> getPermissionDao() {
		return permissionDao;
	}

}
