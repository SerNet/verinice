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
package sernet.gs.ui.rcp.main.bsi.risikoanalyse.model;

import sernet.gs.model.Gefaehrdung;
import sernet.verinice.model.bsi.risikoanalyse.GefaehrdungsUmsetzung;
import sernet.verinice.model.bsi.risikoanalyse.OwnGefaehrdung;
import sernet.verinice.model.common.CnATreeElement;

public class GefaehrdungsUmsetzungFactory {

	public static GefaehrdungsUmsetzung build(
			CnATreeElement parent, Gefaehrdung source) {
		GefaehrdungsUmsetzung gefaehrdungsUmsetzung = new GefaehrdungsUmsetzung(parent);
		
		gefaehrdungsUmsetzung.setId(source.getId());
		gefaehrdungsUmsetzung.setTitel(source.getTitel());
		gefaehrdungsUmsetzung.setAlternative(GefaehrdungsUmsetzung.GEFAEHRDUNG_ALTERNATIVE_C);
		gefaehrdungsUmsetzung.setOkay(true);
		gefaehrdungsUmsetzung.setUrl(source.getUrl());

		
		gefaehrdungsUmsetzung.setKategorie(source.getKategorieAsString());
		gefaehrdungsUmsetzung.setStand(source.getStand());

		if (source instanceof OwnGefaehrdung) {
			OwnGefaehrdung gefaehrdungSource = (OwnGefaehrdung) source;
			gefaehrdungsUmsetzung.setDescription(gefaehrdungSource.getBeschreibung());
		}
		return gefaehrdungsUmsetzung;
	}

}
