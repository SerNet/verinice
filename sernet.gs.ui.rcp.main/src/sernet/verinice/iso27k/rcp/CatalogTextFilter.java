/*******************************************************************************
 * Copyright (c) 2009 Daniel Murygin <dm[at]sernet[dot]de>.
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
 *     Daniel <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.iso27k.rcp;

import java.util.Collection;
import java.util.Iterator;
import java.util.regex.Matcher;

import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;

import sernet.gs.ui.rcp.main.bsi.filter.TextFilter;
import sernet.verinice.interfaces.iso27k.IItem;

/**
 * @author Daniel <dm[at]sernet[dot]de>
 *
 */
public class CatalogTextFilter extends TextFilter {

	
	public CatalogTextFilter(StructuredViewer viewer) {
		super(viewer);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ViewerFilter#select(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		boolean match = true;
		if (getRegex()!=null) {
			IItem item = (IItem) element;
			if(item.getName()!=null) {
				Matcher matcherName = getRegex().matcher(item.getName());
				match = matcherName.find();
			}
			if(item.getDescription()!=null) {
				Matcher matcherDesc = getRegex().matcher(item.getDescription());
				match = match || matcherDesc.find();
			}
			
			// if there is no match, check children
			Collection<IItem> children = item.getItems();
			if(!match && children!=null && children.size()>0) {
				for (Iterator<IItem> iterator = children.iterator(); iterator.hasNext();) {
					match = select(viewer, parentElement, iterator.next());
					if(match) {
						break;
					}
				}
				
			}
		} 
		return match;
	}
	
}
