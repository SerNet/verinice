package sernet.gs.server;

public interface IAuthService {
	/**
	 * Get user roles of currently active user.
	 * 
	 * @return
	 */
	public String[] getRoles();
}
