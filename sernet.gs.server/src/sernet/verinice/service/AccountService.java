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

import sernet.verinice.interfaces.IAccountSearchParameter;
import sernet.verinice.interfaces.IAccountService;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.common.configuration.Configuration;
import sernet.verinice.model.common.group.Group;

/**
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 *
 */
public class AccountService implements IAccountService, Serializable {

    private DAOFactory daoFactory;
 
    private IBaseDao<Configuration, Serializable> configurationDao;
    
    @Override
    public List<Configuration> findAccounts(IAccountSearchParameter parameter) {
        HqlQuery hqlQuery = AccountSearchQueryFactory.createHql(parameter);
        return getConfigurationDao().findByQuery(hqlQuery.getHql(), hqlQuery.getParams());
    }
    
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
    public void delete(Group group) {
        getDao().delete(group);
    }

    public IBaseDao<Configuration, Serializable> getConfigurationDao() {
        return configurationDao;
    }

    public void setConfigurationDao(IBaseDao<Configuration, Serializable> configurationDao) {
        this.configurationDao = configurationDao;
    }

    
   

}
