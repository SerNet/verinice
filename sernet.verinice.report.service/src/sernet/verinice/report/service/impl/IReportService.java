package sernet.verinice.report.service.impl;

public interface IReportService {

	void runTestReportGeneration();

	void runSamtReportGeneration(IReportOptions reportOptions);
	
	IReportType[] getReportTypes();
}
