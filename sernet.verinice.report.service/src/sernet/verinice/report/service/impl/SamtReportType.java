package sernet.verinice.report.service.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;
import org.eclipse.birt.report.engine.api.IDataExtractionTask;
import org.eclipse.birt.report.engine.api.IRunAndRenderTask;
import org.jfree.chart.JFreeChart;

import sernet.gs.ui.rcp.main.bsi.views.chart.MaturitySpiderChart;
import sernet.verinice.interfaces.oda.IImageProvider;
import sernet.verinice.interfaces.report.IOutputFormat;
import sernet.verinice.interfaces.report.IReportOptions;
import sernet.verinice.interfaces.report.IReportType;
import sernet.verinice.model.iso27k.ControlGroup;
import sernet.verinice.report.service.Activator;
import sernet.verinice.samt.service.FindSamtGroup;

public class SamtReportType implements IReportType {
	
	private static final Logger LOG = Logger.getLogger(SamtReportType.class);

	public String getDescription() {
		return "An Information Security Assessment report.";
	}

	public String getId() {
		return "test";
	}

	public String getLabel() {
		return "Information Security Assessment report (demo)";
	}

	public IOutputFormat[] getOutputFormats() {
		return new IOutputFormat[] { new PDFOutputFormat(), new HTMLOutputFormat(), new CSVOutputFormat() };
	}

	public void createReport(IReportOptions reportOptions) {
		prepareReport();
		
		BIRTReportService brs = new BIRTReportService();
		
		URL reportDesign = SamtReportType.class.getResource("samt-report.rptdesign");
		
		if (((AbstractOutputFormat) reportOptions.getOutputFormat()).isRenderOutput())
		{
			IRunAndRenderTask task = brs.createTask(reportDesign);
			brs.render(task, reportOptions);
		}
		else
		{
			IDataExtractionTask task = brs.createExtractionTask(reportDesign);
			// In a SAMT report the 4th result set is the one that is of interest.
			brs.extract(task, reportOptions, 3);
		}
	}
	
	private void prepareReport()
	{
		ControlGroup samtGroup = getSamtGroup();
		
		JFreeChart chart = new MaturitySpiderChart().createChart(samtGroup);
		final ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			ImageIO.write(chart.createBufferedImage(750, 750), "png", bos);
		} catch (IOException e1) {
			LOG.warn("Unable to generate spider chart for report");
		}

		Activator.getDefault().getOdaDriver().setImageProvider(
				"spider-graph", new IImageProvider() {

					@Override
					public InputStream newInputStream() {
						return new ByteArrayInputStream(bos.toByteArray());
					}

				});
		
		Map<String, Object> variables = new HashMap<String, Object>();
		variables.put("date", new Date());
		variables.put("totalSecurityFigure", 23);
		variables.put("samtGroup", samtGroup);
		
		Activator.getDefault().getOdaDriver().setScriptVariables(variables);
	}

	private ControlGroup getSamtGroup() {
		FindSamtGroup command = new FindSamtGroup(true);
		try {
			command = Activator.getDefault().getCommandService().executeCommand(command);
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

}
