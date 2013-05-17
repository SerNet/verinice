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
package sernet.verinice.web;

import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.IISO27kGroup;

/**
 * Wrapper class for web frontend to show information about a {@link CnATreeElement}
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class ElementInformation {

    private CnATreeElement element;
    
    private static final int MAX_TITLE_LENGTH = 100;

    public ElementInformation(CnATreeElement element) {
        super();
        if(element==null) {
            throw new IllegalArgumentException("Element must not be null.");
        }
        this.element = element;
    }
    
    public CnATreeElement getElement() {
        return element;
    }

    public void setElement(CnATreeElement element) {
        this.element = element;
    }

    public String getTitle() {
        String title = element.getTitle();
        if(title.length()>MAX_TITLE_LENGTH) {
            title = title.substring(0, MAX_TITLE_LENGTH - 1) + "...";
        }
        return title;
    }
    
    public String getIcon() {
        String path = Icons.ICONS.get(element.getTypeId());
        if(path==null && element instanceof IISO27kGroup) {
            path = Icons.FOLDER;
        }
        return path;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((element == null) ? 0 : element.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj){
            return true;
        }
        if (obj == null){
            return false;
        }
        if (getClass() != obj.getClass()){
            return false;
        }
        ElementInformation other = (ElementInformation) obj;
        if (element == null) {
            if (other.element != null){
                return false;
            }
        } else if (!element.equals(other.element)){
            return false;
        }
        return true;
    }
    
    
}
