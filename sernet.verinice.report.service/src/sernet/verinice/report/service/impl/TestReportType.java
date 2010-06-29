package sernet.verinice.report.service.impl;

import java.net.URL;

import org.eclipse.birt.report.engine.api.IRunAndRenderTask;

import sernet.verinice.interfaces.report.IOutputFormat;
import sernet.verinice.interfaces.report.IReportOptions;
import sernet.verinice.interfaces.report.IReportType;


public class TestReportType implements IReportType {

	public String getDescription() {
		return "Report for testing purposed. It is of hardly any use.";
	}

	public String getId() {
		return "test";
	}

	public String getLabel() {
		return "Test report";
	}

	public IOutputFormat[] getOutputFormats() {
		return new IOutputFormat[] { new PDFOutputFormat() };
	}

	public void createReport(IReportOptions reportOptions) {
		BIRTReportService brs = new BIRTReportService();
		
		URL reportDesign = TestReportType.class.getResource("test-report.rptdesign");
		
		IRunAndRenderTask task = brs.createTask(reportDesign);
		
		brs.render(task, reportOptions);
	}

}
