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

import sernet.gs.service.RetrieveInfo;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.common.configuration.Configuration;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 *
 */
public class ConfigurationService implements IConfigurationService {
    
    private HashMap<String, Object[]> roleMap = new HashMap<String, Object[]>();
    
    private Map<String, Boolean> scopeMap = new HashMap<String, Boolean>();
    
    private Map<String, Integer> scopeIdMap = new HashMap<String, Integer>();
    
    IBaseDao<Configuration, Serializable> configurationDao;
 
    private void loadUserData() {
        List<Configuration> configurations = getConfigurationDao().findAll(RetrieveInfo.getPropertyInstance());
        for (Configuration c : configurations) {
            Object[] roleArray = c.getRoles().toArray();
            String user = c.getUser();
            // Put result into map and save asking the DB next time.
            roleMap.put(user, roleArray);           
            scopeMap.put(user, c.isScopeOnly());     
            scopeIdMap.put(user, c.getPerson().getScopeId());  
        }
        getConfigurationDao().clear();
    }
    
    public void discardUserData() {
        roleMap.clear();
        scopeMap.clear();
        scopeIdMap.clear();
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.service.IConfigurationService#isScopeOnly(java.lang.String)
     */
    @Override
    public boolean isScopeOnly(String user) {
        Boolean result = scopeMap.get(user);
        if (result == null) {
            loadUserData();
            result = scopeMap.get(user);
        }
        return (result==null) ? false : result;
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.service.IConfigurationService#getScopeId(java.lang.String)
     */
    @Override
    public Integer getScopeId(String user) {
        Integer result = scopeIdMap.get(user);
        if (result == null) {
            loadUserData();
            result = scopeIdMap.get(user);
        }
        return result;
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.service.IConfigurationService#getRoles(java.lang.String)
     */
    @Override
    public Object[] getRoles(String user) {
        Object[] result = roleMap.get(user);
        if (result == null) {
            loadUserData();
            result = roleMap.get(user);
        }
        return result;
    }
    
    /**
     * @return the configurationDao
     */
    public IBaseDao<Configuration, Serializable> getConfigurationDao() {
        return configurationDao;
    }

    /**
     * @param configurationDao the configurationDao to set
     */
    public void setConfigurationDao(IBaseDao<Configuration, Serializable> configurationDao) {
        this.configurationDao = configurationDao;
    }
}
