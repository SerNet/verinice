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
package sernet.gs.ui.rcp.main.bsi.model;

import java.io.Serializable;
import java.util.List;

public class BausteinVorschlag implements Serializable {

	private Integer dbId;
	
	private String bausteine;
	private String name;
	
	protected BausteinVorschlag() {
		// hibernate constructor
	}

	public BausteinVorschlag(String name, String list) {
		this.name = name;
		this.bausteine = list;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getDbId() {
		return dbId;
	}

	public void setDbId(Integer dbId) {
		this.dbId = dbId;
	}

	public String getBausteine() {
		return bausteine;
	}
	
	public String[] getSplitBausteine() {
		return bausteine.split(",\\s*");
	}

	public void setBausteine(String bausteine) {
		this.bausteine = bausteine;
	}



}
