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

import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.apache.log4j.Logger;
import org.junit.Test;

import sernet.verinice.interfaces.CommandException;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.service.commands.ExportCommand;
import sernet.verinice.service.commands.SyncParameter;
import sernet.verinice.service.commands.SyncParameterException;
import sernet.verinice.service.test.helper.vnaimport.BeforeAllVNAImportHelper;

/**
 * Tests the command to export data to a VNA.
 * 
 */
public class ExportCommandTest extends BeforeAllVNAImportHelper {

    private static final Logger LOG = Logger
            .getLogger(ExportCommandTest.class);

    private static final String VNA_FILENAME = "Export_Test.vna";
    private static final String SOURCE_ID = "41a219";
    private static final String EXT_ID_BP_ITNETWORK = "1f581c34-b512-46ba-ab74-7d9b776748dc";
    
    /**
     * Tests the command by loading only processes that are relevant for data
     * privacy and additionally filtering out all target objects of the type "room" from
     * the result.
     * @throws CommandException 
     * 
     * @throws Exception
     */
    @Test
    public void exportScope() throws CommandException  {
        // Given:
        CnATreeElement org = loadElement(SOURCE_ID, EXT_ID_BP_ITNETWORK);

        // When:
        ExportCommand cmd = new ExportCommand(Arrays.asList(org),
                "testSourceId", 
                false // no re-import
        );
        cmd = commandService.executeCommand(cmd);

        // Then:
        assertTrue("Export did not produce any data.", 
                cmd.getResult() != null && cmd.getResult().length>0);
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
