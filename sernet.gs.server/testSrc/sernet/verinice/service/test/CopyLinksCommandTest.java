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

import java.util.Collections;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Test;

import sernet.verinice.interfaces.CommandException;
import sernet.verinice.model.bp.elements.BpRequirement;
import sernet.verinice.model.bp.elements.ItNetwork;
import sernet.verinice.model.bp.elements.Room;
import sernet.verinice.model.bp.elements.Safeguard;
import sernet.verinice.model.bp.groups.BpRequirementGroup;
import sernet.verinice.model.bp.groups.RoomGroup;
import sernet.verinice.model.bp.groups.SafeguardGroup;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.service.commands.CopyLinksCommand;
import sernet.verinice.service.commands.CopyLinksCommand.CopyLinksMode;
import sernet.verinice.service.commands.RemoveElement;

public class CopyLinksCommandTest extends AbstractModernizedBaseProtection {

    private static final Logger logger = Logger.getLogger(CopyLinksCommandTest.class);

    private ItNetwork network;

    @After
    public void tearDown() throws CommandException {
        RemoveElement removeElementCmd = new RemoveElement(network);
        commandService.executeCommand(removeElementCmd);
    }

    @Test
    public void copyLinksFromSafeguard() throws Exception {
        network = createNewBPOrganization();
        SafeguardGroup safeguards = createGroup(network, SafeguardGroup.class);
        BpRequirementGroup requirements = createGroup(network, BpRequirementGroup.class);
        BpRequirement requirement1 = createBpRequirement(requirements);
        Safeguard safeguard1 = createSafeguard(safeguards);
        createLink(requirement1, safeguard1, BpRequirement.REL_BP_REQUIREMENT_BP_SAFEGUARD);
        Safeguard safeguard2 = createSafeguard(safeguards);
        requirement1 = reloadElement(requirement1);
        safeguard1 = reloadElement(safeguard1);
        safeguard2 = reloadElement(safeguard2);
        assertEquals(1, requirement1.getLinksDown().size());
        assertEquals(1, safeguard1.getLinksUp().size());
        assertEquals(0, safeguard2.getLinksUp().size());

        CopyLinksCommand copyLinksCommand = new CopyLinksCommand(
                Collections.singletonMap(safeguard1.getUuid(), safeguard2.getUuid()),
                CopyLinksMode.ALL);
        commandService.executeCommand(copyLinksCommand);
        requirement1 = reloadElement(requirement1);
        safeguard1 = reloadElement(safeguard1);
        safeguard2 = reloadElement(safeguard2);
        assertEquals(2, requirement1.getLinksDown().size());
        assertEquals(1, safeguard1.getLinksUp().size());
        assertEquals(1, safeguard2.getLinksUp().size());
    }

    @Test
    public void copyLinksFromRequirement() throws Exception {
        network = createNewBPOrganization();
        SafeguardGroup safeguards = createGroup(network, SafeguardGroup.class);
        BpRequirementGroup requirements = createGroup(network, BpRequirementGroup.class);
        BpRequirement requirement1 = createBpRequirement(requirements);
        Safeguard safeguard1 = createSafeguard(safeguards);
        createLink(requirement1, safeguard1, BpRequirement.REL_BP_REQUIREMENT_BP_SAFEGUARD);
        BpRequirement requirement2 = createBpRequirement(requirements);
        requirement1 = reloadElement(requirement1);
        safeguard1 = reloadElement(safeguard1);
        requirement2 = reloadElement(requirement2);
        assertEquals(1, requirement1.getLinksDown().size());
        assertEquals(1, safeguard1.getLinksUp().size());
        assertEquals(0, requirement2.getLinksDown().size());

        CopyLinksCommand copyLinksCommand = new CopyLinksCommand(
                Collections.singletonMap(requirement1.getUuid(), requirement2.getUuid()),
                CopyLinksMode.ALL);
        commandService.executeCommand(copyLinksCommand);
        requirement1 = reloadElement(requirement1);
        safeguard1 = reloadElement(safeguard1);
        requirement2 = reloadElement(requirement2);
        assertEquals(1, requirement1.getLinksDown().size());
        assertEquals(2, safeguard1.getLinksUp().size());
        assertEquals(1, requirement2.getLinksDown().size());
    }

    @Test
    public void copyLinksFromRoomWithLinkToItself() throws Exception {
        network = createNewBPOrganization();
        RoomGroup rooms = createGroup(network, RoomGroup.class);
        Room room1 = createElement(rooms, Room.class);
        createLink(room1, room1, "rel_bp_room_bp_room");
        Room room2 = createElement(rooms, Room.class);
        room1 = reloadElement(room1);
        room2 = reloadElement(room2);
        assertEquals(1, room1.getLinksDown().size());
        assertEquals(1, room1.getLinksUp().size());
        assertEquals(room1, room1.getLinksDown().iterator().next().getDependency());
        assertEquals(0, room2.getLinksDown().size());
        assertEquals(0, room2.getLinksUp().size());

        CopyLinksCommand copyLinksCommand = new CopyLinksCommand(
                Collections.singletonMap(room1.getUuid(), room2.getUuid()), CopyLinksMode.ALL);
        commandService.executeCommand(copyLinksCommand);
        room1 = reloadElement(room1);
        room2 = reloadElement(room2);
        assertEquals(1, room1.getLinksDown().size());
        assertEquals(1, room1.getLinksUp().size());
        assertEquals(1, room2.getLinksDown().size());
        assertEquals(1, room2.getLinksUp().size());
        assertEquals(room2, room2.getLinksDown().iterator().next().getDependency());
    }

    @Test
    public void copyLinkWithComment() throws Exception {
        network = createNewBPOrganization();
        SafeguardGroup safeguards = createGroup(network, SafeguardGroup.class);
        BpRequirementGroup requirements = createGroup(network, BpRequirementGroup.class);
        BpRequirement requirement1 = createBpRequirement(requirements);
        Safeguard safeguard1 = createSafeguard(safeguards);
        createLink(requirement1, safeguard1, BpRequirement.REL_BP_REQUIREMENT_BP_SAFEGUARD,
                "Important link");
        Safeguard safeguard2 = createSafeguard(safeguards);
        requirement1 = reloadElement(requirement1);
        safeguard1 = reloadElement(safeguard1);
        safeguard2 = reloadElement(safeguard2);
        assertEquals(1, requirement1.getLinksDown().size());
        assertEquals(1, safeguard1.getLinksUp().size());
        assertEquals(0, safeguard2.getLinksUp().size());

        CopyLinksCommand copyLinksCommand = new CopyLinksCommand(
                Collections.singletonMap(safeguard1.getUuid(), safeguard2.getUuid()),
                CopyLinksMode.ALL);
        commandService.executeCommand(copyLinksCommand);
        requirement1 = reloadElement(requirement1);
        safeguard1 = reloadElement(safeguard1);
        safeguard2 = reloadElement(safeguard2);
        assertEquals(2, requirement1.getLinksDown().size());
        assertEquals(1, safeguard1.getLinksUp().size());
        assertEquals(1, safeguard2.getLinksUp().size());
        assertEquals(safeguard1.getLinksUp().iterator().next().getComment(),
                safeguard2.getLinksUp().iterator().next().getComment());
    }

}
