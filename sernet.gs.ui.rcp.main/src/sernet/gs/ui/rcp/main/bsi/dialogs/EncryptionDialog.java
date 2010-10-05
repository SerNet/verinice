package sernet.gs.ui.rcp.main.bsi.dialogs;

import java.io.File;

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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
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
	 * Possible methods for encryption.
	 *  
	 * @author Sebastian Engel <s.engel@tarent.de>
	 *
	 */
	public enum EncryptionMethod {
		PASSWORD,
		X509_CERTIFICATE
	}
	
	/**
	 * The default title of this dialog.
	 */
	private static final String DEFAULT_DIALOG_TITLE = Messages.EncryptionDialog_0;
	
	/**
	 * The default message displayed in the head of this dialog.
	 */
	private static final String DEFAULT_DIALOG_MESSAGE = 
		Messages.EncryptionDialog_1;
	
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
	
	/**
	 * Creates a new EncryptionDialog.
	 * 
	 * @param parentShell the parent shell
	 */
	public EncryptionDialog(Shell parentShell) {
		super(parentShell);
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		setTitle(DEFAULT_DIALOG_TITLE);
		setMessage(DEFAULT_DIALOG_MESSAGE);

		Composite composite = (Composite) super.createDialogArea(parent);
		GridLayout compositeLayout = (GridLayout) composite.getLayout();
		compositeLayout.marginWidth = 10;
		compositeLayout.marginHeight = 10;

		// ===== Password Based Encryption controls =====
		Composite encryptionChoicePanel = new Composite(composite, SWT.NONE);
		GridLayout pbeLayout = new GridLayout(3, false);
		encryptionChoicePanel.setLayout(pbeLayout);

		final Button passwordEncryptionRadio = new Button(encryptionChoicePanel, SWT.RADIO);
		passwordEncryptionRadio.setSelection(true);
		passwordEncryptionRadio.setText(Messages.EncryptionDialog_3);
		passwordEncryptionRadio.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				selectedEncryptionMethod = EncryptionMethod.PASSWORD;
			}
		});
		
		final Text passwordField = new Text(encryptionChoicePanel, SWT.PASSWORD | SWT.BORDER);
		GridData data = new GridData();
		data.widthHint = 280;
		passwordField.setLayoutData(data);
		
		// FIXME: span two cols instead of using an invisible label here 
		new Label(encryptionChoicePanel, SWT.NONE);
		
		// ==== Certificate Based Encryption controls
		final Button certificateEncryptionRadio = new Button(encryptionChoicePanel, SWT.RADIO);
		certificateEncryptionRadio.setText(Messages.EncryptionDialog_4);
		certificateEncryptionRadio.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				selectedEncryptionMethod = EncryptionMethod.X509_CERTIFICATE;
			}
		});
		
		final Text certificatePathField = new Text(encryptionChoicePanel, SWT.SINGLE | SWT.BORDER);
		data = new GridData();
		data.widthHint = 280;
		certificatePathField.setLayoutData(data);
		certificatePathField.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				passwordEncryptionRadio.setSelection(false);
				certificateEncryptionRadio.setSelection(true);
				selectedEncryptionMethod = EncryptionMethod.X509_CERTIFICATE;
			}
		});
		
		Button browseX509CertificateButton = new Button(encryptionChoicePanel, SWT.NONE);
		browseX509CertificateButton.setText(Messages.EncryptionDialog_5);
		browseX509CertificateButton.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog dialog = new FileDialog(Display.getCurrent().getActiveShell());
				dialog.setFilterExtensions(new String[]{ "*.pem",}); //$NON-NLS-1$
				String certificatePath = dialog.open();
				if(certificatePath != null) {
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
                if(passwordField.getText()!=null) {
                    enteredPassword=passwordField.getText().toCharArray(); 
                } else {
                    enteredPassword="".toCharArray(); //$NON-NLS-1$
                }
            }
        });

		encryptionChoicePanel.pack();
		composite.pack();
		return composite;
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
		return enteredPassword;
	}
	
	/**
	 * @return the X.509 certificate file to use for encryption selected by the user
	 */
	public File getSelectedX509CertificateFile() {
		return selectedX509CertificateFile;
	}
}
