/*******************************************************************************
 * Copyright (c) 2014 Benjamin Weißenfels.
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
 *     Benjamin Weißenfels <bw[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.service.test;

import static org.junit.Assert.assertNull;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import sernet.verinice.interfaces.CommandException;
import sernet.verinice.model.bsi.BSIModel;
import sernet.verinice.model.bsi.Gebaeude;
import sernet.verinice.model.bsi.GebaeudeKategorie;
import sernet.verinice.model.bsi.ITVerbund;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Document;
import sernet.verinice.model.iso27k.ISO27KModel;
import sernet.verinice.model.iso27k.Organization;
import sernet.verinice.service.commands.CreateElement;
import sernet.verinice.service.commands.LoadBSIModel;
import sernet.verinice.service.commands.LoadElementByUuid;
import sernet.verinice.service.commands.RemoveElement;
import sernet.verinice.service.iso27k.LoadModel;

/**
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 * 
 */
public class RemoveElementTest extends CommandServiceProvider {

    private ITVerbund itVerbund;

    private Organization organization;

    @Before
    public void setUp() throws CommandException {

        LoadBSIModel loadBSIModel = new LoadBSIModel();
        BSIModel bSIModel = commandService.executeCommand(loadBSIModel).getModel();
        itVerbund = createElement(ITVerbund.class, bSIModel);

        LoadModel loadISO27Model = new LoadModel();
        ISO27KModel iSO27Model = commandService.executeCommand(loadISO27Model).getModel();
        organization = createElement(Organization.class, iSO27Model);
    }

    @Test
    public void removeITVerbund() throws CommandException {

        removeElement(itVerbund);
        assertElementIsDeleted(itVerbund);
    }

    @Test
    public void removeOrganization() throws CommandException {

        removeElement(organization);
        assertElementIsDeleted(organization);
    }

    @Test
    public void removeKategorieFromVerbund() throws CommandException {

        GebaeudeKategorie gebaeudeKategorie = createElement(GebaeudeKategorie.class, itVerbund);
        removeElement(gebaeudeKategorie);
        assertElementIsDeleted(gebaeudeKategorie);
    }

    @Test
    public void removeElementFromKategorie() throws CommandException {
        GebaeudeKategorie gebaeudeKategorie = createElement(GebaeudeKategorie.class, itVerbund);
        Gebaeude gebaeude = createElement(Gebaeude.class, gebaeudeKategorie);

        removeElement(gebaeude);
        assertElementIsDeleted(gebaeude);
    }

    @Test
    public void removeParentKategorie() throws CommandException {
        GebaeudeKategorie gebaeudeKategorie = createElement(GebaeudeKategorie.class, itVerbund);
        Gebaeude gebaeude = createElement(Gebaeude.class, gebaeudeKategorie);

        RemoveElement<GebaeudeKategorie> removeCommand = removeElement(gebaeudeKategorie);

        assertElementIsDeleted(gebaeude);
        }

    @Test
    public void removeElementFromGroup() throws CommandException {

        for (Entry<String, Class> entry : GROUP_TYPE_MAP.entrySet()) {
            
            CnATreeElement element = createElement(entry.getValue(), organization);

            int documents = new SecureRandom().nextInt(20);
            List<Document> documentList = new ArrayList<Document>(0);
            for (int i = documents; i > 0; i--) {
                createElement(Document.class, element);
            }

            RemoveElement<CnATreeElement> removeCommand = removeElement(element);

            for (Document doc : documentList) {
                assertElementIsDeleted(doc);
            }

            assertElementIsDeleted(element);
        }
    }

    private void assertElementIsDeleted(CnATreeElement element) throws CommandException {
        LoadElementByUuid<CnATreeElement> command = new LoadElementByUuid<CnATreeElement>(element.getUuid());
        command = commandService.executeCommand(command);
        CnATreeElement loadByUUid = command.getElement();
        assertNull("element " + element.getUuid() + " was not deleted.", loadByUUid);
    }

    private <T extends CnATreeElement> T createElement(Class<T> type, CnATreeElement element) throws CommandException {
        CreateElement<T> createElementCommand = new CreateElement<T>(element, type, RemoveElementTest.class.getSimpleName() + " [" + UUID.randomUUID() + "]");
        createElementCommand = commandService.executeCommand(createElementCommand);

        return createElementCommand.getNewElement();
    }

    private <T extends CnATreeElement> RemoveElement<T> removeElement(T element) throws CommandException {
        RemoveElement<T> removeCommand = new RemoveElement<T>(element);
        return commandService.executeCommand(removeCommand);
    }

    @After
    public void tearDown() throws CommandException {
        removeElement(itVerbund);
        removeElement(organization);
        this.itVerbund = null;
        this.organization = null;
    }
}
