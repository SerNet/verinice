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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import sernet.hui.common.connect.Entity;
import sernet.verinice.model.common.CnATreeElement;


/**
 * Reusable filter to select items based on property values.
 * 
 * @author koderman[at]sernet[dot]de
 *
 */
public class StringPropertyFilter extends ViewerFilter {

	private StructuredViewer viewer;
	private String pattern=null;
	protected String propertyType;
	protected Pattern regex;

	/**
	 * 
	 * @param viewer
	 * @param type
	 */
	public StringPropertyFilter(StructuredViewer viewer, String type) {
		this.viewer = viewer;
		this.propertyType = type;
	}

	public String getPattern() {
		return pattern;
	}
	
	public void setPattern(String newPattern) {
		boolean active = pattern != null;
		if (newPattern != null && newPattern.length() > 0) {
			pattern = newPattern;
			regex = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
			if (active){
				viewer.refresh();
			} else {
				viewer.addFilter(this);
				active = true;
			}
			return;
		}
		
		// else deactivate:
		pattern = null;
		regex=null;
		if (active){
			viewer.removeFilter(this);
		}
	}
	
	
	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (!(element instanceof CnATreeElement)){
			return true;
		}
		Entity entity = ((CnATreeElement)element).getEntity();
		String value = entity.getSimpleValue(this.propertyType);
		Matcher matcher = regex.matcher(value);
		if (matcher.find()){
			return true;
		}
		return false;
	}
}
