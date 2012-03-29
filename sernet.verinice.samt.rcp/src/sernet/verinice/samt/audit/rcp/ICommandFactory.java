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

import sernet.verinice.iso27k.service.commands.LoadLinkedElements;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.service.commands.LoadElementByTypeId;

/**
 * Returns commands to load {@link CnATreeElement}s
 * ICommandFactory is used in {@link GenericElementView}
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public interface ICommandFactory {

    public String getElementTypeId();
    
    public String getGroupTypeId();
    
    /**
     * @return a command which loads {@link CnATreeElement}s
     * for a given class
     */
    public LoadElementByTypeId getElementCommand();
    
    /**
     * Returns a command which loads {@link CnATreeElement}s
     * which are linked to a {@link CnATreeElement} with primary key selectedId
     * 
     * @param selectedId primary key of an {@link CnATreeElement}
     * @return a command which loads linked {@link CnATreeElement}s
     */
    public LoadLinkedElements getLinkedElementCommand(int selectedId);

}
