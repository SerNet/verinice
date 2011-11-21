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
import java.util.List;
import java.util.Properties;

import sernet.hui.common.connect.Property;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.IRightsService;
import sernet.verinice.model.auth.Auth;
import sernet.verinice.model.auth.ConfigurationType;
import sernet.verinice.model.auth.Profiles;
import sernet.verinice.model.auth.Userprofile;
import sernet.verinice.model.common.configuration.Configuration;

/**
 *
 */
public class DummyRightsService implements IRightsService{
    
    Auth auth;
    List<Userprofile> userprofile;
    
    private IBaseDao<Configuration, Integer> configurationDao;
    
    private IBaseDao<Property, Integer> propertyDao;
    
    private IRemoteMessageSource messages;

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IRightsService#getConfiguration()
     */
    @Override
    public Auth getConfiguration() {
        if(auth == null){
            auth = new Auth();
            auth.setType(ConfigurationType.BLACKLIST);
        }
        return auth;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IRightsService#updateConfiguration(sernet.verinice.model.auth.Auth)
     */
    @Override
    public void updateConfiguration(Auth auth) {
        this.auth = auth;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IRightsService#getUserprofile(java.lang.String)
     */
    @Override
    public List<Userprofile> getUserprofile(String username) {
        if(userprofile == null){
            userprofile = new ArrayList<Userprofile>();
        }
        Userprofile profile = new Userprofile();
        profile.setLogin(username);
        userprofile.add(profile);
        return userprofile;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IRightsService#getProfiles()
     */
    @Override
    public Profiles getProfiles() {
        return null;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IRightsService#getUsernames()
     */
    @Override
    public List<String> getUsernames() {
        return new ArrayList<String>(0);
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IRightsService#getGroupnames()
     */
    @Override
    public List<String> getGroupnames() {
        return new ArrayList<String>(0);
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IRightsService#getMessage(java.lang.String)
     */
    @Override
    public String getMessage(String key) {
        return "DummyImplementation for Standalone-Mode";
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IRightsService#getAllMessages()
     */
    @Override
    public Properties getAllMessages() {
        // TODO Auto-generated method stub
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

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IRightsService#getUsernames(java.lang.String)
     */
    @Override
    public List<String> getUsernames(String username) {
        return null;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IRightsService#getGroupnames(java.lang.String)
     */
    @Override
    public List<String> getGroupnames(String username) {
        return null;
    }

}
