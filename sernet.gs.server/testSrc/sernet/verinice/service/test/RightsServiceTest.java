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
        ActionRightIDs.SHOWALLFILES
    };
    
    @Resource(name="rightsServiceTest")
    private IRightsService rightsService;
    
    @Resource(name="rightsServerHandlerTest")
    private IRightsServerHandler rightsServerHandler;
    
    @Test
    public void testUpdate() throws Exception {
        Action removedAction = new Action();
        removedAction.setId(ActionRightIDs.ISMVIEW);
        Auth conf = rightsService.getConfiguration();
        List<Profile> profileList = conf.getProfiles().getProfile();
        for (Profile profile : profileList) {
            if(profile.getAction().contains(removedAction)) {
                profile.getAction().remove(removedAction);  
                profile.setOrigin(OriginType.MODIFICATION);
            }
        }
        rightsService.updateConfiguration(clone(conf));
        assertFalse( "Action: " + ActionRightIDs.ISMVIEW + " is enabled after removal.", rightsServerHandler.isEnabled("nn", ActionRightIDs.ISMVIEW));
        conf = rightsService.getConfiguration();
        profileList = conf.getProfiles().getProfile();
        for (Profile profile : profileList) {
            profile.getAction().add(removedAction);  
            profile.setOrigin(OriginType.MODIFICATION);
        }
        rightsService.updateConfiguration(clone(conf));
        assertTrue( "Action: " + ActionRightIDs.ISMVIEW + " is disabled after adding.", rightsServerHandler.isEnabled("nn", ActionRightIDs.ISMVIEW));
    }
    
    @Test
    public void testDefaultProfile() throws Exception {
        String[] allActionIds = ActionRightIDs.getAllRightIDs();
        Arrays.sort(adminActionIds);
        for (String id : allActionIds) {
            boolean expected = Arrays.binarySearch(adminActionIds,id) < 0;
            if(expected) {
                assertTrue( "Action: " + id + " is disabled for user.", rightsServerHandler.isEnabled("nn", id));
            } else {
                assertFalse( "Admin action: " + id + " is enabled for non admin user.", rightsServerHandler.isEnabled("nn", id));
            }
        }
        boolean isWritePermission = rightsServerHandler.isEnabled("internalAdmin", ActionRightIDs.EDITPROFILE); 
        assertTrue( "Action: " + ActionRightIDs.EDITPROFILE + " is disabled for user internalAdmin.", isWritePermission);
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
