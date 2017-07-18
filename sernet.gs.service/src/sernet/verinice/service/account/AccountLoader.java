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
package sernet.verinice.service.account;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import sernet.gs.service.NumericStringComparator;
import sernet.hui.common.VeriniceContext;
import sernet.verinice.interfaces.ApplicationRoles;
import sernet.verinice.interfaces.IAccountService;
import sernet.verinice.interfaces.IAuthService;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.interfaces.IRightsService;
import sernet.verinice.model.auth.OriginType;
import sernet.verinice.model.auth.Profile;
import sernet.verinice.model.common.accountgroup.AccountGroup;
import sernet.verinice.model.common.configuration.Configuration;
import sernet.verinice.service.commands.LoadCurrentUserConfiguration;

/**
 * Helper class to account data
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public final class AccountLoader {
    
    private static final Logger log = Logger.getLogger(AccountLoader.class);
 
    private static final NumericStringComparator NSC = new NumericStringComparator();
    
    private AccountLoader() {        
    }
      
    public static List<String> loadLoginAndGroupNames() {
        List<String> accountGroups = getAccountService().listGroupNames();
        Set<String> accounts = getAccountService().listAccounts();
        accountGroups.addAll(accounts);
        Collections.sort(accountGroups, NSC);
        return accountGroups;
    } 
    
    public static List<String> loadLoginNames() {
        List<String> accounts = new LinkedList<String>(getAccountService().listAccounts());
        Collections.sort(accounts, NSC);
        return accounts;
    } 
    
    public static List<String> loadGroupNames() {
        List<String> accountGroups = getAccountService().listGroupNames();
        Collections.sort(accountGroups, NSC);
        return accountGroups;
    }
    
    public static List<Configuration> loadAccounts() {
        return getAccountService().findAccounts(AccountSearchParameter.newInstance());
    } 
    
    public static Set<String> loadCurrentUserGroups() {
        Set<String> userRoles = new HashSet<String>();
        try {
            LoadCurrentUserConfiguration lcuc = new LoadCurrentUserConfiguration();
            lcuc = getCommandService().executeCommand(lcuc);

            Configuration c = lcuc.getConfiguration();
            if (c != null) {
                userRoles = c.getRoles();
            }
        } catch (Exception e) {
            log.error("Error while loading current user roles.", e);
        }
        return userRoles;
    }

    public static List<String> loadGroupNamesForLocalAdmin() {
        List<String> groups = new ArrayList<String>();
        List<String> groupNames = AccountLoader.loadGroupNames();

        for (String groupName : groupNames) {
            if (AccountLoader.isLocalAdminOwnerOrCreator(groupName)) {
                groups.add(groupName);
            }
        }
        return groups;
    }

    public static List<String> loadAccountsAndGroupNamesForLocalAdmin() {
        List<String> accountsAndGroups = AccountLoader.loadLoginNames();
        List<String> groupNames = AccountLoader.loadGroupNames();

        for (String groupName : groupNames) {
            if (AccountLoader.isLocalAdminOwnerOrCreator(groupName)) {
                accountsAndGroups.add(groupName);
            }
        }
        return accountsAndGroups;
    }

    public static boolean isLocalAdminOwnerOrCreator(String groupName) {
        Set<String> userGroups = AccountLoader.loadCurrentUserGroups();
        if (IRightsService.ADMINLOCALDEFAULTGROUPNAME.equals(groupName) || userGroups.contains(groupName)) {
            return true;
        }

        List<AccountGroup> accountGroups = getAccountService().listGroups();
        String username = getAuthService().getUsername();
        for (AccountGroup accountGroup : accountGroups) {
            if (accountGroup.getName().equals(groupName) && username.equals(accountGroup.getCreator())) {
                return true;
            }
        }
        return false;
    }

    public static boolean isLocalAdminCreator(Profile profile) {
        if (OriginType.DEFAULT.equals(profile.getOrigin())) {
            return false;
        }
        String username = getAuthService().getUsername();
        return username != null && username.equals(profile.getCreator());
    }

    public static boolean isEditAllowed(Configuration account) {
        final boolean isAdmin = getAuthService().currentUserHasRole(new String[] { ApplicationRoles.ROLE_ADMIN });
        final boolean isLocalAdmin = getAuthService().currentUserHasRole(new String[] { ApplicationRoles.ROLE_LOCAL_ADMIN });
        return isAdmin || (isLocalAdmin && !account.isAdminUser());
    }
    
    private static IAuthService getAuthService() {
        return (IAuthService) VeriniceContext.get(VeriniceContext.AUTH_SERVICE);
    }
    
    public static IAccountService getAccountService() {
        return (IAccountService) VeriniceContext.get(VeriniceContext.ACCOUNT_SERVICE);
    }
    
    public static ICommandService getCommandService() {
        return (ICommandService) VeriniceContext.get(VeriniceContext.COMMAND_SERVICE);
    }
}
