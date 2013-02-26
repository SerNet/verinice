package sernet.gs.ui.rcp.main;

import sernet.verinice.interfaces.encryption.IEncryptionService;
import sernet.verinice.interfaces.report.IReportService;

public class ServiceComponent {

    private IEncryptionService encryptionService;

    private IReportService reportService;
    
    private static ServiceComponent instance;
    
    /**
     * The constructor
     */
    public ServiceComponent() {
    	instance = this; 
    }
    
    public static ServiceComponent getDefault()
    {
    	return instance;
    }

    public IEncryptionService getEncryptionService()
    {
    	return encryptionService;
    }
    
    public void bindEncryptionService(IEncryptionService encryptionService)
    {
    	this.encryptionService = encryptionService;
    }
    
    public void unbindEncryptionService(IEncryptionService encryptionService)
    {
    	if (this.encryptionService == encryptionService){
    		this.encryptionService = null;
    	}
    }
    
    public IReportService getReportService()
    {
        return reportService;
    }
    
    public void bindReportService(IReportService reportService)
    {
        this.reportService = reportService;
    }
    
    public void unbindReportService(IReportService reportService)
    {
        if (this.reportService == reportService){
            this.reportService = null;
        }
    }
}
