package sernet.gs.server;

import org.springframework.security.GrantedAuthority;
import org.springframework.security.context.SecurityContextHolder;

public class AuthenticationService implements IAuthService {

	public String[] getRoles() {
		 GrantedAuthority[] gas = SecurityContextHolder.getContext().getAuthentication().getAuthorities();
		 String[] roles = new String[gas.length];
		 for (int i=0;i<gas.length; i++) {
		 roles[i] = gas[i].getAuthority();
		 }
		 return roles;
	}

}
