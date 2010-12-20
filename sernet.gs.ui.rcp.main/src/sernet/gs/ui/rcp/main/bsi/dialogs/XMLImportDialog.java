//Neu hinzugefÃ¼gt vom Projektteam: XML import

package sernet.gs.ui.rcp.main.bsi.dialogs;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
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
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import sernet.gs.service.VeriniceCharset;
import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ServiceComponent;
import sernet.gs.ui.rcp.main.bsi.dialogs.EncryptionDialog.EncryptionMethod;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.sync.commands.SyncCommand;
import sernet.verinice.interfaces.encryption.IEncryptionService;
import sernet.verinice.interfaces.encryption.PasswordException;
import sernet.verinice.iso27k.rcp.JobScheduler;
import sernet.verinice.iso27k.rcp.Mutex;
import sernet.verinice.iso27k.rcp.action.ExportAction;
import sernet.verinice.model.common.CnATreeElement;

/**
 * 
 * @author: Projektteam HFU
 */
public class XMLImportDialog extends Dialog {

    private static final Logger LOG = Logger.getLogger(XMLImportDialog.class);

    private boolean insert;
    private boolean update;
    private boolean delete;

    private Text dataPathText;
    private boolean dataPathFlag;

    private File dataFile;

    private static ISchedulingRule iSchedulingRule = new Mutex();

    EncryptionMethod selectedEncryptionMethod = null;

    protected File x509CertificateFile;
    protected File privateKeyPemFile;
    private Text privateKeyPasswordField;
    private String privateKeyPassword = null;
    
    private Text passwordField;
    private String password = ""; //$NON-NLS-1$
    
    private Text certificatePathField;
    
    public XMLImportDialog(Shell shell) {
        super(shell);
    }

    @Override
    public void okPressed() {
        if (!dataPathFlag && (!insert && !update && !delete)) {
            createErrorMessage(3);
        } else if (!dataPathFlag) {
            createErrorMessage(1);
        } else if ((!insert && !update && !delete)) {
            createErrorMessage(2);
        } else {
            WorkspaceJob importJob = new WorkspaceJob(Messages.XMLImportDialog_4) {
                @Override
                public IStatus runInWorkspace(final IProgressMonitor monitor) {
                    IStatus status = Status.OK_STATUS;
                    try {                        
                        monitor.beginTask(NLS.bind(Messages.XMLImportDialog_5, new Object[] {dataFile.getName()}), IProgressMonitor.UNKNOWN);
                        doImport();
                    } catch (PasswordException e) {
                        status = new Status(IStatus.ERROR, "sernet.gs.ui.rcp.main", Messages.XMLImportDialog_13, e); //$NON-NLS-1$
                    } catch (Exception e) {
                        LOG.error("Error while importing data.", e); //$NON-NLS-1$
                        status = new Status(IStatus.ERROR, "sernet.gs.ui.rcp.main", Messages.XMLImportDialog_17, e); //$NON-NLS-1$
                    } finally {
                        monitor.done();
                    }
                    return status;
                }
            };
            JobScheduler.scheduleJob(importJob, iSchedulingRule);
            close();
        }
    }

    private void createErrorMessage(int caseNumber) {
        String titel = Messages.XMLImportDialog_18;
        String messageBody = Messages.XMLImportDialog_19;

        switch (caseNumber) {
        case 1:
            messageBody = Messages.XMLImportDialog_20;
            break;
        case 2:
            messageBody = Messages.XMLImportDialog_21;
            break;
        case 3:
            messageBody = Messages.XMLImportDialog_22;
            break;
        case 4:
            messageBody = Messages.XMLImportDialog_23;
            break;
        }

        MessageDialog messageDialog = new MessageDialog(this.getShell(), titel, null, messageBody, MessageDialog.ERROR, new String[] { Messages.XMLImportDialog_24 }, 1);
        messageDialog.open();
    }

    @Override
    protected Control createDialogArea(Composite parent) {

        final Composite container = (Composite) super.createDialogArea(parent);

        GridLayout layout = new GridLayout();
        layout.numColumns = 5;
        layout.verticalSpacing = 15;
        container.setLayout(layout);

        Label seperator = new Label(container, SWT.SEPARATOR | SWT.HORIZONTAL);
        seperator.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 5, 1));

        // Operations of database (update,insert,delete)

        Group operationGroup = new Group(container, SWT.NULL);
        operationGroup.setText(Messages.XMLImportDialog_6);
        operationGroup.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 5, 3));

        layout = new GridLayout();
        layout.numColumns = 2;
        operationGroup.setLayout(layout);

        Label operationIntro = new Label(operationGroup, SWT.LEFT);
        operationIntro.setText(Messages.XMLImportDialog_7);
        operationIntro.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 2, 1));

        final Button insertCheck = new Button(operationGroup, SWT.CHECK);
        insertCheck.setText(Messages.XMLImportDialog_25);
        insertCheck.setLayoutData(new GridData(GridData.BEGINNING, GridData.CENTER, false, false, 1, 1));
        insertCheck.setSelection(true);
        insert = true;
        insertCheck.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent arg0) {
                insert = insertCheck.getSelection();
            }
        });

        Label insertText = new Label(operationGroup, SWT.LEFT);
        insertText.setText(Messages.XMLImportDialog_8);
        insertText.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 1, 1));

        final Button updateCheck = new Button(operationGroup, SWT.CHECK);
        updateCheck.setText(Messages.XMLImportDialog_26);
        updateCheck.setLayoutData(new GridData(GridData.BEGINNING, GridData.CENTER, false, false, 1, 1));
        updateCheck.setSelection(true);
        update = true;
        updateCheck.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent arg0) {
                update = updateCheck.getSelection();
            }
        });

        Label updateText = new Label(operationGroup, SWT.LEFT);
        updateText.setText(Messages.XMLImportDialog_9);
        updateText.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 1, 1));

        /* 
        final Button deleteCheck = new Button(operationGroup, SWT.CHECK);
        deleteCheck.setText(Messages.XMLImportDialog_27);
        deleteCheck.setLayoutData(new GridData(GridData.BEGINNING, GridData.CENTER, false, false, 1, 1));
        deleteCheck.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent arg0) {
                delete = deleteCheck.getSelection();
            }
        });

        Label deleteText = new Label(operationGroup, SWT.LEFT);
        deleteText.setText(Messages.XMLImportDialog_10);
        deleteText.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 1, 1));
		*/

        // decryption
        
        final Group cryptGroup = new Group(container, SWT.NULL);
        cryptGroup.setText(Messages.XMLImportDialog_15);
        cryptGroup.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 5, 1));       
        GridLayout pbeLayout = new GridLayout(3, false);
        cryptGroup.setLayout(pbeLayout);
        

        // ==== Password Based Encryption controls
        final Button passwordEncryptionRadio = new Button(cryptGroup, SWT.RADIO);
        passwordEncryptionRadio.setText(Messages.XMLImportDialog_16);
        passwordEncryptionRadio.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if(EncryptionMethod.PASSWORD.equals(selectedEncryptionMethod) ) {
                    passwordEncryptionRadio.setSelection(false);
                    selectedEncryptionMethod = null;
                } else {
                    selectedEncryptionMethod = EncryptionMethod.PASSWORD;
                }
            }
        });
        
        passwordField = new Text(cryptGroup, SWT.PASSWORD | SWT.BORDER);
        GridData data = new GridData();
        data.widthHint = 280;
        passwordField.setLayoutData(data); 
        // FocusListener is added to passwordField afterwards
        new Label(cryptGroup, SWT.NONE);
        
        // ==== Certificate Based Encryption controls
        final Button certificateEncryptionRadio = new Button(cryptGroup, SWT.RADIO);
        certificateEncryptionRadio.setText(Messages.XMLImportDialog_28);
        certificateEncryptionRadio.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if(EncryptionMethod.X509_CERTIFICATE.equals(selectedEncryptionMethod) ) {
                    certificateEncryptionRadio.setSelection(false);
                    selectedEncryptionMethod = null;
                } else {
                    selectedEncryptionMethod = EncryptionMethod.X509_CERTIFICATE;
                }
            }
        });
        
        passwordField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                passwordEncryptionRadio.setSelection(true);
                certificateEncryptionRadio.setSelection(false);
                selectedEncryptionMethod = EncryptionMethod.PASSWORD;
            }
            public void focusLost(FocusEvent e) {
                
            }
        });
        passwordField.addModifyListener(new ModifyListener() {         
            @Override
            public void modifyText(ModifyEvent e) {
               password = passwordField.getText();
                
            }
        });
       
        certificatePathField = new Text(cryptGroup, SWT.SINGLE | SWT.BORDER);
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
        
        Button browseX509CertificateButton = new Button(cryptGroup, SWT.NONE);
        browseX509CertificateButton.setText(Messages.XMLImportDialog_29);
        browseX509CertificateButton.addSelectionListener(new SelectionAdapter() {         
            @Override
            public void widgetSelected(SelectionEvent e) {
                FileDialog dialog = new FileDialog(Display.getCurrent().getActiveShell());
                dialog.setFilterExtensions(new String[]{ "*.pem",}); //$NON-NLS-1$
                String certificatePath = dialog.open();
                if(certificatePath != null) {
                    x509CertificateFile = new File(certificatePath);
                    certificatePathField.setText(certificatePath);
                } else {
                    certificatePathField.setText(""); //$NON-NLS-1$
                }             
                passwordEncryptionRadio.setSelection(false);
                certificateEncryptionRadio.setSelection(true);
                selectedEncryptionMethod = EncryptionMethod.X509_CERTIFICATE;
            }
        });
        
        final Text privateKeyPathField = new Text(cryptGroup, SWT.SINGLE | SWT.BORDER );
        data = new GridData();
        data.widthHint = 280;
        data.horizontalSpan = 2;
        data.horizontalAlignment = SWT.RIGHT;
        privateKeyPathField.setLayoutData(data);
        privateKeyPathField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                passwordEncryptionRadio.setSelection(false);
                certificateEncryptionRadio.setSelection(true);
                selectedEncryptionMethod = EncryptionMethod.X509_CERTIFICATE;
            }
        });
        
        Button browsePrivateKeyButton = new Button(cryptGroup, SWT.NONE);
        browsePrivateKeyButton.setText(Messages.XMLImportDialog_32);
        browsePrivateKeyButton.addSelectionListener(new SelectionAdapter() {         
            @Override
            public void widgetSelected(SelectionEvent e) {
                FileDialog dialog = new FileDialog(Display.getCurrent().getActiveShell());
                dialog.setFilterExtensions(new String[]{ "*.pem",}); //$NON-NLS-1$
                String path = dialog.open();
                if(path != null) {
                    privateKeyPemFile = new File(path);
                    privateKeyPathField.setText(path);
                } else {
                    privateKeyPathField.setText(""); //$NON-NLS-1$
                }             
                passwordEncryptionRadio.setSelection(false);
                certificateEncryptionRadio.setSelection(true);
                selectedEncryptionMethod = EncryptionMethod.X509_CERTIFICATE;
            }
        });
        
        Label privateKeyPasswordLabel = new Label(cryptGroup, SWT.NONE);
        data = new GridData();
        data.horizontalAlignment = SWT.RIGHT;
        privateKeyPasswordLabel.setLayoutData(data);
        privateKeyPasswordLabel.setText(Messages.XMLImportDialog_0);
        
        privateKeyPasswordField = new Text(cryptGroup, SWT.PASSWORD | SWT.BORDER);
        data = new GridData();
        data.widthHint = 280;
        privateKeyPasswordField.setLayoutData(data); 
        // FocusListener is added to passwordField afterwards
        new Label(cryptGroup, SWT.NONE);
        privateKeyPasswordField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                certificateEncryptionRadio.setSelection(true);
                passwordEncryptionRadio.setSelection(false);
                selectedEncryptionMethod = EncryptionMethod.X509_CERTIFICATE;
            }
            public void focusLost(FocusEvent e) {
                
            }
        });
        privateKeyPasswordField.addModifyListener(new ModifyListener() {         
            @Override
            public void modifyText(ModifyEvent e) {
                privateKeyPassword = privateKeyPasswordField.getText();
                
            }
        });
        
        cryptGroup.pack();
        
        // set and save path to zip- archiv

        Group dataGroup = new Group(container, SWT.NULL);
        dataGroup.setText(Messages.XMLImportDialog_11);
        dataGroup.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 5, 1));
        layout = new GridLayout();
        layout.numColumns = 4;
        layout.makeColumnsEqualWidth = true;
        dataGroup.setLayout(layout);

        Label dataIntro1 = new Label(dataGroup, SWT.LEFT);
        dataIntro1.setText(Messages.XMLImportDialog_12);
        dataIntro1.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 4, 1));

        dataPathText = new Text(dataGroup, SWT.BORDER);
        dataPathText.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, false, false, 3, 1));
        dataPathText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                dataFile = new File(dataPathText.getText());
                if (dataFile.exists()) {
                    dataPathFlag = true;
                } else {
                    dataPathFlag = false;
                }
            }
        });

        final Button dataBrowse = new Button(dataGroup, SWT.PUSH);
        dataBrowse.setText(Messages.XMLImportDialog_14);
        dataBrowse.setLayoutData(new GridData(GridData.BEGINNING, GridData.CENTER, false, false, 1, 1));
        dataBrowse.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                displayFiles(container.getShell(), dataPathText, dataFile);
            }
        });

        return container;
    }

    private void displayFiles(Shell shell, Text pathText, File file) {
        FileDialog dialog = new FileDialog(shell, SWT.NULL);
        dialog.setFilterExtensions(new String[] { 
                "*"+ExportAction.EXTENSION_XML, //$NON-NLS-1$
                "*"+ExportAction.EXTENSION_PASSWORD_ENCRPTION, //$NON-NLS-1$
                "*"+ExportAction.EXTENSION_CERTIFICATE_ENCRPTION }); //$NON-NLS-1$
        dialog.setFilterNames(new String[] { 
                Messages.XMLImportDialog_33,
                Messages.XMLImportDialog_34,
                Messages.XMLImportDialog_35 });
        String path = dialog.open();

        if (path != null) {
            file = new File(path);

            if (file.isFile()) {
                pathText.setText(file.getPath());
                pathText.setEditable(true);
            }
        }
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(Messages.XMLImportDialog_1);
    }

    @Override
    protected boolean isResizable() {
        return true;
    }

    public boolean getInsertState() {
        return insert;
    }

    public boolean getUpdateState() {
        return update;
    }

    public boolean getDeleteState() {
        return delete;
    }

    private void doImport() {
        Activator.inheritVeriniceContextState();

        SyncCommand command;
        try {
            byte[] fileData =  FileUtils.readFileToByteArray(dataFile);
            if (selectedEncryptionMethod!=null) {           
                IEncryptionService service = ServiceComponent.getDefault().getEncryptionService();
                if (selectedEncryptionMethod == EncryptionMethod.PASSWORD) {
                    fileData = service.decrypt(fileData, password.toCharArray());
                } else if (selectedEncryptionMethod == EncryptionMethod.X509_CERTIFICATE) {             
                    fileData = service.decrypt(fileData, x509CertificateFile, privateKeyPemFile,privateKeyPassword);                  
                    if(fileData!=null) {
                        // fileData ends with lines
                        //Content-Type: text/plain
                        //Content-Transfer-Encoding: 7bit
                        String content = new String(fileData);                   
                        content = content.trim();
                        final String SYNC_REQUEST = "syncRequest>"; //$NON-NLS-1$
                        int n = content.lastIndexOf(SYNC_REQUEST);
                        
                        if(!content.endsWith(SYNC_REQUEST) && n!=-1) {
                            if (LOG.isDebugEnabled()) {
                                // charset debugging
                                LOG.debug("Encoding: " + content.substring(n+SYNC_REQUEST.length()));
                            }
                            content = content.substring(0, n+SYNC_REQUEST.length());
                        }
                        fileData = content.getBytes();
                    }
                }                       
            }
            command = new SyncCommand(insert, update, delete, fileData);    
            command = ServiceFactory.lookupCommandService().executeCommand(command);

        } catch (PasswordException  e) {
            LOG.warn("Wrong password while decrypting import file."); //$NON-NLS-1$
            if (LOG.isDebugEnabled()) {
                LOG.debug("Stacktrace: ", e); //$NON-NLS-1$
            }
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

        Set<CnATreeElement> importRootObjectSet = command.getImportRootObject();
        Set<CnATreeElement> changedElement = command.getElementSet();
        updateModel(importRootObjectSet, changedElement);
    }

    private void updateModel(Set<CnATreeElement> importRootObjectSet, Set<CnATreeElement> changedElement) {
        if(changedElement!=null && changedElement.size()>9) {
            // if more than 9 elements changed or added do a complete reload
            CnAElementFactory.getInstance().reloadModelFromDatabase();
        } else {
            if (importRootObjectSet != null && importRootObjectSet.size()>0) {     
            	for (CnATreeElement importRootObject : importRootObjectSet) {				
	                CnAElementFactory.getModel(importRootObject).childAdded(importRootObject.getParent(), importRootObject);
	                CnAElementFactory.getModel(importRootObject).databaseChildAdded(importRootObject);
	                if (changedElement != null) {
	                    for (CnATreeElement cnATreeElement : changedElement) {
	                        CnAElementFactory.getModel(cnATreeElement).childAdded(cnATreeElement.getParent(), cnATreeElement);
	                        CnAElementFactory.getModel(cnATreeElement).databaseChildAdded(cnATreeElement);
	                    }
	                }
            	}
            }    
            if (changedElement != null) {
                for (CnATreeElement cnATreeElement : changedElement) {
                    CnAElementFactory.getModel(cnATreeElement).childChanged(cnATreeElement.getParent(), cnATreeElement);
                    CnAElementFactory.getModel(cnATreeElement).databaseChildChanged(cnATreeElement);
                }
            }
        }
    }
}