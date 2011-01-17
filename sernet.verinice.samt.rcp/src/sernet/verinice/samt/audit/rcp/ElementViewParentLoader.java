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

import sernet.verinice.iso27k.rcp.IParentLoader;
import sernet.verinice.iso27k.service.Retriever;
import sernet.verinice.model.common.CnATreeElement;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 *
 */
public class ElementViewParentLoader implements IParentLoader {

    /* (non-Javadoc)
     * @see sernet.verinice.iso27k.rcp.IParentLoader#getParent(sernet.verinice.model.common.CnATreeElement)
     */
    @Override
    public CnATreeElement getParent(CnATreeElement child) {
        CnATreeElement parent = null;
        if(child!=null) {
            child = retrieveParent(child);
        }
        return child.getParent(); 
    }
    
    private CnATreeElement retrieveParent(CnATreeElement element) {
        CnATreeElement parent = element.getParent();
        if(parent!=null) {
            parent = Retriever.checkRetrieveParent(parent);
            element.setParent(retrieveParent(parent));
        }
        return element;
    }

}
