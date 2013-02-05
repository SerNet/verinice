/*******************************************************************************
 * Copyright (c) 2013 Daniel Murygin.
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
package sernet.verinice.bpm.gsm;

import java.io.Serializable;
import java.util.Set;

import sernet.verinice.model.common.CnATreeElement;

/**
 *
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class GsmServiceParameter implements Serializable {

    private CnATreeElement controlGroup;
    
    private CnATreeElement person;
    
    private Set<CnATreeElement> elementSet;

    public GsmServiceParameter() {
        super();
    }

    /**
     * @param controlGroup
     * @param person
     */
    public GsmServiceParameter(CnATreeElement controlGroup, CnATreeElement person) {
        super();
        this.controlGroup = controlGroup;
        this.person = person;
    }

    public CnATreeElement getControlGroup() {
        return controlGroup;
    }

    public void setControlGroup(CnATreeElement controlGroup) {
        this.controlGroup = controlGroup;
    }

    public CnATreeElement getPerson() {
        return person;
    }

    public void setPerson(CnATreeElement person) {
        this.person = person;
    }

    public Set<CnATreeElement> getElementSet() {
        return elementSet;
    }

    public void setElementSet(Set<CnATreeElement> elementSet) {
        this.elementSet = elementSet;
    }
    
    
}
