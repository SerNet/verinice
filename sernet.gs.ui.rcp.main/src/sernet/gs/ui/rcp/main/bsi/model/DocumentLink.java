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
package sernet.gs.ui.rcp.main.bsi.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class DocumentLink implements Serializable {

	public static final Object NO_LINK_MESSAGE = ""; //$NON-NLS-1$


	public DocumentLink(String name, String href) {
		super();
		this.name = name;
		this.href = href;
	}

	private Set<DocumentReference> children = new HashSet<DocumentReference>();
	private String name;
	private String href;
	

	public String getName() {
		return this.name;
	}

	public String getHref() {
		return this.href;
	}
	
	public void addChild(DocumentReference child) {
		if (children.add(child)){
			child.setParent(this);
		}
	}
	
	public Set<DocumentReference> getChildren() {
		return children;
	}

	public String toString()
	{
		return "name=" + name + ";href=" + href;
	}

}
