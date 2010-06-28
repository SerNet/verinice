package sernet.gs.ui.rcp.main;

import sernet.verinice.interfaces.encryption.IEncryptionService;

public class ServiceComponent {

    private IEncryptionService encryptionService;

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
    	if (this.encryptionService == encryptionService)
    		this.encryptionService = null;
    }
}
