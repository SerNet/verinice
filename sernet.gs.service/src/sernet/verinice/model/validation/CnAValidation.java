/*******************************************************************************
 * Copyright (c) 2012 Sebastian Hagedorn <sh@sernet.de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details.
 *     You should have received a copy of the GNU General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Sebastian Hagedorn <sh@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.model.validation;

import java.io.Serializable;

import sernet.hui.common.VeriniceContext;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.hui.common.connect.ITypedElement;
import sernet.verinice.model.common.CnATreeElement;

/**
 * Validation item. Represents a failed validation on one property of one {@link CnATreeElement}
 * provides a hint, that tells the user how to fix the failed validation
 */
@SuppressWarnings("serial")
public class CnAValidation implements Serializable, Comparable<CnAValidation>, ITypedElement {
    
    public static final String TYPE_ID = "cna_validation";
    
    private String propertyId;
    
    private Integer elmtDbId;
    
    private String elmtTitle;
    
    private String hintId;
    
    private String elementType;
    
    // dbID of this
    private Integer dbId;
    
    private Integer scopeId;
    
    public CnAValidation(){
        super();
    }
    
    
    
    public String getPropertyId() {
        return propertyId;
    }



    public void setPropertyId(String propertyId) {
        this.propertyId = propertyId;
    }



    public Integer getElmtDbId() {
        return elmtDbId;
    }



    public void setElmtDbId(Integer elmtDbId) {
        this.elmtDbId = elmtDbId;
    }



    public String getHintId() {
        return hintId;
    }



    public void setHintId(String hintId) {
        this.hintId = hintId;
    }



    /**
     * sorted by typeID of cnaTreeElement that validation fails
     */
    @Override
    public int compareTo(CnAValidation o) {
        int result = 1; // this is greater
        if(o!=null && o.getElmtDbId()!=null) {
            if(this.getElmtDbId() !=null) {
                result = this.getElmtDbId().compareTo(o.getElmtDbId());
            } else {
                result = 0;
            }
        }
        return result;
    }


    public Integer getDbId() {
        return dbId;
    }


    public void setDbId(Integer dbId) {
        this.dbId = dbId;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((elmtDbId == null) ? 0 : elmtDbId.hashCode());
        result = prime * result + ((hintId == null) ? 0 : hintId.hashCode());
        result = prime * result + ((propertyId == null) ? 0 : propertyId.hashCode());
        return result;
    }





    /* (non-Javadoc)
     * @see sernet.hui.common.connect.ITypedElement#getTypeId()
     */
    @Override
    public String getTypeId() {
        return TYPE_ID;
    }   
    
    protected HUITypeFactory getTypeFactory() {
        return (HUITypeFactory) VeriniceContext.get(VeriniceContext.HUI_TYPE_FACTORY);
    }



    public String getElmtTitle() {
        return elmtTitle;
    }



    public void setElmtTitle(String elmtTitle) {
        this.elmtTitle = elmtTitle;
    }



    public Integer getScopeId() {
        return scopeId;
    }



    public void setScopeId(Integer scopeId) {
        this.scopeId = scopeId;
    }



    public String getElementType() {
        return elementType;
    }



    public void setElementType(String elementType) {
        this.elementType = elementType;
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
        CnAValidation other = (CnAValidation) obj;
        if (elmtDbId == null) {
            if (other.elmtDbId != null){
                return false;
            }
        } else if (!elmtDbId.equals(other.elmtDbId)){
            return false;
        }
        if (hintId == null) {
            if (other.hintId != null){
                return false;
            }
        } else if (!hintId.equals(other.hintId)){
            return false;
        }
        if (propertyId == null) {
            if (other.propertyId != null){
                return false;
            }
        } else if (!propertyId.equals(other.propertyId)){
            return false;
        }
        return true;
    }
    
}
