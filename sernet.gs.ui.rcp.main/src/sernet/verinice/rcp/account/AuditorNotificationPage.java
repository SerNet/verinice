package sernet.verinice.rcp.account;

import org.apache.log4j.Logger;
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
import sernet.verinice.iso27k.rcp.ComboModelLabelProvider;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.configuration.Configuration;
import sernet.verinice.rcp.KeyAdapter;
import sernet.verinice.rcp.SelectionAdapter;

/**
 * Wizard page of wizard {@link AccountWizard}.
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class AuditorNotificationPage extends BaseWizardPage {

    private static final Logger LOG = Logger.getLogger(AuditorNotificationPage.class);    
    public static final String PAGE_NAME = "account-wizard-auditor-notification-page";
     
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
        setTitle("Account (6/7)");
        setMessage("Auditor notifications");

        addAllControlsFields(composite, Configuration.PROP_AUDITOR_NOTIFICATION_GLOBAL_ALL, Configuration.PROP_AUDITOR_NOTIFICATION_GLOBAL_SELF);
        addDeadlineFields(composite);      
    }

    @Override
    protected void initData() throws Exception {
    }
    
    protected void addAllControlsFields(Composite composite, final String keyYes, final String keyNo) {
        createLabel(composite, "All controls / only my own");
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
        comboModelGlobal = new ComboModel<KeyValue>(new ComboModelLabelProvider<KeyValue>() {
            @Override
            public String getLabel(KeyValue element) {
                return element.getValue();
            }       
        });   
        comboModelGlobal.add(new KeyValue(keyYes, "All controls"));
        comboModelGlobal.add(new KeyValue(keyNo, "Only my own"));
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
        cbDeadlineWarning = createCheckbox(composite, "Deadline warning", isDeadlineWarning);
        cbDeadlineWarning.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                isDeadlineWarning = cbDeadlineWarning.getSelection();
            } 
        });
        final String label = "Deadline warning: ending in X days";
        createLabel(composite, label);
        textDeadlineInDays = createTextfield(composite);
        textDeadlineInDays.setText(Integer.toString(getDeadlineInDays()));
        textDeadlineInDays.addKeyListener(new KeyAdapter() {   
            @Override
            public void keyReleased(KeyEvent e) {
                try {
                    deadlineInDays = Integer.parseInt(textDeadlineInDays.getText());
                    setErrorMessage(null);
                } catch(NumberFormatException ex) {
                    setErrorMessage("Please enter a number in field \"" + label + "\".");
                    textDeadlineInDays.setText("14");
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
