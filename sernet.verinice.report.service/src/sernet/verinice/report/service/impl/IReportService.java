package sernet.verinice.report.service.impl;

import java.util.Map;

public interface IReportService {

	void runTestReportGeneration();

	void runSamtReportGeneration(IReportOptions reportOptions);
	
	IReportType[] getReportTypes();
}
