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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.junit.Test;

import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.service.commands.SyncCommand;
import sernet.verinice.service.commands.SyncParameter;
import sernet.verinice.service.commands.SyncParameterException;
import sernet.verinice.service.test.helper.util.BooleanCombinator;

/**
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 * 
 */
public class SyncParameterTest extends ContextConfiguration {

    private static final String IT_NETWORK_VNA = "IT_Network.vna";
    private static final String IT_NETWORK_XML = "IT_Network.xml";

    private Logger log = Logger.getLogger(SyncParameter.class);

    @Resource(name = "commandService")
    protected ICommandService commandService;
    
    @Test
    public void testSyncSettingsWithInsertUpdateDelete() throws IOException, CommandException, SyncParameterException {
        try {
            for (boolean[] arguments : new BooleanCombinator(4).getBooleanList()) {
                SyncParameter syncParameter = new SyncParameter(arguments[0], arguments[1], arguments[2], arguments[3]);
                importFromByteArray(syncParameter, IT_NETWORK_VNA);
                importFromFile(syncParameter, IT_NETWORK_VNA);
            }
        } catch (IOException ex) {
            log.debug("reading test file failed");
            throw ex;
        }
    }

    @Test(expected = SyncParameterException.class)
    public void testSyncSettingsInsertUpdateDeleteFormat() throws IOException, CommandException, SyncParameterException {
        try {
            testSyncSettingsInVNAFormat(IT_NETWORK_VNA);
            testSyncSettingsInXMLPureFormat(IT_NETWORK_XML);
        } catch (IOException ex) {
            log.debug("reading test file failed");
            throw ex;
        }
    }    

    private void testSyncSettingsInVNAFormat(String file) throws IOException, CommandException, SyncParameterException {

        for (SyncParameter syncParameter : getAllSyncParameterCombinations()) {
            importFromByteArray(syncParameter, file);
            importFromFile(syncParameter, file);
        }

    }

    private void testSyncSettingsInXMLPureFormat(String file) throws IOException, CommandException, SyncParameterException {

        for (boolean[] arguments : new BooleanCombinator(4).getBooleanList()) {
            SyncParameter syncParameter = new SyncParameter(arguments[0], arguments[1], arguments[2], arguments[3], SyncParameter.EXPORT_FORMAT_XML_PURE);
            importFromByteArray(syncParameter, file);
            // testVnAImportFromFile(syncParameter, file);
        }
    }

    private List<SyncParameter> getAllSyncParameterCombinations() throws SyncParameterException {

        List<SyncParameter> syncParameterCombinations = new ArrayList<SyncParameter>(0);
        for (boolean[] arguments : new BooleanCombinator(4).getBooleanList()) {
            syncParameterCombinations.add(new SyncParameter(arguments[0], arguments[1], arguments[2], arguments[3], SyncParameter.EXPORT_FORMAT_DEFAULT));
            syncParameterCombinations.add(new SyncParameter(arguments[0], arguments[1], arguments[2], arguments[3], SyncParameter.EXPORT_FORMAT_VERINICE_ARCHIV));
        }

        return syncParameterCombinations;
    }

    private void importFromByteArray(SyncParameter syncParameter, String file) throws IOException, CommandException {
        byte[] it_network_vna = FileUtils.readFileToByteArray(new File(getClass().getResource(file).getPath()));
        SyncCommand syncCommand = new SyncCommand(syncParameter, it_network_vna);
        commandService.executeCommand(syncCommand);
    }

    private void importFromFile(SyncParameter syncParameter, String file) throws IOException, CommandException {
        SyncCommand syncCommand = new SyncCommand(syncParameter, getFilePathRelativeToThisClass(file));
        commandService.executeCommand(syncCommand);
    };

    private String getFilePathRelativeToThisClass(String file) {
        return getClass().getResource(file).getPath();
    }

}
