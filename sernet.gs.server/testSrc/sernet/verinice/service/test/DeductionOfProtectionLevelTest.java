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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import sernet.verinice.interfaces.CommandException;
import sernet.verinice.model.bp.elements.Application;
import sernet.verinice.model.bp.elements.BusinessProcess;
import sernet.verinice.model.bp.elements.Device;
import sernet.verinice.model.bp.elements.IcsSystem;
import sernet.verinice.model.bp.elements.ItNetwork;
import sernet.verinice.model.bp.elements.Network;
import sernet.verinice.model.bp.elements.Room;
import sernet.verinice.model.bp.groups.ApplicationGroup;
import sernet.verinice.model.bp.groups.BusinessProcessGroup;
import sernet.verinice.model.bp.groups.DeviceGroup;
import sernet.verinice.model.bp.groups.IcsSystemGroup;
import sernet.verinice.model.bp.groups.NetworkGroup;
import sernet.verinice.model.bp.groups.RoomGroup;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Group;

/**
 * @author uz[at]sernet.de
 *
 */
@TransactionConfiguration(transactionManager = "txManager", defaultRollback = false)
@Transactional
public class DeductionOfProtectionLevelTest extends AbstractModernizedBaseProtection {

    private List<Class<? extends CnATreeElement>> elementList = new ArrayList<>();
    private List<Class<? extends Group<?>>> groupList = new ArrayList<>();

    enum CiaType {
        C("_confidentiality"), I("_integrity"), A("_availability");

        private final String expression;

        CiaType(String expression) {
            this.expression = expression;
        }

        public String getTypeName(String type) {
            return type + "_value" + expression;
        }

        public String getSwitchName(String type) {
            return type + "_value_method" + expression;
        }
    }

    enum CiaValue {
        L("_low"), N("_normal"), H("_high"), V("_very_high");

        private final String value;

        CiaValue(String ciaValue) {
            this.value = ciaValue;
        }

        public String getValueName(String type, CiaType ctype) {
            return type + "_value" + ctype.expression + value;
        }
    }

    public DeductionOfProtectionLevelTest() {
        super();
        elementList.add(Application.class);
        groupList.add(ApplicationGroup.class);
        elementList.add(BusinessProcess.class);
        groupList.add(BusinessProcessGroup.class);
        elementList.add(Device.class);
        groupList.add(DeviceGroup.class);
        elementList.add(IcsSystem.class);
        groupList.add(IcsSystemGroup.class);
        elementList.add(Network.class);
        groupList.add(NetworkGroup.class);
        elementList.add(Room.class);
        groupList.add(RoomGroup.class);
    }

    
    /**
     * Checks the basic implementation requirements.
     */
    @Transactional
    @Rollback(true)
    @Test
    public void testProtectionRequirements() throws Exception {
        ItNetwork network = createNewBPOrganization();

        BusinessProcessGroup bpGroup = createGroup(network, BusinessProcessGroup.class);
        BusinessProcess source = createElement(bpGroup, BusinessProcess.class);
        source = prepareDeductionSource(source);

        for (int i = 0; i < groupList.size(); i++) {
            Group<?> g = createGroup(network, groupList.get(i));
            CnATreeElement dependend = createElement(g, elementList.get(i));

            dependend = enableAllCIADeduction(dependend);
            createLink(source, dependend, null);

            assertNotNull(dependend.getProtectionRequirementsProvider());
            assertTrue(dependend.getProtectionRequirementsProvider().isCalculatedConfidentiality());
            assertTrue(dependend.getProtectionRequirementsProvider().isCalculatedIntegrity());
            assertTrue(dependend.getProtectionRequirementsProvider().isCalculatedAvailability());

            disableCIADeduction(dependend, CiaType.C);
            source = update(source);
            assertFalse(dependend.getProtectionRequirementsProvider().isCalculatedConfidentiality());
            disableCIADeduction(dependend, CiaType.I);
            source = update(source);
            assertFalse(dependend.getProtectionRequirementsProvider().isCalculatedIntegrity());
            disableCIADeduction(dependend, CiaType.A);
            source = update(source);
            assertFalse(dependend.getProtectionRequirementsProvider().isCalculatedAvailability());
        }
    }

    /**
     * Test a simple chain of deduction.
     * <pre>
     * source -> depend -> depend1
     * </pre>
     */
    @Transactional
    @Rollback(true)
    @Test
    public void testBasicDeduction() throws Exception {
        ItNetwork network = createNewBPOrganization();

        BusinessProcessGroup bpGroup = createGroup(network, BusinessProcessGroup.class);
        BusinessProcess source = createElement(bpGroup, BusinessProcess.class);
        source = prepareDeductionSource(source);

        BusinessProcess dependent = createElement(bpGroup, BusinessProcess.class);
        dependent = enableAllCIADeduction(dependent);
        createLink(source, dependent, null);

        assertCIAPropertyValue(dependent, CiaType.C, CiaValue.N);
        assertCIAPropertyValue(dependent, CiaType.I, CiaValue.N);
        assertCIAPropertyValue(dependent, CiaType.A, CiaValue.N);

        BusinessProcess dependent1 = createElement(bpGroup, BusinessProcess.class);
        dependent1 = enableAllCIADeduction(dependent1);
        createLink(dependent, dependent1, null);

        assertCIAPropertyValue(dependent1, CiaType.C, CiaValue.N);
        assertCIAPropertyValue(dependent1, CiaType.I, CiaValue.N);
        assertCIAPropertyValue(dependent1, CiaType.A, CiaValue.N);

        setCIAProperty(source, CiaType.C, CiaValue.V);
        source = update(source);
        assertCIAPropertyValue(dependent, CiaType.C, CiaValue.V);
        assertCIAPropertyValue(dependent1, CiaType.C, CiaValue.V);

        setCIAProperty(source, CiaType.I, CiaValue.V);
        source = update(source);
        assertCIAPropertyValue(dependent, CiaType.I, CiaValue.V);
        assertCIAPropertyValue(dependent1, CiaType.I, CiaValue.V);

        setCIAProperty(source, CiaType.A, CiaValue.V);
        source = update(source);
        assertCIAPropertyValue(dependent, CiaType.A, CiaValue.V);
        assertCIAPropertyValue(dependent1, CiaType.A, CiaValue.V);
    }

    /**
     * Test the deduction for all types as sources and as dependent.
     * 
     * @throws Exception
     */
    @Transactional
    @Rollback(true)
    @Test
    public void testAllRelevantTypes() throws Exception {
        ItNetwork network = createNewBPOrganization();

        for (int i = 0; i < groupList.size(); i++) {
            Group<?> bpGroup = createGroup(network, groupList.get(i));
            CnATreeElement source = createElement(bpGroup, elementList.get(i));

            source = prepareDeductionSource(source);
            for (int j = 0; j < groupList.size(); j++) {
                Group<?> dGroup = createGroup(network, groupList.get(j));
                CnATreeElement dependent = createElement(dGroup, elementList.get(j));
                enableAllCIADeduction(dependent);

                createLink(source, dependent, null);

                assertCIAPropertyValue(dependent, CiaType.C, CiaValue.N);
                assertCIAPropertyValue(dependent, CiaType.I, CiaValue.N);
                assertCIAPropertyValue(dependent, CiaType.A, CiaValue.N);

                setCIAProperty(source, CiaType.I, CiaValue.V);
                source = update(source);
                assertCIAPropertyValue(dependent, CiaType.I, CiaValue.V);

                setCIAProperty(source, CiaType.I, CiaValue.N);
                source = update(source);
                assertCIAPropertyValue(dependent, CiaType.I, CiaValue.N);
            }
        }
    }

    /**
     * Two source for one dependent the value is always the highest one.
     * <pre>
     * source  -> dependent
     * source1 ---^
     * </pre>
     * 
     * @throws Exception
     */
    @Transactional
    @Rollback(true)
    @Test
    public void testTwoSources() throws Exception {
        ItNetwork network = createNewBPOrganization();

        BusinessProcessGroup bpGroup = createGroup(network, BusinessProcessGroup.class);
        BusinessProcess source = createElement(bpGroup, BusinessProcess.class);
        source = prepareDeductionSource(source);

        BusinessProcess dependent = createElement(bpGroup, BusinessProcess.class);
        dependent = enableAllCIADeduction(dependent);
        createLink(source, dependent, null);

        assertCIAPropertyValue(dependent, CiaType.C, CiaValue.N);
        assertCIAPropertyValue(dependent, CiaType.I, CiaValue.N);
        assertCIAPropertyValue(dependent, CiaType.A, CiaValue.N);

        BusinessProcess source1 = createElement(bpGroup, BusinessProcess.class);
        source1 = prepareDeductionSource(source1);
        setCIAProperty(source1, CiaType.C, CiaValue.L);
        setCIAProperty(source1, CiaType.I, CiaValue.H);
        setCIAProperty(source1, CiaType.A, CiaValue.V);
        source1 = update(source1);

        createLink(source1, dependent, null);

        assertCIAPropertyValue(dependent, CiaType.C, CiaValue.N);
        assertCIAPropertyValue(dependent, CiaType.I, CiaValue.H);
        assertCIAPropertyValue(dependent, CiaType.A, CiaValue.V);

        setCIAProperty(source1, CiaType.I, CiaValue.V);
        source1 = update(source1);
        assertCIAPropertyValue(dependent, CiaType.I, CiaValue.V);

        setCIAProperty(source1, CiaType.I, CiaValue.L);
        source1 = update(source1);
        assertCIAPropertyValue(dependent, CiaType.I, CiaValue.N);
    }

    /**
     * Test a cycle in the deduction.
     * <pre>
     * source -> dependent -> dependend1 -> dependent2 -> dependent3
     *               ^---------------------------------------|
     * </pre>
     * 
     * @throws Exception
     */
    @Transactional
    @Rollback(true)
    @Test
    public void testRingDeduction() throws Exception {
        List<BusinessProcess> elements = new ArrayList<>();
        ItNetwork network = createNewBPOrganization();

        BusinessProcessGroup bpGroup = createGroup(network, BusinessProcessGroup.class);
        BusinessProcess source = createElement(bpGroup, BusinessProcess.class);
        source = prepareDeductionSource(source);

        BusinessProcess dependent = createElement(bpGroup, BusinessProcess.class);
        dependent = enableAllCIADeduction(dependent);
        createLink(source, dependent, null);
        elements.add(dependent);

        BusinessProcess dependent1 = createElement(bpGroup, BusinessProcess.class);
        dependent1 = enableAllCIADeduction(dependent1);
        createLink(dependent, dependent1, null);
        elements.add(dependent1);

        BusinessProcess dependent2 = createElement(bpGroup, BusinessProcess.class);
        dependent2 = enableAllCIADeduction(dependent2);
        createLink(dependent1, dependent2, null);
        elements.add(dependent2);

        BusinessProcess dependent3 = createElement(bpGroup, BusinessProcess.class);
        dependent3 = enableAllCIADeduction(dependent3);
        createLink(dependent, dependent3, null);
        elements.add(dependent3);

        for (BusinessProcess element : elements) {
            assertCIAPropertyValue(element, CiaType.C, CiaValue.N);
            assertCIAPropertyValue(element, CiaType.I, CiaValue.N);
            assertCIAPropertyValue(element, CiaType.A, CiaValue.N);
        }

        setCIAProperty(source, CiaType.C, CiaValue.V);
        source = update(source);
        for (BusinessProcess element : elements) {
            assertCIAPropertyValue(element, CiaType.C, CiaValue.V);
        }

        setCIAProperty(source, CiaType.I, CiaValue.H);
        source = update(source);
        for (BusinessProcess element : elements) {
            assertCIAPropertyValue(element, CiaType.I, CiaValue.H);
        }

        setCIAProperty(source, CiaType.A, CiaValue.L);
        source = update(source);
        for (BusinessProcess element : elements) {
            assertCIAPropertyValue(element, CiaType.A, CiaValue.L);
        }
    }

    /**
     * Test for switching the deduction off.
     * 
     * @throws CommandException
     */
    @Transactional
    @Rollback(true)
    @Test
    public void testStopDeduction() throws CommandException {
        ItNetwork network = createNewBPOrganization();

        BusinessProcessGroup bpGroup = createGroup(network, BusinessProcessGroup.class);
        BusinessProcess source = createElement(bpGroup, BusinessProcess.class);
        source = prepareDeductionSource(source);

        BusinessProcess dependent = createElement(bpGroup, BusinessProcess.class);
        dependent = enableAllCIADeduction(dependent);
        createLink(source, dependent, null);

        BusinessProcess dependent1 = createElement(bpGroup, BusinessProcess.class);
        dependent1 = enableAllCIADeduction(dependent1);
        createLink(dependent, dependent1, null);

        assertCIAPropertyValue(dependent, CiaType.C, CiaValue.N);
        assertCIAPropertyValue(dependent, CiaType.I, CiaValue.N);
        assertCIAPropertyValue(dependent, CiaType.A, CiaValue.N);
        assertCIAPropertyValue(dependent1, CiaType.C, CiaValue.N);
        assertCIAPropertyValue(dependent1, CiaType.I, CiaValue.N);
        assertCIAPropertyValue(dependent1, CiaType.A, CiaValue.N);

        disableCIADeduction(dependent1, CiaType.A);

        setCIAProperty(source, CiaType.A, CiaValue.L);
        source = update(source);
        assertCIAPropertyValue(dependent, CiaType.A, CiaValue.L);
        assertCIAPropertyValue(dependent1, CiaType.A, CiaValue.N);

        setCIAProperty(source, CiaType.C, CiaValue.H);
        source = update(source);
        assertCIAPropertyValue(dependent, CiaType.C, CiaValue.H);
        assertCIAPropertyValue(dependent1, CiaType.C, CiaValue.H);
    }

    /**
     * Enables the deduction for all cases {@link CiaType}.
     */
    private <T extends CnATreeElement> T enableAllCIADeduction(T dependend)
            throws CommandException {
        for (CiaType ct : CiaType.values()) {
            enableCIADeduction(dependend, ct);
        }
        dependend = update(dependend);
        return dependend;
    }

    /**
     * Prepares a deduction source by disable the deduction and set the values
     * for all {@link CiaType} to CiaValue.N.
     * 
     */
    private <T extends CnATreeElement> T prepareDeductionSource(T element) throws CommandException {
        for (CiaType ct : CiaType.values()) {
            disableCIADeduction(element, ct);
            setCIAProperty(element, ct, CiaValue.N);
        }
        element = update(element);
        for (CiaType ct : CiaType.values()) {
            assertCIAPropertyValue(element, ct, CiaValue.N);
        }
        return element;
    }

    /**
     * Assert the numeric value of the property is the given {@link CiaValue}.
     * 
>>>>>>> branch 'feature/vn-1889-implement-deduction-of-protection-level' of ssh://git@git.verinice.org:7999/isms/verinice.git
     * @param element
     *            - the element to test
     * @param type
     *            - the cia property
     * @param expectedValue
     *            - the cia value
     */
    private static void assertCIAPropertyValue(CnATreeElement element, CiaType type,
            CiaValue expectedValue) {
        assertEquals(expectedValue.ordinal(),
                element.getNumericProperty(type.getTypeName(element.getTypeId())));
    }

    private static void setCIAProperty(CnATreeElement element, CiaType type, CiaValue value) {
        element.setNumericProperty(type.getTypeName(element.getTypeId()), value.ordinal());
    }

    private static void setCIADeduction(CnATreeElement element, CiaType type, String value) {
        element.setPropertyValue(type.getSwitchName(element.getTypeId()), value);
    }

    private static void enableCIADeduction(CnATreeElement element, CiaType type) {
        setCIADeduction(element, type, "1");
    }

    private static void disableCIADeduction(CnATreeElement element, CiaType type) {
        setCIADeduction(element, type, "0");
    }
}
