package sernet.gs.ui.rcp.main.actions;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionDelegate;

import sernet.gs.ui.rcp.main.Activator;
import sernet.hui.common.VeriniceContext;
import sernet.springclient.RightsServiceClient;
import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.interfaces.RightEnabledUserInteraction;
import sernet.verinice.interfaces.report.IOutputFormat;
import sernet.verinice.interfaces.report.IReportOptions;
import sernet.verinice.interfaces.report.IReportType;
import sernet.verinice.report.rcp.GenerateReportDialog;
import sernet.verinice.report.rcp.Messages;

public class AuditReportAction extends ActionDelegate implements IWorkbenchWindowActionDelegate, RightEnabledUserInteraction {
    private static final Logger LOG = Logger.getLogger(AuditReportAction.class);
    Shell shell;
    private GenerateReportDialog dialog;
	private List<Object> rootObjects;

    @Override
    public void init(IWorkbenchWindow window) {
        try {
            shell = window.getShell();
        } catch(Exception t) {
            LOG.error("Error creating dialog", t); //$NON-NLS-1$
        }
    }
    
    @Override
    public void run(IAction action) {
        try {
        	if(rootObjects.size() == 1){
        		dialog = new GenerateReportDialog(shell, rootObjects.get(0));
        	} else {
        		dialog = new GenerateReportDialog(shell, rootObjects, IReportType.USE_CASE_ID_AUDIT_REPORT);
        	}
        	dialog.setContextMenuCall(true);
            if (dialog.open() == Dialog.OK) {
                final IReportOptions ro = new IReportOptions() {
                    Integer rootElmt;
                	Integer[] rootElmts; 
                    public boolean isToBeEncrypted() { return false; }
                    public boolean isToBeCompressed() { return false; }
                    public IOutputFormat getOutputFormat() { return dialog.getOutputFormat(); } 
                    public File getOutputFile() { return dialog.getOutputFile(); }
                    public void setRootElement(Integer rootElement) { rootElmt = rootElement; }
                    public void setRootElements(Integer[] rootElements){ rootElmts = rootElements;}
                    public Integer getRootElement() {return rootElmt; }
                    public Integer[] getRootElements() {return rootElmts;}
                    public Boolean useReportCache() {return dialog.getUseReportCache();}
                };
                if(dialog.getRootElement() != null){
                	ro.setRootElement(dialog.getRootElement());
                } else if (dialog.getRootElements() != null && dialog.getRootElements().length > 0){
                	ro.setRootElements(dialog.getRootElements());
                }
                
                 PlatformUI.getWorkbench().getProgressService().busyCursorWhile(new IRunnableWithProgress() {
                    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                        monitor.beginTask(Messages.GenerateReportAction_1, IProgressMonitor.UNKNOWN);
                        Activator.inheritVeriniceContextState();
                        dialog.getReportType().createReport(ro);
                        monitor.done();
                    }
                 });
                
            }
        } catch(Exception t) {
            LOG.error("Error while generation report", t); //$NON-NLS-1$
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.actions.ActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
     */
    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        action.setEnabled(checkRights());
        if(selection instanceof ITreeSelection) {
            ITreeSelection treeSelection = (ITreeSelection) selection;
            rootObjects = treeSelection.toList();
        }
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.RightEnabledUserInteraction#checkRights()
     */
    @Override
    public boolean checkRights() {
        RightsServiceClient service = (RightsServiceClient)VeriniceContext.get(VeriniceContext.RIGHTS_SERVICE);
        return service.isEnabled(getRightID());
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.RightEnabledUserInteraction#getRightID()
     */
    @Override
    public String getRightID() {
        return ActionRightIDs.GENERATEAUDITREPORT;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.RightEnabledUserInteraction#setRightID(java.lang.String)
     */
    @Override
    public void setRightID(String rightID) {
        // DO nothing
    }

  
  

}


