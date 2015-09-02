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
/**
 * 
 */
package sernet.verinice.model.bsi.risikoanalyse;

import java.util.UUID;

import sernet.gs.model.Massnahme;
import sernet.hui.common.connect.ITypedElement;


/**
 * @author ahanekop[at]sernet[dot]de
 *
 */
public class RisikoMassnahme extends Massnahme implements ITypedElement {
	
	private int dbId;
	private String number;
	private String name;
	private String description;
	
	private String uuid;
	
	public static final String SIEGEL = "Z";
    public static final String TYPE_ID = "risikomassnahme";
	
	
	public RisikoMassnahme() {
		uuid = UUID.randomUUID().toString();
	}
	
	 /* (non-Javadoc)
     * @see sernet.hui.common.connect.ITypedElement#getTypeId()
     */
    public String getTypeId() {
        return TYPE_ID;
    }
	
	@Override
	public boolean equals(Object obj) {
		return (this == obj
				|| (obj instanceof RisikoMassnahme
					&& this.uuid.equals(((RisikoMassnahme)obj).getUuid())
					)
				);
	}
	
	@Override
	public int hashCode() {
		return uuid.hashCode();
	}
	

	/**
	 * Must be implemented due to Interface IGefaehrdungsBaumElement.
	 * 
	 * @return - description (String) of the RisikoMassnahmenUmsetzung.
	 */
	public String getDescription() {
		return description;
	}
	
	/**
	 * Sets the description of the RisikoMassnahmenUmsetzung.
	 * 
	 * @param newDescription - new description (String) of the
	 * 		  RisikoMassnahmenUmsetzung
	 */
	public void setDescription(String newDescription) {
		description = newDescription;
	}

	/**
	 * Returns the nuber of the Massnahme.
	 * 
	 * @return the number
	 */
	public String getNumber() {
		return number;
	}

	/**
	 * Sets the number of the Massnahme.
	 * 
	 * @param newNumber the number to set
	 */
	public void setNumber(String newNumber) {
		number = newNumber;
	}
	
	/**
	 * Returns the Siegelstufe of the Massnahme, which is always "Z" (for
	 * additional Massnahmen).
	 * 
	 * @return the Siegelstufe of the Massnahme
	 */
	public String getSiegel() {
		return SIEGEL;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getDbId() {
		return dbId;
	}

	public void setDbId(int dbId) {
		this.dbId = dbId;
	}


	public String getUuid() {
		return uuid;
	}


	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
}
