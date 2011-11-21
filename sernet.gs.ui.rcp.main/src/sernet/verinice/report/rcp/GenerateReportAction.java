package sernet.verinice.report.rcp;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionDelegate;

import sernet.gs.ui.rcp.main.Activator;
import sernet.hui.common.VeriniceContext;
import sernet.springclient.RightsServiceClient;
import sernet.verinice.interfaces.report.IOutputFormat;
import sernet.verinice.interfaces.report.IReportOptions;
import sernet.verinice.interfaces.report.IReportType;
import sernet.verinice.interfaces.IInternalServerStartListener;
import sernet.verinice.interfaces.InternalServerEvent;
import sernet.verinice.interfaces.RightEnabledUserInteraction;
import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.iso27k.rcp.ISMView;

@SuppressWarnings("restriction")
public class GenerateReportAction extends ActionDelegate implements IWorkbenchWindowActionDelegate, RightEnabledUserInteraction {

	private static final Logger LOG = Logger.getLogger(GenerateReportAction.class);

	Shell shell;
	
	private GenerateReportDialog dialog;

	@Override
	public void init(IWorkbenchWindow window) {
	    try {
	        shell = window.getShell();
	    } catch( Throwable t) {
            LOG.error("Error creating dialog", t); //$NON-NLS-1$
        }
	}
	
	@Override
	public void init(final IAction action){
	    if(Activator.getDefault().isStandalone()){
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
            action.setEnabled(checkRights());
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
	    try {
	        if(dialog==null) {
	            dialog = new GenerateReportDialog(shell, IReportType.USE_CASE_ID_GENERAL_REPORT);
	        }
            
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
                    public void setRootElements(Integer[] rootElements) { this.rootElmts = rootElements;					}
					
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
	    } catch( Throwable t) {
	        LOG.error("Error while generation report", t); //$NON-NLS-1$
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
        return ActionRightIDs.GENERATEORGREPORT;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.RightEnabledUserInteraction#setRightID(java.lang.String)
     */
    @Override
    public void setRightID(String rightID) {
        // DO nothing, no need for an implementation              
    }

}
