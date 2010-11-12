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
 *     Robert Schuster <r.schuster@tarent.de> - rewritten to work on set of classes
 ******************************************************************************/
package sernet.gs.ui.rcp.main.bsi.filter;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;


public class BSIModelElementFilter extends ViewerFilter {

	private StructuredViewer viewer;
	private Set<Class<?>> filteredClasses;

	public BSIModelElementFilter(StructuredViewer viewer) {
		this.viewer = viewer;
	}

	public Set<Class<?>> getFilteredClasses() {
		if (filteredClasses == null)
			return new HashSet<Class<?>>();
		else
			return new HashSet<Class<?>>(filteredClasses);
	}

	public void setFilteredClasses(Set<Class<?>> newFilteredClasses) {
		boolean active = filteredClasses != null;
		if (newFilteredClasses != null && !newFilteredClasses.isEmpty()) {
			filteredClasses = newFilteredClasses;
			
			if (active)
				viewer.refresh();
			else {
				viewer.addFilter(this);
				active = true;
			}
		}
		else
		{
			filteredClasses = null;
			if (active)
				viewer.removeFilter(this);
		}
	}
	
	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		return !filteredClasses.contains(element.getClass());
	}
	
	/**
	 * Returns whether no classes are filtered.
	 * 
	 * @return
	 */
	public boolean isEmpty()
	{
		return filteredClasses == null || filteredClasses.isEmpty();
	}
}
