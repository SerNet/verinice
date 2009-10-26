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
package sernet.gs.ui.rcp.main.bsi.model;

import java.io.Serializable;
import java.util.Set;

import sernet.gs.ui.rcp.main.common.model.CnALink;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;


public class LinkKategorie implements Serializable  {
	
	private CnATreeElement parent;

	public LinkKategorie(CnATreeElement parent) {
		this.parent = parent;
	}
	
	private LinkKategorie() {
		
	}
	
	public String getTitle() {
		return "Abhängigkeiten"; //$NON-NLS-1$
	}



	public Set<CnALink> getChildren() {
		return parent.getLinksDown();
	}
	
	



	public CnATreeElement getParent() {
		return parent;
	}

	public void setParent(CnATreeElement parent) {
		this.parent = parent;
	}

}
