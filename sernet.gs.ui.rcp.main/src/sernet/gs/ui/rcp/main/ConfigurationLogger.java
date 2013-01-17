/*******************************************************************************
 * Copyright (c) 2012 Daniel Murygin.
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProduct;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.preferences.ConfigurationScope;

import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;

/**
 * ConfigurationLogger provides method to log system configuration properties:
 * <ul>
 *   <li>Java system properties</li>
 *   <li>verinice preferences</li>
 *   <li>RCP proxy preferences</li>
 * </ul>
 * 
 * You can enalbe and disable the logging in Log4j configuration file log4j.xml:
 * 
 * To enable logging add this snippet to the file:
 * 
 * <logger name="sernet.gs.ui.rcp.main.ConfigurationLogger">
 *     <level value="INFO" />
 * </logger>
 * 
 * To disable logging add this snippet:
 * 
 * <logger name="sernet.gs.ui.rcp.main.ConfigurationLogger">
 *     <level value="WARN" />
 * </logger> 
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
@SuppressWarnings("restriction")
public final class ConfigurationLogger {

    private static final Logger LOG = Logger.getLogger(ConfigurationLogger.class);
    
    private static final DateFormat DF = DateFormat.getDateTimeInstance(DateFormat.MEDIUM,DateFormat.MEDIUM);
    
    //org.eclipse.core.internal.net.ProxyType.PREF_PROXY_DATA_NODE = "proxyData" is not public
    public static final String PREF_PROXY_DATA_NODE = "proxyData";
    
    public static final String ECLIPSE_VMARGS = "eclipse.vmargs";
    public static final String SUN_JAVA_COMMAND = "sun.java.command";
    
    /**
     * Properties with a key from this list will not be logged by methods from this class
     * to prevend the logging of passwords or other sensibel data.
     */
    public static final String[] PREFERENCE_BLACKLIST = {
        PreferenceConstants.DB_PASS,
        PreferenceConstants.DB_USER,
        PreferenceConstants.GS_DB_PASS,
        PreferenceConstants.GS_DB_USER_DEFAULT,
        PreferenceConstants.GS_DB_USER,
        PreferenceConstants.VNSERVER_PASS,
        PreferenceConstants.VNSERVER_USER,
        PreferenceConstants.CRYPTO_TRUSTSTORE_FILE,
        PreferenceConstants.CRYPTO_KEYSTORE_FILE,
        PreferenceConstants.CRYPTO_TRUSTSTORE_PASSWORD,
        PreferenceConstants.CRYPTO_KEYSTORE_PASSWORD,
        ConfigurationLogger.SUN_JAVA_COMMAND};
    
    public static List<String> preferenceBlacklist = Arrays.asList(PREFERENCE_BLACKLIST);
    
    private ConfigurationLogger() {
        super();
    }
     
    /**
     * Logs a start message for verinice.
     * The title and the version are the first line 
     * from the about text shown in the help menu.
     */
    public static synchronized void logStart() {
        IProduct product = Platform.getProduct();
        String aboutText = product.getProperty("aboutText");
        String application = "verinice";
        if(aboutText!=null) {
            String lines[] = aboutText.split("\\r?\\n");
            if(lines!=null && lines.length>0) {
                application = lines[0];
            }
        }
        LOG.info("Starting " + application + ", " + DF.format(System.currentTimeMillis()));
    }
    
    /**
     * Logs are stop message
     */
    public static synchronized void logStop() {
        LOG.info("Application stopped, " + DF.format(System.currentTimeMillis()));
    }
    
    /**
     * Logs all java system properties sorted by its key.
     */
    public static void logSystemProperties() {
        LOG.info("System properties: ");
        Properties properties = System.getProperties();     
        Enumeration<Object> keys = properties.keys();
        List<String> keyList = new ArrayList<String>();
        while(keys.hasMoreElements()) {
            Object key = keys.nextElement();
            if(key instanceof String && !preferenceBlacklist.contains(key)) {
                keyList.add((String) key);
            }
        }
        Collections.sort(keyList);
        for (String key : keyList) { 
            if(ConfigurationLogger.ECLIPSE_VMARGS.equals(key)) {
                logVmArgsExceptD((String) properties.get(key));
            } else {
                LOG.info(key + ": " + properties.get(key));
            }
        }
        
    }
    
    private static void logVmArgsExceptD(String vmArgs) {
       StringTokenizer st = new StringTokenizer(vmArgs,"-");
       while(st.hasMoreTokens()) {
           String prop = st.nextToken();
           if(prop!=null) {
               prop = prop.trim();
               if(!prop.toLowerCase().startsWith("d")) {
                   LOG.info("-" + prop);
               }
           }
       }
        
    }

    /**
     * Logs verinice preferences the user set in the verinice preference dialog.
     */
    public static void logApplicationProperties() {     
        LOG.info("Application properties: ");
        Preferences preferences = Activator.getDefault().getPluginPreferences();
        logPreferences(preferences);
    }
    
    /**
     * Logs the proxy preferences. Since verinice uses the proxy settings from
     * RCP bundles "org.eclipse.ui.net" and "org.eclipse.core.net" the preferences a loaded from
     * "org.eclipse.core.net" preference node.
     */
    public static void logProxyPreferences() {   
        org.osgi.service.prefs.Preferences rootNode = ConfigurationScope.INSTANCE.getNode(org.eclipse.core.internal.net.Activator.ID);        
        logNodeProperties(rootNode);
    }
    
    /**
     * Logs all properties of a preference node and
     * calls logNodeProperties for all children of this node.
     * 
     * @param node A preference node
     */
    private static void logNodeProperties(org.osgi.service.prefs.Preferences node) {
        if(node==null) {
            return;
        }
        try {
           LOG.info(node.name() + " properties: ");   
           logProperties(node);
           String[] childrenNames = node.childrenNames();
           for (int i = 0; i < childrenNames.length; i++) {
               logNodeProperties(node.node(childrenNames[i]));
           }
        } catch (Exception t) {
            LOG.error("Error while logging preferences.", t);
        }
    }
    
    /**
     * @param preferences
     */
    private static void logProperties(org.osgi.service.prefs.Preferences preferences) {
        if(preferences==null) {
            return;
        }
        // load default and user keys in one set
        try {
            List<String> keyList = Arrays.asList(preferences.keys());
            Collections.sort(keyList);
            for (String key : keyList) {
                if(!preferenceBlacklist.contains(key)) {              
                    LOG.info(key + ": " + preferences.get(key,"<NO VALUE SET>"));
                }
            }
        } catch (Exception t) {
            LOG.error("Error while logging preferences.", t);
        }
    }


    /**
     * @param preferences
     */
    private static void logPreferences(Preferences preferences) {
        // load default and user keys in one set
        String[] keyArray = preferences.propertyNames();
        String[] defaultKeyArray = preferences.defaultPropertyNames();
        Set<String> keySet = new HashSet<String>();
        for (int i = 0; i < defaultKeyArray.length; i++) {
            keySet.add(defaultKeyArray[i]);         
        }
        for (int i = 0; i < keyArray.length; i++) {
            keySet.add(keyArray[i]);         
        } 
        List<String> keyList = new ArrayList<String>(keySet);
        Collections.sort(keyList);
        for (String key : keyList) {
            if(!preferenceBlacklist.contains(key)) {
                String defaultValue = preferences.getDefaultString(key);
                String value = preferences.getString(key);
                if(value==null) {
                    value = defaultValue;
                }          
                LOG.info(key + ": " + value);
            }
        }
    }
    
    

}
