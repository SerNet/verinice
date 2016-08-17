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
package sernet.gs.server.security;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.security.GrantedAuthority;
import org.springframework.security.GrantedAuthorityImpl;
import org.springframework.security.providers.UsernamePasswordAuthenticationToken;

import sernet.gs.common.ApplicationRoles;
import sernet.verinice.interfaces.ActionRightIDs;

@SuppressWarnings("serial")
public final class DummyAuthentication extends UsernamePasswordAuthenticationToken {

    
    public DummyAuthentication(String username) {
        super(
                new VeriniceUserDetails(username, "$dummypwd$"), 
                "$notused$", getRolesAndActionRightIds());
        setAuthenticated(true);
    }
    
    public DummyAuthentication() {
        super(
                new VeriniceUserDetails("$internaluser$", "$dummypwd$"), 
                "$notused$", getRolesAndActionRightIds());
        setAuthenticated(true);
    }

    @Override
    public void setAuthenticated(boolean b) {
        // Allow being authenticated only when the caller is an
        // InternalAuthenticationProvider instance.
        StackTraceElement[] t = Thread.currentThread().getStackTrace();
        if (b && t.length >= 1 && InternalAuthenticationProvider.class.getName().equals(t[1].getClassName()) && "authenticate".equals(t[1].getMethodName())) { //$NON-NLS-1$
            super.setAuthenticated(true);
        }
    }

    private static GrantedAuthority[] getRolesAndActionRightIds(){
        List<GrantedAuthority> userRoles = Arrays.asList(getUserRoles());
        List<GrantedAuthority> actionRightsIds = Arrays.asList(addActionRightIds());
        List<GrantedAuthority> allRoles = new ArrayList<>(userRoles.size() + actionRightsIds.size());
        allRoles.addAll(userRoles);
        allRoles.addAll(actionRightsIds);

        return allRoles.toArray(new GrantedAuthority[allRoles.size()]);
    }

    private static GrantedAuthority[] addActionRightIds(){
        String[] allRightIDs = ActionRightIDs.getAllRightIDs();
        GrantedAuthority[] authorities = new GrantedAuthorityImpl[allRightIDs.length];
        for (int i = 0; i < allRightIDs.length; i++) {
            authorities[i] = new GrantedAuthorityImpl(allRightIDs[i]);
        }

        return authorities;
    }

    private static GrantedAuthority[] getUserRoles(){
        return new GrantedAuthority[] {
                new GrantedAuthorityImpl(ApplicationRoles.ROLE_USER),
                new GrantedAuthorityImpl(ApplicationRoles.ROLE_WEB),
                new GrantedAuthorityImpl(ApplicationRoles.ROLE_ADMIN)};
    }
}