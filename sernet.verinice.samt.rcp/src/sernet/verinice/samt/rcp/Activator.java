package sernet.verinice.samt.rcp;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import sernet.verinice.oda.driver.impl.IVeriniceOdaDriver;
import sernet.verinice.report.service.impl.IReportService;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

    // The plug-in ID
    public static final String PLUGIN_ID = "sernet.verinice.samt.rcp";
    
    public static final String REPORT_SERVICE_PLUGIN_ID = "sernet.verinice.report.service";

    // The shared instance
    private static Activator plugin;
    
    private IReportService reportService;
    
    private IVeriniceOdaDriver odaDriver;

    /**
     * The constructor
     */
    public Activator() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
     * )
     */
    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
        
        ServiceReference sr = context.getServiceReference(IReportService.class.getName());
        if (sr != null)
        {
        	reportService = (IReportService) context.getService(sr);
        }
        
        sr = context.getServiceReference(IVeriniceOdaDriver.class.getName());
        if (sr != null)
        {
        	odaDriver = (IVeriniceOdaDriver) context.getService(sr);
        }
        
        // set workdir preference:
        SamtWorkspace.getInstance().prepareWorkDir();
        SamtWorkspace.getInstance().createSelfAssessmemtCatalog();
        
        // Sets the default preferences values of the self assessment
        SamtPreferencePage.setDefaults();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
     * )
     */
    @Override
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
    }

    /**
     * Returns the shared instance
     * 
     * @return the shared instance
     */
    public static Activator getDefault() {
        return plugin;
    }

    /**
     * Returns an image descriptor for the image file at the given plug-in
     * relative path
     * 
     * @param path
     *            the path
     * @return the image descriptor
     */
    public static ImageDescriptor getImageDescriptor(String path) {
        return imageDescriptorFromPlugin(PLUGIN_ID, path);
    }
    
    public IReportService getReportService()
    {
    	return reportService;
    }
    
    public IVeriniceOdaDriver getOdaDriver()
    {
    	return odaDriver;
    }
    
}
