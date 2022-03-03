package sernet.verinice.interfaces.ldap;

import java.util.List;

import sernet.verinice.service.ldap.PersonInfo;

public interface ILdapService {

    List<PersonInfo> getPersonList(PersonParameter parameter, String password);

    boolean isUsePasswordFromClient();

}
