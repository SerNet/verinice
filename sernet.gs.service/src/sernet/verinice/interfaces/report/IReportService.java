package sernet.verinice.interfaces.report;

public interface IReportService {

	void runTestReportGeneration();

	void runSamtReportGeneration(IReportOptions reportOptions);
	
	IReportType[] getReportTypes();
}
