package sernet.verinice.report.rcp;

import java.io.File;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.actions.ActionDelegate;

import sernet.verinice.interfaces.report.IOutputFormat;
import sernet.verinice.interfaces.report.IReportOptions;

@SuppressWarnings("restriction")
public class GenerateReportAction extends ActionDelegate implements IWorkbenchWindowActionDelegate {

	private static final Logger LOG = Logger.getLogger(GenerateReportAction.class);

	Shell shell;
	
	private GenerateReportDialog dialog;

	@Override
	public void init(IWorkbenchWindow window) {
	    try {
	        shell = window.getShell();
	    } catch( Throwable t) {
            LOG.error("Error creating dialog", t);
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
	            dialog = new GenerateReportDialog(shell);
	        }
    		if (dialog.open() == Dialog.OK) {
    			IReportOptions ro = new IReportOptions() {
    			    Integer rootElmt; 
    				public boolean isToBeEncrypted() { return false; }
    				public boolean isToBeCompressed() { return false; }
    				public IOutputFormat getOutputFormat() { return dialog.getOutputFormat(); } 
    				public File getOutputFile() { return dialog.getOutputFile(); }
                    public void setRootElement(Integer rootElement) { rootElmt = rootElement; }
                    public Integer getRootElement() {return rootElmt; }
    			};
    			ro.setRootElement(dialog.getRootElement());
    
    			dialog.getReportType().createReport(ro);
    		}
	    } catch( Throwable t) {
	        LOG.error("Error while generation report", t);
	    }
	}

}
