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
package sernet.verinice.rcp.tree;

import sernet.verinice.model.bsi.IBSIStrukturKategorie;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.IISO27kGroup;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 *
 */
public enum ChildrenExist {
    
    YES, NO, UNKNOWN;
    
    public static ChildrenExist convert(Boolean hasChildren) {
        ChildrenExist childrenState = ChildrenExist.UNKNOWN;
        if(hasChildren!=null) {
            if(hasChildren.booleanValue()) {
                childrenState = ChildrenExist.YES;
            } else {
                childrenState = ChildrenExist.NO;
            }
        }
        return childrenState;
    }
    
    public static boolean isAlwaysChildless(CnATreeElement element) {
        return !(element instanceof IISO27kGroup)
            && !(element instanceof IBSIStrukturKategorie);
    }
}
