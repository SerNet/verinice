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

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;

import sernet.gs.service.NumericStringComparator;
import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.verinice.interfaces.IAccountSearchParameter;
import sernet.verinice.interfaces.IAccountService;
import sernet.verinice.model.common.PersonAdapter;
import sernet.verinice.model.common.accountgroup.AccountGroup;
import sernet.verinice.model.common.configuration.Configuration;
import sernet.verinice.service.account.AccountSearchParameter;
import sernet.verinice.service.account.AccountSearchParameterFactory;

/**
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 * @contributor Sebastian Hagedorn <sh[at]sernet[dot]de> - Concurrency Implementation
 *
 */
public class AccountGroupDataService implements IAccountGroupViewDataService {

    private static final Logger logger = Logger.getLogger(AccountGroupDataService.class);

    private static final String[] EMPTY_STRING_ARRAY = new String[0];

    private IAccountService accountService;

    private Map<String, Set<String>> accountGroupToConfiguration;

    private Set<String> accounts;
    
    private Map<String, String> prettyAccountNames;
    
    private AccountGroupView view;

    public AccountGroupDataService() {
        accountService = ServiceFactory.lookupAccountService();
        loadAccountGroupData();
    }
    
    public AccountGroupDataService(AccountGroupView view){
        this();
        this.view = view;
    }
    
    @Override
    public String[] getAccountGroups() {
        if(accountGroupToConfiguration != null && accountGroupToConfiguration.keySet() != null){
            return convertToStringArray(accountGroupToConfiguration.keySet());
        } else {
            return EMPTY_STRING_ARRAY;
        }
    }

    @Override
    public String[] getAllAccounts() {
        if(accounts != null){
            return convertToStringArray(accounts);
        } else {
            return EMPTY_STRING_ARRAY;
        }
    }

    @Override
    public final void loadAccountGroupData() {

        final class LoadAccountGroupDataJob extends Job {

            public LoadAccountGroupDataJob(String name) {
                super(name);
            }

            @Override
            protected IStatus run(IProgressMonitor monitor) {

                try {
                    Activator.inheritVeriniceContextState();
                    List<AccountGroup> accountGroups = accountService.listGroups();

                    accountGroupToConfiguration = new TreeMap<>(new NumericStringComparator());

                    accounts = accountService.listAccounts();

                    for (AccountGroup accountGroup : accountGroups) {
                        IAccountSearchParameter parameter = AccountSearchParameterFactory
                                .createAccountGroupParameter(accountGroup.getName());
                        List<Configuration> configurationsForAccountGroup = accountService
                                .findAccounts(parameter);
                        accountGroupToConfiguration.put(accountGroup.getName(),
                                new HashSet<String>());
                        for (Configuration account : configurationsForAccountGroup) {
                            accountGroupToConfiguration.get(accountGroup.getName())
                                    .add(account.getUser());
                        }
                    }
                    initPrettyAccountNames();
                    if (view != null) {
                        Display.getDefault().syncExec(new Runnable() {

                            @Override
                            public void run() {

                                view.passServiceToLabelProvider();
                                view.refreshView();
                                view.switchButtons(true);
                            }
                        });
                    }
                } catch (Exception e) {
                    logger.error("Error loading account group data", e);
                }
                return Status.OK_STATUS;
            }

        }

        new LoadAccountGroupDataJob(Messages.loadDataJoblabel).schedule();
    }

    
    @Override
    public String[] getAccountNamesForGroup(String accountGroupName) {
        return convertToStringArray(accountGroupToConfiguration.get(accountGroupName));
    }

    private <T> String[] convertToStringArray(Set<T> accountGroupOrConfiguration) {

        Set<String> set = new TreeSet<>(new NumericStringComparator());
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

        if (!accountGroupToConfiguration.containsKey(accountGroupName)) {
            accountGroupToConfiguration.put(accountGroupName, new TreeSet<String>(new NumericStringComparator()));
        }

        accountService.createAccountGroup(accountGroupName);
    }

    @Override
    public String[] saveAccountGroupData(String groupName, String[] accountNames) {
        try {

            Set<String> selectedAccounts = accountService.addRole(new HashSet<String>(Arrays.asList(accountNames)), groupName);

            for (String account : selectedAccounts) {
                accountGroupToConfiguration.get(groupName).add(account);
            }

            return convertToStringArray(accountGroupToConfiguration.get(groupName));

        } catch (Exception ex) {
            logger.error("updated view for account groups failed", ex);
        }

        return new String[] {};
    }

    @Override
    public void editAccountGroupName(String newRoleName, String oldRoleName) {

        if (newRoleName.equals(oldRoleName)) {
            throw new IllegalArgumentException(String.format("name is not changed: %s", newRoleName));
        }

        // delete role from configurations
        if (!accountGroupToConfiguration.containsKey(newRoleName)) {
            accountGroupToConfiguration.put(newRoleName, new TreeSet<String>(new NumericStringComparator()));
            accountService.createAccountGroup(newRoleName);
        }

        accountGroupToConfiguration.get(newRoleName).addAll(accountGroupToConfiguration.get(oldRoleName));
        accountGroupToConfiguration.remove(oldRoleName);

        accountService.deleteAccountGroup(oldRoleName);
        accountService.deleteRole(accountGroupToConfiguration.get(newRoleName), oldRoleName);

        accountService.addRole(accountGroupToConfiguration.get(newRoleName), newRoleName);
        accountService.updatePermissions(newRoleName, oldRoleName);
    }

    @Override
    public Set<String> deleteAccountGroup(String role) {

        Set<String> evictedAccounts = accountGroupToConfiguration.get(role);
        accountGroupToConfiguration.remove(role);

        accountService.deleteAccountGroup(role);
        accountService.deletePermissions(role);
        return accountService.deleteRole(evictedAccounts, role);
    }

    @Override
    public String[] deleteAccountGroupData(String groupName, String[] userNames) {

        accountGroupToConfiguration.get(groupName).removeAll(new HashSet<String>(Arrays.asList(userNames)));
        Set<String> deletedAccounts = accountService.deleteRole(new HashSet<String>(Arrays.asList(userNames)), groupName);
        return convertToStringArray(deletedAccounts);
    }

    /* (non-Javadoc)
     * @see sernet.verinice.rcp.accountgroup.IAccountGroupViewDataService#prettyPrintAccountName(java.lang.String)
     */
    @Override
    public String getPrettyPrintAccountName(String account) {
        initPrettyAccountNames();
        if(prettyAccountNames.containsKey(account)){
            return prettyAccountNames.get(account);
        }
        return account;
    }
    
    private void initPrettyAccountNames(){
        if(prettyAccountNames == null){
            prettyAccountNames = new HashMap<>(0);
            for(Configuration conf : accountService.findAccounts(AccountSearchParameter.newInstance())){
                prettyAccountNames.put(conf.getUser(), createPrettyAccountName(conf));
            }
        }
    }
    
    private static String createPrettyAccountName(Configuration account){
        StringBuilder sb = new StringBuilder(PersonAdapter.getFullName(account.getPerson()));
        sb.append(" [").append(account.getUser()).append("]");
        return sb.toString();
    }
    

}
