package sernet.verinice.kerberos;

import org.eclipse.core.runtime.Plugin;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import sernet.verinice.kerberos.ticket.KerberosTicketServiceWindowsImpl;
import sernet.verinice.service.auth.KerberosTicketService;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

    // The plug-in ID
    public static final String PLUGIN_ID = "sernet.verinice.kerberos"; //$NON-NLS-1$

    // The shared instance
    private static Activator plugin;

    /** Needed by OSGI */
    public Activator() {
    }

    public void start(BundleContext context) throws Exception {

        super.start(context);

        // register an osgi service
        context.registerService(KerberosTicketService.class.getName(), new KerberosTicketServiceWindowsImpl(), null);

        plugin = this;
    }

    public void stop(BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
    }

    public static Activator getDefault() {
        return plugin;
    }

}
