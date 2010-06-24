package sernet.verinice.report.service.impl;

import java.io.File;
import java.util.Map;

import sernet.verinice.oda.driver.impl.IVeriniceOdaDriver;
import sernet.verinice.report.service.Activator;
import sernet.verinice.report.service.output.HTMLOutputFormat;
import sernet.verinice.report.service.output.PDFOutputFormat;
import sernet.verinice.report.service.support.SamtReportType;
import sernet.verinice.report.service.support.TestReportType;

public class ReportService implements IReportService {

	@Override
	public void runTestReportGeneration() {
		IReportType rt = new TestReportType();
		
		IReportOptions ro = new IReportOptions() {
			public boolean isToBeEncrypted() { return false; }
			public boolean isToBeCompressed() { return false; }
			public IOutputFormat getOutputFormat() { return new PDFOutputFormat(); } 
			public File getOutputFile() { return new File("/tmp/test-report.pdf"); }
		};
		
		rt.createReport(ro);
	}

	@Override
	public void runSamtReportGeneration(IReportOptions reportOptions) {
		IReportType rt = new SamtReportType();
		
		IVeriniceOdaDriver odaDriver = Activator.getDefault().getOdaDriver();
		
		rt.createReport(reportOptions);
	}

	@Override
	public IReportType[] getReportTypes() {
		return new IReportType[] { new SamtReportType() };
	}
	
}
