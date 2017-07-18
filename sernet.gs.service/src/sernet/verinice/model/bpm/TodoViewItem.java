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
package sernet.verinice.model.bpm;

import java.io.Serializable;
import java.util.Date;
import java.util.Hashtable;
import java.util.Map;

import sernet.verinice.model.bsi.IMassnahmeUmsetzung;
import sernet.verinice.model.bsi.MassnahmenUmsetzung;

public class TodoViewItem implements Serializable, IMassnahmeUmsetzung, Comparable<TodoViewItem> {
	
    private static final long serialVersionUID = -2499910171824084508L;

    private static final Map<String, String> UMSETZUNG_IMAGE_MAP = new Hashtable<String, String>();
	
	public static final String MASSNAHMEN_UMSETZUNG_UNBEARBEITET = "exclamation.png";
    public static final String MASSNAHMEN_UMSETZUNG_NEIN = "16-em-cross.png";
    public static final String MASSNAHMEN_UMSETZUNG_JA = "16-em-check.png";
    public static final String MASSNAHMEN_UMSETZUNG_ENTBEHRLICH = "progress_rem.gif";
    public static final String MASSNAHMEN_UMSETZUNG_TEILWEISE = "16-clock.png";
	
	static {
		UMSETZUNG_IMAGE_MAP.put(MassnahmenUmsetzung.P_UMSETZUNG_ENTBEHRLICH, MASSNAHMEN_UMSETZUNG_ENTBEHRLICH);
		UMSETZUNG_IMAGE_MAP.put(MassnahmenUmsetzung.P_UMSETZUNG_JA, MASSNAHMEN_UMSETZUNG_JA);
		UMSETZUNG_IMAGE_MAP.put(MassnahmenUmsetzung.P_UMSETZUNG_NEIN, MASSNAHMEN_UMSETZUNG_NEIN);
		UMSETZUNG_IMAGE_MAP.put(MassnahmenUmsetzung.P_UMSETZUNG_TEILWEISE, MASSNAHMEN_UMSETZUNG_TEILWEISE);
		UMSETZUNG_IMAGE_MAP.put(MassnahmenUmsetzung.P_UMSETZUNG_UNBEARBEITET, MASSNAHMEN_UMSETZUNG_UNBEARBEITET);
		UMSETZUNG_IMAGE_MAP.put(MassnahmenUmsetzung.P_UMSETZUNG_ESTABLISHED, MASSNAHMEN_UMSETZUNG_TEILWEISE);
		UMSETZUNG_IMAGE_MAP.put(MassnahmenUmsetzung.P_UMSETZUNG_MANAGED, MASSNAHMEN_UMSETZUNG_JA);
		UMSETZUNG_IMAGE_MAP.put(MassnahmenUmsetzung.P_UMSETZUNG_OPTIMIZING, MASSNAHMEN_UMSETZUNG_TEILWEISE);
		UMSETZUNG_IMAGE_MAP.put(MassnahmenUmsetzung.P_UMSETZUNG_PERFORMED, MASSNAHMEN_UMSETZUNG_JA);
		UMSETZUNG_IMAGE_MAP.put(MassnahmenUmsetzung.P_UMSETZUNG_PREDICTABLE, MASSNAHMEN_UMSETZUNG_ENTBEHRLICH);
	}
	
	private String titel;
	private String umsetzung;
	private Date umsetzungBis;
	private String umsetzungDurch;
	private char stufe;
	private String parentTitle;
	private String url;
	private String stand;
	private Integer dbId;
	private Date naechsteRevision;
	private String revisionDurch;

	public String getTitle() {
		return titel;
	}

	public String getUmsetzung() {
		return umsetzung;
	}
	
	public String getUmsetzungIcon() {
		// ISO 27001
		// Grundschutz
		return "/resources/verinice-layout/images/icon/" + UMSETZUNG_IMAGE_MAP.get(getUmsetzung()) ;
	}

	public Date getUmsetzungBis() {
		return (umsetzungBis != null) ? (Date)umsetzungBis.clone() : null;
	}

	public String getUmsetzungDurch() {
		return umsetzungDurch;
	}

	public char getStufe() {
		return stufe;
	}

	public String getParentTitle() {
		return parentTitle;
	}

	public void setTitel(String title) {
		this.titel = title;
	}

	public void setUmsetzung(String umsetzung) {
		this.umsetzung = umsetzung;
	}

	public void setUmsetzungBis(Date umsetzungBis) {
		this.umsetzungBis = (umsetzungBis != null) ? (Date)umsetzungBis.clone() : null;
	}

	public void setUmsetzungDurch(String umsetzungDurch) {
		this.umsetzungDurch = umsetzungDurch;
	}

	public void setStufe(char stufe) {
		this.stufe = stufe;
	}

	public void setParentTitle(String parentTitle) {
		this.parentTitle = parentTitle;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void setStand(String stand) {
		this.stand = stand;
	}

	public String getUrl() {
		return url;
	}

	public String getStand() {
		return stand;
	}

	public int getDbId() {
		return dbId;
	}

	public void setDbId(Integer dbId2) {
		this.dbId = dbId2;
	}
	
	@Override
	public boolean equals(Object obj) {
		return (this == obj
				|| (obj instanceof TodoViewItem
					&& this.getParentTitle().equals(((TodoViewItem)obj).getParentTitle())
					&& this.titel.equals(((TodoViewItem)obj).getTitle())
					)
				);
	}
	
	@Override
	public int hashCode() {
		return dbId.hashCode() + url.hashCode() + titel.hashCode();
	}

	public void setNaechsteRevision(Date naechsteRevision) {
		this.naechsteRevision = (naechsteRevision != null) ? (Date)naechsteRevision.clone() : null; 
	}

	public void setRevisionDurch(String revisionDurch) {
		this.revisionDurch = revisionDurch;
	}

	public String getRevisionDurch() {
		return this.revisionDurch;
	}

	public Date getNaechsteRevision() {
		return (this.naechsteRevision !=  null) ? (Date)this.naechsteRevision.clone() : null;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(TodoViewItem other) {
		int result = 1;
		if(other!=null && other.getTitle()!=null) {
			if(this.getTitle()==null) {
				result = -1;
			} else {
				result = this.getTitle().compareTo(other.getTitle());
			}
		} else if(this.getTitle()==null) {
			result = 0;		
		}
		return result;
	}

}
