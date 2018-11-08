/*******************************************************************************
 * Copyright (c) 2015 Daniel Murygin.
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.model.common;

import sernet.verinice.model.bp.elements.BpPerson;
import sernet.verinice.model.bsi.Person;
import sernet.verinice.model.iso27k.PersonIso;

/**
 * PersonAdapter adapts methods for
 * {@link PersonIso} and {@link Person} objects.
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public final class PersonAdapter {

    private PersonAdapter() {
        // do not instantiate this class
    }
    
    /**
     * @param person A person
     * @return the name of a person in the form: "SURNAME, FIRST NAME"
     */
    public static String getFullName(CnATreeElement person) {
        String name = null;
        if(person instanceof PersonIso) {
            name = getFullName((PersonIso)person);
        }
        if(person instanceof Person) {
            name = getFullName((Person)person);
        }
        if (person instanceof BpPerson) {
            name = getFullName((BpPerson) person);
        }
        return name;
    }
    
    private static String getFullName(PersonIso person) {
        StringBuilder sb = new StringBuilder();
        final String surname = person.getEntity().getPropertyValue(PersonIso.PROP_SURNAME);
        if(surname!=null && !surname.isEmpty()) {
            sb.append(surname);
        }
        final String name = person.getEntity().getPropertyValue(PersonIso.PROP_NAME);
        if(name!=null && !name.isEmpty()) {
            if(sb.length()>0) {
                sb.append(", ");
            }
            sb.append(name);
        }
        return sb.toString();
    }
    
    private static String getFullName(Person person) {
        StringBuilder sb = new StringBuilder();
        final String surname = person.getNachname();
        if(surname!=null && !surname.isEmpty()) {
            sb.append(surname);
        }
        final String name = person.getEntity().getPropertyValue(Person.P_VORNAME);
        if(name!=null && !name.isEmpty()) {
            if(sb.length()>0) {
                sb.append(", ");
            }
            sb.append(name);
        }
        return sb.toString();
    }

    private static String getFullName(BpPerson person) {
        StringBuilder sb = new StringBuilder();
        final String surname = person.getEntity().getPropertyValue(BpPerson.PROP_LAST_NAME);
        if (surname != null && !surname.isEmpty()) {
            sb.append(surname);
        }
        final String name = person.getEntity().getPropertyValue(BpPerson.PROP_FIRST_NAME);
        if (name != null && !name.isEmpty()) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(name);
        }
        return sb.toString();
    }
}
