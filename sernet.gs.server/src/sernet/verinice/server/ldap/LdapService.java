package sernet.verinice.server.ldap;

import java.util.List;

import sernet.verinice.interfaces.ldap.ILdapService;
import sernet.verinice.interfaces.ldap.IPersonDao;
import sernet.verinice.interfaces.ldap.PersonParameter;
import sernet.verinice.service.ldap.PersonInfo;

public class LdapService implements ILdapService {

	private IPersonDao personDao;

	@Override
	public List<PersonInfo> getPersonList() {
		return getPersonList(null);
	}
	
	@Override
	public List<PersonInfo> getPersonList(PersonParameter parameter) {
		return  getPersonDao().getPersonList(parameter);
	}
	
	public IPersonDao getPersonDao() {
		return personDao;
	}

	public void setPersonDao(IPersonDao personDao) {
		this.personDao = personDao;
	}

	

}
