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

public class OwnGefaehrdung extends Gefaehrdung {
	
	private String beschreibung;
	private String ownkategorie;

	
	
	// TODO eigener Entity-Type für eigene Gefährundengen
//	private Entity entity;
	
	
	public OwnGefaehrdung() {
		setTitel("");
		setId("");
		this.beschreibung = "";
		this.ownkategorie = "";
	}
	
	
	public String getKategorieAsString() {
		return this.ownkategorie;
	}
	
	public void setBeschreibung(String newDescr) {
		this.beschreibung =  newDescr;
	}
	
	public String getBeschreibung() {
		return this.beschreibung;
	}
	
	
	/**
	 * @return the ownkategorie
	 */
	public String getOwnkategorie() {
		return ownkategorie;
	}

	/**
	 * @param ownkategorie the ownkategorie to set
	 */
	public void setOwnkategorie(String ownkategorie) {
		this.ownkategorie = ownkategorie;
	}

	
}
