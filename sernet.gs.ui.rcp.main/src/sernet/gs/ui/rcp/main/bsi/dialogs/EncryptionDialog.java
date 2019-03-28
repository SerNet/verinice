package sernet.gs.ui.rcp.main.bsi.dialogs;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Dialog asking the user to enter a password or select a X.509 certificate file
 * used for encryption.
 * 
 * <p>
 * The default selected encryption method is Password Based Encryption.
 * </p>
 * 
 * @author Sebastian Engel <sengel@tarent.de>
 * 
 * 
 */
public class EncryptionDialog extends TitleAreaDialog {

    /**
     * The default title of this dialog.
     */
    private static final String DEFAULT_DIALOG_TITLE = Messages.EncryptionDialog_0;

    /**
     * The default message displayed in the head of this dialog.
     */
    private static final String DEFAULT_DIALOG_MESSAGE = Messages.EncryptionDialog_1;

    /**
     * The password entered by the user
     */
    private char[] enteredPassword = "".toCharArray(); //$NON-NLS-1$

    private Text passwordField;
    private Text passwordField2;

    /**
     * Creates a new EncryptionDialog.
     * 
     * @param parentShell
     *            the parent shell
     */
    public EncryptionDialog(Shell parentShell) {
        super(parentShell);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        final int defaultMarginWidth = 10;
        final int defaultMarginHeight = defaultMarginWidth;
        final int defaultColumnNr = 2;
        final int defaultGridDataWidthHint = 280;
        setTitle(DEFAULT_DIALOG_TITLE);
        setMessage(DEFAULT_DIALOG_MESSAGE);

        Composite composite = (Composite) super.createDialogArea(parent);
        GridLayout compositeLayout = (GridLayout) composite.getLayout();
        compositeLayout.marginWidth = defaultMarginWidth;
        compositeLayout.marginHeight = defaultMarginHeight;

        // ===== Password Based Encryption controls =====
        Composite encryptionChoicePanel = new Composite(composite, SWT.NONE);
        GridLayout pbeLayout = new GridLayout(defaultColumnNr, false);
        encryptionChoicePanel.setLayout(pbeLayout);

        Label labelpassword = new Label(encryptionChoicePanel, SWT.NONE);
        labelpassword.setText(Messages.EncryptionDialog_3);

        passwordField = new Text(encryptionChoicePanel, SWT.PASSWORD | SWT.BORDER);
        GridData data = new GridData();
        data.widthHint = defaultGridDataWidthHint;
        passwordField.setLayoutData(data);

        Label labelpassword2 = new Label(encryptionChoicePanel, SWT.NONE);
        labelpassword2.setText(Messages.EncryptionDialog_6);

        passwordField2 = new Text(encryptionChoicePanel, SWT.PASSWORD | SWT.BORDER);
        GridData data2 = new GridData();
        data2.widthHint = defaultGridDataWidthHint;
        passwordField2.setLayoutData(data2);

        passwordField.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                if (passwordField.getText() != null) {
                    enteredPassword = passwordField.getText().toCharArray();
                } else {
                    enteredPassword = "".toCharArray(); //$NON-NLS-1$
                }
            }
        });

        encryptionChoicePanel.pack();
        composite.pack();
        return composite;
    }

    /**
     * @return the password to use for encryption entered by the user
     */
    public char[] getEnteredPassword() {
        return (enteredPassword != null) ? enteredPassword.clone() : null;
    }

    protected void okPressed() {
        if (passwordField.getText().equals(passwordField2.getText())) {
            passwordField.getText();
            super.okPressed();
        } else {
            MessageDialog.openWarning(this.getShell(), Messages.EncryptionDialog_7,
                    Messages.EncryptionDialog_8);
        }
    }

}
