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
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static sernet.verinice.model.bp.DeductionImplementationUtil.IMPLEMENTATION_DEDUCE;
import static sernet.verinice.model.bp.DeductionImplementationUtil.IMPLEMENTATION_STATUS;
import static sernet.verinice.model.bp.DeductionImplementationUtil.IMPLEMENTATION_STATUS_CODE_NO;
import static sernet.verinice.model.bp.DeductionImplementationUtil.IMPLEMENTATION_STATUS_CODE_NOT_APPLICABLE;
import static sernet.verinice.model.bp.DeductionImplementationUtil.IMPLEMENTATION_STATUS_CODE_PARTIALLY;
import static sernet.verinice.model.bp.DeductionImplementationUtil.IMPLEMENTATION_STATUS_CODE_YES;
import static sernet.verinice.model.bp.DeductionImplementationUtil.getImplementationStatus;
import static sernet.verinice.model.bp.DeductionImplementationUtil.getImplementationStatusId;
import static sernet.verinice.model.bp.DeductionImplementationUtil.setImplementationStausToRequirement;

import org.apache.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import sernet.verinice.interfaces.CommandException;
import sernet.verinice.model.bp.DeductionImplementationUtil;
import sernet.verinice.model.bp.elements.BpRequirement;
import sernet.verinice.model.bp.elements.ItNetwork;
import sernet.verinice.model.bp.elements.Safeguard;
import sernet.verinice.model.bp.groups.BpRequirementGroup;
import sernet.verinice.model.bp.groups.SafeguardGroup;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.service.commands.CreateLink;
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
    private class Dou<A, B> {
        A a;
        B b;

        public Dou(A a, B b) {
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
    public void testSetImplementationStausToRequirement() throws CommandException {
        Dou<Safeguard, BpRequirement> dou = createTestElements();
        Safeguard safeguard = dou.a;
        BpRequirement requirement = dou.b;

        updateSafeguard(safeguard, IMPLEMENTATION_STATUS_CODE_NO);
        prepareRequirement(requirement);

        assertTrue(setImplementationStausToRequirement(safeguard, requirement));
        assertFalse(setImplementationStausToRequirement(safeguard, requirement));

        updateSafeguard(safeguard, IMPLEMENTATION_STATUS_CODE_NOT_APPLICABLE);
        assertEquals(requirement.getTypeId() + IMPLEMENTATION_STATUS_CODE_NO,
                getImplementationStatus(requirement));
        assertTrue(setImplementationStausToRequirement(safeguard, requirement));
        assertEquals(requirement.getTypeId() + IMPLEMENTATION_STATUS_CODE_NOT_APPLICABLE,
                getImplementationStatus(requirement));

    }

    /**
     * Change the implementation_status after the link is created.
     *
     */
    @Transactional
    @Rollback(true)
    @Test
    public void testDefaultDeductionAfterLink() throws CommandException {
        Dou<Safeguard, BpRequirement> dou = createTestElements();
        Safeguard safeguard = dou.a;
        BpRequirement requirement = dou.b;

        CreateLink<CnATreeElement, CnATreeElement> createLink = new CreateLink<CnATreeElement, CnATreeElement>(
                requirement, safeguard, null, null);
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
        Dou<Safeguard, BpRequirement> dou = createTestElements();
        Safeguard safeguard = dou.a;
        BpRequirement requirement = dou.b;

        CreateLink<CnATreeElement, CnATreeElement> createLink = new CreateLink<CnATreeElement, CnATreeElement>(
                safeguard, requirement, null, null);
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
        Dou<Safeguard, BpRequirement> dou = createTestElements();
        Safeguard safeguard = dou.a;
        BpRequirement requirement = dou.b;

        if (LOG.isDebugEnabled()) {
            LOG.debug("Change the safeguard implementation status to: "
                    + IMPLEMENTATION_STATUS_CODE_NO);
        }
        safeguard = updateSafeguard(safeguard, IMPLEMENTATION_STATUS_CODE_NO);

        CreateLink<CnATreeElement, CnATreeElement> createLink = new CreateLink<CnATreeElement, CnATreeElement>(
                requirement, safeguard, null, null);
        createLink = commandService.executeCommand(createLink);

        assertEquals("Must be option no.", BpRequirement.TYPE_ID + IMPLEMENTATION_STATUS_CODE_NO,
                getImplementationStatus(requirement));

        if (LOG.isDebugEnabled()) {
            LOG.debug("Change the safeguard implementation status to: "
                    + IMPLEMENTATION_STATUS_CODE_YES);
        }
        safeguard = updateSafeguard(safeguard, IMPLEMENTATION_STATUS_CODE_YES);
        assertEquals("Must be option yes.", BpRequirement.TYPE_ID + IMPLEMENTATION_STATUS_CODE_YES,
                getImplementationStatus(requirement));

        if (LOG.isDebugEnabled()) {
            LOG.debug("Change the safeguard implementation status to: "
                    + IMPLEMENTATION_STATUS_CODE_NOT_APPLICABLE);
        }
        safeguard = updateSafeguard(safeguard, IMPLEMENTATION_STATUS_CODE_NOT_APPLICABLE);
        assertEquals("Must be option not applicable.", BpRequirement.TYPE_ID + IMPLEMENTATION_STATUS_CODE_NOT_APPLICABLE,
                getImplementationStatus(requirement));
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
        Dou<Safeguard, BpRequirement> dou = createTestElements();
        Safeguard safeguard = dou.a;
        BpRequirement requirement = dou.b;

        if (LOG.isDebugEnabled()) {
            LOG.debug("Change the safeguard implementation status to: "
                    + IMPLEMENTATION_STATUS_CODE_NO);
        }
        safeguard = updateSafeguard(safeguard, IMPLEMENTATION_STATUS_CODE_NO);

        CreateLink<CnATreeElement, CnATreeElement> createLink = new CreateLink<CnATreeElement, CnATreeElement>(
                safeguard, requirement, null, null);
        createLink = commandService.executeCommand(createLink);

        assertEquals("Must be option no.", BpRequirement.TYPE_ID + IMPLEMENTATION_STATUS_CODE_NO,
                getImplementationStatus(requirement));

        if (LOG.isDebugEnabled()) {
            LOG.debug("Change the safeguard implementation status to: "
                    + IMPLEMENTATION_STATUS_CODE_YES);
        }
        safeguard = updateSafeguard(safeguard, IMPLEMENTATION_STATUS_CODE_YES);
        assertEquals("Must be option yes.", BpRequirement.TYPE_ID + IMPLEMENTATION_STATUS_CODE_YES,
                getImplementationStatus(requirement));

        if (LOG.isDebugEnabled()) {
            LOG.debug("Change the safeguard implementation status to: "
                    + IMPLEMENTATION_STATUS_CODE_NOT_APPLICABLE);
        }
        safeguard = updateSafeguard(safeguard, IMPLEMENTATION_STATUS_CODE_NOT_APPLICABLE);
        assertEquals("Must be option not applicable.", BpRequirement.TYPE_ID + IMPLEMENTATION_STATUS_CODE_NOT_APPLICABLE,
                getImplementationStatus(requirement));
    }

    /**
     * Switch the deduction off.
     *
     */
    @Transactional
    @Rollback(true)
    @Test
    public void testDefaultDeductionSwitchedOff() throws CommandException {
        Dou<Safeguard, BpRequirement> dou = createTestElements();
        Safeguard safeguard = dou.a;
        BpRequirement requirement = dou.b;

        if (LOG.isDebugEnabled()) {
            LOG.debug("Change the safeguard implementation status to: "
                    + IMPLEMENTATION_STATUS_CODE_NO);
        }
        safeguard = updateSafeguard(safeguard, IMPLEMENTATION_STATUS_CODE_NO);

        CreateLink<CnATreeElement, CnATreeElement> createLink = new CreateLink<CnATreeElement, CnATreeElement>(
                requirement, safeguard, null, null);
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
        Dou<Safeguard, BpRequirement> dou = createTestElements();
        Safeguard safeguard = dou.a;
        BpRequirement requirement = dou.b;

        if (LOG.isDebugEnabled()) {
            LOG.debug("Change the safeguard implementation status to: "
                    + IMPLEMENTATION_STATUS_CODE_NO);
        }
        safeguard = updateSafeguard(safeguard, IMPLEMENTATION_STATUS_CODE_NO);

        CreateLink<CnATreeElement, CnATreeElement> createLink = new CreateLink<CnATreeElement, CnATreeElement>(
                safeguard, requirement, null, null);
        createLink = commandService.executeCommand(createLink);
        assertDisabledDeduction(safeguard, requirement);
    }

    @Transactional
    @Rollback(true)
    @Test
    @Ignore
    public void testDefaultDeductionBeforeLinkRemoveLink() throws CommandException {
        ItNetwork itNetwork = createNewBPOrganization();

        BpRequirementGroup requirementGroup = createRequirementGroup(itNetwork);
        BpRequirement requirement = createBpRequirement(requirementGroup);
        SafeguardGroup safeguardGroup = createSafeguardGroup(itNetwork);
        Safeguard safeguard = createSafeguard(safeguardGroup);
        requirement = prepareRequirement(requirement);
        safeguard = updateSafeguard(safeguard, IMPLEMENTATION_STATUS_CODE_NO);

        CreateLink<CnATreeElement, CnATreeElement> createLink = new CreateLink<CnATreeElement, CnATreeElement>(
                requirement, safeguard, null, null);
        createLink = commandService.executeCommand(createLink);

        assertEquals("Must be option no.", BpRequirement.TYPE_ID + IMPLEMENTATION_STATUS_CODE_NO,
                getImplementationStatus(requirement));

        RemoveLink removeLink = new RemoveLink(createLink.getLink());
        removeLink = commandService.executeCommand(removeLink);

        assertNotSame("", BpRequirement.TYPE_ID + IMPLEMENTATION_STATUS_CODE_NO,
                getImplementationStatus(requirement));
        assertEquals("Must be option yes.", BpRequirement.TYPE_ID + IMPLEMENTATION_STATUS_CODE_YES,
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

        createLink(requirement1, safeguard, null);
        createLink(requirement2, safeguard, null);

        safeguard = updateSafeguard(safeguard, IMPLEMENTATION_STATUS_CODE_NO);
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

        createLink(safeguard, requirement1, null);
        createLink(safeguard, requirement2, null);

        safeguard = updateSafeguard(safeguard, IMPLEMENTATION_STATUS_CODE_NO);
        assertDeduction(safeguard, requirement1);
        assertDeduction(safeguard, requirement2);
    }

    /**
     * Create the test elements. Create a new it network and the necessary
     * groups for the target test objects. Returns the two objects under test.
     *
     */
    private Dou<Safeguard, BpRequirement> createTestElements() throws CommandException {
        ItNetwork itNetwork = createNewBPOrganization();

        BpRequirementGroup requirementGroup = createRequirementGroup(itNetwork);
        BpRequirement requirement = createBpRequirement(requirementGroup);
        SafeguardGroup safeguardGroup = createSafeguardGroup(itNetwork);
        Safeguard safeguard = createSafeguard(safeguardGroup);
        requirement = prepareRequirement(requirement);

        return new Dou<Safeguard, BpRequirement>(safeguard, requirement);
    }

    /**
     * Assert the deduction of the implementation value.
     *
     */
    private void assertDeduction(Safeguard safeguard, BpRequirement requirement)
            throws CommandException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Change the safeguard implementation status to: "
                    + IMPLEMENTATION_STATUS_CODE_NO);
        }
        updateSafeguard(safeguard, IMPLEMENTATION_STATUS_CODE_NO);
        assertEquals("Must be option no.", BpRequirement.TYPE_ID + IMPLEMENTATION_STATUS_CODE_NO,
                getImplementationStatus(requirement));

        if (LOG.isDebugEnabled()) {
            LOG.debug("Change the safeguard implementation status to: "
                    + IMPLEMENTATION_STATUS_CODE_YES);
        }
        safeguard = updateSafeguard(safeguard, IMPLEMENTATION_STATUS_CODE_YES);
        assertEquals("Must be option yes.", BpRequirement.TYPE_ID + IMPLEMENTATION_STATUS_CODE_YES,
                getImplementationStatus(requirement));

        if (LOG.isDebugEnabled()) {
            LOG.debug("Change the safeguard implementation status to: "
                    + IMPLEMENTATION_STATUS_CODE_NOT_APPLICABLE);
        }
        safeguard = updateSafeguard(safeguard, IMPLEMENTATION_STATUS_CODE_NOT_APPLICABLE);
        assertEquals("Must be option not applicable.", BpRequirement.TYPE_ID + IMPLEMENTATION_STATUS_CODE_NOT_APPLICABLE,
                getImplementationStatus(requirement));
    }

    /**
     * Disables the deduction and assert the implementation state don't change.
     *
     */
    private void assertDisabledDeduction(Safeguard safeguard, BpRequirement requirement)
            throws CommandException {

        assertEquals("Must be option no.", BpRequirement.TYPE_ID + IMPLEMENTATION_STATUS_CODE_NO,
                getImplementationStatus(requirement));

        if (LOG.isDebugEnabled()) {
            LOG.debug("Change the safeguard implementation status to: "
                    + IMPLEMENTATION_STATUS_CODE_PARTIALLY);
        }
        safeguard = updateSafeguard(safeguard, IMPLEMENTATION_STATUS_CODE_PARTIALLY);
        assertEquals("Must be option partially.",
                BpRequirement.TYPE_ID + IMPLEMENTATION_STATUS_CODE_PARTIALLY,
                getImplementationStatus(requirement));

        if (LOG.isDebugEnabled()) {
            LOG.debug("Switch deduction off for the requierment.");
        }
        requirement.setPropertyValue(requirement.getTypeId() + IMPLEMENTATION_DEDUCE, "0");
        requirement.setSimpleProperty(getImplementationStatusId(requirement),
                BpRequirement.TYPE_ID + IMPLEMENTATION_STATUS_CODE_YES);
        UpdateElement<BpRequirement> command1 = new UpdateElement<>(requirement, true, null);
        commandService.executeCommand(command1);
        requirement = command1.getElement();

        if (LOG.isDebugEnabled()) {
            LOG.debug("Change the safeguard implementation status to: "
                    + IMPLEMENTATION_STATUS_CODE_PARTIALLY);
        }
        safeguard = updateSafeguard(safeguard, IMPLEMENTATION_STATUS_CODE_NO);
        assertEquals("Must be option yes.", BpRequirement.TYPE_ID + IMPLEMENTATION_STATUS_CODE_YES,
                getImplementationStatus(requirement));
    }

    /**
     * Will set implementation_status and update the safeguard.
     *
     */
    private Safeguard updateSafeguard(Safeguard safeguard, String option) throws CommandException {
        safeguard.setSimpleProperty(safeguard.getTypeId() + IMPLEMENTATION_STATUS,
                Safeguard.TYPE_ID + option);
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
        requirement.setSimpleProperty(requirement.getTypeId() + IMPLEMENTATION_STATUS,
                BpRequirement.TYPE_ID + IMPLEMENTATION_STATUS_CODE_YES);
        UpdateElement<BpRequirement> command1 = new UpdateElement<>(requirement, true, null);
        commandService.executeCommand(command1);
        requirement = command1.getElement();
        assertEquals("Must be option yes.", BpRequirement.TYPE_ID + IMPLEMENTATION_STATUS_CODE_YES,
                getImplementationStatus(requirement));

        assertTrue("Deduction should be enabled.",
                DeductionImplementationUtil.isDeductiveImplementationEnabled(requirement));
        return requirement;
    }

}
