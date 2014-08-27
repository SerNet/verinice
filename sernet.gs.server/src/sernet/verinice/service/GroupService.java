/*******************************************************************************
 * Copyright (c) 2014 Benjamin Weißenfels.
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
 *     Benjamin Weißenfels <bw[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.service;

import java.io.Serializable;
import java.util.List;

import org.hibernate.criterion.DetachedCriteria;

import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.common.configuration.Configuration;
import sernet.verinice.model.common.group.Group;

/**
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 *
 */
public class GroupService implements IGroupService, Serializable {

    private DAOFactory daoFactory;
 
    @Override
    public List<Group> listGroups() {
        return getDao().findAll();
    }

    @Override
    public Group createGroup(String name) {
        Group group = new Group();
        group.setName(name);      
        getDao().merge(group, false);
        return group;
    }


    public DAOFactory getDaoFactory() {
        return daoFactory;
    }


    public void setDaoFactory(DAOFactory daoFactory) {
        this.daoFactory = daoFactory;
    }
    
    private IBaseDao<Group, Serializable> getDao(){
        return getDaoFactory().getDAO(Group.TYPE_ID);
    }


    @Override
    public void connectGroupWithConfiguration(Group group, Configuration configuration) {
        group.addConfiguration(configuration);
        getDao().saveOrUpdate(group);
    }


    @Override
    public void delete(Group group) {
        getDao().delete(group);
    }
    
    @Override
    public List<Group> listHydratedGroups() {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(Group.class);
                  
            
        
        return null;
    }

}
