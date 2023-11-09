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
package sernet.verinice.service.linktable;

import static org.junit.Assert.assertEquals;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.junit.Test;

import sernet.verinice.model.bp.elements.BpRequirement;
import sernet.verinice.service.test.ContextConfiguration;

public class EntityPropertyAdapterTest extends ContextConfiguration {

    @Test
    public void test_get_last_changed_date_from_requirement() {
        BpRequirement requirement = new BpRequirement(null);
        ZonedDateTime date = ZonedDateTime.of(2019, 1, 1, 0, 0, 0, 0, ZoneId.systemDefault());

        requirement.getEntity().setPropertyValue(BpRequirement.PROP_LAST_CHANGE,
                String.valueOf(date.toInstant().toEpochMilli()));
        EntityPropertyAdapter entityPropertyAdapter = new EntityPropertyAdapter(requirement);
        String valueFromPropertyAdapter = entityPropertyAdapter
                .getPropertyValue(BpRequirement.PROP_LAST_CHANGE);
        assertEquals("2019-01-01", valueFromPropertyAdapter);
    }

    @Test
    public void test_get_bp_requirement_bcm_wiederanlaufzeit_unset() {
        BpRequirement requirement = new BpRequirement(null);

        EntityPropertyAdapter entityPropertyAdapter = new EntityPropertyAdapter(requirement);
        String valueFromPropertyAdapter = entityPropertyAdapter
                .getPropertyValue("bp_requirement_bcm_wiederanlaufzeit");
        assertEquals("0", valueFromPropertyAdapter);
    }

    @Test
    public void test_get_bp_requirement_bcm_wiederanlaufzeit() {
        BpRequirement requirement = new BpRequirement(null);
        requirement.setNumericProperty("bp_requirement_bcm_wiederanlaufzeit", 2);

        EntityPropertyAdapter entityPropertyAdapter = new EntityPropertyAdapter(requirement);
        String valueFromPropertyAdapter = entityPropertyAdapter
                .getPropertyValue("bp_requirement_bcm_wiederanlaufzeit");
        assertEquals("2", valueFromPropertyAdapter);
    }

}
