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
 *     Daniel Murygin <dm[at]sernet[dot]de> - findAccounts
 ******************************************************************************/
package sernet.verinice.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import sernet.verinice.interfaces.IAccountSearchParameter;
import sernet.verinice.interfaces.IAccountService;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.interfaces.IDao;
import sernet.verinice.iso27k.service.Retriever;
import sernet.verinice.model.common.accountgroup.AccountGroup;
import sernet.verinice.model.common.configuration.Configuration;

/**
 * Service to find, remove and add new accounts and account groups.
 * This service is configured in veriniceserver-common.xml. Remote access is configured in
 * springDispatcher-servlet.xml.
 * 
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
@SuppressWarnings("serial")
public class AccountService implements IAccountService, Serializable {

    public transient static Logger log = Logger.getLogger(AccountService.class);

    private IDao<AccountGroup, Serializable> accountGroupDao;
    private IBaseDao<Configuration, Serializable> configurationDao;
    private ICommandService commandService;
    
    @Override
    public List<Configuration> findAccounts(IAccountSearchParameter parameter) {
        HqlQuery hqlQuery = AccountSearchQueryFactory.createHql(parameter);
        List<Configuration> resultNoProps = getConfigurationDao().findByQuery(hqlQuery.getHql(), hqlQuery.getParams());
        List<Configuration> result;
        if (resultNoProps != null && !resultNoProps.isEmpty()) {
            Set<Integer> dbIds = new HashSet<Integer>(resultNoProps.size());
            for (Configuration configuration : resultNoProps) {
                dbIds.add(configuration.getDbId());
            }
            hqlQuery = AccountSearchQueryFactory.createRetrieveHql(dbIds);
            hqlQuery.setNames(new String[] { "dbIds" });
            Set<Configuration> set = new HashSet<Configuration>(getConfigurationDao().findByQuery(hqlQuery.getHql(), hqlQuery.getNames(), hqlQuery.getParams()));
            result = new ArrayList<Configuration>(set);
       } else {
           result = Collections.emptyList(); 
       }
       Collections.sort(result);
       return result;
    }

    @Override
    public void delete(Configuration account) {
        getConfigurationDao().delete(account);
        // When a Configuration instance got deleted the server needs to
        // update
        // its cached role map. This is done here.
        getCommandService().discardUserData();
    }
    
    @Override
    public void deactivate(Configuration account) {
        if(!account.isDeactivatedUser()) {
            account.setIsDeactivatedUser(true);
            getConfigurationDao().merge(account);
        }
    }
    
    @Override
    public List<AccountGroup> listGroups() {
        return getAccountGroupDao().findAll();
    }

    @Override
    public AccountGroup createAccountGroup(String name) {

        List<Configuration> accounts = listAccounts();
        for (Configuration account : accounts) {
            if (account.getUser().equals(name))
                throw new IllegalArgumentException("group name is equivalent to an account name");
        }

        AccountGroup group = new AccountGroup(name);
        AccountGroup savedGroup = getAccountGroupDao().merge(group);
        return savedGroup;
    }

    @Override
    public void deleteAccountGroup(AccountGroup group) {
        getAccountGroupDao().delete(group);
    }

    public IBaseDao<Configuration, Serializable> getConfigurationDao() {
        return configurationDao;
    }

    public void setConfigurationDao(IBaseDao<Configuration, Serializable> configurationDao) {
        this.configurationDao = configurationDao;
    }

    public IDao<AccountGroup, Serializable> getAccountGroupDao() {
        return accountGroupDao;
    }

    public void setAccountGroupDao(IDao<AccountGroup, Serializable> accountGroupDao) {
        this.accountGroupDao = accountGroupDao;
    }

    public ICommandService getCommandService() {
        return commandService;
    }

    public void setCommandService(ICommandService commandService) {
        this.commandService = commandService;
    }    
    
    @Override
    public void deleteAccountGroup(String name) {
        AccountGroup accountGroup = findGroupByHQL(name);
        getAccountGroupDao().delete(accountGroup);
    }

    private AccountGroup findGroupByHQL(String name) {

        String hqlQuery = " FROM AccountGroup accountGroup WHERE name = ?";
        Object[] params = new Object[] { name };

        @SuppressWarnings("unchecked")
        List<AccountGroup> accountGroups = (List<AccountGroup>) getAccountGroupDao().findByQuery(hqlQuery, params);

        // name of a group is unique, so there only exists one result
        if (accountGroups != null)
            return accountGroups.get(0);

        return null;
    }

    @Override
    public List<Configuration> listAccounts() {
        HqlQuery hqlQuery = AccountSearchQueryFactory.createRetrieveAllConfigurations();

        List<Configuration> configurations = (List<Configuration>) getConfigurationDao().findByQuery(hqlQuery.getHql(), new String[] {}, new Object[] {});
        return (configurations == null) ? Collections.<Configuration> emptyList() : configurations;
    }

    @Override
    public void saveAccountGroups(Set<String> accountGroupNames) {

        for (String accountGroup : accountGroupNames) {
            createAccountGroup(accountGroup);
        }
    }

    @Override
    public Set<Configuration> addRole(Set<Configuration> configurations, String role) {

        Set<Configuration> result = new HashSet<Configuration>();

        for (Configuration configuration : configurations) {

            if (!isRoleSet(role, configuration)) {
                configuration.addRole(role);
                result.add((Configuration) getConfigurationDao().merge(configuration));
            }
        }

        return result;
    }

    private boolean isRoleSet(String role, Configuration configuration) {
        return configuration.getRoles().contains(role);
    }

    @Override
    public Set<Configuration> deleteRole(Set<Configuration> configurations, String role) {

        Set<Configuration> result = new HashSet<Configuration>();
        for (Configuration configuration : configurations) {
            if (configuration.deleteRole(role)) {
                result.add((Configuration) getConfigurationDao().merge(configuration));
            }
        }

        return result;
    }

    private Logger getLog() {
        if (log == null) {
            log = Logger.getLogger(AccountService.class);
        }

        return log;
    }
}
