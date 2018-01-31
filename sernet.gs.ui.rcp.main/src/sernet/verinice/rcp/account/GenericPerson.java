/*******************************************************************************
 * Copyright (c) 2014 Daniel Murygin.
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
package sernet.verinice.rcp.account;

import sernet.hui.common.connect.HUITypeFactory;
import sernet.verinice.model.bp.elements.BpPerson;
import sernet.verinice.model.bsi.Person;
import sernet.verinice.model.bsi.PersonenKategorie;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.PersonIso;
import sernet.verinice.rcp.ElementTitleCache;

/**
 * GenericPerson is a adapter class to to handle three types of persons:
 * {@link BpPerson}, {@link Person}, and {@link PersonIso}.
 * 
 * Feel free to extend to this class to adapt more methods.
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class GenericPerson {

    CnATreeElement person;

    public GenericPerson(CnATreeElement person) {
        super();
        this.person = person;
    }

    public String getName() {
        String name = null;
        if (person instanceof BpPerson) {
            name = ((BpPerson) person).getTitle();
        } else if (person instanceof PersonIso) {
            name = ((PersonIso) person).getFullName();
        } else if (person instanceof Person) {
            name = ((Person) person).getFullName();
        }
        return name;
    }

    public String getParentName() {
        String name = null;
        if (person instanceof BpPerson || person instanceof PersonIso) {
            name = ElementTitleCache.get(person.getParentId());
        }
        if (person instanceof Person) {
            name = HUITypeFactory.getInstance().getMessage(PersonenKategorie.TYPE_ID);
        }
        return name;
    }

}