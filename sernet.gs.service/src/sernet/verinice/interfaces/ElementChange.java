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
package sernet.verinice.interfaces;

import java.util.Calendar;
import java.util.Date;

import sernet.verinice.model.common.CnATreeElement;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 *
 */
public class ElementChange {

    private CnATreeElement element;
    
    private int changeType;
    
    private Date time;

    /**
     * @param element
     * @param changeType
     */
    public ElementChange(CnATreeElement element, int changeType) {
        super();
        this.element = element;
        this.changeType = changeType;
        this.time = Calendar.getInstance().getTime();
    }

    public CnATreeElement getElement() {
        return element;
    }

    public int getChangeType() {
        return changeType;
    }

    public void setElement(CnATreeElement element) {
        this.element = element;
    }

    public void setChangeType(int changeType) {
        this.changeType = changeType;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + changeType;
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
        ElementChange other = (ElementChange) obj;
        if (changeType != other.changeType){
            return false;
        }
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
