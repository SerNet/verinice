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
package sernet.gs.server.security;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import sernet.hui.common.connect.Entity;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.common.HydratorUtil;
import sernet.verinice.model.common.configuration.Configuration;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 *
 */
public abstract class UserLoader {
    
    // injected by spring
    private IBaseDao<Entity, Serializable> entityDao;
    
    
    protected List<Entity> loadUserEntites(String username) {       
        String query = "from Entity entity join fetch entity.typedPropertyLists where entity.entityType = ?"; //$NON-NLS-1$
        List<Entity> entities = getEntityDao().findByQuery(query, new String[] {"configuration"});
        // only searched user if present, otherwise return all:
        if (username != null && username.length()>0) {
            allResults: for (Entity entity : entities) {
                if (username.equals(entity.getSimpleValue(Configuration.PROP_USERNAME))) {
                    // hydrate call removed
                    // lazy="false" added to PropertyList.hbm.xml and PropertyListOracle.hbm.xml added
                    // See Bug 297
                    // HydratorUtil.hydrateEntity(dao, entity);
                    entities = new ArrayList<Entity>();
                    entities.add(entity);
                    break allResults;
                }
            }      
        }
        else {
            for (Entity entity : entities) {
                HydratorUtil.hydrateEntity(getEntityDao(), entity);
            }
        }
        return entities;
    }
    
    public IBaseDao<Entity, Serializable> getEntityDao() {
        return entityDao;
    }
    
    public void setEntityDao(IBaseDao<Entity, Serializable> entityDao ) {
        this.entityDao = entityDao;
    }
}
