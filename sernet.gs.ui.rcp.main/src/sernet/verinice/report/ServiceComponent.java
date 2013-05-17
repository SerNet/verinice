package sernet.verinice.report;

import sernet.verinice.interfaces.report.IReportService;

public class ServiceComponent {

    private IReportService reportService;
    
    private static ServiceComponent INSTANCE;
    
    /**
     * The constructor
     */
    public ServiceComponent() {
    	INSTANCE = this; 
    }
    
    public static ServiceComponent getDefault()
    {
    	return INSTANCE;
    }

    public IReportService getReportService()
    {
    	return reportService;
    }
    
	public void bindReportService(IReportService reportService) {
		this.reportService = reportService;
	}
    
    public void unbindReportService(IReportService reportService) {
    	if (this.reportService == reportService)
    		this.reportService = null;
    }
    
}
