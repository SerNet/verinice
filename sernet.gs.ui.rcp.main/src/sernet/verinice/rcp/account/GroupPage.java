package sernet.verinice.rcp.account;

import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.swt.widgets.Composite;

import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.verinice.interfaces.IAccountService;
import sernet.verinice.iso27k.rcp.ElementMultiselectWidget;
import sernet.verinice.model.common.accountgroup.AccountGroup;
import sernet.verinice.model.common.configuration.Configuration;
import sernet.verinice.rcp.MultiselectWidget;

/**
 * Wizard page of wizard {@link AccountWizard}.
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class GroupPage extends BaseWizardPage {

    private static final Logger LOG = Logger.getLogger(GroupPage.class);    
    public static final String PAGE_NAME = "account-wizard-group-page";
     
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
        setTitle("Account (4/7)");
        setMessage("Account groups");
        
        groupWidget = new AccountGroupMultiselectWidget(composite, account);
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
    
    public Set<AccountGroup> getGroups() {
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

}
