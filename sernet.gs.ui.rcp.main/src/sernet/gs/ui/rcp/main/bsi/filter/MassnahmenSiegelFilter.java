/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak@sernet.de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
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

import sernet.gs.model.Massnahme;
import sernet.gs.ui.rcp.main.bsi.model.IMassnahmeUmsetzung;


public class MassnahmenSiegelFilter extends ViewerFilter {

	private StructuredViewer viewer;
	private Collection<String> siegelPattern;

	public MassnahmenSiegelFilter(StructuredViewer viewer) {
		this.viewer = viewer;
	}

	public String[] getPattern() {
		return siegelPattern != null ? 
				(String[]) siegelPattern.toArray(new String[siegelPattern.size()])
				: new String[] {};
	}

	public void setPattern(String[] newPattern) {
		boolean active = siegelPattern != null;
		if (newPattern != null && newPattern.length > 0) {
			siegelPattern = new HashSet<String>();
			for (String type : newPattern) 
				siegelPattern.add(type);
			if (active)
				viewer.refresh();
			else {
				viewer.addFilter(this);
				active = true;
			}
			return;
		}
		
		// else deactivate:
		siegelPattern = null;
		if (active)
			viewer.removeFilter(this);
	}
	
	
	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (!(element instanceof IMassnahmeUmsetzung
				|| element instanceof Massnahme))
			return true;
		
		if (element instanceof IMassnahmeUmsetzung) {
			IMassnahmeUmsetzung mn = (IMassnahmeUmsetzung) element;
			return siegelPattern.contains(Character.toString(mn.getStufe()));
		}
		
		Massnahme mn = (Massnahme) element;
		return siegelPattern.contains(Character.toString(mn.getSiegelstufe()));
	}
	
//	public boolean isFilterProperty(Object element, String property) {
//		return true;
//	}
}
