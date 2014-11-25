/*******************************************************************************
 * Copyright (c) 2010 Daniel Murygin.
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
package sernet.verinice.samt.audit.rcp;

import java.util.Arrays;

import sernet.verinice.iso27k.service.commands.LoadLinkedElements;
import sernet.verinice.service.commands.LoadElementByTypeId;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class ElementViewCommandFactory implements ICommandFactory {

    private String elementTypeId;
    
    private String groupTypeId;
    
    public ElementViewCommandFactory(String elementTypeId, String groupTypeId) {
        super();
        this.elementTypeId = elementTypeId;
        this.groupTypeId = groupTypeId;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.samt.audit.rcp.ICommandFactory#getElementCommand()
     */
    @Override
    public LoadElementByTypeId getElementCommand() {
        return new LoadElementByTypeId(groupTypeId);
    }

    /* (non-Javadoc)
     * @see sernet.verinice.samt.audit.rcp.ICommandFactory#getLinkedElementCommand(int)
     */
    @Override
    public LoadLinkedElements getLinkedElementCommand(int selectedId) {
        return new LoadLinkedElements(Arrays.asList(new String[]{elementTypeId,groupTypeId}),selectedId);
    }

    public String getElementTypeId() {
        return elementTypeId;
    }


    public String getGroupTypeId() {
        return groupTypeId;
    }

}
