package sernet.verinice.report.rcp;

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

import sernet.gs.ui.rcp.main.Activator;
import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.interfaces.RightEnabledUserInteraction;
import sernet.verinice.interfaces.report.IOutputFormat;
import sernet.verinice.interfaces.report.IReportOptions;
import sernet.verinice.interfaces.report.IReportType;
import sernet.verinice.rcp.RightsEnabledActionDelegate;

public class GenerateReportAction extends RightsEnabledActionDelegate implements IWorkbenchWindowActionDelegate, RightEnabledUserInteraction {

    private static final Logger LOG = Logger.getLogger(GenerateReportAction.class);

    Shell shell;

    private GenerateReportDialog dialog;
    private List<Object> rootObjects;
    
    private boolean isContextMenuCall;

    @Override
    public void init(IWorkbenchWindow window) {
        try {
            shell = window.getShell();
        } catch(Exception t) {
            LOG.error("Error creating dialog", t); //$NON-NLS-1$
        }
    }

	/* (non-Javadoc)
	 * @see sernet.verinice.rcp.RightsEnabledActionDelegate#doRun(org.eclipse.jface.action.IAction)
	 */
	@Override
	public void doRun(IAction action) {    
	    try {
	        if(!isContextMenuCall() || rootObjects == null || rootObjects.size() == 0){
	            dialog = new GenerateReportDialog(shell);
	        }
	        else if(rootObjects.size() == 1 && isContextMenuCall()){
                dialog = new GenerateReportDialog(shell, rootObjects.get(0));
            } else if(rootObjects != null && rootObjects.size() > 1 && isContextMenuCall()){
                dialog = new GenerateReportDialog(shell, rootObjects, IReportType.USE_CASE_ID_GENERAL_REPORT);
            }
            dialog.setContextMenuCall(isContextMenuCall());
    		if (dialog.open() == Dialog.OK) {
    		    File f = dialog.getOutputFile();
    			final IReportOptions ro = new IReportOptions() {
    			    Integer rootElmt; 
    			    Integer[] rootElmts;
    				@Override
                    public boolean isToBeEncrypted() { return false; }
    				@Override
                    public boolean isToBeCompressed() { return false; }
    				@Override
                    public IOutputFormat getOutputFormat() { return dialog.getOutputFormat(); } 
    				@Override
                    public File getOutputFile() { return dialog.getOutputFile(); }
                    @Override
                    public void setRootElement(Integer rootElement) { rootElmt = rootElement; }
                    @Override
                    public Integer getRootElement() {return rootElmt; }
                    @Override
                    public Integer[] getRootElements(){return rootElmts;}
                    @Override
                    public void setRootElements(Integer[] rootElements) { this.rootElmts = rootElements;}
    			};
    			if(dialog.getRootElement() != null){
    				ro.setRootElement(dialog.getRootElement());
    			} else if (dialog.getRootElements() != null && dialog.getRootElements().length > 0){
    				ro.setRootElements(dialog.getRootElements());
    			}
    			
    			 PlatformUI.getWorkbench().getProgressService().busyCursorWhile(new IRunnableWithProgress() {
                    @Override
                    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                        monitor.beginTask(Messages.GenerateReportAction_1, IProgressMonitor.UNKNOWN);
                        Activator.inheritVeriniceContextState();
                        dialog.getReportMetaData();
                        IOutputFormat format = dialog.getOutputFormat();
                        dialog.getReportType().createReport(ro);
                        dialog.getReportType().createReport(dialog.getReportMetaData());
                        monitor.done();
                    }
    			 });
    			
    		}
	    } catch(Exception t) {
	        LOG.error("Error while generation report", t); //$NON-NLS-1$
	    }
	}

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.RightEnabledUserInteraction#getRightID()
     */
    @Override
    public String getRightID() {
        return ActionRightIDs.GENERATEORGREPORT;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.actions.ActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
     */
    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        if(selection instanceof ITreeSelection) {
            ITreeSelection treeSelection = (ITreeSelection) selection;
            rootObjects = treeSelection.toList();
        }
    }

    public boolean isContextMenuCall() {
        return isContextMenuCall;
    }

}
