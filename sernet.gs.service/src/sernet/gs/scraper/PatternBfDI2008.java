package sernet.gs.scraper;

import java.util.regex.Pattern;

/**
 * Patterns to extract Threats and Safeguards from BfDI data protection module.
 * (BfDI: Der Bundesbeauftragte für den Datenschutz und die Informationsfreiheit)
 * 
 * Slight differences in paths to regular GS-catalogues.
 * 
 * @author koderman@sernet.de
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
