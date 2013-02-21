/*******************************************************************************
 * Copyright (c) 2011 Robert Schuster <r.schuster[at]tarent[dot]de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *     You should have received a copy of the GNU Lesser General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Robert Schuster <r.schuster[at]tarent[dot]de> - initial API and implementation
 ******************************************************************************/

package sernet.gs.ui.rcp.main.security;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * A dialog which asks the user for the key- and truststore password
 * as well as a smartcard PIN.
 * 
 * <p>The dialog allows the user to save some time by typing all the
 * credentials that are possibly needed at once.</p>
 * 
 * @author Robert Schuster <r.schuster[at]tarent[dot]de>
 *
 */
final class PasswordDialog extends Dialog {
	private Text keyStorePasswordText;
	private Text tokenPINText;

	private char[] keyStorePassword = null;
	private char[] tokenPIN = null;
	
	private boolean isKeyStoreEnabled;
	private boolean isTokenPINEnabled;
	
	static enum Type { KEY, TOKEN };
	
	private Type focus;

	/**
	 * Creates a dialog where certain inputs will be disabled, if necessary.
	 * 
	 * @param parentShell
	 * @param isKeyStoreEnabled
	 * @param isTokenPINEnabled
	 */
	protected PasswordDialog(Shell parentShell, boolean isKeyStoreEnabled, boolean isTokenPINEnabled) {
		super(parentShell);
		setShellStyle(SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		setBlockOnOpen(true);
		
		this.isKeyStoreEnabled = isKeyStoreEnabled;
		this.isTokenPINEnabled = isTokenPINEnabled;
	}
	
	/**
	 * Allows setting the focus on a specific textfield.
	 * 
	 * @param t
	 */
	void setFocus(Type t) {
		focus = t;
	}

	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText("Zertifikat- und Schlüsselspeicher und PIN-Eingabe");
	}

    @Override
	protected Control createDialogArea(Composite parent) {
        final int defaultTextLimit = 15;
		Composite container = (Composite) super.createDialogArea(parent);
		GridLayout layout = new GridLayout(2, true);
		container.setLayout(layout);

		Label l2 = new Label(container, SWT.HORIZONTAL);
		l2.setText("keystore password: ");
		l2.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
		l2.setEnabled(isKeyStoreEnabled);
		
		keyStorePasswordText = new Text(container, SWT.SINGLE | SWT.PASSWORD | SWT.BORDER);
		keyStorePasswordText.setTextLimit(defaultTextLimit);
		keyStorePasswordText.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));
		keyStorePasswordText.setEnabled(isKeyStoreEnabled);

		Label l3 = new Label(container, SWT.HORIZONTAL);
		l3.setText("token PIN: ");
		l3.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
		l3.setEnabled(isTokenPINEnabled);
		
		tokenPINText = new Text(container, SWT.SINGLE | SWT.PASSWORD | SWT.BORDER);
		tokenPINText.setTextLimit(defaultTextLimit);
		tokenPINText.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));
		tokenPINText.setEnabled(isTokenPINEnabled);
		
		switch (focus) {
		case KEY:
			keyStorePasswordText.forceFocus();
			break;
		case TOKEN:
			tokenPINText.forceFocus();
			break;
		}

		return container;
	}
    
    @Override
    protected void okPressed() {
    	/*
    	 * If a user did not type anything into a field this should be regarded as
    	 * no entry. To make that happens the string values from the textfields have
    	 * to be checked for their lengths.
    	 */
		keyStorePassword = zeroLengthCheck(keyStorePasswordText.getText());
		tokenPIN = zeroLengthCheck(tokenPINText.getText());
		
		super.okPressed();
    }
    
    /** Returns 'null' if the string is of zero length after removing
     * whitespace from it.
     * 
     * @param s
     * @return
     */
    private char[] zeroLengthCheck(String s) {
    	return (s.trim().length() == 0) ? null : s.toCharArray();
    }
    
    char[] getKeyStorePassword() {
    	return keyStorePassword;
    }
    
    char[] getTokenPIN() {
    	return tokenPIN;
    }
	
    /** Removes references to sensitive data from this object.
     */
    void clearPasswords() {
    	keyStorePassword = null;
    	tokenPIN = null;
    }
}