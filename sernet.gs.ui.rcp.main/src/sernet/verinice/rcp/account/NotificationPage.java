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

import org.apache.log4j.Logger;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import sernet.verinice.model.common.configuration.Configuration;
import sernet.verinice.rcp.SelectionAdapter;

/**
 * Wizard page of wizard {@link AccountWizard}.
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class NotificationPage extends AuditorNotificationPage {

    private static final Logger LOG = Logger.getLogger(NotificationPage.class);    
    public static final String PAGE_NAME = "account-wizard-notification-page"; //$NON-NLS-1$
     
    private boolean isNotification = false;
    private boolean isNewTasks = false;
    private boolean isModifyReminder = false;
    
    private Button cbNotification;
    private Button cbNewTasks;
    private Button cbModifyReminder;
    
    protected NotificationPage() {
        super(PAGE_NAME);
    }
    
    @Override
    protected void initGui(Composite composite) {
        setTitle(Messages.NotificationPage_1);
        setMessage(Messages.NotificationPage_2);

        cbNotification = createCheckbox(composite, Messages.NotificationPage_3, isNotification);
        cbNotification.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                isNotification = cbNotification.getSelection();
            } 
        });
        addAllControlsFields(composite, Configuration.PROP_NOTIFICATION_GLOBAL_ALL, Configuration.PROP_NOTIFICATION_GLOBAL_SELF);
        cbNewTasks = createCheckbox(composite, Messages.NotificationPage_4, isNewTasks);
        cbNewTasks.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                isNewTasks = cbNewTasks.getSelection();
            } 
        });
        cbModifyReminder = createCheckbox(composite, Messages.NotificationPage_5, isModifyReminder);
        cbModifyReminder.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                isModifyReminder = cbModifyReminder.getSelection();
            } 
        });
        addDeadlineFields(composite);      
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

    public boolean isNotification() {
        return isNotification;
    }

    public void setNotification(boolean isNotification) {
        this.isNotification = isNotification;
    }

    public boolean isNewTasks() {
        return isNewTasks;
    }

    public void setNewTasks(boolean isNewTasks) {
        this.isNewTasks = isNewTasks;
    }

    public boolean isModifyReminder() {
        return isModifyReminder;
    }

    public void setModifyReminder(boolean isModifyReminder) {
        this.isModifyReminder = isModifyReminder;
    }

}
