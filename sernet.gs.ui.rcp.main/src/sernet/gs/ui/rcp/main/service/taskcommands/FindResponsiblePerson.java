/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak[at]sernet[dot]de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *     You should have received a copy of the GNU Lesser General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Alexander Koderman <ak[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.service.taskcommands;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import sernet.hui.common.connect.Property;
import sernet.hui.common.connect.PropertyList;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.bsi.MassnahmenUmsetzung;
import sernet.verinice.model.bsi.Person;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;

@SuppressWarnings("serial")
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
		
		// try to find someone responsible directly by role:
		foundPersons = getLinkedPersonsByRoles();
	}
	
	/**
	 * Go through linked persons of this target object or parents.
	 * If person's role equals this control's role, add to list of responsible persons.
	 * 
	 * @param the propertyId for the field containing all roles for which persons who have this role must be found. 
	 * 
	 * @return
	 */
	private List<Person> getLinkedPersonsByRoles() {
		PropertyList roles = umsetzung.getEntity().getProperties(field);
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
