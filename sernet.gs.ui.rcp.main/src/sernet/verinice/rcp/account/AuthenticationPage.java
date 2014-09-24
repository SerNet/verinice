package sernet.verinice.rcp.account;

import org.apache.log4j.Logger;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import sernet.verinice.rcp.KeyAdapter;

/**
 * Wizard page of wizard {@link AccountWizard}.
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class AuthenticationPage extends BaseWizardPage {

    private static final Logger LOG = Logger.getLogger(AuthenticationPage.class);    
    public static final String PAGE_NAME = "account-wizard-authentication-page"; //$NON-NLS-1$
     
    private String login;
    private String password;
    private String password2;
    private String email;
    
    private Text textLogin;  
    private Text textPassword;    
    private Text textPassword2;
    private Text textEmail; 
    
    
    protected AuthenticationPage() {
        super(PAGE_NAME);
    }
   
    @Override
    protected void initGui(Composite composite) {
        setTitle(sernet.verinice.rcp.account.Messages.AuthenticationPage_1);
        setMessage(sernet.verinice.rcp.account.Messages.AuthenticationPage_2);
        
        createLabel(composite, sernet.verinice.rcp.account.Messages.AuthenticationPage_3);
        textLogin = createTextfield(composite);
        setText(textLogin,getLogin());
        textLogin.addKeyListener(new KeyAdapter() {   
            @Override
            public void keyReleased(KeyEvent e) {
                login = avoidEmptyStrings(textLogin.getText());
                setPageComplete(isPageComplete());
            }
        });

        createLabel(composite, sernet.verinice.rcp.account.Messages.AuthenticationPage_4);       
        textPassword = createPasswordField(composite);
        textPassword.addKeyListener(new KeyAdapter() {   
            @Override
            public void keyReleased(KeyEvent e) {
                password = avoidEmptyStrings(textPassword.getText());
                setPageComplete(isPageComplete());
            }
        });

        createLabel(composite, sernet.verinice.rcp.account.Messages.AuthenticationPage_5);     
        textPassword2 = createPasswordField(composite);
        textPassword2.addKeyListener(new KeyAdapter() {   
            @Override
            public void keyReleased(KeyEvent e) {
                password2 = avoidEmptyStrings(textPassword2.getText());
                setPageComplete(isPageComplete());
            }
        });
        
        createLabel(composite, sernet.verinice.rcp.account.Messages.AuthenticationPage_6);
        textEmail = createTextfield(composite);
        setText(textEmail, getEmail());
        textEmail.addKeyListener(new KeyAdapter() {   
            @Override
            public void keyReleased(KeyEvent e) {
                email = avoidEmptyStrings(textEmail.getText());
                setPageComplete(isPageComplete());
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
        boolean complete = (getLogin()!=null) 
                && ((getPassword()!=null && getPassword2()!=null) || (getPassword()==null && getPassword2()==null))
                && (getEmail()!=null);
        boolean isPasswordValid = validatePassword();
        if (LOG.isDebugEnabled()) {
            LOG.debug("page complete: " + complete); //$NON-NLS-1$
        }
        return complete && isPasswordValid;
    }

    private boolean validatePassword() {
        boolean valid = ((getPassword()!=null && getPassword2()!=null) || (getPassword()==null && getPassword2()==null));
        if(valid && getPassword()!=null) {
            valid = getPassword().equals(getPassword2());
            if(!valid) {
                setErrorMessage(sernet.verinice.rcp.account.Messages.AuthenticationPage_7);
            } else {
                setErrorMessage(null);
                setMessage(sernet.verinice.rcp.account.Messages.AuthenticationPage_8);
            }   
        }
        return valid;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        login = avoidEmptyStrings(login);
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        password = avoidEmptyStrings(password);
        this.password = password;
    }

    public String getPassword2() {
        return password2;
    }

    public void setPassword2(String password2) {
        password2 = avoidEmptyStrings(password2);
        this.password2 = password2;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        email = avoidEmptyStrings(email);
        this.email = email;
    }

}
