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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import sernet.gs.service.NumericStringComparator;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.verinice.interfaces.IAccountService;
import sernet.verinice.model.common.configuration.Configuration;
import sernet.verinice.service.account.AccountSearchParameter;

/**
 * Helper class to account data
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public final class AccountLoader {
 
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
    
    public static List<Configuration> loadAccounts() {
        IAccountService accountService = ServiceFactory.lookupAccountService();
        List<Configuration> accounts = accountService.findAccounts(AccountSearchParameter.newInstance());
        return accounts;
    } 
}
