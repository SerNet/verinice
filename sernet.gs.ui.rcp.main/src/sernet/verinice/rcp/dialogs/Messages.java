package sernet.verinice.rcp.dialogs;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
    private static final String BUNDLE_NAME = "sernet.verinice.rcp.dialogs.messages"; //$NON-NLS-1$
    public static String ScrollableMultilineDialog_save_button_text;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
