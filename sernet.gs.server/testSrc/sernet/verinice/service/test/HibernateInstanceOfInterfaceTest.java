/*******************************************************************************
 * Copyright (c) 2019 Daniel Murygin
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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import sernet.gs.service.RetrieveInfo;
import sernet.hui.common.connect.IAbbreviatedElement;
import sernet.hui.common.connect.ITaggableElement;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Asset;
import sernet.verinice.model.iso27k.AssetGroup;
import sernet.verinice.model.iso27k.Group;
import sernet.verinice.model.iso27k.IISO27Scope;
import sernet.verinice.model.iso27k.IISO27kElement;
import sernet.verinice.model.iso27k.IISO27kGroup;
import sernet.verinice.model.iso27k.Organization;
import sernet.verinice.model.iso27k.Process;
import sernet.verinice.model.iso27k.ProcessGroup;
import sernet.verinice.service.commands.LoadElementById;

/**
 * This test checks whether entity instances loaded by Hibernate that contain
 * proxies implement interfaces defined in the Entity classes.
 */
@TransactionConfiguration(transactionManager = "txManager")
@Transactional
@SuppressWarnings("restriction")
public class HibernateInstanceOfInterfaceTest extends CommandServiceProvider {

    protected Asset asset1;
    protected Asset asset2;
    protected Process process;

    /**
     * This test checks whether an instance loaded with Hibernate implements the
     * interfaces that implement the class of the instance.
     */
    @Test
    public void testInstanceOf() throws CommandException {
        CnATreeElement elementCreated = createOrganization();
        RetrieveInfo doNotRetrieveAnyRelations = new RetrieveInfo();
        LoadElementById<CnATreeElement> command = new LoadElementById<>(
                elementCreated.getDbId(), doNotRetrieveAnyRelations);
        command = commandService.executeCommand(command);
        CnATreeElement element = command.getElement();
        assertNotNull(element);
        checkIfElementImplementsInterfacesOfOrganization(element);
    }

    /**
     * This test checks whether the children of an instance loaded with
     * Hibernate implements the interfaces that implement the classes of the
     * children.
     */
    @Test
    public void testInstanceOfChildren() throws CommandException {
        CnATreeElement elementCreated = createOrganization();
        RetrieveInfo doNotRetrieveAnyRelations = new RetrieveInfo();
        LoadElementById<CnATreeElement> command = new LoadElementById<>(
                elementCreated.getDbId(), doNotRetrieveAnyRelations);
        command = commandService.executeCommand(command);
        CnATreeElement element = command.getElement();
        assertNotNull(element);
        Set<CnATreeElement> children = element.getChildren();
        for (CnATreeElement child : children) {
            assertTrue(child instanceof IISO27kGroup);
        }
    }

    /**
     * This test checks whether the parent of an instance loaded with Hibernate
     * implements the interfaces that implement the classes of the parent.
     */
    @Test
    public void testInstanceOfParent() throws CommandException {
        CnATreeElement elementCreated = createOrganization();
        RetrieveInfo doNotRetrieveAnyRelations = new RetrieveInfo();
        LoadElementById<CnATreeElement> command = new LoadElementById<>(
                elementCreated.getDbId(), doNotRetrieveAnyRelations);
        command = commandService.executeCommand(command);
        CnATreeElement element = command.getElement();
        assertNotNull(element);
        Set<CnATreeElement> children = element.getChildren();
        for (CnATreeElement child : children) {
            checkIfElementImplementsInterfacesOfOrganization(child.getParent());
        }
    }

    /**
     * This test checks whether the links of an instance loaded with Hibernate
     * implements the interfaces that implement the classes of the links.
     */
    @Test
    public void testInstanceOfLinks() throws CommandException {
        beforeTestInstanceOfLinks();

        RetrieveInfo doNotRetrieveAnyRelations = new RetrieveInfo();
        LoadElementById<CnATreeElement> command = new LoadElementById<>(process.getDbId(),
                doNotRetrieveAnyRelations);
        command = commandService.executeCommand(command);
        process = (Process) command.getElement();
        assertNotNull(process);
        Set<CnALink> links = process.getLinksDown();
        links.addAll(process.getLinksUp());
        assertTrue(!links.isEmpty());
        for (CnALink link : links) {
            checkIfElementImplementsInterfacesOfIsoElement(link.getDependant());
            checkIfElementImplementsInterfacesOfIsoElement(link.getDependency());
        }
    }

    public void beforeTestInstanceOfLinks() throws CommandException {
        createOrganizationWithLinkedProcessAndAssets();
    }

    @SuppressWarnings("unchecked")
    public void createOrganizationWithLinkedProcessAndAssets() throws CommandException {
        Organization organization = createOrganization();
        CnATreeElement processGroup = organization.getGroup(ProcessGroup.TYPE_ID);
        process = (Process) createNewElement((Group<CnATreeElement>) processGroup, Process.class);
        process.setAbbreviation("p1");
        updateElement(process);

        CnATreeElement assetGroup = organization.getGroup(AssetGroup.TYPE_ID);
        asset1 = (Asset) createNewElement((Group<CnATreeElement>) assetGroup, Asset.class);
        asset1.setAbbreviation("a1");
        updateElement(asset1);
        createLink(process, asset1, Process.REL_PROCESS_ASSET);

        asset2 = (Asset) createNewElement((Group<CnATreeElement>) assetGroup, Asset.class);
        asset2.setAbbreviation("a2");
        updateElement(asset2);
        createLink(process, asset2, Process.REL_PROCESS_ASSET);
    }

    private void checkIfElementImplementsInterfacesOfOrganization(CnATreeElement element) {
        assertTrue(element instanceof IISO27kGroup);
        assertTrue(element instanceof IISO27Scope);
        assertTrue(element instanceof ITaggableElement);
        assertTrue(element instanceof IAbbreviatedElement);
    }

    private void checkIfElementImplementsInterfacesOfIsoElement(CnATreeElement element) {
        assertTrue(element instanceof IISO27kElement);
        assertTrue(element instanceof ITaggableElement);
        assertTrue(element instanceof IAbbreviatedElement);
    }

}
