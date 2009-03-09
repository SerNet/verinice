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
package sernet.gs.ui.rcp.main.bsi.filter;

import java.util.Collection;
import java.util.HashSet;

import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import sernet.gs.ui.rcp.main.bsi.model.IMassnahmeUmsetzung;


public class MassnahmenUmsetzungFilter extends ViewerFilter {

	private StructuredViewer viewer;
	private Collection<String> umsetzungPattern;

	public MassnahmenUmsetzungFilter(StructuredViewer viewer) {
		this.viewer = viewer;
	}

	public String[] getUmsetzungPattern() {
		return umsetzungPattern != null ? 
				(String[]) umsetzungPattern.toArray(new String[umsetzungPattern.size()])
				: new String[] {};
	}

	public void setUmsetzungPattern(String[] newPattern) {
		boolean active = umsetzungPattern != null;
		if (newPattern != null && newPattern.length > 0) {
			umsetzungPattern = new HashSet<String>();
			for (String type : newPattern) 
				umsetzungPattern.add(type);
			if (active)
				viewer.refresh();
			else {
				viewer.addFilter(this);
				active = true;
			}
			return;
		}
		
		// else deactivate:
		umsetzungPattern = null;
		if (active)
			viewer.removeFilter(this);
	}
	
	
	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (!(element instanceof IMassnahmeUmsetzung))
			return true;
		
		IMassnahmeUmsetzung mn = (IMassnahmeUmsetzung) element;
		return umsetzungPattern.contains(mn.getUmsetzung());
	}
	
//	public boolean isFilterProperty(Object element, String property) {
//		return true;
//	}
}
