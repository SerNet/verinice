package sernet.verinice.samt.rcp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.actions.ActionDelegate;
import org.eclipse.ui.commands.ICommandService;
import org.jfree.chart.JFreeChart;
import org.jfree.util.Log;

import sernet.gs.ui.rcp.main.bsi.views.chart.MaturitySpiderChart;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.verinice.model.iso27k.ControlGroup;
import sernet.verinice.model.samt.SamtTopic;
import sernet.verinice.oda.driver.impl.IImageProvider;
import sernet.verinice.report.service.impl.IOutputFormat;
import sernet.verinice.report.service.impl.IReportOptions;
import sernet.verinice.samt.service.FindSamtGroup;
import sernet.verinice.samt.service.LoadAllSamtTopics;

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
		// TODO:
		/*
		 * Show a dialog to chose - a report type - an output format - a file
		 * name - whether compression, encryption is desired - allow doing all
		 * this
		 */

		// TODO: Demo
		/*
		 * - show a dialog - hardcode report type - allow output format - allow
		 * file name - create the report
		 */

		if (dialog.open() == Dialog.OK) {
			IReportOptions ro = new IReportOptions() {
				public boolean isToBeEncrypted() { return false; }
				public boolean isToBeCompressed() { return false; }
				public IOutputFormat getOutputFormat() { return dialog.getOutputFormat(); } 
				public File getOutputFile() { return dialog.getOutputFile(); }
			};

			ServiceComponent.getDefault().getReportService().runSamtReportGeneration(ro);
		}
	}

}
