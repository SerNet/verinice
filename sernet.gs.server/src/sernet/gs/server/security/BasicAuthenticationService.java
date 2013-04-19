/*******************************************************************************
 * Copyright (c) 2010 Alexander Koderman <ak@sernet.de>.
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
 *     Alexander Koderman <ak@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.server.security;

import java.io.Serializable;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.security.Authentication;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.context.SecurityContext;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.security.ui.basicauth.BasicProcessingFilterEntryPoint;

import sernet.gs.common.ApplicationRoles;
import sernet.verinice.interfaces.IAuthService;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.common.configuration.Configuration;

/**
 * HTTP basic method authentication service.
 * Allows access to roles and name of user that is currently logged on.
 * 
 * 
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public class BasicAuthenticationService implements IAuthService {
    
    private BasicProcessingFilterEntryPoint entryPoint;
    private String guestUser = "";
    private String adminUsername;
    private IBaseDao<Configuration, Serializable> configurationDao;
    private boolean handlingPasswords;

    /**
     * @param guestUser the guestUser to set
     */
    public void setGuestUser(String guestUser) {
        this.guestUser = guestUser;
    }
    
    /**
     * @param entryPoint the entryPoint to set
     */
    public void setEntryPoint(BasicProcessingFilterEntryPoint entryPoint) {
        this.entryPoint = entryPoint;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IAuthService#getRoles()
     */
    @Override
    public String[] getRoles() {
        GrantedAuthority[] authority = SecurityContextHolder.getContext().getAuthentication().getAuthorities();
        String[] roles = new String[authority.length];
        for (int i=0;i<authority.length; i++) {
            roles[i] = authority[i].getAuthority();
        }
        return roles;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IAuthService#getUsername()
     */
    @Override
    public String getUsername() {
        try {
            SecurityContext context = SecurityContextHolder.getContext();
            Authentication authentication = context.getAuthentication();
            GrantedAuthority[] authorities = authentication.getAuthorities();
            if (guestUser != null && guestUser.length()>0 && isGuestUser(authorities)){
                return guestUser;
            } else {
                return authentication.getName();
            }
        } catch (Exception e) {
            // do nothing, just return no user name
            Logger.getLogger( this.getClass() ).error( Messages.getString("AuthenticationService.1"), e ); //$NON-NLS-1$
        }
        // no user authenticated:
        return ""; //$NON-NLS-1$
    
    }

    @Override
    public String getAdminUsername() {
        return adminUsername;
    }

    public void setAdminUsername(String adminUsername) {
        this.adminUsername = adminUsername;
    }

    /**
     * @param authorities
     * @return
     */
    private boolean isGuestUser(GrantedAuthority[] authorities) {
        for (GrantedAuthority auth : authorities) {
            if (auth.getAuthority().equals(ApplicationRoles.ROLE_GUEST)) {
                return true;
            }
        }
        return false;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IAuthService#hashOwnPassword(java.lang.String, java.lang.String)
     */
    @Override
    public String hashOwnPassword(String username, String clearText) {
        // not implemented, user cannot change his passowrd using this service
        return null;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IAuthService#hashPassword(java.lang.String, java.lang.String)
     */
    @Override
    public String hashPassword(String username, String clearText) {
        // not implemented, user cannot change his passowrd using this service
        return null;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IAuthService#isPermissionHandlingNeeded()
     */
    @Override
    public boolean isPermissionHandlingNeeded() {
        return true;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IAuthService#isScopeOnly()
     */
    @Override
    public boolean isScopeOnly() {
        String hql = "select scopeprops.propertyValue from Configuration as conf " + //$NON-NLS-1$
                "inner join conf.entity as entity " + //$NON-NLS-1$
                "inner join entity.typedPropertyLists as propertyList " + //$NON-NLS-1$
                "inner join propertyList.properties as props " + //$NON-NLS-1$
                "inner join conf.entity as entity2 " + //$NON-NLS-1$
                "inner join entity2.typedPropertyLists as propertyList2 " + //$NON-NLS-1$
                "inner join propertyList2.properties as scopeprops " + //$NON-NLS-1$
                "where props.propertyType = ? " + //$NON-NLS-1$
                "and props.propertyValue like ? " + //$NON-NLS-1$
                "and scopeprops.propertyType = ?";   //$NON-NLS-1$
        Object[] params = new Object[]{Configuration.PROP_USERNAME,getUsername(),Configuration.PROP_SCOPE};                
        List<String> resultList = getConfigurationDao().findByQuery(hql,params);
        String value = null;
        if (resultList != null && resultList.size() == 1) {
            value = resultList.get(0);
        }       
        return Configuration.PROP_SCOPE_YES.equals(value);
    }

    /**
     * @return the handlingPasswords
     */
    @Override
    public boolean isHandlingPasswords() {
        return handlingPasswords;
    }

    /**
     * @param handlingPasswords the handlingPasswords to set
     */
    public void setHandlingPasswords(boolean handlingPasswords) {
        this.handlingPasswords = handlingPasswords;
    }

    /**
     * @return the configurationDao
     */
    public IBaseDao<Configuration, Serializable> getConfigurationDao() {
        return configurationDao;
    }

    /**
     * @param configurationDao the configurationDao to set
     */
    public void setConfigurationDao(IBaseDao<Configuration, Serializable> configurationDao) {
        this.configurationDao = configurationDao;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IAuthService#isLogoutPossible()
     */
    @Override
    public boolean isLogoutPossible() {
        return true;
    }

}


