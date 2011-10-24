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
package sernet.verinice.model.bsi;

import java.io.Serializable;

import sernet.hui.common.connect.ITypedElement;

public class BausteinVorschlag implements Serializable, ITypedElement {

	private Integer dbId;
	
	private String bausteine;
	private String name;

    public static final String TYPE_ID = "bst_vorschlag";
	
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

    /* (non-Javadoc)
     * @see sernet.hui.common.connect.ITypedElement#getTypeId()
     */
    public String getTypeId() {
        return TYPE_ID;
    }



}
