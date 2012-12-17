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

import java.util.HashMap;
import java.util.Map;

import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.model.auth.Action;
import sernet.verinice.model.auth.Auth;
import sernet.verinice.model.auth.ConfigurationType;
import sernet.verinice.model.auth.Profile;
import sernet.verinice.model.auth.Profiles;
import sernet.verinice.model.auth.Userprofile;
import sernet.verinice.model.auth.Userprofiles;

/**
 * Static helper methods to handle authorization configurations.
 * 
 * This class is used in a multi threaded web server environment.
 * Never ever define any member vars. in this class.
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public abstract class AuthHelper {
    
    /**
     * Inverts a authorization configuration
     * from <i>blacklist</i> to <i>whitelist</i>
     * or vice versa.
     * 
     * @param Auth a authorization configuration
     * @return The authorization configuration inverted
     */
    public static Auth invert(final Auth auth) {
        Auth result = new Auth();
        result.setType((auth.getType().equals(ConfigurationType.BLACKLIST) ? ConfigurationType.BLACKLIST : ConfigurationType.WHITELIST));
        result.setVersion(auth.getVersion());
        Profiles profiles = new Profiles(); 
        for (Profile profileSrc : auth.getProfiles().getProfile()) {
            Map<String, String> actionMap = new HashMap<String, String>();
            for (Action action : profileSrc.getAction()) {
                actionMap.put(action.getId(), action.getId());
            }
            Profile profile = new Profile();
            profile.setName(profileSrc.getName());
            profile.setOrigin(profileSrc.getOrigin());
            for (String actionId : ActionRightIDs.getAllRightIDs()) {
              if(!actionMap.containsKey(actionId)) {
                  Action action = new Action();
                  action.setId(actionId);
                  profile.getAction().add(action);
              }
            }
            profiles.getProfile().add(profile);
        }
        result.setProfiles(profiles);
        result.setUserprofiles(auth.getUserprofiles()); 
        return result;
    }
    
    /**
     * Merges authorization configurations by putting
     * profiles and userprofiles of all documents in 
     * one configuration.
     * 
     * The first documents wins if there are profiles or userprofiles
     * with the same names/logins in the documents.
     * 
     * @param Documents authorization configuration documents
     * @return Merged configuration document
     */
    public static Auth merge(Auth... documents) { 
        Profiles[] profiles = new Profiles[documents.length];
        Userprofiles[] userprofiles = new Userprofiles[documents.length];
        Auth result = new Auth();
        if(profiles.length>0) {
            result.setVersion(documents[0].getVersion());
            result.setType(documents[0].getType());
            for (int i = 0; i < documents.length; i++) {
                profiles[i] = documents[i].getProfiles();
                userprofiles[i] = documents[i].getUserprofiles();
            } 
        }      
        result.setProfiles(merge(profiles));
        result.setUserprofiles(merge(userprofiles));
        return result;     
    }
    
    /**
     * Merges authorization configurations by putting all
     * profiles in one element.
     * 
     * The first profile wins if there are profiles 
     * with the same names.
     * 
     * @param Profile definitions
     * @return Merged profiles definition
     */
    public static Profiles merge(Profiles... profiles) {
        Profiles result = new Profiles(); 
        Map<String, Boolean> existMap = new HashMap<String, Boolean>();
        for (Profiles profileContainer : profiles) {
            for (Profile p : profileContainer.getProfile()) {
                // only one profile with the same name is added, the first one wins
                if(!existMap.containsKey(p.getName())) {
                    existMap.put(p.getName(), Boolean.TRUE);
                    result.getProfile().add(p);                 
                }
            }
        }
        return result;     
    }
    
    /**
     * Merges authorization configurations by putting all
     * userprofiles in one element.
     * 
     * The first userprofile wins if there are userprofiles 
     * with the same login.
     * 
     * @param Userprofile definitions
     * @return Merged userprofile definition
     */
    public static Userprofiles merge(Userprofiles... userprofiles) {
        Userprofiles result = new Userprofiles();
        Map<String, Boolean> existMap = new HashMap<String, Boolean>();
        for (Userprofiles userprofilesContainer : userprofiles) {
            for (Userprofile up : userprofilesContainer.getUserprofile()) {
                // only one userprofile per login is added, the first one wins
                if(!existMap.containsKey(up.getLogin())) {
                    existMap.put(up.getLogin(), Boolean.TRUE);
                    result.getUserprofile().add(up);
                }
            }
        }
        return result;     
    }
    
}
