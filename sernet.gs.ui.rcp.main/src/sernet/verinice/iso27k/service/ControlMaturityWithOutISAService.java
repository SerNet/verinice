/*******************************************************************************
 * Copyright (c) 2011 Sebastian Hagedorn <sh@sernet.de>.
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
package sernet.verinice.iso27k.service;

import sernet.hui.common.connect.PropertyList;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Control;
import sernet.verinice.model.iso27k.ControlGroup;
import sernet.verinice.model.iso27k.IControl;

/**
 * Class provides methods for getting properties of a control, that are numericProperties in SamtTopics,
 * which doesnt work for Controls.
 *
 */

public class ControlMaturityWithOutISAService extends ControlMaturityService {

	
    /**
     * Return sum of all weights of all controls contained in this group and subgroups.
     * @return combined weight
     */
    @Override
	public Integer getWeights(ControlGroup cg) {
        int weight = 0;
        for (CnATreeElement child : cg.getChildren()) {
            if (child instanceof IControl) {
            	weight += Integer.parseInt(getProperty(child, Control.PROP_WEIGHT2));
            }
            if (child instanceof ControlGroup) {
                ControlGroup control = (ControlGroup) child;
                weight += getWeights(control);
            }
        }
        return weight;
    }
	
    
    /**
     * Calculate accumulated maturity times weight of each control contained in this group.
     * @return the calculated maturity times the weights for each control
     */
    @Override
    public Integer getWeightedMaturity(ControlGroup cg) {
        int maturity = 0;
        for (CnATreeElement child : cg.getChildren()) {
            if (child instanceof IControl) {
                maturity += Integer.parseInt(getProperty(child, Control.PROP_MATURITY));
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
    @Override
    public Integer getMaturity(ControlGroup cg) {
        int maturity = 0;
        for (CnATreeElement child : cg.getChildren()) {
            if (child instanceof IControl) {
                maturity += Integer.parseInt(getProperty(child, Control.PROP_MATURITY));
            }
            if (child instanceof ControlGroup) {
                maturity += getMaturity((ControlGroup) child);
            }
        }
        return maturity;
    }
    
    
    @Override
    public Integer getWeightedMaturity(IControl contr) {
    	if(contr instanceof Control){
    		Control c = (Control)contr;
    		return Integer.parseInt(getProperty(c, Control.PROP_MATURITY)) + Integer.parseInt(getProperty(c, Control.PROP_WEIGHT2));
    	}
    	return null;
    }
    
    @Override
    public Double getMaturityByWeight(IControl contr) {
    	if(contr instanceof Control){
    		Control c = (Control) contr;
    		return ((double)getWeightedMaturity(contr)) / ((double)Integer.parseInt(getProperty(c, Control.PROP_WEIGHT2)));
    	}
    	return null;
    }
    
    /**
     * gets property of a {@link CnATreeElement}, here especially  for Control, without using the getInt()-method
     * of {@link Entity} which uses the isNumericOption()-property that only works for instances of {@link SamtTopic}
     * @param element
     * @param propertyType
     * @return the propertyValue as a string, "0" if property equals null or ""
     */
    public String getProperty(CnATreeElement element, String propertyType){
    	PropertyList list = element.getEntity().getProperties(propertyType);
    	if(list.getProperties().size() == 1){
    		String retVal = list.getProperty(0).getPropertyValue(); 
    		if(retVal == null || retVal.equals("")){
    			retVal = "0";
    		}
    		return retVal;
    	}
    	return "0";
    }
}
