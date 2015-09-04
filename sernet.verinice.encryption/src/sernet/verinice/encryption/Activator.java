package sernet.verinice.encryption;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 * 
 * @author Sebastian Engel <s.engel@tarent.de>
 */
public class Activator extends Plugin {

    private final Logger log = Logger.getLogger(Activator.class);
    
	// The plug-in ID
	public static final String PLUGIN_ID = "sernet.verinice.encryption";

	// The shared instance
	private static Activator plugin;
	
	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		plugin = this;
		if (log.isInfoEnabled()) {
            final Bundle bundle = context.getBundle();
            log.info("Starting bundle " + bundle.getSymbolicName() + " " + bundle.getVersion());
        }
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

}
