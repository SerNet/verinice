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

import org.junit.AfterClass;
import org.junit.Before;

import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.service.commands.SyncCommand;
import sernet.verinice.service.commands.SyncParameter;
import sernet.verinice.service.commands.SyncParameterException;
import sernet.verinice.service.test.CommandServiceProvider;

/**
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 *
 */
public abstract class BeforeAllVNAImportHelper extends CommandServiceProvider {

    private static SyncCommand syncCommand;
    private static IBaseDao<CnATreeElement, Integer> elementDaoStaticRef;

    @Before
    public void setUp() throws IOException, CommandException, SyncParameterException {
        if (syncCommand == null) {
            syncCommand = VNAImportHelper.importFile(getFilePath(), getSyncParameter());
            elementDaoStaticRef = elementDao;
        }
    }

    @AfterClass
    public static void tearDown() throws CommandException {
        VNAImportHelper.tearDown(syncCommand, elementDaoStaticRef);
        syncCommand = null;
    }

    protected abstract String getFilePath();

    protected abstract SyncParameter getSyncParameter() throws SyncParameterException;

}
