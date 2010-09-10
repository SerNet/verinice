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
import java.util.LinkedList;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.actions.ActionDelegate;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ServiceComponent;
import sernet.gs.ui.rcp.main.bsi.dialogs.EncryptionDialog;
import sernet.gs.ui.rcp.main.bsi.dialogs.EncryptionDialog.EncryptionMethod;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.taskcommands.ExportCommand;
import sernet.verinice.interfaces.encryption.EncryptionException;
import sernet.verinice.interfaces.encryption.IEncryptionService;
import sernet.verinice.iso27k.rcp.JobScheduler;
import sernet.verinice.iso27k.rcp.Mutex;
import sernet.verinice.iso27k.rcp.ExportDialog;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Organization;

/**
 * {@link Action} that exports assessment objects from the
 * database to an XML file at the selected path. This uses
 * {@link ExportDialog} to retrieve user selections.
 */
@SuppressWarnings("restriction")
public class ExportAction extends ActionDelegate implements IViewActionDelegate, IWorkbenchWindowActionDelegate
{
	public static final String ID = "sernet.verinice.samt.rcp.ExportSelfAssessment"; //$NON-NLS-1$
	
	private static final Logger LOG = Logger.getLogger(ExportAction.class);
	
	private EncryptionDialog encDialog;
	
	private char[] password = null;
	
	private File x509CertificateFile = null; 
	
	private static ISchedulingRule iSchedulingRule = new Mutex();
	
	Organization selectedOrganization;
	
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
	
	/*
	 * @see
	 * org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.
	 * ExecutionEvent)
	 */
    @Override
    public void run(IAction action) {
		final ExportDialog dialog = new ExportDialog(Display.getCurrent().getActiveShell(), selectedOrganization);
		if( dialog.open() == Dialog.OK )
		{	     
		    if(dialog.getEncryptOutput()) {
                encDialog = new EncryptionDialog(Display.getDefault().getActiveShell());
                if (encDialog.open() == Window.OK) {
                    EncryptionMethod encMethod = encDialog.getSelectedEncryptionMethod();            
                    if (encMethod == EncryptionMethod.PASSWORD) {
                        password = encDialog.getEnteredPassword();
                    } else if (encMethod == EncryptionMethod.X509_CERTIFICATE) {
                        x509CertificateFile = encDialog.getSelectedX509CertificateFile();
                    }
                }
            }
		    WorkspaceJob exportJob = new WorkspaceJob("Exporting...") {
                public IStatus runInWorkspace(final IProgressMonitor monitor) {
                    IStatus status = Status.OK_STATUS;
                    try {
                        monitor.beginTask(NLS.bind(Messages.getString("ExportAction_4"), new Object[] {dialog.getFilePath()}), IProgressMonitor.UNKNOWN); //$NON-NLS-1$
                        export(dialog.getSelectedElement(),dialog.getFilePath(),password,x509CertificateFile);                    
                    } catch (Exception e) {
                        LOG.error("Error while exporting data.", e); //$NON-NLS-1$
                        status= new Status(Status.ERROR, "sernet.verinice.samt.rcp", "Error while exporting data.",e); 
                    } finally {
                        password = null;
                        x509CertificateFile = null;
                        monitor.done();
                        this.done(status);
                    }
                    return status;
                }
            };
            exportJob.addJobChangeListener(new JobChangeListener(Display.getDefault().getActiveShell(),dialog.getFilePath(),dialog.getSelectedElement().getTitle()));
            JobScheduler.scheduleJob(exportJob,iSchedulingRule);
            
		}
	}

    private void export(CnATreeElement element, String path, char[] exportPassword, File x509CertificateFile) {
        LinkedList<CnATreeElement> exportElements = new LinkedList<CnATreeElement>();
        if(element!=null) {
            Activator.inheritVeriniceContextState();
        	exportElements.add(element);
        	ExportCommand exportCommand = new ExportCommand(exportElements, element.getUuid());
        	try
        	{
        		exportCommand = ServiceFactory.lookupCommandService().executeCommand(exportCommand);
        		FileUtils.writeByteArrayToFile(new File(path), encrypt(exportCommand.getResult(),exportPassword, x509CertificateFile));
        		//IOUtils.write(exportCommand.getResult(), getExportOutputStream( path ,exportPassword, x509CertificateFile ));
        	} catch (Exception e) {
        		throw new IllegalStateException(e);
        	}

        	String title = "";
        	if(element instanceof Organization) {
        	    title = ((Organization)element).getTitle();
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
    private byte[] encrypt(byte[] result, char[] password, File x509CertificateFile2) throws CertificateNotYetValidException, CertificateExpiredException, CertificateException, EncryptionException, IOException {
        if (password!=null || x509CertificateFile!=null) {               
            IEncryptionService service = ServiceComponent.getDefault().getEncryptionService();
            if (password!=null) {
                result = service.encrypt(result, password);
            } else if (x509CertificateFile!=null) {
                result = service.encrypt(result, x509CertificateFile);
            }                     
        }
        return result;
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
    @Override
    public void selectionChanged(IAction action, ISelection selection) {
      if(selection instanceof ITreeSelection) {
          ITreeSelection treeSelection = (ITreeSelection) selection;
          Object selectedElement = treeSelection.getFirstElement();
          if(selectedElement instanceof Organization) {
              selectedOrganization = (Organization) selectedElement;
          }
      }
    }
    
    class JobChangeListener implements IJobChangeListener {
        Shell shell; 
        String path;
        String title;
        public JobChangeListener(Shell shell, String path, String title) {
            super();
            this.shell = shell;
            this.path = path;
            this.title = title;
        }
  
        /* (non-Javadoc)
         * @see org.eclipse.core.runtime.jobs.IJobChangeListener#aboutToRun(org.eclipse.core.runtime.jobs.IJobChangeEvent)
         */
        @Override
        public void aboutToRun(IJobChangeEvent event) {}

        /* (non-Javadoc)
         * @see org.eclipse.core.runtime.jobs.IJobChangeListener#awake(org.eclipse.core.runtime.jobs.IJobChangeEvent)
         */
        @Override
        public void awake(IJobChangeEvent event) {}

        /* (non-Javadoc)
         * @see org.eclipse.core.runtime.jobs.IJobChangeListener#done(org.eclipse.core.runtime.jobs.IJobChangeEvent)
         */
        @Override
        public void done(IJobChangeEvent event) {
            shell.getDisplay().asyncExec(new Runnable() {          
                @Override
                public void run() {
                    MessageDialog.openInformation(shell, 
                            Messages.getString("ExportAction_2"), 
                            NLS.bind(Messages.getString("ExportAction_3"), new Object[] {title, path}));
                }
            });         
        }

        /* (non-Javadoc)
         * @see org.eclipse.core.runtime.jobs.IJobChangeListener#running(org.eclipse.core.runtime.jobs.IJobChangeEvent)
         */
        @Override
        public void running(IJobChangeEvent event) {}
        /* (non-Javadoc)
         * @see org.eclipse.core.runtime.jobs.IJobChangeListener#scheduled(org.eclipse.core.runtime.jobs.IJobChangeEvent)
         */
        @Override
        public void scheduled(IJobChangeEvent event) {}
        /* (non-Javadoc)
         * @see org.eclipse.core.runtime.jobs.IJobChangeListener#sleeping(org.eclipse.core.runtime.jobs.IJobChangeEvent)
         */
        @Override
        public void sleeping(IJobChangeEvent event) {}
     
    }

}
