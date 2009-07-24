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

import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import sernet.gs.ui.rcp.main.bsi.model.BausteinUmsetzung;
import sernet.gs.ui.rcp.main.bsi.model.LinkKategorie;
import sernet.gs.ui.rcp.main.bsi.model.MassnahmenUmsetzung;


public class BSIModelElementFilter extends ViewerFilter {

	private StructuredViewer viewer;
	private boolean[] pattern = null;

	public BSIModelElementFilter(StructuredViewer viewer) {
		this.viewer = viewer;
	}

	public boolean[] getPattern() {
		return pattern;
	}

	public void setPattern(boolean[] newPattern) {
		boolean active = pattern != null;
		if (newPattern != null && newPattern.length > 0) {
			pattern = newPattern;
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
		if (element instanceof BausteinUmsetzung
				&& pattern[0] /*filter bausteinzuordnungen*/) {
			return false;
		}
		
		if (element instanceof MassnahmenUmsetzung
				&& pattern[1] /*filter massnahmenumsetzungen*/) {
			return false;
		}
		
		if (element instanceof LinkKategorie
				&& pattern[2] /*filter links*/) {
			return false;
		}
		
		return true;
	}
	
//	public boolean isFilterProperty(Object element, String property) {
//		return true;
//	}
}
