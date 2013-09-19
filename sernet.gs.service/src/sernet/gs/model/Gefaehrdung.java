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
package sernet.gs.model;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;


public class Gefaehrdung implements IGSModel {
	private Integer dbId;
	
	private String id;
	private String titel;
	private String url;
	private int kategorie = 0;
	private String stand;
	
	private String uuid;
	private String encoding;
	
	
	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public static final int KAT_UNDEF 			= 0;
	public static final int KAT_HOEHERE_GEWALT	= 1;
	public static final int KAT_ORG_MANGEL		= 2;
	public static final int KAT_MENSCH 		= 3;
	public static final int KAT_TECHNIK 		= 4; 
	public static final int KAT_VORSATZ 		= 5;
	
	// do not output these values, they are used for string matching:
	public static final String KAT_MATCH_HOEHERE_GEWALT		= "Gewalt";
	public static final String KAT_MATCH_ORG_MANGEL			= "Organisatorisch";
	public static final String KAT_MATCH_MENSCH 			= "Menschliche Fehlhandlungen";
	public static final String KAT_MATCH_TECHNIK 			= "Technisches Versagen";
	public static final String KAT_MATCH_VORSATZ 			= "tzliche Handlungen";

	public static final String KAT_STRING_HOEHERE_GEWALT	= "Höhere Gewalt";
	public static final String KAT_STRING_ORG_MANGEL		= "Organisatorische Mängel";
	public static final String KAT_STRING_MENSCH 			= "Menschliche Fehlhandlungen";
	public static final String KAT_STRING_TECHNIK 			= "Technisches Versagen";
	public static final String KAT_STRING_VORSATZ 			= "Vorsätzliche Handlungen";

    public static final String TYPE_ID = "gefaehrdung";
	
	private static Pattern kapitelPattern = Pattern.compile("(\\d+)\\.(\\d+)");

	
	public Gefaehrdung() {
		uuid = UUID.randomUUID().toString();
	}
	
	@Override
	public String toString() {
		return id + " "+ titel + " ["  + getKategorieAsString() + "]"; 
	}
	
	@Override
	public boolean equals(Object obj) {
		return (this == obj
				|| (obj instanceof Gefaehrdung
					&& this.uuid.equals(((Gefaehrdung)obj).getUuid())
					)
				);
	}
	
	@Override
	public int hashCode() {
		return uuid.hashCode();
	}
	
	public String getId() {
		return id;
	}
	
	/**
	 * Return Kapitel as comparable value, i.e. converts 3.42 to 3042 or 3.221 to 3221
	 * 
	 * 
	 * @return
	 */
	public int getKapitelValue() {
	    final int absValueFactor = 1000;
		int absvalue = 0;
		Matcher m = kapitelPattern.matcher(getId());
		if (m.find()) {
			try {
				 int whole = Integer.parseInt(m.group(1));
				 int radix = Integer.parseInt(m.group(2));
				 absvalue = whole * absValueFactor + radix;
			} catch (NumberFormatException e) {
				Logger.getLogger(this.getClass())
					.error("Kapitelnummer der Gefaehrdung ist kein Float.", e);
			}
		}
		return absvalue;
	
	}
	
	public void setId(String id) {
		this.id = id;
	}
	public String getTitel() {
		return titel;
	}
	public void setTitel(String titel) {
		this.titel = titel;
	}
	
	public String getKategorieAsString() {
		switch (this.kategorie) {
		case KAT_HOEHERE_GEWALT:
			return KAT_STRING_HOEHERE_GEWALT;
		case KAT_MENSCH:
			return KAT_STRING_MENSCH;
		case KAT_ORG_MANGEL:
			return KAT_STRING_ORG_MANGEL;
		case KAT_TECHNIK:
			return KAT_STRING_TECHNIK;
		case KAT_VORSATZ:
			return KAT_STRING_VORSATZ;
		}
		return "";
	}
	
	
	public void setUrl(String url) {
		this.url = url;
	}
	public String getUrl() {
		return this.url;
	}

	public int getKategorie() {
		return kategorie;
	}

	public void setKategorie(int kategorie) {
		this.kategorie = kategorie;
	}

	public static int kategorieAsInt(String kategorie) {
		if (kategorie.indexOf(KAT_MATCH_HOEHERE_GEWALT) != -1){
			return KAT_HOEHERE_GEWALT;
		}
		if (kategorie.indexOf(KAT_MATCH_MENSCH) != -1){
			return KAT_MENSCH;
		}
		if (kategorie.indexOf(KAT_MATCH_ORG_MANGEL) != -1){
			return KAT_ORG_MANGEL;
		}
		if (kategorie.indexOf(KAT_MATCH_TECHNIK) != -1){
			return KAT_TECHNIK;
		}
		if (kategorie.indexOf(KAT_MATCH_VORSATZ) != -1){
			return KAT_VORSATZ;
		}
		return KAT_UNDEF;
		
	}

	public void setStand(String stand) {
		this.stand = stand;
		
	}
	
	public String getStand() {
		return stand;
	}

	public Integer getDbId() {
		return dbId;
	}

	public void setDbId(Integer dbId) {
		this.dbId = dbId;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	

	

}
