package sernet.verinice.report.service.support;

import java.net.URL;

import org.eclipse.birt.report.engine.api.IRunAndRenderTask;

import sernet.verinice.report.service.impl.BIRTReportService;
import sernet.verinice.report.service.impl.IOutputFormat;
import sernet.verinice.report.service.impl.IReportOptions;
import sernet.verinice.report.service.impl.IReportType;
import sernet.verinice.report.service.output.PDFOutputFormat;

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
