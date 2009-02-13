package sernet.gs.server.security;

import org.springframework.security.GrantedAuthority;
import org.springframework.security.context.SecurityContextHolder;

import sernet.gs.ui.rcp.main.common.model.configuration.Configuration;
import sernet.gs.ui.rcp.main.service.IAuthService;
import sernet.gs.ui.rcp.main.service.ICommandService;
import sernet.gs.ui.rcp.main.service.commands.CommandException;
import sernet.gs.ui.rcp.main.service.commands.RuntimeCommandException;
import sernet.gs.ui.rcp.main.service.crudcommands.CreateConfiguration;
import sernet.gs.ui.rcp.main.service.crudcommands.CreateDefaultConfiguration;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadConfiguration;
import sernet.gs.ui.rcp.main.service.crudcommands.SaveConfiguration;


public class AuthenticationService implements IAuthService {

	private ICommandService commandService;
	
	public String[] getRoles() {
		 GrantedAuthority[] authority = SecurityContextHolder.getContext().getAuthentication().getAuthorities();
		 String[] roles = new String[authority.length];
		 for (int i=0;i<authority.length; i++) {
			 roles[i] = authority[i].getAuthority();
		 }
		 return roles;
	}

	@Override
	public void setInitialPassword(String user, String pass) {
		System.out.println("alles paletti");
//		try {
//			// create new config:
//			CreateDefaultConfiguration command2 = new CreateDefaultConfiguration(user, pass);
//			command2 = commandService.executeCommand(
//					command2);
//		} catch (CommandException e) {
//			throw new RuntimeCommandException(e);
//		}
	}

	public ICommandService getCommandService() {
		return commandService;
	}

	public void setCommandService(ICommandService commandService) {
		this.commandService = commandService;
	}

}
