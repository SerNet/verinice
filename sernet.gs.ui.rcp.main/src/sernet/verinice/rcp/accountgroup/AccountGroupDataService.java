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
package sernet.verinice.rcp.accountgroup;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;

import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.verinice.interfaces.IAccountSearchParameter;
import sernet.verinice.interfaces.IAccountService;
import sernet.verinice.model.common.accountgroup.AccountGroup;
import sernet.verinice.model.common.configuration.Configuration;
import sernet.verinice.service.account.AccountSearchParameterFactory;

/**
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 *
 */
public class AccountGroupDataService implements IAccountGroupViewDataService {

    private Logger log = Logger.getLogger(AccountGroupDataService.class);

    private IAccountService accountService;

    private Map<String, Set<Configuration>> accountGroupToConfiguration;

    private Set<Configuration> accounts;

    public AccountGroupDataService() {
        accountService = ServiceFactory.lookupAccountService();
        loadAccountGroupData();
    }

    @Override
    public String[] getAccountGroups() {
        return convertToStringArray(accountGroupToConfiguration.keySet());
    }

    @Override
    public String[] getAllAccounts() {
        return convertToStringArray(accounts);
    }

    @Override
    final public void loadAccountGroupData() {

        List<AccountGroup> accountGroups = accountService.listGroups();
        accountGroupToConfiguration = new HashMap<String, Set<Configuration>>();

        for (AccountGroup accountGroup : accountGroups) {
            IAccountSearchParameter parameter = AccountSearchParameterFactory.createAccountGroupParameter(accountGroup.getName());
            List<Configuration> configurationsForAccountGroup = accountService.findAccounts(parameter);
            accountGroupToConfiguration.put(accountGroup.getName(), new HashSet<Configuration>(configurationsForAccountGroup));
        }

        accounts = new HashSet<Configuration>(accountService.listAccounts());
    }

    @Override
    public String[] getAccountNamesForGroup(String accountGroupName) {
        return convertToStringArray(accountGroupToConfiguration.get(accountGroupName));
    }

    private <T> String[] convertToStringArray(Set<T> accountGroupOrConfiguration) {

        Set<String> set = new HashSet<String>();
        for (T accountOrGroup : accountGroupOrConfiguration) {
            if (accountOrGroup instanceof AccountGroup) {
                set.add(((AccountGroup) accountOrGroup).getName());
            } else if (accountOrGroup instanceof Configuration) {
                set.add(((Configuration) accountOrGroup).getUser());
            } else if (accountOrGroup instanceof String) {
                set.add((String) accountOrGroup);
            } else {
                throw new IllegalArgumentException(String.format("%s is not supported", accountOrGroup.getClass().getSimpleName()));
            }
        }

        String[] result = new String[set.size()];
        set.toArray(result);
        return result;
    }

    @Override
    public void addAccountGroup(String accountGroupName) {
        AccountGroup accountGroup = new AccountGroup(accountGroupName);
        if (!accountGroupToConfiguration.containsKey(accountGroup)) {
            accountGroupToConfiguration.put(accountGroup.getName(), Collections.<Configuration> emptySet());
        }
    }

    @Override
    public void saveAccountGroupData(String groupName, String[] accountNames) {
        Set<Configuration> selectedAccounts = getSelectedConfigurations(accountNames);
        selectedAccounts = accountService.addRole(selectedAccounts, groupName);
        try{
        accountGroupToConfiguration.get(groupName).addAll(selectedAccounts);
        } catch (Exception ex){
            log.warn("updated view for account groups failed", ex);
        }
    }

    @Override
    public void editAccountGroupName(String newName, String oldName) {

        // delete role from configurations
        accountGroupToConfiguration.put(newName, deleteAccountGroup(oldName));

        // add role to configurations
        Set<Configuration> configurations = accountGroupToConfiguration.get(newName);
        String[] accountNames = new String[configurations.size()];
        int i = 0;
        for (Iterator<Configuration> iterator = configurations.iterator(); iterator.hasNext(); i++) {
            Configuration configuration = iterator.next();
            accountNames[i++] = configuration.getUser();

        }

        saveAccountGroupData(newName, accountNames);
    }

    @Override
    public Set<Configuration> deleteAccountGroup(String groupName) {
        Set<Configuration> configurations = accountGroupToConfiguration.get(groupName);
        accountGroupToConfiguration.remove(groupName);
        return accountService.deleteRole(configurations, groupName);
    }

    @Override
    public void deleteAccountGroupData(String groupName, String[] accountNames) {
        Set<Configuration> selectedAccounts = getSelectedConfigurations(accountNames);
        selectedAccounts = accountService.deleteRole(selectedAccounts, groupName);
        accountGroupToConfiguration.get(groupName).removeAll(selectedAccounts);
    }

    private Set<Configuration> getSelectedConfigurations(String[] accountNames) {
        Set<Configuration> selectedAccounts = new HashSet<Configuration>();
        for (Configuration configuration : accounts) {
            if (ArrayUtils.contains(accountNames, configuration.getUser())) {
                selectedAccounts.add(configuration);
            }
        }
        return selectedAccounts;
    }

}
