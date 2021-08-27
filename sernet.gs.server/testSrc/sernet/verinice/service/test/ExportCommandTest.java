/*******************************************************************************
 * Copyright (c) 2019 Alexander Koderman
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
 *     Alexander Koderman - initial API and implementation
 ******************************************************************************/
package sernet.verinice.service.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.JAXB;

import org.apache.log4j.Logger;
import org.junit.Test;

import de.sernet.sync.data.SyncObject;
import de.sernet.sync.sync.SyncRequest;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.service.commands.ExportCommand;
import sernet.verinice.service.commands.SyncParameterException;
import sernet.verinice.service.sync.VeriniceArchive;
import sernet.verinice.service.test.helper.vnaimport.VNAImportHelper;

/**
 * Tests the command to export data to a VNA.
 * 
 */
public class ExportCommandTest extends CommandServiceProvider {

    private static final Logger LOG = Logger.getLogger(ExportCommandTest.class);

    private static final String VNA_FILENAME_Export_test = "Export_Test.vna";
    private static final String SOURCE_ID_Export_test = "41a219";
    private static final String EXT_ID_BP_ITNETWORK_Export_test = "1f581c34-b512-46ba-ab74-7d9b776748dc";

    private static final String VNA_FILENAME_testVnaImport = "testVnaImport.vna";
    private static final String SOURCE_ID_testVnaImport = "CommandServiceTest";
    private static final String EXT_ID_ORGANIZATION_testVnaImport = "ENTITY_191053";

    private static final String VNA_FILENAME_modplast = "modplast-1.1.vna";
    private static final String SOURCE_ID_modplast = "SerNet-DM";
    private static final String EXT_ID_BP_ITNETWORK_modplast = "ENTITY_159007";

    @Test
    public void exportScope() throws CommandException, SyncParameterException, IOException {
        // Given:
        VNAImportHelper.importFile(VNA_FILENAME_Export_test);
        CnATreeElement org = loadElement(SOURCE_ID_Export_test, EXT_ID_BP_ITNETWORK_Export_test,
                false, false, false);

        // When:
        ExportCommand cmd = new ExportCommand(Arrays.asList(org), "testSourceId", false // no
                                                                                        // re-import
        );
        cmd = commandService.executeCommand(cmd);

        // Then:
        assertTrue("Export did not produce any data.",
                cmd.getResult() != null && cmd.getResult().length > 0);
        VeriniceArchive vna = new VeriniceArchive(cmd.getResult());

        SyncRequest syncRequest = JAXB.unmarshal(vna.getVeriniceXml(), SyncRequest.class);
        assertEquals(1, syncRequest.getSyncData().getSyncObject().size());
        assertEquals(11, syncRequest.getSyncData().getSyncObject().get(0).getChildren().size());
        List<SyncObject> allSyncObjects = getAllSyncObjects(syncRequest);
        assertEquals(351, allSyncObjects.size());
        assertEquals(0, allSyncObjects.stream().flatMap(o -> o.getFile().stream()).count());
        assertEquals(983, syncRequest.getSyncData().getSyncLink().size());
        removeElement(org);
    }

    @Test
    public void exportScopeWithAttachments()
            throws CommandException, IOException, SyncParameterException {

        // Given:
        VNAImportHelper.importFile(VNA_FILENAME_testVnaImport);
        CnATreeElement org = loadElement(SOURCE_ID_testVnaImport, EXT_ID_ORGANIZATION_testVnaImport,
                false, false, false);

        // When:
        ExportCommand cmd = new ExportCommand(Arrays.asList(org), "testSourceId", false // no
                                                                                        // re-import
        );
        cmd = commandService.executeCommand(cmd);

        // Then:
        assertTrue("Export did not produce any data.",
                cmd.getResult() != null && cmd.getResult().length > 0);
        VeriniceArchive vna = new VeriniceArchive(cmd.getResult());

        SyncRequest syncRequest = JAXB.unmarshal(vna.getVeriniceXml(), SyncRequest.class);
        assertEquals(1, syncRequest.getSyncData().getSyncObject().size());
        assertEquals(4, syncRequest.getSyncData().getSyncObject().get(0).getChildren().size());
        List<SyncObject> allSyncObjects = getAllSyncObjects(syncRequest);
        assertEquals(29, allSyncObjects.size());
        assertEquals(2, allSyncObjects.stream().flatMap(o -> o.getFile().stream()).count());
        assertEquals(20, syncRequest.getSyncData().getSyncLink().size());
        removeElement(org);
    }

    @Test
    public void exportModplast() throws CommandException, SyncParameterException, IOException {
        // Given:
        VNAImportHelper.importFile(VNA_FILENAME_modplast);

        CnATreeElement org = loadElement(SOURCE_ID_modplast, EXT_ID_BP_ITNETWORK_modplast, false,
                false, false);

        // When:
        ExportCommand cmd = new ExportCommand(Arrays.asList(org), "testSourceId", false // no
                                                                                        // re-import
        );
        cmd = commandService.executeCommand(cmd);

        // Then:
        assertTrue("Export did not produce any data.",
                cmd.getResult() != null && cmd.getResult().length > 0);
        VeriniceArchive vna = new VeriniceArchive(cmd.getResult());

        SyncRequest syncRequest = JAXB.unmarshal(vna.getVeriniceXml(), SyncRequest.class);
        assertEquals(1, syncRequest.getSyncData().getSyncObject().size());
        assertEquals(18, syncRequest.getSyncData().getSyncObject().get(0).getChildren().size());
        List<SyncObject> allSyncObjects = getAllSyncObjects(syncRequest);
        assertEquals(5366, allSyncObjects.size());
        assertEquals(0, allSyncObjects.stream().flatMap(o -> o.getFile().stream()).count());
        assertEquals(18322, syncRequest.getSyncData().getSyncLink().size());
        removeElement(org);
    }

    private List<SyncObject> getAllSyncObjects(SyncRequest syncRequest) {
        List<SyncObject> result = new ArrayList<>();
        syncRequest.getSyncData().getSyncObject()
                .forEach(o -> addSyncObjectAndDescendants(result, o));
        return result;
    }

    private void addSyncObjectAndDescendants(List<SyncObject> result, SyncObject o) {
        result.add(o);
        o.getChildren().forEach(child -> addSyncObjectAndDescendants(result, child));
    }

}
