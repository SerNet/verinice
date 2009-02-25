package sernet.gs.ui.rcp.main.service.taskcommands;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import sernet.gs.ui.rcp.main.bsi.model.MassnahmenUmsetzung;
import sernet.gs.ui.rcp.main.bsi.model.Person;
import sernet.gs.ui.rcp.main.common.model.CnALink;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.connect.IBaseDao;
import sernet.gs.ui.rcp.main.service.commands.GenericCommand;
import sernet.hui.common.connect.Property;
import sernet.hui.common.connect.PropertyList;

public class FindResponsiblePerson extends GenericCommand {

	private Integer massnahmenUmsetzungDbId;
	private String field;
	private List<Person> foundPersons;
	
	private transient MassnahmenUmsetzung umsetzung;

	public FindResponsiblePerson(Integer massnahmenUmsetzungDbId,
			String field) {
		this.field = field;
		this.massnahmenUmsetzungDbId = massnahmenUmsetzungDbId;
	}

	public void execute() {
		IBaseDao<MassnahmenUmsetzung, Serializable> mnDAO = getDaoFactory().getDAO(MassnahmenUmsetzung.class);
		umsetzung = mnDAO.findById(massnahmenUmsetzungDbId);
		
		foundPersons = new ArrayList<Person>(1);
		// try to find someone responsible directly by role:
		List<Person> persons = getLinkedPersonsByRoles(field);
		StringBuilder names = new StringBuilder();
		for (Iterator iterator = persons.iterator(); iterator.hasNext();) {
			Person person = (Person) iterator.next();
			foundPersons.add(person);
		}
	}
	
	/**
	 * Go through linked persons of this target object or parents.
	 * If person's role equals this control's role, add to list of responsible persons.
	 * 
	 * @param the propertyId for the field containing all roles for which persons who have this role must be found. 
	 * 
	 * @return
	 */
	public List<Person>  getLinkedPersonsByRoles(String propertyTypeId) {
		PropertyList roles = umsetzung.getEntity().getProperties(propertyTypeId);
		List<Person> result = new ArrayList<Person>();
		if (roles.getProperties() == null || roles.getProperties().size() == 0 )
			return result;
		
		// search tree upward for linked persons:
		Set<Property> rolesToSearch = new HashSet<Property>();
		rolesToSearch.addAll(roles.getProperties());
		findLinkedPersons(result, umsetzung.getParent().getParent(), rolesToSearch);
		return result;
	}
	
	private void findLinkedPersons(List<Person> result, CnATreeElement currentElement, Set<Property> rolesToSearch ) {
		IBaseDao<Person, Serializable> personDAO = getDaoFactory().getDAO(Person.class);
		
		allRoles: for (Property role : rolesToSearch) {
			Set<CnALink> links = currentElement.getLinksDown();
			if (links != null) {
				for (CnALink link : links) {
					if (link.getDependency().getTypeId().equals(Person.TYPE_ID)) {
						Person person = personDAO.findById(link.getDependency().getDbId());
						if (person.hasRole(role)) {
							// we found someone for this role, continue with next role:
							result.add(person);
							continue allRoles;
						}
					}
				}
			}
			// no matching person here, try further up the tree for this role:
			Set<Property> justOneRole = new HashSet<Property>(1);
			justOneRole.add(role);
			if (currentElement.getParent() != null) {
				findLinkedPersons(result, currentElement.getParent(), justOneRole);
			}
		}
	}

	public List<Person> getFoundPersons() {
		return foundPersons;
	}


}
