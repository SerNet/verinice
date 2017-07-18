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
package sernet.verinice.service.commands.migration;

import static sernet.verinice.interfaces.IRightsService.ADMINDEFAULTGROUPNAME;
import static sernet.verinice.interfaces.IRightsService.ADMINSCOPEDEFAULTGROUPNAME;
import static sernet.verinice.interfaces.IRightsService.USERDEFAULTGROUPNAME;
import static sernet.verinice.interfaces.IRightsService.USERSCOPEDEFAULTGROUPNAME;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import sernet.hui.common.VeriniceContext;
import sernet.verinice.interfaces.IAccountService;
import sernet.verinice.model.common.accountgroup.AccountGroup;
import sernet.verinice.model.common.configuration.Configuration;
import sernet.verinice.service.account.AccountSearchParameter;

/**
 * Writes the default user groups (sometimes also called accountGroups or roles)
 * into the table accountGroup.
 *
 * This table is needed to create something like empty groups. This comes from
 * that circumstances, that groups only lives as a property in
 * {@link Configuration}.
 *
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 *
 */
@SuppressWarnings("serial")
public class MigrateDbTo1_00D extends DbMigration {

    private transient Logger log;

    private IAccountService accountService;

    @Override
    public void execute() throws RuntimeException {

        accountService = (IAccountService) VeriniceContext.get(VeriniceContext.ACCOUNT_SERVICE);
        updateAccountGroupTableWithDefaultAccountGroups();
        updateAccountGroupTableWithExistingAccountGroups();

        super.updateVersion();
    }

    private void updateAccountGroupTableWithDefaultAccountGroups() {
        final String[] defaultGroups = new String[] { ADMINDEFAULTGROUPNAME, ADMINSCOPEDEFAULTGROUPNAME, USERDEFAULTGROUPNAME, USERSCOPEDEFAULTGROUPNAME };
        Set<String> accountGroupNames = getAccountGroupNameSetFromList(accountService.listGroups());

        for (String defaultGroup : defaultGroups) {
            if (accountGroupNames.contains(defaultGroup))
                continue;
            try {
                accountService.createAccountGroup(defaultGroup);
            } catch (Exception ex) {
                String message = String.format("default group %s not added to account group table: %s", defaultGroup, ex.getLocalizedMessage());
                handleError(ex, message);
            }
        }
    }

    private void handleError(Exception ex, String message) {
        getLog().error(message, ex);
        throw new RuntimeException(message);
    }

    private void updateAccountGroupTableWithExistingAccountGroups() {

        List<Configuration> accounts = accountService.findAccounts(new AccountSearchParameter());
        Set<String> accountGroupNames = getAccountGroupNameSetFromList(accountService.listGroups());

        for (Configuration account : accounts) {
            for (String accountGroupName : account.getRoles(false)) {
                if (accountGroupNames.contains(accountGroupName)) {
                    continue;
                }

                accountGroupNames.add(accountGroupName);

                try {
                    accountService.createAccountGroup(accountGroupName);
                } catch (Exception ex) {
                    String message = String.format("migration of account group %s failed: %s", accountGroupName, ex.getLocalizedMessage());
                    handleError(ex, message);
                }
            }
        }
    }

    private Set<String> getAccountGroupNameSetFromList(List<AccountGroup> accountGroups) {
        Set<String> accountGroupNames = new HashSet<String>();
        for (AccountGroup accountGroup : accountGroups) {
            accountGroupNames.add(accountGroup.getName());
        }

        return accountGroupNames;
    }

    private Logger getLog() {
        if (log == null)
            log = Logger.getLogger(MigrateDbTo1_00D.class);
        return log;
    }

    @Override
    public double getVersion() {
        return 1.00D;
    }

}
