package sernet.verinice.service.ldap;

import java.io.Serializable;
import java.util.List;

import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.ldap.ILdapCommand;
import sernet.verinice.interfaces.ldap.ILdapService;
import sernet.verinice.interfaces.ldap.PersonParameter;

@SuppressWarnings("serial")
public class LoadLdapUser extends GenericCommand implements ILdapCommand,Serializable {

	private transient ILdapService ldapService;
	
	private PersonParameter parameter;
	
	private List<PersonInfo> personList;

	public LoadLdapUser() {
		super();
	}

	public LoadLdapUser(PersonParameter parameter) {
		super();
		this.parameter = parameter;
	}

	@Override
	public void execute() {
		personList = getLdapService().getPersonList(getParameter());		
	}
	
	public PersonParameter getParameter() {
		return parameter;
	}

	public void setParameter(PersonParameter parameter) {
		this.parameter = parameter;
	}

	public List<PersonInfo> getPersonList() {
		return personList;
	}
	
	@Override
	public ILdapService getLdapService() {
		return ldapService;
	}

	@Override
	public void setLdapService(ILdapService ldapService) {
		this.ldapService = ldapService;
	}

}
