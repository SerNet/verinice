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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.Set;

import org.junit.Test;

import sernet.gs.service.RetrieveInfo;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.iso27k.service.Retriever;
import sernet.verinice.model.bsi.Anwendung;
import sernet.verinice.model.bsi.AnwendungenKategorie;
import sernet.verinice.model.bsi.Client;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.ds.Personengruppen;
import sernet.verinice.model.iso27k.PersonGroup;
import sernet.verinice.service.commands.SyncCommand;
import sernet.verinice.service.commands.SyncParameter;
import sernet.verinice.service.commands.SyncParameterException;
import sernet.verinice.service.test.helper.vnaimport.BeforeEachVNAImportHelper;

/**
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 * 
 */
public class SyncInsertUpdateTest extends BeforeEachVNAImportHelper {

    private static final String VNA_FILE = "SyncInsertUpdateTest.vna";

    private static final String VNA_FILE_INSERT = "SyncInsertUpdateTestInsert.vna";
    
    private static final String VNA_FILE_UPDATE = "SyncInsertUpdateTestUpdate.vna";
    
    private static final String VNA_FILE_UPDATE_INSERT = "SyncInsertUpdateTestUpdateInsert.vna";
    
    private static final String VNA_FILE_DELETE = "SyncInsertUpdateTestDelete.vna";
    
    private static final String SOURCE_ID = "JUNIT SyncInsertUpdate";
       
    private static final String ANWENDUNGEN_KATEGORIE_EXT_ID = "ENTITY_676f8622-f7e3-4e9f-9247-2bd473b8e256";
    
    private static final String ANWENDUNG_1_EXT_ID = "ENTITY_112";
    
    private static final String ANWENDUNG_2_EXT_ID = "ENTITY_330";
    
    private static final String CLIENT_EXT_ID = "ENTITY_40214";

    @Test
    public void insertTest() throws IOException, CommandException, SyncParameterException {

        SyncParameter syncParameter = new SyncParameter(true, false, false, false, SyncParameter.EXPORT_FORMAT_VERINICE_ARCHIV);
        SyncCommand syncCommand = importFile(getAbsoluteFilePath(VNA_FILE_INSERT), syncParameter);                
        
      
        Anwendung anwendung1 = (Anwendung) loadElement(SOURCE_ID, ANWENDUNG_1_EXT_ID);
        Anwendung anwendung2 = (Anwendung) loadElement(SOURCE_ID, ANWENDUNG_2_EXT_ID);
        
        assertEquals("inserted Object anwendung must have the same parent id as the already existing", anwendung1.getParent().getDbId(), anwendung2.getParent().getDbId());
        
        assertEquals("updated flag is set to false", 0, syncCommand.getUpdated());
//        assertEquals("only one object should have been inserted", 1, syncCommand.getInserted());
    }
    
    @Test
    public void updateTest() throws IOException, CommandException, SyncParameterException {
        
        Anwendung anwendungBeforeImport = (Anwendung) loadElement(SOURCE_ID, ANWENDUNG_1_EXT_ID);
        AnwendungenKategorie anwendungenKategorieBeforeImport = (AnwendungenKategorie) loadElement(SOURCE_ID, ANWENDUNGEN_KATEGORIE_EXT_ID);
        
        SyncParameter syncParameter = new SyncParameter(false, true, false, false, SyncParameter.EXPORT_FORMAT_VERINICE_ARCHIV);
        SyncCommand syncCommand = importFile(getAbsoluteFilePath(VNA_FILE_UPDATE), syncParameter);
        
        Anwendung anwendungAfterImport = (Anwendung) loadElement(SOURCE_ID, ANWENDUNG_1_EXT_ID);               
        
        String anwendungsStatusBefore = anwendungBeforeImport.getEntity().getSimpleValue("anwendung_status");
        String anwendungsStatusAfter = anwendungAfterImport.getEntity().getSimpleValue("anwendungs_status");        
        assertFalse(anwendungsStatusBefore.equals(anwendungsStatusAfter));
        
        String anwendungsPersonenBezogenBefore = anwendungBeforeImport.getEntity().getSimpleValue("anwendung_persbez");
        String anwendungsPersonenBezogenAfter = anwendungAfterImport.getEntity().getSimpleValue("anwendung_persbez");        
        assertFalse(anwendungsPersonenBezogenBefore.equals(anwendungsPersonenBezogenAfter));
        
        assertEquals(anwendungBeforeImport.getKuerzel(), anwendungAfterImport.getKuerzel());
        
        assertEquals("AnwendungenKategorie must still have only 1 child", 1, anwendungenKategorieBeforeImport.getChildren().size());
//        assertEquals("updated exactly one object", 1, syncCommand.getUpdated());
        assertEquals("insert flag is set to false", 0, syncCommand.getInserted());
    }
    
    @Test
    public void insertUpdateTest() throws IOException, CommandException, SyncParameterException{
        
        Anwendung anwendungBeforeImport = (Anwendung) loadElement(SOURCE_ID, ANWENDUNG_1_EXT_ID);
        
        SyncParameter syncParameter = new SyncParameter(true, true, false, false, SyncParameter.EXPORT_FORMAT_VERINICE_ARCHIV);
        importFile(getAbsoluteFilePath(VNA_FILE_UPDATE_INSERT), syncParameter);
        
        Anwendung anwendung1 = (Anwendung) loadElement(SOURCE_ID, ANWENDUNG_1_EXT_ID);
        Anwendung anwendung2 = (Anwendung) loadElement(SOURCE_ID, ANWENDUNG_2_EXT_ID);
        
        
        assertEquals("inserted Object anwendung must have the same parent id as the already existing", anwendung1.getParent().getDbId(), anwendung2.getParent().getDbId());        
        
        Client client = (Client) loadElement(SOURCE_ID, CLIENT_EXT_ID);
        assertNotNull(client);
                
        String anwendungsStatusBefore = anwendungBeforeImport.getEntity().getSimpleValue("anwendung_status");
        String anwendungsStatusAfter = anwendung1.getEntity().getSimpleValue("anwendungs_status");        
        assertFalse(anwendungsStatusBefore.equals(anwendungsStatusAfter));      
    }
    
    @Test
    public void relationImported() throws SyncParameterException, IOException, CommandException{
        
        SyncParameter syncParameter = new SyncParameter(true, true, false, false, SyncParameter.EXPORT_FORMAT_VERINICE_ARCHIV);
        importFile(getAbsoluteFilePath(VNA_FILE_UPDATE_INSERT), syncParameter);   
        
        Anwendung anwendungWithLink = (Anwendung) loadElement(SOURCE_ID, ANWENDUNG_1_EXT_ID);
        Client clientWithLink = (Client) loadElement(SOURCE_ID, CLIENT_EXT_ID);
        
        anwendungWithLink = (Anwendung) Retriever.retrieveElement(anwendungWithLink, new RetrieveInfo().setLinksDown(true));  
        Set<CnALink> links = anwendungWithLink.getLinksDown();
        Client client = (Client) links.iterator().next().getDependency();
        
        assertEquals(client.getDbId(), clientWithLink.getDbId());        
    }
    
    @Test(expected=AssertionError.class)
    public void delete() throws SyncParameterException, IOException, CommandException {
        
        PersonGroup personengruppen = (PersonGroup) loadElement(SOURCE_ID, "ENTITY_44561");
        
        assertNotNull(personengruppen);
        
        SyncParameter syncParameter = new SyncParameter(false, false, true, false, SyncParameter.EXPORT_FORMAT_VERINICE_ARCHIV);
        importFile(getAbsoluteFilePath(VNA_FILE_DELETE), syncParameter);
        
        personengruppen = (PersonGroup) loadElement(SOURCE_ID, "ENTITY_44561");
    }

    private String getAbsoluteFilePath(String path) {
        return getClass().getResource(path).getPath();
    }

    @Override
    protected String getFilePath() {
        return getAbsoluteFilePath(VNA_FILE);
    }

    @Override
    protected SyncParameter getSyncParameter() throws SyncParameterException {
        return new SyncParameter(true, true, true, false, SyncParameter.EXPORT_FORMAT_VERINICE_ARCHIV);
    }

}
