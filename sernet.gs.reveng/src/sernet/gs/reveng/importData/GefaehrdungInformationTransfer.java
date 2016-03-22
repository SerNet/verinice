/*******************************************************************************
 * Copyright (c) 2015 Sebastian Hagedorn.
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Sebastian Hagedorn <sh[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.reveng.importData;

import java.io.Serializable;

/**
 * @author Sebastian Hagedorn <sh[at]sernet[dot]de>
 */
public class GefaehrdungInformationTransfer implements Serializable {

    private static final long serialVersionUID = 20160222104609L;
    private String description;
    private String titel;
    private String id;
    private int kategorie;
    private String stand;
    private String extId;
    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }
    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }
    /**
     * @return the titel
     */
    public String getTitel() {
        return titel;
    }
    /**
     * @param titel the titel to set
     */
    public void setTitel(String titel) {
        this.titel = titel;
    }
    /**
     * @return the id
     */
    public String getId() {
        return id;
    }
    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }
    
    /**
     * @return the kategorie
     */
    public int getKategorie() {
        return kategorie;
    }
    /**
     * @param kategorie the kategorie to set
     */
    public void setKategorie(int kategorie) {
        this.kategorie = kategorie;
    }
    /**
     * @return the stand
     */
    public String getStand() {
        return stand;
    }
    /**
     * @param stand the stand to set
     */
    public void setStand(String stand) {
        this.stand = stand;
    }

    public String getExtId() {
        return extId;
    }

    public void setExtId(String extId) {
        this.extId = extId;
    }
    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("ID:<" + getId() + ">\t");
        sb.append("Title:<" + getTitel() + ">");
        return sb.toString();
    }
    
    
}
