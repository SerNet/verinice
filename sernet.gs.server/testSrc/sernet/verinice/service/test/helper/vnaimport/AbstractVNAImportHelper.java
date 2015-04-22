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
package sernet.verinice.service.test.helper.vnaimport;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import sernet.verinice.interfaces.CommandException;
import sernet.verinice.model.bsi.ITVerbund;
import sernet.verinice.model.bsi.ImportBsiGroup;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.ImportIsoGroup;
import sernet.verinice.model.iso27k.Organization;
import sernet.verinice.service.commands.LoadElementByTypeId;
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
            this.syncCommand = importFile(vnaFilePath, syncParameter);            
        } catch (Exception e) {
            log.error("import of " + vnaFilePath + " aborted", e);
            throw e;
        }
    }

    final protected SyncCommand importFile(String path, SyncParameter syncParameter) throws IOException, CommandException {
        
        byte[] it_network_vna = FileUtils.readFileToByteArray(new File(path));
        SyncCommand syncCommand = new SyncCommand(syncParameter, it_network_vna);
        return commandService.executeCommand(syncCommand);        
    }

    abstract protected String getFilePath();

    abstract protected SyncParameter getSyncParameter() throws SyncParameterException;
    
    private <T extends CnATreeElement> RemoveElement<T> removeElement(T element) throws CommandException {
        RemoveElement<T> removeCommand = new RemoveElement<T>(element);
        return commandService.executeCommand(removeCommand);
    }
    
    private void removeAllElementsByType(String type) throws CommandException {
        LoadElementByTypeId loadElementByTypeId = new LoadElementByTypeId(type);
        loadElementByTypeId = commandService.executeCommand(loadElementByTypeId);

        for (CnATreeElement element : loadElementByTypeId.getElementList()) {
            removeElement(element);
        }
    }

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
            
            // clean up the parents of imported cnatreeelements
            removeAllElementsByType(ImportBsiGroup.TYPE_ID);
            removeAllElementsByType(ImportIsoGroup.TYPE_ID);            
            
        } catch (CommandException e) {
            log.error("deleting element of " + vnaFilePath + " failed", e);
            throw e;
        }
    }
    
    public Set<Integer> getScopeIds() {
        Set<CnATreeElement> importedScopes = syncCommand.getImportRootObject();
        Set<Integer> scopeIds = new HashSet<Integer>(importedScopes.size());
        for (CnATreeElement scope : importedScopes) {
            scopeIds.add(scope.getDbId());         
        }
        return scopeIds;
    }
}
