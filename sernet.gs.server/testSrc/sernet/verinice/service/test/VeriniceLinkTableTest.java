/*******************************************************************************
 * Copyright (c) 2016 Daniel Murygin <dm{a}sernet{dot}de>.
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
 *     Daniel Murygin <dm{a}sernet{dot}de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.service.test;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import sernet.verinice.service.linktable.ILinkTableConfiguration;
import sernet.verinice.service.linktable.LinkTableConfiguration;
import sernet.verinice.service.linktable.vlt.VeriniceLinkTableIO;

/**
 *
 *
 * @author Daniel Murygin <dm{a}sernet{dot}de>
 */
public class VeriniceLinkTableTest {

    private static final String NAME = VeriniceLinkTableTest.class.getSimpleName();
    private static final String FILE_NAME = NAME + ".vlt";

    @Test
    public void testWrite() {
        LinkTableConfiguration.Builder builder = new LinkTableConfiguration.Builder();
        builder.addScopeId(204060)
        .addColumnPath("incident_scenario.incident_scenario_name")
        .addColumnPath("incident_scenario:person-iso")
        .addColumnPath("incident_scenario/person-iso.person-iso_name")
        .addColumnPath("incident_scenario/person-iso.person-iso_surname")
        .addColumnPath("incident_scenario/asset.asset_name")
        .addColumnPath("incident_scenario/asset:person-iso")
        .addColumnPath("incident_scenario/asset/person-iso.person-iso_name")
        .addColumnPath("incident_scenario/asset/person-iso.person-iso_surname")
        .addColumnPath("incident_scenario/control.control_name")
        .addColumnPath("incident_scenario/control:person-iso")
        .addColumnPath("incident_scenario/control/person-iso.person-iso_name")
        .addColumnPath("incident_scenario/control/person-iso.person-iso_surname")
        .addLinkTypeId("rel_incscen_asset")
        .addLinkTypeId("rel_control_incscen")
        .addLinkTypeId("rel_person_incscen_modl");
        LinkTableConfiguration before = builder.build();
        VeriniceLinkTableIO.write(before, getFilePath());
        ILinkTableConfiguration after = VeriniceLinkTableIO.readLinkTableConfiguration(getFilePath());
        assertTrue(before.equals(after));
    }

    private String getFilePath() {
        return "./" + FILE_NAME;
    }

}
