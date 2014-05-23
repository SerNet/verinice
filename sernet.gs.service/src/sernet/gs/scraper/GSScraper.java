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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
 * Scraper to extract modules and safeguards from BSI's HTML Files using XQuery
 * FLWOR expressions.
 * 
 * @author akoderman[at]sernet[dot]de
 * 
 */
public class GSScraper {

	private final Map<String, String[]> brokenRoles = new HashMap<String, String[]>();
	
	private static final int GROUP3 = 3;
    private static final int GROUP4 = 4;
    private static final int GROUP5 = 5;
	

	private IGSPatterns patterns;

	public IGSPatterns getPatterns() {
		return patterns;
	}

	private String stand;

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

	private XQueryExpression massnahmenVerantwortlicheExp;

	private DynamicQueryContext massnahmenVerantowrtlicheContext;

	private Pattern trailingwhitespace = Pattern.compile("\\s*$");
	private Pattern leadingwhitespace = Pattern.compile("^\\s*");

	private String cacheDir = "gscache";

	public GSScraper(IGSSource source, IGSPatterns patterns)
			throws GSServiceException {

		try {
			createBrokenRoleReplacements();
			this.patterns = patterns;
			this.source = source;
			config = new Configuration();
			StaticQueryContext staticContext;
			staticContext = new StaticQueryContext(config);

			getBausteineExp = staticContext.compileQuery(patterns
					.getBausteinPattern());
			bausteinContext = new DynamicQueryContext(config);

			getMassnahmenExp = staticContext.compileQuery(patterns
					.getMassnahmePattern());
			massnahmenContext = new DynamicQueryContext(config);

			getGefaehrdungenExp = staticContext.compileQuery(patterns
					.getGefaehrdungPattern());
			gefaehrdungenContext = new DynamicQueryContext(config);

			getTitleExp = staticContext
					.compileQuery(patterns.getTitlePattern());
			titleContext = new DynamicQueryContext(config);

			massnahmenVerantwortlicheExp = staticContext.compileQuery(patterns
					.getMassnahmeVerantwortlichePattern());
			massnahmenVerantowrtlicheContext = new DynamicQueryContext(config);

		} catch (XPathException e) {
			Logger.getLogger(GSScraper.class).error(e);
			throw new GSServiceException(e);
		}

	}

	private void createBrokenRoleReplacements() {
	    String itSicherheitsManagement = "IT-Sicherheitsmanagement";
		brokenRoles.put("Behörden-/Unter-nehmensleitung",
				new String[] { "Behörden-/Unternehmensleitung" });
		brokenRoles.put("IT-Sicherheits-management",
				new String[] { itSicherheitsManagement });
		brokenRoles.put("IT-Sicherheitsmanagement-Team",
				new String[] { itSicherheitsManagement });
		brokenRoles.put("IT-Sicherheitsmanagement Administrator",
				new String[] { itSicherheitsManagement, "Administrator" });
		brokenRoles.put("Leiter IT Administrator", new String[] { "Leiter IT",
				"Administrator" });
		brokenRoles.put("Leiter IT IT-Sicherheitsmanagement", new String[] {
				"Leiter IT", itSicherheitsManagement });
	}

	public List<Baustein> getBausteine(String kapitel)
			throws GSServiceException, IOException {
		ArrayList<Baustein> result = new ArrayList<Baustein>();
		try {
			List fromCache = getFromCache("bausteine_", kapitel);
			for (Object object : fromCache) {
				result.add((Baustein) object);
			}
		} catch (Exception e) {
			// do nothing
		}
		if (result != null && result.size() > 0){
			return result;
		}
		// else parse from HTML:
		try {
			Node root = source.parseBausteinDocument(kapitel);
			getStand(kapitel, root);

			bausteinContext.setContextItem(new DocumentWrapper(root, kapitel,
					config));

			SequenceIterator iterator = getBausteineExp
					.iterator(bausteinContext);

			while (true) {
				NodeInfo baust = (NodeInfo) iterator.next();
				if (baust == null){
					break;
				}
				String found = baust.getStringValue();
				found = found.replaceAll("\n", "");
				found = found.replaceAll(".html", "");
				found = found.replaceAll(".htm", "");

				Matcher matcher = patterns.getBaustPat().matcher(found);
				if (matcher.matches()) {
					Baustein b = new Baustein();
					b.setStand(stand);
					b.setId(matcher.group(1));
					b.setTitel(matcher.group(2));
					b.setUrl(matcher.group(GROUP3));
					b.setEncoding(getPatterns().getEncoding());

					Matcher schichtMatcher = patterns.getSchichtPat().matcher(
							matcher.group(1));
					String schicht = "0";
					if (schichtMatcher.find()){
						schicht = schichtMatcher.group(1);
					}
					b.setSchicht(Integer.parseInt(schicht));
					result.add(b);

				}
			}
		} catch (XPathException e) {
			Logger.getLogger(GSScraper.class).error(e);
			throw new GSServiceException(e);
		}
		writeToFile("bausteine_" + kapitel, result);
		return result;
	}

	private List getFromCache(String prefix, String fileName) {
		// try to get from cache:
		try {
			List resultFromFile = readFromFile(prefix + fileName);
			if (resultFromFile != null && resultFromFile.size() > 0) {
				return resultFromFile;
			}
		} catch (IOException e1) {
			// do nothing
		} catch (ClassNotFoundException e1) {
			// do nothing
		}
		return new ArrayList();
	}

	private List readFromFile(String fileName) throws IOException,
			ClassNotFoundException {
		File dir = new File(cacheDir);
		if (!dir.exists()) {
			return null;
		}
		String filename0 = null;
		filename0 = fileName.replaceAll("\\.\\./", "");
		filename0 = fileName.replaceAll("/", "_");

		FileInputStream fin = new FileInputStream(dir.getAbsolutePath()
				+ File.separator + filename0);
		ObjectInputStream ois = new ObjectInputStream(fin);
		ArrayList result = (ArrayList) ois.readObject();
		ois.close();
		return result;
	}

	public boolean flushCache() {
		File dir = new File(cacheDir);
		Logger.getLogger(this.getClass()).debug("Deleting cache dir: " + dir.getAbsolutePath());
		return delete(dir);
	}

	public static boolean delete(File dir) {
		if (dir.isDirectory()) {
			String[] subdirs = dir.list();
			for (int i = 0; i < subdirs.length; i++) {
				boolean success = delete(new File(dir, subdirs[i]));
				if (!success) {
					return false;
				}
			}
		}
		return dir.delete();
	}

	private void writeToFile(String fileName, ArrayList b) throws IOException{
	    String fileName0 = fileName.replaceAll("\\.\\./", "");
		fileName0 = fileName0.replaceAll("/", "_");
		writeToFile(b, fileName0);
	}

	private void writeToFile(Serializable object, String fileName) throws IOException{
		File dir = new File(cacheDir);
		if (!dir.exists()) {
			boolean success = dir.mkdirs();
			if(!success){
			    throw new IOException("Could not create directory");
			}
			Logger.getLogger(this.getClass()).debug(
					"Creating GS cache dir " + dir.getAbsolutePath());
		}

		try {
			FileOutputStream fout = new FileOutputStream(dir.getAbsolutePath()
					+ File.separator + fileName);
			ObjectOutputStream oos = new ObjectOutputStream(fout);
			oos.writeObject(object);
			oos.close();
		} catch (Exception e) {
			Logger.getLogger(this.getClass()).error(
					"Fehler beim Schreiben von Objekt in Festplatten-Cache", e);
		}
	}

	private void getStand(String kapitel, Node root) throws XPathException {
		titleContext.setContextItem(new DocumentWrapper(root, kapitel, config));
		SequenceIterator iterator = getTitleExp.iterator(titleContext);
		NodeInfo title = (NodeInfo) iterator.next();
		if (title != null) {
			Matcher matcher = patterns.getStandPat().matcher(
					title.getStringValue());
			if (matcher.find()) {
				stand = matcher.group(1);
			}
		}
	}

	public List<Massnahme> getMassnahmen(String baustein)
			throws GSServiceException, IOException {
		ArrayList<Massnahme> result = new ArrayList<Massnahme>();
		try {
			List fromCache = getFromCache("massnahmen_", baustein);
			for (Object object : fromCache) {
				result.add((Massnahme) object);
			}
		} catch (Exception e) {
			// do nothing
		}
		if (result != null && result.size() > 0){
			return result;
		}
		try {
			Node root = source.parseBausteinDocument(baustein);
			getStand(baustein, root);
			massnahmenContext.setContextItem(new DocumentWrapper(root,
					baustein, config));

			result = fillResult(result);
		} catch (XPathException e) {
			Logger.getLogger(GSScraper.class).error(e);
			throw new GSServiceException(e);
		}
		writeToFile("massnahmen_" + baustein, result);
		return result;

	}

    private ArrayList<Massnahme> fillResult(ArrayList<Massnahme> result) throws XPathException, GSServiceException {
        SequenceIterator iterator = getMassnahmenExp
        		.iterator(massnahmenContext);

        // normal pattern for massnahmen:
        Pattern pat = Pattern.compile("(.*)°(.*)°(.+)°(.*)°\\((.*)\\)");

        // sometimes the 3rd column is missing:
        Pattern pat2 = Pattern
        		.compile("(.*)°(.*)°°(.*)°\\s*\\((.)\\)\\s*(.*)");
        // i.e.: Planung und Konzeption°M 2.343°°m02343°(C) Absicherung
        // eines SAP Systems im Portal-Szenario

        while (true) {
        	NodeInfo mnNode = (NodeInfo) iterator.next();
        	if (mnNode == null){
        		break;
        	}
        	String found = mnNode.getStringValue();
        	// clear up paths, remove relative paths (don't work in zipfile)
        	found = found.replaceAll("\n", "");
        	found = found.replaceAll(".html", "");
        	found = found.replaceAll(".htm", "");
        	found = found.replaceAll("\\.\\./\\.\\./m/m\\d\\d/", "");
        	found = found.replaceAll("\\.\\./m/", "");
        	found = found.replaceAll("\\.\\./\\.\\./", "");

        	Matcher matcher = pat.matcher(found);
        	if (matcher.matches()) {
        		Massnahme mn = new Massnahme();
        		mn.setStand(stand);
        		setLebenszyklus(mn, matcher.group(1));
        		mn.setId(matcher.group(2));
        		mn.setTitel(matcher.group(GROUP3));
        		mn.setUrl(matcher.group(GROUP4));
        		if (matcher.group(GROUP5) != null
        				&& matcher.group(GROUP5).length() > 0){
        			mn.setSiegelstufe(matcher.group(GROUP5).charAt(0));
        		} else {
        			Logger.getLogger(this.getClass()).error(
        					"Konnte Siegelstufe nicht bestimmen für: "
        							+ mn.getId()
        							+ "\n Setze auf Stufe A (höchste).");
        			mn.setSiegelstufe('A');
        		}
        		addRoles(mn);

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
        			mn.setUrl(matcher.group(GROUP3));
        			mn.setTitel(matcher.group(GROUP5));
        			if (matcher.group(GROUP4) != null
        					&& matcher.group(GROUP4).length() > 0){
        				mn.setSiegelstufe(matcher.group(GROUP4).charAt(0));
        			} else {
        				Logger
        						.getLogger(this.getClass())
        						.error(
        								"Konnte Siegelstufe nicht bestimmen für: "
        										+ mn.getId()
        										+ "\n Setze auf Stufe A (höchste).");
        				mn.setSiegelstufe('A');
        			}
        			addRoles(mn);
        			result.add(mn);
        		}
        	}

        }
        return result;
    }

	private void addRoles(Massnahme mn) throws GSServiceException,
			XPathException {
		Node root = source.parseMassnahmenDocument(mn.getUrl());

		massnahmenVerantowrtlicheContext.setContextItem(new DocumentWrapper(
				root, mn.getUrl(), config));
		SequenceIterator iterator = massnahmenVerantwortlicheExp
				.iterator(massnahmenVerantowrtlicheContext);

		int foundItems = 0;

		while (true) {
			NodeInfo roleNode = (NodeInfo) iterator.next();
			if (roleNode == null){
				break;
			}
			foundItems++;
			String allRoles = roleNode.getStringValue();
			allRoles = allRoles.replaceAll("\n", "");

			if (allRoles != null && allRoles.length() > 0) {
				String[] rolesInit = allRoles.split(", *");
				for (String role : rolesInit) {
					role = trailingwhitespace.matcher(role).replaceFirst("");
					role = leadingwhitespace.matcher(role).replaceFirst("");
					String[] repairedRoles = repairBrokenRole(role);
					for (String repairedRole : repairedRoles) {
						switch (foundItems) {
						case 1:
							mn.addVerantwortlicheInitiierung(repairedRole);
							break;
						case 2:
							mn.addVerantwortlicheUmsetzung(repairedRole);
							break;
						default: break;
						}
					}
				}
			}
		}

	}

	/**
	 * Repair some roles with dashes etc. Split roles missing commas in
	 * GS-Catalogues etc.
	 * 
	 * @param role
	 * @return
	 */
	private String[] repairBrokenRole(String role) {
		String[] repairedRole = this.brokenRoles.get(role);
		if (repairedRole != null){
			return repairedRole;
		} else {
			return new String[] { role };
		}
	}

	private void setLebenszyklus(Massnahme mn, String lzString) {
		if (lzString.equals(Massnahme.LZ_STRING_Ausonderung)){
			mn.setLebenszyklus(Massnahme.LZ_AUSSONDERUNG);
		} else if (lzString.equals(Massnahme.LZ_STRING_Beschaffung)){
			mn.setLebenszyklus(Massnahme.LZ_BESCHAFFUNG);
		} else if (lzString.equals(Massnahme.LZ_STRING_Betrieb)){
			mn.setLebenszyklus(Massnahme.LZ_BETRIEB);
		} else if (lzString.equals(Massnahme.LZ_STRING_Notfall)){
			mn.setLebenszyklus(Massnahme.LZ_NOTFALL);
		} else if (lzString.equals(Massnahme.LZ_STRING_Planung)){
			mn.setLebenszyklus(Massnahme.LZ_PLANUNG);
		} else if (lzString.equals(Massnahme.LZ_STRING_Umsetzung)){
			mn.setLebenszyklus(Massnahme.LZ_UMSETZUNG);
		}
	}

	public InputStream getBausteinText(String url, String stand)
			throws GSServiceException {
		return source.getBausteinAsStream(url);
	}

	public InputStream getMassnahme(String url, String stand)
			throws GSServiceException {
		return source.getMassnahmeAsStream(url);
	}

	public InputStream getGefaehrdung(String url, String stand)
			throws GSServiceException {
		return source.getGefaehrdungAsStream(url);
	}

	public List<Gefaehrdung> getGefaehrdungen(String baustein)
			throws GSServiceException, IOException {
		ArrayList<Gefaehrdung> result = new ArrayList<Gefaehrdung>();
		try {
			List fromCache = getFromCache("gefaehrdungen_", baustein);
			for (Object object : fromCache) {
				result.add((Gefaehrdung) object);
			}
		} catch (Exception e) {
			// do nothing
		}
		if (result != null && result.size() > 0){
			return result;
		}
		try {
			Node root = source.parseBausteinDocument(baustein);
			getStand(baustein, root);
			gefaehrdungenContext.setContextItem(new DocumentWrapper(root,
					baustein, config));

			// return kategorie, id, titel, url
			SequenceIterator iterator = getGefaehrdungenExp
					.iterator(gefaehrdungenContext);
			Pattern pat = Pattern.compile("(.*)°(.*)°(.*)°(.*)");

			while (true) {
				NodeInfo gfNode = (NodeInfo) iterator.next();
				if (gfNode == null){
					break;
				}
				String found = gfNode.getStringValue();
				found = found.replaceAll("\n", "");
				found = found.replaceAll(".html", "");
				found = found.replaceAll(".htm", "");
				found = found.replaceAll("\\.\\./\\.\\./g/g\\d\\d/", "");
				found = found.replaceAll("../g/", "");

				Matcher matcher = pat.matcher(found);
				if (matcher.matches()) {
					Gefaehrdung gef = new Gefaehrdung();
					gef.setStand(stand);
					gef.setKategorie(Gefaehrdung.kategorieAsInt(matcher
							.group(1)));
					gef.setId(matcher.group(2));
					gef.setTitel(matcher.group(GROUP3));
					gef.setUrl(matcher.group(GROUP4));
					gef.setEncoding(getPatterns().getEncoding());

					result.add(gef);
				}
			}
		} catch (XPathException e) {
			Logger.getLogger(GSScraper.class).error(e);
			throw new GSServiceException(e);
		}
		writeToFile("gefaehrdungen_" + baustein, result);
		return result;

	}

	public String getCacheDir() {
		return cacheDir;
	}

	public void setCacheDir(String cacheDir) {
		this.cacheDir = cacheDir;
	}

}
