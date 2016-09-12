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
package sernet.verinice.model.bsi.risikoanalyse;

import sernet.gs.model.Gefaehrdung;
import sernet.hui.common.connect.ITypedElement;

public class OwnGefaehrdung extends Gefaehrdung implements ITypedElement {
	
    private static final long serialVersionUID = 20160222105816L;
    private String beschreibung;
	private String ownkategorie;
    private String extId;
	
	public static final String TYPE_ID = "owngefaehrdung";

    public static final String NEW_CATEGORY_DE = "[Neue Kategorie]";
    public static final String NEW_CATEGORY_EN = "[New Category]";
	
	public OwnGefaehrdung() {
		super();
		this.beschreibung = "";
		this.ownkategorie = "";
        this.extId = "";
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

    /* (non-Javadoc)
     * @see sernet.hui.common.connect.ITypedElement#getTypeId()
     */
    public String getTypeId() {
        return TYPE_ID;
    }
    
    public String getExtId() {
        return extId;
    }

    public void setExtId(String extId) {
        this.extId = extId;
    }

    @Override
    public String getKategorieAsString(String language) {
        if(ownkategorie==null || ownkategorie.isEmpty() || ownkategorie.equalsIgnoreCase(NEW_CATEGORY_DE) || ownkategorie.equalsIgnoreCase(NEW_CATEGORY_EN)){
            return getCategory(this.getKategorie());
        }else{
            return ownkategorie;
        }
    }
}
