/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak@sernet.de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details.
 *     You should have received a copy of the GNU General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Alexander Koderman <ak@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.hui.common.connect;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.springframework.core.io.Resource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import sernet.hui.common.VeriniceContext;
import sernet.hui.common.multiselectionlist.IMLPropertyOption;
import sernet.hui.common.rules.IFillRule;
import sernet.hui.common.rules.NotEmpty;
import sernet.hui.common.rules.RuleFactory;
import sernet.snutils.DBException;

/**
 * Parses XML file with defined systemproperties and creates appropriate
 * <code>DocProperty </code> objects with possible <code>DocValue </code>
 * entries for each.
 * 
 * 
 */
public class HUITypeFactory {
	private static final Logger log = Logger.getLogger(HUITypeFactory.class);
	
	private static Document doc;

	private Map<String, EntityType> allEntities = null;

	private Set<String> allDependecies = new HashSet<String>();

	// last-modified fields for local file or HTTP:
	private static Date fileDate;
	private static String lastModified;

	public static HUITypeFactory createInstance(URL xmlUrl) throws DBException {
		return new HUITypeFactory(xmlUrl);
	}
	
	public static HUITypeFactory getInstance()
	{
		return (HUITypeFactory) VeriniceContext.get(VeriniceContext.HUI_TYPE_FACTORY);
	}
	 
	public HUITypeFactory(Resource resource) throws DBException, IOException
	{
		this(resource.getURL());
	}

	/**
	 * Create new validating parser with schema support.
	 * 
	 * @throws DBException
	 */
	private HUITypeFactory(URL xmlFile) throws DBException {
		if (xmlFile == null)
			throw new DBException(
					"Pfad für XML Systemdefinition nicht initialisiert. "
							+ "Config File korrekt?");
		if (xmlFile.getProtocol().equals("http")
				|| xmlFile.getProtocol().equals("ftp"))
			try {
				xmlFile = new URL(xmlFile.toString() + "?nocache=" + Math.random());
			} catch (MalformedURLException e) {
				throw new RuntimeException(e);
			}
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		factory.setValidating(true);
		DocumentBuilder parser = null;
		
		// uncomment this to enable validating of the schema:
		try {
			factory.setFeature("http://xml.org/sax/features/validation", true);
			factory.setFeature(
					"http://apache.org/xml/features/validation/schema", true);
			
			factory.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaLanguage",
					"http://www.w3.org/2001/XMLSchema");
			factory.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaSource",
					getClass().getResource("/hitro.xsd").toString());
			
			parser = factory.newDocumentBuilder();
					
		} catch (ParserConfigurationException e) {
			log.error("Unrecognized parser feature.", e);
			throw new RuntimeException(e);
		}
		
		try {
			log.debug("Getting XML property definition from " + xmlFile);
			doc = parser.parse(xmlFile.openStream());
			readAllEntities();
			
		} catch (IOException ie) {
			log.error(ie);
			throw new DBException("Die XML Datei mit der Definition der Formularfelder konnte nicht " +
							"geladen werden! Bitte Pfad und Erreichbarkeit laut Konfigurationsfile" +
							" überprüfen.", ie);
		} catch (SAXException e) {
			throw new DBException("Die XML Datei mit der Definition der Formularfelder " +
					"ist defekt!", e);
		}
	}

	private void readAllEntities() {
		this.allEntities = new HashMap<String, EntityType>();
		NodeList entities = doc.getElementsByTagName("huientity");
		for(int i=0; i < entities.getLength(); ++i) {
			
			Element entityEl = (Element) entities.item(i);
			EntityType entityObj = new EntityType();
			entityObj.setId(entityEl.getAttribute("id"));
			entityObj.setName(entityEl.getAttribute("name"));
			this.allEntities.put(entityEl.getAttribute("id"), entityObj);
			readChildElements(entityObj, null);
		}
	}
	
	public EntityType getEntityType(String id) {
		return this.allEntities.get(id);
	}
	
	public Collection<EntityType> getAllEntityTypes() {
		return this.allEntities.values();
		
	}
	
	public List<PropertyType> getURLPropertyTypes() {
		List<PropertyType> result = new ArrayList<PropertyType>();
		Set<Entry<String, EntityType>> entrySet = allEntities.entrySet();
		for (Entry<String, EntityType> entry : entrySet) {
			List<PropertyType> types = entry.getValue().getPropertyTypes();
			for (PropertyType propertyType : types) {
				if (propertyType.isURL())
					result.add(propertyType);
			}
		}
		return result;
	}

	/**
	 * Check if file has changed (or was never read). Timestamp for local files, for
	 * remote files the server will only deliver the file if it has changed,
	 * because we set the modified-since property on the request.
	 * 
	 * @param xmlFile the URI of the xml file
	 */
	private static boolean fileChanged(String xmlFile) {
		if (xmlFile.matches("^http.*")) {
			try {
				URL xml = new URL(xmlFile);
				HttpURLConnection.setFollowRedirects(true);
				HttpURLConnection connection = (HttpURLConnection) xml.openConnection();
				if (lastModified != null) {
					  connection.addRequestProperty("If-Modified-Since",lastModified);
				}
				connection.connect();
				
				if (connection.getResponseCode() == HttpURLConnection.HTTP_NOT_MODIFIED) {
					  connection.disconnect();
					  log.debug("Remote PropertyType file not modified.");
					  return false;
				}
				lastModified = connection.getHeaderField("Last-Modified");
				connection.disconnect();
				log.debug("CHANGED: Remote PropertyType file modified.");
				return true;
			} catch (MalformedURLException e) {
				log.error(e);
				log.error(e);
			} catch (ProtocolException e) {
				log.error(e);
			} catch (UnsupportedEncodingException e) {
				log.error(e);
			} catch (IOException e) {
				log.error(e);
			}
		} else {
			// check local :
			File xml = new File(xmlFile);
			Date fileNow = new Date(xml.lastModified());
			if (fileDate == null || fileNow.after(fileDate)) {
				log.debug("CHANGED: Local PropertyType file was modified.");
				fileDate = fileNow;
				return true;
			}  
			log.debug("Local PropertyType file was not modified.");
			return false;
		}
		return true;
	}

	
	private void readChildElements(EntityType entityType, PropertyGroup propGroup) {
		NodeList nodes = null;
		if (propGroup != null) {
			Element groupEl = doc.getElementById(propGroup.getId());
			nodes = groupEl.getChildNodes();
		}
		else { 
			Element entityEl = doc.getElementById(entityType.getId());
			if (entityEl == null)
				throw new RuntimeException("EntityType not found in XML definition: "+ entityType.getId());
			nodes = entityEl.getChildNodes();
		}
		
		allProperties: for (int i = 0; i < nodes.getLength(); ++i) {
			if (!(nodes.item(i) instanceof Element))
				continue allProperties;
			Element child = (Element) nodes.item(i);
			if (child.getTagName().equals("huiproperty")) {
				PropertyType type = readPropertyType(child.getAttribute("id"));
				if (propGroup != null)
					propGroup.addPropertyType(type);
				else
					entityType.addPropertyType(type);
			}
			else if (child.getTagName().equals("huipropertygroup")) {
				PropertyGroup group = readPropertyGroup(child.getAttribute("id"));
				entityType.addPropertyGroup(group);
				readChildElements(entityType, group);
			}
		}
	}
	
	public PropertyType readPropertyType(String id) {
		Element prop = doc.getElementById(id);
		if (prop == null)
			return null;
			
		PropertyType propObj = new PropertyType();
		propObj.setId(id);
		propObj.setName(prop.getAttribute("name"));
		propObj.setTooltiptext(prop.getAttribute("tooltip"));
		propObj.setInputType(prop.getAttribute("inputtype"));
		propObj.setRequired(prop.getAttribute("required").equals("true"));
		propObj.setInitialFocus(prop.getAttribute("focus").equals("true"));
		propObj.setEditable(prop.getAttribute("editable").equals("true"));
		propObj.setVisible(prop.getAttribute("visible").equals("true"));
		propObj.setURL(prop.getAttribute("isURL").equals("true"));
		propObj.setReferencedEntityType(readReferencedEntityId(prop));
		propObj.setPredefinedValues(this.getOptionsForPropertyType(id));
		propObj.setDependencies(readDependencies(prop));

		// the shortcut to set a "NotEmpty" validator:
		if (prop.getAttribute("required").equals("true"))
			propObj.addValidator(new NotEmpty());
		
		propObj.setDefaultRule(readDefaultRule(prop));
		
		return propObj;
	}
	
	

	private String readReferencedEntityId(Element prop) {
		NodeList list = prop.getElementsByTagName("references");
		for (int i=0; i < list.getLength(); ++i) {
			Element referencesElmt = (Element) list.item(i);
			return referencesElmt.getAttribute("entitytype"); 
		}
		return "";
	}

	public PropertyGroup readPropertyGroup(String id) {
		Element group = doc.getElementById(id);
		if (group == null)
			return null;

		PropertyGroup groupObj = new PropertyGroup();
		groupObj.setId(group.getAttribute("id"));
		groupObj.setName(group.getAttribute("name"));
		groupObj.setDependencies(readDependencies(group));
		return groupObj;
	}

	/**
	 * @param prop
	 * @return
	 */
	private HashSet<String> readDependencies(Element prop) {
		HashSet<String> depends = new HashSet<String>();
		NodeList dependList = prop.getElementsByTagName("depends");
		for (int i = 0; i < dependList.getLength(); ++i) {
			Element depend = (Element) dependList.item(i);
			depends.add(depend.getAttribute("option"));
		}
		this.allDependecies.addAll(depends);
		return depends;
	}
	
	private IFillRule readDefaultRule(Element prop) {
		IFillRule rule = null;
		NodeList list = prop.getElementsByTagName("defaultRule");
		for (int i=0; i < list.getLength(); ++i) {
			
			Element ruleElmt = (Element) list.item(i);
			String className = ruleElmt.getAttribute("class");
			rule = RuleFactory.getDefaultRule(className);
			
			if (rule != null) {
				String[] params = readRuleParams(ruleElmt, rule);
				rule.init(params);
				
			}
		}
		return rule;
	}
	
	
	private String[] readRuleParams(Element ruleElmt, IFillRule rule) {
		NodeList nodes = ruleElmt.getElementsByTagName("param");
		String[] params = new String[nodes.getLength()];
		for (int i = 0; i < nodes.getLength(); i++) {
			Element e = (Element) nodes.item(i);
			params[i] = nodes.item(i).getTextContent();
			
			//FIXME read rules from file
		}
		return params;
	}

	public ArrayList getOptionsForPropertyType(String id) {
		Element prop = doc.getElementById(id);
		NodeList values = prop.getElementsByTagName("option");
		ArrayList possibleValues = new ArrayList(values.getLength());
		for (int i = 0; i < values.getLength(); ++i) {
			Element value = (Element) values.item(i);
			PropertyOption dv = new PropertyOption();
			dv.setId(value.getAttribute("id"));
			dv.setName(value.getAttribute("name"));
			possibleValues.add(dv);
		}
		return possibleValues;
	}

	public PropertyOption getOptionById(String valueId) {
		Element value = doc.getElementById(valueId);
		if (value == null)
			return null;
		PropertyOption dv = new PropertyOption();
		dv.setId(value.getAttribute("id"));
		dv.setName(value.getAttribute("name"));
		return dv;
	}

	public List<PropertyType> getAllPropertyTypes(String entityTypeID) {
		return allEntities.get(entityTypeID).getPropertyTypes();
	}

	public PropertyType getPropertyType(String entityTypeID, String id) {
		return allEntities.get(entityTypeID).getPropertyType(id);
	}

	public boolean isDependency(IMLPropertyOption opt) {
		return allDependecies.contains(opt.getId());
	}
}
