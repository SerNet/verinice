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
package sernet.gs.ui.rcp.main.bsi.filter;

import java.util.Collection;
import java.util.HashSet;

import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import sernet.gs.model.Baustein;

/**
 * 
 * Filters controls based on level.
 * 
 * @author koderman[at]sernet[dot]de
 *
 */
public class BSISchichtFilter extends ViewerFilter {

	private StructuredViewer viewer;
	private Collection<String> pattern;

	public BSISchichtFilter(StructuredViewer viewer) {
		this.viewer = viewer;
	}

	public String[] getPattern() {
		return pattern != null ? 
				(String[]) pattern.toArray(new String[pattern.size()])
				: new String[] {};
	}
	

	public void setPattern(String[] newPattern) {
		boolean active = pattern != null;
		if (newPattern != null && newPattern.length > 0) {
			pattern = new HashSet<String>();
			for (String type : newPattern) 
				pattern.add(type);
			if (active)
				viewer.refresh();
			else {
				viewer.addFilter(this);
				active = true;
			}
			return;
		}
		
		// else deactivate:
		pattern = null;
		if (active)
			viewer.removeFilter(this);
	}
	
	
	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (!(element instanceof Baustein))
			return true;

		Baustein bs = (Baustein) element;
		return pattern.contains(Integer.toString(bs.getSchicht()));
	}
	
//	public boolean isFilterProperty(Object element, String property) {
//		return true;
//	}
}
