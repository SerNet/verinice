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

import sernet.hui.common.connect.Entity;
import sernet.verinice.model.bsi.MassnahmenUmsetzung;
import sernet.verinice.model.common.CnATreeElement;

/**
 * 
 * @author koderman[at]sernet[dot]de
 *
 */
public class LebenszyklusPropertyFilter extends StringPropertyFilter {

	public LebenszyklusPropertyFilter(StructuredViewer viewer) {
		super(viewer, MassnahmenUmsetzung.P_LEBENSZYKLUS);
	}
	
	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (!(element instanceof MassnahmenUmsetzung)){
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
