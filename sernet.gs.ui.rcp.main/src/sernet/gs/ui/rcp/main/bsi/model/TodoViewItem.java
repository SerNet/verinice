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
package sernet.gs.ui.rcp.main.bsi.model;

import java.io.Serializable;
import java.util.Date;
import java.util.Hashtable;
import java.util.Map;

import sernet.gs.ui.rcp.main.ImageCache;
import sernet.verinice.model.bsi.IMassnahmeUmsetzung;
import sernet.verinice.model.bsi.MassnahmenUmsetzung;

public class TodoViewItem implements Serializable, IMassnahmeUmsetzung, Comparable<TodoViewItem> {
	
	private static final Map<String, String> umsetzungImageMap = new Hashtable<String, String>();
	
	static {
		umsetzungImageMap.put(MassnahmenUmsetzung.P_UMSETZUNG_ENTBEHRLICH, ImageCache.MASSNAHMEN_UMSETZUNG_ENTBEHRLICH);
		umsetzungImageMap.put(MassnahmenUmsetzung.P_UMSETZUNG_JA, ImageCache.MASSNAHMEN_UMSETZUNG_JA);
		umsetzungImageMap.put(MassnahmenUmsetzung.P_UMSETZUNG_NEIN, ImageCache.MASSNAHMEN_UMSETZUNG_NEIN);
		umsetzungImageMap.put(MassnahmenUmsetzung.P_UMSETZUNG_TEILWEISE, ImageCache.MASSNAHMEN_UMSETZUNG_TEILWEISE);
		umsetzungImageMap.put(MassnahmenUmsetzung.P_UMSETZUNG_UNBEARBEITET, ImageCache.MASSNAHMEN_UMSETZUNG_UNBEARBEITET);
		umsetzungImageMap.put(MassnahmenUmsetzung.P_UMSETZUNG_ESTABLISHED, ImageCache.MASSNAHMEN_UMSETZUNG_TEILWEISE);
		umsetzungImageMap.put(MassnahmenUmsetzung.P_UMSETZUNG_MANAGED, ImageCache.MASSNAHMEN_UMSETZUNG_JA);
		umsetzungImageMap.put(MassnahmenUmsetzung.P_UMSETZUNG_OPTIMIZING, ImageCache.MASSNAHMEN_UMSETZUNG_TEILWEISE);
		umsetzungImageMap.put(MassnahmenUmsetzung.P_UMSETZUNG_PERFORMED, ImageCache.MASSNAHMEN_UMSETZUNG_JA);
		umsetzungImageMap.put(MassnahmenUmsetzung.P_UMSETZUNG_PREDICTABLE, ImageCache.MASSNAHMEN_UMSETZUNG_ENTBEHRLICH);
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
		//return umsetzungImageMap.get(getStand());
		// Grundschutz
		return umsetzungImageMap.get(getUmsetzung());
	}

	public Date getUmsetzungBis() {
		return umsetzungBis;
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
		this.umsetzungBis = umsetzungBis;
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

	public int getdbId() {
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
		this.naechsteRevision = naechsteRevision; 
	}

	public void setRevisionDurch(String revisionDurch) {
		this.revisionDurch = revisionDurch;
	}

	public String getRevisionDurch() {
		return this.revisionDurch;
	}

	public Date getNaechsteRevision() {
		return this.naechsteRevision;
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
