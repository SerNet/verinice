/*******************************************************************************
 * Copyright (c) 2019 Jochen Kemnade.
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
 ******************************************************************************/
package sernet.verinice.service.ldap;

import java.util.function.BiFunction;

import sernet.verinice.model.bp.elements.BpPerson;
import sernet.verinice.model.bsi.Person;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.Domain;
import sernet.verinice.model.iso27k.PersonIso;

public class PersonConverter implements BiFunction<PersonInfo, Domain, CnATreeElement> {

    @Override
    public CnATreeElement apply(PersonInfo personInfo, Domain domain) {
        CnATreeElement person;
        switch (domain) {
        case ISM:
            person = new PersonIso();
            break;
        case BASE_PROTECTION_OLD:
            person = new Person(null);
            break;
        case BASE_PROTECTION:
            person = new BpPerson(null);
            break;
        default:
            throw new IllegalArgumentException("Unknown domain " + domain);
        }

        setPersonName(person, personInfo.getGivenName());
        setPersonSurname(person, personInfo.getSurname());
        setPersonEmail(person, personInfo.getEMail());
        setPersonPhone(person, personInfo.getPhone());

        return person;
    }

    private void setPersonName(CnATreeElement person, String data) {
        if (person instanceof Person) {
            setPersonProperty(person, Person.P_VORNAME, data);
        } else if (person instanceof PersonIso) {
            setPersonProperty(person, PersonIso.PROP_NAME, data);
        } else if (person instanceof BpPerson) {
            setPersonProperty(person, BpPerson.PROP_FIRST_NAME, data);
        } else {
            throwUnsupportedType(person);
        }
    }

    private void setPersonSurname(CnATreeElement person, String data) {
        if (person instanceof Person) {
            setPersonProperty(person, Person.P_NAME, data);
        } else if (person instanceof PersonIso) {
            setPersonProperty(person, PersonIso.PROP_SURNAME, data);
        } else if (person instanceof BpPerson) {
            setPersonProperty(person, BpPerson.PROP_LAST_NAME, data);
        } else {
            throwUnsupportedType(person);
        }
    }

    private void setPersonEmail(CnATreeElement person, String data) {
        if (person instanceof Person) {
            setPersonProperty(person, Person.P_EMAIL, data);
        } else if (person instanceof PersonIso) {
            setPersonProperty(person, PersonIso.PROP_EMAIL, data);
        } else if (person instanceof BpPerson) {
            setPersonProperty(person, BpPerson.PROP_EMAIL, data);
        } else {
            throwUnsupportedType(person);
        }

    }

    private void setPersonPhone(CnATreeElement person, String data) {
        if (person instanceof Person) {
            setPersonProperty(person, Person.P_PHONE, data);
        } else if (person instanceof PersonIso) {
            setPersonProperty(person, PersonIso.PROP_TELEFON, data);
        } else if (person instanceof BpPerson) {
            setPersonProperty(person, BpPerson.PROP_PHONE, data);
        } else {
            throwUnsupportedType(person);
        }

    }

    private static void setPersonProperty(CnATreeElement person, String propertyName,
            String value) {
        if (value != null) {
            person.getEntity().setSimpleValue(person.getEntityType().getPropertyType(propertyName),
                    value);
        }
    }

    private static void throwUnsupportedType(CnATreeElement person) {
        throw new UnsupportedOperationException(
                "Cannot handle person with unsupported type " + person.getClass());
    }
}
