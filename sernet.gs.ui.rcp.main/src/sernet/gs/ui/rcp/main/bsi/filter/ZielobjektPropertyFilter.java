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

import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;

import sernet.verinice.model.bsi.IMassnahmeUmsetzung;

/**
 * Filter to select items based on search string comparison with Zielobjekt.
 * 
 * @author koderman[at]sernet[dot]de
 *
 */
public class ZielobjektPropertyFilter extends StringPropertyFilter {

	public ZielobjektPropertyFilter(StructuredViewer viewer) {
		super(viewer, "");
	}
	
	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (!(element instanceof IMassnahmeUmsetzung)){
			return true;
		}
		
		
		String value = ((IMassnahmeUmsetzung)element).getParentTitle();
		Matcher matcher = super.regex.matcher(value);
		if (matcher.find()){
			return true;
		}
		return false;
	}

}
