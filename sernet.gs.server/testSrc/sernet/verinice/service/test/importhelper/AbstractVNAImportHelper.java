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
package sernet.verinice.service.test.importhelper;

import java.io.File;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import sernet.verinice.interfaces.CommandException;
import sernet.verinice.model.bsi.ITVerbund;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Organization;
import sernet.verinice.service.commands.RemoveElement;
import sernet.verinice.service.commands.SyncCommand;
import sernet.verinice.service.commands.SyncParameter;
import sernet.verinice.service.commands.SyncParameterException;
import sernet.verinice.service.test.CommandServiceProvider;

/**
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 * 
 */
abstract public class AbstractVNAImportHelper extends CommandServiceProvider {

    private Logger log = Logger.getLogger(AbstractVNAImportHelper.class);

    private String vnaFilePath;

    private SyncParameter syncParameter;

    private SyncCommand syncCommand;

    public void setUp() throws Exception {

        try {
            this.vnaFilePath = getFilePath();
            this.syncParameter = getSyncParameter();

            byte[] it_network_vna = FileUtils.readFileToByteArray(new File(vnaFilePath));

            this.syncCommand = new SyncCommand(syncParameter, it_network_vna);
            this.syncCommand = commandService.executeCommand(syncCommand);

        } catch (Exception e) {
            log.debug("import of " + vnaFilePath + " aborted", e);
            throw e;
        }
    }

    abstract protected String getFilePath();

    abstract protected SyncParameter getSyncParameter() throws SyncParameterException;

    public void tearDown() throws CommandException {
        try {
            Set<CnATreeElement> importedElements = this.syncCommand.getElementSet();
            for (CnATreeElement element : importedElements) {
                
                if (element instanceof Organization) {
                    RemoveElement<Organization> removeCommand = new RemoveElement<Organization>((Organization) element);
                    commandService.executeCommand(removeCommand);
                }
                
                else if (element instanceof ITVerbund) {
                    RemoveElement<ITVerbund> removeCommand = new RemoveElement<ITVerbund>((ITVerbund) element);
                    commandService.executeCommand(removeCommand);
                }
            }
        } catch (CommandException e) {
            log.error("deleting element of " + vnaFilePath + " failed", e);
            throw e;
        }
    }
}
