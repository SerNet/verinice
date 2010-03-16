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

public class Baustein implements IGSModel{
	

	private static Pattern kapitelPattern = Pattern.compile("(\\d+)\\.(\\d+)");

	
	public static final int SCHICHT_0_MISSING = 0;
	public static final int SCHICHT_1_UEBERGEORDNETE_ASPEKTE = 1;
	public static final int SCHICHT_2_INFRASTRUKTUR = 2;
	public static final int SCHICHT_3_IT_SYSTEME = 3;
	public static final int SCHICHT_4_NETZ = 4;
	public static final int SCHICHT_5_ANWENDUNGEN = 5;
	
	private String id;
	private String titel;
	private String url;
	private List<Massnahme> massnahmen;
	private List<Gefaehrdung> gefaehrdungen;
	private String encoding;
	
	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	private int schicht = 0;


	private String stand;
	
	public Baustein() {
		massnahmen = new ArrayList<Massnahme>();
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
	
	@Override
	public String toString() {
		return id + " " + titel;
	}
	
	public List<Massnahme> getMassnahmen() {
		return massnahmen;
	}

	public void setMassnahmen(List<Massnahme> massnahmen) {
		this.massnahmen = massnahmen;
	}

	public String getId() {
		return id;
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
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	
	public void add(Massnahme m) {
		massnahmen.add(m);
	}

	public int getSchicht() {
		return schicht;
	}

	public void setSchicht(int schicht) {
		this.schicht = schicht;
	}

	public void setStand(String stand) {
		this.stand = stand;
	}

	public String getStand() {
		return stand;
	}

	public List<Gefaehrdung> getGefaehrdungen() {
		return gefaehrdungen;
	}

	public void setGefaehrdungen(List<Gefaehrdung> gefaehrdungen) {
		this.gefaehrdungen = gefaehrdungen;
	}
	
}
