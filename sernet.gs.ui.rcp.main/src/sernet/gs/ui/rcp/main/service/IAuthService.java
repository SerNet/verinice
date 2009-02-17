package sernet.gs.ui.rcp.main.service;

public interface IAuthService {
	/**
	 * Get user roles of currently active user.
	 * 
	 * @return
	 */
	public String[] getRoles();

	/**
	 * Set initial admin passwort.
	 * Will only work when no admin password has been set.
	 * @param pass 
	 * @param user 
	 */
	public void setInitialPassword(String user, String pass);
}
