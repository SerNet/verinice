package sernet.verinice.report.service.impl;

import org.apache.log4j.Logger;

import sernet.verinice.interfaces.report.IReportService;
import sernet.verinice.interfaces.report.IReportType;


public class ReportService implements IReportService {
	
	private static final Logger LOG = Logger.getLogger(ReportService.class);
	
	private IReportType[] reportTypes;

	@Override
	public IReportType[] getReportTypes() {
		if (reportTypes == null)
			reportTypes = new IReportType[] { new SamtReportType(), new ComprehensiveSamtReportType() };
		
		return reportTypes;
	}
	
}
