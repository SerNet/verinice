package sernet.gs.ui.rcp.main.service.commands;

import sernet.gs.ui.rcp.main.service.IAuthService;

public interface IAuthAwareCommand {
	public void setAuthService(IAuthService service);
	public IAuthService getAuthService();
}
