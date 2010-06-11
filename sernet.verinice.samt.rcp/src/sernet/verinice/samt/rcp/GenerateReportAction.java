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
import org.jfree.chart.JFreeChart;
import org.jfree.util.Log;

import sernet.gs.ui.rcp.main.bsi.views.chart.MaturitySpiderChart;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.service.ICommandService;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadChildrenForExpansion;
import sernet.verinice.iso27k.model.ControlGroup;
import sernet.verinice.oda.driver.impl.IImageProvider;
import sernet.verinice.report.service.impl.IOutputFormat;
import sernet.verinice.report.service.impl.IReportOptions;
import sernet.verinice.samt.model.SamtTopic;
import sernet.verinice.samt.service.FindSamtGroup;
import sernet.verinice.samt.service.LoadAllSamtTopics;

public class GenerateReportAction extends ActionDelegate implements
		IWorkbenchWindowActionDelegate {

	private static final Logger LOG = Logger
			.getLogger(GenerateReportAction.class);

	private ICommandService commandService;

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
			ControlGroup samtGroup = getSamtGroup();
			JFreeChart chart = new MaturitySpiderChart().createChart(samtGroup);
			final ByteArrayOutputStream bos = new ByteArrayOutputStream();
			try {
				ImageIO.write(chart.createBufferedImage(750, 750), "png", bos);
			} catch (IOException e1) {
				Log.warn("Unable to generate spider chart for report");
			}

			Activator.getDefault().getOdaDriver().setImageProvider(
					"spider-graph", new IImageProvider() {

						@Override
						public InputStream newInputStream() {
							return new ByteArrayInputStream(bos.toByteArray());
						}

					});
			
			List<SamtTopic> samtTopics = getAllSamtTopics(samtGroup);

			Map<String, Object> variables = new HashMap<String, Object>();
			variables.put("companyName", samtGroup.getParent().getTitle());
			variables.put("date", new Date());
			variables.put("totalSecurityFigure", 23);
			variables.put("samtTopics", samtTopics);
			
			IReportOptions ro = new IReportOptions() {
				public boolean isToBeEncrypted() { return false; }
				public boolean isToBeCompressed() { return false; }
				public IOutputFormat getOutputFormat() { return dialog.getOutputFormat(); } 
				public File getOutputFile() { return dialog.getOutputFile(); }
			};

			Activator.getDefault().getReportService().runSamtReportGeneration(
					variables,
					ro);
		}
	}

	public ICommandService getCommandService() {
		if (commandService == null) {
			commandService = ServiceFactory.lookupCommandService();
		}
		return commandService;
	}

	private ControlGroup getSamtGroup() {
		FindSamtGroup command = new FindSamtGroup(true);
		try {
			command = getCommandService().executeCommand(command);
		} catch (RuntimeException e) {
			LOG.error("Error while executing FindSamtGroup command", e); //$NON-NLS-1$
			throw e;
		} catch (Exception e) {
			final String message = "Error while executing FindSamtGroup command"; //$NON-NLS-1$
			LOG.error(message, e);
			throw new RuntimeException(message, e);
		}
		return command.getSelfAssessmentGroup();
	}
	
	private List<SamtTopic> getAllSamtTopics(ControlGroup cg)
	{
		LoadAllSamtTopics command = new LoadAllSamtTopics(cg);
		try {
			command = getCommandService().executeCommand(command);
		} catch (RuntimeException e) {
			LOG.error("Error while executing FindSamtGroup command", e); //$NON-NLS-1$
			throw e;
		} catch (Exception e) {
			final String message = "Error while executing FindSamtGroup command"; //$NON-NLS-1$
			LOG.error(message, e);
			throw new RuntimeException(message, e);
		}
		return command.getAllSamtTopics();
	}
}
