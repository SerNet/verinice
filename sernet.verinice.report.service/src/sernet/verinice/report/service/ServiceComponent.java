package sernet.verinice.report.service;

import sernet.verinice.oda.driver.impl.IVeriniceOdaDriver;

public class ServiceComponent {

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

	public IVeriniceOdaDriver getOdaDriver()
    {
    	return odaDriver;
    }

	public void bindOdaDriver(IVeriniceOdaDriver odaDriver) {
		this.odaDriver = odaDriver;
	}
    
    public void unbindOdaDriver(IVeriniceOdaDriver odaDriver) {
    	if (this.odaDriver == odaDriver)
    		this.odaDriver = null;
    }

}
