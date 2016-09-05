/*******************************************************************************
 * Copyright (c) 2016 Sebastian Hagedorn.
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
 *     Sebastian Hagedorn sh[at]sernet.de - initial API and implementation
 ******************************************************************************/
package sernet.verinice.service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import sernet.gs.service.NumericStringComparator;
import sernet.verinice.interfaces.updatenews.IUpdateNewsService;

/**
 * @author Sebastian Hagedorn sh[at]sernet.de
 *
 */
public class UpdateNewsService implements IUpdateNewsService {
    
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    
    private static final Logger LOG = Logger.getLogger(UpdateNewsService.class);

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.updatenews.IUpdateNewsService#getCurrentInstalledVersion()
     */
    @Override
    public String getCurrentInstalledVersion() {
        try {
            Bundle bundle = Platform.getBundle("sernet.gs.ui.rcp.main.feature");
            if (bundle == null) {
                LOG.warn("verinice server bundle is not available. Assuming it is started separately."); //$NON-NLS-1$
            } else if (bundle.getState() == Bundle.INSTALLED || bundle.getState() == Bundle.RESOLVED) {
                LOG.debug("Manually starting GS rcp.main.feature"); //$NON-NLS-1$
                bundle.start();
            }
            URL fileURL = bundle.getEntry("/oc.product");
            
            if(fileURL == null){
                throw new FileNotFoundException("Couldnt load oc.product");
            }
            
            java.io.File file = null;
            file = new java.io.File(FileLocator.resolve(fileURL).toURI());
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder parser = factory.newDocumentBuilder();
            Document document = parser.parse(file);
            NodeList nodeList = document.getElementsByTagName("product"); 
            for (int i = 0; i < nodeList.getLength(); i++){
                Node node = nodeList.item(i);
                NamedNodeMap namedNodeMap = node.getAttributes();
                for (int j = 0; j < namedNodeMap.getLength(); j++){
                    if ("version".equals(namedNodeMap.item(j).getNodeName())){
                        return namedNodeMap.item(j).getNodeValue();
                    }
                }
            };

//        } catch (java.net.URISyntaxException e1) {
//            e1.printStackTrace();
//        } catch (java.io.IOException e1) {
//            e1.printStackTrace();
//        } catch (ParserConfigurationException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        } catch (SAXException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
        } catch (Exception e){
            LOG.error("Unable to determine version of running client:\t", e);
        }

        return null;
    }
    
    public boolean isUpdateNecessary(){
        NumericStringComparator ncs = new NumericStringComparator();
        String installedVersion = getCurrentInstalledVersion();
        String availableVersion = getCurrentNewsVersion();
        // is availableVersion > installedVersion
        return ncs.compare(installedVersion, availableVersion) == 1;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.updatenews.IUpdateNewsService#getCurrentNewsMessage()
     */
    @Override
    public String getCurrentNewsMessage(Locale locale) {
        return parseNewsEntry(getLatestNewsFromRepository()).getMessage(locale);
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.updatenews.IUpdateNewsService#getCurrentNewsVersion()
     */
    @Override
    public String getCurrentNewsVersion() {
        return parseNewsEntry(getLatestNewsFromRepository()).getVersion();
    }
    
    
    @Override
    public URL getUpdateSite(){
        try {
            return new URL(parseNewsEntry(getLatestNewsFromRepository()).getUpdateSite());
        } catch (MalformedURLException e) {
            LOG.error("URL of updatesite to contact was not formed well", e);
        }
        return null;
    }
    
    private UpdateNewsMessageEntry parseNewsEntry(String newsEntry){
        return gson.fromJson(newsEntry, UpdateNewsMessageEntry.class);
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.updatenews.IUpdateNewsService#getNewsRepository()
     */
    @Override
    public String getLatestNewsFromRepository() {
        try {
            URL repositoryURL = new URL("http://localhost:8081/verinicenews/news.txt");
            InputStream in = repositoryURL.openStream();
            return IOUtils.toString(in);
        } catch (IOException e) {
            LOG.error("Error reading the update news", e);
        } 
        return "";
        
    }
    
    
    /**
     * representation of a update news message, which needs to be
     * formatted like this (in json):
     * 
     * {
     *    "version" : "1.13.0",
     *    "message" : "<h1> Update News Headline </h1>Some text
     *              that describes the Update and informs <p> the user</p>",
     *    "message_de" : "<h1> Update News Überschrift </h1>Text
     *              der das Update beschreibt und den 
     *              <p>Benutzer informiert</p>",
     *    "updatesite" : "http://path_to/updateSite"          
     *           
     * }
     *
     * @author Sebastian Hagedorn sh[at]sernet.de
     *
     */
    static class UpdateNewsMessageEntry {
        private String version;
        private String message;
        private String message_de;
        private String updatesite;
        
        public String getVersion(){return version;}
        public String getMessage(Locale locale){
            if(Locale.GERMAN.equals(locale) || Locale.GERMANY.equals(locale)){
                return message_de;
            } else {
                return message;
            }

        }
        public String getMessageDE(){return message_de;}
        public String getUpdateSite(){return updatesite;}
    }

    


}
