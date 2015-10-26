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
package sernet.gs.ui.rcp.gsimport;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;

import sernet.gs.ui.rcp.main.CnAWorkspace;
import sernet.verinice.model.bsi.Anwendung;
import sernet.verinice.model.bsi.Client;
import sernet.verinice.model.bsi.Gebaeude;
import sernet.verinice.model.bsi.ITVerbund;
import sernet.verinice.model.bsi.NetzKomponente;
import sernet.verinice.model.bsi.Person;
import sernet.verinice.model.bsi.Raum;
import sernet.verinice.model.bsi.Server;
import sernet.verinice.model.bsi.SonstIT;
import sernet.verinice.model.bsi.TelefonKomponente;

/**
 * This class provides access to the mapping from a GSTOOL
 * type or subtype id to a verinice element type id.
 * 
 * A verinice element type id is the id of a huientity in file SNCA.xml.
 * 
 * A default mapping is defined in this class in maps
 * DEFAULT_GSTOOL_TYPES and DEFAULT_GSTOOL_SUBTYPES.
 * Additional mapping are read from two property files.
 * 
 * @author Alexander Koderman <ak[at]sernet[dot]de>
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public abstract class GstoolTypeMapper {

    private static final Logger LOG = Logger.getLogger(GstoolTypeMapper.class);
    
    public static final String TYPE_PROPERTIES_FILE = "gstool-types.properties";
    public static final String SUBTYPE_PROPERTIES_FILE = "gstool-subtypes.properties";
 
    /**
     * DEFAULT_TYPE_ID is used as element type id if no 
     * type id is found in mapping or no type id is set by user
     * 
     * DEFAULT_TYPE_ID must be a id of a huientity from SNCA.xml
     */
    public static final String DEFAULT_TYPE_ID = SonstIT.TYPE_ID;
    
	public static final Map<String, String> DEFAULT_GSTOOL_TYPES = new HashMap<String, String>();
	public static final Map<String, String> DEFAULT_GSTOOL_SUBTYPES = new HashMap<String, String>();
	
	private static Map<String, String> gstoolTypes;
	private static Map<String, String> gstoolSubtypes;

	static {
        DEFAULT_GSTOOL_TYPES.put("Anwendung", Anwendung.TYPE_ID);
        DEFAULT_GSTOOL_TYPES.put("Gebäude", Gebaeude.TYPE_ID);
        DEFAULT_GSTOOL_TYPES.put("Informationsverbund", ITVerbund.TYPE_ID);
        DEFAULT_GSTOOL_TYPES.put("IT-Verbund", ITVerbund.TYPE_ID);
        DEFAULT_GSTOOL_TYPES.put("Mitarbeiter", Person.TYPE_ID);
        DEFAULT_GSTOOL_TYPES.put("Netz", NetzKomponente.TYPE_ID);
        DEFAULT_GSTOOL_TYPES.put("Raum", Raum.TYPE_ID);

        DEFAULT_GSTOOL_SUBTYPES.put("Active Directory", Anwendung.TYPE_ID);
        DEFAULT_GSTOOL_SUBTYPES.put("[allgemeine Anwendung]", Anwendung.TYPE_ID);
        DEFAULT_GSTOOL_SUBTYPES.put("[allgemeiner Client/PC]", Client.TYPE_ID);
        DEFAULT_GSTOOL_SUBTYPES.put("allgemeiner Client/PC]", Client.TYPE_ID);
        DEFAULT_GSTOOL_SUBTYPES.put("[allgemeiner Informationsverbund]", ITVerbund.TYPE_ID);
        DEFAULT_GSTOOL_SUBTYPES.put("[allgemeiner Laptop]", Client.TYPE_ID);
        DEFAULT_GSTOOL_SUBTYPES.put("[allgemeiner Laptop]", Client.TYPE_ID);
        DEFAULT_GSTOOL_SUBTYPES.put("[allgemeiner Raum]", Raum.TYPE_ID);
        DEFAULT_GSTOOL_SUBTYPES.put("[allgemeiner Server]", Server.TYPE_ID);
        DEFAULT_GSTOOL_SUBTYPES.put("[allgemeiner Server]", Server.TYPE_ID);
        DEFAULT_GSTOOL_SUBTYPES.put("Allgemeiner Verzeichnisdienst", Anwendung.TYPE_ID);
        DEFAULT_GSTOOL_SUBTYPES.put("[allgemeines Gebäude]", Gebaeude.TYPE_ID);
        DEFAULT_GSTOOL_SUBTYPES.put("[allgemeines Netz]", NetzKomponente.TYPE_ID);
        DEFAULT_GSTOOL_SUBTYPES.put("Anrufbeantworter", TelefonKomponente.TYPE_ID);
        DEFAULT_GSTOOL_SUBTYPES.put("Anrufbeantworter", TelefonKomponente.TYPE_ID);
        DEFAULT_GSTOOL_SUBTYPES.put("Apache Webserver", Anwendung.TYPE_ID);
        DEFAULT_GSTOOL_SUBTYPES.put("Besprechungs-, Veranstaltungs- und Schulungsräume", Raum.TYPE_ID);
        DEFAULT_GSTOOL_SUBTYPES.put("Büroraum", Raum.TYPE_ID);
        DEFAULT_GSTOOL_SUBTYPES.put("Client/PC unter DOS", Client.TYPE_ID);
        DEFAULT_GSTOOL_SUBTYPES.put("Client/PC unter DOS", Client.TYPE_ID);
        DEFAULT_GSTOOL_SUBTYPES.put("Client/PC unter Unix/Linux", Client.TYPE_ID);
        DEFAULT_GSTOOL_SUBTYPES.put("Client/PC unter Unix/Linux", Client.TYPE_ID);
        DEFAULT_GSTOOL_SUBTYPES.put("Client/PC unter Windows 2000", Client.TYPE_ID);
        DEFAULT_GSTOOL_SUBTYPES.put("Client/PC unter Windows 2000", Client.TYPE_ID);
        DEFAULT_GSTOOL_SUBTYPES.put("Client/PC unter Windows 9x", Client.TYPE_ID);
        DEFAULT_GSTOOL_SUBTYPES.put("Client/PC unter Windows 9x ** OBSOLET", Client.TYPE_ID);
        DEFAULT_GSTOOL_SUBTYPES.put("Client/PC unter Windows NT", Client.TYPE_ID);
        DEFAULT_GSTOOL_SUBTYPES.put("Client/PC unter Windows NT ** OBSOLET", Client.TYPE_ID);
        DEFAULT_GSTOOL_SUBTYPES.put("Client/PC unter Windows XP", Client.TYPE_ID);
        DEFAULT_GSTOOL_SUBTYPES.put("Client/PC unter Windows XP", Client.TYPE_ID);
        DEFAULT_GSTOOL_SUBTYPES.put("Client unter Windows Vista", Client.TYPE_ID);
        DEFAULT_GSTOOL_SUBTYPES.put("Datenbank", Anwendung.TYPE_ID);
        DEFAULT_GSTOOL_SUBTYPES.put("Datenträgerarchiv", Raum.TYPE_ID);
        DEFAULT_GSTOOL_SUBTYPES.put("Datenträgeraustausch", Anwendung.TYPE_ID);
        DEFAULT_GSTOOL_SUBTYPES.put("Drucker, Kopierer, Multifunktionsgeräte", SonstIT.TYPE_ID);
        DEFAULT_GSTOOL_SUBTYPES.put("Drucker, Kopierer, Multifunktionsgeräte", SonstIT.TYPE_ID);
        DEFAULT_GSTOOL_SUBTYPES.put("E-Mail", Anwendung.TYPE_ID);
        DEFAULT_GSTOOL_SUBTYPES.put("Exchange/Outlook 2000", Anwendung.TYPE_ID);
        DEFAULT_GSTOOL_SUBTYPES.put("Faxgerät", TelefonKomponente.TYPE_ID);
        DEFAULT_GSTOOL_SUBTYPES.put("Faxgerät", TelefonKomponente.TYPE_ID);
        DEFAULT_GSTOOL_SUBTYPES.put("Faxserver", Anwendung.TYPE_ID);
        DEFAULT_GSTOOL_SUBTYPES.put("Häuslicher Arbeitsplatz", Raum.TYPE_ID);
        DEFAULT_GSTOOL_SUBTYPES.put("heterogenes Netz", NetzKomponente.TYPE_ID);
        DEFAULT_GSTOOL_SUBTYPES.put("Internet Information Server", Anwendung.TYPE_ID);
        DEFAULT_GSTOOL_SUBTYPES.put("Internet-PC", Client.TYPE_ID);
        DEFAULT_GSTOOL_SUBTYPES.put("Internet-PC", Client.TYPE_ID);
        DEFAULT_GSTOOL_SUBTYPES.put("ISDN-Anbindung", NetzKomponente.TYPE_ID);
        DEFAULT_GSTOOL_SUBTYPES.put("Kommunikationsverbindung", NetzKomponente.TYPE_ID);
        DEFAULT_GSTOOL_SUBTYPES.put("Laptop unter DOS", Client.TYPE_ID);
        DEFAULT_GSTOOL_SUBTYPES.put("Laptop unter DOS", Client.TYPE_ID);
        DEFAULT_GSTOOL_SUBTYPES.put("Laptop unter Unix/Linux", Client.TYPE_ID);
        DEFAULT_GSTOOL_SUBTYPES.put("Laptop unter Unix/Linux", Client.TYPE_ID);
        DEFAULT_GSTOOL_SUBTYPES.put("Laptop unter Windows 2000", Client.TYPE_ID);
        DEFAULT_GSTOOL_SUBTYPES.put("Laptop unter Windows 2000", Client.TYPE_ID);
        DEFAULT_GSTOOL_SUBTYPES.put("Laptop unter Windows 9x", Client.TYPE_ID);
        DEFAULT_GSTOOL_SUBTYPES.put("Laptop unter Windows 9x ** OBSOLET", Client.TYPE_ID);
        DEFAULT_GSTOOL_SUBTYPES.put("Laptop unter Windows NT", Client.TYPE_ID);
        DEFAULT_GSTOOL_SUBTYPES.put("Laptop unter Windows NT ** OBSOLET", Client.TYPE_ID);
        DEFAULT_GSTOOL_SUBTYPES.put("Laptop unter Windows Vista", Client.TYPE_ID);
        DEFAULT_GSTOOL_SUBTYPES.put("Laptop unter Windows XP", Client.TYPE_ID);
        DEFAULT_GSTOOL_SUBTYPES.put("Laptop unter Windows XP", Client.TYPE_ID);
        DEFAULT_GSTOOL_SUBTYPES.put("Lotus Notes", Anwendung.TYPE_ID);
        DEFAULT_GSTOOL_SUBTYPES.put("[Mitarbeiterin/Mitarbeiter]", Person.TYPE_ID);
        DEFAULT_GSTOOL_SUBTYPES.put("Mobile Datenträger", Anwendung.TYPE_ID);
        DEFAULT_GSTOOL_SUBTYPES.put("Mobiler Arbeitsplatz", Raum.TYPE_ID);
        DEFAULT_GSTOOL_SUBTYPES.put("Mobiltelefon", TelefonKomponente.TYPE_ID);
        DEFAULT_GSTOOL_SUBTYPES.put("Mobiltelefon", TelefonKomponente.TYPE_ID);
        DEFAULT_GSTOOL_SUBTYPES.put("Modem", NetzKomponente.TYPE_ID);
        DEFAULT_GSTOOL_SUBTYPES.put("Netz- und Systemmanagement", NetzKomponente.TYPE_ID);
        DEFAULT_GSTOOL_SUBTYPES.put("Novell eDirectory", Anwendung.TYPE_ID);
        DEFAULT_GSTOOL_SUBTYPES.put("PDA", SonstIT.TYPE_ID);
        DEFAULT_GSTOOL_SUBTYPES.put("PDA", SonstIT.TYPE_ID);
        DEFAULT_GSTOOL_SUBTYPES.put("Raum für technische Infrastruktur", Raum.TYPE_ID);
        DEFAULT_GSTOOL_SUBTYPES.put("Rechenzentrum", Raum.TYPE_ID);
        DEFAULT_GSTOOL_SUBTYPES.put("Router/Switches", NetzKomponente.TYPE_ID);
        DEFAULT_GSTOOL_SUBTYPES.put("Router/Switches", SonstIT.TYPE_ID);
        DEFAULT_GSTOOL_SUBTYPES.put("Samba", Anwendung.TYPE_ID);
        DEFAULT_GSTOOL_SUBTYPES.put("SAP", Anwendung.TYPE_ID);
        DEFAULT_GSTOOL_SUBTYPES.put("Schutzschrank", Raum.TYPE_ID);
        DEFAULT_GSTOOL_SUBTYPES.put("Serverraum", Raum.TYPE_ID);
        DEFAULT_GSTOOL_SUBTYPES.put("Server unter Netware 3.x", Server.TYPE_ID);
        DEFAULT_GSTOOL_SUBTYPES.put("Server unter Netware 4.x", Server.TYPE_ID);
        DEFAULT_GSTOOL_SUBTYPES.put("Server unter Novell Netware 3.x ** OBSOLET", Server.TYPE_ID);
        DEFAULT_GSTOOL_SUBTYPES.put("Server unter Novell Netware 4.x", Server.TYPE_ID);
        DEFAULT_GSTOOL_SUBTYPES.put("Server unter Unix/Linux", Server.TYPE_ID);
        DEFAULT_GSTOOL_SUBTYPES.put("Server unter Unix/Linux", Server.TYPE_ID);
        DEFAULT_GSTOOL_SUBTYPES.put("Server unter Windows 2000", Server.TYPE_ID);
        DEFAULT_GSTOOL_SUBTYPES.put("Server unter Windows 2000", Server.TYPE_ID);
        DEFAULT_GSTOOL_SUBTYPES.put("Server unter Windows 2003", Server.TYPE_ID);
        DEFAULT_GSTOOL_SUBTYPES.put("Server unter Windows 2003", Server.TYPE_ID);
        DEFAULT_GSTOOL_SUBTYPES.put("Server unter Windows NT", Server.TYPE_ID);
        DEFAULT_GSTOOL_SUBTYPES.put("Server unter Windows NT", Server.TYPE_ID);
        DEFAULT_GSTOOL_SUBTYPES.put("Sicherheitsgateway (Firewall)", Server.TYPE_ID);
        DEFAULT_GSTOOL_SUBTYPES.put("Sicherheitsgateway (Firewall)", Server.TYPE_ID);
        DEFAULT_GSTOOL_SUBTYPES.put("Smartphone", SonstIT.TYPE_ID);
        DEFAULT_GSTOOL_SUBTYPES.put("Smartphone", SonstIT.TYPE_ID);
        DEFAULT_GSTOOL_SUBTYPES.put("Speichersysteme und Speichernetze", Server.TYPE_ID);
        DEFAULT_GSTOOL_SUBTYPES.put("Speichersysteme und Speichernetze", SonstIT.TYPE_ID);
        DEFAULT_GSTOOL_SUBTYPES.put("TK-Anlage", TelefonKomponente.TYPE_ID);
        DEFAULT_GSTOOL_SUBTYPES.put("TK-Anlage", TelefonKomponente.TYPE_ID);
        DEFAULT_GSTOOL_SUBTYPES.put("VoIP", NetzKomponente.TYPE_ID);
        DEFAULT_GSTOOL_SUBTYPES.put("VPN", NetzKomponente.TYPE_ID);
        DEFAULT_GSTOOL_SUBTYPES.put("WLAN", NetzKomponente.TYPE_ID);
        DEFAULT_GSTOOL_SUBTYPES.put("WWW-Dienst", Anwendung.TYPE_ID);
        DEFAULT_GSTOOL_SUBTYPES.put("zSeries-Mainframe", Server.TYPE_ID);
        DEFAULT_GSTOOL_SUBTYPES.put("zSeries-Mainframe", SonstIT.TYPE_ID);
	}

	/**
	 * The verinice type-id for a GSTOOL type and subtype.
	 * The verinice type is derived from the mapping defined in this class and in thr property files
	 * TYPE_PROPERTIES_FILE and SUBTYPE_PROPERTIES_FILE.
	 * 
	 * If no type is found in the mapping DEFAULT_TYPE_ID is returned.
	 * 
	 * @param gstoolType A GSTOOL type
	 * @param gstoolSubtype A GSTOOL subtype
	 * @return The verinice type-id for a GSTOOL type and subtype
	 */
	public static String getVeriniceType(String gstoolType, String gstoolSubtype) {
	    String type = getGstoolTypes().get(gstoolType);
		if (type == null){
			type = getGstoolSubtypes().get(gstoolSubtype);
		}
		if(type == null){
		    LOG.error("Could not find a type id for GSTOOL type: " + gstoolType + " and sub type: " + gstoolSubtype + ", using default verinice type id instead: " + DEFAULT_TYPE_ID);
		    type = DEFAULT_TYPE_ID;
		}
		if (LOG.isDebugEnabled()) {
            LOG.debug("GSTOOL type: '" + gstoolType + "' and subtype: '" + gstoolSubtype + "', returning verinice type: " + type);
        }
		return type;
	}

    public static Map<String, String> getGstoolTypes() {
        if(gstoolTypes==null) {
            gstoolTypes = createGstoolTypes();
        }
        return gstoolTypes;
    }


    private static Map<String, String> createGstoolTypes() {
        gstoolTypes = DEFAULT_GSTOOL_TYPES;    
        Properties properties =  readPropertyFile(TYPE_PROPERTIES_FILE);
        Set<Object> keys = properties.keySet();
        for (Object key : keys) {
            gstoolTypes.put((String) key, (String) properties.get(key));
            if (LOG.isInfoEnabled()) {
                LOG.info("Type added: " + key + " = " + properties.get(key));
            }
        }
        return gstoolTypes;
    }
    
    public static void addGstoolSubtypeToPropertyFile(Object[] mappingEntry) throws IOException {
        Properties properties = readPropertyFile(SUBTYPE_PROPERTIES_FILE);
        properties.put((String)mappingEntry[0], (String)mappingEntry[1]);
        writePropertyFile(properties, SUBTYPE_PROPERTIES_FILE);
    }

    public static Map<String, String> getGstoolSubtypes() {
        if(gstoolSubtypes==null) {
            gstoolSubtypes = createGstoolSubtypes();
        }
        return gstoolSubtypes;
    }

    private static Map<String, String> createGstoolSubtypes() {
        gstoolSubtypes = DEFAULT_GSTOOL_SUBTYPES;
        Properties subProperties = readPropertyFile(SUBTYPE_PROPERTIES_FILE);
        Set<Object> keys = subProperties.keySet();
        for (Object key : keys) {
            gstoolSubtypes.put((String) key, (String) subProperties.get(key));
            if (LOG.isInfoEnabled()) {
                LOG.info("Subtype added: " + key + " = " + subProperties.get(key));
            }
        }
        return gstoolSubtypes;
    }
    
    public static Map<String, String> getGsToolSubtypesFromFile(){
        return createGstoolSubtypes();
    }
    
    private static void writePropertyFile(Properties properties, String filename) throws IOException {
        File file = new File(getPropertyFolderPath() + File.separator + filename);
        FileOutputStream fileOut = new FileOutputStream(file);
        properties.store(fileOut, "");
        fileOut.close();
    }
    
    private static Properties readPropertyFile(String fileName) {
        String fullPath = null;
        Properties properties = new Properties();
        try {  
            fullPath = getPropertyFolderPath() + File.separator + fileName;
            properties.load(new FileReader(new File(fullPath)));
            LOG.debug("Reading types from " + fullPath + "...");
        } catch (Exception e) {
            LOG.warn("Can not load file " + fullPath + ". Using default values.");
        }
        return properties;
    }

    private static String getPropertyFolderPath() {
        return CnAWorkspace.getInstance().getConfDir();
    }
    
}
