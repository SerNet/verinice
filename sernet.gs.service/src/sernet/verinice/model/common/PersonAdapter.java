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
 * PersonAdapter adapts methods for {@link PersonIso}, {@link BpPerson}, and
 * {@link Person} objects.
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public final class PersonAdapter {

    private PersonAdapter() {
        // do not instantiate this class
    }

    /**
     * @param person
     *            A person
     * @return the name of a person in the form: "SURNAME, FIRST NAME"
     */
    public static String getFullName(CnATreeElement person) {
        if (person instanceof PersonIso) {
            final String firstName = person.getEntity().getSimpleValue(PersonIso.PROP_NAME);
            final String surname = person.getEntity().getSimpleValue(PersonIso.PROP_SURNAME);
            return getFullName(firstName, surname);
        } else if (person instanceof Person) {
            final String firstName = person.getEntity().getSimpleValue(Person.P_VORNAME);
            final String surname = ((Person) person).getNachname();
            return getFullName(firstName, surname);
        } else if (person instanceof BpPerson) {
            final String firstName = person.getEntity().getSimpleValue(BpPerson.PROP_FIRST_NAME);
            final String surname = person.getEntity().getSimpleValue(BpPerson.PROP_LAST_NAME);
            return getFullName(firstName, surname);
        } else {
            throw new IllegalArgumentException("Unsupported entity type: " + person.getTypeId());
        }
    }

    private static String getFullName(String name, String surname) {
        StringBuilder sb = new StringBuilder();
        if (surname != null && !surname.isEmpty()) {
            sb.append(surname);
        }
        if (name != null && !name.isEmpty()) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(name);
        }
        return sb.toString();
    }
}
