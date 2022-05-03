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
package sernet.verinice.model.common;

import org.junit.Assert;
import org.junit.Test;

import sernet.gs.service.AbstractRequiresHUITypeFactoryTest;
import sernet.verinice.model.bp.elements.BpRequirement;
import sernet.verinice.model.bp.elements.ItSystem;

public class CnATreeElementLabelGeneratorTest extends AbstractRequiresHUITypeFactoryTest {

    @Test
    public void titleIsReturnedIfIdentifierIsEmpty() {
        ItSystem itSystem = new ItSystem(null);
        itSystem.setPropertyValue(ItSystem.PROP_NAME, "Server 1");
        Assert.assertEquals("Server 1", CnATreeElementLabelGenerator.getElementTitle(itSystem));
    }

    @Test
    public void identifierIsReturnedIfTitleIsEmpty() {
        ItSystem itSystem = new ItSystem(null);
        itSystem.setPropertyValue(ItSystem.PROP_NAME, null);
        itSystem.getEntity()
                .setSimpleValue(itSystem.getEntityType().getPropertyType(ItSystem.PROP_ABBR), "S1");
        Assert.assertEquals("S1 ", CnATreeElementLabelGenerator.getElementTitle(itSystem));
    }

    @Test
    public void identifierIsIncluded() {
        ItSystem itSystem = new ItSystem(null);
        itSystem.setPropertyValue(ItSystem.PROP_NAME, "Server 1");
        itSystem.getEntity()
                .setSimpleValue(itSystem.getEntityType().getPropertyType(ItSystem.PROP_ABBR), "S1");
        Assert.assertEquals("S1 Server 1", CnATreeElementLabelGenerator.getElementTitle(itSystem));
    }

    @Test
    public void requirementWithoutIdentifier() {
        BpRequirement requirement = new BpRequirement(null);
        Assert.assertEquals("[] Baustein-Anforderung",
                CnATreeElementLabelGenerator.getElementTitle(requirement));
    }

}
