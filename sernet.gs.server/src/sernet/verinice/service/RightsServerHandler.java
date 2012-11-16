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
package sernet.verinice.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import sernet.verinice.interfaces.IRightsChangeListener;
import sernet.verinice.interfaces.IRightsServerHandler;
import sernet.verinice.interfaces.IRightsService;
import sernet.verinice.model.auth.Action;
import sernet.verinice.model.auth.Auth;
import sernet.verinice.model.auth.ConfigurationType;
import sernet.verinice.model.auth.Profile;
import sernet.verinice.model.auth.ProfileRef;
import sernet.verinice.model.auth.Profiles;
import sernet.verinice.model.auth.Userprofile;

/**
 *
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class RightsServerHandler implements IRightsServerHandler, IRightsChangeListener {

    private static final Logger LOG = Logger.getLogger(RightsServerHandler.class);
    
    private Map<String,Map<String, Action>> userActionMap;
    
    private Map<String, List<Userprofile>> userprofileMap;
    
    private Map<String, Profile> profileMap;
    
    private IRightsService rightsService;
    
    public RightsServerHandler() {
        super();
        XmlRightsService.addChangeListener(this);
    }

    public RightsServerHandler(IRightsService rightsService) {
        this();
        this.rightsService = rightsService;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IRightsServerHandler#isEnabled(java.lang.String, java.lang.String)
     */
    @Override
    public boolean isEnabled(String username, String actionId) {      
        boolean returnValue = isBlacklist(); 
        Map<String, Action> actionMap = getUserActionMap().get(username);
        if(actionMap!=null) {
            Action action = actionMap.get(actionId);
            returnValue = action!=null && isWhitelist() || 
                          action==null && isBlacklist();
        }
        return returnValue;
    }
    
    private Map<String,Map<String, Action>> getUserActionMap() {
        if(userActionMap==null) {
            userActionMap = loadUserActionMap();
        }
        return userActionMap;
    }
    
    private Map<String, Map<String, Action>> loadUserActionMap() {
        userActionMap = new HashMap<String,Map<String, Action>>();
        for (String user : getUserprofileMap().keySet()) {
            userActionMap.put(user, loadActionMap(user));
        }
        return userActionMap;
    }

    private Map<String, Action> loadActionMap(String username) {
        HashMap<String, Action> actionMap = new HashMap<String, Action>();
        List<Userprofile> userprofileList = getUserprofileMap().get(username);
        if(userprofileList!=null) {
            for (Userprofile userprofile : userprofileList) {  
                List<ProfileRef> profileList = userprofile.getProfileRef();
                if(profileList!=null) {
                    for (ProfileRef profileRef : profileList) {
                        Profile profileWithActions = getProfileMap().get(profileRef.getName());
                        if(profileWithActions!=null) {
                            List<Action> actionList = profileWithActions.getAction();
                            for (Action action : actionList) {
                                actionMap.put(action.getId(), action);            
                            }
                        } else {
                            LOG.error("Could not find profile " + profileRef.getName());
                        }
                    }
                }
            }
        }
        return actionMap;
    }
    
    public Map<String, List<Userprofile>> getUserprofileMap() {
        if(userprofileMap==null) {
            loadUserprofileMap();
        }
        return userprofileMap;
    }

    private Map<String, List<Userprofile>> loadUserprofileMap() {
        List<String> usernameList = getRightsService().getUsernames();
        if(usernameList!=null) {
            userprofileMap = new HashMap<String, List<Userprofile>>(usernameList.size());
            for (String name : usernameList) {
                userprofileMap.put(name, getRightsService().getUserprofile(name));
            }
        }
        return userprofileMap;
    }
    
    public Map<String, Profile> getProfileMap() {
        if(profileMap==null) {
            loadProfileMap();
        }
        return profileMap;
    }
    
    private Profiles loadProfileMap() {
        Profiles profiles = getRightsService().getProfiles();   
        profileMap = new HashMap<String, Profile>();
        for (Profile profile : profiles.getProfile()) {
            profileMap.put(profile.getName(), profile);
        }
        return profiles;
    }
    
    public boolean isBlacklist() {
        return ConfigurationType.BLACKLIST.equals(getRightsService().getConfiguration().getType());
    }
    
    public boolean isWhitelist() {
        return ConfigurationType.WHITELIST.equals(getRightsService().getConfiguration().getType());
    }

    public IRightsService getRightsService() {
        return rightsService;
    }

    public void setRightsService(IRightsService rightsService) {
        this.rightsService = rightsService;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IRightsChangeListener#configurationChanged(sernet.verinice.model.auth.Auth)
     */
    @Override
    public void configurationChanged(Auth auth) {
        discardData();       
    }

   
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IRightsServerHandler#discardData()
     */
    public void discardData() {
        profileMap=null;
        userActionMap=null;
        userprofileMap=null;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#finalize()
     */
    @Override
    protected void finalize() throws Throwable {
        XmlRightsService.removeChangeListener(this);
        super.finalize();
    }

}
