/*******************************************************************************
 * Copyright (c) 2015 Daniel Murygin.
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
package sernet.gs.ui.rcp.gsimport;

import java.util.List;

import sernet.verinice.interfaces.CommandException;
import sernet.verinice.model.common.Link;
import sernet.verinice.service.commands.CreateMultipleLinks;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class LinkCreater extends ChunkExecuter<Link> {

    public LinkCreater(List<Link> elementList, IProgress monitor) {
        super(elementList, monitor);
    }

    /* (non-Javadoc)
     * @see sernet.gs.ui.rcp.gsimport.ChunkExecuter#executeChunk(java.util.List)
     */
    @Override
    protected void executeChunk(List<Link> chunkList) throws CommandException {        
        CreateMultipleLinks createMultipleLinks = new CreateMultipleLinks(chunkList);
        getCommandService().executeCommand(createMultipleLinks);
    }

}
