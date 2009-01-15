package sernet.gs.ui.rcp.main.service;

/**
 * Find out if code is running on a client or the server.
 * Needed to find configuration files. Defaults to server.
 * 
 * Explanation: the command service is designed to be transparent
 * as to the execution of commands. It can be switched from remote
 * to local without the commands or application having to be aware of this switch.
 * 
 * For commands that do need to know where they are running, this utility class
 * can be queried. 
 *
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
