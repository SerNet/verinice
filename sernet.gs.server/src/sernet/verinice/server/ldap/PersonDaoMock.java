package sernet.verinice.server.ldap;

import java.util.ArrayList;
import java.util.List;

import sernet.verinice.interfaces.ldap.IPersonDao;
import sernet.verinice.interfaces.ldap.PersonParameter;
import sernet.verinice.model.iso27k.PersonIso;
import sernet.verinice.service.ldap.PersonInfo;

public class PersonDaoMock implements IPersonDao {

	@Override
	public List<PersonInfo> getPersonList(PersonParameter parameter) {
		ArrayList<PersonInfo> personList = new ArrayList<PersonInfo>();
		
		PersonIso person = new PersonIso();
		person.setSurname("Müller");
		personList.add(new PersonInfo(person, "am"));
		
		person = new PersonIso();
		person.setSurname("Mayer");
		personList.add(new PersonInfo(person, "tm"));
		
		person = new PersonIso();
		person.setSurname("Schmidt");
		personList.add(new PersonInfo(person, "ms"));
		
		person = new PersonIso();
		person.setSurname("Peters");
		personList.add(new PersonInfo(person, "gp"));
		
		person = new PersonIso();
		person.setSurname("Wagner");
		personList.add(new PersonInfo(person, "rw"));
		
		person = new PersonIso();
		person.setSurname("Rudolph");
		personList.add(new PersonInfo(person, "mr"));
		
		person = new PersonIso();
		person.setSurname("Koch");
		personList.add(new PersonInfo(person, "tk"));
		
		person = new PersonIso();
		person.setSurname("Richard");
		personList.add(new PersonInfo(person, "sr"));
		
		person = new PersonIso();
		person.setSurname("Schuster");
		personList.add(new PersonInfo(person, "ds"));
		
		return personList;		
	}

}
