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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import sernet.gs.service.StringUtil;

/**
 * Wizard page of wizard {@link AccountWizard} which shows user name, password
 * and email address.
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
        setMessage(createMessage());

        createLabel(composite, sernet.verinice.rcp.account.Messages.AuthenticationPage_3);
        textLogin = createTextfield(composite);
        setText(textLogin, getLogin());
        textLogin.addModifyListener(e -> {
            login = StringUtil.replaceEmptyStringByNull(textLogin.getText());
            setPageComplete(isPageComplete());
        });

        if (isPasswordManagedInternally()) {
            createLabel(composite, sernet.verinice.rcp.account.Messages.AuthenticationPage_4);
            textPassword = createPasswordField(composite);
            textPassword.addModifyListener(e -> {
                password = StringUtil.replaceEmptyStringByNull(textPassword.getText());
                setPageComplete(isPageComplete());
            });

            createLabel(composite, sernet.verinice.rcp.account.Messages.AuthenticationPage_5);
            textPassword2 = createPasswordField(composite);
            textPassword2.addModifyListener(e -> {
                password2 = StringUtil.replaceEmptyStringByNull(textPassword2.getText());
                setPageComplete(isPageComplete());
            });
        }

        createLabel(composite, sernet.verinice.rcp.account.Messages.AuthenticationPage_6);
        textEmail = createTextfield(composite);
        setText(textEmail, getEmail());
        textEmail.addModifyListener(e -> {
            email = StringUtil.replaceEmptyStringByNull(textEmail.getText());
            setPageComplete(isPageComplete());
        });
     }

    private String createMessage() {
        if (isPasswordManagedInternally()) {
            return Messages.AuthenticationPage_2;
        } else {
            return Messages.AuthenticationPage_0;
        }
    }

    @Override
    protected void initData() throws Exception {
        // nothing to do
    }

    @Override
    public boolean isPageComplete() {
        boolean complete = (getLogin() != null) && isPassword() && (getEmail() != null);
        boolean isPasswordValid = validatePassword();
        if (LOG.isDebugEnabled()) {
            LOG.debug("page complete: " + complete); //$NON-NLS-1$
        }
        return complete && isPasswordValid;
    }

    private boolean isPasswordManagedInternally() {
        return AccountWizard.getAuthService().isHandlingPasswords();
    }

    private boolean isPassword() {
        return (getPassword() != null && getPassword2() != null)
                || (getPassword() == null && getPassword2() == null);
    }

    private boolean validatePassword() {
        boolean valid = isPassword();

        if (valid && getPassword() != null) {
            valid = getPassword().equals(getPassword2());
            if (!valid) {
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
        this.login = StringUtil.replaceEmptyStringByNull(login);
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = StringUtil.replaceEmptyStringByNull(password);
    }

    public String getPassword2() {
        return password2;
    }

    public void setPassword2(String password2) {
        this.password2 = StringUtil.replaceEmptyStringByNull(password2);
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = StringUtil.replaceEmptyStringByNull(email);
    }
}
