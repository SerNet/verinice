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

import java.io.Serializable;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 *
 */
public class UnifyMapping implements Serializable {

    private UnifyElement sourceElement;
    
    private UnifyElement destinationElement;

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
        super();
        this.sourceElement = sourceElement;
        this.destinationElement = destinationElement;
    }

    /**
     * @return the sourceElement
     */
    public UnifyElement getSourceElement() {
        return sourceElement;
    }

    /**
     * @return the destinationElement
     */
    public UnifyElement getDestinationElement() {
        return destinationElement;
    }

    /**
     * @param sourceElement the sourceElement to set
     */
    public void setSourceElement(UnifyElement sourceElement) {
        this.sourceElement = sourceElement;
    }

    /**
     * @param destinationElement the destinationElement to set
     */
    public void setDestinationElement(UnifyElement destinationElement) {
        this.destinationElement = destinationElement;
    }
    
    
    
    
}
