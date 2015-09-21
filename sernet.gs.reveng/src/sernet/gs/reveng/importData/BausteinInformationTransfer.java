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
import java.util.Date;

import sernet.gs.reveng.MbBaust;
import sernet.gs.reveng.MbMassnTxt;
import sernet.gs.reveng.ModZobjBst;

/**
 *  * since all textual information (especially name and description) are stored in a {@link Clob}
 * within {@link MbMassnTxt} and {@link Clob} can only be accessed within the same sql/hibernate session,
 * this object wraps all information needed to create an instance of sernet.gs.model.Massnahme or
 * sernet.verinice.model.bsi.MassnahmenUmsetzung
 * @author Sebastian Hagedorn <sh[at]sernet[dot]de>
 */
@SuppressWarnings("serial")
public class BausteinInformationTransfer implements Serializable {
    
    private String titel;
    private String kapitel;
    private String id;
    private char siegel;
    private String description;
    private String encoding;
    private String schicht;
    private String nr;
    private Date erfasstAm;
    private int zobId;
    private ModZobjBst mzb;
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
     * @return the kapitel
     */
    public String getKapitel() {
        return kapitel;
    }
    /**
     * @param kapitel the kapitel to set
     */
    public void setKapitel(String kapitel) {
        this.kapitel = kapitel;
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
     * @return the siegel
     */
    public char getSiegel() {
        return siegel;
    }
    /**
     * @param siegel the siegel to set
     */
    public void setSiegel(char siegel) {
        this.siegel = siegel;
    }
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
     * @return the encoding
     */
    public String getEncoding() {
        return encoding;
    }
    /**
     * @param encoding the encoding to set
     */
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }
    /**
     * @return the schicht
     */
    public String getSchicht() {
        return schicht;
    }
    /**
     * @param schicht the schicht to set
     */
    public void setSchicht(String schicht) {
        this.schicht = schicht;
    }
    /**
     * @return the nr
     */
    public String getNr() {
        return nr;
    }
    /**
     * @param nr the nr to set
     */
    public void setNr(String nr) {
        this.nr = nr;
    }
    /**
     * @return the erfasstAm
     */
    public Date getErfasstAm() {
        return erfasstAm;
    }
    /**
     * @param erfasstAm the erfasstAm to set
     */
    public void setErfasstAm(Date erfasstAm) {
        this.erfasstAm = erfasstAm;
    }
    /**
     * @return the zobId
     */
    public int getZobId() {
        return zobId;
    }
    /**
     * @param zobId the zobId to set
     */
    public void setZobId(int zobId) {
        this.zobId = zobId;
    }
    /**
     * @return the baust
     */
    public MbBaust getBaust() {
        return mzb.getMbBaust();
    }
    /**
     * @return the mzb
     */
    public ModZobjBst getMzb() {
        return mzb;
    }
    /**
     * @param mzb the mzb to set
     */
    public void setMzb(ModZobjBst mzb) {
        this.mzb = mzb;
    }
    
}
