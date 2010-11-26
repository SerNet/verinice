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

import java.util.regex.Pattern;

import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

/**
 * Abstract superclass for view filters that filter elements using a string pattern.
 * 
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public abstract class TextFilter extends ViewerFilter {

	protected Pattern regex;
	private String suche;
	private StructuredViewer viewer;

	public TextFilter(StructuredViewer viewer) {
		this.viewer = viewer;
	}

	@Override
	public abstract boolean select(Viewer viewer, Object parentElement, Object element);
	
	
	public String getPattern() {
		return suche;
	}
	
	public void setPattern(String newPattern) {
		boolean active = suche != null;
		if (newPattern != null && newPattern.length() > 0) {
			suche = newPattern;
			regex = Pattern.compile(suche, Pattern.CASE_INSENSITIVE);
			if (active)
				viewer.refresh();
			else {
				viewer.addFilter(this);
				active = true;
			}
			return;
		}
		
		// else deactivate:
		suche = null;
		regex=null;
		if (active)
			viewer.removeFilter(this);
	}

}
