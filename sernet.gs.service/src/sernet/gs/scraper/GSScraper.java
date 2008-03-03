package sernet.gs.scraper;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.saxon.Configuration;
import net.sf.saxon.dom.DocumentWrapper;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.query.DynamicQueryContext;
import net.sf.saxon.query.StaticQueryContext;
import net.sf.saxon.query.XQueryExpression;
import net.sf.saxon.trans.XPathException;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;

import sernet.gs.model.Baustein;
import sernet.gs.model.Gefaehrdung;
import sernet.gs.model.Massnahme;
import sernet.gs.service.GSServiceException;

/**
 * Scraper to extract modules and safeguards from BSI's
 * HTML Files using XQuery FLWOR expressions.
 * 
 * @author akoderman@sernet.de
 *
 */
public class GSScraper {

	
	private static final String gefName = 
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
	final Pattern standPat = Pattern.compile("(\\d{4})");
	private String stand;

	final Pattern baustPat = Pattern.compile("(B \\d+.\\d+)\\s+(\\w*.*)°(.*)");
	
	final Pattern schichtPat = Pattern.compile("(\\d)\\.\\d+");
	
	private IGSSource source;
	private Configuration config;
	private XQueryExpression getBausteineExp;
	private DynamicQueryContext bausteinContext;
	
	private XQueryExpression getMassnahmenExp;
	private XQueryExpression getGefaehrdungenExp;
	private DynamicQueryContext massnahmenContext;

	private XQueryExpression getTitleExp;
	private DynamicQueryContext titleContext;

	private DynamicQueryContext gefaehrdungenContext;


	public GSScraper(IGSSource source) throws  GSServiceException {
		
		try {
			this.source = source;
			config = new Configuration();
			StaticQueryContext staticContext;
			staticContext = new StaticQueryContext(config);
			
			getBausteineExp = staticContext.compileQuery(GET_BAUSTEINE);
			bausteinContext = new DynamicQueryContext(config);
			
			getMassnahmenExp = staticContext.compileQuery(GET_MASSNAHMEN);
			massnahmenContext = new DynamicQueryContext(config);
			
			getGefaehrdungenExp = staticContext.compileQuery(GET_GEFAEHRDUNGEN);
			gefaehrdungenContext = new DynamicQueryContext(config);

			getTitleExp = staticContext.compileQuery(GET_TITLE);
			titleContext = new DynamicQueryContext(config);
			
			
		} catch (XPathException e) {
			Logger.getLogger(GSScraper.class).error(e);
			throw new GSServiceException(e);
		}

	}

	public List<Baustein> getBausteine(String kapitel) throws GSServiceException {
		ArrayList<Baustein> result = new ArrayList<Baustein>();
		try {
			
			Node root = source.parseDocument(kapitel);
			getStand(kapitel, root);
			
			
			bausteinContext.setContextItem(new DocumentWrapper(root, kapitel, config));
//			dynamicContext.setContextItem(
//		        staticContext.buildDocument(
//		                new StreamSource(new File("test.xml"))));
			

			SequenceIterator iterator = getBausteineExp.iterator(bausteinContext);
			
			while (true) {
			    NodeInfo baust = (NodeInfo)iterator.next();
			    if (baust==null) break;
			    String found = baust.getStringValue();
			    found = found.replaceAll("\n", "");
			    found = found.replaceAll(".htm", "");
			    
			    
			    Matcher matcher = baustPat.matcher(found);
			    if (matcher.matches()) {
			    	Baustein b = new Baustein();
			    	b.setStand(stand);
			    	b.setId(matcher.group(1));
			    	b.setTitel(matcher.group(2));
			    	b.setUrl(matcher.group(3));
			    	
			    	Matcher schichtMatcher = schichtPat.matcher(matcher.group(1));
			    	String schicht="0";
			    	if (schichtMatcher.find()) 
			    		schicht = schichtMatcher.group(1);
			    	b.setSchicht(Integer.parseInt(schicht));
			    	
			    	result.add(b);
			    	
			    }
			}
		} catch (XPathException e) {
			Logger.getLogger(GSScraper.class).error(e);
			throw new GSServiceException(e);
		}
		return result ;
	}

	private void getStand(String kapitel, Node root) throws XPathException {
//		if (stand != null)
//			return;
		
		titleContext.setContextItem(new DocumentWrapper(root, kapitel, config));
		SequenceIterator iterator = getTitleExp.iterator(titleContext);
		NodeInfo title = (NodeInfo)iterator.next();
		if (title != null) {
		 Matcher matcher = standPat.matcher(title.getStringValue());
		 if (matcher.find()) {
			 stand = matcher.group(1);
		 }
		}
	}

	public List<Massnahme> getMassnahmen(String baustein) throws GSServiceException {
		List<Massnahme> result = new ArrayList<Massnahme>();
		try {
			Node root = source.parseDocument(baustein);
			getStand(baustein, root);
			massnahmenContext.setContextItem(new DocumentWrapper(root, baustein, config));

			SequenceIterator iterator = getMassnahmenExp.iterator(massnahmenContext);
			Pattern pat = Pattern.compile("(.*)°(.*)°(.+)°(.*)°\\((.*)\\)");
			Pattern pat2 = Pattern.compile("(.*)°(.*)°°(.*)°\\s*\\((.)\\)\\s*(.*)");
			//i.e.: Planung und Konzeption°M 2.343°°m02343°(C)	Absicherung eines SAP Systems im Portal-Szenario
			
			while (true) {
			    NodeInfo mnNode = (NodeInfo)iterator.next();
			    if (mnNode==null) break;
			    String found = mnNode.getStringValue();
			    found = found.replaceAll("\n", "");
			    found = found.replaceAll(".htm", "");
			    found = found.replaceAll("../m/", "");
//			    System.out.println(found);
			    
			    Matcher matcher = pat.matcher(found);
			    if (matcher.matches()) {
			    	Massnahme mn = new Massnahme();
			    	mn.setStand(stand);
			    	setLebenszyklus(mn, matcher.group(1));
			    	mn.setId(matcher.group(2));
			    	mn.setTitel(matcher.group(3));
			    	mn.setUrl(matcher.group(4));
			    	if (matcher.group(5)!=null && matcher.group(5).length()>0)
			    		mn.setSiegelstufe(matcher.group(5).charAt(0));
			    	else {
			    		Logger.getLogger(this.getClass())
		    			.error("Konnte Siegelstufe nicht bestimmen für: " + mn.getId()
		    					+ "\n Setze auf Stufe A (höchste).");
			    		mn.setSiegelstufe('A');
			    	}
			    	result.add(mn);
			    } else {
			    	// sometimes, 3rd column is missing
			    	// siegel included in 4th column:
			    	matcher = pat2.matcher(found);
			    	if (matcher.matches()) {
			    		Massnahme mn = new Massnahme();
			    		mn.setStand(stand);
				    	setLebenszyklus(mn, matcher.group(1));
				    	mn.setId(matcher.group(2));
				    	mn.setUrl(matcher.group(3));
				    	mn.setTitel(matcher.group(5));
				    	if (matcher.group(4)!=null && matcher.group(4).length()>0)
				    		mn.setSiegelstufe(matcher.group(4).charAt(0));
				    	else {
				    		Logger.getLogger(this.getClass())
				    			.error("Konnte Siegelstufe nicht bestimmen für: " + mn.getId()
				    					+ "\n Setze auf Stufe A (höchste).");
				    		mn.setSiegelstufe('A');
				    	}
				    	result.add(mn);
			    	}
			    }
			    
			}
		} catch (XPathException e) {
			Logger.getLogger(GSScraper.class).error(e);
			throw new GSServiceException(e);
		}
		return result ;
	
	}

	private void setLebenszyklus(Massnahme mn, String lzString) {
		if (lzString.equals(Massnahme.LZ_STRING_Ausonderung))
			mn.setLebenszyklus(Massnahme.LZ_AUSSONDERUNG);
		
		else if (lzString.equals(Massnahme.LZ_STRING_Beschaffung))
			mn.setLebenszyklus(Massnahme.LZ_BESCHAFFUNG);
		
		else if (lzString.equals(Massnahme.LZ_STRING_Betrieb))
			mn.setLebenszyklus(Massnahme.LZ_BETRIEB);
		
		else if (lzString.equals(Massnahme.LZ_STRING_Notfall))
			mn.setLebenszyklus(Massnahme.LZ_NOTFALL);
		
		else if (lzString.equals(Massnahme.LZ_STRING_Planung))
			mn.setLebenszyklus(Massnahme.LZ_PLANUNG);
		
		else if (lzString.equals(Massnahme.LZ_STRING_Umsetzung))
			mn.setLebenszyklus(Massnahme.LZ_UMSETZUNG);
	}
	
	public InputStream getBaustein(String url, String stand) throws GSServiceException {
		if (!stand.equals(this.stand))
			throw new GSServiceException("Versionstand des Bausteins weicht von geladenen " +
					"Grundschutz-Katalogen ab."); 
		return source.getBausteinAsStream(url);
	}
	
	public InputStream getMassnahme(String url, String stand) throws GSServiceException {
		if (!stand.equals(this.stand))
			throw new GSServiceException("Versionstand der Massnahme weicht von geladenen " +
					"Grundschutz-Katalogen ab.");
		return source.getMassnahmeAsStream(url);
	}
	
	public InputStream getGefaehrdung(String url, String stand) throws GSServiceException {
		if (!stand.equals(this.stand))
			throw new GSServiceException("Versionstand des Bausteins weicht von geladenen " +
					"Grundschutz-Katalogen ab.");
		return source.getGefaehrdungAsStream(url);
	}

	public List<Gefaehrdung> getGefaehrdungen(String baustein) throws GSServiceException {
		List<Gefaehrdung> result = new ArrayList<Gefaehrdung>();
		try {
			Node root = source.parseDocument(baustein);
			getStand(baustein, root);
			gefaehrdungenContext.setContextItem(new DocumentWrapper(root, baustein, config));

//			 return kategorie, id, titel, url
			SequenceIterator iterator = getGefaehrdungenExp.iterator(gefaehrdungenContext);
			Pattern pat = Pattern.compile("(.*)°(.*)°(.*)°(.*)");
			
			while (true) {
			    NodeInfo gfNode = (NodeInfo)iterator.next();
			    if (gfNode==null) break;
			    String found = gfNode.getStringValue();
			    found = found.replaceAll("\n", "");
			    found = found.replaceAll(".htm", "");
			    found = found.replaceAll("../g/", "");
//			    System.out.println(found);
			    
			    Matcher matcher = pat.matcher(found);
			    if (matcher.matches()) {
			    	Gefaehrdung gef = new Gefaehrdung();
			    	gef.setStand(stand);
			    	gef.setKategorie(Gefaehrdung.kategorieAsInt(matcher.group(1)));
			    	gef.setId(matcher.group(2));
			    	gef.setTitel(matcher.group(3));
			    	gef.setUrl(matcher.group(4));
			    	result.add(gef);
			    } 
			}			    
		} catch (XPathException e) {
			Logger.getLogger(GSScraper.class).error(e);
			throw new GSServiceException(e);
		}
		return result ;
	
	}

}
