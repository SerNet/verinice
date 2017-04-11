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

import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.swt.widgets.Composite;

import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.verinice.interfaces.ApplicationRoles;
import sernet.verinice.interfaces.IAccountService;
import sernet.verinice.interfaces.IAuthService;
import sernet.verinice.model.common.accountgroup.AccountGroup;
import sernet.verinice.model.common.configuration.Configuration;

/**
 * Wizard page of wizard {@link AccountWizard}.
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class GroupPage extends BaseWizardPage {

    private static final Logger LOG = Logger.getLogger(GroupPage.class);    
    public static final String PAGE_NAME = "account-wizard-group-page"; //$NON-NLS-1$
     
    private Configuration account;
    
    private AccountGroupMultiselectWidget groupWidget;
    
    private IAccountService accountService;
    
    protected GroupPage() {
        super(PAGE_NAME);      
    }
    
    protected GroupPage(Configuration account) {
        super(PAGE_NAME);
        this.account = account;
    }

    @Override
    protected void initGui(Composite composite) {
        setTitle(Messages.GroupPage_1);
        setMessage(Messages.GroupPage_2);
        
        groupWidget = new AccountGroupMultiselectWidget(composite, account);
        final boolean isLocalAdmin = getAuthService().currentUserHasRole(new String[] { ApplicationRoles.ROLE_LOCAL_ADMIN });
        groupWidget.setEnabled(!isLocalAdmin);
    }

    @Override
    protected void initData() throws Exception {
    }
    
    public void syncCheckboxesToAccountGroups() {
        account.deleteAllRoles();    
        for (AccountGroup accountGroup : getGroupsFromWidget()) {
            account.addRole(accountGroup.getName());
        }
    }
    
    public void reSelectStandartGroups(Set<String> standartGroupSet) {
        deselectStandartGroups();
        this.groupWidget.resetData();
        
    }
    
    private void deselectStandartGroups() {
        Set<AccountGroup> allStandartAccountGroups = AccountGroup.createStandartGroupSet();
        for (AccountGroup accountGroup : allStandartAccountGroups) {
            this.groupWidget.removeSelectedElements(accountGroup);      
        }
    }
        
    
    public void selectStandartGroups(Set<String> standartGroupNames) {
        Set<AccountGroup> accountGroupSet = AccountGroup.createSetForNames(standartGroupNames);
        for (AccountGroup accountGroup : accountGroupSet) {
            this.groupWidget.selectCheckboxForElement(accountGroup);      
        }    
    }

    

    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.WizardPage#isPageComplete()
     */
    @Override
    public boolean isPageComplete() {
        boolean complete = true;
        if (LOG.isDebugEnabled()) {
            LOG.debug("page complete: " + complete); //$NON-NLS-1$
        }
        return complete;
    }
    
    public Set<AccountGroup> getGroupsFromWidget() {
        return groupWidget.getSelectedElementSet();
    }
    
  

    public void setAccount(Configuration account) {
        this.account = account;
        this.groupWidget.setAccount(account);
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

    private IAuthService getAuthService() {
        return ServiceFactory.lookupAuthService();
    }
}
