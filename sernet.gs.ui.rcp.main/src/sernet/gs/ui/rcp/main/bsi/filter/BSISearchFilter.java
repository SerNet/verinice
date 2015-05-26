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

import java.util.List;
import java.util.regex.Matcher;

import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;

import sernet.gs.model.Baustein;
import sernet.gs.model.Gefaehrdung;
import sernet.gs.model.Massnahme;

public class BSISearchFilter extends TextFilter {

	
	public BSISearchFilter(StructuredViewer viewer) {
		super(viewer);
	}

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (getRegex()==null){
			return true;
		}
		if (element instanceof Baustein) {
			Baustein bs = (Baustein) element;
			Matcher matcher = getRegex().matcher(bs.getTitel());
			if (matcher.find()){
				return true;
			}
			// show baustein if one child matches:
			return checkMassnahmen(bs) || checkGefaehrdungen(bs);
		}
		
		if (element instanceof Massnahme) {
			Massnahme mn = (Massnahme) element;
			Matcher matcher = getRegex().matcher(mn.getTitel());
			return (matcher.find());
		}
		
		if (element instanceof Gefaehrdung) {
			Gefaehrdung gef = (Gefaehrdung) element;
			Matcher matcher = getRegex().matcher(gef.getTitel());
			return (matcher.find());
		}
		
		return false;
	}
	
	private boolean checkMassnahmen(Baustein bs) {
		List<Massnahme> massnahmen = bs.getMassnahmen();
		for (Massnahme mn : massnahmen) {
			Matcher matcher = getRegex().matcher(mn.getTitel());
			if (matcher.find()){
				return true;
			}
		}
		return false;
	}

	private boolean checkGefaehrdungen(Baustein bs) {
		List<Gefaehrdung> gefaehrdungen= bs.getGefaehrdungen();
		for (Gefaehrdung gefaehrdung : gefaehrdungen) {
			Matcher m = getRegex().matcher(gefaehrdung.getTitel());
			if (m.find()){
				return true;
			}
		}
		return false;
	}
	

}
