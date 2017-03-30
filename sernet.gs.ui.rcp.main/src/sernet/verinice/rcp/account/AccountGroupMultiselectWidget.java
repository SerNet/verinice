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
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.swt.widgets.Composite;

import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.IAccountService;
import sernet.verinice.model.common.accountgroup.AccountGroup;
import sernet.verinice.model.common.configuration.Configuration;
import sernet.verinice.rcp.MultiselectWidget;

public class AccountGroupMultiselectWidget extends MultiselectWidget<AccountGroup> {

    private static final Logger LOG = Logger.getLogger(AccountGroupMultiselectWidget.class);
    
    private Configuration account;
    
    private IAccountService accountService;
    
    public AccountGroupMultiselectWidget(Composite parent, Configuration account) {
        this.account = account;
        try{
            initData();
            initGui(parent);
        } catch( Exception e ) {
           String message = "Error while creating widget."; //$NON-NLS-1$
           LOG.error(message, e);
           throw new RuntimeException(message, e);
        }
    }

    @Override
    protected String getLabel(AccountGroup accountGroup) {
        return accountGroup.getName();
    }

    @Override
    protected void initData() throws CommandException {
        itemList = getAccountService().listGroups();
        itemList = sortItems(itemList);
        Set<String> rolesOfAccount = account.getRoles(false);
        
        for (AccountGroup group : itemList) {
            if(rolesOfAccount.contains(group.getName())) {
                preSelectedElements.add(group);
                if (LOG.isDebugEnabled()) {
                    LOG.debug(group.getName() + " added to preSelectedElements");
                }
            }
        }
        if(rolesOfAccount.isEmpty()) {
            setShowOnlySelected(false);
        }
    }
    
    public void resetData() {
        try {
            initData();
        } catch (CommandException e) {
            LOG.error("Error while resetting data.", e);
        }
        removeCheckboxes();
        addCheckboxes();
    }
    
    public IAccountService getAccountService() {
        if (accountService == null) {
            accountService = createAccountServive();
        }
        return accountService;
    }

    private IAccountService createAccountServive() {
        return ServiceFactory.lookupAccountService();
    }

    @Override
    protected List<AccountGroup> sortItems(List<AccountGroup> list) {
        Collections.sort(list);
        return list;
    }

    public void setAccount(Configuration account) {
        this.account = account;
    }

    public void removeSelectedElements(AccountGroup accountGroup) {
        preSelectedElements.remove(accountGroup);
        selectedElementSet.remove(accountGroup);
        if (LOG.isDebugEnabled()) {
            LOG.debug(accountGroup + " removed from preSelectedElements");
        }
    }

    public void setEnabled(boolean enabled) {
        group.getParent().setEnabled(enabled);
    }
}
