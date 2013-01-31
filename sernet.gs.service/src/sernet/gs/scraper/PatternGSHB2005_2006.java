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
package sernet.gs.scraper;

import java.util.regex.Pattern;

/**
 * Patterns to extract Threats and Safeguards from Baseline-Catalogues 2005, 2006, 2007.
 * 
 * @author koderman[at]sernet[dot]de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public class PatternGSHB2005_2006 implements IGSPatterns {
	
	private static final String ENCODING = "iso-8859-1";
	
	private static final String GEFNAME = 
		"//html:a[contains(@href,'../g/g')]/../following-sibling::html:td/following-sibling::html:td";

	private static final String GET_BAUSTEINE = 
		" declare namespace html = \"http://www.w3.org/1999/xhtml\";" +
	    " for $a in //html:a " +
	    " let $u := data($a/@href)" +
	    " where contains($a/@href, \"b0\")" +
	    " return <b>{$a}°{$u}</b>"; // id titel, url
	
	private static final String GET_MASSNAHMEN = 
		" declare namespace html = \"http://www.w3.org/1999/xhtml\";" +
		" for $a in //html:a" +
		" let $u := data($a/@href)" +
		" let $h := $a/../../../preceding-sibling::html:h3[position()=1]" + // LZ: a/td/tr/table/h3
		" let $s := $a/../../html:td[position()=3]" + // a/td/tr/3th column: Siegelstufe
		" let $t := $a/../../html:td[position()=4]" + // a/td/tr/4th column: Titel
		" where   contains($a/@href, \"../m/m\")" + //link to massnahme
		" and not ( contains($a/@href, \"../m/m01.htm\"))" + // not link to overview
		" and $h" + // header must exist
		" return <mn>{$h}°{$a}°{$t}°{$u}°{$s}</mn>"; // return lebenszyklus, id, titel, url, siegel

	
	private static final String GET_GEFAEHRDUNGEN = 
		" declare namespace html = \"http://www.w3.org/1999/xhtml\";" +
		" for $a in //html:a" + // id (filename in url)
		" let $u := data($a/@href)" + //url
		" let $h := $a/../../../preceding-sibling::html:h3[position()=1]" + // KAT: a/td/tr/table/h3
		" let $t := $a/../../html:td[position()=3]" + // a/td/tr/3rd column: Titel
		" where   contains($a/@href, \"../g/g\")" + //link
		" and not ( contains($a/@href, \"../g/g01.htm\"))" + // not link to overview
		" and $h" + // header must exist
		// return kategorie, id, titel, url
		" return <mn>{$h}°{$a}°{$t}°{$u}</mn>"; 
	
	private static final String GET_TITLE = " declare namespace html = \"http://www.w3.org/1999/xhtml\";" +
		" for $t in //html:title" +
		" let $d := data($t)" +
		" return <title>{$d}</title>";
	
	private static final String GET_VERANTWORTLICHE = "declare namespace html = \"http://www.w3.org/1999/xhtml\"; " +
		"for $s in //html:span " +
		"let $r := data($s) " +
		"where contains($s/@class, \"gshbmassverantwortlichist\") " +
		"return <role>{$r}</role>";

	private final Pattern standPat = Pattern.compile("(\\d{4})");
	
	private final Pattern baustPat = Pattern.compile("(B \\d+.\\d+)\\s+(\\w*.*)°(.*)");
	
	private final Pattern schichtPat = Pattern.compile("(\\d)\\.\\d+");



	public String getGefName() {
		return GEFNAME;
	}

	public String getBausteinPattern() {
		return GET_BAUSTEINE;
	}

	public String getMassnahmePattern() {
		return GET_MASSNAHMEN;
	}

	public String getGefaehrdungPattern() {
		return GET_GEFAEHRDUNGEN;
	}

	public String getTitlePattern() {
		return GET_TITLE;
	}

	public Pattern getStandPat() {
		return standPat;
	}

	public Pattern getBaustPat() {
		return baustPat;
	}

	public Pattern getSchichtPat() {
		return schichtPat;
	}

	public String getMassnahmeVerantwortlichePattern() {
		return GET_VERANTWORTLICHE;
	}

	/* (non-Javadoc)
	 * @see sernet.gs.scraper.IGSPatterns#getEncoding()
	 */
	public String getEncoding() {
		return ENCODING;
	}

	
}
