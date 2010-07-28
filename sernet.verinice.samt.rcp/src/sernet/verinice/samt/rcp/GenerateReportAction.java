package sernet.verinice.samt.rcp;

import java.io.File;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.actions.ActionDelegate;

import sernet.verinice.interfaces.report.IOutputFormat;
import sernet.verinice.interfaces.report.IReportOptions;

public class GenerateReportAction extends ActionDelegate implements
		IWorkbenchWindowActionDelegate {

	private static final Logger LOG = Logger.getLogger(GenerateReportAction.class);

	private GenerateReportDialog dialog;

	@Override
	public void init(IWorkbenchWindow window) {
		// do nothing
		dialog = new GenerateReportDialog(window.getShell());
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
		if (dialog.open() == Dialog.OK) {
			IReportOptions ro = new IReportOptions() {
				public boolean isToBeEncrypted() { return false; }
				public boolean isToBeCompressed() { return false; }
				public IOutputFormat getOutputFormat() { return dialog.getOutputFormat(); } 
				public File getOutputFile() { return dialog.getOutputFile(); }
			};

			dialog.getReportType().createReport(ro);
		}
	}

}
