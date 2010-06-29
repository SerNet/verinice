package sernet.verinice.report.service.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;
import org.eclipse.birt.report.engine.api.IRunAndRenderTask;

import sernet.verinice.model.iso27k.ControlGroup;
import sernet.verinice.model.samt.SamtTopic;
import sernet.verinice.oda.driver.impl.IImageProvider;
import sernet.verinice.report.service.Activator;
import sernet.verinice.report.service.ServiceComponent;
import sernet.verinice.samt.service.FindSamtGroup;
import sernet.verinice.samt.service.LoadAllSamtTopics;

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
		return new IOutputFormat[] { new PDFOutputFormat(), new HTMLOutputFormat() };
	}

	public void createReport(IReportOptions reportOptions) {
		prepareReport();
		
		BIRTReportService brs = new BIRTReportService();
		
		URL reportDesign = SamtReportType.class.getResource("samt-report.rptdesign");
		
		IRunAndRenderTask task = brs.createTask(reportDesign);
		
		brs.render(task, reportOptions);
	}
	
	private void prepareReport()
	{
		ControlGroup samtGroup = getSamtGroup();
		
		/*
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
				*/
		
		List<SamtTopic> samtTopics = getAllSamtTopics(samtGroup);

		Map<String, Object> variables = new HashMap<String, Object>();
		variables.put("companyName", samtGroup.getParent().getTitle());
		variables.put("date", new Date());
		variables.put("totalSecurityFigure", 23);
		variables.put("samtTopics", samtTopics);
		
		ServiceComponent.getDefault().getOdaDriver().setScriptVariables(variables);
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
	
	private List<SamtTopic> getAllSamtTopics(ControlGroup cg)
	{
		LoadAllSamtTopics command = new LoadAllSamtTopics(cg);
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
		return command.getAllSamtTopics();
	}


}
