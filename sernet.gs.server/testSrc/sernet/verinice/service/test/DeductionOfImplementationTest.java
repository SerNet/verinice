/*******************************************************************************
 * Copyright (c) 2017 Urs Zeidler.
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
 *     Urs Zeidler uz[at]sernet.de - initial API and implementation
 ******************************************************************************/
package sernet.verinice.service.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static sernet.verinice.model.bp.DeductionImplementationUtil.IMPLEMENTATION_DEDUCE;
import static sernet.verinice.model.bp.DeductionImplementationUtil.getComputedImplementationStatus;
import static sernet.verinice.model.bp.DeductionImplementationUtil.getImplementationStatus;
import static sernet.verinice.model.bp.DeductionImplementationUtil.setImplementationStatusToRequirement;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import sernet.verinice.interfaces.CommandException;
import sernet.verinice.model.bp.DeductionImplementationUtil;
import sernet.verinice.model.bp.ImplementationStatus;
import sernet.verinice.model.bp.elements.BpRequirement;
import sernet.verinice.model.bp.elements.ItNetwork;
import sernet.verinice.model.bp.elements.Safeguard;
import sernet.verinice.model.bp.groups.BpRequirementGroup;
import sernet.verinice.model.bp.groups.SafeguardGroup;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.service.commands.CreateLink;
import sernet.verinice.service.commands.RemoveElement;
import sernet.verinice.service.commands.RemoveLink;
import sernet.verinice.service.commands.UpdateElement;

/**
 * Test the deduction of the implementation for a requirement. A
 * {@link BpRequirement} can deduce the implementation status from a linked
 * {@link Safeguard} when the property 'xxx_implementation_deduce' is set.
 *
 * @author uz[at]sernet.de
 *
 */
@TransactionConfiguration(transactionManager = "txManager", defaultRollback = false)
@Transactional
public class DeductionOfImplementationTest extends AbstractModernizedBaseProtection {
    private static final Logger LOG = Logger.getLogger(DeductionOfImplementationTest.class);

    /**
     * Generic dataholder.
     *
     * @author uz[at]sernet.de
     *
     * @param <A>
     * @param <B>
     */
    private class Duo<A, B> {
        A a;
        B b;

        public Duo(A a, B b) {
            super();
            this.a = a;
            this.b = b;
        }
    }

    /**
     * Test the util method.
     *
     * @throws CommandException
     */
    @Transactional
    @Rollback(true)
    @Test
    public void testSetImplementationStatusToRequirement() throws CommandException {
        Duo<Safeguard, BpRequirement> duo = createTestElements();
        Safeguard safeguard = duo.a;
        BpRequirement requirement = duo.b;

        updateSafeguard(safeguard, ImplementationStatus.NO);
        prepareRequirement(requirement);

        assertTrue(setImplementationStatusToRequirement(safeguard, requirement));
        assertFalse(setImplementationStatusToRequirement(safeguard, requirement));

        updateSafeguard(safeguard, ImplementationStatus.NOT_APPLICABLE);
        assertEquals(ImplementationStatus.NO, requirement.getImplementationStatus());

        assertTrue(setImplementationStatusToRequirement(safeguard, requirement));
        assertEquals(ImplementationStatus.NOT_APPLICABLE, requirement.getImplementationStatus());

        updateSafeguard(safeguard, null);
        assertEquals(ImplementationStatus.NOT_APPLICABLE, requirement.getImplementationStatus());

        assertTrue(setImplementationStatusToRequirement(safeguard, requirement));
        assertEquals(null, requirement.getImplementationStatus());

    }

    /**
     * Change the implementation_status after the link is created.
     *
     */
    @Transactional
    @Rollback(true)
    @Test
    public void testDefaultDeductionAfterLink() throws CommandException {
        Duo<Safeguard, BpRequirement> duo = createTestElements();
        Safeguard safeguard = duo.a;
        BpRequirement requirement = duo.b;

        CreateLink<CnATreeElement, CnATreeElement> createLink = new CreateLink<CnATreeElement, CnATreeElement>(
                requirement, safeguard, BpRequirement.REL_BP_REQUIREMENT_BP_SAFEGUARD, null);
        createLink = commandService.executeCommand(createLink);
        assertDeduction(safeguard, requirement);
    }

    /**
     * Change the implementation_status after the link is created. Opposite link
     * direction.
     *
     */
    @Transactional
    @Rollback(true)
    @Test
    public void testDefaultDeductionAfterLinkOppositeDirection() throws CommandException {
        Duo<Safeguard, BpRequirement> duo = createTestElements();
        Safeguard safeguard = duo.a;
        BpRequirement requirement = duo.b;

        CreateLink<CnATreeElement, CnATreeElement> createLink = new CreateLink<CnATreeElement, CnATreeElement>(
                requirement, safeguard, BpRequirement.REL_BP_REQUIREMENT_BP_SAFEGUARD, null);
        createLink = commandService.executeCommand(createLink);
        assertDeduction(safeguard, requirement);
    }

    /**
     * Change the implementation_status before the link is created.
     *
     */
    @Transactional
    @Rollback(true)
    @Test
    public void testDefaultDeductionBeforeLink() throws CommandException {
        Duo<Safeguard, BpRequirement> duo = createTestElements();
        Safeguard safeguard = duo.a;
        BpRequirement requirement = duo.b;

        if (LOG.isDebugEnabled()) {
            LOG.debug("Change the safeguard implementation status to: " + ImplementationStatus.NO);
        }
        safeguard = updateSafeguard(safeguard, ImplementationStatus.NO);

        CreateLink<CnATreeElement, CnATreeElement> createLink = new CreateLink<CnATreeElement, CnATreeElement>(
                requirement, safeguard, BpRequirement.REL_BP_REQUIREMENT_BP_SAFEGUARD, null);
        createLink = commandService.executeCommand(createLink);

        assertEquals("Must be option 'no'.", ImplementationStatus.NO,
                requirement.getImplementationStatus());

        if (LOG.isDebugEnabled()) {
            LOG.debug("Change the safeguard implementation status to: " + ImplementationStatus.YES);
        }
        safeguard = updateSafeguard(safeguard, ImplementationStatus.YES);
        assertEquals("Must be option 'yes'.", ImplementationStatus.YES,
                requirement.getImplementationStatus());

        if (LOG.isDebugEnabled()) {
            LOG.debug("Change the safeguard implementation status to: "
                    + ImplementationStatus.NOT_APPLICABLE);
        }
        safeguard = updateSafeguard(safeguard, ImplementationStatus.NOT_APPLICABLE);
        assertEquals("Must be option 'not applicable'.", ImplementationStatus.NOT_APPLICABLE,
                requirement.getImplementationStatus());
    }

    /**
     * Change the implementation_status before the link is created. Opposite
     * link direction.
     *
     */
    @Transactional
    @Rollback(true)
    @Test
    public void testDefaultDeductionBeforeLinkOppositeDirection() throws CommandException {
        Duo<Safeguard, BpRequirement> duo = createTestElements();
        Safeguard safeguard = duo.a;
        BpRequirement requirement = duo.b;

        if (LOG.isDebugEnabled()) {
            LOG.debug("Change the safeguard implementation status to: " + ImplementationStatus.NO);
        }
        safeguard = updateSafeguard(safeguard, ImplementationStatus.NO);

        CreateLink<CnATreeElement, CnATreeElement> createLink = new CreateLink<CnATreeElement, CnATreeElement>(
                requirement, safeguard, BpRequirement.REL_BP_REQUIREMENT_BP_SAFEGUARD, null);
        createLink = commandService.executeCommand(createLink);

        assertEquals("Must be option 'no'.", ImplementationStatus.NO,
                requirement.getImplementationStatus());

        if (LOG.isDebugEnabled()) {
            LOG.debug("Change the safeguard implementation status to: " + ImplementationStatus.YES);
        }
        safeguard = updateSafeguard(safeguard, ImplementationStatus.YES);
        assertEquals("Must be option 'yes'.", ImplementationStatus.YES,
                requirement.getImplementationStatus());

        if (LOG.isDebugEnabled()) {
            LOG.debug("Change the safeguard implementation status to: "
                    + ImplementationStatus.NOT_APPLICABLE);
        }
        safeguard = updateSafeguard(safeguard, ImplementationStatus.NOT_APPLICABLE);
        assertEquals("Must be option 'not applicable'.", ImplementationStatus.NOT_APPLICABLE,
                requirement.getImplementationStatus());
    }

    /**
     * Switch the deduction off.
     *
     */
    @Transactional
    @Rollback(true)
    @Test
    public void testDefaultDeductionSwitchedOff() throws CommandException {
        Duo<Safeguard, BpRequirement> duo = createTestElements();
        Safeguard safeguard = duo.a;
        BpRequirement requirement = duo.b;

        if (LOG.isDebugEnabled()) {
            LOG.debug("Change the safeguard implementation status to: " + ImplementationStatus.NO);
        }
        safeguard = updateSafeguard(safeguard, ImplementationStatus.NO);

        CreateLink<CnATreeElement, CnATreeElement> createLink = new CreateLink<CnATreeElement, CnATreeElement>(
                requirement, safeguard, BpRequirement.REL_BP_REQUIREMENT_BP_SAFEGUARD, null);
        createLink = commandService.executeCommand(createLink);
        assertDisabledDeduction(safeguard, requirement);
    }

    /**
     * Switch the deduction off. Opposite link direction.
     *
     */
    @Transactional
    @Rollback(true)
    @Test
    public void testDefaultDeductionSwitchedOffOppositeDirection() throws CommandException {
        Duo<Safeguard, BpRequirement> duo = createTestElements();
        Safeguard safeguard = duo.a;
        BpRequirement requirement = duo.b;

        if (LOG.isDebugEnabled()) {
            LOG.debug("Change the safeguard implementation status to: " + ImplementationStatus.NO);
        }
        safeguard = updateSafeguard(safeguard, ImplementationStatus.NO);

        CreateLink<CnATreeElement, CnATreeElement> createLink = new CreateLink<CnATreeElement, CnATreeElement>(
                requirement, safeguard, BpRequirement.REL_BP_REQUIREMENT_BP_SAFEGUARD, null);
        createLink = commandService.executeCommand(createLink);
        assertDisabledDeduction(safeguard, requirement);
    }

    @Transactional
    @Rollback(true)
    @Test
    public void testDeductionWorksWhenRemovingLink() throws CommandException {
        ItNetwork itNetwork = createNewBPOrganization();

        BpRequirementGroup requirementGroup = createRequirementGroup(itNetwork);
        CnATreeElement requirement = createBpRequirement(requirementGroup);
        requirement = prepareRequirement((BpRequirement) requirement);

        SafeguardGroup safeguardGroup = createSafeguardGroup(itNetwork);
        Safeguard safeguard1 = createSafeguard(safeguardGroup);
        safeguard1 = updateSafeguard(safeguard1, ImplementationStatus.NO);
        Safeguard safeguard2 = createSafeguard(safeguardGroup);
        safeguard2 = updateSafeguard(safeguard2, ImplementationStatus.YES);

        CnALink link1 = createLink(requirement, safeguard1,
                BpRequirement.REL_BP_REQUIREMENT_BP_SAFEGUARD);
        createLink(requirement, safeguard2, BpRequirement.REL_BP_REQUIREMENT_BP_SAFEGUARD);
        assertEquals("Must be option 'partially'.", ImplementationStatus.PARTIALLY,
                getImplementationStatus(requirement));
        elementDao.flush();
        elementDao.clear();
        RemoveLink removeLink = new RemoveLink(link1);
        removeLink = commandService.executeCommand(removeLink);

        requirement = reloadElement(requirement);
        assertEquals("Must be option 'yes'.", ImplementationStatus.YES,
                getImplementationStatus(requirement));

    }

    @Transactional
    @Rollback(true)
    @Test
    public void testDeductionWorksWhenRemovingSafeguard() throws CommandException {
        ItNetwork itNetwork = createNewBPOrganization();

        BpRequirementGroup requirementGroup = createRequirementGroup(itNetwork);
        CnATreeElement requirement = createBpRequirement(requirementGroup);
        requirement = prepareRequirement((BpRequirement) requirement);

        SafeguardGroup safeguardGroup = createSafeguardGroup(itNetwork);
        Safeguard safeguard1 = createSafeguard(safeguardGroup);
        safeguard1 = updateSafeguard(safeguard1, ImplementationStatus.NO);
        Safeguard safeguard2 = createSafeguard(safeguardGroup);
        safeguard2 = updateSafeguard(safeguard2, ImplementationStatus.NOT_APPLICABLE);

        createLink(requirement, safeguard1, BpRequirement.REL_BP_REQUIREMENT_BP_SAFEGUARD);
        createLink(requirement, safeguard2, BpRequirement.REL_BP_REQUIREMENT_BP_SAFEGUARD);
        assertEquals("Must be option 'no'.", ImplementationStatus.NO,
                getImplementationStatus(requirement));
        elementDao.flush();
        elementDao.clear();

        RemoveElement<Safeguard> removeSafeguard = new RemoveElement<>(safeguard1);

        removeSafeguard = commandService.executeCommand(removeSafeguard);

        requirement = reloadElement(requirement);
        assertEquals("Must be option 'n/a'.", ImplementationStatus.NOT_APPLICABLE,
                getImplementationStatus(requirement));

    }

    @Transactional
    @Rollback(true)
    @Test
    public void testDeductionWorksWhenRemovingSafeguardGroup() throws CommandException {
        ItNetwork itNetwork = createNewBPOrganization();

        BpRequirementGroup requirementGroup = createRequirementGroup(itNetwork);
        CnATreeElement requirement = createBpRequirement(requirementGroup);
        requirement = prepareRequirement((BpRequirement) requirement);

        SafeguardGroup safeguardGroup1 = createSafeguardGroup(itNetwork);
        Safeguard safeguard1 = createSafeguard(safeguardGroup1);
        safeguard1 = updateSafeguard(safeguard1, ImplementationStatus.NO);
        SafeguardGroup safeguardGroup2 = createSafeguardGroup(itNetwork);
        Safeguard safeguard2 = createSafeguard(safeguardGroup2);
        safeguard2 = updateSafeguard(safeguard2, ImplementationStatus.YES);

        createLink(requirement, safeguard1, BpRequirement.REL_BP_REQUIREMENT_BP_SAFEGUARD);
        createLink(requirement, safeguard2, BpRequirement.REL_BP_REQUIREMENT_BP_SAFEGUARD);
        assertEquals("Must be option 'partially'.", ImplementationStatus.PARTIALLY,
                getImplementationStatus(requirement));

        RemoveElement<Safeguard> removeSafeguardgroup = new RemoveElement<>(safeguardGroup2);
        elementDao.flush();
        elementDao.clear();
        removeSafeguardgroup = commandService.executeCommand(removeSafeguardgroup);

        requirement = reloadElement(requirement);
        assertEquals("Must be option 'no'.", ImplementationStatus.NO,
                getImplementationStatus(requirement));

    }

    /**
     * Two requirements linked to one safeguard. Opposite link direction.
     */
    @Transactional
    @Rollback(true)
    @Test
    public void testOneSafeGuardTwoRequirements() throws Exception {
        ItNetwork itNetwork = createNewBPOrganization();

        BpRequirementGroup requirementGroup = createRequirementGroup(itNetwork);
        BpRequirement requirement1 = createBpRequirement(requirementGroup);
        BpRequirement requirement2 = createBpRequirement(requirementGroup);
        SafeguardGroup safeguardGroup = createSafeguardGroup(itNetwork);
        Safeguard safeguard = createSafeguard(safeguardGroup);
        requirement1 = prepareRequirement(requirement1);
        requirement2 = prepareRequirement(requirement2);

        createLink(requirement1, safeguard, BpRequirement.REL_BP_REQUIREMENT_BP_SAFEGUARD);
        createLink(requirement2, safeguard, BpRequirement.REL_BP_REQUIREMENT_BP_SAFEGUARD);

        safeguard = updateSafeguard(safeguard, ImplementationStatus.NO);
        assertDeduction(safeguard, requirement1);
        assertDeduction(safeguard, requirement2);
    }

    /**
     * Two requirements linked to one safeguard .
     */
    @Transactional
    @Rollback(true)
    @Test
    public void testOneSafeGuardTwoRequirementsOppositeDirection() throws Exception {
        ItNetwork itNetwork = createNewBPOrganization();

        BpRequirementGroup requirementGroup = createRequirementGroup(itNetwork);
        BpRequirement requirement1 = createBpRequirement(requirementGroup);
        BpRequirement requirement2 = createBpRequirement(requirementGroup);
        SafeguardGroup safeguardGroup = createSafeguardGroup(itNetwork);
        Safeguard safeguard = createSafeguard(safeguardGroup);
        requirement1 = prepareRequirement(requirement1);
        requirement2 = prepareRequirement(requirement2);

        createLink(requirement1, safeguard, BpRequirement.REL_BP_REQUIREMENT_BP_SAFEGUARD);
        createLink(requirement2, safeguard, BpRequirement.REL_BP_REQUIREMENT_BP_SAFEGUARD);

        safeguard = updateSafeguard(safeguard, ImplementationStatus.NO);
        assertDeduction(safeguard, requirement1);
        assertDeduction(safeguard, requirement2);
    }

    /**
     * Test one requirement and n safeguards, safeguard with all the same value.
     *
     * @throws Exception
     */
    @Transactional
    @Rollback(true)
    @Test
    public void testOneRequirementNSafeguards() throws Exception {
        Duo<BpRequirement, List<Safeguard>> duo = createNSafeguards(5);
        BpRequirement requirement = duo.a;
        List<Safeguard> safeGuards = duo.b;
        assertNull("Must be unset", requirement.getImplementationStatus());

        safeGuards = updateSafeguards(safeGuards, ImplementationStatus.NO);
        assertEquals("Must be option 'no'.", ImplementationStatus.NO,
                requirement.getImplementationStatus());

        safeGuards = updateSafeguards(safeGuards, ImplementationStatus.YES);
        assertEquals("Must be option 'yes'.", ImplementationStatus.YES,
                requirement.getImplementationStatus());

        safeGuards = updateSafeguards(safeGuards, ImplementationStatus.PARTIALLY);
        assertEquals("Must be option 'partial'.", ImplementationStatus.PARTIALLY,
                requirement.getImplementationStatus());

        safeGuards = updateSafeguards(safeGuards, ImplementationStatus.NOT_APPLICABLE);
        assertEquals("Must be option 'not applicable'.", ImplementationStatus.NOT_APPLICABLE,
                requirement.getImplementationStatus());
    }

    /**
     * Test one requirement and n safeguards, some safeguard with yes all others
     * with na.
     *
     * @throws Exception
     */
    @Transactional
    @Rollback(true)
    @Test
    public void testOneRequirementNSafeguards_Yes() throws Exception {
        Duo<BpRequirement, List<Safeguard>> duo = createNSafeguards(5);
        BpRequirement requirement = duo.a;
        List<Safeguard> safeGuards = duo.b;
        assertNull("Must be unset", requirement.getImplementationStatus());

        safeGuards = updateSafeguards(safeGuards, ImplementationStatus.YES);
        assertEquals("Must be option 'yes'.", ImplementationStatus.YES,
                requirement.getImplementationStatus());

        Safeguard safeGuard = updateSafeguard(safeGuards.get(3),
                ImplementationStatus.NOT_APPLICABLE);
        safeGuards.set(3, safeGuard);
        safeGuard = updateSafeguard(safeGuards.get(4), ImplementationStatus.NOT_APPLICABLE);
        safeGuards.set(4, safeGuard);
        assertEquals("Must be option 'yes'.", ImplementationStatus.YES,
                requirement.getImplementationStatus());

        safeGuards = updateSafeguards(safeGuards, ImplementationStatus.NOT_APPLICABLE);
        assertEquals("Must be option 'na'.", ImplementationStatus.NOT_APPLICABLE,
                requirement.getImplementationStatus());

        safeGuard = updateSafeguard(safeGuards.get(0), ImplementationStatus.YES);
        safeGuards.set(0, safeGuard);
        safeGuard = updateSafeguard(safeGuards.get(1), ImplementationStatus.YES);
        safeGuards.set(1, safeGuard);
        safeGuard = updateSafeguard(safeGuards.get(2), ImplementationStatus.YES);
        safeGuards.set(2, safeGuard);
        assertEquals("Must be option 'yes'.", ImplementationStatus.YES,
                requirement.getImplementationStatus());
    }

    /**
     * Test one requirement and n safeguards, some safeguard with no all others
     * with na.
     *
     * @throws Exception
     */
    @Transactional
    @Rollback(true)
    @Test
    public void testOneRequirementNSafeguards_No() throws Exception {
        Duo<BpRequirement, List<Safeguard>> duo = createNSafeguards(5);
        BpRequirement requirement = duo.a;
        List<Safeguard> safeGuards = duo.b;
        assertNull("Must be unset", requirement.getImplementationStatus());

        safeGuards = updateSafeguards(safeGuards, ImplementationStatus.NOT_APPLICABLE);
        assertEquals("Must be option 'na'.", ImplementationStatus.NOT_APPLICABLE,
                requirement.getImplementationStatus());

        Safeguard safeGuard = updateSafeguard(safeGuards.get(0), ImplementationStatus.NO);
        safeGuards.set(0, safeGuard);
        safeGuard = updateSafeguard(safeGuards.get(1), ImplementationStatus.NO);
        safeGuards.set(1, safeGuard);
        safeGuard = updateSafeguard(safeGuards.get(2), ImplementationStatus.NO);
        safeGuards.set(2, safeGuard);
        assertEquals("Must be option 'no'.", ImplementationStatus.NO,
                requirement.getImplementationStatus());

        safeGuard = updateSafeguard(safeGuards.get(1), ImplementationStatus.NOT_APPLICABLE);
        safeGuards.set(1, safeGuard);
        assertEquals("Must be option 'no'.", ImplementationStatus.NO,
                requirement.getImplementationStatus());

        safeGuard = updateSafeguard(safeGuards.get(0), ImplementationStatus.NOT_APPLICABLE);
        safeGuards.set(0, safeGuard);
        assertEquals("Must be option 'no'.", ImplementationStatus.NO,
                getImplementationStatus(requirement));

        safeGuard = updateSafeguard(safeGuards.get(1), ImplementationStatus.NO);
        safeGuards.set(1, safeGuard);
        safeGuard = updateSafeguard(safeGuards.get(0), ImplementationStatus.YES);
        safeGuards.set(0, safeGuard);
        assertEquals("Must be option 'no'.", ImplementationStatus.NO,
                getImplementationStatus(requirement));
    }

    /**
     * Test one requirement and n safeguards, some safeguard with no all others
     * with na.
     *
     * @throws Exception
     */
    @Transactional
    @Rollback(true)
    @Test
    public void testOneRequirementNSafeguards_No_with_no_not_applicable() throws Exception {
        Duo<BpRequirement, List<Safeguard>> duo = createNSafeguards(5);
        BpRequirement requirement = duo.a;
        List<Safeguard> safeGuards = duo.b;
        assertNull("Must be unset", getImplementationStatus(requirement));

        safeGuards = updateSafeguards(safeGuards, ImplementationStatus.NOT_APPLICABLE);
        assertEquals("Must be option 'na'.", ImplementationStatus.NOT_APPLICABLE,
                getImplementationStatus(requirement));

        Safeguard safeGuard = updateSafeguard(safeGuards.get(0), ImplementationStatus.NO);
        safeGuards.set(0, safeGuard);
        assertEquals("Must be option 'no'.", ImplementationStatus.NO,
                getImplementationStatus(requirement));

        safeGuard = updateSafeguard(safeGuards.get(1), ImplementationStatus.NO);
        safeGuards.set(1, safeGuard);
        assertEquals("Must be option 'no'.", ImplementationStatus.NO,
                getImplementationStatus(requirement));
    }

    /**
     * Test one requirement and n safeguards, some safeguard with no all others
     * with na.
     *
     * @throws Exception
     */
    @Transactional
    @Rollback(true)
    @Test
    public void testOneRequirementNSafeguards_NoHalf() throws Exception {
        Duo<BpRequirement, List<Safeguard>> duo = createNSafeguards(10);
        BpRequirement requirement = duo.a;
        List<Safeguard> safeGuards = duo.b;
        assertNull("Must be unset", requirement.getImplementationStatus());

        safeGuards = updateSafeguards(safeGuards, ImplementationStatus.NOT_APPLICABLE);
        assertEquals("Must be option 'na'.", ImplementationStatus.NOT_APPLICABLE,
                requirement.getImplementationStatus());

        Safeguard safeGuard = updateSafeguard(safeGuards.get(0), ImplementationStatus.NO);
        safeGuards.set(0, safeGuard);
        safeGuard = updateSafeguard(safeGuards.get(1), ImplementationStatus.NO);
        safeGuards.set(1, safeGuard);
        safeGuard = updateSafeguard(safeGuards.get(2), ImplementationStatus.NO);
        safeGuards.set(2, safeGuard);
        safeGuard = updateSafeguard(safeGuards.get(3), ImplementationStatus.PARTIALLY);
        safeGuards.set(3, safeGuard);
        safeGuard = updateSafeguard(safeGuards.get(4), ImplementationStatus.PARTIALLY);
        safeGuards.set(4, safeGuard);
        assertEquals("Must be option 'no'.", ImplementationStatus.NO,
                requirement.getImplementationStatus());// 3/5->no

        safeGuard = updateSafeguard(safeGuards.get(2), ImplementationStatus.YES);
        safeGuards.set(2, safeGuard);
        assertEquals("Must be option 'partially'.", ImplementationStatus.PARTIALLY,
                requirement.getImplementationStatus());// 2/5->pa

        safeGuard = updateSafeguard(safeGuards.get(5), ImplementationStatus.NO);
        safeGuards.set(5, safeGuard);
        assertEquals("Must be option 'no'.", ImplementationStatus.PARTIALLY,
                requirement.getImplementationStatus());// 3/6->pa

        safeGuard = updateSafeguard(safeGuards.get(6), ImplementationStatus.NO);
        safeGuards.set(6, safeGuard);
        assertEquals("Must be option 'no'.", ImplementationStatus.NO,
                requirement.getImplementationStatus());// 4/7->no

        safeGuard = updateSafeguard(safeGuards.get(6), ImplementationStatus.NOT_APPLICABLE);
        safeGuards.set(6, safeGuard);
        assertEquals("Must be option 'partially'.", ImplementationStatus.PARTIALLY,
                requirement.getImplementationStatus());// 3/6->pa

        safeGuard = updateSafeguard(safeGuards.get(2), ImplementationStatus.NO);
        safeGuards.set(2, safeGuard);
        assertEquals("Must be option 'no'.", ImplementationStatus.NO,
                requirement.getImplementationStatus());// 4/6->no
    }

    @Transactional
    @Rollback(true)
    @Test
    public void testThreeSafeguard() {

        List<CnATreeElement> safeGuards = createSafeguards(null, null, null);
        ImplementationStatus implementationStatus = getComputedImplementationStatus(safeGuards);
        assertEquals(null, implementationStatus);

        safeGuards = createSafeguards(ImplementationStatus.NO, ImplementationStatus.NO,
                ImplementationStatus.NO);
        implementationStatus = getComputedImplementationStatus(safeGuards);
        assertEquals(ImplementationStatus.NO, implementationStatus);

        safeGuards = createSafeguards(ImplementationStatus.YES, ImplementationStatus.YES,
                ImplementationStatus.NO);
        implementationStatus = getComputedImplementationStatus(safeGuards);
        assertEquals(ImplementationStatus.PARTIALLY, implementationStatus);

        safeGuards = createSafeguards(ImplementationStatus.YES, ImplementationStatus.NO,
                ImplementationStatus.NO);
        implementationStatus = getComputedImplementationStatus(safeGuards);
        assertEquals(ImplementationStatus.NO, implementationStatus);

        safeGuards = createSafeguards(ImplementationStatus.YES, ImplementationStatus.NO,
                ImplementationStatus.NOT_APPLICABLE);
        implementationStatus = getComputedImplementationStatus(safeGuards);
        assertEquals(ImplementationStatus.PARTIALLY, implementationStatus);
    }

    @Transactional
    @Rollback(true)
    @Test
    public void testFiveSafeguard() {

        List<CnATreeElement> safeGuards = createSafeguards(null, null, null, null, null);
        ImplementationStatus implementationStatus = getComputedImplementationStatus(safeGuards);
        assertEquals(null, implementationStatus);

        safeGuards = createSafeguards(ImplementationStatus.NO, ImplementationStatus.NO,
                ImplementationStatus.NO, ImplementationStatus.NO, ImplementationStatus.NO);
        implementationStatus = getComputedImplementationStatus(safeGuards);
        assertEquals(ImplementationStatus.NO, implementationStatus);

        safeGuards = createSafeguards(ImplementationStatus.YES, ImplementationStatus.NO,
                ImplementationStatus.NO, ImplementationStatus.NOT_APPLICABLE,
                ImplementationStatus.NOT_APPLICABLE);
        implementationStatus = getComputedImplementationStatus(safeGuards);
        assertEquals(ImplementationStatus.NO, implementationStatus);

        safeGuards = createSafeguards(ImplementationStatus.YES, ImplementationStatus.NO,
                ImplementationStatus.NO, ImplementationStatus.YES,
                ImplementationStatus.NOT_APPLICABLE);
        implementationStatus = getComputedImplementationStatus(safeGuards);
        assertEquals(ImplementationStatus.PARTIALLY, implementationStatus);

        safeGuards = createSafeguards(ImplementationStatus.YES, ImplementationStatus.YES,
                ImplementationStatus.NOT_APPLICABLE, ImplementationStatus.NOT_APPLICABLE,
                ImplementationStatus.NOT_APPLICABLE);
        implementationStatus = getComputedImplementationStatus(safeGuards);
        assertEquals(ImplementationStatus.YES, implementationStatus);

        safeGuards = createSafeguards(ImplementationStatus.YES, ImplementationStatus.YES,
                ImplementationStatus.NO, ImplementationStatus.NOT_APPLICABLE,
                ImplementationStatus.NOT_APPLICABLE);
        implementationStatus = getComputedImplementationStatus(safeGuards);
        assertEquals(ImplementationStatus.PARTIALLY, implementationStatus);
    }

    private List<CnATreeElement> createSafeguards(ImplementationStatus... implementationStatuses) {
        return Stream.of(implementationStatuses).map(status -> {
            Safeguard safeguard = new Safeguard(null);
            if (status != null) {
                safeguard.setImplementationStatus(status);
            }
            return safeguard;
        }).collect(Collectors.toList());
    }

    private List<Safeguard> updateSafeguards(List<Safeguard> safeGuards,
            ImplementationStatus implementationStatus) throws CommandException {
        List<Safeguard> list = new ArrayList<Safeguard>(safeGuards.size());
        for (Safeguard safeguard : safeGuards) {
            Safeguard updateSafeguard = updateSafeguard(safeguard, implementationStatus);
            list.add(updateSafeguard);
        }
        return list;
    }

    private Duo<BpRequirement, List<Safeguard>> createNSafeguards(int safeGuardNumber)
            throws CommandException {
        ItNetwork itNetwork = createNewBPOrganization();

        BpRequirementGroup requirementGroup = createRequirementGroup(itNetwork);
        BpRequirement requirement = createBpRequirement(requirementGroup);
        SafeguardGroup safeguardGroup = createSafeguardGroup(itNetwork);
        requirement = prepareRequirement(requirement);

        List<Safeguard> safeGuards = new ArrayList<>(safeGuardNumber);
        for (int i = 0; i < safeGuardNumber; i++) {
            Safeguard safeguard = createSafeguard(safeguardGroup);
            createLink(requirement, safeguard, BpRequirement.REL_BP_REQUIREMENT_BP_SAFEGUARD);
            safeguard = updateSafeguard(safeguard, null);
            safeGuards.add(safeguard);
        }
        return new Duo<BpRequirement, List<Safeguard>>(requirement, safeGuards);
    }

    /**
     * Create the test elements. Create a new it network and the necessary
     * groups for the target test objects. Returns the two objects under test.
     *
     */
    private Duo<Safeguard, BpRequirement> createTestElements() throws CommandException {
        ItNetwork itNetwork = createNewBPOrganization();

        BpRequirementGroup requirementGroup = createRequirementGroup(itNetwork);
        BpRequirement requirement = createBpRequirement(requirementGroup);
        SafeguardGroup safeguardGroup = createSafeguardGroup(itNetwork);
        Safeguard safeguard = createSafeguard(safeguardGroup);
        requirement = prepareRequirement(requirement);

        return new Duo<Safeguard, BpRequirement>(safeguard, requirement);
    }

    /**
     * Assert the deduction of the implementation value.
     *
     */
    private void assertDeduction(Safeguard safeguard, BpRequirement requirement)
            throws CommandException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Change the safeguard implementation status to: " + ImplementationStatus.NO);
        }
        updateSafeguard(safeguard, ImplementationStatus.NO);
        assertEquals("Must be option 'no'.", ImplementationStatus.NO,
                requirement.getImplementationStatus());

        if (LOG.isDebugEnabled()) {
            LOG.debug("Change the safeguard implementation status to: " + ImplementationStatus.YES);
        }
        safeguard = updateSafeguard(safeguard, ImplementationStatus.YES);
        assertEquals("Must be option 'yes'.", ImplementationStatus.YES,
                requirement.getImplementationStatus());

        if (LOG.isDebugEnabled()) {
            LOG.debug("Change the safeguard implementation status to: "
                    + ImplementationStatus.NOT_APPLICABLE);
        }
        safeguard = updateSafeguard(safeguard, ImplementationStatus.NOT_APPLICABLE);
        assertEquals("Must be option 'not applicable'.", ImplementationStatus.NOT_APPLICABLE,
                requirement.getImplementationStatus());
    }

    /**
     * Disables the deduction and assert the implementation state don't change.
     *
     */
    private void assertDisabledDeduction(Safeguard safeguard, BpRequirement requirement)
            throws CommandException {

        assertEquals("Must be option 'no'.", ImplementationStatus.NO,
                requirement.getImplementationStatus());

        if (LOG.isDebugEnabled()) {
            LOG.debug("Change the safeguard implementation status to: "
                    + ImplementationStatus.PARTIALLY);
        }
        safeguard = updateSafeguard(safeguard, ImplementationStatus.PARTIALLY);
        assertEquals("Must be option 'partially'.", ImplementationStatus.PARTIALLY,
                requirement.getImplementationStatus());

        if (LOG.isDebugEnabled()) {
            LOG.debug("Switch deduction off for the requirement.");
        }
        requirement.setPropertyValue(requirement.getTypeId() + IMPLEMENTATION_DEDUCE, "0");
        requirement.setImplementationStatus(ImplementationStatus.YES);
        UpdateElement<BpRequirement> command1 = new UpdateElement<>(requirement, true, null);
        commandService.executeCommand(command1);
        requirement = command1.getElement();

        if (LOG.isDebugEnabled()) {
            LOG.debug("Change the safeguard implementation status to: "
                    + ImplementationStatus.PARTIALLY);
        }
        safeguard = updateSafeguard(safeguard, ImplementationStatus.NO);
        assertEquals("Must be option 'yes'.", ImplementationStatus.YES,
                requirement.getImplementationStatus());
    }

    /**
     * Will set implementation_status and update the safeguard.
     *
     */
    private Safeguard updateSafeguard(Safeguard safeguard,
            ImplementationStatus implementationStatus) throws CommandException {

        safeguard.setImplementationStatus(implementationStatus);
        UpdateElement<Safeguard> command = new UpdateElement<>(safeguard, true, null);
        commandService.executeCommand(command);
        safeguard = command.getElement();

        return safeguard;
    }

    /**
     * Prepare a requirement for the test. The field 'implementation_status'
     * will be set to yes and deduction of the implementation is enabled.
     *
     */
    private BpRequirement prepareRequirement(BpRequirement requirement) throws CommandException {
        requirement.setImplementationStatus(ImplementationStatus.YES);
        UpdateElement<BpRequirement> command1 = new UpdateElement<>(requirement, true, null);
        commandService.executeCommand(command1);
        requirement = command1.getElement();
        assertEquals("Must be option 'yes'.", ImplementationStatus.YES,
                requirement.getImplementationStatus());

        assertTrue("Deduction should be enabled.",
                DeductionImplementationUtil.isDeductiveImplementationEnabled(requirement));
        return requirement;
    }

}
