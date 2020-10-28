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
package sernet.gs.ui.rcp.main.bsi.views;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Control;
import org.junit.Assert;
import org.junit.Test;

import sernet.gs.ui.rcp.main.AbstractRequiresHUITypeFactoryTest;
import sernet.verinice.model.bp.SecurityLevel;
import sernet.verinice.model.bp.elements.Application;
import sernet.verinice.model.bp.elements.BpPerson;
import sernet.verinice.model.bp.elements.BpRequirement;
import sernet.verinice.model.bp.elements.BusinessProcess;
import sernet.verinice.model.bp.elements.IcsSystem;
import sernet.verinice.model.bp.elements.ItNetwork;
import sernet.verinice.model.bp.elements.Safeguard;
import sernet.verinice.model.bsi.Anwendung;
import sernet.verinice.model.bsi.Gebaeude;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Organization;
import sernet.verinice.model.iso27k.PersonIso;

public class RelationComparatorTest extends AbstractRequiresHUITypeFactoryTest {

    @Test
    public void testCompareDifferentTypeIds() {
        BusinessProcess businessProcess = new BusinessProcess(null);
        BpPerson person = new BpPerson(null);
        Application application = new Application(null);

        CnALink linkToApplication = new CnALink(businessProcess, application,
                "rel_bp_businessprocess_bp_application", null);
        CnALink linkToPerson = new CnALink(person, businessProcess,
                "rel_bp_person_bp_businessprocess", null);

        assertFirstArgumentSmaller(businessProcess, linkToApplication, linkToPerson);
    }

    @Test
    public void testCompareSameTypeIdsDifferentLinkTypes() {
        BusinessProcess businessProcess = new BusinessProcess(null);
        BpPerson person = new BpPerson(null);

        CnALink linkToPerson1 = new CnALink(person, businessProcess,
                "rel_bp_person_bp_businessprocess_primarily", null);
        CnALink linkToPerson2 = new CnALink(person, businessProcess,
                "rel_bp_person_bp_businessprocess", null);

        assertFirstArgumentSmaller(businessProcess, linkToPerson1, linkToPerson2);
    }

    @Test
    public void testCompareSafeguardsBySecurityLevel() {
        BpRequirement requirement = new BpRequirement(null);
        Safeguard safeguard1 = new Safeguard(null);
        Safeguard safeguard2 = new Safeguard(null);
        safeguard1.setSecurityLevel(SecurityLevel.BASIC);
        safeguard2.setSecurityLevel(SecurityLevel.HIGH);

        CnALink linkToSafeguard1 = new CnALink(requirement, safeguard1,
                "rel_bp_requirement_bp_safeguard", null);
        CnALink linkToSafeguard2 = new CnALink(requirement, safeguard2,
                "rel_bp_requirement_bp_safeguard", null);

        assertFirstArgumentSmaller(requirement, linkToSafeguard1, linkToSafeguard2);
    }

    @Test
    public void testCompareRequirementsBySecurityLevel() {
        Safeguard safeguard = new Safeguard(null);
        BpRequirement requirement1 = new BpRequirement(null);
        BpRequirement requirement2 = new BpRequirement(null);
        requirement2.setSecurityLevel(SecurityLevel.BASIC);

        CnALink linkToRequirement1 = new CnALink(requirement1, safeguard,
                "rel_bp_requirement_bp_safeguard", null);
        CnALink linkToRequirement2 = new CnALink(requirement2, safeguard,
                "rel_bp_requirement_bp_safeguard", null);

        assertFirstArgumentSmaller(safeguard, linkToRequirement1, linkToRequirement2);
    }

    @Test
    public void testCompareRequirementsByIdentifier() {
        ItNetwork itNetwork = new ItNetwork(null);
        BpRequirement requirement1 = new BpRequirement(null);
        BpRequirement requirement2 = new BpRequirement(null);
        requirement1.setIdentifier("R1");
        requirement2.setIdentifier("R2");

        CnALink linkToRequirement1 = new CnALink(requirement1, itNetwork,
                "rel_bp_requirement_bp_itnetwork", null);
        CnALink linkToRequirement2 = new CnALink(requirement2, itNetwork,
                "rel_bp_requirement_bp_itnetwork", null);

        assertFirstArgumentSmaller(itNetwork, linkToRequirement1, linkToRequirement2);
    }

    @Test
    public void testCompareIsoPersonsByAbbreviation() {
        Organization organization = new Organization();
        PersonIso person1 = new PersonIso(null);
        PersonIso person2 = new PersonIso(null);
        person1.setAbbreviation("P1");
        person2.setAbbreviation("P2");

        CnALink linkToPerson1 = new CnALink(person1, organization, "rel_person_org_ceo", null);
        CnALink linkToPerson2 = new CnALink(person2, organization, "rel_person_org_ceo", null);

        assertFirstArgumentSmaller(organization, linkToPerson1, linkToPerson2);
    }

    @Test
    public void testCompareApplicationByTitle() {
        IcsSystem icsSystem = new IcsSystem(null);
        Application application1 = new Application(null);
        Application application2 = new Application(null);
        application1.setTitel("A");
        application2.setTitel("B");

        CnALink linkToApplication1 = new CnALink(application1, icsSystem,
                "rel_bp_application_bp_icssystem", null);
        CnALink linkToApplication2 = new CnALink(application2, icsSystem,
                "rel_bp_application_bp_icssystem", null);

        assertFirstArgumentSmaller(icsSystem, linkToApplication1, linkToApplication2);
    }

    @Test
    public void testGenericLinksFromOldItbp() {
        Anwendung application = new Anwendung(null);
        application.setTitel("A1");
        Gebaeude building1 = new Gebaeude(null);
        building1.setTitel("B1");
        Gebaeude building2 = new Gebaeude(null);
        building2.setTitel("B2");

        CnALink linkToApplication1 = new CnALink(application, building1, "", null);
        CnALink linkToApplication2 = new CnALink(application, building2, "", null);

        assertFirstArgumentSmaller(application, linkToApplication1, linkToApplication2);

    }

    private void assertFirstArgumentSmaller(CnATreeElement elementInQuestion, CnALink link1,
            CnALink link2) {
        RelationComparator relationComparator = new RelationComparator();
        Viewer mockViewer = new Viewer() {

            @Override
            public Control getControl() {
                throw new UnsupportedOperationException();
            }

            @Override
            public Object getInput() {
                return elementInQuestion;
            }

            @Override
            public ISelection getSelection() {
                throw new UnsupportedOperationException();
            }

            @Override
            public void refresh() {
                throw new UnsupportedOperationException();

            }

            @Override
            public void setInput(Object input) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void setSelection(ISelection selection, boolean reveal) {
                throw new UnsupportedOperationException();
            }

        };

        int result = relationComparator.compare(mockViewer, link1, link2);
        int resultInverseComparison = relationComparator.compare(mockViewer, link2, link1);

        Assert.assertTrue("Expected " + link1 + " link to be smaller than " + link2, result < 0);
        Assert.assertTrue("Expected " + link2 + " link to be greater than " + link1,
                resultInverseComparison > 0);

    }

}
