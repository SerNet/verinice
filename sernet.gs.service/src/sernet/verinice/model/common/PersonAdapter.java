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

import sernet.hui.common.connect.IPerson;

/**
 * PersonAdapter adapts methods for {@link IPerson} objects.
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
     * @return the name of a person in the form: "LAST NAME, FIRST NAME"
     */
    public static String getFullName(CnATreeElement person) {
        String name = null;
        if (person instanceof IPerson) {
            name = getFullName((IPerson) person);
        }
        return name;
    }

    private static String getFullName(IPerson person) {
        StringBuilder sb = new StringBuilder();
        final String lastName = person.getLastName();
        if (lastName != null && !lastName.isEmpty()) {
            sb.append(lastName);
        }
        final String firstName = person.getFirstName();
        if (firstName != null && !firstName.isEmpty()) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(firstName);
        }
        return sb.toString();
    }

}
