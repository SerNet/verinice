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

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;

import sernet.verinice.model.common.configuration.Configuration;

/**
 * Wizard to create and edit user account. 
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class AccountWizard extends Wizard {
    
    private Configuration account;
    
    private PersonPage personPage;
    private AuthenticationPage authenticationPage;
    private LimitationPage limitationPage;
    private GroupPage groupPage;
    private NotificationPage notificationPage;
    private AuditorNotificationPage auditorNotificationPage;
    private ProfilePage profilePage;
    private LicenseMgmtPage licenseMgmtPage;
    
    public AccountWizard(Configuration account) {
        super(); 
        this.account = account;
        init();
    }
    
    private void init() {
        setNeedsProgressMonitor(true);
        setWindowTitle(Messages.AccountWizard_0);
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
        limitationPage = new LimitationPage(account);
        addPage(limitationPage);
        groupPage = new GroupPage(account);
        addPage(groupPage);     
        licenseMgmtPage = new LicenseMgmtPage(account);
        addPage(licenseMgmtPage);
        notificationPage = new NotificationPage();
        addPage(notificationPage);
        auditorNotificationPage = new AuditorNotificationPage();
        addPage(auditorNotificationPage);
        profilePage = new ProfilePage();
        addPage(profilePage); 

        if (this.account != null) {
            personPage.setPerson(account.getPerson());
            personPage.setNewAccount(isNewAccount());
            authenticationPage.setLogin(account.getUser());
            authenticationPage.setEmail(account.getNotificationEmail());
            limitationPage.setAdmin(account.isAdminUser());
            limitationPage.setLocalAdmin(account.isLocalAdminUser());
            limitationPage.setScopeOnly(account.isScopeOnly());
            limitationPage.setWeb(account.isWebUser());
            limitationPage.setDesktop(account.isRcpUser());
            limitationPage.setDeactivated(account.isDeactivatedUser());
            licenseMgmtPage.setUser(account.getUser());
            licenseMgmtPage.setAssignedLicenseIds(account.getAssignedLicenseIds());
            
            licenseMgmtPage.setSendEmail(account.getNotificationLicense());
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
        getAccount().setUserNew(authenticationPage.getLogin());
        getAccount().setPassNew(authenticationPage.getPassword());
        getAccount().setNotificationEmail(authenticationPage.getEmail());
        getAccount().setAdminUser(limitationPage.isAdmin());
        getAccount().setLocalAdminUser(limitationPage.isLocalAdmin());
        getAccount().setScopeOnly(limitationPage.isScopeOnly());
        getAccount().setWebUser(limitationPage.isWeb());
        getAccount().setRcpUser(limitationPage.isDesktop());
        getAccount().setIsDeactivatedUser(limitationPage.isDeactivated());
        
        groupPage.syncCheckboxesToAccountGroups();

        getAccount().setNotificationEnabled(notificationPage.isNotification());
        getAccount().setNotificationGlobal(notificationPage.isGlobal());
        getAccount().setNotificationMeasureAssignment(notificationPage.isNewTasks());
        getAccount().setNotificationMeasureModification(notificationPage.isModifyReminder());
        getAccount().setNotificationExpirationEnabled(notificationPage.isDeadlineWarning());
        
        getAccount().setNotificationExpirationDays(notificationPage.getDeadlineInDays());
        
        getAccount().setAuditorNotificationGlobal(auditorNotificationPage.isGlobal());
        getAccount().setAuditorNotificationExpirationEnabled(auditorNotificationPage.isDeadlineWarning());
        
        getAccount().setAuditorNotificationExpirationDays(auditorNotificationPage.getDeadlineInDays());
        
        getAccount().setNotificationLicense(licenseMgmtPage.isSendEmail());
        
        return true;
    }


    
    @Override
    public IWizardPage getStartingPage() {
        IWizardPage startingPage = super.getStartingPage();
        if (!isNewAccount()) {
            startingPage = authenticationPage;
        }
        return startingPage;
    }
    
    private boolean isNewAccount() {
        boolean isNew = true;
        if (account != null) {
            isNew = (account.getDbId() == null);
        }
        return isNew;
    }
  
    public Configuration getAccount() {
        return account;
    }

    public void setAccount(Configuration account) {
        this.account = account;
    }

}
