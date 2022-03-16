/*******************************************************************************
 * Copyright (c) 2011 Sebastian Hagedorn <sh@sernet.de>.
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
 *     Sebastian Hagedorn <sh@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.log4j.Logger;
import org.springframework.core.io.Resource;

import sernet.hui.common.connect.Property;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.IRightsChangeListener;
import sernet.verinice.interfaces.IRightsService;
import sernet.verinice.model.auth.Auth;
import sernet.verinice.model.auth.Profiles;
import sernet.verinice.model.auth.Userprofile;
import sernet.verinice.model.common.configuration.Configuration;

public class DummyRightsService implements IRightsService {

    private final Logger log = Logger.getLogger(DummyRightsService.class);

    private Auth auth;

    private IBaseDao<Configuration, Integer> configurationDao;

    private IBaseDao<Property, Integer> propertyDao;

    private IRemoteMessageSource messages;

    private Resource authConfigurationDefault;

    private Resource authConfigurationSchema;

    private Schema schema;

    private JAXBContext context;

    /*
     * @see sernet.verinice.interfaces.IRightsService#getConfiguration()
     */
    @Override
    public Auth getConfiguration() {
        if (auth == null) {
            auth = loadConfiguration();
        }
        return auth;
    }

    /*
     * @see
     * sernet.verinice.interfaces.IRightsService#updateConfiguration(sernet.
     * verinice.model.auth.Auth)
     */
    @Override
    public void updateConfiguration(Auth auth) {
        this.auth = auth;
    }

    @Override
    public Map<String, List<Userprofile>> getUserprofileMap(Set<String> usernames) {
        Map<String, List<Userprofile>> result = new HashMap<>(usernames.size());
        Map<String, List<String>> roleMap = getRoleMap(usernames);

        for (String username : usernames) {
            List<String> roleList = roleMap.getOrDefault(username, new ArrayList<>());
            roleList.add(username);
            List<Userprofile> userprofileList = new ArrayList<>(1);
            List<Userprofile> allUserprofileList = getConfiguration().getUserprofiles()
                    .getUserprofile();
            for (Userprofile userprofile : allUserprofileList) {
                if (roleList.contains(userprofile.getLogin())) {
                    userprofileList.add(userprofile);
                }
            }
            result.put(username, userprofileList);
        }
        return result;
    }

    /*
     * @see sernet.verinice.interfaces.IRightsService#getProfiles()
     */
    @Override
    public Profiles getProfiles() {
        return getConfiguration().getProfiles();
    }

    /*
     * @see sernet.verinice.interfaces.IRightsService#getUsernames()
     */
    @Override
    public List<String> getUsernames() {
        return new ArrayList<>(0);
    }

    /*
     * @see sernet.verinice.interfaces.IRightsService#getGroupnames()
     */
    @Override
    public List<String> getGroupnames() {
        return new ArrayList<>(0);
    }

    /*
     * @see
     * sernet.verinice.interfaces.IRightsService#getMessage(java.lang.String)
     */
    @Override
    public String getMessage(String key) {
        return "DummyImplementation for Standalone-Mode";
    }

    /*
     * @see sernet.verinice.interfaces.IRightsService#getAllMessages()
     */
    @Override
    public Properties getAllMessages() {
        return null;
    }

    public IBaseDao<Configuration, Integer> getConfigurationDao() {
        return configurationDao;
    }

    public void setConfigurationDao(IBaseDao<Configuration, Integer> configurationDao) {
        this.configurationDao = configurationDao;
    }

    public IBaseDao<Property, Integer> getPropertyDao() {
        return propertyDao;
    }

    public void setPropertyDao(IBaseDao<Property, Integer> propertyDao) {
        this.propertyDao = propertyDao;
    }

    public IRemoteMessageSource getMessages() {
        return messages;
    }

    public void setMessages(IRemoteMessageSource messages) {
        this.messages = messages;
    }

    /*
     * @see
     * sernet.verinice.interfaces.IRightsService#getUsernames(java.lang.String)
     */
    @Override
    public List<String> getUsernames(String username) {
        return null;
    }

    /*
     * @see
     * sernet.verinice.interfaces.IRightsService#getGroupnames(java.lang.String)
     */
    @Override
    public List<String> getGroupnames(String username) {
        return null;
    }

    public Resource getAuthConfigurationDefault() {
        return authConfigurationDefault;
    }

    public void setAuthConfigurationDefault(Resource authConfigurationDefault) {
        this.authConfigurationDefault = authConfigurationDefault;
    }

    /**
     * Loads the configuration by merging the default and installation
     * configuration
     * 
     * @return the authorization configuration
     */
    private Auth loadConfiguration() {
        try {
            Unmarshaller um = getContext().createUnmarshaller();
            um.setSchema(getSchema());

            // read default configuration
            auth = (Auth) um.unmarshal(getAuthConfigurationDefault().getInputStream());
            return auth;
        } catch (RuntimeException e) {
            log.error("Error while reading verinice authorization definition from file: "
                    + getAuthConfigurationDefault().getFilename(), e);
            throw e;
        } catch (Exception e) {
            log.error("Error while reading verinice authorization definition from file: "
                    + getAuthConfigurationDefault().getFilename(), e);
            throw new RuntimeException(e);
        }
    }

    private Schema getSchema() {
        if (schema == null) {
            SchemaFactory sf = SchemaFactory
                    .newInstance(javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI);
            try {
                schema = sf.newSchema(getAuthConfigurationSchema().getURL());
            } catch (Exception e) {
                log.error("Error while creating schema.", e);
            }
        }
        return schema;
    }

    public JAXBContext getContext() {
        if (context == null) {
            try {
                context = JAXBContext.newInstance(Auth.class);
            } catch (JAXBException e) {
                log.error("Error while creating JAXB context.", e);
            }
        }
        return context;
    }

    public void setContext(JAXBContext context) {
        this.context = context;
    }

    public void setSchema(Schema schema) {
        this.schema = schema;
    }

    public Resource getAuthConfigurationSchema() {
        return authConfigurationSchema;
    }

    public void setAuthConfigurationSchema(Resource authConfigurationSchema) {
        this.authConfigurationSchema = authConfigurationSchema;
    }

    private Map<String, List<String>> getRoleMap(Collection<String> usernames) {
        String hql = "select roleprops.propertyValue, props.propertyValue from Configuration as conf " //$NON-NLS-1$
                + "inner join conf.entity as entity "
                + "inner join entity.typedPropertyLists as propertyList "
                + "inner join propertyList.properties as props "
                + "inner join entity.typedPropertyLists as propertyList2 "
                + "inner join propertyList2.properties as roleprops "
                + "where props.propertyType = :type "
                + "and cast(props.propertyValue as string) in (:values) "
                + "and roleprops.propertyType = :rtype";
        String[] paramNames = new String[] { "type", "values", "rtype" };
        Object[] params = new Object[] { Configuration.PROP_USERNAME, usernames,
                Configuration.PROP_ROLES };
        Map<String, List<String>> userToRoles = new HashMap<>();

        List<Object[]> result = getConfigurationDao().findByQuery(hql, paramNames, params);
        for (Object[] object : result) {
            String role = (String) object[0];
            String user = (String) object[1];
            userToRoles.computeIfAbsent(user, u -> new ArrayList<String>()).add(role);
        }
        return userToRoles;
    }

    @Override
    public void addChangeListener(IRightsChangeListener rightsChangeListener) {

    }

    @Override
    public void removeChangeListener(IRightsChangeListener rightsChangeListener) {

    }

}
