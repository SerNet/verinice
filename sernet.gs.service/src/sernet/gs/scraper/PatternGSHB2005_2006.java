package sernet.gs.scraper;

import java.util.regex.Pattern;

/**
 * Patterns to extract Threats and Safeguards from Baseline-Catalogues 2005, 2006, 2007.
 * 
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public class PatternGSHB2005_2006 implements IGSPatterns {
	
	private final String gefName = 
		"//html:a[contains(@href,'../g/g')]/../following-sibling::html:td/following-sibling::html:td";

	final String GET_BAUSTEINE = 
		" declare namespace html = \"http://www.w3.org/1999/xhtml\";" +
	    " for $a in //html:a " +
	    " let $u := data($a/@href)" +
	    " where contains($a/@href, \"b0\")" +
	    " return <b>{$a}°{$u}</b>"; // id titel, url
	
	final String GET_MASSNAHMEN = 
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

	
	final String GET_GEFAEHRDUNGEN = 
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
	
	final String GET_TITLE = " declare namespace html = \"http://www.w3.org/1999/xhtml\";" +
		" for $t in //html:title" +
		" let $d := data($t)" +
		" return <title>{$d}</title>";
	
	private static final String GET_VERANTWORTLICHE = "declare namespace html = \"http://www.w3.org/1999/xhtml\"; " +
		"for $s in //html:span " +
		"let $r := data($s) " +
		"where contains($s/@class, \"gshbmassverantwortlichist\") " +
		"return <role>{$r}</role>";

	final Pattern standPat = Pattern.compile("(\\d{4})");
	
	final Pattern baustPat = Pattern.compile("(B \\d+.\\d+)\\s+(\\w*.*)°(.*)");
	
	final Pattern schichtPat = Pattern.compile("(\\d)\\.\\d+");



	public String getGefName() {
		return gefName;
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

	
}
