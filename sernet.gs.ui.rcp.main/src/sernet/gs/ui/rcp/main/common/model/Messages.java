package sernet.gs.ui.rcp.main.common.model;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "sernet.gs.ui.rcp.main.common.model.messages"; //$NON-NLS-1$

	public static String CnALink_admin;

	public static String CnALink_dependant;

	public static String CnALink_used;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
