/*******************************************************************************
 * Copyright (c) 2011 Daniel Murygin.
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
package sernet.verinice.service;

import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.log4j.Logger;
import org.springframework.core.io.Resource;

import sernet.verinice.interfaces.IRightsService;
import sernet.verinice.model.auth.Auth;
import sernet.verinice.model.auth.Profiles;
import sernet.verinice.model.auth.Userprofile;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 *
 */
public class XmlRightsService implements IRightsService {

    private final Logger log = Logger.getLogger(XmlRightsService.class);
    
    Resource authConfiguration;
    
    Resource authConfigurationSchema;
    
    Auth auth;

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IRightsService#getConfiguration()
     */
    @Override
    public Auth getConfiguration() {
        if(auth==null) {
            auth = loadConfiguration();
        }      
        return auth;
    }

    /**
     * @return
     */
    private Auth loadConfiguration() {
        try {
            JAXBContext context = JAXBContext.newInstance(Auth.class);
            Unmarshaller um = context.createUnmarshaller();                 
            SchemaFactory sf = SchemaFactory.newInstance(javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = sf.newSchema(getAuthConfigurationSchema().getURL());
            um.setSchema(schema);         
            Auth auth = (Auth) um.unmarshal(getAuthConfiguration().getInputStream());
            return auth;
        } catch (RuntimeException e) {
            log.error("Error while reading verinice authorization definition from file: " + getAuthConfiguration().getFilename(), e);
            throw e;
        } catch (Exception e) {
            log.error("Error while reading verinice authorization definition from file: " + getAuthConfiguration().getFilename(), e);
            throw new RuntimeException(e);
        }
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IRightsService#getUserprofile(java.lang.String)
     */
    @Override
    public Userprofile getUserprofile(String username) {
        List<Userprofile> userprofileList = getConfiguration().getUserprofiles().getUserprofile();
        for (Userprofile userprofile : userprofileList) {
            if(userprofile.getLogin().equals(username)) {
                return userprofile;
            }
        }
        return null;
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IRightsService#getProfiles()
     */
    @Override
    public Profiles getProfiles() {
        return getConfiguration().getProfiles();
    }

    /**
     * @return the authConfiguration
     */
    public Resource getAuthConfiguration() {
        return authConfiguration;
    }

    /**
     * @param authConfiguration the authConfiguration to set
     */
    public void setAuthConfiguration(Resource authConfiguration) {
        this.authConfiguration = authConfiguration;
    }

    /**
     * @return the authConfigurationSchema
     */
    public Resource getAuthConfigurationSchema() {
        return authConfigurationSchema;
    }

    /**
     * @param authConfigurationSchema the authConfigurationSchema to set
     */
    public void setAuthConfigurationSchema(Resource authConfigurationSchema) {
        this.authConfigurationSchema = authConfigurationSchema;
    }
    
   

}
