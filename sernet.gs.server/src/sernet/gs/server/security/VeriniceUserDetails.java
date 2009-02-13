package sernet.gs.server.security;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.GrantedAuthority;
import org.springframework.security.GrantedAuthorityImpl;
import org.springframework.security.userdetails.UserDetails;

public class VeriniceUserDetails implements UserDetails {

	public VeriniceUserDetails(String user, String pass) {
		super();
		this.user = user;
		this.pass = pass;
	}

	private String user;
	private String pass;
	private List<GrantedAuthority> roles = new ArrayList<GrantedAuthority>();
	
	public GrantedAuthority[] getAuthorities() {
		return (GrantedAuthority[]) roles.toArray(new GrantedAuthority[roles.size()]);
	}

	public String getPassword() {
		return pass;
	}

	public String getUsername() {
		return user;
	}

	public boolean isAccountNonExpired() {
		return true;
	}

	public boolean isAccountNonLocked() {
		return true;
	}

	public boolean isCredentialsNonExpired() {
		return true;
	}

	public boolean isEnabled() {
		return true;
	}

	public void addRole(String role) {
		roles.add(new GrantedAuthorityImpl(role));
	}
	
}
