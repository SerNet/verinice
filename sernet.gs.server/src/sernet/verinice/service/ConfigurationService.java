/*******************************************************************************
 * Copyright (c) 2011 Daniel Murygin.
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.service;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.hibernate.FetchMode;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

import sernet.gs.service.CollectionUtil;
import sernet.gs.service.RetrieveInfo;
import sernet.hui.common.connect.IPerson;
import sernet.verinice.interfaces.ApplicationRoles;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.IAuthService;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.interfaces.IConfigurationService;
import sernet.verinice.interfaces.IDao;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.Permission;
import sernet.verinice.model.common.PersonAdapter;
import sernet.verinice.model.common.configuration.Configuration;
import sernet.verinice.service.commands.LoadCurrentUserConfiguration;

/**
 * Thread save implementation of {@link IConfigurationService}.
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class ConfigurationService implements IConfigurationService {

    private static final Logger LOG = Logger.getLogger(ConfigurationService.class);

    private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final Lock readLock = readWriteLock.readLock();
    private final Lock writeLock = readWriteLock.writeLock();

    private Map<String, String[]> roleMap = new HashMap<>();
    private Map<String, Boolean> scopeMap = new HashMap<>();
    private Map<String, Integer> scopeIdMap = new HashMap<>();
    private Map<String, String> nameMap = new HashMap<>();

    private IBaseDao<Configuration, Serializable> configurationDao;
    private IBaseDao<CnATreeElement, Long> cnaTreeElementDao;

    private IAuthService authService;
    private ICommandService commandService;

    private void loadUserData() {
        DetachedCriteria criteria = DetachedCriteria.forClass(Configuration.class);
        criteria.setFetchMode("entity", FetchMode.JOIN);
        criteria.setFetchMode("entity.typedPropertyLists", FetchMode.JOIN);
        criteria.setFetchMode("entity.typedPropertyLists.properties", FetchMode.JOIN);
        criteria.setFetchMode("entity.typedPropertyLists.properties", FetchMode.JOIN);
        criteria.setFetchMode("person", FetchMode.JOIN);

        @SuppressWarnings("unchecked")
        List<Configuration> configurations = getConfigurationDao().findByCriteria(criteria);
        Set<Integer> personIDs = configurations.stream().map(c -> c.getPerson().getDbId())
                .collect(Collectors.toSet());
        Map<Integer, CnATreeElement> personsById = new HashMap<>(personIDs.size());
        CollectionUtil.partition(List.copyOf(personIDs), IDao.QUERY_MAX_ITEMS_IN_LIST)
                .forEach(partition -> {
                    DetachedCriteria crit = DetachedCriteria.forClass(IPerson.class)
                            .add(Restrictions.in("dbId", partition));
                    RetrieveInfo.getPropertyInstance().configureCriteria(crit);
                    List<CnATreeElement> persons = getConfigurationDao().findByCriteria(crit);
                    for (CnATreeElement person : persons) {
                        personsById.put(person.getDbId(), person);
                    }
                });

        // Block all other threads before filling the maps
        writeLock.lock();
        try {
            StringBuilder sb = new StringBuilder();
            for (Configuration c : configurations) {
                sb.setLength(0);
                String[] roleArray = getRoles(c);
                String user = c.getUser();
                // Put result into map and save asking the DB next time.
                roleMap.put(user, roleArray);
                scopeMap.put(user, c.isScopeOnly());
                CnATreeElement person = c.getPerson();
                if (person != null) {
                    scopeIdMap.put(user, person.getScopeId());
                    sb.append(PersonAdapter.getFullName(person)).append(" [").append(c.getUser())
                            .append("]");
                    nameMap.put(user, sb.toString());
                }
            }
            String[] adminRoleArray = new String[] { ApplicationRoles.ROLE_ADMIN,
                    ApplicationRoles.ROLE_WEB, ApplicationRoles.ROLE_USER };
            roleMap.put(getAuthService().getAdminUsername(), adminRoleArray);
            scopeMap.put(getAuthService().getAdminUsername(), false);
        } finally {
            writeLock.unlock();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * sernet.verinice.service.IConfigurationService#setRoles(java.lang.String,
     * java.lang.String[])
     */
    @Override
    public void setRoles(String user, String[] roles) {
        // Block all other threads before filling the maps
        writeLock.lock();
        try {
            roleMap.put(user, roles);
        } finally {
            writeLock.unlock();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * sernet.verinice.service.IConfigurationService#setScopeOnly(java.lang.
     * String, boolean)
     */
    @Override
    public void setScopeOnly(String user, boolean isScopeOnly) {
        // Block all other threads before filling the maps
        writeLock.lock();
        try {
            scopeMap.put(user, isScopeOnly);
        } finally {
            writeLock.unlock();
        }
    }

    private String[] getRoles(Configuration c) {
        Set<String> roleSet = c.getRoles();
        if (c.isAdminUser()) {
            roleSet.add(ApplicationRoles.ROLE_ADMIN);
        }
        if (c.isWebUser()) {
            roleSet.add(ApplicationRoles.ROLE_WEB);
        }
        if (c.isRcpUser()) {
            roleSet.add(ApplicationRoles.ROLE_USER);
        }
        String[] roleArray = new String[roleSet.size()];
        roleArray = roleSet.toArray(roleArray);
        return roleArray;
    }

    /*
     * (non-Javadoc)
     * 
     * @see sernet.verinice.service.IConfigurationService#discardUserData()
     */
    @Override
    public void discardUserData() {
        // Block all other threads before clearing the maps
        writeLock.lock();
        try {
            roleMap.clear();
            scopeMap.clear();
            scopeIdMap.clear();
            nameMap.clear();
        } finally {
            writeLock.unlock();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see sernet.verinice.service.IConfigurationService#isScopeOnly(java.lang.
     * String)
     */
    @Override
    public boolean isScopeOnly(String user) {
        Boolean result = null;
        // prevent reading the configuration while another thread is writing it
        readLock.lock();
        try {
            result = scopeMap.get(user);
        } finally {
            readLock.unlock();
        }
        if (result == null) {
            loadUserData();
            readLock.lock();
            try {
                result = scopeMap.get(user);
            } finally {
                readLock.unlock();
            }
            if (result == null) {
                // prevent calling loadUserData() again
                // if user was not found in db
                result = false;
                // Block all other threads before filling the maps
                writeLock.lock();
                try {
                    scopeMap.put(user, result);
                } finally {
                    writeLock.unlock();
                }
            }
        }
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see sernet.verinice.service.IConfigurationService#getScopeId(java.lang.
     * String)
     */
    @Override
    public Integer getScopeId(String user) {
        Integer result = null;
        readLock.lock();
        try {
            result = scopeIdMap.get(user);
        } finally {
            readLock.unlock();
        }
        if (result == null) {
            loadUserData();
            readLock.lock();
            try {
                result = scopeIdMap.get(user);
            } finally {
                readLock.unlock();
            }
        }
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * sernet.verinice.service.IConfigurationService#getRoles(java.lang.String)
     */
    @Override
    public String[] getRoles(String user) {
        String[] result = null;
        readLock.lock();
        try {
            result = roleMap.get(user);
        } finally {
            readLock.unlock();
        }
        if (result == null) {
            loadUserData();
            readLock.lock();
            try {
                result = roleMap.get(user);
            } finally {
                readLock.unlock();
            }
        }
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * sernet.verinice.service.IConfigurationService#getName(java.lang.String)
     */
    @Override
    public String getName(String user) {
        String result = null;
        readLock.lock();
        try {
            result = nameMap.get(user);
        } finally {
            readLock.unlock();
        }
        if (result == null) {
            loadUserData();
            readLock.lock();
            try {
                result = nameMap.get(user);
            } finally {
                readLock.unlock();
            }
        }
        return result;
    }

    @Override
    public boolean isWriteAllowed(CnATreeElement cte) {
        // Server implementation of CnAElementHome.isWriteAllowed
        try {
            // Short cut: If no permission handling is needed than all objects
            // are
            // writable.
            if (!authService.isPermissionHandlingNeeded()) {
                return true;
            }
            // Short cut 2: If we are the admin, then everything is writable as
            // well.
            if (getAuthService().currentUserHasRole(new String[] { ApplicationRoles.ROLE_ADMIN })) {
                return true;
            }
            Set<String> userRoles = loadRoles();
            for (Permission p : cte.getPermissions()) {
                if (p != null && p.isWriteAllowed() && userRoles.contains(p.getRole())) {
                    return true;
                }
            }
        } catch (SecurityException | sernet.gs.service.SecurityException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Write is not allowed", e);
            }
            return false;
        } catch (RuntimeException re) {
            LOG.error("Error while checking write permissions", re);
            throw re;
        } catch (CommandException t) {
            LOG.error("Error while checking write permissions", t);
            throw new RuntimeException("Error while checking write permissions", t);
        }
        return false;
    }

    public Set<String> loadRoles() throws CommandException {
        LoadCurrentUserConfiguration lcuc = new LoadCurrentUserConfiguration();
        lcuc = getCommandService().executeCommand(lcuc);
        Configuration c = lcuc.getConfiguration();
        // No configuration for the current user (anymore?). Then nothing is
        // writable.
        if (c == null) {
            return Collections.emptySet();
        }
        return c.getRoles();
    }

    public IBaseDao<Configuration, Serializable> getConfigurationDao() {
        return configurationDao;
    }

    public void setConfigurationDao(IBaseDao<Configuration, Serializable> configurationDao) {
        this.configurationDao = configurationDao;
    }

    public IBaseDao<CnATreeElement, Long> getCnaTreeElementDao() {
        return cnaTreeElementDao;
    }

    public void setCnaTreeElementDao(IBaseDao<CnATreeElement, Long> cnaTreeElementDAO) {
        this.cnaTreeElementDao = cnaTreeElementDAO;
    }

    public IAuthService getAuthService() {
        return authService;
    }

    public void setAuthService(IAuthService authService) {
        this.authService = authService;
    }

    public ICommandService getCommandService() {
        return commandService;
    }

    public void setCommandService(ICommandService commandService) {
        this.commandService = commandService;
    }

}
