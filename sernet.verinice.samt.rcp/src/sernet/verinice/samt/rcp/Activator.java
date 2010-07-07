package sernet.verinice.samt.rcp;

import org.apache.log4j.Logger;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
@SuppressWarnings("restriction")
public class Activator extends AbstractUIPlugin {

    private final Logger log = Logger.getLogger(Activator.class);
    
    // The plug-in ID
    public static final String PLUGIN_ID = "sernet.verinice.samt.rcp";
    
    // The shared instance
    private static Activator plugin;

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
        
        if (log.isInfoEnabled()) {
            final Bundle bundle = context.getBundle();
            log.info("Starting bundle " + bundle.getSymbolicName() + " " + bundle.getVersion());
        }
        
        // set workdir preference:
        SamtWorkspace.getInstance().prepareWorkDir();
        SamtWorkspace.getInstance().createConfDir();
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

}
