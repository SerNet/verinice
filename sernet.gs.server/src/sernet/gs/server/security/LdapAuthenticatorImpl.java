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

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.Authentication;
import org.springframework.security.BadCredentialsException;
import org.springframework.security.ldap.DefaultSpringSecurityContextSource;
import org.springframework.security.providers.ldap.LdapAuthenticator;
import org.springframework.security.ui.digestauth.DigestProcessingFilter;
import org.springframework.security.userdetails.UsernameNotFoundException;

import sernet.gs.common.ApplicationRoles;
import sernet.gs.service.ServerInitializer;
import sernet.hui.common.connect.Entity;
import sernet.verinice.model.common.configuration.Configuration;

public class LdapAuthenticatorImpl extends UserLoader implements LdapAuthenticator {
    
    private static final Logger LOG = Logger.getLogger(LdapAuthenticatorImpl.class);
    
    // injected by spring
    private DefaultSpringSecurityContextSource contextFactory;
    
    // injected by spring
    private String principalPrefix = "";
    
    // injected by spring
    private String principalSuffix = "";
    
    // injected by spring
    private String guestUser = "";
    
    // injected by spring
    private String adminuser = "";
    
    // injected by spring
    private String passwordRealm ="";

    /**
     * @param passwordRealm the passwordRealm to set
     */
    public void setPasswordRealm(String passwordRealm) {
        this.passwordRealm = passwordRealm;
    }

    /**
     * @param adminuser the adminuser to set
     */
    public void setAdminuser(String adminuser) {
        this.adminuser = adminuser;
    }

    /**
     * @param adminpass the adminpass to set
     */
    public void setAdminpass(String adminpass) {
        this.adminpass = adminpass;
    }

    // injected by spring
    private String adminpass = "";

    /* (non-Javadoc)
     * @see org.springframework.security.providers.ldap.LdapAuthenticator#authenticate(org.springframework.security.Authentication)
     */
    @Override
    public DirContextOperations authenticate(Authentication authentication) {
        // Grab the username and password out of the authentication object.
        String username = authentication.getName();
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("Authentication start, username: \"" + username + "\"");
        }
        
        String principal = principalPrefix + username + principalSuffix;
        String password = "";
        if (authentication.getCredentials() != null) {
            password = authentication.getCredentials().toString();
        }
        
        // If we have a valid username and password, try to authenticate.
        if (!("".equals(principal.trim())) && !("".equals(password.trim()))) {
            
            // compare against the admin definied in the config file:
            if (!adminuser.isEmpty() && !adminpass.isEmpty() && username.equals(adminuser)) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Administrative username entered");
                }
                checkAdminPassword(username, password);
                return defaultAdministrator(); 
            }
            
            // authenticate against LDAP:
            if (LOG.isDebugEnabled()) {
                LOG.debug("Authenticating against AD or LDAP, user-dn: \"" + principal + "\"");
            }
            try {
                contextFactory.getReadWriteContext(principal, password);
            } catch(RuntimeException e) {
                // log auth failure and re-throw exception
                if (LOG.isInfoEnabled()) {
                    LOG.info("AD or LDAP authentication failed.");
                }
                if (LOG.isDebugEnabled()) {
                    LOG.info("Stacktrace: ", e);
                }
                throw e;
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("AD or LDAP authentication was successful");
            }
            
            ServerInitializer.inheritVeriniceContextState();
            List<Entity> entities = loadUserEntites(username);
            
            if (entities != null && entities.size()>0) {
                for (Entity entity : entities) {
                    if (DbUserDetailsService.isUser(username, entity)) {                    
                        return ldapUser(entity);
                    }
                }
            } else if (LOG.isDebugEnabled()) {
                LOG.debug("Username not found in verinice DB: " + username);
            }
            
            // no user found, we could have a guest account defined, in this case associate the authenticated ldap user 
            // with the guest account:
            if (guestUser != null && guestUser.length() > 0) {
                
                entities = loadUserEntites(guestUser);
                
                if (entities != null && entities.size()>0) {
                    for (Entity entity : entities) {
                        if (DbUserDetailsService.isUser(guestUser, entity)) {
                            // replace username in entity
                            return ldapUser(entity, new String[] {ApplicationRoles.ROLE_GUEST});
                        }
                    }
                    
                }
            }
            
            if (LOG.isDebugEnabled()) {
                LOG.debug("Authentication fails: Username and guest account not found in verinice DB." );
            }
            
            throw new UsernameNotFoundException("No matching account or guest account found for authenticated directory user " 
                    + username 
                    + " in the verinice database. Create an account for the user in verinice first, matching the directory's account name.");
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Blank username and/or password entered.");
            }
            throw new BadCredentialsException("Blank username and/or password!");
        }
    }

    /**
     * @param entity
     * @param roleGuest
     * @return
     */
    private DirContextOperations ldapUser(Entity entity, String[] specialRoles) {
        DirContextOperations authAdapter = new DirContextAdapter();
        List<String> roles = new ArrayList<String>();
           
        // All users without explicitly set Configuration.PROP_RCP==Configuration.PROP_RCP_NO
        // get ROLE_USER, user with ROLE_USER can access the RCP client 
        if (!entity.isSelected(Configuration.PROP_RCP, Configuration.PROP_RCP_NO)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("User Desktop: yes");
            }
            roles.add(ApplicationRoles.ROLE_USER);
        }
        
        // All users without explicitly set PROP_WEB==Configuration.PROP_RCP_NO
        // get ROLE_WEB, user with ROLE_WEB can access the web client 
        if (!entity.isSelected(Configuration.PROP_WEB, Configuration.PROP_WEB_NO)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Web Desktop: yes");
            }
            roles.add(ApplicationRoles.ROLE_WEB);
        }
        
        // if set in the entity, the user may also have the admin role:
        if (entity.isSelected(Configuration.PROP_ISADMIN, "configuration_isadmin_yes")){
            if (LOG.isDebugEnabled()) {
                LOG.debug("Administrator: yes");
            }
            roles.add(ApplicationRoles.ROLE_ADMIN);
        }
        // add special roles:
        if (specialRoles != null && specialRoles.length>0) {
            for (String role: specialRoles) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Additional role: " + role);
                }
                roles.add(role);
            }
        }
        
        String[] rolesArray= roles.toArray(new String[roles.size()]);
        authAdapter.setAttributeValues(LdapAuthenticationProvider.ROLES_ATTRIBUTE, rolesArray);
        return authAdapter;
        
    }

    /**
     * @return
     */
    private DirContextOperations defaultAdministrator() {
        DirContextOperations authAdapter = new DirContextAdapter();
        List<String> roles = new ArrayList<String>();
        
        roles.add(ApplicationRoles.ROLE_USER);
        roles.add(ApplicationRoles.ROLE_ADMIN);
        roles.add(ApplicationRoles.ROLE_WEB);
        
        String[] rolesArray= roles.toArray(new String[roles.size()]);
        authAdapter.setAttributeValues(LdapAuthenticationProvider.ROLES_ATTRIBUTE, rolesArray);
        return authAdapter;
    }

    /**
     * @param username
     * @param password
     */
    private void checkAdminPassword(String username, String password) {
        String hash = DigestProcessingFilter.encodePasswordInA1Format(username,
                passwordRealm, password);
        if (hash.equals(adminpass)){
            return;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Wrong password for administrative user");
        }
        throw new BadCredentialsException("Wrong username / password for administrative user.");
    }

    /**
     * @param guestUser the guestUser to set
     */
    public void setGuestUser(String guestUser) {
        this.guestUser = guestUser;
    }

    /**
     * @param entity
     * @return
     */
    private DirContextOperations ldapUser(Entity entity) {
        return ldapUser(entity, null);
    }

    public DefaultSpringSecurityContextSource getContextFactory() {
        return contextFactory;
    }

    /**
     * Set the context factory to use for generating a new LDAP context.
     * 
     * @param contextFactory
     */
    public void setContextFactory(DefaultSpringSecurityContextSource contextFactory) {
        this.contextFactory = contextFactory;
    }

    public String getPrincipalPrefix() {
        return principalPrefix;
    }

    /**
     * Set the string to be prepended to all principal names prior to attempting
     * authentication against the LDAP server. (For example, if the Active
     * Directory wants the domain-name-plus backslash prepended, use this.)
     * 
     * @param principalPrefix
     */
    public void setPrincipalPrefix(String principalPrefix) {
        if (principalPrefix != null) {
            this.principalPrefix = principalPrefix;
        } else {
            this.principalPrefix = "";
        }
    }

    public String getPrincipalSuffix() {
        return principalSuffix;
    }

    public void setPrincipalSuffix(String principalSuffix) {
        this.principalSuffix = principalSuffix;
    }
}
