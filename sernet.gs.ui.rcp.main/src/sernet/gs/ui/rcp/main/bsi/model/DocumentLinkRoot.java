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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import sernet.hui.common.connect.HUITypeFactory;
import sernet.hui.common.connect.PropertyType;

public class DocumentLinkRoot implements Serializable {

	private Set<DocumentLink> children = new HashSet<DocumentLink>();
	
	

	public void addChild(DocumentLink link) {
		this.children.add(link);
	}
	
	public DocumentLink[] getChildren() {
		return (DocumentLink[]) children.toArray(new DocumentLink[children.size()]);
	}

	public DocumentLink getDocumentLink(String name, String href) {
		for (DocumentLink link : children) {
			if (link.getName().equals(name)
					&& link.getHref().equals(href))
				return link;
		}
		return null;
	}
}
