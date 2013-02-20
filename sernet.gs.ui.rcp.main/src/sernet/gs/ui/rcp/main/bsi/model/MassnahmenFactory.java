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
package sernet.gs.ui.rcp.main.bsi.model;

import sernet.gs.model.Massnahme;
import sernet.hui.common.connect.Entity;
import sernet.verinice.model.bsi.BausteinUmsetzung;
import sernet.verinice.model.bsi.MassnahmenUmsetzung;

public class MassnahmenFactory {

	/**
	 * Create MassnahmenUmsetzung (control instance) and add to given BausteinUmsetzung (module instance).
	 * @param bu
	 * @param mn
	 */
	public void createMassnahmenUmsetzung(BausteinUmsetzung bu, Massnahme mn) {
		MassnahmenUmsetzung mu = new MassnahmenUmsetzung(bu);
		
		copyValues(mn, mu);
		bu.addChild(mu);
	}

	/**
	 * Creyte MassnahmenUmsetzung (control instance) from given Massnahme (control).
	 * 
	 * @param mn
	 * @return
	 */
	public MassnahmenUmsetzung createMassnahmenUmsetzung(Massnahme mn) {
		MassnahmenUmsetzung mu = new MassnahmenUmsetzung();
		mu.setEntity(new Entity(MassnahmenUmsetzung.TYPE_ID));
		copyValues(mn, mu);
		return mu;
	}
	
	private void copyValues(Massnahme mn, MassnahmenUmsetzung mu) {
		mu.setKapitel(mn.getId());
		mu.setUrl(mn.getUrl());
		mu.setName(mn.getTitel());
		mu.setLebenszyklus(mn.getLZAsString());
		mu.setStufe(mn.getSiegelstufe());
		mu.setStand(mn.getStand());
		mu.setVerantwortlicheRollenInitiierung(mn.getVerantwortlichInitiierung());
		mu.setVerantwortlicheRollenUmsetzung(mn.getVerantwortlichUmsetzung());
	}
	
	
}
