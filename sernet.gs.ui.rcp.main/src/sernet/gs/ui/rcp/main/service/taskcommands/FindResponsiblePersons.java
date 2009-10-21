/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak@sernet.de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details.
 *     You should have received a copy of the GNU General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Alexander Koderman <ak@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.service.taskcommands;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import sernet.gs.ui.rcp.main.bsi.model.MassnahmenUmsetzung;
import sernet.gs.ui.rcp.main.bsi.model.Person;
import sernet.gs.ui.rcp.main.bsi.model.TodoViewItem;
import sernet.gs.ui.rcp.main.common.model.CnALink;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.connect.IBaseDao;
import sernet.gs.ui.rcp.main.service.commands.GenericCommand;
import sernet.hui.common.connect.Property;
import sernet.hui.common.connect.PropertyList;

public class FindResponsiblePersons extends GenericCommand {
	
	private String field;
	private Set<UnresolvedItem> unresolvedItems;
	

	/**
	 * @param unresolvedItems2
	 */
	public FindResponsiblePersons(Set<UnresolvedItem> unresolvedItems, String field) {
		this.field = field;
		this.unresolvedItems = unresolvedItems;
	}
	
	private String getNames(List<Person> persons) {
		StringBuffer names = new StringBuffer();
		for (Iterator iterator = persons.iterator(); iterator.hasNext();) {
			Person person = (Person) iterator.next();
			names.append(person.getFullName());
			if (iterator.hasNext())
				names.append(", "); //$NON-NLS-1$
		}
		return names.toString();
	}

	public void execute() {
		IBaseDao<MassnahmenUmsetzung, Serializable> mnDAO = getDaoFactory().getDAO(MassnahmenUmsetzung.class);
		
		for (UnresolvedItem unresolvedItem : unresolvedItems) {
			MassnahmenUmsetzung umsetzung = mnDAO.findById(unresolvedItem.getDbId());
			
			// try to find someone responsible by role:
			List<Person> foundPersons = getLinkedPersonsByRoles(umsetzung, field);
			
			if (foundPersons == null || foundPersons.size()==0)
				unresolvedItem.getItem().setUmsetzungDurch("");
			else {
				unresolvedItem.getItem().setUmsetzungDurch(getNames(foundPersons));
			}
			
		}
		
		
	}
	
	/**
	 * Go through linked persons of this target object or parents.
	 * If person's role equals this control's role, add to list of responsible persons.
	 * @param umsetzung 
	 * 
	 * @param the propertyId for the field containing all roles for which persons who have this role must be found. 
	 * 
	 * @return
	 */
	public List<Person>  getLinkedPersonsByRoles(MassnahmenUmsetzung umsetzung, String propertyTypeId) {
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

	/**
	 * @return
	 */
	public Set<UnresolvedItem> getResolvedItems() {
		return this.unresolvedItems;
	}


}
