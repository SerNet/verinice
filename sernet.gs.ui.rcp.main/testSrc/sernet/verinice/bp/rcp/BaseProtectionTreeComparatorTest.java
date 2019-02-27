/*******************************************************************************
 * Copyright (c) 2019 Alexander Ben Nasrallah.
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
package sernet.verinice.bp.rcp;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import sernet.hui.common.VeriniceContext;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.snutils.DBException;
import sernet.verinice.model.bp.groups.SafeguardGroup;

public class BaseProtectionTreeComparatorTest {

    private final int LT = -1;
    private final int EQ = 0;

    private BaseProtectionTreeComparator underTest = new BaseProtectionTreeComparator();

    @BeforeClass
    public static void setupTypeFactory() throws DBException {
        HUITypeFactory huiTypeFactory = HUITypeFactory
                .createInstance(BaseProtectionTreeComparatorTest.class
                        .getResource("/" + HUITypeFactory.HUI_CONFIGURATION_FILE));
        VeriniceContext.put(VeriniceContext.HUI_TYPE_FACTORY, huiTypeFactory);
    }

    @Test
    public void compareSafeguardGroupsIsReflexive() {
        SafeguardGroup group;

        group = new SafeguardGroup(null);
        group.setTitel("titile");
        Assert.assertEquals(EQ, underTest.compare(null, group, group));

        group.setIdentifier("identifier");
        Assert.assertEquals(EQ, underTest.compare(null, group, group));
    }

    @Test
    public void compareNestedSafeguardGroupsIsReflexive() {
        SafeguardGroup group;
        group = new SafeguardGroup(new SafeguardGroup(null));
        group.setTitel("titile");
        Assert.assertEquals(EQ, underTest.compare(null, group, group));

        group.setIdentifier("identifier");
        Assert.assertEquals(EQ, underTest.compare(null, group, group));
    }

    @Test
    public void compareSafeguardGroupsIsInvertible() {
        SafeguardGroup parent = new SafeguardGroup(null);
        parent.setTitel("Prozess-Bausteine");

        SafeguardGroup group1 = new SafeguardGroup(parent);
        group1.setTitel("APP");
        group1.setIdentifier("APP");

        SafeguardGroup group2 = new SafeguardGroup(null);
        group2.setTitel("ISMS");
        group2.setIdentifier("ISMS");

        Assert.assertEquals(-underTest.compare(null, group2, group1),
                underTest.compare(null, group1, group2));
    }

    @Test
    public void compareSafeguardGroupsIsTransitive() {
        SafeguardGroup parent = new SafeguardGroup(null);
        parent.setTitel("Prozess-Bausteine");

        SafeguardGroup group1 = new SafeguardGroup(parent);
        group1.setTitel("ISMS");
        group1.setIdentifier("ISMS");

        SafeguardGroup group2 = new SafeguardGroup(null);
        group2.setTitel("OPS");
        group2.setIdentifier("OPS");

        SafeguardGroup group3 = new SafeguardGroup(parent);
        group3.setTitel("NET");
        group3.setIdentifier("NET");

        Assert.assertEquals(LT, underTest.compare(null, group1, group2));
        Assert.assertEquals(LT, underTest.compare(null, group2, group3));
        Assert.assertEquals("a<b and b<c should imply a<c", LT,
                underTest.compare(null, group1, group3));
    }
}
