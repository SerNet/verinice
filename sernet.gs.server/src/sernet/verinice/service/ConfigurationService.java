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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import sernet.gs.common.ApplicationRoles;
import sernet.gs.service.RetrieveInfo;
import sernet.verinice.interfaces.IAuthService;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.common.configuration.Configuration;

/**
 * Thread save implementation of {@link IConfigurationService}.
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class ConfigurationService implements IConfigurationService {
    
    private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final Lock readLock = readWriteLock.readLock();
    private final Lock writeLock = readWriteLock.writeLock();
    
    private Map<String, String[]> roleMap = new HashMap<String, String[]>();   
    private Map<String, Boolean> scopeMap = new HashMap<String, Boolean>(); 
    private Map<String, Integer> scopeIdMap = new HashMap<String, Integer>();    
    private Map<String, String> nameMap = new HashMap<String, String>();
    
    private IBaseDao<Configuration, Serializable> configurationDao;
    
    private IAuthService authService;
 
    private void loadUserData() {
        List<Configuration> configurations = getConfigurationDao().findAll(RetrieveInfo.getPropertyInstance());
        // Block all other threads before filling the maps
        writeLock.lock();
        try {
            for (Configuration c : configurations) {
                String[] roleArray = getRoles(c);
                String user = c.getUser();
                // Put result into map and save asking the DB next time.
                roleMap.put(user, roleArray);           
                scopeMap.put(user, c.isScopeOnly());     
                scopeIdMap.put(user, c.getPerson().getScopeId());  
                nameMap.put(user, c.getPerson().getTitle());  
            }
            String[] adminRoleArray = new String[]{ApplicationRoles.ROLE_ADMIN,ApplicationRoles.ROLE_WEB,ApplicationRoles.ROLE_USER};
            roleMap.put(getAuthService().getAdminUsername(), adminRoleArray);
            scopeMap.put(getAuthService().getAdminUsername(), false);
        } finally {
            writeLock.unlock();
        }    
        getConfigurationDao().clear();
    }

    private String[] getRoles(Configuration c) {
        Set<String> roleSet = c.getRoles();
        if(c.isAdminUser()) {
            roleSet.add(ApplicationRoles.ROLE_ADMIN);
        }
        if(c.isWebUser()) {
            roleSet.add(ApplicationRoles.ROLE_WEB);
        }
        if(c.isRcpUser()) {
            roleSet.add(ApplicationRoles.ROLE_USER);
        }
        String[] roleArray = new String[roleSet.size()];
        roleArray = roleSet.toArray(roleArray);
        return roleArray;
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.service.IConfigurationService#discardUserData()
     */
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
    
    /* (non-Javadoc)
     * @see sernet.verinice.service.IConfigurationService#isScopeOnly(java.lang.String)
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
            if(result==null) {
                // prevent calling loadUserData() again
                // if user was not found in db
                result = false;
                // Block all other threads before filling the maps
                writeLock.lock();
                try {
                    scopeMap.put(user,result);
                } finally {
                    writeLock.unlock();
                }
            }
        }
        return (result==null) ? false : result;
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.service.IConfigurationService#getScopeId(java.lang.String)
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
    
    /* (non-Javadoc)
     * @see sernet.verinice.service.IConfigurationService#getRoles(java.lang.String)
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
      
    /* (non-Javadoc)
     * @see sernet.verinice.service.IConfigurationService#getName(java.lang.String)
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
    
    public IBaseDao<Configuration, Serializable> getConfigurationDao() {
        return configurationDao;
    }

    public void setConfigurationDao(IBaseDao<Configuration, Serializable> configurationDao) {
        this.configurationDao = configurationDao;
    }

    public IAuthService getAuthService() {
        return authService;
    }

    public void setAuthService(IAuthService authService) {
        this.authService = authService;
    }
}
