package sernet.verinice.interfaces.ldap;

import java.util.List;

import sernet.verinice.service.ldap.PersonInfo;

public interface ILdapService {
	
	List<PersonInfo> getPersonList();
	
	List<PersonInfo> getPersonList(PersonParameter paramerter);
	
}

