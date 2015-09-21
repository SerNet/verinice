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
import java.sql.Clob;

import sernet.gs.reveng.MbMassnTxt;

/**
 * since all textual information (especially name and description) are stored in a {@link Clob}
 * within {@link MbMassnTxt} and {@link Clob} can only be accessed within the same sql/hibernate session,
 * this object wraps all information needed to create an instance of sernet.gs.model.Massnahme or
 * sernet.verinice.model.bsi.MassnahmenUmsetzung
 * @author Sebastian Hagedorn <sh[at]sernet[dot]de>
 */


@SuppressWarnings("serial")
public class MassnahmeInformationTransfer implements Serializable{

    private String description;
    private String titel;
    private String zyklus;
    private char siegelstufe;
    private String id;
    private String htmltext;
    private String abstract_;
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
     * @return the zyklus
     */
    public String getZyklus() {
        return zyklus;
    }
    /**
     * @param zyklus the zyklus to set
     */
    public void setZyklus(String zyklus) {
        this.zyklus = zyklus;
    }
    /**
     * @return the siegelstufe
     */
    public char getSiegelstufe() {
        return siegelstufe;
    }
    /**
     * @param siegelstufe the siegelstufe to set
     */
    public void setSiegelstufe(char siegelstufe) {
        this.siegelstufe = siegelstufe;
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
     * @return the htmltext
     */
    public String getHtmltext() {
        return htmltext;
    }
    /**
     * @param htmltext the htmltext to set
     */
    public void setHtmltext(String htmltext) {
        this.htmltext = htmltext;
    }
    /**
     * @return the abstract_
     */
    public String getAbstract_() {
        return abstract_;
    }
    /**
     * @param abstract_ the abstract_ to set
     */
    public void setAbstract_(String abstract_) {
        this.abstract_ = abstract_;
    }
    
}
