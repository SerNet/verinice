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

import sernet.verinice.model.common.CnATreeElement;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 *
 */
public class CacheObject {

    private CnATreeElement element;
    
    private boolean childrenPropertiesLoaded;
    
    private ChildrenExist hasChildren;

    public CacheObject(CnATreeElement element, boolean childrenPropertiesLoaded) {
        ChildrenExist hasChildren;     
        if(element.getChildren().size()>0) {
            hasChildren = ChildrenExist.YES; 
        } else {
            hasChildren = ChildrenExist.NO; 
        }
        this.element = element;
        this.childrenPropertiesLoaded = childrenPropertiesLoaded;
        this.hasChildren = hasChildren;
    }
    
    /**
     * @param element
     * @param childrenPropertiesLoaded
     */
    public CacheObject(CnATreeElement element, boolean childrenLoaded, ChildrenExist hasChildren) {
        super();
        if(element==null) {
            throw new NullPointerException("Element must not be null.");
        }
        this.element = element;
        this.childrenPropertiesLoaded = childrenLoaded;
        this.hasChildren = hasChildren;
    }
    
    /**
     * @return the element
     */
    public CnATreeElement getElement() {
        return element;
    }

    /**
     * @return the childrenPropertiesLoaded
     */
    public boolean isChildrenPropertiesLoaded() {
        return childrenPropertiesLoaded;
    }

    /**
     * @return the hasChildren
     */
    public ChildrenExist getHasChildren() {
        return hasChildren;
    }
       
}
