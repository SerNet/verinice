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

import java.io.IOException;

import org.apache.log4j.Logger;

import sernet.hui.common.VeriniceContext;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.model.bp.groups.ImportBpGroup;
import sernet.verinice.model.bsi.ImportBsiGroup;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.ImportIsoGroup;
import sernet.verinice.service.commands.LoadElementByTypeId;
import sernet.verinice.service.commands.SyncCommand;
import sernet.verinice.service.commands.SyncParameter;
import sernet.verinice.service.commands.SyncParameterException;

/**
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 * 
 */
public final class VNAImportHelper {

    private static final Logger log = Logger.getLogger(VNAImportHelper.class);

    public static SyncCommand importFile(String path, SyncParameter syncParameter)
            throws IOException, CommandException {

        SyncCommand syncCommand = new SyncCommand(syncParameter, path);
        return getCommandService().executeCommand(syncCommand);
    }

    public static SyncCommand importFile(String filename, boolean insert, boolean update,
            boolean delete, boolean integrate) throws IOException, CommandException {
        try {
            SyncParameter syncParameter = new SyncParameter(insert, update, delete, integrate);
            return importFile(VNAImportHelper.class.getResource("../../" + filename).getPath(),
                    syncParameter);
        } catch (SyncParameterException e) {
            throw new RuntimeException(e);
        }
    }

    public static SyncCommand importFile(String filename) throws IOException, CommandException {
        return importFile(filename, true, false, false, false);
    }

    private static void removeAllElementsByType(String type,
            IBaseDao<CnATreeElement, Integer> elementDao) throws CommandException {
        LoadElementByTypeId loadElementByTypeId = new LoadElementByTypeId(type);
        loadElementByTypeId = getCommandService().executeCommand(loadElementByTypeId);

        for (CnATreeElement element : loadElementByTypeId.getElementList()) {
            elementDao.delete(element);
        }
    }

    public static void tearDown(SyncCommand syncCommand,
            IBaseDao<CnATreeElement, Integer> elementDao) throws CommandException {
        try {
            // clean up the parents of imported cnatreeelements
            removeAllElementsByType(ImportBsiGroup.TYPE_ID, elementDao);
            removeAllElementsByType(ImportIsoGroup.TYPE_ID, elementDao);
            removeAllElementsByType(ImportBpGroup.TYPE_ID, elementDao);

        } catch (CommandException e) {
            log.error("deleting element of " + syncCommand + " failed", e);
            throw e;
        }
    }

    private static ICommandService getCommandService() {
        return (ICommandService) VeriniceContext.get(VeriniceContext.COMMAND_SERVICE);
    }

    private VNAImportHelper() {
    }
}
