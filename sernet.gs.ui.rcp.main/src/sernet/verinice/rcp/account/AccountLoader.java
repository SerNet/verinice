/*******************************************************************************
 * Copyright (c) 2014 Daniel Murygin.
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
package sernet.verinice.rcp.account;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import sernet.gs.service.NumericStringComparator;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.verinice.interfaces.IAccountService;
import sernet.verinice.interfaces.IRightsService;
import sernet.verinice.model.common.accountgroup.AccountGroup;
import sernet.verinice.model.common.configuration.Configuration;
import sernet.verinice.service.account.AccountSearchParameter;
import sernet.verinice.service.commands.LoadCurrentUserConfiguration;

/**
 * Helper class to account data
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public final class AccountLoader {
 
    private static final Logger LOG = Logger.getLogger(AccountLoader.class);

    private static final NumericStringComparator NSC = new NumericStringComparator();
    
    private AccountLoader() {        
    }
      
    @SuppressWarnings("unchecked")
    public static List<String> loadLoginAndGroupNames() {
        IAccountService accountService = ServiceFactory.lookupAccountService();
        List<String> accountGroups = accountService.listGroupNames();
        Set<String> accounts = accountService.listAccounts();
        accountGroups.addAll(accounts);
        Collections.sort(accountGroups, NSC);
        return accountGroups;
    } 
    
    @SuppressWarnings("unchecked")
    public static List<String> loadLoginNames() {
        IAccountService accountService = ServiceFactory.lookupAccountService();
        List<String> accounts = new LinkedList<String>(accountService.listAccounts());
        Collections.sort(accounts, NSC);
        return accounts;
    } 
    
    @SuppressWarnings("unchecked")
    public static List<String> loadGroupNames() {
        IAccountService accountService = ServiceFactory.lookupAccountService();
        List<String> accountGroups = accountService.listGroupNames();
        Collections.sort(accountGroups, NSC);
        return accountGroups;
    }

    public static List<Configuration> loadAccounts() {
        IAccountService accountService = ServiceFactory.lookupAccountService();
        return accountService.findAccounts(AccountSearchParameter.newInstance());
    } 

    public static Set<String> loadCurrentUserGroups() {
        Set<String> userRoles = new HashSet<String>();
        try {
            LoadCurrentUserConfiguration lcuc = new LoadCurrentUserConfiguration();
            lcuc = ServiceFactory.lookupCommandService().executeCommand(lcuc);

            Configuration c = lcuc.getConfiguration();
            if (c != null) {
                userRoles = c.getRoles();
            }
        } catch (Exception e) {
            LOG.error("Error while loading current user roles.", e);
        }
        return userRoles;
    }

    public static List<String> loadGroupNamesForLocalAdmin() {
        List<String> groups = new ArrayList<String>();
        List<String> groupNames = AccountLoader.loadGroupNames();
        List<AccountGroup> accountGroups = ServiceFactory.lookupAccountService().listGroups();
        Set<String> userGroups = AccountLoader.loadCurrentUserGroups();
        String username = ServiceFactory.lookupAuthService().getUsername();

        for (String groupName : groupNames) {
            if (AccountLoader.isLocalAdminOwnerOrCreator(groupName, accountGroups, userGroups, username)) {
                groups.add(groupName);
            }
        }
        return groups;
    }

    public static List<String> loadAccountsAndGroupNamesForLocalAdmin() {
        List<String> accountsAndGroups = AccountLoader.loadLoginNames();
        List<String> groupNames = AccountLoader.loadGroupNames();
        List<AccountGroup> accountGroups = ServiceFactory.lookupAccountService().listGroups();
        Set<String> userGroups = AccountLoader.loadCurrentUserGroups();
        String username = ServiceFactory.lookupAuthService().getUsername();

        for (String groupName : groupNames) {
            if (AccountLoader.isLocalAdminOwnerOrCreator(groupName, accountGroups, userGroups, username)) {
                accountsAndGroups.add(groupName);
            }
        }
        return accountsAndGroups;
    }

    public static boolean isLocalAdminOwnerOrCreator(String groupName, List<AccountGroup> accountGroups, Set<String> userGroups, String username) {
        if (IRightsService.ADMINLOCALDEFAULTGROUPNAME.equals(groupName) || userGroups.contains(groupName)) {
            return true;
        }
        for (AccountGroup accountGroup : accountGroups) {
            if (StringUtils.isNotEmpty(accountGroup.getCreator()) && accountGroup.getCreator().equals(username)) {
                return true;
            }
        }
        return false;
    }
}
