/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak@sernet.de>.
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
package sernet.gs.ui.rcp.main.bsi.risikoanalyse.wizard;

import java.util.ArrayList;
import java.util.List;

import sernet.gs.ui.rcp.main.bsi.risikoanalyse.model.GefaehrdungsUmsetzung;

/**
 * Root element of a tree. Used as root element in TreeViewer. All childen are
 * of type GefaehrdungsUmsetzungen.
 * 
 * @author ahanekop@sernet.de
 */
public class GefaehrdungsBaumRoot implements IGefaehrdungsBaumElement {

	private List<IGefaehrdungsBaumElement> children =
			new ArrayList<IGefaehrdungsBaumElement>();
	
	/**
	 * Constructor.
	 * 
	 * @param arrListGefaehrdungsUmsetzungen
	 *            the list of GefaehrdungsUmsetzungen to add as children of the
	 *            root element
	 */
	public GefaehrdungsBaumRoot(
			List<GefaehrdungsUmsetzung> arrListGefaehrdungsUmsetzungen) {
		for (GefaehrdungsUmsetzung gefaehrdungsUmsetzung : arrListGefaehrdungsUmsetzungen) {
			children.add(gefaehrdungsUmsetzung);
		}
	}
	
	/**
	 * Returns the description of the root element. Since the root element is
	 * not a Gefaehrdung, it does not need a description and returns an empty
	 * string.
	 * Method must be implemnted due to  IGefaehrdungsBaumElement.
	 * 
	 * @return an empty string
	 */
	public String getDescription() {
		return "";
	}

	/**
	 * Returns the list of children of the root element in the tree.
	 * All children must be of the abstract type IGefaehrdungsBaumElement.
	 * 
	 * @return the list of children of the root element
	 */
	public List<IGefaehrdungsBaumElement> getGefaehrdungsBaumChildren() {
		return children;
	}

	/**
	 * GefaehrdungsBaumRoot is already the root element of the tree.
	 * Hence, no parent is to be returned.
	 * 
	 * @return null no parent to return
	 */
	public IGefaehrdungsBaumElement getGefaehrdungsBaumParent() {
		return null;
	}

	
	/**
	 * Returns the title of the root element.
	 * 
	 * @return the title of the root element
	 */
	public String getText() {
		return "root";
	}
}
