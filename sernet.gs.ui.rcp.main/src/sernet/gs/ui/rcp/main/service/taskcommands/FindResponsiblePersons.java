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
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import sernet.hui.common.connect.Property;
import sernet.hui.common.connect.PropertyList;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.bsi.ITVerbund;
import sernet.verinice.model.bsi.MassnahmenUmsetzung;
import sernet.verinice.model.bsi.Person;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Organization;

public class FindResponsiblePersons extends GenericCommand {
	
	private final Logger log = Logger.getLogger(FindResponsiblePersons.class);
	
	private String field;
	private Set<UnresolvedItem> unresolvedItems;
	
	private Map<Integer, Person> cachePerson;
	private Map<CacheRolePersonKey, Set<Person>> cacheRolePerson;
	private IBaseDao<Person, Serializable> personDAO;
	
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
			if (iterator.hasNext()){
				names.append(", "); //$NON-NLS-1$
			}
		}
		return names.toString();
	}

	public void execute() {
		cachePerson = new Hashtable<Integer, Person>();
		cacheRolePerson = new Hashtable<CacheRolePersonKey, Set<Person>>();
		IBaseDao<MassnahmenUmsetzung, Serializable> mnDAO = getDaoFactory().getDAO(MassnahmenUmsetzung.class);
		
		for (UnresolvedItem unresolvedItem : unresolvedItems) {
			MassnahmenUmsetzung umsetzung = unresolvedItem.getMassnahmenUmsetzung();
			if(umsetzung==null) {
				umsetzung = mnDAO.findById(unresolvedItem.getDbId());
			}
			
			// try to find someone responsible by role:
			List<Person> foundPersons = getLinkedPersonsByRoles(umsetzung, field);
			
			if (foundPersons == null || foundPersons.size()==0){
				unresolvedItem.getItem().setUmsetzungDurch("");
			} else {
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
		if (log.isDebugEnabled()) {
			log.debug("getLinkedPersonsByRoles - massnahme: " + umsetzung.getDbId() + ", propertyTypeId: " + propertyTypeId);
		}
		PropertyList roles = umsetzung.getEntity().getProperties(propertyTypeId);
		Set<Person> result = new HashSet<Person>();
		if (roles.getProperties() != null && roles.getProperties().size() != 0 ){
			// search tree upward for linked persons:
			Set<Property> rolesToSearch = new HashSet<Property>();
			rolesToSearch.addAll(roles.getProperties());
			findLinkedPersons(result, umsetzung.getParent().getParent(), rolesToSearch);
		}
		return new ArrayList<Person>(result);
	}
	
	private void findLinkedPersons(Set<Person> result, CnATreeElement currentElement, Set<Property> rolesToSearch ) {
		final String STD_ERR_MSG = "findLinkedPersons - currentElement:";
	    Integer id = null;
		if (log.isDebugEnabled()) {
			StringBuffer sb = new StringBuffer();
			for (Property role : rolesToSearch) {
				sb.append(role.getPropertyValue()).append(", ");
			}
			id = (currentElement!=null) ? currentElement.getDbId() : -1;
			log.debug(STD_ERR_MSG + id + ", rollen:" + sb.toString());
		}
		
		allRoles: for (Property role : rolesToSearch) {
			CacheRolePersonKey key = new CacheRolePersonKey(currentElement.getDbId(),role.getPropertyValue());
			Set<Person> subResult = cacheRolePerson.get(key);
			if(subResult==null) {
				subResult = new HashSet<Person>();
				Set<CnALink> links = currentElement.getLinksDown();
				if (links != null) {
					for (CnALink link : links) {
						if (link.getDependency().getTypeId().equals(Person.TYPE_ID)) {
							
							Person person = getPerson(link.getDependency().getDbId());
								
							if (log.isDebugEnabled()) {
								log.debug(STD_ERR_MSG + id + ", person found: " + person.getDbId());
							}
							if (person.hasRole(role)) {
								// we found someone for this role, continue with next role:
								subResult.add(person);
								if (log.isDebugEnabled()) {
									log.debug(STD_ERR_MSG + id + ", role match: " + role.getPropertyValue() + ", person: " + person.getDbId());
								}
								cacheRolePerson.put(key, subResult);
								result.addAll(subResult);
								continue allRoles;
							}
						}
					}
				}
				if (log.isDebugEnabled()) {
					log.debug(STD_ERR_MSG + id + ", no role found" );
				}
				// no matching person here, try further up the tree for this role:
				Set<Property> justOneRole = new HashSet<Property>(1);
				justOneRole.add(role);
				if (!ITVerbund.TYPE_ID.equals(currentElement.getTypeId()) 
					&& !Organization.TYPE_ID.equals(currentElement.getTypeId())
					&& currentElement.getParent() != null) {
					findLinkedPersons(result, currentElement.getParent(), justOneRole);
				}
				cacheRolePerson.put(key, subResult);
			} else {
				result.addAll(subResult);
			}
		}
	
	}

	private Person getPerson(Integer dbId) {
		Person person = cachePerson.get(dbId);
		if(person==null) {
			person = getPersonDao().findById(dbId);
			cachePerson.put(dbId, person);
		}
		return person;
	}

	/**
	 * @return
	 */
	public Set<UnresolvedItem> getResolvedItems() {
		return this.unresolvedItems;
	}

	private IBaseDao<Person, Serializable> getPersonDao() {
		if(personDAO==null) {
			personDAO = getDaoFactory().getDAO(Person.class);
		}
		return personDAO;
	}
	
	class CacheRolePersonKey {
		private Integer elementDbId;
		private String role;
		public CacheRolePersonKey(Integer elementDbId, String role) {
			super();
			this.elementDbId = elementDbId;
			this.role = role;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((elementDbId == null) ? 0 : elementDbId.hashCode());
			result = prime * result + ((role == null) ? 0 : role.hashCode());
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj){
				return true;
			}
			if (obj == null){
				return false;
			}
			if (getClass() != obj.getClass()){
				return false;
			}
			CacheRolePersonKey other = (CacheRolePersonKey) obj;
			if (!getOuterType().equals(other.getOuterType())){
				return false;
			}
			if (elementDbId == null) {
				if (other.elementDbId != null){
					return false;
				}
			} else if (!elementDbId.equals(other.elementDbId)){
				return false;
			}
			if (role == null) {
				if (other.role != null){
					return false;
				}
			} else if (!role.equals(other.role)){
				return false;
			}
			return true;
		}
		private FindResponsiblePersons getOuterType() {
			return FindResponsiblePersons.this;
		}
		
	}
}
