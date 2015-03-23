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
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;

import sernet.verinice.interfaces.bpm.KeyValue;
import sernet.verinice.iso27k.rcp.ComboModel;
import sernet.verinice.iso27k.rcp.IComboModelLabelProvider;
import sernet.verinice.model.common.configuration.Configuration;
import sernet.verinice.rcp.SelectionAdapter;
import sernet.verinice.rcp.TextEventAdapter;

/**
 * Wizard page of wizard {@link AccountWizard}.
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class AuditorNotificationPage extends BaseWizardPage {

    private static final Logger LOG = Logger.getLogger(AuditorNotificationPage.class);    
    public static final String PAGE_NAME = "account-wizard-auditor-notification-page"; //$NON-NLS-1$
     
    private boolean isGlobal = false;
    private boolean isDeadlineWarning = false;
    private Integer deadlineInDays = 14;

    private Button cbDeadlineWarning;
    private Text textDeadlineInDays;
    
    private ComboModel<KeyValue> comboModelGlobal;
    private Combo comboGlobal;
    
    protected AuditorNotificationPage() {
        super(PAGE_NAME);
    }

    public AuditorNotificationPage(String pageName) {
        super(pageName);
    }

    @Override
    protected void initGui(Composite composite) {
        setTitle(Messages.AuditorNotificationPage_1);
        setMessage(Messages.AuditorNotificationPage_2);

        addAllControlsFields(composite, Configuration.PROP_AUDITOR_NOTIFICATION_GLOBAL_ALL, Configuration.PROP_AUDITOR_NOTIFICATION_GLOBAL_SELF);
        addDeadlineFields(composite);      
    }

    @Override
    protected void initData() throws Exception {
    }
    
    protected void addAllControlsFields(Composite composite, final String keyYes, final String keyNo) {
        createLabel(composite, Messages.AuditorNotificationPage_3);
        comboGlobal = new Combo(composite, SWT.DROP_DOWN | SWT.READ_ONLY);
        comboGlobal.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        comboGlobal.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                comboModelGlobal.setSelectedIndex(comboGlobal.getSelectionIndex());
                KeyValue keyValue = comboModelGlobal.getSelectedObject();
                if (keyValue != null) {
                    isGlobal = keyValue.getKey().equals(keyYes);
                }
            }
        });
        comboModelGlobal = new ComboModel<KeyValue>(new IComboModelLabelProvider<KeyValue>() {
            @Override
            public String getLabel(KeyValue element) {
                return element.getValue();
            }       
        });   
        comboModelGlobal.add(new KeyValue(keyYes, Messages.AuditorNotificationPage_4));
        comboModelGlobal.add(new KeyValue(keyNo, Messages.AuditorNotificationPage_5));
        Display.getDefault().syncExec(new Runnable(){
            @Override
            public void run() {
                comboGlobal.setItems(comboModelGlobal.getLabelArray());
                int i = (isGlobal()) ? 0 : 1;
                comboGlobal.select(i);
                comboModelGlobal.setSelectedIndex(comboGlobal.getSelectionIndex()); 
            }
        });
        
    }
    

    protected void addDeadlineFields(Composite composite) {
        cbDeadlineWarning = createCheckbox(composite, Messages.AuditorNotificationPage_6, isDeadlineWarning);
        cbDeadlineWarning.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                isDeadlineWarning = cbDeadlineWarning.getSelection();
            } 
        });
        final String label = Messages.AuditorNotificationPage_7;
        createLabel(composite, label);
        textDeadlineInDays = createTextfield(composite);
        textDeadlineInDays.setText(String.valueOf(getDeadlineInDays()));
        textDeadlineInDays.addKeyListener(new TextEventAdapter() {   
            @Override
            public void keyReleased(KeyEvent e) {
                try {
                    deadlineInDays = Integer.parseInt(textDeadlineInDays.getText());
                    setErrorMessage(null);
                } catch(NumberFormatException ex) {
                    setErrorMessage(NLS.bind(Messages.AuditorNotificationPage_0, label));
                    textDeadlineInDays.setText("14"); //$NON-NLS-1$
                    deadlineInDays = 14;
                }
            }
        });
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

    public boolean isGlobal() {
        return isGlobal;
    }

    public void setGlobal(boolean isGlobal) {
        this.isGlobal = isGlobal;
    }

    public boolean isDeadlineWarning() {
        return isDeadlineWarning;
    }

    public void setDeadlineWarning(boolean isDeadlineWarning) {
        this.isDeadlineWarning = isDeadlineWarning;
    }

    public Integer getDeadlineInDays() {
        return deadlineInDays;
    }

    public void setDeadlineInDays(Integer deadlineInDays) {
        this.deadlineInDays = deadlineInDays;
    }

}
