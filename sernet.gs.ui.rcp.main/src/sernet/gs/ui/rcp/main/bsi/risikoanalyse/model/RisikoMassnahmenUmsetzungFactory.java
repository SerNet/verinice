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
package sernet.gs.ui.rcp.main.bsi.risikoanalyse.model;

import sernet.gs.ui.rcp.main.bsi.model.MassnahmenUmsetzung;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;

/**
 * Create new RirikomassnahmenUmsetzung from various sources, copying all relevant values to the new object.
 * 
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public class RisikoMassnahmenUmsetzungFactory {

	public static RisikoMassnahmenUmsetzung buildFromRisikomassnahme(RisikoMassnahme draftMn,
			CnATreeElement superParent, 
			GefaehrdungsUmsetzung myParent) {
		
		RisikoMassnahmenUmsetzung umsetzung = new RisikoMassnahmenUmsetzung(superParent, myParent, draftMn);

		umsetzung.setNumber(draftMn.getNumber());
		umsetzung.setName(draftMn.getName());
		umsetzung.setUrl(draftMn.getUrl());
		umsetzung.setStand(draftMn.getStand());
		umsetzung.setStufe(draftMn.getSiegelstufe());
		umsetzung.setLebenszyklus(draftMn.getLZAsString());
		
		return umsetzung;
	}
	
	public static RisikoMassnahmenUmsetzung buildFromRisikomassnahmenUmsetzung(RisikoMassnahmenUmsetzung draftMnUms,
			CnATreeElement superParent, 
			GefaehrdungsUmsetzung myParent) {
		
		RisikoMassnahme massnahme = draftMnUms.getRisikoMassnahme();
		RisikoMassnahmenUmsetzung massnahmenUmsetzung = new RisikoMassnahmenUmsetzung(superParent, myParent, massnahme);

		
		massnahmenUmsetzung.setName(draftMnUms.getName());
		massnahmenUmsetzung.setNumber(massnahme.getNumber());
		massnahmenUmsetzung.setUrl(massnahme.getUrl());
		massnahmenUmsetzung.setStand(massnahme.getStand());
		massnahmenUmsetzung.setStufe(massnahme.getSiegelstufe());
		massnahmenUmsetzung.setLebenszyklus(massnahme.getLZAsString());
		
		return massnahmenUmsetzung;
	}
	
	public static RisikoMassnahmenUmsetzung buildFromMassnahmenUmsetzung(MassnahmenUmsetzung draftMnUms,
			CnATreeElement superParent,
			GefaehrdungsUmsetzung myParent) {
		
		RisikoMassnahmenUmsetzung massnahmenUmsetzung = new RisikoMassnahmenUmsetzung(superParent, myParent);
		
		massnahmenUmsetzung.setLebenszyklus(draftMnUms.getLebenszyklus());
		massnahmenUmsetzung.setName(draftMnUms.getName());
		massnahmenUmsetzung.setNumber(draftMnUms.getKapitel());
		massnahmenUmsetzung.setUrl(draftMnUms.getUrl());
		massnahmenUmsetzung.setStand(draftMnUms.getStand());
		massnahmenUmsetzung.setStufe(draftMnUms.getStufe());
		
		return massnahmenUmsetzung;
	}

}
