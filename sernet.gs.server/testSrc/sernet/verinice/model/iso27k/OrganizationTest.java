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

package sernet.verinice.model.iso27k;

import org.junit.Test;

import org.junit.Assert;
import sernet.verinice.model.bp.groups.ItSystemGroup;
import sernet.verinice.service.test.ContextConfiguration;

public class OrganizationTest extends ContextConfiguration {

    @Test
    public void itbpGroupCannotBeAChild() {
        Organization organization = new Organization();
        ItSystemGroup itSystemGroup = new ItSystemGroup(null);

        Assert.assertFalse(organization.canContain(itSystemGroup));
    }

    @Test
    public void ismGroupCanBeAChild() {
        Organization organization = new Organization();
        AssetGroup assetGroup = new AssetGroup();

        Assert.assertTrue(organization.canContain(assetGroup));
    }
}
