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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static sernet.verinice.oda.driver.impl.filters.Filters.and;
import static sernet.verinice.oda.driver.impl.filters.Filters.is;
import static sernet.verinice.oda.driver.impl.filters.Filters.noneOf;

import java.io.IOException;
import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.graph.IGraphService;
import sernet.verinice.model.bp.elements.BusinessProcess;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.service.commands.SyncParameterException;
import sernet.verinice.service.commands.crud.LoadBpProcessAndTargetObjects;
import sernet.verinice.service.test.helper.vnaimport.VNAImportHelper;

/**
 * Tests the command to load BP process and all linked target objects,
 * recursively to the leaf target objects with filters applied.
 * 
 */
@Transactional
@TransactionConfiguration(transactionManager = "txManager")
public class LoadBPProcessAndTargetObjectsFilterTest extends CommandServiceProvider {

    private static final Logger LOG = Logger
            .getLogger(LoadBPProcessAndTargetObjectsFilterTest.class);

    private static final String VNA_FILENAME = "LoadBpProcessAndTargObjTest.vna";
    private static final String SOURCE_ID = "ak-2019-09-09";
    private static final String EXT_ID_BP_ITNETWORK = "ENTITY_159007";

    private static final String WANTED_PROCESS_EXT_ID_1 = "ENTITY_4065966";
    private static final String WANTED_PROCESS_EXT_ID_2 = "ENTITY_4066052";
    private static final String UNWANTED_PROCESS_EXT_ID = "ENTITY_4065967";

    private static final String WANTED_PROCESS_TITLE_1 = "Customer-Relationship-Management";
    private static final String WANTED_PROCESS_TITLE_2 = "Personaldatenverarbeitung";
    private static final String UNWANTED_PROCESS_TITLE_1 = "Softwareentwicklung";

    private static final String WANTED_TARGETOBJECT_TITLE_1 = "Datenbankserver Finanzbuchhaltung";
    private static final String UNWANTED_TARGETOBJECT_TITLE_1 = "Serverraum Berlin";

    private static final String WANTED_TARGETOBJECT_EXTID_ID_1 = "ENTITY_4064118";
    private static final String UNWANTED_TARGETOBJECT_EXTID_1 = "ENTITY_328200";

    @Resource(name = "graphService")
    IGraphService graphService;

    @Before
    public void importData() throws IOException, CommandException, SyncParameterException {
        VNAImportHelper.importFile(VNA_FILENAME);
    }

    /**
     * Tests the command by loading only processes that are relevant for data
     * privacy and additionally filtering out all target objects of the type
     * "room" from the result.
     * 
     * @throws CommandException
     * 
     * @throws Exception
     */
    @Test
    public void loadsProcessesAndTargetObjectsWithFilter() throws CommandException {
        // Given:
        CnATreeElement org = loadElement(SOURCE_ID, EXT_ID_BP_ITNETWORK);
        Integer rootId = org.getDbId();

        // When:
        LoadBpProcessAndTargetObjects loadCommand = new LoadBpProcessAndTargetObjects(rootId);
        loadCommand.setFilterCriteria(
                and(is(BusinessProcess.TYPE_ID, "bp_businessprocess_privacy_yesno", "1"),
                        // check if this property is present - if it is, this is
                        // a room. get rid of it.
                        noneOf(is("bp_room_method_integrity", ".+"))));
        LoadBpProcessAndTargetObjects resultCommand = commandService.executeCommand(loadCommand);

        // Then:
        assertTrue(
                "A required process was missing in the result (EXT_ID " + WANTED_PROCESS_TITLE_1
                        + ")",
                isProcessPresent(resultCommand.getElements(), WANTED_PROCESS_EXT_ID_1));
        assertTrue(
                "A required process was missing in the result (EXT_ID " + WANTED_PROCESS_TITLE_2
                        + ")",
                isProcessPresent(resultCommand.getElements(), WANTED_PROCESS_EXT_ID_2));
        assertTrue(
                "A required target object was missing in the result (EXT_ID "
                        + WANTED_TARGETOBJECT_TITLE_1 + ")",
                isTargetObjectPresent(resultCommand.getElements(), WANTED_TARGETOBJECT_EXTID_ID_1));
        assertFalse(
                "An unwanted process was present in the result (EXT_ID " + UNWANTED_PROCESS_TITLE_1
                        + ")",
                isProcessPresent(resultCommand.getElements(), UNWANTED_PROCESS_EXT_ID));
        assertFalse(
                "An unwanted target object was present in the result (EXT_ID "
                        + UNWANTED_TARGETOBJECT_TITLE_1 + ")",
                isTargetObjectPresent(resultCommand.getElements(), UNWANTED_TARGETOBJECT_EXTID_1));
    }

    private boolean isProcessPresent(List<List<String>> elements, String wantedElementExtId)
            throws CommandException {
        String wantedElementDbId = loadElement(SOURCE_ID, wantedElementExtId).getDbId().toString();
        return elements.stream()
                .anyMatch(resultTable -> resultTable.get(0).equals(wantedElementDbId));
    }

    private boolean isTargetObjectPresent(List<List<String>> tableRows, String wantedElementExtId)
            throws CommandException {
        String wantedElementDbId = loadElement(SOURCE_ID, wantedElementExtId).getDbId().toString();
        return tableRows.stream().flatMap(row -> row.stream())
                .anyMatch(cell -> cell.equals(wantedElementDbId));
    }

}
