/*******************************************************************************
 * Copyright (c) 2017 Urs Zeidler.
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
 *     Urs Zeidler uzeidler<at>sernet.de - initial API and implementation
 ******************************************************************************/
package sernet.verinice.service.test;

import sernet.verinice.interfaces.IAuthService;
import sernet.verinice.service.NoAuthenticationService;

/**
 * This is the {@link IAuthService} used for testing, it reproduce the behavior
 * of the {@link NoAuthenticationService} with the exception of changing the
 * username to make some checkrights test possible. It is configured in the
 * veriniceserver-security-osgi-test.xml. To change the username a cast to this
 * class is necessary.
 * 
 * @author urszeidler
 *
 */
public class TestAuthenticationService implements IAuthService {
    public static final String INTERNAL_ADMIN = "internalAdmin";
    private static final String[] NO_ROLES = new String[0];
    private String[] roles;
    private String username;

    @Override
    public String[] getRoles() {
        if (roles == null)
            return NO_ROLES;
        return roles;
    }

    @Override
    public String getUsername() {
        if (username == null)
            return INTERNAL_ADMIN;
        return username;
    }

    @Override
    public boolean isLogoutPossible() {
        return false;
    }

    @Override
    public boolean isHandlingPasswords() {
        return false;
    }

    @Override
    public String hashPassword(String username, String clearText) {
        return null;
    }

    @Override
    public String hashOwnPassword(String username, String clearText) {
        return null;
    }

    @Override
    public boolean isPermissionHandlingNeeded() {
        return false;
    }

    @Override
    public String getAdminUsername() {
        return INTERNAL_ADMIN;
    }

    @Override
    public boolean isScopeOnly() {
        return false;
    }

    @Override
    public boolean isDeactivated() {
        return false;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setRoles(String[] roles) {
        this.roles = roles;
    }

    @Override
    public boolean currentUserHasRole(String[] allowedRoles) {
        return false;
    }
}
