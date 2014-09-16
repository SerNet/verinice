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
import sernet.verinice.interfaces.IRightsServerHandler;
import sernet.verinice.model.common.Permission;
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

    private final static Logger LOG = Logger.getLogger(AccountService.class);

    private IDao<AccountGroup, Serializable> accountGroupDao;
    private IBaseDao<Configuration, Serializable> configurationDao;
    private ICommandService commandService;


    private IConfigurationService configurationService;

    private IRightsServerHandler rightsServerHandler;

    private IBaseDao<Permission, Serializable> permissionDao;

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
        if (!account.isDeactivatedUser()) {
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

        Set<String> accounts = listAccounts();

        if (accounts.contains(name))
            throw new IllegalArgumentException("group name is equivalent to an account name");

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
        if (accountGroups != null && !accountGroups.isEmpty())
            return accountGroups.get(0);

        return null;
    }

    @Override
    public Set<String> listAccounts() {
        List<Configuration> configurations = getAllConfigurations();
        Set<String> accountNames = new HashSet<String>();

        if (configurations != null) {
            for (Configuration configuration : configurations) {
                accountNames.add(configuration.getUser());
            }
        }

        return accountNames;
    }

    @Override
    public void saveAccountGroups(Set<String> accountGroupNames) {

        for (String accountGroup : accountGroupNames) {
            createAccountGroup(accountGroup);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Set<String> addRole(Set<String> usernames, String role) {

        Set<String> result = new HashSet<String>();
        for (Configuration account : extractConfiguration(usernames, getAllConfigurations())) {

            if (!isRoleSet(role, account)) {
                try {

                    account.addRole(role);
                    getConfigurationDao().merge(account);

                    result.add(account.getUser());

                } catch (Exception ex) {
                    LOG.error(String.format("adding role %s for user %s failed: %s", role, account.getUser(), ex.getLocalizedMessage()), ex);
                }
            }
        }

        configurationService.discardUserData();
        rightsServerHandler.discardData();

        return result;
    }

    private boolean isRoleSet(String role, Configuration account) {
        return account.getRoles(false).contains(role);
    }

    @Override
    public Set<String> deleteRole(Set<String> usernames, String role) {

        Set<String> result = new HashSet<String>();
        for (Configuration account : extractConfiguration(usernames, getAllConfigurations())) {

            try {

                account.deleteRole(role);
                getConfigurationDao().merge(account);

                result.add(account.getUser());

            } catch (Exception ex) {
                LOG.error(String.format("deleting role %s for user %s failed: %s", role, account.getUser(), ex.getLocalizedMessage()), ex);
            }
        }

        configurationService.discardUserData();
        rightsServerHandler.discardData();
        return result;
    }

    public void deletePermissions(String role) {
        String hqlQuery = "delete Permission where role = ?";
        String[] params = new String[] { role };
        getPermissionDao().updateByQuery(hqlQuery, params);
    }

    public void updatePermissions(String newRole, String oldRole){
        String hqlQuery = "update Permission set role = ? where role = ?";
        String[] params = new String[] { newRole, oldRole };
        getPermissionDao().updateByQuery(hqlQuery, params);
    }

    public ICommandService getCommandService() {
        return commandService;
    }

    public void setCommandService(ICommandService commandService) {
        this.commandService = commandService;
    }

    public IConfigurationService getConfigurationService() {
        return configurationService;
    }

    public void setConfigurationService(IConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    public IRightsServerHandler getRightsServerHandler() {
        return rightsServerHandler;
    }

    public void setRightsServerHandler(IRightsServerHandler rightsServerHandler) {
        this.rightsServerHandler = rightsServerHandler;
    }

    private List<Configuration> getAllConfigurations() {
        HqlQuery hqlQuery = AccountSearchQueryFactory.createRetrieveAllConfigurations();
        List<Configuration> configurations = (List<Configuration>) getConfigurationDao().findByQuery(hqlQuery.getHql(), new String[] {}, new Object[] {});

        return configurations == null ? new ArrayList<Configuration>() : configurations;
    }

    private Set<Configuration> extractConfiguration(Set<String> usernames, List<Configuration> configurations) {
        Set<Configuration> result = new HashSet<Configuration>();
        for (String username : usernames) {
            for (Configuration c : configurations) {
                if (c.getUser().equals(username))
                    result.add(c);
            }
        }

        return result;
    }

    public IBaseDao<Permission, Serializable> getPermissionDao() {
        return permissionDao;
    }

    public void setPermissionDao(IBaseDao<Permission, Serializable> permissionDao) {
        this.permissionDao = permissionDao;
    }
}
