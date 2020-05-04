package sernet.verinice.model.report;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
    private static final String BUNDLE_NAME = "sernet.verinice.model.report.messages"; //$NON-NLS-1$
    public static String ReportTemplateMetaDataUnspecified;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
