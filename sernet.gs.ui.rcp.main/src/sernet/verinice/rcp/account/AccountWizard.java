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
import org.eclipse.jface.wizard.Wizard;

import sernet.verinice.model.common.accountgroup.AccountGroup;
import sernet.verinice.model.common.configuration.Configuration;

/**
 * Wizard to create and edit user account. 
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class AccountWizard extends Wizard {

    private static final Logger LOG = Logger.getLogger(AccountWizard.class);
    
    private Configuration account;
    
    private PersonPage personPage;
    private AuthenticationPage authenticationPage;
    private LimitationPage limitationPage;
    private GroupPage groupPage;
    private NotificationPage notificationPage;
    private AuditorNotificationPage auditorNotificationPage;
    private ProfilePage profilePage;
    

    public AccountWizard() {
        super();
        setNeedsProgressMonitor(true);
        setWindowTitle("Account");
        account = new Configuration();
    }
    
    public AccountWizard(Configuration account) {
        this();
        this.account = account;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.Wizard#addPages()
     */
    @Override
    public void addPages() {
        personPage = new PersonPage();
              
        addPage(personPage);
        authenticationPage = new AuthenticationPage();
        addPage(authenticationPage);
        limitationPage = new LimitationPage();
        addPage(limitationPage);
        groupPage = new GroupPage(account);
        addPage(groupPage);     
        notificationPage = new NotificationPage();
        addPage(notificationPage);
        auditorNotificationPage = new AuditorNotificationPage();
        addPage(auditorNotificationPage);
        profilePage = new ProfilePage();
        addPage(profilePage); 

        if(this.account!=null) {
            personPage.setPerson(account.getPerson());
            authenticationPage.setLogin(account.getUser());
            authenticationPage.setEmail(account.getNotificationEmail());
            limitationPage.setAdmin(account.isAdminUser());
            limitationPage.setScopeOnly(account.isScopeOnly());
            limitationPage.setWeb(account.isWebUser());
            limitationPage.setDesktop(account.isRcpUser());
            limitationPage.setDeactivated(account.isDeactivatedUser());
            notificationPage.setNotification(getAccount().isNotificationEnabled());
            notificationPage.setGlobal(getAccount().isNotificationGlobal());
            notificationPage.setNewTasks(getAccount().isNotificationMeasureAssignment());
            notificationPage.setModifyReminder(getAccount().isNotificationMeasureModification());
            notificationPage.setDeadlineWarning(getAccount().isNotificationExpirationEnabled());
            notificationPage.setDeadlineInDays(getAccount().getNotificationExpirationDays());
            auditorNotificationPage.setGlobal(getAccount().isAuditorNotificationGlobal());
            auditorNotificationPage.setDeadlineWarning(getAccount().isAuditorNotificationExpirationEnabled());
            auditorNotificationPage.setDeadlineInDays(getAccount().getAuditorNotificationExpirationDays());
            profilePage.setLogin(account.getUser());
        } 
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.Wizard#performFinish()
     */
    @Override
    public boolean performFinish() {
        getAccount().setPerson(personPage.getPerson());
        getAccount().setUser(authenticationPage.getLogin());
        getAccount().setPass(authenticationPage.getPassword());
        getAccount().setNotificationEmail(authenticationPage.getEmail());
        getAccount().setAdminUser(limitationPage.isAdmin());
        getAccount().setScopeOnly(limitationPage.isScopeOnly());
        getAccount().setWebUser(limitationPage.isWeb());
        getAccount().setRcpUser(limitationPage.isDesktop());
        getAccount().setIsDeactivatedUser(limitationPage.isDeactivated());
        
        getAccount().deleteAllRoles();
        Set<AccountGroup> selectedGroups = groupPage.getGroups();     
        for (AccountGroup accountGroup : selectedGroups) {
            getAccount().addRole(accountGroup.getName());
        }
        
        getAccount().setNotificationEnabled(notificationPage.isNotification());
        getAccount().setNotificationGlobal(notificationPage.isGlobal());
        getAccount().setNotificationMeasureAssignment(notificationPage.isNewTasks());
        getAccount().setNotificationMeasureModification(notificationPage.isModifyReminder());
        getAccount().setNotificationExpirationEnabled(notificationPage.isDeadlineWarning());
        getAccount().setNotificationExpirationDays(notificationPage.getDeadlineInDays());      
        getAccount().setAuditorNotificationGlobal(auditorNotificationPage.isGlobal());
        getAccount().setAuditorNotificationExpirationEnabled(auditorNotificationPage.isDeadlineWarning());
        getAccount().setAuditorNotificationExpirationDays(auditorNotificationPage.getDeadlineInDays());
        return true;
    }

    public Configuration getAccount() {
        return account;
    }

    public void setAccount(Configuration account) {
        this.account = account;
    }

}
