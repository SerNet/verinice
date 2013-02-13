package sernet.gs.server;

import org.eclipse.osgi.util.NLS;

final class Messages extends NLS {
	private static final String BUNDLE_NAME = "sernet.gs.server.messages"; //$NON-NLS-1$
	public static String InternalServer_0;
	public static String InternalServer_1;
	public static String InternalServer_2;
	public static String InternalServer_3;
	public static String InternalServer_4;
	public static String InternalServer_5;
	
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
