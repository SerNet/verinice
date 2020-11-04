/*******************************************************************************
 * Copyright (c) 2018 Jochen Kemnade.
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
package sernet.verinice.service.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.hamcrest.CoreMatchers;
import org.junit.Test;

import org.junit.Assert;
import sernet.hui.common.connect.DirectedHuiRelation;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.hui.common.connect.HuiRelation;
import sernet.verinice.web.DirectedHuiRelationConverter;

public class DirectedHuiRelationConverterTest extends ContextConfiguration {

    @Test
    public void testGetAsObjectForwardRelation() {
        Object object = new DirectedHuiRelationConverter().getAsObject(null, null,
                "frel_process_person_zugriffsberechtigt");
        assertThat(object, CoreMatchers.instanceOf(DirectedHuiRelation.class));
        DirectedHuiRelation relation = (DirectedHuiRelation) object;
        assertTrue(relation.isForward());
        assertEquals("rel_process_person_zugriffsberechtigt", relation.getHuiRelation().getId());
    }

    @Test
    public void testGetAsObjectBackwardRelation() {
        Object object = new DirectedHuiRelationConverter().getAsObject(null, null,
                "brel_process_person_zugriffsberechtigt");
        assertThat(object, CoreMatchers.instanceOf(DirectedHuiRelation.class));
        DirectedHuiRelation relation = (DirectedHuiRelation) object;
        assertFalse(relation.isForward());
        assertEquals("rel_process_person_zugriffsberechtigt", relation.getHuiRelation().getId());
    }

    @Test
    public void testGetAsStringForwardRelation() {
        HuiRelation relation = HUITypeFactory.getInstance()
                .getRelation("rel_process_person_zugriffsberechtigt");
        DirectedHuiRelation directedRelation = DirectedHuiRelation.getDirectedHuiRelation(relation,
                true);
        Assert.assertEquals("frel_process_person_zugriffsberechtigt",
                new DirectedHuiRelationConverter().getAsString(null, null, directedRelation));
    }

    @Test
    public void testGetAsStringBackwardRelation() {
        HuiRelation relation = HUITypeFactory.getInstance()
                .getRelation("rel_process_person_zugriffsberechtigt");
        DirectedHuiRelation directedRelation = DirectedHuiRelation.getDirectedHuiRelation(relation,
                false);
        Assert.assertEquals("brel_process_person_zugriffsberechtigt",
                new DirectedHuiRelationConverter().getAsString(null, null, directedRelation));
    }

    @Test
    public void testRoundtripForwardRelation() {
        HuiRelation relation = HUITypeFactory.getInstance()
                .getRelation("rel_process_person_zugriffsberechtigt");
        DirectedHuiRelation directedRelation = DirectedHuiRelation.getDirectedHuiRelation(relation,
                true);
        String stringRepresentation =  new DirectedHuiRelationConverter().getAsString(null, null, directedRelation);
        Object convertedBack = new DirectedHuiRelationConverter().getAsObject(null, null, stringRepresentation);
        Assert.assertEquals(directedRelation, convertedBack);
    }

    @Test
    public void testRoundtripBackwardRelation() {
        HuiRelation relation = HUITypeFactory.getInstance()
                .getRelation("rel_process_person_zugriffsberechtigt");
        DirectedHuiRelation directedRelation = DirectedHuiRelation.getDirectedHuiRelation(relation,
                false);
        String stringRepresentation =  new DirectedHuiRelationConverter().getAsString(null, null, directedRelation);
        Object convertedBack = new DirectedHuiRelationConverter().getAsObject(null, null, stringRepresentation);
        Assert.assertEquals(directedRelation, convertedBack);
    }
}
