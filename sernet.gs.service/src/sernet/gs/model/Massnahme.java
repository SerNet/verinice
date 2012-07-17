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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

public class Massnahme implements IGSModel {
	private String id;
	private String titel;
	private int lebenszyklus = 0;
	private char siegelstufe = ' ';
	private String url;
	private String stand;
	private List<String> verantwortlichInitiierung = new ArrayList<String>(3);
	private List<String> verantwortlichUmsetzung = new ArrayList<String>(3);
	
	
	public static final int LZ_PLANUNG			= 1; 
	public static final int LZ_BESCHAFFUNG  	= 2; 
	public static final int LZ_UMSETZUNG 		= 3; 
	public static final int LZ_BETRIEB 		= 4;
	public static final int LZ_AUSSONDERUNG 	= 5;
	public static final int LZ_NOTFALL 		= 6;
	
	public static final String LZ_STRING_Planung 		= "Planung und Konzeption";
	public static final String LZ_STRING_Beschaffung 	= "Beschaffung";
	public static final String LZ_STRING_Umsetzung 	= "Umsetzung";
	public static final String LZ_STRING_Betrieb 		= "Betrieb";
	public static final String LZ_STRING_Ausonderung 	= "Aussonderung";
	public static final String LZ_STRING_Notfall 		= "Notfallvorsorge";
	
	
	private static Pattern kapitelPattern = Pattern.compile("(\\d+)\\.(\\d+)");

	
	@Override
	public String toString() {
		return id + " "+ titel + ", " + siegelstufe + ", " + lebenszyklus;
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
		int absvalue = 0;
		Matcher m = kapitelPattern.matcher(getId());
		if (m.find()) {
			try {
				 int whole = Integer.parseInt(m.group(1));
				 int radix = Integer.parseInt(m.group(2));
				 absvalue = whole * 1000 + radix;
			} catch (NumberFormatException e) {
				Logger.getLogger(this.getClass())
					.error("Kapitelnummer der Massnahme ist kein Float.", e);
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
	public int getLebenszyklus() {
		return lebenszyklus;
	}
	public void setLebenszyklus(int lebenszyklus) {
		this.lebenszyklus = lebenszyklus;
	}
	
	public String getLZAsString() {
		switch (this.lebenszyklus) {
		case LZ_AUSSONDERUNG:
			return LZ_STRING_Ausonderung;
		case LZ_BESCHAFFUNG:
			return LZ_STRING_Beschaffung;
		case LZ_BETRIEB:
			return LZ_STRING_Betrieb;
		case LZ_NOTFALL:
			return LZ_STRING_Notfall;
		case LZ_PLANUNG:
			return LZ_STRING_Planung;
		case LZ_UMSETZUNG:
			return LZ_STRING_Umsetzung;
		}
		return "";
	}
	
	public  boolean isStufeA() {
		return (siegelstufe == 'A');
	}
	
	public boolean isStufeB() {
		return isStufeA()
			|| siegelstufe == 'B';
	}
	
	public boolean isStufeC() {
		return isStufeB()
			|| siegelstufe == 'C';
	}
	
	public boolean isStufeZ() {
		return isStufeC()
			|| siegelstufe == 'Z';
	}
	
	public char getSiegelstufe() {
		return siegelstufe;
	}
	public void setSiegelstufe(char siegelstufe) {
		this.siegelstufe = siegelstufe;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getUrl() {
		return this.url;
	}

	public void setStand(String stand) {
		this.stand = stand;
	}
	
	public String getStand() {
		return stand;
	}

	public List<String> getVerantwortlichInitiierung() {
		return verantwortlichInitiierung;
	}

	public void setVerantwortlichInitiierung(List<String> verantwortlichInitiierung) {
		this.verantwortlichInitiierung = verantwortlichInitiierung;
	}

	public List<String> getVerantwortlichUmsetzung() {
		return verantwortlichUmsetzung;
	}

	public void setVerantwortlichUmsetzung(List<String> verantwortlichUmsetzung) {
		this.verantwortlichUmsetzung = verantwortlichUmsetzung;
	}
	
	public void addVerantwortlicheInitiierung(String role) {
		this.verantwortlichInitiierung.add(role);
	}
	
	public void addVerantwortlicheUmsetzung(String role) {
		this.verantwortlichUmsetzung.add(role);
	}

	}
