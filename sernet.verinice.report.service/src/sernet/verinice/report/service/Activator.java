package sernet.verinice.report.service;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.oda.driver.impl.IVeriniceOdaDriver;
import sernet.verinice.report.service.impl.IReportService;
import sernet.verinice.report.service.impl.ReportService;

public class Activator implements BundleActivator {
	
	private IVeriniceOdaDriver odaDriver;
	
	private ICommandService commandService;
	
    // The shared instance
    private static Activator plugin;
	
	public void start(BundleContext context) throws Exception {
		plugin = this;
		
		context.registerService(IReportService.class.getName(), new ReportService(), null);
		
		ServiceReference sr = context.getServiceReference(IVeriniceOdaDriver.class.getName());
		if (sr != null)
		{
			odaDriver = (IVeriniceOdaDriver) context.getService(sr);
		}
		
		sr = context.getServiceReference(ICommandService.class.getName());
		if (sr != null)
		{
			commandService = (ICommandService) context.getService(sr);
		}
	}

	public void stop(BundleContext context) throws Exception {
	}
	
	public static Activator getDefault()
	{
		return plugin;
	}

	public IVeriniceOdaDriver getOdaDriver()
	{
		return odaDriver;
	}
	
	public ICommandService getCommandService()
	{
		return commandService;
	}
	
}
