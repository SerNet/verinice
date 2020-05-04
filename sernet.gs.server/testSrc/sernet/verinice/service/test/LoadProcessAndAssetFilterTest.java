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
import static sernet.verinice.oda.driver.impl.filters.Filters.*;
import static sernet.verinice.model.iso27k.Process.PROP_NAME;

import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.junit.Test;

import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.graph.IGraphService;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.service.commands.SyncParameter;
import sernet.verinice.service.commands.SyncParameterException;
import sernet.verinice.service.commands.crud.LoadProcessAndAsset;
import sernet.verinice.service.test.helper.vnaimport.BeforeAllVNAImportHelper;

/**
 * Tests the LoadProcessAndAssetCommand with filters applied.
 * 
 */
public class LoadProcessAndAssetFilterTest extends BeforeAllVNAImportHelper {


    private static final Logger LOG = Logger.getLogger(LoadProcessAndAssetFilterTest.class);

    private static final String VNA_FILENAME = "LoadProcessAndAssetTest.vna";
    private static final String SOURCE_ID = "ak-2019-09-03";
    private static final String EXT_ID_ORG = "ENTITY_15063";

    private static final String WANTED_PROCESS_EXT_ID_1 = "ENTITY_24181"; // GDPDR process 1
    private static final String WANTED_PROCESS_EXT_ID_2 = "ENTITY_24271"; // GDPDR process 2
    private static final String UNWANTED_PROCESS_EXT_ID = "ENTITY_24091"; // not relevant process 1
    
    private static final String WANTED_PROCESS_TITLE_1 = "GDPDR-Process \\d";
    private static final String WANTED_PROCESS_TITLE_2 = "GDPDR-Process \\d";
    private static final String WANTED_PROCESS_TITLE_REGEX = "GDPDR-Process \\d";

    private static final String UNWANTED_PROCESS_TITLE_1 = "NOT relevant process 1"; // not relevant process 1
    private static final String UNWANTED_PROCESS_TITLE_REGEX = "NOT relevant process \\d"; // not relevant process 1

    @Resource(name = "graphService")
    IGraphService graphService;

    @Test
    public void loadsAndFiltersProcessesBySingleBooleanCriteria() throws Exception {
        // Given:
        CnATreeElement org = loadElement(SOURCE_ID, EXT_ID_ORG);
        Integer rootId = org.getDbId();

        // When:
        LoadProcessAndAsset loadCommand = new LoadProcessAndAsset(rootId);
        loadCommand.setFilterCriteria( is("process_privacy_yesno", "1") );
        LoadProcessAndAsset command = commandService.executeCommand(loadCommand);

        assertFilterResultsAreCorrect(command);
    }

    private void assertFilterResultsAreCorrect(LoadProcessAndAsset command)
            throws CommandException {
        // Then:
        assertFalse("The command returned no elements at all", command.getElements().isEmpty());
        assertTrue("A required process was missing in the result",
                isProcessPresent(command.getElements(), WANTED_PROCESS_EXT_ID_1));
        assertTrue("A required process was missing in the result",
                isProcessPresent(command.getElements(), WANTED_PROCESS_EXT_ID_2));
        assertTrue("An unwanted process was present in the result",
                !isProcessPresent(command.getElements(), UNWANTED_PROCESS_EXT_ID));
        assertTrue("No assets were loaded for the wanted processes",
                isAssetsPresent(command.getElements()));
    }
    
    private void assertUnfilteredResultsAreCorrect(LoadProcessAndAsset command) throws CommandException {
        assertFalse("The command returned no elements at all", command.getElements().isEmpty());
        assertTrue("A required process was missing in the result",
                isProcessPresent(command.getElements(), WANTED_PROCESS_EXT_ID_1));
        assertTrue("A required process was missing in the result",
                isProcessPresent(command.getElements(), WANTED_PROCESS_EXT_ID_2));
        assertTrue("A required process was present in the result",
                isProcessPresent(command.getElements(), UNWANTED_PROCESS_EXT_ID));
        assertTrue("No assets were loaded for the wanted processes",
                isAssetsPresent(command.getElements()));
    }

    @Test
    public void loadsAndFiltersProcessesByAnyOfTwoSelectionCriteria() throws Exception {
        // Given:
        CnATreeElement org = loadElement(SOURCE_ID, EXT_ID_ORG);
        Integer rootId = org.getDbId();

        // When:
        LoadProcessAndAsset loadCommand = new LoadProcessAndAsset(rootId);
        loadCommand.setFilterCriteria( or(
                is(PROP_NAME, WANTED_PROCESS_TITLE_1),
                is(PROP_NAME, WANTED_PROCESS_TITLE_2)) );
        LoadProcessAndAsset command = commandService.executeCommand(loadCommand);

        assertFilterResultsAreCorrect(command);
    }

    @Test
    public void loadsAndFiltersProcessesByARegularExpression() throws Exception {
        // Given:
        CnATreeElement org = loadElement(SOURCE_ID, EXT_ID_ORG);
        Integer rootId = org.getDbId();

        // When:
        LoadProcessAndAsset loadCommand = new LoadProcessAndAsset(rootId);
        loadCommand.setFilterCriteria( is(PROP_NAME, WANTED_PROCESS_TITLE_REGEX) );
        LoadProcessAndAsset command = commandService.executeCommand(loadCommand);

        assertFilterResultsAreCorrect(command);
    }

    @Test
    public void loadsAndFiltersProcessesByAllOfTwoCriteria() throws Exception {
        // Given:
        CnATreeElement org = loadElement(SOURCE_ID, EXT_ID_ORG);
        Integer rootId = org.getDbId();

        // When:
        LoadProcessAndAsset loadCommand = new LoadProcessAndAsset(rootId);
        loadCommand.setFilterCriteria( and(
                is(PROP_NAME, WANTED_PROCESS_TITLE_1),
                is(PROP_NAME, WANTED_PROCESS_TITLE_REGEX)) );
        LoadProcessAndAsset command = commandService.executeCommand(loadCommand);

        assertFilterResultsAreCorrect(command);
    }

    @Test
    public void loadsAndFiltersProcessesByNoneOfTwoCriteria() throws Exception {
        // Given:
        CnATreeElement org = loadElement(SOURCE_ID, EXT_ID_ORG);
        Integer rootId = org.getDbId();

        // When:
        LoadProcessAndAsset loadCommand = new LoadProcessAndAsset(rootId);
        loadCommand.setFilterCriteria( noneOf(
                is(PROP_NAME, UNWANTED_PROCESS_TITLE_1),
                is(PROP_NAME, UNWANTED_PROCESS_TITLE_REGEX)) );
        LoadProcessAndAsset command = commandService.executeCommand(loadCommand);

        assertFilterResultsAreCorrect(command);
    }

    @Test
    public void loadsAndFiltersProcessesByNotAllOfTwoCriteria() throws Exception {
        // Given:
        CnATreeElement org = loadElement(SOURCE_ID, EXT_ID_ORG);
        Integer rootId = org.getDbId();

        // When:
        LoadProcessAndAsset loadCommand = new LoadProcessAndAsset(rootId);
        loadCommand.setFilterCriteria( notAll(
                is(PROP_NAME, UNWANTED_PROCESS_TITLE_1),
                is(PROP_NAME, UNWANTED_PROCESS_TITLE_REGEX)) );
        LoadProcessAndAsset command = commandService.executeCommand(loadCommand);

        assertFilterResultsAreCorrect(command);
    }
    
    @Test
    public void loadsAndFiltersProcessesWithoutFilter() throws Exception {
        // Given:
        CnATreeElement org = loadElement(SOURCE_ID, EXT_ID_ORG);
        Integer rootId = org.getDbId();

        // When:
        LoadProcessAndAsset loadCommand = new LoadProcessAndAsset(rootId);
        // do not set a filter
        LoadProcessAndAsset command = commandService.executeCommand(loadCommand);

        // Then:
        assertUnfilteredResultsAreCorrect(command);
    }

    @Test
    public void loadsAndFiltersProcessesByNonExistingPropertyId() throws Exception {
        // Given:
        CnATreeElement org = loadElement(SOURCE_ID, EXT_ID_ORG);
        Integer rootId = org.getDbId();

        // When:
        LoadProcessAndAsset loadCommand = new LoadProcessAndAsset(rootId);
        loadCommand.setFilterCriteria(is("this_is_not_a_property", "foobaar"));
        LoadProcessAndAsset command = commandService.executeCommand(loadCommand);

        // Then:
        assertTrue("A result was returned for a non-existing property.", command.getElements().isEmpty());
    }

    private boolean isAssetsPresent(List<List<String>> resultTable) {
        return resultTable.stream().allMatch(row -> row.size() > 1);
    }

    private boolean isProcessPresent(List<List<String>> elements, String wantedProcessExtId)
            throws CommandException {
        String wantedProcessDbId = loadElement(SOURCE_ID, wantedProcessExtId).getDbId().toString();
        return elements.stream()
                .anyMatch(resultTable -> resultTable.get(0).equals(wantedProcessDbId));
    }

    @Override
    protected String getFilePath() {
        return this.getClass().getResource(VNA_FILENAME).getPath();
    }

    @Override
    protected SyncParameter getSyncParameter() throws SyncParameterException {
        return new SyncParameter(true, true, true, false,
                SyncParameter.EXPORT_FORMAT_VERINICE_ARCHIV);
    }

}
