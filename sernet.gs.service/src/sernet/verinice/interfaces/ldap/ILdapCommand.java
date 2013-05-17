package sernet.verinice.interfaces.ldap;

import java.io.Serializable;

public interface ILdapCommand extends Serializable {

	ILdapService getLdapService();
	
	void setLdapService(ILdapService ldapService);
}
