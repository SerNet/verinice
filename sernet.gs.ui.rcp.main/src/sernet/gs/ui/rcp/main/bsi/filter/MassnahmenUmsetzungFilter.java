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

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import sernet.verinice.model.bsi.IMassnahmeUmsetzung;


public class MassnahmenUmsetzungFilter extends ViewerFilter {
	
	/**
	 * Denotes the property which triggers a filtering when an element
	 * is updated.
	 * 
	 * <p>Use this string when calling {@link StructuredViewer#update(Object, String[])}.</p>
	 */
	public static final String UMSETZUNG_PROPERTY = "umsetzung";

	private StructuredViewer viewer;
	private Set<String> umsetzungPattern;

	public MassnahmenUmsetzungFilter(StructuredViewer viewer) {
		this.viewer = viewer;
	}
	
	
	public String[] getUmsetzungPattern() {
        return umsetzungPattern != null ? 
                (String[]) umsetzungPattern.toArray(new String[umsetzungPattern.size()])
                : new String[] {};
    }

	public Set<String> getUmsetzungPatternSet() {
		return umsetzungPattern;
	}

	public void setUmsetzungPattern(String[] newPattern) {
		boolean active = umsetzungPattern != null;
		if (newPattern != null && newPattern.length > 0) {
			umsetzungPattern = new HashSet<String>();
			for (String type : newPattern){
				umsetzungPattern.add(type);
			}
			if (active){
				viewer.refresh();
			} else {
				viewer.addFilter(this);
				active = true;
			}
			return;
		}
		
		// else deactivate:
		umsetzungPattern = null;
		if (active){
			viewer.removeFilter(this);
		}
	}
	
	
	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (!(element instanceof IMassnahmeUmsetzung)){
			return true;
		}
		IMassnahmeUmsetzung mn = (IMassnahmeUmsetzung) element;
		return umsetzungPattern.contains(mn.getUmsetzung());
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ViewerFilter#isFilterProperty(java.lang.Object, java.lang.String)
	 */
	public boolean isFilterProperty(Object element, String property) {
		return UMSETZUNG_PROPERTY.equals(property);
	}
	
}
