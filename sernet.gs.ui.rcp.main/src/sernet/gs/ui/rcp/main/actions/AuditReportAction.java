package sernet.gs.ui.rcp.main.actions;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionDelegate;

import sernet.gs.ui.rcp.main.Activator;
import sernet.verinice.interfaces.report.IOutputFormat;
import sernet.verinice.interfaces.report.IReportOptions;
import sernet.verinice.model.iso27k.Audit;
import sernet.verinice.model.iso27k.Organization;
import sernet.verinice.report.rcp.GenerateReportDialog;
import sernet.verinice.report.rcp.Messages;

public class AuditReportAction extends ActionDelegate implements IWorkbenchWindowActionDelegate {
    private static final Logger LOG = Logger.getLogger(AuditReportAction.class);
    Shell shell;
    private GenerateReportDialog dialog;
    private Audit audit;

    @Override
    public void init(IWorkbenchWindow window) {
        try {
            shell = window.getShell();
        } catch( Throwable t) {
            LOG.error("Error creating dialog", t); //$NON-NLS-1$
        }
    }
    
    @Override
    public void run(IAction action) {
        try {
            dialog = new GenerateReportDialog(shell, audit);
            if (dialog.open() == Dialog.OK) {
                final IReportOptions ro = new IReportOptions() {
                    Integer rootElmt; 
                    public boolean isToBeEncrypted() { return false; }
                    public boolean isToBeCompressed() { return false; }
                    public IOutputFormat getOutputFormat() { return dialog.getOutputFormat(); } 
                    public File getOutputFile() { return dialog.getOutputFile(); }
                    public void setRootElement(Integer rootElement) { rootElmt = rootElement; }
                    public Integer getRootElement() {return rootElmt; }
                };
                ro.setRootElement(dialog.getRootElement());
                
                 PlatformUI.getWorkbench().getProgressService().busyCursorWhile(new IRunnableWithProgress() {
                    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                        monitor.beginTask(Messages.GenerateReportAction_1, IProgressMonitor.UNKNOWN);
                        Activator.inheritVeriniceContextState();
                        dialog.getReportType().createReport(ro);
                        monitor.done();
                    }
                 });
                
            }
        } catch( Throwable t) {
            LOG.error("Error while generation report", t); //$NON-NLS-1$
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.actions.ActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
     */
    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        if(selection instanceof ITreeSelection) {
            ITreeSelection treeSelection = (ITreeSelection) selection;
            Object selectedElement = treeSelection.getFirstElement();
            if(selectedElement instanceof Audit) {
                audit = (Audit) selectedElement;
            }
        }
    }

  
  

}


