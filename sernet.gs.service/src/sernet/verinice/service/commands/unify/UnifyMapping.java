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
package sernet.verinice.service.commands.unify;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import sernet.verinice.service.commands.UnifyElement;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 *
 */
public class UnifyMapping implements Serializable {

    private UnifyElement sourceElement;
    
    private List<UnifyElement> destinationElements;

    /**
     * @param sourceElement
     */
    public UnifyMapping(UnifyElement sourceElement) {
        super();
        this.sourceElement = sourceElement;
    }

    /**
     * @param sourceElement
     * @param destinationElement
     */
    public UnifyMapping(UnifyElement sourceElement, UnifyElement destinationElement) {
        this(sourceElement);
        destinationElements = createDestinationElements();
        addDestinationElement(destinationElement);
    }
    
    public String getDestinationText() {
        StringBuilder sb = null;
        for (UnifyElement destination : destinationElements) {
            if(sb!=null) {
                sb.append(", \n");
            } else {
                sb = new StringBuilder();
            }
            sb.append(destination.getTitle());
        }
        String text = "";
        if(sb!=null) {
            text = sb.toString();
        }
        return text;
    }

    public UnifyElement getSourceElement() {
        return sourceElement;
    }

    public List<UnifyElement> getDestinationElements() {
        if(destinationElements==null) {
            destinationElements = createDestinationElements();
        }
        return destinationElements;
    }

    private List<UnifyElement> createDestinationElements() {
        destinationElements = new ArrayList<UnifyElement>(2);
        return destinationElements;
    }

    public void setSourceElement(UnifyElement sourceElement) {
        this.sourceElement = sourceElement;
    }

    public void addDestinationElement(UnifyElement destinationElement) {
        getDestinationElements().add(destinationElement);
    } 
}
