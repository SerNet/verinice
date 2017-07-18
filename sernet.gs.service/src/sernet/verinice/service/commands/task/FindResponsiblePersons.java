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
package sernet.verinice.service.commands.task;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
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
    
    private static final String STD_ERR_MSG = "findLinkedPersons - currentElement:";

    private String propertyId;
    private Set<UnresolvedItem> unresolvedItems;

    private Map<Integer, Person> cachePerson;
    private Map<CacheRolePersonKey, Set<Person>> cacheRolePerson;
    private IBaseDao<Person, Serializable> personDAO;

    /**
     * @param unresolvedItems2
     */
    public FindResponsiblePersons(Set<UnresolvedItem> unresolvedItems, String propertyId) {
        this.propertyId = propertyId;
        this.unresolvedItems = unresolvedItems;
    }

    @Override
    public void execute() {
        cachePerson = new Hashtable<Integer, Person>();
        cacheRolePerson = new Hashtable<CacheRolePersonKey, Set<Person>>();
        findPersons();
    }

    private void findPersons() {
        for (UnresolvedItem unresolvedItem : unresolvedItems) {
            MassnahmenUmsetzung massnahme = unresolvedItem.getMassnahmenUmsetzung();
            if (massnahme == null) {
                massnahme = getMassnahmeDao().findById(unresolvedItem.getDbId());
            }
            List<CnATreeElement> foundPersons = findPersonsInLinks(massnahme);
            
            if(foundPersons.isEmpty()) {
                // try to find someone responsible by role:
                foundPersons = findPersonsInParent(massnahme);
            }

            if (foundPersons != null && foundPersons.size() > 0) {
                unresolvedItem.getItem().setUmsetzungDurch(getNames(foundPersons));
            }
        }
    }

    protected List<CnATreeElement> findPersonsInLinks(MassnahmenUmsetzung massnahme) {
        List<CnATreeElement> foundPersons = new LinkedList<CnATreeElement>();
        Set<CnALink> links = massnahme.getLinksUp();
        Set<Property> rolesToSearch = getRoles(massnahme);
        for (CnALink link : links) {
            if (link.getDependant().getTypeId().equals(Person.TYPE_ID)) {
                Person person = getPerson(link.getDependant().getDbId());
                for (Property role : rolesToSearch) {
                    if (person.hasRole(role)) {
                        foundPersons.add(person);
                    }
                }                  
            }
        }
        return foundPersons;
    }
    
    /**
     * Go through linked persons of this target object or parents. If person's
     * role equals this control's role, add to list of responsible persons.
     * 
     * @param umsetzung
     * @return A list with responsable Persons
     */
    public List<CnATreeElement> findPersonsInParent(MassnahmenUmsetzung umsetzung) {
        if (log.isDebugEnabled()) {
            log.debug("getLinkedPersonsByRoles - massnahme: " + umsetzung.getDbId() + ", propertyTypeId: " + propertyId);
        }
        Set<Person> result = new HashSet<Person>();
        Set<Property> rolesToSearch = getRoles(umsetzung);
        if (rolesToSearch != null && !rolesToSearch.isEmpty()) {
            findPersonsInParent(result, umsetzung.getParent().getParent(), rolesToSearch);
        }
        return new ArrayList<CnATreeElement>(result);
    }

    private void findPersonsInParent(Set<Person> result, CnATreeElement currentElement, Set<Property> rolesToSearch) {
        Integer id = null;
        if (log.isDebugEnabled()) {
            StringBuffer sb = new StringBuffer();
            for (Property role : rolesToSearch) {
                sb.append(role.getPropertyValue()).append(", ");
            }
            id = (currentElement != null) ? currentElement.getDbId() : -1;
            log.debug(STD_ERR_MSG + id + ", rollen:" + sb.toString());
        }

        for (Property role : rolesToSearch) {
            findLinkedPersons(result, currentElement, role);
        }
        if (!ITVerbund.TYPE_ID.equals(currentElement.getTypeId()) && !Organization.TYPE_ID.equals(currentElement.getTypeId()) && currentElement.getParent() != null) {
            findPersonsInParent(result, currentElement.getParent(), rolesToSearch);
        }
    }

    protected void findLinkedPersons(Set<Person> result, CnATreeElement currentElement, Property role) {
        Integer id = -1;
        if (log.isDebugEnabled() && (currentElement != null)) {
            id =  currentElement.getDbId();
        }
        CacheRolePersonKey key = new CacheRolePersonKey(currentElement.getDbId(), role.getPropertyValue());
        Set<Person> subResult = cacheRolePerson.get(key);
        if (subResult == null) {
            subResult = new HashSet<Person>();
            Set<CnALink> links = currentElement.getLinksUp();
            if (links != null) {
                for (CnALink link : links) {
                    if (link.getDependant().getTypeId().equals(Person.TYPE_ID)) {
                        Person person = getPerson(link.getDependant().getDbId());
                        if (log.isDebugEnabled()) {
                            log.debug(STD_ERR_MSG + id + ", person found: " + person.getDbId());
                        }
                        if (person.hasRole(role)) {
                            // we found someone for this role, continue with
                            // next role:
                            subResult.add(person);
                            if (log.isDebugEnabled()) {
                                log.debug(STD_ERR_MSG + id + ", role match: " + role.getPropertyValue() + ", person: " + person.getDbId());
                            }
                        }
                    }
                }
            }
            if (log.isDebugEnabled()) {
                log.debug(STD_ERR_MSG + id + ", no role found");
            }
            cacheRolePerson.put(key, subResult);
        }
        result.addAll(subResult);
    }

    private Person getPerson(Integer dbId) {
        Person person = cachePerson.get(dbId);
        if (person == null) {
            person = getPersonDao().findById(dbId);
            cachePerson.put(dbId, person);
        }
        return person;
    }
    
    private String getNames(List<CnATreeElement> persons) {
        StringBuffer names = new StringBuffer();
        boolean first = true;
        for (CnATreeElement element : persons) {
            if(!first) {
                names.append(", ");              
            }
            first = false;
            Person person = (Person) element;
            names.append(person.getFullName());
        }
        return names.toString();
    }
    
    protected Set<Property> getRoles(MassnahmenUmsetzung massnahme) {
        PropertyList roles = massnahme.getEntity().getProperties(propertyId);
        Set<Property> rolesToSearch = new HashSet<Property>();
        rolesToSearch.addAll(roles.getProperties());
        return rolesToSearch;
    }

    public Set<UnresolvedItem> getResolvedItems() {
        return this.unresolvedItems;
    }

    private IBaseDao<Person, Serializable> getPersonDao() {
        if (personDAO == null) {
            personDAO = getDaoFactory().getDAO(Person.class);
        }
        return personDAO;
    }
    
    private IBaseDao<MassnahmenUmsetzung, Serializable> getMassnahmeDao() {
        return getDaoFactory().getDAO(MassnahmenUmsetzung.class);
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
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            CacheRolePersonKey other = (CacheRolePersonKey) obj;
            if (!getOuterType().equals(other.getOuterType())) {
                return false;
            }
            if (elementDbId == null) {
                if (other.elementDbId != null) {
                    return false;
                }
            } else if (!elementDbId.equals(other.elementDbId)) {
                return false;
            }
            if (role == null) {
                if (other.role != null) {
                    return false;
                }
            } else if (!role.equals(other.role)) {
                return false;
            }
            return true;
        }

        private FindResponsiblePersons getOuterType() {
            return FindResponsiblePersons.this;
        }

    }
}
