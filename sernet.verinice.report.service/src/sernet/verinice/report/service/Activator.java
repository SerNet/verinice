package sernet.verinice.report.service;

import org.apache.log4j.Logger;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.interfaces.oda.IVeriniceOdaDriver;

public class Activator implements BundleActivator {
	
    private final Logger log = Logger.getLogger(Activator.class);
    
    // The shared instance
    private static Activator plugin;
    
    private ServiceTracker commandServiceTracker;
    
    private ServiceTracker odaDriverTracker;
	
	public void start(BundleContext context) throws Exception {
		plugin = this;

		if (log.isInfoEnabled()) {
            final Bundle bundle = context.getBundle();
            log.info("Starting bundle " + bundle.getSymbolicName() + " " + bundle.getVersion());
        }
		
		// Reach ICommandService implementation via service tracker since the instance
		// is provided via Spring (and should not be instantiated by OSGi)
		commandServiceTracker = new ServiceTracker(context, ICommandService.class.getName(), null);
		commandServiceTracker.open();
		
		odaDriverTracker = new ServiceTracker(context, IVeriniceOdaDriver.class.getName(), null);
		odaDriverTracker.open();
	}

	public void stop(BundleContext context) throws Exception {
		commandServiceTracker.close();
		odaDriverTracker.close();
	}
	
	public static Activator getDefault()
	{
		return plugin;
	}

	public ICommandService getCommandService()
	{
		return (ICommandService) commandServiceTracker.getService();
	}
	
	public IVeriniceOdaDriver getOdaDriver()
	{
		return (IVeriniceOdaDriver) odaDriverTracker.getService();
	}
	
}
