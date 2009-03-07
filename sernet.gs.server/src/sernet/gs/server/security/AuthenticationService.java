package sernet.gs.server.security;

import org.springframework.security.GrantedAuthority;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.security.ui.digestauth.DigestProcessingFilter;
import org.springframework.security.ui.digestauth.DigestProcessingFilterEntryPoint;

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

	private DigestProcessingFilterEntryPoint entryPoint;
	
	public String[] getRoles() {
		 GrantedAuthority[] authority = SecurityContextHolder.getContext().getAuthentication().getAuthorities();
		 String[] roles = new String[authority.length];
		 for (int i=0;i<authority.length; i++) {
			 roles[i] = authority[i].getAuthority();
		 }
		 return roles;
	}

	public String hashPassword(String username, String pass) {
		return DigestProcessingFilter.encodePasswordInA1Format(username,
			getEntryPoint().getRealmName(), pass);
	}
	
	public DigestProcessingFilterEntryPoint getEntryPoint() {
		return entryPoint;
	}

	public void setEntryPoint(DigestProcessingFilterEntryPoint entryPoint) {
		this.entryPoint = entryPoint;
	}

}
