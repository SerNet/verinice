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

import static sernet.verinice.interfaces.IRightsService.STANDARD_GROUPS;

import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.verinice.interfaces.ApplicationRoles;
import sernet.verinice.interfaces.IAuthService;
import sernet.verinice.interfaces.IRightsService;
import sernet.verinice.model.common.configuration.Configuration;
import sernet.verinice.rcp.SelectionAdapter;

/**
 * Wizard page of wizard {@link AccountWizard}.
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class LimitationPage extends BaseWizardPage {

    private static final Logger LOG = Logger.getLogger(LimitationPage.class);    
    public static final String PAGE_NAME = "account-wizard-limitation-page"; //$NON-NLS-1$
     
    private Configuration account;
    
    private boolean isAdmin = false;
    private boolean isLocalAdmin = false;
    private boolean isScopeOnly = false;
    private boolean isDesktop = true;
    private boolean isWeb = true;
    private boolean isDeactivated = false;
    
    private Button cbAdmin;
    private Button cbLocalAdmin;
    private Button cbScopeOnly;
    private Button cbDesktop;
    private Button cbWeb;
    private Button cbDeactivated;
    
    protected LimitationPage(Configuration account) {
        super(PAGE_NAME);
        this.account = account;
    }
    
    @Override
    protected void initGui(Composite composite) {
        setTitle(Messages.LimitationPage_1);
        setMessage(Messages.LimitationPage_2);
        
        final boolean currentUserIsLocalAdmin = getAuthService().currentUserHasRole(new String[] { ApplicationRoles.ROLE_LOCAL_ADMIN });

        cbAdmin = createCheckbox(composite, Messages.LimitationPage_3, isAdmin);
        cbAdmin.setEnabled(!currentUserIsLocalAdmin && !isLocalAdmin);
        cbAdmin.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                isAdmin = cbAdmin.getSelection();
                cbLocalAdmin.setEnabled(!isAdmin && !isScopeOnly);
                configureStandartGroup();
                changeGroupPage();
            } 
        });
        cbLocalAdmin = createCheckbox(composite, Messages.LimitationPage_8, isLocalAdmin);
        cbLocalAdmin.setEnabled(!isAdmin && !isScopeOnly);
        cbLocalAdmin.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                isLocalAdmin = cbLocalAdmin.getSelection();
                cbAdmin.setEnabled(!currentUserIsLocalAdmin && !isLocalAdmin);
                cbScopeOnly.setEnabled(!isLocalAdmin);
                configureStandartGroup();
                changeGroupPage();
            }
        });
        cbScopeOnly = createCheckbox(composite, Messages.LimitationPage_4, isScopeOnly);
        if (currentUserIsLocalAdmin) {
            cbScopeOnly.setEnabled(!isAdmin && !isLocalAdmin);
        } else {
            cbScopeOnly.setEnabled(!isLocalAdmin);
        }
        cbScopeOnly.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                isScopeOnly = cbScopeOnly.getSelection();
                cbLocalAdmin.setEnabled(!isAdmin && !isScopeOnly);
                configureStandartGroup();
                changeGroupPage();
            } 
        });
        cbDesktop = createCheckbox(composite, Messages.LimitationPage_5, isDesktop);
        cbDesktop.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                isDesktop = cbDesktop.getSelection();
            } 
        });
        cbWeb = createCheckbox(composite, Messages.LimitationPage_6, isWeb);
        cbWeb.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                isWeb = cbWeb.getSelection();
            } 
        });
        cbDeactivated = createCheckbox(composite, Messages.LimitationPage_7, isDeactivated);
        cbDeactivated.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                isDeactivated = cbDeactivated.getSelection();
            } 
        });
    }
    
    private void changeGroupPage() {
        GroupPage groupPage = (GroupPage) getNextPage();
        Set<String> standartGroupsOfAccount = account.getStandartGroups();
        groupPage.reSelectStandartGroups(standartGroupsOfAccount);
    }

    private void configureStandartGroup() {
        deleteStandartGroups();
        if (isAdmin()) {
            configureAdminGroup();
        } else if (isLocalAdmin()) {
            configureLocalAdminGroup();
        } else {
            configureUserGroup();
        }
    }

    private void configureAdminGroup() {
        if(isScopeOnly()) {
            account.addRole(IRightsService.ADMINSCOPEDEFAULTGROUPNAME);   
        } else {
            account.addRole(IRightsService.ADMINDEFAULTGROUPNAME); 
        }
    }
    
    private void configureLocalAdminGroup() {
        account.addRole(IRightsService.ADMINLOCALDEFAULTGROUPNAME);
    }

    private void configureUserGroup() {
        if(isScopeOnly()) {
            account.addRole(IRightsService.USERSCOPEDEFAULTGROUPNAME);    
        } else {
            account.addRole(IRightsService.USERDEFAULTGROUPNAME);   
        }
    }

    private void deleteStandartGroups() {
        Set<String> rolesInAccount = account.getRoles(false);
        for (String role : rolesInAccount) {
            if(isStandardGroup(role)) {
                account.deleteRole(role);
            }
        }
    }


    private static boolean isStandardGroup(String role) {
        return ArrayUtils.contains(STANDARD_GROUPS, role);
    }

    @Override
    protected void initData() throws Exception {
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

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    public boolean isLocalAdmin() {
        return isLocalAdmin;
    }

    public void setLocalAdmin(boolean isLocalAdmin) {
        this.isLocalAdmin = isLocalAdmin;
    }

    public boolean isScopeOnly() {
        return isScopeOnly;
    }

    public void setScopeOnly(boolean isScopeOnly) {
        this.isScopeOnly = isScopeOnly;
    }

    public boolean isDesktop() {
        return isDesktop;
    }

    public void setDesktop(boolean isDesktop) {
        this.isDesktop = isDesktop;
    }

    public boolean isWeb() {
        return isWeb;
    }

    public boolean isDeactivated() {
        return isDeactivated;
    }

    public void setDeactivated(boolean isDeactivated) {
        this.isDeactivated = isDeactivated;
    }

    public void setWeb(boolean isWeb) {
        this.isWeb = isWeb;
    }

    private IAuthService getAuthService() {
        return ServiceFactory.lookupAuthService();
    }
}
