package sernet.verinice.report.service.impl;

import java.net.URL;

import org.apache.log4j.Logger;
import org.eclipse.birt.report.engine.api.IDataExtractionTask;
import org.eclipse.birt.report.engine.api.IRunAndRenderTask;

import sernet.verinice.interfaces.report.IOutputFormat;
import sernet.verinice.interfaces.report.IReportOptions;
import sernet.verinice.interfaces.report.IReportType;

public class ComprehensiveSamtReportType implements IReportType {
	
	private static final Logger LOG = Logger.getLogger(ComprehensiveSamtReportType.class);

	public String getDescription() {
		return "A comprehensive Information Security Assessment report.";
	}

	public String getId() {
		return "csamt";
	}

	public String getLabel() {
		return "Comprehensive Information Security Assessment report";
	}

	public IOutputFormat[] getOutputFormats() {
		return new IOutputFormat[] { new PDFOutputFormat(), new HTMLOutputFormat() };
	}

	public void createReport(IReportOptions reportOptions) {
		BIRTReportService brs = new BIRTReportService();
		
		URL reportDesign = ComprehensiveSamtReportType.class.getResource("comprehensive-samt-report.rptdesign");
		
		if (((AbstractOutputFormat) reportOptions.getOutputFormat()).isRenderOutput())
		{
			IRunAndRenderTask task = brs.createTask(reportDesign);
			brs.render(task, reportOptions);
		}
		else
		{
			IDataExtractionTask task = brs.createExtractionTask(reportDesign);
			// In a comprehensive SAMT report the 4th result set is the one that is of interest.
			brs.extract(task, reportOptions, 3);
		}
	}

}
