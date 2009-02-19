package sernet.gs.ui.rcp.main.service;

import org.apache.derby.impl.sql.compile.CurrentRowLocationNode;

public class AuthenticationHelper {

	private String[] currentRoles;
	private static AuthenticationHelper instance = new AuthenticationHelper();

	private AuthenticationHelper() {
		currentRoles = ServiceFactory.lookupAuthService().getRoles();
	}
	
	public boolean currentUserHasRole(String[] allowedRoles) {
		for (String role : allowedRoles) {
			for (String userRole : currentRoles) {
				if (role.equals(userRole))
					return true;
			}
		}
		return false;
	}

	public static AuthenticationHelper getInstance() {
		return instance;
	}

}
