package sernet.verinice.rcp.account;

import org.apache.log4j.Logger;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import sernet.verinice.rcp.SelectionAdapter;

/**
 * Wizard page of wizard {@link AccountWizard}.
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class LimitationPage extends BaseWizardPage {

    private static final Logger LOG = Logger.getLogger(LimitationPage.class);    
    public static final String PAGE_NAME = "account-wizard-limitation-page"; //$NON-NLS-1$
     
    private boolean isAdmin = false;
    private boolean isScopeOnly = false;
    private boolean isDesktop = true;
    private boolean isWeb = true;
    private boolean isDeactivated = false;
    
    private Button cbAdmin;
    private Button cbScopeOnly;
    private Button cbDesktop;
    private Button cbWeb;
    private Button cbDeactivated;
    
    protected LimitationPage() {
        super(PAGE_NAME);
    }
    
    @Override
    protected void initGui(Composite composite) {
        setTitle(Messages.LimitationPage_1);
        setMessage(Messages.LimitationPage_2);
        
        cbAdmin = createCheckbox(composite, Messages.LimitationPage_3, isAdmin);
        cbAdmin.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                isAdmin = cbAdmin.getSelection();
            } 
        });
        cbScopeOnly = createCheckbox(composite, Messages.LimitationPage_4, isScopeOnly);
        cbScopeOnly.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                isScopeOnly = cbScopeOnly.getSelection();
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

}
