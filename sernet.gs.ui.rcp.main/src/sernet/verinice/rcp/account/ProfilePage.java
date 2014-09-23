package sernet.verinice.rcp.account;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import sernet.hui.common.VeriniceContext;
import sernet.verinice.interfaces.IRightsServiceClient;
import sernet.verinice.model.auth.ProfileRef;
import sernet.verinice.model.auth.Userprofile;
import sernet.verinice.rcp.ProfileLabelProvider;
import sernet.verinice.rcp.ProfileTableComparator;

/**
 * Wizard page of wizard {@link AccountWizard}.
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class ProfilePage extends BaseWizardPage {

    private static final Logger LOG = Logger.getLogger(ProfilePage.class);    
    public static final String PAGE_NAME = "account-wizard-profile-page";
    
    private String login;
    Set<ProfileRef> profileSet;
    
    private TableViewer table;
    
    private IRightsServiceClient rightsService;
    
    protected ProfilePage() {
        super(PAGE_NAME);
    }

    @Override
    protected void initGui(Composite composite) {
        setTitle("Account (7/7)");
        setMessage("User profiles");
        
        Label label = new Label(composite, SWT.NONE);
        label.setText("Please note that rather than assigning profiles \nto accounts directly you should assign them \nto an account group instead.\n");
        table = createTable(composite,"User profiles of account");
        table.setLabelProvider(new ProfileLabelProvider());
        table.setComparator(new ProfileTableComparator());
        table.setContentProvider(new ArrayContentProvider());       
        table.refresh(true);

    }
    
    private TableViewer createTable(Composite parent, String title) {
        Label label = new Label(parent, SWT.WRAP);
        label.setText(title);
        label.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

        TableViewer table0 = new TableViewer(parent, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI);

        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        table0.getControl().setLayoutData(gd);

        table0.setUseHashlookup(true);

        return table0;
    }

    @Override
    protected void initData() throws Exception {
        profileSet = new HashSet<ProfileRef>();
        List<Userprofile> userProfileList = getRightService().getUserprofile(login);
        for (Userprofile userprofile : userProfileList) {  
            profileSet.addAll(userprofile.getProfileRef());           
        }
        table.setInput(profileSet);
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
    
    public void setLogin(String login) {
        this.login = login;
    }

    private IRightsServiceClient getRightService() {
        if (rightsService == null) {
            rightsService = (IRightsServiceClient) VeriniceContext.get(VeriniceContext.RIGHTS_SERVICE);
        }
        return rightsService;
    }

}
