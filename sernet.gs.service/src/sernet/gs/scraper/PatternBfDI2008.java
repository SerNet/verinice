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


/**
 * Patterns to extract Threats and Safeguards from BfDI data protection module.
 * (BfDI: Der Bundesbeauftragte für den Datenschutz und die Informationsfreiheit)
 * 
 * Slight differences in paths to regular GS-catalogues.
 * 
 * @author koderman[at]sernet[dot]de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public class PatternBfDI2008 extends PatternGSHB2005_2006 {
	final String GET_GEFAEHRDUNGEN = 
		" declare namespace html = \"http://www.w3.org/1999/xhtml\";" +
		" for $a in //html:a" + // id (filename in url)
		" let $u := data($a/@href)" + //url
		" let $h := $a/../../../preceding-sibling::html:h3[position()=1]" + // KAT: a/td/tr/table/h3
		" let $t := $a/../../html:td[position()=3]" + // a/td/tr/3rd column: Titel
		" where   contains($a/@href, \"g0\")" + //link
		" and not ( contains($a/@href, \"../g/g01.htm\"))" + // not link to overview
		" and $h" + // header must exist
		// return kategorie, id, titel, url
		" return <mn>{$h}°{$a}°{$t}°{$u}</mn>"; 
	
	final String GET_MASSNAHMEN = 
		" declare namespace html = \"http://www.w3.org/1999/xhtml\";" +
		" for $a in //html:a" +
		" let $u := data($a/@href)" +
		" let $h := $a/../../../preceding-sibling::html:h3[position()=1]" + // LZ: a/td/tr/table/h3
		" let $s := $a/../../html:td[position()=3]" + // a/td/tr/3th column: Siegelstufe
		" let $t := $a/../../html:td[position()=4]" + // a/td/tr/4th column: Titel
		" where   contains($a/@href, \"m0\")" + //link to massnahme
		" and not ( contains($a/@href, \"../m/m01.htm\"))" + // not link to overview
		" and $h" + // header must exist
		" return <mn>{$h}°{$a}°{$t}°{$u}°{$s}</mn>"; // return lebenszyklus, id, titel, url, siegel

	@Override
	public String getMassnahmePattern() {
		return this.GET_MASSNAHMEN;
	}
	
	@Override
	public String getGefaehrdungPattern() {
		return this.GET_GEFAEHRDUNGEN;
	}
		
}
