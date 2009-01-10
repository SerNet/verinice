package sernet.gs.ui.rcp.main.service;

/**
 * Find out if code is running on a client or the server.
 * Needed to find configuration files. Defaults to server.
 * 
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public abstract class WhereAmIUtil {

	public static final int LOCATION_CLIENT = 1;
	public static final int LOCATION_SERVER = 2;
	
	private static int location = LOCATION_SERVER;
	
	public static void setLocation(int loc) {
		location = loc;
	}

	public static boolean runningOnClient() {
		return (location == LOCATION_CLIENT);
	}

	public static boolean runningOnServer() {
		return (location == LOCATION_SERVER);
	}
	
	
	
	
	
}
