/*******************************************************************************
 * Copyright (c) 2014 Daniel Murygin.
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
package sernet.verinice.service.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Resource;

import org.junit.Test;

import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.interfaces.IRightsServerHandler;
import sernet.verinice.interfaces.IRightsService;
import sernet.verinice.model.auth.Action;
import sernet.verinice.model.auth.Auth;
import sernet.verinice.model.auth.OriginType;
import sernet.verinice.model.auth.Profile;
import sernet.verinice.model.auth.ProfileRef;
import sernet.verinice.model.auth.Userprofile;
import sernet.verinice.service.XmlRightsService;

/**
 *
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class RightsServiceTest extends ContextConfiguration {
    
    public static final String[] adminActionIds = {
        ActionRightIDs.ACCOUNTSETTINGS,
        ActionRightIDs.ACCESSCONTROL,
        ActionRightIDs.EDITPROFILE,
        ActionRightIDs.TASKDELETE,
        ActionRightIDs.TASKSHOWALL,
        ActionRightIDs.SEARCHREINDEX,
        ActionRightIDs.SHOWALLFILES,
        ActionRightIDs.TASKCHANGEASSIGNEE,
        ActionRightIDs.TASKCHANGEDUEDATE,
        ActionRightIDs.TASKWITHRELEASEPROCESS,
    };
    
    public static final String[] newProfileActionIds = {
        ActionRightIDs.IMPORTCSV,
        ActionRightIDs.IMPORTLDAP,
        ActionRightIDs.ISMCATALOG,
        ActionRightIDs.XMLIMPORT
    };
    
    public static final String NEW_ACTION_ID = "RightsServiceTestAction";
    
    public static final String USER_NAME = "nn"; 
    
    public static final String ADMIN_NAME = "rr";
    
    public static final String USER_DEFAULT_PROFILE = "user-default-profile";
    
    public static final String PROFILE_NAME = XmlRightsService.class.getSimpleName();
    
    @Resource(name="rightsService")
    private IRightsService rightsService;
    
    @Resource(name="rightsServerHandler")
    private IRightsServerHandler rightsServerHandler;
    
    @Test
    public void testUpdate() throws Exception {
        Action action = new Action();
        action.setId(NEW_ACTION_ID);
        
        Auth conf = rightsService.getConfiguration();
        List<Profile> profileList = conf.getProfiles().getProfile();
        for (Profile profile : profileList) {
            profile.getAction().add(action);  
            profile.setOrigin(OriginType.MODIFICATION);
        }
        rightsService.updateConfiguration(clone(conf));
        assertTrue( "Action: " + NEW_ACTION_ID + " is disabled after adding.", rightsServerHandler.isEnabled(USER_NAME, NEW_ACTION_ID));
        
        conf = rightsService.getConfiguration();
        profileList = conf.getProfiles().getProfile();
        for (Profile profile : profileList) {
            if(profile.getAction().contains(action)) {
                profile.getAction().remove(action);  
                profile.setOrigin(OriginType.MODIFICATION);
            }
        }
        rightsService.updateConfiguration(clone(conf));
//        rightsServerHandler.discardData();
        assertFalse( "Action: " + NEW_ACTION_ID + " is enabled after removal.", rightsServerHandler.isEnabled(USER_NAME, NEW_ACTION_ID));       
    }
    
    @Test
    public void testAddProfile() throws Exception {
        Profile unitTestProfile = createNewProfile();
        Auth conf = addNewProfile(unitTestProfile);
        setProfileForLogin(conf, PROFILE_NAME, IRightsService.USERDEFAULTGROUPNAME);
        rightsService.updateConfiguration(clone(conf));
        testNewProfile();
        setProfileForLogin(conf, USER_DEFAULT_PROFILE, IRightsService.USERDEFAULTGROUPNAME);
        rightsService.updateConfiguration(clone(conf));
        testDefaultProfile();
        conf = removeNewProfile(unitTestProfile);
        rightsService.updateConfiguration(clone(conf));
    }

    private void testNewProfile() {
        String[] allActionIds = ActionRightIDs.getAllRightIDs();
        Arrays.sort(newProfileActionIds);
        for (String id : allActionIds) {
            boolean expected = Arrays.binarySearch(newProfileActionIds,id) > -1;
            if(expected) {
                assertTrue( "Action: " + id + " is disabled for user.", rightsServerHandler.isEnabled(USER_NAME, id));
            } else {
                assertFalse( "Action: " + id + " is enabled for  user.", rightsServerHandler.isEnabled(USER_NAME, id));
            }
        }
    }

    private Auth addNewProfile(Profile unitTestProfile) {
        Auth conf = rightsService.getConfiguration();
        List<Profile> profileList = conf.getProfiles().getProfile();
        profileList.add(unitTestProfile);
        return conf;
    }

    private Profile createNewProfile() {
        Profile unitTestProfile = new Profile();
        unitTestProfile.setName(PROFILE_NAME);
        unitTestProfile.setOrigin(OriginType.MODIFICATION);
        for (String actionId : newProfileActionIds) {
            Action action = new Action();
            action.setId(actionId);
            unitTestProfile.getAction().add(action);
        }
        return unitTestProfile;
    }
    
    private Auth removeNewProfile(Profile unitTestProfile) {
        Auth conf = rightsService.getConfiguration();
        conf.getProfiles().getProfile().remove(unitTestProfile);
        return conf;
    }
    
    @Test
    public void testDefaultProfile() throws Exception {
        String[] allActionIds = ActionRightIDs.getAllRightIDs();
        Arrays.sort(adminActionIds);
        for (String id : allActionIds) {
            boolean expected = Arrays.binarySearch(adminActionIds,id) < 0;
            if(expected) {
                assertTrue( "Action: " + id + " is disabled for user.", rightsServerHandler.isEnabled(USER_NAME, id));
            } else {
                assertFalse( "Admin action: " + id + " is enabled for non admin user.", rightsServerHandler.isEnabled(USER_NAME, id));
            }
        }
    }
    
    @Test
    public void testAdminProfile() throws Exception {
        String[] allActionIds = ActionRightIDs.getAllRightIDs();
        for (String id : allActionIds) {
            assertTrue( "Action: " + id + " is disabled for admin.", rightsServerHandler.isEnabled(ADMIN_NAME, id));
        }
    }
    
    private void setProfileForLogin(Auth conf, String profile, String login) {
        List<Userprofile> userProfileList  = conf.getUserprofiles().getUserprofile();
        for (Userprofile userprofile : userProfileList) {
            if(userprofile.getLogin().equals(login)) {
                userprofile.getProfileRef().clear();
                ProfileRef profileRef = new ProfileRef();
                profileRef.setName(profile);              
                userprofile.getProfileRef().add(profileRef);
                userprofile.setOrigin(OriginType.MODIFICATION);
            }
        }
    }

    private Auth clone(Auth conf) {
        Auth clone = new Auth();
        clone.setProfiles(conf.getProfiles());
        clone.setType(conf.getType());
        clone.setUserprofiles(conf.getUserprofiles());
        clone.setVersion(conf.getVersion());
        return clone;
    }
}
