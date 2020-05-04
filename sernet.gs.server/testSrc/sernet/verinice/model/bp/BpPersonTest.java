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
package sernet.verinice.model.bp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.BeforeClass;
import org.junit.Test;

import sernet.hui.common.VeriniceContext;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.snutils.DBException;
import sernet.verinice.model.bp.elements.BpPerson;

public class BpPersonTest {

    @BeforeClass
    public static void setupTypeFactory() throws DBException {
        if (!VeriniceContext.exists(VeriniceContext.HUI_TYPE_FACTORY)) {
            HUITypeFactory huiTypeFactory = HUITypeFactory.createInstance(
                    BpPersonTest.class.getResource("/" + HUITypeFactory.HUI_CONFIGURATION_FILE));
            VeriniceContext.put(VeriniceContext.HUI_TYPE_FACTORY, huiTypeFactory);
        }
    }

    @Test
    public void testGetSalutation() {
        BpPerson person = new BpPerson(null);
        assertNull(person.getSalutation());
        person.setSimpleProperty(BpPerson.PROP_TITLE, "bp_person_titel_mr");
        assertEquals("Herr", person.getSalutation());
    }

    @Test
    public void invalidValueForSalutationReturnsNull() {
        BpPerson person = new BpPerson(null);
        person.setSimpleProperty(BpPerson.PROP_TITLE, "invalid");
        assertNull(person.getSalutation());
    }

}