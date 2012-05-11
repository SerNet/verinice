package sernet.verinice.interfaces.ldap;

import java.util.List;

import sernet.verinice.service.ldap.PersonInfo;

/**
 * @author Daniel Murygin <dm@sernet.de>
 *
 */
public interface IPersonDao {

	public List<PersonInfo> getPersonList(PersonParameter parameter);
	
}
