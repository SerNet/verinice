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
		return "samt";
	}

	public String getLabel() {
		return "Information Security Assessment report";
	}

	public IOutputFormat[] getOutputFormats() {
		return new IOutputFormat[] { new PDFOutputFormat(), new HTMLOutputFormat(), new CSVOutputFormat() };
	}

	public void createReport(IReportOptions reportOptions) {
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

}
