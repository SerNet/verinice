/*******************************************************************************
 * Copyright (c) 2010 Andreas Becker <andreas[at]becker[dot]name>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details.
 *     You should have received a copy of the GNU General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Andreas Becker <andreas[at]becker[dot]name> - initial API and implementation
 ******************************************************************************/

package sernet.verinice.iso27k.rcp.action;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.actions.ActionDelegate;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ServiceComponent;
import sernet.gs.ui.rcp.main.bsi.dialogs.EncryptionDialog;
import sernet.gs.ui.rcp.main.bsi.dialogs.EncryptionDialog.EncryptionMethod;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.hui.common.VeriniceContext;
import sernet.springclient.RightsServiceClient;
import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.interfaces.IInternalServerStartListener;
import sernet.verinice.interfaces.InternalServerEvent;
import sernet.verinice.interfaces.RightEnabledUserInteraction;
import sernet.verinice.interfaces.encryption.EncryptionException;
import sernet.verinice.interfaces.encryption.IEncryptionService;
import sernet.verinice.iso27k.rcp.ExportDialog;
import sernet.verinice.iso27k.rcp.JobScheduler;
import sernet.verinice.iso27k.rcp.Mutex;
import sernet.verinice.model.bsi.ITVerbund;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Organization;
import sernet.verinice.service.commands.ExportCommand;

/**
 * {@link Action} that exports assessment objects from the
 * database to an XML file at the selected path. This uses
 * {@link ExportDialog} to retrieve user selections.
 */
@SuppressWarnings("restriction")
public class ExportAction extends ActionDelegate implements IViewActionDelegate, IWorkbenchWindowActionDelegate, RightEnabledUserInteraction
{
	public static final String ID = "sernet.verinice.samt.rcp.ExportSelfAssessment"; //$NON-NLS-1$
	
	private static final Logger LOG = Logger.getLogger(ExportAction.class);
	
	public static final String EXTENSION_XML = ".xml"; //$NON-NLS-1$
	
	public static final String EXTENSION_PASSWORD_ENCRPTION = ".pcr"; //$NON-NLS-1$
    
    public static final String EXTENSION_CERTIFICATE_ENCRPTION = ".ccr"; //$NON-NLS-1$
   
	private EncryptionDialog encDialog;
	
	private String filePath;
	
	private char[] password = null;
	
	private File x509CertificateFile = null;
	
	private String keyAlias = null;
	
	private static ISchedulingRule iSchedulingRule = new Mutex();
	
	private ITreeSelection selection;
	
	private boolean serverIsRunning = true;
	
	 /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
     */
    @Override
    public void init(IViewPart view) {
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
     */
    @Override
    public void init(IWorkbenchWindow window) {
        
    }
	
    @Override
    public void init(final IAction action){
        if(Activator.getDefault().isStandalone()  && !Activator.getDefault().getInternalServer().isRunning()){
            serverIsRunning = false;
            IInternalServerStartListener listener = new IInternalServerStartListener(){
                @Override
                public void statusChanged(InternalServerEvent e) {
                    if(e.isStarted()){
                        serverIsRunning = true;
                        action.setEnabled(checkRights());
                    }
                }

            };
            Activator.getDefault().getInternalServer().addInternalServerStatusListener(listener);
        } else {
            action.setEnabled(checkRights());
        }
    }
    
	/*
	 * @see
	 * org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.
	 * ExecutionEvent)
	 */
    @Override
    public void run(IAction action) {
		final ExportDialog dialog = new ExportDialog(Display.getCurrent().getActiveShell(), selection);
		if( dialog.open() == Dialog.OK )
		{	     
		    if(dialog.getEncryptOutput()) {
                encDialog = new EncryptionDialog(Display.getDefault().getActiveShell());
                if (encDialog.open() == Window.OK) {
                    EncryptionMethod encMethod = encDialog.getSelectedEncryptionMethod();
                    switch (encMethod) {
                    case PASSWORD:
                    	password = encDialog.getEnteredPassword();
                    	break;
                    case X509_CERTIFICATE:
                        x509CertificateFile = encDialog.getSelectedX509CertificateFile();
                        break;
                    case PKCS11_KEY:
                    	keyAlias = encDialog.getSelectedKeyAlias();
                    }
                } else {
                    return;
                }
            }
		    filePath = dialog.getFilePath();
		    filePath = ExportAction.addExtension(filePath,ExportDialog.getExtensionArray()[dialog.getFormat()]);
		    if(password!=null) {		        
		        filePath = addExtension(filePath, EXTENSION_PASSWORD_ENCRPTION);
		    }
		    if(x509CertificateFile!=null) {
		        filePath = addExtension(filePath, EXTENSION_CERTIFICATE_ENCRPTION);
            }
		    WorkspaceJob exportJob = new WorkspaceJob("Exporting...") {
                @Override
                public IStatus runInWorkspace(final IProgressMonitor monitor) {
                    IStatus status = Status.OK_STATUS;
                    try {
                        monitor.beginTask(NLS.bind(Messages.getString("ExportAction_4"), new Object[] {dialog.getFilePath()}), IProgressMonitor.UNKNOWN); //$NON-NLS-1$
                        export( dialog.getSelectedElementSet(),
                        		filePath,
                        		dialog.getReImport(),
                        		dialog.getSourceId(),
                        		password,
                        		x509CertificateFile,
                        		keyAlias,
                        		dialog.getFormat());                    
                    } catch (Exception e) {
                        LOG.error("Error while exporting data.", e); //$NON-NLS-1$
                        status= new Status(Status.ERROR, "sernet.verinice.samt.rcp", "Error while exporting data.",e); 
                    } finally {
                        password = null;
                        x509CertificateFile = null;
                        keyAlias = null;
                        monitor.done();
                        this.done(status);
                    }
                    return status;
                }
            };
            exportJob.addJobChangeListener(new ExportJobChangeListener(Display.getDefault().getActiveShell(),filePath,dialog.getSelectedElement().getTitle()));
            JobScheduler.scheduleJob(exportJob,iSchedulingRule);
            
		}
	}
    
    private void export(
            Set<CnATreeElement> elementSet, 
            String path, 
            boolean reImport, 
            String sourceId, 
            char[] exportPassword, 
            File x509CertificateFile,
            String keyAlias,
            int fileFormat) {
        String internalSourceId = null;
        final int uuidStringLength = 6;
        if(elementSet!=null && elementSet.size()>0) {
        	if(sourceId==null || sourceId.isEmpty()) {
        		// if source id is not set by user the first 6 char. of an uuid is used
        		internalSourceId = UUID.randomUUID().toString().substring(0, uuidStringLength);
        	} else {
        	    internalSourceId = sourceId;
        	}
            Activator.inheritVeriniceContextState();
            ExportCommand exportCommand = new ExportCommand(new LinkedList<CnATreeElement>(elementSet), internalSourceId, reImport, fileFormat);
        	try {
        		exportCommand = ServiceFactory.lookupCommandService().executeCommand(exportCommand);
        		FileUtils.writeByteArrayToFile(new File(path), encrypt(exportCommand.getResult(),exportPassword, keyAlias));
        		updateModel(exportCommand.getChangedElements());
        	} catch (Exception e) {
        		throw new IllegalStateException(e);
        	}      	
        }
    }
    
    private void updateModel(List<CnATreeElement> changedElementList) {
        final int maxChangedElements = 9;
        if(changedElementList!=null && !changedElementList.isEmpty() ) {
        	if(changedElementList.size()>maxChangedElements) {
	            // if more than 9 elements changed or added do a complete reload
	            CnAElementFactory.getInstance().reloadModelFromDatabase();
        	} else {
                for (CnATreeElement cnATreeElement : changedElementList) {
                    CnAElementFactory.getModel(cnATreeElement).childChanged(cnATreeElement);
                    CnAElementFactory.getModel(cnATreeElement).databaseChildChanged(cnATreeElement);
                }
            }
        }
    }
	

    /**
     * @param result
     * @param password2
     * @param x509CertificateFile2
     * @return
     * @throws IOException 
     * @throws EncryptionException 
     * @throws CertificateException 
     * @throws CertificateExpiredException 
     * @throws CertificateNotYetValidException 
     */
    private byte[] encrypt(byte[] result, char[] password, String keyAlias) throws CertificateException, EncryptionException, IOException {
        IEncryptionService service = ServiceComponent.getDefault().getEncryptionService();
        byte[] returnResult;
        if (keyAlias != null) {
        	returnResult = service.encrypt(result, keyAlias);
        } else if (password!=null) {
            returnResult = service.encrypt(result, password);
        } else if (x509CertificateFile!=null) {
            returnResult = service.encrypt(result, x509CertificateFile);
        } else {
            returnResult = result;
        }
        
        return returnResult;
    }

    public OutputStream getExportOutputStream(String path, char[] password, File x509CertificateFile) {
        OutputStream os;
        try {
            os = new FileOutputStream(path);     
            if (password!=null || x509CertificateFile!=null) {               
                IEncryptionService service = ServiceComponent.getDefault().getEncryptionService();
                if (password!=null) {
                    os = service.encrypt(os, password);
                } else if (x509CertificateFile!=null) {
                    os = service.encrypt(os, x509CertificateFile);
                }                     
            }
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        } 
        return os;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action
     * .IAction, org.eclipse.jface.viewers.ISelection)
     */
    @SuppressWarnings("unchecked")
    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        if (serverIsRunning) {
            action.setEnabled(checkRights());
        }
        if (selection instanceof ITreeSelection) {
            ITreeSelection treeSelection = (ITreeSelection) selection;
            Object selectedElement = treeSelection.getFirstElement();
            Iterator<Object> iter = treeSelection.iterator();
            while(iter.hasNext()){
                Object obj = iter.next();
                if(obj instanceof CnATreeElement) {
                    CnATreeElement elmt = (CnATreeElement) obj;
                    if(!(elmt instanceof ITVerbund) && !(elmt instanceof Organization) ){
                        if(this.selection != null){
                            this.selection = null;
                        }
                        return;
                    }
                }
            }
            if (selectedElement instanceof Organization || selectedElement instanceof ITVerbund) {
                this.selection = treeSelection;
            }
        }
    }
    
    public static String addExtension(String exportPath,String extension) {
        String returnedPath = null;
        if(exportPath!=null 
           && !exportPath.isEmpty()
           && !exportPath.endsWith(extension)) {
            returnedPath = exportPath + extension;
        } else {
            returnedPath = exportPath;
        }
        return returnedPath;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.RightEnabledUserInteraction#checkRights()
     */
    @Override
    public boolean checkRights() {
        Activator.inheritVeriniceContextState();
        RightsServiceClient service = (RightsServiceClient)VeriniceContext.get(VeriniceContext.RIGHTS_SERVICE);
        return service.isEnabled(getRightID());
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.RightEnabledUserInteraction#getRightID()
     */
    @Override
    public String getRightID() {
        return ActionRightIDs.XMLEXPORT;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.RightEnabledUserInteraction#setRightID(java.lang.String)
     */
    @Override
    public void setRightID(String rightID) {
        // DO NOTHING          
    }

}
