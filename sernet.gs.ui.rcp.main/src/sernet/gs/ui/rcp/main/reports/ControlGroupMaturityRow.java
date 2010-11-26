/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak[at]sernet[dot]de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *     You should have received a copy of the GNU Lesser General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Alexander Koderman <ak[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.reports;

import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hibernate.LazyInitializationException;

import sernet.gs.ui.rcp.office.IOOTableRow;
import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.Property;
import sernet.hui.common.multiselectionlist.IMLPropertyOption;
import sernet.verinice.iso27k.service.ControlMaturityService;
import sernet.verinice.iso27k.service.Retriever;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Control;
import sernet.verinice.model.iso27k.ControlGroup;

/**
 * Returns the given properties as columns for OpenOffice export.
 * Adds three calculated columns for maturity level, weight and average by control group.
 * 
 * @author koderman[at]sernet[dot]de
 *
 */
public class ControlGroupMaturityRow implements IOOTableRow, ICnaItemRow {

	private ControlGroup control;
	private List<String> properties;
	
	private Pattern numbersOnly = Pattern.compile("^\\d+[\\.,]*\\d*$");

	protected List<String> getProperties() {
		return properties;
	}

	protected void setProperties(List<String> properties) {
		this.properties = properties;
	}

	private String style;
    private int maxColumns;
	

	public ControlGroupMaturityRow(ControlGroup item, List<String> properties, String style, int maxColumns) {
		this.control = item;
		this.properties = properties;
		this.style = style;
		this.maxColumns = properties.size() > maxColumns ? properties.size() : maxColumns;
	}

	public double getCellAsDouble(int column) {
	    double double1 = 0;
	    try {
            double1 = Double.parseDouble(getCellAsString(column));
        } catch (NumberFormatException e) {
            return 0;
        }
		return double1;
	}
	
	public String getCellAsString(int column) {
	    ControlMaturityService maturityService = new ControlMaturityService();
	    // we start at index 0, so this is one column after the last property:
	    if (column == maxColumns) {
	        return Integer.toString(maturityService.getWeightedMaturity(control));
	    }
	    // two columns after the last property:
	    if (column == maxColumns+1) {
            return Integer.toString(maturityService.getWeights(control));
        }
	    // three columns after the last property:
	    if (column == maxColumns+2) {
	        return Double.toString(maturityService.getMaturityByWeight(control));
	    }
	    if (column > properties.size()-1)
	        return "";
	    else
	        return control.getEntity().getSimpleValue(properties.get(column));
	}

	

    public int getCellType(int column) {
	    Matcher match = numbersOnly.matcher(getCellAsString(column));
	    if (match.matches())
	        return IOOTableRow.CELL_TYPE_DOUBLE;
	    else
	        return IOOTableRow.CELL_TYPE_STRING;
	}

	public int getNumColumns() {
	    return maxColumns+3;
	}
	
	public String getRowStyle() {
		return style;
	}

	public CnATreeElement getItem() {
		return control;
	}
	
}
