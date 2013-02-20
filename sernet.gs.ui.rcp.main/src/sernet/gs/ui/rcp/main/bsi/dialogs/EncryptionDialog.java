package sernet.gs.ui.rcp.main.bsi.dialogs;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import sernet.verinice.iso27k.rcp.JobScheduler;

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
     * Possible methods for encryption.
     * 
     * @author Sebastian Engel <s.engel@tarent.de>
     * 
     */
    public enum EncryptionMethod {
        PASSWORD, X509_CERTIFICATE, PKCS11_KEY,
    }

    /**
     * The default title of this dialog.
     */
    private static final String DEFAULT_DIALOG_TITLE = Messages.EncryptionDialog_0;

    /**
     * The default message displayed in the head of this dialog.
     */
    private static final String DEFAULT_DIALOG_MESSAGE = Messages.EncryptionDialog_1;

    /**
     * Indicates which encryption method is used.
     */
    private EncryptionMethod selectedEncryptionMethod = EncryptionMethod.PASSWORD;

    /**
     * The password entered by the user
     */
    private char[] enteredPassword = "".toCharArray(); //$NON-NLS-1$

    /**
     * The X.509 public certificate file selected by the user
     */
    private File selectedX509CertificateFile;

    private String selectedKeyAlias;

    private Combo pkcs11KeyEncryptionCombo;

    private Text passwordField;
    private Text passwordField2;

    private Text certificatePathField;
    private Button browseX509CertificateButton;
    private Button certificateEncryptionRadio;

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
        final int defaultColumnNr = 3;
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

        final Button passwordEncryptionRadio = new Button(encryptionChoicePanel, SWT.RADIO);
        passwordEncryptionRadio.setSelection(true);
        passwordEncryptionRadio.setText(Messages.EncryptionDialog_3);
        passwordEncryptionRadio.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                passwordField.setEnabled(true);
                passwordField2.setEnabled(true);
                certificatePathField.setEnabled(false);
                browseX509CertificateButton.setEnabled(false);
                selectedEncryptionMethod = EncryptionMethod.PASSWORD;
                
            }
        });

        passwordField = new Text(encryptionChoicePanel, SWT.PASSWORD | SWT.BORDER);
        GridData data = new GridData();
        data.widthHint = defaultGridDataWidthHint;
        passwordField.setLayoutData(data);
        new Label(encryptionChoicePanel, SWT.NONE);

        Label labelpassword2 = new Label(encryptionChoicePanel, SWT.NONE);
        labelpassword2.setText(Messages.EncryptionDialog_6);

        passwordField2 = new Text(encryptionChoicePanel, SWT.PASSWORD | SWT.BORDER);
        GridData data2 = new GridData();
        data2.widthHint = defaultGridDataWidthHint;
        passwordField2.setLayoutData(data2);
        // FIXME: span two cols instead of using an invisible label here
        new Label(encryptionChoicePanel, SWT.NONE);

        // ==== Certificate Based Encryption controls
        certificateEncryptionRadio = new Button(encryptionChoicePanel, SWT.RADIO);
        certificateEncryptionRadio.setText(Messages.EncryptionDialog_4);
        certificateEncryptionRadio.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                selectedEncryptionMethod = EncryptionMethod.X509_CERTIFICATE;
                passwordField.setEnabled(false);
                passwordField.setText("");
                passwordField2.setEnabled(false);
                passwordField2.setText("");
                certificatePathField.setEnabled(true);
                browseX509CertificateButton.setEnabled(true);
            }
        });

        certificatePathField = new Text(encryptionChoicePanel, SWT.SINGLE | SWT.BORDER);
        data = new GridData();
        data.widthHint = defaultGridDataWidthHint;
        certificatePathField.setLayoutData(data);
        certificatePathField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                passwordEncryptionRadio.setSelection(false);
                certificateEncryptionRadio.setSelection(true);
                selectedEncryptionMethod = EncryptionMethod.X509_CERTIFICATE;
            }
        });

        browseX509CertificateButton = new Button(encryptionChoicePanel, SWT.NONE);
        browseX509CertificateButton.setText(Messages.EncryptionDialog_5);
        browseX509CertificateButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                FileDialog dialog = new FileDialog(Display.getCurrent().getActiveShell());
                dialog.setFilterExtensions(new String[] { "*.pem", }); //$NON-NLS-1$
                String certificatePath = dialog.open();
                if (certificatePath != null) {
                    selectedX509CertificateFile = new File(certificatePath);
                    certificatePathField.setText(certificatePath);
                } else {
                    certificatePathField.setText(""); //$NON-NLS-1$
                }

                passwordEncryptionRadio.setSelection(false);
                certificateEncryptionRadio.setSelection(true);
                selectedEncryptionMethod = EncryptionMethod.X509_CERTIFICATE;
            }
        });

        passwordField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                passwordEncryptionRadio.setSelection(true);
                certificateEncryptionRadio.setSelection(false);
                selectedEncryptionMethod = EncryptionMethod.PASSWORD;
            }
        });
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

        // ==== Certificate Based Encryption controls
        final Button pkcs11KeyEncryptionRadio = new Button(encryptionChoicePanel, SWT.RADIO);
        pkcs11KeyEncryptionRadio.setText("Verschlüsselung mit Schlüssel aus PKCS#11-Bibliothek");
        pkcs11KeyEncryptionRadio.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                selectedEncryptionMethod = EncryptionMethod.PKCS11_KEY;
                passwordField.setEnabled(false);
                passwordField.setText("");
                passwordField2.setEnabled(false);
                passwordField2.setText("");
                certificatePathField.setEnabled(false);
                browseX509CertificateButton.setEnabled(false);
                updateCombo();
            }
        });
        pkcs11KeyEncryptionCombo = new Combo(encryptionChoicePanel, SWT.DROP_DOWN | SWT.READ_ONLY);
        data = new GridData();
        data.widthHint = defaultGridDataWidthHint;
        pkcs11KeyEncryptionCombo.setLayoutData(data);
        pkcs11KeyEncryptionCombo.setEnabled(false);
        pkcs11KeyEncryptionCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                selectedKeyAlias = pkcs11KeyEncryptionCombo.getText();
            }
        });

        encryptionChoicePanel.pack();
        composite.pack();
        return composite;
    }

    private void updateCombo() {
        WorkspaceJob job = new WorkspaceJob("verfügbare Schlüssel einlesen") {
            @Override
            public IStatus runInWorkspace(final IProgressMonitor monitor) {
                IStatus status = Status.OK_STATUS;

                try {
                    KeyStore ks = KeyStore.getInstance("PKCS11", "SunPKCS11-verinice");
                    ks.load(null, null);
                    Enumeration<String> en = ks.aliases();
                    final List<String> l = new ArrayList<String>();
                    while (en.hasMoreElements()) {
                        l.add(en.nextElement());
                    }

                    Display.getDefault().asyncExec(new Runnable() {
                        public void run() {
                            pkcs11KeyEncryptionCombo.removeAll();
                            for (String s : l) {
                                pkcs11KeyEncryptionCombo.add(s);
                            }
                            pkcs11KeyEncryptionCombo.setEnabled(true);
                        }
                    });
                } catch (GeneralSecurityException gse) {
                    status = Status.CANCEL_STATUS;
                } catch (IOException ioe) {
                    status = Status.CANCEL_STATUS;
                }

                return status;
            }
        };
        JobScheduler.scheduleJob(job, null);
    }

    /**
     * @return the selected encryption method
     */
    public EncryptionMethod getSelectedEncryptionMethod() {
        return selectedEncryptionMethod;
    }

    /**
     * @return the password to use for encryption entered by the user
     */
    public char[] getEnteredPassword() {
        return (enteredPassword != null) ? enteredPassword.clone() : null;
    }

    /**
     * @return the X.509 certificate file to use for encryption selected by the
     *         user
     */
    public File getSelectedX509CertificateFile() {
        return selectedX509CertificateFile;
    }

    public String getSelectedKeyAlias() {
        return selectedKeyAlias;
    }

    protected void okPressed() {
        if (passwordField.getText().equals(passwordField2.getText())) {
            passwordField.getText();
            super.okPressed();
        } else {
            MessageDialog.openWarning(this.getShell(), Messages.EncryptionDialog_7, Messages.EncryptionDialog_8);
        }
    }

}
