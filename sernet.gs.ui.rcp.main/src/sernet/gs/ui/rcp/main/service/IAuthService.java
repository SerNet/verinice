package sernet.gs.ui.rcp.main.service;

public interface IAuthService {
	/**
	 * Get user roles of currently active user.
	 * 
	 * @return
	 */
	public String[] getRoles();
	
	public String hashPassword(String username, String clearText);

}
