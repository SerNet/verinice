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
import org.eclipse.ui.actions.ActionDelegate;

import sernet.gs.ui.rcp.main.Activator;
import sernet.hui.common.VeriniceContext;
import sernet.springclient.RightsServiceClient;
import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.interfaces.IInternalServerStartListener;
import sernet.verinice.interfaces.InternalServerEvent;
import sernet.verinice.interfaces.RightEnabledUserInteraction;
import sernet.verinice.interfaces.report.IOutputFormat;
import sernet.verinice.interfaces.report.IReportOptions;
import sernet.verinice.interfaces.report.IReportType;

@SuppressWarnings("restriction")
public class GenerateReportAction extends ActionDelegate implements IWorkbenchWindowActionDelegate, RightEnabledUserInteraction {

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
	
	@Override
	public void init(final IAction action){
	    if(Activator.getDefault().isStandalone()  && !Activator.getDefault().getInternalServer().isRunning()){
        IInternalServerStartListener listener = new IInternalServerStartListener(){
            @Override
            public void statusChanged(InternalServerEvent e) {
                if(e.isStarted()){
                    action.setEnabled(checkRights());
                }
            }
            
        };
        Activator.getDefault().getInternalServer().addInternalServerStatusListener(listener);
        } else {
            try{
                Activator.inheritVeriniceContextState();
                action.setEnabled(checkRights());
            } catch (NullPointerException npe){
                action.setEnabled(false);
            }
        }
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.actions.ActionDelegate#run(org.eclipse.jface.action.IAction
	 * )
	 */
	@Override
	public void run(IAction action) {
	    if(!checkRights()){
	        return;
	    }
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
    			final IReportOptions ro = new IReportOptions() {
    			    Integer rootElmt; 
    			    Integer[] rootElmts;
    				public boolean isToBeEncrypted() { return false; }
    				public boolean isToBeCompressed() { return false; }
    				public IOutputFormat getOutputFormat() { return dialog.getOutputFormat(); } 
    				public File getOutputFile() { return dialog.getOutputFile(); }
                    public void setRootElement(Integer rootElement) { rootElmt = rootElement; }
                    public Integer getRootElement() {return rootElmt; }
                    public Integer[] getRootElements(){return rootElmts;}
                    public void setRootElements(Integer[] rootElements) { this.rootElmts = rootElements;}
                    public Boolean useReportCache(){return dialog.getUseReportCache();}
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
        return ActionRightIDs.GENERATEORGREPORT;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.RightEnabledUserInteraction#setRightID(java.lang.String)
     */
    @Override
    public void setRightID(String rightID) {
        // DO nothing, no need for an implementation              
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
