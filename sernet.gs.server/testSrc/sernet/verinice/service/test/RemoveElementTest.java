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

import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import sernet.verinice.interfaces.CommandException;
import sernet.verinice.model.bsi.BSIModel;
import sernet.verinice.model.bsi.GebaeudeKategorie;
import sernet.verinice.model.bsi.ITVerbund;
import sernet.verinice.model.common.CnATreeElement;
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
    public void setUp() throws CommandException{
        
        LoadBSIModel loadBSIModel = new LoadBSIModel();
        BSIModel bSIModel = commandService.executeCommand(loadBSIModel).getModel();
        itVerbund = createElement(ITVerbund.class, bSIModel);        
        
        LoadModel loadISO27Model = new LoadModel();
        ISO27KModel iSO27Model = commandService.executeCommand(loadISO27Model).getModel();
        organization = createElement(Organization.class, iSO27Model); 
    }
    
    @Test
    public void removeITVerbund() throws CommandException {

        RemoveElement<CnATreeElement> removeCommand = new RemoveElement<CnATreeElement>(itVerbund);
        commandService.executeCommand(removeCommand);

        assertElementIsDeleted(itVerbund);
    }
    
    @Test
    public void removeOrganization() throws CommandException {
        
        RemoveElement<CnATreeElement> removeCommand = new RemoveElement<CnATreeElement>(organization);
        commandService.executeCommand(removeCommand);

        assertElementIsDeleted(organization);         
    }
    
    @Test
    public void removeGroupFromOrganization() throws CommandException{       
        
        GebaeudeKategorie gebaeudeKategorie = createElement(GebaeudeKategorie.class, itVerbund);
     
        RemoveElement<GebaeudeKategorie> removeCommand =  new RemoveElement<GebaeudeKategorie>(gebaeudeKategorie);
        commandService.executeCommand(removeCommand);
        
        assertElementIsDeleted(gebaeudeKategorie);
    }
    
    
    private void assertElementIsDeleted(CnATreeElement element) throws CommandException{
        LoadElementByUuid<CnATreeElement> command = new LoadElementByUuid<CnATreeElement>(element.getUuid());
        command = commandService.executeCommand(command);
        CnATreeElement loadByUUid = command.getElement();
        assertNull("element " + element.getUuid() + " was not deleted.", loadByUUid);
    }
    
    private  <T extends CnATreeElement> T createElement(Class<T> type, CnATreeElement element) throws CommandException {
        CreateElement<T> saveOrganizationCommand = new CreateElement<T>(element, type, RemoveElementTest.class.getSimpleName() + " [" + UUID.randomUUID() + "]");
        saveOrganizationCommand = commandService.executeCommand(saveOrganizationCommand);

        return saveOrganizationCommand.getNewElement();        
    }
    
    
    @After
    public void tearDown(){
        this.itVerbund = null;
        this.organization = null;
    }
}
