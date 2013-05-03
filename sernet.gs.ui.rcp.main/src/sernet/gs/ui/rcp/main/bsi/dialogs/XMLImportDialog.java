//Neu hinzugefÃ¼gt vom Projektteam: XML import

package sernet.gs.ui.rcp.main.bsi.dialogs;

import java.io.File;
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
import org.eclipse.jface.preference.IPreferenceStore;
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

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ServiceComponent;
import sernet.gs.ui.rcp.main.bsi.dialogs.EncryptionDialog.EncryptionMethod;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.encryption.IEncryptionService;
import sernet.verinice.interfaces.encryption.PasswordException;
import sernet.verinice.iso27k.rcp.JobScheduler;
import sernet.verinice.iso27k.rcp.Mutex;
import sernet.verinice.iso27k.rcp.action.ExportAction;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.rcp.SWTElementFactory;
import sernet.verinice.service.commands.SyncCommand;
import sernet.verinice.service.commands.SyncParameter;
import sernet.verinice.service.sync.VeriniceArchive;

/**
 * 
 * @author: Projektteam HFU
 */
public class XMLImportDialog extends Dialog {

    private static final Logger LOG = Logger.getLogger(XMLImportDialog.class);

    private static final String SYNC_REQUEST = "syncRequest>"; //$NON-NLS-1$
    
    private boolean insert;
    private boolean update;
    private boolean delete;
    private boolean integrate;

    private Text dataPathText;
    private boolean dataPathFlag;

    private File dataFile;

    private ISchedulingRule iSchedulingRule = new Mutex();

    private EncryptionMethod selectedEncryptionMethod = null;

    private File x509CertificateFile;
    private File privateKeyPemFile;
    private Text privateKeyPasswordField;
    private String privateKeyPassword = null;
    
    private Text passwordField;
    private String password = ""; //$NON-NLS-1$
    
    private Text certificatePathField;
    
    private boolean useDefaultFolder;
    private String defaultFolder;
    
    private Integer format = SyncParameter.EXPORT_FORMAT_DEFAULT;
    
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
            if(useDefaultFolder) {
                setDefaultFolder(dataFile.getAbsolutePath());
            }
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

    private void setDefaultFolder(String dataPath) {
        String currentPath = "";    
        try {
            if (dataPath != null && !dataPath.isEmpty()) {
                int lastSlash = dataPath.lastIndexOf(System.getProperty("file.separator"));
                if (lastSlash != -1) {
                    currentPath = dataPath.substring(0, lastSlash + 1);
                } else {
                    currentPath = dataPath.substring(0, lastSlash);
                }
            }
            Activator.getDefault().getPreferenceStore().setValue(PreferenceConstants.DEFAULT_FOLDER_IMPORT, currentPath);
        } catch (Exception exe) {
            LOG.error("Error while setting Preference Constants", exe);
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
        default:
            messageBody = Messages.XMLImportDialog_19;
            break;
        }

        MessageDialog messageDialog = new MessageDialog(this.getShell(), titel, null, messageBody, MessageDialog.ERROR, new String[] { Messages.XMLImportDialog_24 }, 1);
        messageDialog.open();
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        final int layoutNumColumns = 5;
        final int layoutVerticalSpacing = 15;
        final int separatorHorizontalSpan = 5;
        final int operationHorizontalSpan = 5;
        final int operationVerticalSpan = 3;
        final int pbeNumColumns = 3;
        final int cryptGroupHorizontalSpan = 5;
        final int passwordWidthHint = 280;
        final int certificateWidthHint = 280;
        final int dataGroupHorizontalSpan = 5;
        final int dataGroupNumColumns = 4;
        final int dataIntroHorizontalSpan = 4;
        final int dataPathHorizontalSpan = 3;
        final int privateKeyPathWidthHint = 280;
        final int privateKeyPasswordWidthHint = 280;
        
        initDefaultFolder();
        final Composite container = (Composite) super.createDialogArea(parent);

        GridLayout layout = new GridLayout();
        layout.numColumns = layoutNumColumns;
        layout.verticalSpacing = layoutVerticalSpacing;
        container.setLayout(layout);

        Label seperator = new Label(container, SWT.SEPARATOR | SWT.HORIZONTAL);
        seperator.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, separatorHorizontalSpan, 1));

        // Operations of database (update,insert,delete)

        Group operationGroup = new Group(container, SWT.NULL);
        operationGroup.setText(Messages.XMLImportDialog_6);
        operationGroup.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, operationHorizontalSpan, operationVerticalSpan));

        layout = new GridLayout();
        layout.numColumns = 2;
        operationGroup.setLayout(layout);

        Label operationIntro = new Label(operationGroup, SWT.LEFT);
        operationIntro.setText(Messages.XMLImportDialog_7);
        operationIntro.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 2, 1));

        insert = true;
        SelectionAdapter insertListener = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                insert = (e.getSource() instanceof Button) ? ((Button)(e.getSource())).getSelection() : insert;
            }
        };
        
        Button insertCheck = SWTElementFactory.generateCheckboxButton(operationGroup, Messages.XMLImportDialog_25, true, insertListener);
        insertCheck.setLayoutData(new GridData(GridData.BEGINNING, GridData.CENTER, false, false, 1, 1));
        
        Label insertText = new Label(operationGroup, SWT.LEFT);
        insertText.setText(Messages.XMLImportDialog_8);
        insertText.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 1, 1));

        update = true;
        SelectionAdapter updateCheckListener = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e){
                update = (e.getSource() instanceof Button) ? ((Button)(e.getSource())).getSelection() : update;
            }
        };
        Button updateCheck = SWTElementFactory.generateCheckboxButton(operationGroup, Messages.XMLImportDialog_26, true, updateCheckListener);
        updateCheck.setLayoutData(new GridData(GridData.BEGINNING, GridData.CENTER, false, false, 1, 1));

        Label updateText = new Label(operationGroup, SWT.LEFT);
        updateText.setText(Messages.XMLImportDialog_9);
        updateText.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 1, 1));

        integrate = false;
        SelectionAdapter integrateListener = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                integrate = (e.getSource() instanceof Button) ? ((Button)(e.getSource())).getSelection() : integrate ;
            }
        };
        Button integrateButton = SWTElementFactory.generateCheckboxButton(operationGroup, Messages.XMLImportDialog_31, false, integrateListener);
        integrateButton.setLayoutData(new GridData(GridData.BEGINNING, GridData.CENTER, false, false, 1, 1));


        Label integrateText = new Label(operationGroup, SWT.LEFT);
        integrateText.setText(Messages.XMLImportDialog_37);
        integrateText.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 1, 1));

        // decryption
        
        final Group cryptGroup = new Group(container, SWT.NULL);
        cryptGroup.setText(Messages.XMLImportDialog_15);
        cryptGroup.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, cryptGroupHorizontalSpan, 1));       
        GridLayout pbeLayout = new GridLayout(pbeNumColumns, false);
        cryptGroup.setLayout(pbeLayout);
        
        // by default, no encryption is selected
        final Button useNoEncryptionRadio = SWTElementFactory.generateRadioButton(cryptGroup, Messages.XMLImportDialog_36, true, null);

        // insert two placeholder
        new Label(cryptGroup, SWT.NONE);
        new Label(cryptGroup, SWT.NONE);

        // ==== Password Based Encryption controls
        // by default, no encryption is selected
        final Button passwordEncryptionRadio = SWTElementFactory.generateRadioButton(cryptGroup, Messages.XMLImportDialog_16, false, null);
        
        passwordField = new Text(cryptGroup, SWT.PASSWORD | SWT.BORDER);
        GridData data = new GridData();
        data.widthHint = passwordWidthHint;
        passwordField.setLayoutData(data); 
        // FocusListener is added to passwordField afterwards
        new Label(cryptGroup, SWT.NONE);
        
        SelectionAdapter certificateEncryptionAdapter = new SelectionAdapter(){
            @Override
            public void widgetSelected(SelectionEvent e) {
                if(e.getSource() instanceof Button){
                    selectedEncryptionMethod = EncryptionMethod.X509_CERTIFICATE;
                    ((Button)e.getSource()).setSelection(true);
                    passwordEncryptionRadio.setSelection(false);
                    useNoEncryptionRadio.setSelection(false);
                }
            }
        };
            
        
        // ==== Certificate Based Encryption controls
        // by default, no encryption is selected
        final Button certificateEncryptionRadio = SWTElementFactory.generateRadioButton(cryptGroup, Messages.XMLImportDialog_28, false, certificateEncryptionAdapter);
        
        passwordField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                passwordEncryptionRadio.setSelection(true);
                certificateEncryptionRadio.setSelection(false);
                useNoEncryptionRadio.setSelection(false);
                selectedEncryptionMethod = EncryptionMethod.PASSWORD;
            }
            @Override
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
        data.widthHint = certificateWidthHint;
        certificatePathField.setLayoutData(data);
        certificatePathField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                selectedEncryptionMethod = EncryptionMethod.X509_CERTIFICATE;
                certificateEncryptionRadio.setSelection(true);
                passwordEncryptionRadio.setSelection(false);
                useNoEncryptionRadio.setSelection(false);
            }
        });
        
        SelectionAdapter browseX509CertificateListener = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                selectedEncryptionMethod = EncryptionMethod.X509_CERTIFICATE;
                certificateEncryptionRadio.setSelection(true);
                passwordEncryptionRadio.setSelection(false);
                useNoEncryptionRadio.setSelection(false);
                FileDialog dialog = new FileDialog(Display.getCurrent().getActiveShell());
                dialog.setFilterExtensions(new String[]{ "*.pem",}); //$NON-NLS-1$
                String certificatePath = dialog.open();
                if(certificatePath != null) {
                    x509CertificateFile = new File(certificatePath);
                    certificatePathField.setText(certificatePath);
                } else {
                    certificatePathField.setText(""); //$NON-NLS-1$
                }             
            }
        };
        
        SWTElementFactory.generateButton(cryptGroup, Messages.XMLImportDialog_29, null, browseX509CertificateListener);

        useNoEncryptionRadio.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e){
                selectedEncryptionMethod = null;
                useNoEncryptionRadio.setSelection(true);
                passwordEncryptionRadio.setSelection(false);
                certificateEncryptionRadio.setSelection(false);
            }
        });
        
        passwordEncryptionRadio.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                 selectedEncryptionMethod = EncryptionMethod.PASSWORD;
                 passwordEncryptionRadio.setSelection(true);
                 certificateEncryptionRadio.setSelection(false);
                 useNoEncryptionRadio.setSelection(false);
            }
        });
        
        final Text privateKeyPathField = new Text(cryptGroup, SWT.SINGLE | SWT.BORDER );
        data = new GridData();
        data.widthHint = privateKeyPathWidthHint;
        data.horizontalSpan = 2;
        data.horizontalAlignment = SWT.RIGHT;
        privateKeyPathField.setLayoutData(data);
        privateKeyPathField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                selectedEncryptionMethod = EncryptionMethod.X509_CERTIFICATE;
                certificateEncryptionRadio.setSelection(true);
                passwordEncryptionRadio.setSelection(false);
                useNoEncryptionRadio.setSelection(false);
            }
        });
        
        SelectionAdapter browsePrivateKeyListener = new SelectionAdapter() {
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

        };
        
        SWTElementFactory.generateButton(cryptGroup, Messages.XMLImportDialog_32, null, browsePrivateKeyListener);
        
        Label privateKeyPasswordLabel = new Label(cryptGroup, SWT.NONE);
        data = new GridData();
        data.horizontalAlignment = SWT.RIGHT;
        privateKeyPasswordLabel.setLayoutData(data);
        privateKeyPasswordLabel.setText(Messages.XMLImportDialog_0);
        
        privateKeyPasswordField = new Text(cryptGroup, SWT.PASSWORD | SWT.BORDER);
        data = new GridData();
        data.widthHint = privateKeyPasswordWidthHint;
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
            @Override
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
        dataGroup.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, dataGroupHorizontalSpan, 1));
        layout = new GridLayout();
        layout.numColumns = dataGroupNumColumns;
        layout.makeColumnsEqualWidth = true;
        dataGroup.setLayout(layout);

        Label dataIntro1 = new Label(dataGroup, SWT.LEFT);
        dataIntro1.setText(Messages.XMLImportDialog_12);
        dataIntro1.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, dataIntroHorizontalSpan, 1));

        dataPathText = new Text(dataGroup, SWT.BORDER);
        dataPathText.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, false, false, dataPathHorizontalSpan, 1));
        dataPathText.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                dataFile = new File(dataPathText.getText());
                if (dataFile.exists()) {
                    dataPathFlag = true;
                } else {
                    dataPathFlag = false;
                }
            }
        });
        

        useDefaultFolder = true;
        SelectionAdapter useDefaultFolderListener = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                useDefaultFolder   = (e.getSource() instanceof Button) ? ((Button)(e.getSource())).getSelection() : useDefaultFolder;
            }
        };
        
        Button useDefaultFolderButton = SWTElementFactory.generateCheckboxButton(container, Messages.XMLImportDialog_38, true, useDefaultFolderListener);
        useDefaultFolderButton.setLayoutData(new GridData(GridData.FILL, SWT.RIGHT, true, false, 2, 1));
            
        
        SelectionAdapter dataBrowseListener = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
               displayFiles(container.getShell(), dataPathText, dataFile);
            }
        };
        
        final Button dataBrowse = SWTElementFactory.generatePushButton(dataGroup, Messages.XMLImportDialog_14, null, dataBrowseListener);
        dataBrowse.setLayoutData(new GridData(GridData.BEGINNING, GridData.CENTER, false, false, 1, 1));
        
        
        
        
        // prevent passwordtextfield from gaining focus automatically
        // which happens in osx client, and causes wrong default radio selection (bug 341)
        dataPathText.setFocus();
        return container;
    }

    private void displayFiles(Shell shell, Text pathText, File file) {
        File f = file;
        FileDialog dialog = new FileDialog(shell, SWT.NULL);
        dialog.setFilterExtensions(new String[] { 
                "*"+VeriniceArchive.EXTENSION_VERINICE_ARCHIVE, //$NON-NLS-1$
                "*"+ExportAction.EXTENSION_XML, //$NON-NLS-1$
                "*"+ExportAction.EXTENSION_PASSWORD_ENCRPTION, //$NON-NLS-1$
                "*"+ExportAction.EXTENSION_CERTIFICATE_ENCRPTION }); //$NON-NLS-1$
        dialog.setFilterNames(new String[] { 
                Messages.XMLImportDialog_30,
                Messages.XMLImportDialog_33,
                Messages.XMLImportDialog_34,
                Messages.XMLImportDialog_35 });

        if(isFilePath()) {
            dialog.setFilterPath(getOldFolderPath());
        } else {
            dialog.setFilterPath(defaultFolder);
        }
        String path = dialog.open();

        if (path != null) {
            f = new File(path);
            if(dialog.getFilterIndex()<2) {
                // set the format if an uncrypted file was selected
                format = dialog.getFilterIndex();
            }
            if (f.isFile()) {
                pathText.setText(f.getPath());
                pathText.setEditable(true);
            }

        }
    }
    
    boolean isFilePath() {
        return dataPathText!=null && dataPathText.getText()!=null && !dataPathText.getText().isEmpty();
    }

    private String getOldFolderPath() {
        return getFolderFromPath(dataPathText.getText());
    }
    
    private String getFolderFromPath(String path) {
        String returnPath = null;
        if(path!=null && path.indexOf(File.separatorChar)!=-1) {
            returnPath = path.substring(0, path.lastIndexOf(File.separatorChar)+1);
        }
        return returnPath;
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
                    fileData = trimContentSuffix(fileData);
                }
                // data is encrypted, guess format
                format = guessFormat(fileData);
            }         
            
            command = new SyncCommand(
                    new SyncParameter(insert, update, delete, integrate, format), 
                    fileData);    
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
        final Set<CnATreeElement> changedElement = command.getElementSet();
        updateModel(importRootObjectSet, changedElement);
        if(Activator.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.USE_AUTOMATIC_VALIDATION)){
            WorkspaceJob validationCreationJob = new WorkspaceJob(Messages.XMLImportDialog_4) {
                @Override
                public IStatus runInWorkspace(final IProgressMonitor monitor) {
                    IStatus status = Status.OK_STATUS;
                    try {                        
                        monitor.beginTask(NLS.bind(Messages.XMLImportDialog_5, new Object[] {dataFile.getName()}), IProgressMonitor.UNKNOWN);
                        createValidations(changedElement);
                    } catch (Exception e){
                        LOG.error("Exception while executing createValidationsJob", e);
                    } finally {
                        monitor.done();
                    }
                    return status;
                }
            };
            JobScheduler.scheduleJob(validationCreationJob, iSchedulingRule); 
        }
    }

    /**
     * Returns ExportCommand.EXPORT_FORMAT_XML_PURE
     * if fileData is a verinice XML document
     * if not ExportCommand.EXPORT_FORMAT_VERINICE_ARCHIV
     * is returned.
     *
     * @param fileData a verinice XML document or verinice archive
     * @return ExportCommand.EXPORT_FORMAT_XML_PURE or ExportCommand.EXPORT_FORMAT_VERINICE_ARCHIV 
     */
    private Integer guessFormat(byte[] fileData) {
        Integer result = SyncParameter.EXPORT_FORMAT_VERINICE_ARCHIV;
        if(fileData!=null) {
            String content = new String(fileData);
            content = content.trim();
            if(content.endsWith(SYNC_REQUEST)) {
                result = SyncParameter.EXPORT_FORMAT_XML_PURE;
            }
        }
        return result;
    }

    private byte[] trimContentSuffix(byte[] fileData) {
        byte[] fileDataClone = (fileData != null) ? fileData.clone() : null;
        if(fileDataClone!=null) {
            // fileData ends with lines
            //Content-Type: text/plain
            //Content-Transfer-Encoding: 7bit
            String content = new String(fileDataClone);                   
            content = content.trim();
            int n = content.lastIndexOf(SYNC_REQUEST);
            
            if(!content.endsWith(SYNC_REQUEST) && n!=-1) {
                if (LOG.isDebugEnabled()) {
                    // charset debugging
                    LOG.debug("Encoding: " + content.substring(n+SYNC_REQUEST.length())); //$NON-NLS-1$
                }
                content = content.substring(0, n+SYNC_REQUEST.length());
            }
            fileDataClone = content.getBytes();
        }
        return fileDataClone;
    }

    private void updateModel(Set<CnATreeElement> importRootObjectSet, Set<CnATreeElement> changedElement) {
        final int maxChangedElements = 9;
        if(changedElement!=null && changedElement.size()>maxChangedElements) {
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
                    CnAElementFactory.getModel(cnATreeElement).childChanged(cnATreeElement);
                    CnAElementFactory.getModel(cnATreeElement).databaseChildChanged(cnATreeElement);
                }
            }
        }
    }

    private void createValidations(Set<CnATreeElement> elmts){
        try{
            for(CnATreeElement elmt : elmts){
                ServiceFactory.lookupValidationService().createValidationByUuid(elmt.getUuid());
            }
            if(elmts.size() > 0){
                CnAElementFactory.getModel(((CnATreeElement)elmts.toArray()[0])).validationAdded(((CnATreeElement)elmts.toArray()[0]).getScopeId());
            }
        } catch (CommandException e){
            LOG.error("Error while executing validation creation command", e);
        }
    }
    private String initDefaultFolder(){
        IPreferenceStore prefs = Activator.getDefault().getPreferenceStore();
        defaultFolder = prefs.getString(PreferenceConstants.DEFAULT_FOLDER_IMPORT);
        if(defaultFolder != null && !defaultFolder.isEmpty() && !defaultFolder.endsWith(System.getProperty("file.separator"))){
            defaultFolder=defaultFolder+System.getProperty("file.separator"); 
        }
        if(defaultFolder==null || defaultFolder.isEmpty()) {
            defaultFolder = System.getProperty("user.home");
        }
        return defaultFolder; 
    }
    
    
    public boolean getUseDefaultFolder(){
        return useDefaultFolder;
    }
}