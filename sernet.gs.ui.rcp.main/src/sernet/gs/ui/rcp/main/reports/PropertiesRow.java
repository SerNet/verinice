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
import sernet.verinice.iso27k.service.Retriever;
import sernet.verinice.model.common.CnATreeElement;

/**
 * Returns the given properties as columns for OpenOffice export.
 * 
 * @author koderman[at]sernet[dot]de
 *
 */
public class PropertiesRow implements IOOTableRow, ICnaItemRow {

	private CnATreeElement item;
	private List<String> properties;
	
	private Pattern numbersOnly = Pattern.compile("\\d+");

	protected List<String> getProperties() {
		return properties;
	}

	protected void setProperties(List<String> properties) {
		this.properties = properties;
	}

	private String style;
	

	public PropertiesRow(CnATreeElement item, List<String> properties, String style) {
		this.item = item;
		this.properties = properties;
		this.style = style;
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
		return item.getEntity().getSimpleValue(properties.get(column));
	}

	public int getCellType(int column) {
	    Matcher match = numbersOnly.matcher(getCellAsString(column));
	    if (match.matches())
	        return IOOTableRow.CELL_TYPE_DOUBLE;
	    else
	        return IOOTableRow.CELL_TYPE_STRING;
	}

	public int getNumColumns() {
		if (properties == null)
			return 0;
		return properties.size();
	}
	
	public String getRowStyle() {
		return style;
	}

	public CnATreeElement getItem() {
		return item;
	}
	
}
