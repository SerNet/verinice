/*******************************************************************************
 * Copyright (c) 2013 Daniel Murygin.
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
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.service.test;

import static org.junit.Assert.assertNotNull;

import javax.annotation.Resource;

import org.junit.Test;

import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.service.commands.LoadElementByUuid;

/**
 *
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class CommandServiceTest extends ContextConfiguration {
    
    @Resource(name="commandService")
    private ICommandService commandService;
    
    @Test
    public void testCommandService() throws Exception {
        LoadElementByUuid<CnATreeElement> command = new LoadElementByUuid<CnATreeElement>("42340c5c-fa8f-46f3-8e96-6dadddac013e");
        command = commandService.executeCommand(command);
        CnATreeElement element = command.getElement();
        assertNotNull(element);
    }



}
