/*******************************************************************************
 * Copyright (c) 2012 Daniel Murygin.
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
package sernet.verinice.service.commands;

import sernet.verinice.model.common.CnATreeElement;
import de.sernet.sync.data.SyncObject;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 *
 */
public class ExportTransaction {
    
    private CnATreeElement element;
    
    private SyncObject target;

    public ExportTransaction() {
        super();
    }

    public ExportTransaction(CnATreeElement element) {
        this.element = element;
    }

    public CnATreeElement getElement() {
        return element;
    }

    public void setElement(CnATreeElement element) {
        this.element = element;
    }

    public SyncObject getTarget() {
        return target;
    }

    public void setTarget(SyncObject target) {
        this.target = target;
    }
        
}
