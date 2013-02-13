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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.core.io.Resource;

import sernet.gs.service.SecurityException;
import sernet.hui.common.connect.Property;
import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.interfaces.IAuthService;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.IRightsChangeListener;
import sernet.verinice.interfaces.IRightsService;
import sernet.verinice.model.auth.Action;
import sernet.verinice.model.auth.Auth;
import sernet.verinice.model.auth.ConfigurationType;
import sernet.verinice.model.auth.OriginType;
import sernet.verinice.model.auth.Profile;
import sernet.verinice.model.auth.ProfileRef;
import sernet.verinice.model.auth.Profiles;
import sernet.verinice.model.auth.Userprofile;
import sernet.verinice.model.auth.Userprofiles;
import sernet.verinice.model.bsi.Person;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.configuration.Configuration;
import sernet.verinice.model.iso27k.PersonIso;

/**
 * Service to read and change the authorization configuration of verinice.
 * 
 * This implementation loads and saves configuration in an XML file.
 * XML schema is defined in: <code>verinice-auth.xsd</code>.
 * 
 * Configuration is defined in two documents:
 * <ul>
 * <li>WEB-INF/verinice-auth-default.xml: Default configuration. This file is never changed by an administrator.</li>
 * <li>WEB-INF/verinice-auth.xml: Configuration. Settings in this file overwrite verinice-auth-default.xml.</li>
 * </ul>
 * 
 * This service is managed by the Spring container as a _singleton_ but it's running
 * in a multi threaded web server environment. Keep that in mind if you change this service.
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class XmlRightsService implements IRightsService {

    private final Logger log = Logger.getLogger(XmlRightsService.class);
    
    private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final Lock readLock = readWriteLock.readLock();
    private final Lock writeLock = readWriteLock.writeLock();
      
    /**
     * Holds the authorization configuration.
     * Variable will be read and modified by different threads.
     * Read this for a definition of "volatile":
     * http://www.javamex.com/tutorials/synchronization_volatile.shtml
     */
    private volatile Auth auth;
    
    /**
     * Key: scope name, Value: all users of this scope
     */
    private volatile Map<String,List<String>> usernameMap = new Hashtable<String, List<String>>();
    
    /**
     * Key: scope name, Value: all groups of this scope
     */
    private volatile Map<String,List<String>> groupnameMap = new Hashtable<String, List<String>>();
    
    private RightsServerHandler rightsServerHandler;
    
    private Resource authConfigurationDefault;
    
    private Resource authConfiguration;
    
    private Resource authConfigurationSchema;
    
    private JAXBContext context;
    
    private Schema schema;

    private IConfigurationService configurationService;
    
    private IBaseDao<Configuration, Integer> configurationDao;
    
    private IBaseDao<Property, Integer> propertyDao;
    
    private IRemoteMessageSource messages;
    
    private IAuthService authService;
    
    private Map<String, Profile> profileMap;
    
    private static List<IRightsChangeListener> changeListener = new LinkedList<IRightsChangeListener>();

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IRightsService#getConfiguration()
     */
    @Override
    public Auth getConfiguration() {
        // a local var. is used to make this thread save:
        Auth currentAuth = auth;
        if(currentAuth==null) {
            // prevent reading the configuration while another thread is writing it
            readLock.lock();
            try {
                currentAuth = loadConfiguration();
                auth = currentAuth;
            } finally {
                readLock.unlock(); 
            }
         
            if (log.isDebugEnabled()) {
                if (log.isDebugEnabled()) {
                    log.debug("Merged auth configuration: ");
                }
                logAuth(currentAuth);
            }
        }      
        return currentAuth;
    }

    /**
     * For debugging only!
     */
    private void logAuth(Auth auth) {
        try {
            if (log.isDebugEnabled()) {
                Marshaller marshaller = getContext().createMarshaller();
                marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
                marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");  
                StringWriter sw = new StringWriter();
                marshaller.marshal(auth, sw);
                log.debug(sw.toString());
            }
        } catch (Exception e) {
            log.error("Error while logging auth", e);
        }
    }


    /**
     * Loads the configuration by merging the files
     * 'verinice-auth-default.xml' and 'verinice-auth.xml'
     * 
     * @return The authorization configuration
     * @throws IllegalAuthConfTypeException if a different configurationType is detected in 'verinice-auth-default.xml' and 'verinice-auth.xml'
     */
    private Auth loadConfiguration() {
        try {
            Unmarshaller unmarshaller = getContext().createUnmarshaller();            
            unmarshaller.setSchema(getSchema());
            
            // read default configuration
            Auth authDefault = (Auth) unmarshaller.unmarshal(getAuthConfigurationDefault().getInputStream());
            Auth authUser = null;
            
            // check if configuration exists
            if(getAuthConfiguration().exists()) {
                if (log.isDebugEnabled()) {
                    log.debug("Reading authorization configuration from file: " + getAuthConfiguration().getFile().getPath());
                }
                
                authUser = (Auth) unmarshaller.unmarshal(getAuthConfiguration().getInputStream());           
                
                // check configuration type of both files
                // throw an exception if a different type is detected
                if(!authDefault.getType().equals(authUser.getType())) {
                    final String message = "You must use the same configurationType in 'verinice-auth-default.xml' and 'verinice-auth.xml'";
                    throw new IllegalAuthConfTypeException(message);
                }
                // merge both configurations
                authDefault = AuthHelper.merge(new Auth[]{authUser,authDefault});
            }
            
            return authDefault;
        } catch (RuntimeException e) {
            log.error("Error while reading verinice authorization definition from file: " + getAuthConfiguration().getFilename(), e);
            throw e;
        } catch (Exception e) {
            log.error("Error while reading verinice authorization definition from file: " + getAuthConfiguration().getFilename(), e);
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Updates the configuration defined in <code>auth</code>.
     * 
     * Content of <code>authNew</code> is saved in file: verinice-auth.xml
     * if origin of profiles and userprofiles is "modification".
     * 
     * Before writing the content the implementation checks
     * if user is allowed to change the configuration.
     * 
     * @see sernet.verinice.interfaces.IRightsService#updateConfiguration(sernet.verinice.model.auth.Auth)
     */
    @Override
    public void updateConfiguration(Auth authNew) {
        try {     
            if(!isReferenced(ActionRightIDs.EDITPROFILE, authNew)){
                log.warn("Right id: " + ActionRightIDs.EDITPROFILE + " is not referenced in the auth configuration. No user is able to change the configuration anymore.");
            }
    
            Profiles profilesMod = new Profiles();     
            for (Profile profile : authNew.getProfiles().getProfile()) {
                // add profile if origin is "modification"
                if(!OriginType.DEFAULT.equals(profile.getOrigin())) {
                    profilesMod.getProfile().add(profile);
                }
            }
            authNew.setProfiles(profilesMod);
            
            Userprofiles userprofilesMod = new Userprofiles();   
            
            for (Userprofile userprofile : authNew.getUserprofiles().getUserprofile()) {             
                if(!OriginType.DEFAULT.equals(userprofile.getOrigin())) {
                    userprofilesMod.getUserprofile().add(userprofile);
                }
            }
            authNew.setUserprofiles(userprofilesMod);
            
            // Block all other threads before writing the file
            writeLock.lock();
            try {
                checkWritePermission(); //throws sernet.gs.service.SecurityException
                // create a backup of the old configuration
                backupConfigurationFile();
                // write the new configuration
                Marshaller marshaller = getContext().createMarshaller();       
                marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
                marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8"); 
                marshaller.setSchema(getSchema());
                marshaller.marshal( authNew, new FileOutputStream( getAuthConfiguration().getFile().getPath() ) );
                // set auth to null, 
                // next call of getCofiguration will read the new configuration from disk
                this.auth = null;
            } finally {
                writeLock.unlock();
            }
            
            fireChangeEvent();

        } catch (sernet.gs.service.SecurityException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            String message = "Error while updating authorization configuration.";
            log.error(message, e);
            // Big Problem: writing of configuration failed! 
            // Restore it from backup
            // Block all other threads before writing the file
            writeLock.lock();
            try {
                log.error("Trying to restore the authorization configuration from backup file now...");
                restoreConfigurationFile();
                log.error("Authorization configuration restored from backup file.");
            } finally {
                writeLock.unlock();
            }
            throw new RuntimeException(message);
        }
    }

    private void fireChangeEvent() {       
        for (IRightsChangeListener listener : getChangeListener()) {
            listener.configurationChanged(getConfiguration());
        }
    }

    /**
     * Throws a sernet.gs.service.SecurityException
     * if current user has no permission to write the 
     * authorization configuration.
     */
    private void checkWritePermission() throws sernet.gs.service.SecurityException {
        boolean isWritePermission = getRightsServerHandler().isEnabled(getAuthService().getUsername(), ActionRightIDs.EDITPROFILE); 
        if(!isWritePermission) {
            throw new SecurityException("User " + getAuthService().getUsername() + " has no permission to write authorization configuration.");
        }       
    }
    
    /**
     * Creates a copy of the configuration file with suffix ".bak".
     * If the copy file exists, then this method will overwrite it. 
     * @throws IOException 
     */
    private void backupConfigurationFile() {
        try {
            File backup = new File(getBackupFileName());
            FileUtils.copyFile(getAuthConfiguration().getFile(), backup); 
        } catch( Exception t ) {
            log.error("Error while creating backup of authorization configuration.", t);
        }
    }
    
    /**
     * Restores the configuration file from the backup with suffix ".bak".
     */
    private void restoreConfigurationFile() {
        try {
            File backup = new File(getBackupFileName());
            File conf = getAuthConfiguration().getFile();
            FileUtils.copyFile(backup, conf); 
        } catch( Exception t ) {
            log.error("Error while restoring authorization configuration.", t);
        }
    }
    
    private String getBackupFileName() throws IOException {
        return getAuthConfiguration().getFile().getAbsolutePath() + ".bak";
    }

    /**
     * Returns the userprofiles of an user by selecting the 
     * userprofile of the user and the userprofiles of the
     * groups the user belongs to.
     * 
     * @see sernet.verinice.interfaces.IRightsService#getUserprofile(java.lang.String)
     */
    @Override
    public List<Userprofile> getUserprofile(String username) {
        List<String> roleList = getRoleList(username);
        // add the username to the list
        roleList.add(username);   
        List<Userprofile> userprofileList = new ArrayList<Userprofile>(1);
        List<Userprofile> allUserprofileList = getConfiguration().getUserprofiles().getUserprofile();
        for (Userprofile userprofile : allUserprofileList) {
            if(roleList.contains(userprofile.getLogin())) {
                userprofileList.add(userprofile);
            }
        }
        return userprofileList;
    }

    /**
     * Returns an list with the role/groups of an user. 
     * The returned list contains the user name.
     * 
     * @param username an username
     * @return the role/groups of an user
     */
    private List<String> getRoleList(String username) {
        // select all groups of the user
        String hql = "select roleprops.propertyValue from Configuration as conf " + //$NON-NLS-1$
                "inner join conf.entity as entity " + //$NON-NLS-1$
                "inner join entity.typedPropertyLists as propertyList " + //$NON-NLS-1$
                "inner join propertyList.properties as props " + //$NON-NLS-1$
                "inner join conf.entity as entity2 " + //$NON-NLS-1$
                "inner join entity2.typedPropertyLists as propertyList2 " + //$NON-NLS-1$
                "inner join propertyList2.properties as roleprops " + //$NON-NLS-1$
                "where props.propertyType = ? " + //$NON-NLS-1$
                "and props.propertyValue like ? " + //$NON-NLS-1$
                "and roleprops.propertyType = ?"; //$NON-NLS-1$
        Object[] params = new Object[]{Configuration.PROP_USERNAME,username,Configuration.PROP_ROLES};        
        return getConfigurationDao().findByQuery(hql,params);
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IRightsService#getUsernames()
     */
    @Override
    public List<String> getUsernames() {
        String hql = "select props.propertyValue from Property as props " + //$NON-NLS-1$
                "where props.propertyType = ?"; //$NON-NLS-1$
        Object[] params = new Object[]{Configuration.PROP_USERNAME};  
        List<String> usernameList = getPropertyDao().findByQuery(hql,params);
        usernameList.add(getAuthService().getAdminUsername());
        return usernameList;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IRightsService#getGroupnames()
     */
    @Override
    public List<String> getGroupnames() {
        String hql = "select props.propertyValue from Property as props " + //$NON-NLS-1$
                "where props.propertyType = ?"; //$NON-NLS-1$
        Object[] params = new Object[]{Configuration.PROP_ROLES};  
        return getPropertyDao().findByQuery(hql,params);
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IRightsService#getProfiles(java.lang.Integer)
     */
    @Override
    public List<String> getGroupnames(String username) {
        List<String> groupnameList = groupnameMap.get(username);
        if(groupnameList==null) {
            loadUserAndGroupNames(username);
            groupnameList = groupnameMap.get(username);
        }
        return groupnameList;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IRightsService#getUsernames(java.lang.String)
     */
    @Override
    public List<String> getUsernames(String username) {
        List<String> usernameList = usernameMap.get(username);
        if(usernameList==null) {
            loadUserAndGroupNames(username);
            usernameList = usernameMap.get(username);
        }
        return usernameList;
    }
    
    private void loadUserAndGroupNames(String username) {
        Integer scopeId = getConfigurationService().getScopeId(username);
        
        String hql = "from CnATreeElement c " + //$NON-NLS-1$           
                "where c.scopeId = ? " + //$NON-NLS-1$
                "and (c.objectType = ? or c.objectType = ?)"; //$NON-NLS-1$
        Object[] params = new Object[]{scopeId,PersonIso.TYPE_ID,Person.TYPE_ID};  
        List<CnATreeElement> elementList = getPropertyDao().findByQuery(hql,params);
        Object[] idList = new Object[elementList.size()];
        int i=0;
        for (CnATreeElement person : elementList) {
            idList[i]=person.getDbId();
            i++;
        }
        DetachedCriteria crit = DetachedCriteria.forClass(Configuration.class);
        crit.setFetchMode("entity", FetchMode.JOIN); //$NON-NLS-1$
        crit.setFetchMode("entity.typedPropertyLists", FetchMode.JOIN); //$NON-NLS-1$
        crit.setFetchMode("entity.typedPropertyLists.properties", FetchMode.JOIN); //$NON-NLS-1$
        crit.setFetchMode("person", FetchMode.JOIN); //$NON-NLS-1$
        crit.add(Restrictions.in("person.id", idList)); //$NON-NLS-1$
        crit.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        
        List<Configuration> confList = getPropertyDao().findByCriteria(crit);     
        Set<String> usernameList = new HashSet<String>(confList.size());
        Set<String> groupnameList = new HashSet<String>(confList.size());
        for (Configuration configuration : confList) {
            if(configuration.getUser()!=null && !configuration.getUser().trim().isEmpty()) {
                usernameList.add(configuration.getUser());
            }
            groupnameList.addAll(configuration.getRoles());
        }
        this.usernameMap.put(username, new ArrayList<String>(usernameList));
        this.groupnameMap.put(username, new ArrayList<String>(groupnameList));
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IRightsService#getProfiles()
     */
    @Override
    public Profiles getProfiles() {
        return getConfiguration().getProfiles();
    }
    
    public RightsServerHandler getRightsServerHandler() {
        if(rightsServerHandler==null) {
            rightsServerHandler = new RightsServerHandler(this);
        }
        return rightsServerHandler;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IRightsService#getMessage(java.lang.String)
     */
    @Override
    public String getMessage(String key) {
        String message;
        try {
            message= getMessages().getMessage(key, null, Locale.getDefault());
        } catch (Exception e) {
            log.warn("Message not found: " + key);
            if (log.isDebugEnabled()) {
                log.debug("Stacktrace: ", e);
            }
            message = key + " (!)";
        }    
        return message;
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IRightsService#getAllMessages()
     */
    @Override
    public Properties getAllMessages() {
        return getMessages().getAllMessages();
    }

    /**
     * @return the authConfigurationDefault
     */
    public Resource getAuthConfigurationDefault() {
        return authConfigurationDefault;
    }

    /**
     * @param authConfigurationDefault the authConfigurationDefault to set
     */
    public void setAuthConfigurationDefault(Resource authConfigurationDefault) {
        this.authConfigurationDefault = authConfigurationDefault;
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

    /**
     * @return the configurationService
     */
    public IConfigurationService getConfigurationService() {
        return configurationService;
    }

    /**
     * @param configurationService the configurationService to set
     */
    public void setConfigurationService(IConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    /**
     * @return the configurationDao
     */
    public IBaseDao<Configuration, Integer> getConfigurationDao() {
        return configurationDao;
    }

    /**
     * @param configurationDao the configurationDao to set
     */
    public void setConfigurationDao(IBaseDao<Configuration, Integer> configurationDao) {
        this.configurationDao = configurationDao;
    }

    /**
     * @return the propertyDao
     */
    public IBaseDao<Property, Integer> getPropertyDao() {
        return propertyDao;
    }

    /**
     * @param propertyDao the propertyDao to set
     */
    public void setPropertyDao(IBaseDao<Property, Integer> propertyDao) {
        this.propertyDao = propertyDao;
    }

    /**
     * @return the messages
     */
    public IRemoteMessageSource getMessages() {
        return messages;
    }

    /**
     * @param messages the messages to set
     */
    public void setMessages(IRemoteMessageSource messages) {
        this.messages = messages;
    }

    /**
     * @return the context
     */
    private JAXBContext getContext() {
        if(context==null) {
            try {
                context = JAXBContext.newInstance(Auth.class);
            } catch (JAXBException e) {
                log.error("Error while creating JAXB context.", e);
            }
        }
        return context;
    }
    
    private Schema getSchema() {
        if(schema==null) {
            SchemaFactory sf = SchemaFactory.newInstance(javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI);
            try {
                schema = sf.newSchema(getAuthConfigurationSchema().getURL());
            } catch (Exception e) {
                log.error("Error while creating schema.", e);
            } 
        }
        return schema;
    }

    public IAuthService getAuthService() {
        return authService;
    }

    public void setAuthService(IAuthService authService) {
        this.authService = authService;
    }
    
    private Map<String, Profile> getProfileMap() {
        if(profileMap == null){
            Profiles profiles = getProfiles();   
            profileMap = new HashMap<String, Profile>();
            for (Profile profile : profiles.getProfile()) {
                profileMap.put(profile.getName(), profile);
            }
        }
        return profileMap;
    }
    
    /**
     * Returns <code>true</code> if an action is
     * referenced by a user profile, false if not.
     * 
     * @param actionId The id of an action
     * @param auth An auth configuration
     * @return True if actionId is referenced in auth
     */
    private boolean isReferenced(String actionId, Auth auth) {
        boolean returnValue = false;
        try {
            Map<String, Action> actionMap = loadAllReferencedActions(auth);
            if(actionMap != null) {
                returnValue = actionMap.get(actionId)!=null && isWhitelist() || 
                              actionMap.get(actionId)==null && isBlacklist();
            }
            return returnValue;
        } catch (Exception e) {
            log.error("Error while checking action. Returning false", e);
            return returnValue;
        }
    }
    
    /**
     * Returns all actions in param <code>auth</code>
     * which are referenced by a user profile.
     * 
     * Actions are returned in a map. Key is the id of the action,
     * value is the action.
     * 
     * @param auth An auth configuration
     * @return All actions which are referenced by a user profile of an auth configuration
     */
    private Map<String, Action> loadAllReferencedActions(Auth auth) {
        Map<String, Action> actionMap = new HashMap<String, Action>();
        for (Userprofile userprofile : auth.getUserprofiles().getUserprofile()) {  
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
                        log.error("Could not find profile " + profileRef.getName() + " of user " + getAuthService().getUsername());
                    }
                }
            }
        }
        return actionMap;
    }
    
    private static List<IRightsChangeListener> getChangeListener() {    
        return XmlRightsService.changeListener;
    }
    
    public static void addChangeListener(IRightsChangeListener listener) {
        getChangeListener().add(listener);
    }
    
    public static void removeChangeListener(IRightsChangeListener listener) {
        getChangeListener().remove(listener);
    }
    
    public boolean isWhitelist() {
        return ConfigurationType.WHITELIST.equals(getConfiguration().getType());
    }
    
    public boolean isBlacklist() {
        return ConfigurationType.BLACKLIST.equals(getConfiguration().getType());
    }
}
