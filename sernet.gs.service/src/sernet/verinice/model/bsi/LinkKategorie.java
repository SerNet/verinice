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
package sernet.verinice.model.bsi;

import java.io.Serializable;
import java.util.Set;

import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;


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
