package sernet.verinice.report.service.impl;

import java.io.File;

import sernet.verinice.report.service.output.PDFOutputFormat;
import sernet.verinice.report.service.support.SamtReportType;
import sernet.verinice.report.service.support.TestReportType;

public class ReportService implements IReportService {
	
	public ReportService()
	{
		
	}

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
		
		rt.createReport(reportOptions);
	}

	@Override
	public IReportType[] getReportTypes() {
		return new IReportType[] { new SamtReportType() };
	}
	
}
