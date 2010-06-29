package sernet.verinice.samt.rcp;

import sernet.verinice.interfaces.oda.IVeriniceOdaDriver;
import sernet.verinice.interfaces.report.IReportService;

public class ServiceComponent {

    private IReportService reportService;
    
    private IVeriniceOdaDriver odaDriver;

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
    
	public IVeriniceOdaDriver getOdaDriver()
    {
    	return odaDriver;
    }

	public void bindReportService(IReportService reportService) {
		this.reportService = reportService;
	}
    
    public void unbindReportService(IReportService reportService) {
    	if (this.reportService == reportService)
    		this.reportService = null;
    }
    
	public void bindOdaDriver(IVeriniceOdaDriver odaDriver) {
		this.odaDriver = odaDriver;
	}
    
    public void unbindOdaDriver(IVeriniceOdaDriver odaDriver) {
    	if (this.odaDriver == odaDriver)
    		this.odaDriver = null;
    }

}
