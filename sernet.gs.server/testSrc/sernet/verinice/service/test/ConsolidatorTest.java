/*******************************************************************************
 * Copyright (c) 2020 Finn Westendorf.
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
import static org.junit.Assert.assertNotNull;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.springframework.test.context.transaction.TransactionConfiguration;

import sernet.hui.common.connect.EntityType;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.hui.common.connect.HitroUtil;
import sernet.hui.common.connect.PropertyGroup;
import sernet.hui.common.connect.PropertyType;
import sernet.verinice.model.bp.ImplementationStatus;
import sernet.verinice.model.bp.elements.Application;
import sernet.verinice.model.bp.elements.BpRequirement;
import sernet.verinice.model.bp.elements.BpThreat;
import sernet.verinice.model.bp.elements.ItNetwork;
import sernet.verinice.model.bp.elements.Safeguard;
import sernet.verinice.model.bp.groups.BpRequirementGroup;
import sernet.verinice.model.bp.groups.BpThreatGroup;
import sernet.verinice.model.bp.groups.SafeguardGroup;
import sernet.verinice.service.commands.bp.ConsoliData;
import sernet.verinice.service.commands.bp.ConsolidatorCommand;

@TransactionConfiguration(transactionManager = "txManager")
public class ConsolidatorTest extends AbstractModernizedBaseProtection {

    private static final Logger LOG = Logger.getLogger(ConsolidatorTest.class);

    private ItNetwork scope;
    private Random random = new Random();

    /**
     * test randomly consolidated data.
     * 
     * @throws Exception
     */
    @Test
    public void testConsolidatorTitles() throws Exception {
        scope = createNewBPOrganization();
        Application app1 = createBpApplication(createBpApplicationGroup(scope));
        BpRequirementGroup source = createRequirementGroup(app1);
        source.setIdentifier("SYS 1.2.3");
        BpRequirement req1 = createBpRequirement(source);
        req1.setIdentifier("SYS 1.2.3");
        req1.setTitle("Requirement A");
        BpThreatGroup threatGroup1 = createBpThreatGroup(source);
        BpThreat threat1 = createBpThreat(threatGroup1);
        threat1.setTitel("Threat A");
        threat1.setIdentifier("TID");
        createLink(req1, threat1, "");

        Application app2 = createBpApplication(createBpApplicationGroup(scope));
        BpRequirementGroup target = createRequirementGroup(app2);
        target.setIdentifier("SYS 1.2.3");
        BpRequirement req2 = createBpRequirement(target);
        req2.setIdentifier("SYS 1.2.3");
        req2.setTitle("Requirement B");
        BpThreatGroup threatGroup2 = createBpThreatGroup(source);
        BpThreat threat2 = createBpThreat(threatGroup2);
        threat2.setTitel("Threat B");
        threat2.setIdentifier("TID");
        createLink(req2, threat2, "");

        // test the ConsolidatorCommand 20 times as we take some random values
        for (int i = 0; i < 20; i++) {
            Entry<String, String> randomRequirementProperty = randomProperty(BpRequirement.TYPE_ID);
            Entry<String, String> randomThreatProperty = randomProperty(BpThreat.TYPE_ID);

            String reqValue = getPropertyValue(BpRequirement.TYPE_ID,
                    randomRequirementProperty.getValue());
            String threatValue = getPropertyValue(BpThreat.TYPE_ID,
                    randomThreatProperty.getValue());

            // choose the value carefully
            for (int counter = 0; counter < 100; counter++) {
                randomRequirementProperty = randomProperty(BpRequirement.TYPE_ID);
                randomThreatProperty = randomProperty(BpThreat.TYPE_ID);

                reqValue = getPropertyValue(BpRequirement.TYPE_ID,
                        randomRequirementProperty.getValue());
                threatValue = getPropertyValue(BpThreat.TYPE_ID, randomThreatProperty.getValue());
                LOG.info("Chosen values: " + reqValue + " " + threatValue);
                if (reqValue != null && threatValue != null) {
                    break;
                }
            }

            assertNotNull("Sample value null", reqValue);
            assertNotNull("Sample value null", threatValue);

            req1.setSimpleProperty(randomRequirementProperty.getValue(), reqValue);
            threat1.setSimpleProperty(randomThreatProperty.getValue(), threatValue);

            updateElement(scope);

            updateElement(app1);
            updateElement(source);
            updateElement(req1);
            updateElement(threatGroup1);
            updateElement(threat1);

            updateElement(app2);
            updateElement(target);
            updateElement(req2);
            updateElement(threatGroup2);
            updateElement(threat2);

            Set<String> properties = new HashSet<>(
                    Arrays.asList("bp_requirement_general", "bp_threat_general",
                            randomRequirementProperty.getKey(), randomThreatProperty.getKey()));
            Set<String> uuids = new HashSet<>(Arrays.asList(target.getUuid()));

            ConsolidatorCommand command = new ConsolidatorCommand(
                    new ConsoliData(source, properties, uuids));
            commandService.executeCommand(command);

            req2 = reloadElement(req2);
            threat2 = reloadElement(threat2);

            assertEquals(req1.getTitle(), req2.getTitle());
            assertEquals(threat1.getTitle(), threat2.getTitle());
            assertEquals(reqValue, req2.getPropertyValue(randomRequirementProperty.getValue()));
            assertEquals(threatValue, threat2.getPropertyValue(randomThreatProperty.getValue()));
        }
    }

    @Test
    public void testConsolidatorTitlesSafeguardImpl() throws Exception {
        scope = createNewBPOrganization();
        Application app1 = createBpApplication(createBpApplicationGroup(scope));
        BpRequirementGroup source = createRequirementGroup(app1, "SYS 1.2.3", "req-group1");
        BpRequirement req1 = createBpRequirement(source, "SYS 1.2.3", "Requirement A");

        BpThreatGroup threatGroup1 = createBpThreatGroup(source);
        BpThreat threat1 = createBpThreat(threatGroup1, "Threat A");
        threat1.setIdentifier("TID");
        createLink(req1, threat1, "");
        SafeguardGroup safeguardGroup = createSafeguardGroup(source, "SYS 1.2.3",
                "safeguardGroup1");
        Safeguard safeguard1 = createSafeguard(safeguardGroup, "SAFE 1", "Safeguard");
        safeguard1.setImplementationStatus(ImplementationStatus.YES);
        createLink(req1, safeguard1, "");

        Application app2 = createBpApplication(createBpApplicationGroup(scope));
        BpRequirementGroup target = createRequirementGroup(app2, "SYS 1.2.3", "g2");
        BpRequirement req2 = createBpRequirement(target, "SYS 1.2.3", "Requirement B");
        BpThreatGroup threatGroup2 = createBpThreatGroup(source);
        BpThreat threat2 = createBpThreat(threatGroup2, "Threat B");
        threat2.setIdentifier("TID");
        createLink(req2, threat2, "");
        SafeguardGroup safeguardGroup1 = createSafeguardGroup(source, "SYS 1.2.3",
                "safeguardGroup2");
        Safeguard safeguard2 = createSafeguard(safeguardGroup1, "SAFE 1", "Safeguard-Target");
        createLink(req2, safeguard2, "");

        updateElement(scope);

        updateElement(app1);
        updateElement(source);
        req1 = (BpRequirement) updateElement(req1);
        updateElement(threatGroup1);
        updateElement(threat1);
        updateElement(safeguardGroup1);
        updateElement(safeguard1);

        updateElement(app2);
        updateElement(target);
        updateElement(req2);
        updateElement(threatGroup2);
        updateElement(threat2);
        updateElement(safeguard2);

        Set<String> properties = new HashSet<>(
                Arrays.asList("bp_requirement_general", "bp_threat_general", "bp_safeguard_general",
                        "bp_requirement_implementation", "bp_safeguard_implementation"));
        Set<String> uuids = new HashSet<>(Arrays.asList(target.getUuid()));

        ConsolidatorCommand command = new ConsolidatorCommand(
                new ConsoliData(source, properties, uuids));
        commandService.executeCommand(command);

        req2 = reloadElement(req2);
        threat2 = reloadElement(threat2);
        safeguard2 = reloadElement(safeguard2);

        assertEquals(req1.getTitle(), req2.getTitle());
        assertEquals(threat1.getTitle(), threat2.getTitle());
        assertEquals(safeguard1.getImplementationStatus(), safeguard2.getImplementationStatus());
    }

    private String getPropertyValue(String entityTypeId, String propertyTypeId) {
        PropertyType propertyType = HitroUtil.getInstance().getTypeFactory()
                .getPropertyType(entityTypeId, propertyTypeId);
        if (!propertyType.isEditable())
            return null;

        switch (propertyType.getInputName()) {
        case "line":
        case "text":
            return "TestText";
        case "booleanoption":
            return "1";
        case "numericoption":
            return "2";
        default:
            break;
        }
        return null;
    }

    Entry<String, String> randomProperty(String typeId) {
        HUITypeFactory typeFactory = HitroUtil.getInstance().getTypeFactory();
        EntityType type = typeFactory.getEntityType(typeId);

        PropertyGroup propertyGroup = type.getPropertyGroups()
                .get(random.nextInt(type.getPropertyGroups().size()));
        String propertyGroupId = propertyGroup.getId();

        String propertyId = propertyGroup.getPropertyTypes()
                .get(random.nextInt(propertyGroup.getPropertyTypes().size())).getId();
        return new AbstractMap.SimpleEntry<>(propertyGroupId, propertyId);
    }
}