package sernet.verinice.report.service.support;

import java.net.URL;

import org.eclipse.birt.report.engine.api.IRunAndRenderTask;

import sernet.verinice.report.service.impl.BIRTReportService;
import sernet.verinice.report.service.impl.IOutputFormat;
import sernet.verinice.report.service.impl.IReportOptions;
import sernet.verinice.report.service.impl.IReportType;
import sernet.verinice.report.service.output.PDFOutputFormat;

public class SamtReportType implements IReportType {

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
		return new IOutputFormat[] { new PDFOutputFormat() };
	}

	public void createReport(IReportOptions reportOptions) {
		BIRTReportService brs = new BIRTReportService();
		
		URL reportDesign = SamtReportType.class.getResource("samt-report.rptdesign");
		
		IRunAndRenderTask task = brs.createTask(reportDesign);
		
		brs.render(task, reportOptions);
	}

}
