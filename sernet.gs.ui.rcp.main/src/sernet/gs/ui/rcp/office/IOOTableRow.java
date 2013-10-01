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
package sernet.gs.ui.rcp.office;

import java.io.Serializable;

public interface IOOTableRow extends Serializable {
	int CELL_TYPE_STRING = 0;
	int CELL_TYPE_DOUBLE = 1;
	
	// row styles, names must match style definition in OpenOffice template:
	String ROW_STYLE_ELEMENT = "Element";
	String ROW_STYLE_SUBHEADER = "ElementHeader";
	String ROW_STYLE_HEADER = "Kategorie";
	
	String getCellAsString(int column);
	double getCellAsDouble(int column);
	int getCellType(int column);
	int getNumColumns();
	String getRowStyle();
}
