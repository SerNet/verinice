/*******************************************************************************
 * Copyright (c) 2009 Daniel Murygin <dm[at]sernet[dot]de>.
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
 *     Daniel <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.web;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

import sernet.verinice.model.bsi.MassnahmenUmsetzung;

public class UmsetzungConverter implements Converter {

	public static final String ENTBERHRLICH = "Entbehrlich";
	
	public static final String JA = "Ja";
	
	public static final String NEIN = "Nein";
	
	public static final String TEILWEISE = "Teilweise";
	
	public static final String UNBEARBEITET = "Unbearbeitet";

	private static final String ESTABLISHED = "Level 3: Established";

	private static final String MANAGED = "Level 2: Managed";

	private static final String OPTIMIZING = "Level 5: Optimizing";

	private static final String PERFORMED = "Level 1: Performed";

	private static final String PREDICTABLE = "Level 4: Predictable";
	
	
	public Object getAsObject(FacesContext arg0, UIComponent arg1, String text) {
		Object value = null;
		if(ENTBERHRLICH.equals(text)) {
			value=MassnahmenUmsetzung.P_UMSETZUNG_ENTBEHRLICH;
		} else if(JA.equals(text)) {
			value=MassnahmenUmsetzung.P_UMSETZUNG_JA;
		} else if(NEIN.equals(text)) {
			value=MassnahmenUmsetzung.P_UMSETZUNG_NEIN;
		} else if(TEILWEISE.equals(text)) {
			value=MassnahmenUmsetzung.P_UMSETZUNG_TEILWEISE;
		} else if(UNBEARBEITET.equals(text)) {
			value=MassnahmenUmsetzung.P_UMSETZUNG_UNBEARBEITET;
		} else if(ESTABLISHED.equals(text)) {
			value=MassnahmenUmsetzung.P_UMSETZUNG_ESTABLISHED;
		} else if(MANAGED.equals(text)) {
			value=MassnahmenUmsetzung.P_UMSETZUNG_MANAGED;
		} else if(OPTIMIZING.equals(text)) {
			value=MassnahmenUmsetzung.P_UMSETZUNG_OPTIMIZING;
		} else if(PERFORMED.equals(text)) {
			value=MassnahmenUmsetzung.P_UMSETZUNG_PERFORMED;
		} else if(PREDICTABLE.equals(text)) {
			value=MassnahmenUmsetzung.P_UMSETZUNG_PREDICTABLE;
		}
		return value;
	}

	public String getAsString(FacesContext arg0, UIComponent arg1, Object value) {
		String text = null;
		if(MassnahmenUmsetzung.P_UMSETZUNG_ENTBEHRLICH.equals(value)) {
			text=ENTBERHRLICH;
		} else if(MassnahmenUmsetzung.P_UMSETZUNG_JA.equals(value)) {
			text=JA;
		} else if(MassnahmenUmsetzung.P_UMSETZUNG_NEIN.equals(value)) {
			text=NEIN;
		} else if(MassnahmenUmsetzung.P_UMSETZUNG_TEILWEISE.equals(value)) {
			text=TEILWEISE;
		} else if(MassnahmenUmsetzung.P_UMSETZUNG_UNBEARBEITET.equals(value)) {
			text=UNBEARBEITET;
		} else if(MassnahmenUmsetzung.P_UMSETZUNG_ESTABLISHED.equals(value)) {
			text=ESTABLISHED;
		} else if(MassnahmenUmsetzung.P_UMSETZUNG_MANAGED.equals(value)) {
			text=MANAGED;
		} else if(MassnahmenUmsetzung.P_UMSETZUNG_OPTIMIZING.equals(value)) {
			text=OPTIMIZING;
		} else if(MassnahmenUmsetzung.P_UMSETZUNG_PERFORMED.equals(value)) {
			text=PERFORMED;
		} else if(MassnahmenUmsetzung.P_UMSETZUNG_PREDICTABLE.equals(value)) {
			text=PREDICTABLE;
		}
		return text;
	}

}
