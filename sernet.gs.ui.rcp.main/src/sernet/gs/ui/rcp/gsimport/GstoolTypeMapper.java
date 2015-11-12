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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import sernet.gs.service.VeriniceCharset;
import sernet.gs.ui.rcp.main.CnAWorkspace;
import sernet.verinice.model.bsi.SonstIT;

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
    public static final String SUBTYPE_PROPERTIES_FILE_ENCODING = VeriniceCharset.CHARSET_ISO_8859_15.toString();
 
    /**
     * DEFAULT_TYPE_ID is used as element type id if no 
     * type id is found in mapping or no type id is set by user
     * 
     * DEFAULT_TYPE_ID must be a id of a huientity from SNCA.xml
     */
    public static final String DEFAULT_TYPE_ID = SonstIT.TYPE_ID;
    
	
	private static Map<String, String> gstoolTypes;
    private static Map<String, String> gstoolSubtypesMap;
    private static List<GstoolImportMappingElement> gstoolSubtypesList;


    private static Set<IGstoolImportMappingChangeListener> changeListenerSet;

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
	public static String getVeriniceTypeOrDefault(String gstoolType, String gstoolSubtype) {
	    String type;
        try {
            type = getVeriniceType(gstoolType, gstoolSubtype);
        } catch (GstoolTypeNotFoundException e) {
            LOG.error(e.getMessage() + ", using default verinice type id instead: " + DEFAULT_TYPE_ID);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Stacktrace: ", e);
            }
            type = DEFAULT_TYPE_ID;
        }
		return type;
	}
	
	/**
     * The verinice type-id for a GSTOOL type and subtype.
     * The verinice type is derived from the mapping defined in this class and in thr property files
     * TYPE_PROPERTIES_FILE and SUBTYPE_PROPERTIES_FILE.
     * 
     * If no type is found in the mapping GstoolTypeNotFoundException is thrown.
     * 
     * @param gstoolType A GSTOOL type
     * @param gstoolSubtype A GSTOOL subtype
     * @return The verinice type-id for a GSTOOL type and subtype
	 * @throws GstoolTypeNotFoundException If no type is found
	 */
	public static String getVeriniceType(String gstoolType, String gstoolSubtype) throws GstoolTypeNotFoundException {
	    String type = getGstoolTypes().get(gstoolType);
        if (type == null){
            type = getGstoolSubtypes().get(gstoolSubtype);
        }
        if(type == null || type.equals(GstoolImportMappingElement.UNKNOWN)){
            throw new GstoolTypeNotFoundException("Could not find a type id for GSTOOL type: " + gstoolType + " and sub type: " + gstoolSubtype);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("GSTOOL type: '" + gstoolType + "' and subtype: '" + gstoolSubtype + "', returning verinice type: " + type);
        }
        return type;
	}

    public static void addGstoolSubtypeToPropertyFile(GstoolImportMappingElement mappingEntry)  {
        Properties properties = readPropertyFile(SUBTYPE_PROPERTIES_FILE);
        properties.put(mappingEntry.getKey(), mappingEntry.getValue());
        writePropertyFile(properties, SUBTYPE_PROPERTIES_FILE);
        fireMappingAddedEvent(mappingEntry);
    }

    public static void removeGstoolSubtypeToPropertyFile(GstoolImportMappingElement oldElement) {
        Properties properties = readPropertyFile(SUBTYPE_PROPERTIES_FILE);
        properties.remove(oldElement.getKey());
        writePropertyFile(properties, SUBTYPE_PROPERTIES_FILE);
        fireMappingRemovedEvent(oldElement);
    }

    public static void editGstoolSubtypeToPropertyFile(GstoolImportMappingElement oldElement, GstoolImportMappingElement mappingEntry) {
        Properties properties = readPropertyFile(SUBTYPE_PROPERTIES_FILE);
        properties.remove(oldElement.getKey());
        properties.put(mappingEntry.getKey(), mappingEntry.getValue());
        writePropertyFile(properties, SUBTYPE_PROPERTIES_FILE);
        fireMappingChangedEvent(mappingEntry);
    }

    private static Map<String, String> getGstoolTypes() {
        if(gstoolTypes==null) {
            gstoolTypes = getGstoolTypesFromFile();
        }
        return gstoolTypes;
    }


    private static Map<String, String> getGstoolTypesFromFile() {
        Properties properties =  readPropertyFile(TYPE_PROPERTIES_FILE);
        Set<Object> keys = properties.keySet();
        gstoolTypes = new HashMap<>();
        for (Object key : keys) {
            gstoolTypes.put((String) key, (String) properties.get(key));
            if (LOG.isInfoEnabled()) {
                LOG.info("Type added: " + key + " = " + properties.get(key));
            }
        }
        return gstoolTypes;
    }

    public static Map<String, String> getGstoolSubtypes() {
        if (gstoolSubtypesMap == null) {
            gstoolSubtypesMap = getGstoolSubtypesFromFile();
        }
        return gstoolSubtypesMap;
    }

    public static List<GstoolImportMappingElement> getGstoolSubtypesAsList() {

        if (gstoolSubtypesList == null) {
            gstoolSubtypesList = getGstoolSubtypesListFromFile();
        }
        return gstoolSubtypesList;
    }

    private static void refreshGstoolSubTypes(Properties properties) {
        gstoolSubtypesMap = changePropertiesToMap(properties);
        gstoolSubtypesList = changePropertiesToList(properties);
    }

    private static List<GstoolImportMappingElement> changePropertiesToList(Properties properties) {
        ArrayList<GstoolImportMappingElement> gsToolSubtypesList = new ArrayList<>();
        Set<Object> keys = properties.keySet();
        for (Object key : keys) {
            gsToolSubtypesList.add(new GstoolImportMappingElement((String) key, (String) properties.get(key)));

            if (LOG.isInfoEnabled()) {
                LOG.info("Subtype added: " + key + " = " + properties.get(key));
            }
        }
        return gsToolSubtypesList;
    }

    private static Map<String, String> changePropertiesToMap(Properties properties) {
        HashMap<String, String> gstoolSubtypesMap = new HashMap<>();
        Set<Object> keys = properties.keySet();
        for (Object key : keys) {
            gstoolSubtypesMap.put((String) key, (String) properties.get(key));
            if (LOG.isInfoEnabled()) {
                LOG.info("Subtype added: " + key + " = " + properties.get(key));
            }
        }
        return gstoolSubtypesMap;
    }

    public static Map<String, String> getGstoolSubtypesFromFile(){
        gstoolSubtypesMap = new HashMap<>();
        Properties subProperties = readPropertyFile(SUBTYPE_PROPERTIES_FILE);
        return changePropertiesToMap(subProperties);
    }

    public static List<GstoolImportMappingElement> getGstoolSubtypesListFromFile() {
        Properties subProperties = readPropertyFile(SUBTYPE_PROPERTIES_FILE);
        return changePropertiesToList(subProperties);
    }

    private static void writePropertyFile(Properties properties, String filename) {
        File file = null;
        FileOutputStream fileOut = null;
        String filepath = getPropertyFolderPath() + File.separator + filename;
        try {
            if (new File(filepath).delete() && LOG.isDebugEnabled()) {
                LOG.debug("File deleted");
            }
            file = new File(filepath);
            fileOut = new FileOutputStream(file);
            OutputStreamWriter outWrite = new OutputStreamWriter(fileOut, SUBTYPE_PROPERTIES_FILE_ENCODING);
            properties.store(outWrite, "");
            refreshGstoolSubTypes(properties);
        } catch (IOException e) {
            LOG.error("Error wgile writing to property file: " + filename, e);
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(fileOut);
        }
    }
    
    private static Properties readPropertyFile(String fileName) {
        String fullPath = null;
        Properties properties = new Properties();
        try {  
            fullPath = getPropertyFolderPath() + File.separator + fileName;
            InputStreamReader reader = new InputStreamReader(new FileInputStream(fullPath), SUBTYPE_PROPERTIES_FILE_ENCODING);
            properties.load(reader);
            LOG.debug("Reading types from " + fullPath + "...");
        } catch (RuntimeException e) {
            LOG.error("Can not read properties from file " + fullPath, e);
            throw e;
        } catch (Exception e) {
            LOG.error("Can not read properties from file " + fullPath, e);
            throw new RuntimeException(e);
        }
        return properties;
    }

    private static String getPropertyFolderPath() {
        return CnAWorkspace.getInstance().getConfDir();
    }
    
    private static void fireMappingAddedEvent(GstoolImportMappingElement mappingEntry) {
        for (IGstoolImportMappingChangeListener listener : getChangeListenerSet()) {
            listener.mappingAdded(mappingEntry);
        }
    }

    private static void fireMappingChangedEvent(GstoolImportMappingElement mappingEntry) {
        for (IGstoolImportMappingChangeListener listener : getChangeListenerSet()) {
            listener.mappingChanged(mappingEntry);
        }
    }

    private static void fireMappingRemovedEvent(GstoolImportMappingElement mappingEntry) {
        for (IGstoolImportMappingChangeListener listener : getChangeListenerSet()) {
            listener.mappingRemoved(mappingEntry);
        }
    }

    public static void addChangeListener(IGstoolImportMappingChangeListener listener) {
        getChangeListenerSet().add(listener);
    }

    public static void removeChangeListener(IGstoolImportMappingChangeListener listener) {
        getChangeListenerSet().remove(listener);
    }

    private static Set<IGstoolImportMappingChangeListener> getChangeListenerSet() {
        if(changeListenerSet==null) {
            changeListenerSet = new HashSet<IGstoolImportMappingChangeListener>();
        }
        return changeListenerSet;
    }

}
