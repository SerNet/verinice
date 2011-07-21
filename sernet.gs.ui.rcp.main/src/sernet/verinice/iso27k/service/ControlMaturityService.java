/*******************************************************************************
 * Copyright (c) 2010 Alexander Koderman <ak@sernet.de>.
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
 *     Alexander Koderman <ak@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.iso27k.service;

import org.apache.log4j.Logger;

import sernet.hui.common.VeriniceContext;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.hui.common.connect.Property;
import sernet.hui.common.connect.PropertyType;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.ControlGroup;
import sernet.verinice.model.iso27k.IControl;

/**
 * 
 * Calculate maturity values and weights for controls and control groups.
 * 
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public class ControlMaturityService {
    private static final Logger LOG = Logger.getLogger(ControlMaturityService.class);
    
    /**
     * Calculate accumulated maturity times weight of each control contained in this group.
     * @return the calculated maturity times the weights for each control
     */
    public Integer getWeightedMaturity(ControlGroup cg) {
        int maturity = 0;
        for (CnATreeElement child : cg.getChildren()) {
            if (child instanceof IControl) {
                IControl control = (IControl) child;
                maturity += getWeightedMaturity(control);
            }
            if (child instanceof ControlGroup) {
                ControlGroup control = (ControlGroup) child;
                maturity += getWeightedMaturity(control);
            }
        }
        return maturity;
    }
    
    /**
     * Calculate accumulated maturity of each control contained in this group.
     * @return the calculated maturity for each control
     */
    public Integer getMaturity(ControlGroup cg) {
        int maturity = 0;
        for (CnATreeElement child : cg.getChildren()) {
            if (child instanceof IControl) {
                maturity += ((IControl)child).getMaturity();
            }
            if (child instanceof ControlGroup) {
                maturity += getMaturity((ControlGroup) child);
            }
        }
        return maturity;
    }
    
    public Double getMaturityByWeight(ControlGroup cg) {
        double result =0;
        if (getWeights(cg) != 0)
            result = ((double)getWeightedMaturity(cg)) / ((double)getWeights(cg));
        return result;
    }

    /**
     * Return sum of all weights of all controls contained in this group and subgroups.
     * @return combined weight
     */
    public Integer getWeights(ControlGroup cg) {
        int weight = 0;
        for (CnATreeElement child : cg.getChildren()) {
            if (child instanceof IControl) {
                IControl control = (IControl) child;
                weight += control.getWeight2();
            }
            if (child instanceof ControlGroup) {
                ControlGroup control = (ControlGroup) child;
                weight += getWeights(control);
            }
        }
        return weight;
    }
    
    /**
     * @return
     */
    public Integer getWeightedMaturity(IControl contr) {
        int value = contr.getMaturity() * contr.getWeight2();
        return value;
    }
    
    public Double getMaturityByWeight(IControl contr) {
        double result = ((double)getWeightedMaturity(contr)) / ((double)contr.getWeight2());
        return result;
    }

    /**
     * @param group
     * @return
     */
    public Double getMaxMaturityValue(ControlGroup group) {
        Double result = Double.valueOf(0);
        for (CnATreeElement child : group.getChildren()) {
            if (child instanceof IControl) {
                Double maturity = getMaxMaturityValue((IControl) child);
                if(maturity > result) {
                    result = maturity;
                }
            }
            if (child instanceof ControlGroup) {
                Double maturity = getMaxMaturityValue((ControlGroup) child);
                if(maturity > result) {
                    result = maturity;
                }
            }
        }
        return result;
    }
    
    private Double getMaxMaturityValue(IControl control) {
        HUITypeFactory hui = (HUITypeFactory) VeriniceContext.get(VeriniceContext.HUI_TYPE_FACTORY);
        PropertyType propertyType = hui.getPropertyType(control.getTypeId(), control.getMaturityPropertyId());
        return Double.valueOf(propertyType.getMaxValue());
    }
    
    public int getThreshold1(ControlGroup group) {
        int result = 0;
        for (CnATreeElement child : group.getChildren()) {
            if (child instanceof IControl) {
                result+= ((IControl)child).getThreshold1();
            }
            if (child instanceof ControlGroup) {
            	result+= getThreshold1((ControlGroup)child);
            }
        }
        return result;
    }
    
    public int getThreshold2(ControlGroup group) {
        int result = 0;
        for (CnATreeElement child : group.getChildren()) {
            if (child instanceof IControl) {
                result+= ((IControl)child).getThreshold2();
            }
            if (child instanceof ControlGroup) {
            	result+= getThreshold2((ControlGroup)child);
            }
        }
        return result;
    }
    
    /**
     * Returns the implementaiton state based on the maturity level of the <code>IControl.</code>
     * 
     * @param control
     * @return the implementation state as definied in the <code>IControl.IMPLEMENTED</code> constants.
     */
    public String getImplementationState(ControlGroup group) {
    	String state = IControl.IMPLEMENTED_NO;
    	int threshold1 = getThreshold1(group);
    	int maturity = getMaturity(group);
    	if (maturity >= threshold1) {
    		state = IControl.IMPLEMENTED_PARTLY;
        	int threshold2 = getThreshold2(group); 		
    		if (maturity >= threshold2) {
        		state = IControl.IMPLEMENTED_YES;
        	}
    	} 	
    	return state;
    }
    
    /**
     * Returns the implementaiton state based on the maturity level of the <code>IControl.</code>
     * 
     * @param control
     * @return the implementation state as definied in the <code>IControl.IMPLEMENTED</code> constants.
     */
    public String getImplementationState(IControl control) {
    	String state = IControl.IMPLEMENTED_NO;
    	if (control.getMaturity() >= control.getThreshold1()) {
    		state = IControl.IMPLEMENTED_PARTLY;
    	}
    	if (control.getMaturity() >= control.getThreshold2()) {
    		state = IControl.IMPLEMENTED_YES;
    	}
    	return state;
    }
    
    /**
     * Returns the isa implementaiton state based on the maturity level of the <code>IControl.</code>
     * Changed to represent just the states "control has been edited or not".
     * 
     * @param control
     * @return the implementation state as definied in the <code>IControl.IMPLEMENTED</code> constants.
     */
    public String getIsaState(IControl control) {
        
        if (control.getMaturity() == IControl.IMPLEMENTED_NOTEDITED_NUMERIC) {
            return IControl.IMPLEMENTED_NOTEDITED;
        }
        return IControl.IMPLEMENTED_YES;
        
//        
//        if (control.getMaturity() >= control.getThreshold2()) {
//            return IControl.IMPLEMENTED_YES;
//        }
//        if (control.getMaturity() >= control.getThreshold1()) {
//            return IControl.IMPLEMENTED_PARTLY;
//        }
//        return IControl.IMPLEMENTED_PARTLY;
    }
    
      
}


