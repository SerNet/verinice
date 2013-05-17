package sernet.verinice.service.ldap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

import sernet.gs.service.RuntimeCommandException;
import sernet.hui.common.connect.Property;
import sernet.verinice.interfaces.ChangeLoggingCommand;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IAuthAwareCommand;
import sernet.verinice.interfaces.IAuthService;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.IChangeLoggingCommand;
import sernet.verinice.model.bsi.BSIModel;
import sernet.verinice.model.bsi.IBSIStrukturElement;
import sernet.verinice.model.bsi.ImportBsiGroup;
import sernet.verinice.model.common.ChangeLogEntry;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.Permission;
import sernet.verinice.model.common.configuration.Configuration;
import sernet.verinice.model.iso27k.ISO27KModel;
import sernet.verinice.model.iso27k.ImportIsoGroup;
import sernet.verinice.model.iso27k.PersonIso;
import sernet.verinice.service.commands.CreateConfiguration;
import sernet.verinice.service.commands.LoadBSIModel;
import sernet.verinice.service.commands.SaveConfiguration;
import sernet.verinice.service.commands.UsernameExistsRuntimeException;
import sernet.verinice.service.iso27k.LoadImportObjectsHolder;
import sernet.verinice.service.iso27k.LoadModel;

public class SaveLdapUser extends ChangeLoggingCommand implements IChangeLoggingCommand,IAuthAwareCommand {

	 private transient Logger log = Logger.getLogger(SaveLdapUser.class);

	    public Logger getLog() {
	        if (log == null) {
	            log = Logger.getLogger(SaveLdapUser.class);
	        }
	        return log;
	    }
	
	private String stationId;
	
    private transient IAuthService authService;
	
	Set<PersonInfo> personSet;
	
	List<CnATreeElement> savedPersonList;
	
	CnATreeElement importRootObject;
	
	@SuppressWarnings("unchecked")
	private Map<Class,CnATreeElement> containerMap = new HashMap<Class,CnATreeElement>(2);
	
	public SaveLdapUser() {
		super();
        this.stationId = ChangeLogEntry.STATION_ID;
	}

	public SaveLdapUser(Set<PersonInfo> personSet) {
		this();
		this.personSet = personSet;
	}

	@Override
	public void execute() {
		if(getPersonSet()!=null) {
			savedPersonList = new ArrayList<CnATreeElement>();
			for (PersonInfo personInfo : getPersonSet()) {
				// check username
				checkUsername(personInfo.getLoginName());
				// create person
				PersonIso person = personInfo.getPerson();
				person.setParentAndScope(loadContainer(person.getClass()));
				setImportRootObject(person.getParent());
				IBaseDao<PersonIso, Integer> dao = getDaoFactory().getDAO(person.getTypeId());
				person = dao.merge(person);
				dao.flush();
				savedPersonList.add(person);
				// create configuration for person
				CreateConfiguration createConfiguration = new CreateConfiguration(person);
				try {
					createConfiguration = getCommandService().executeCommand(createConfiguration);
				} catch (CommandException e) {
					getLog().error("Error while creating configuration for user: " + personInfo.getLoginName(), e);
				}
				// save username in person
				Configuration configuration = createConfiguration.getConfiguration();
				configuration.setUser(personInfo.getLoginName());
				if(person!=null && person.getEmail()!=null && !person.getEmail().isEmpty()) {
				    configuration.setNotificationEmail(person.getEmail());
				}
				SaveConfiguration<Configuration> saveConfiguration = new SaveConfiguration<Configuration>(configuration, false);
				try {
					saveConfiguration = getCommandService().executeCommand(saveConfiguration);
				} catch (CommandException e) {
					getLog().error("Error while saving username in configuration for user: " + personInfo.getLoginName(), e);
				}
			}
		}
	}
	
	 /**
     * If during the import action an object has to be created for which no
     * parent is available (or can be found) the artificial 'rootImportObject'
     * should be used.
     * 
     * <p>
     * This method should <em>onl</em> be called when the 'rootImportObject' is
     * definitely needed and going to be used because the root object is not
     * only created but also automatically persisted in the database. If it were
     * not used later on the user would see an object node in the object tree.
     * </p>
     * @param clazz 
     * 
     * @return
     */
    private CnATreeElement loadContainer(Class clazz) {
        // Create the importRootObject if it does not exist yet
        // and set the 'importRootObject' variable.
    	CnATreeElement container = containerMap.get(clazz);
        if (container==null) {
            LoadImportObjectsHolder cmdLoadContainer = new LoadImportObjectsHolder(clazz);
            try {
            	
                cmdLoadContainer = getCommandService().executeCommand(cmdLoadContainer);
            } catch (CommandException e) {
            	getLog().error("Error while loading container", e);
                throw new RuntimeCommandException("Error while loading container",e);
            }
            container = cmdLoadContainer.getHolder();
            if(container==null) {
                container = createContainer(clazz);
            }    
            // load the parent
            container.getParent().getTitle();
            containerMap.put(clazz,container);
        } 
        return container;
    }
    
    private CnATreeElement createContainer(Class clazz) {
        if(LoadImportObjectsHolder.isImplementation(clazz, IBSIStrukturElement.class)) {
            return createBsiContainer();
        } else {
            return createIsoContainer();
        }     
    }
    
    private CnATreeElement createBsiContainer() {
        LoadBSIModel cmdLoadModel = new LoadBSIModel();
        try {
            cmdLoadModel = getCommandService().executeCommand(cmdLoadModel);
        } catch (CommandException e) {
        	getLog().error("Error ehile creating model", e);
            throw new RuntimeCommandException("Error ehile creating model", e);
        }
        BSIModel model = cmdLoadModel.getModel();
        ImportBsiGroup holder = null;
        try {
            holder = new ImportBsiGroup(model);
            addPermissions(holder);
            getDaoFactory().getDAO(ImportBsiGroup.class).saveOrUpdate(holder);
        } catch (Exception e1) {
            throw new RuntimeCommandException("Fehler beim Anlegen des Behaelters für importierte Objekte.");
        }
        return holder;
    }
    
    private CnATreeElement createIsoContainer() {
        LoadModel cmdLoadModel = new LoadModel();
        try {
            cmdLoadModel = getCommandService().executeCommand(cmdLoadModel);
        } catch (CommandException e) {
        	getLog().error("Error while creating model", e);
            throw new RuntimeCommandException("Error while creating model", e);
        }
        ISO27KModel model = cmdLoadModel.getModel();
        ImportIsoGroup holder = null;
        try {
            holder = new ImportIsoGroup(model);
            addPermissions(holder);
            getDaoFactory().getDAO(ImportIsoGroup.class).saveOrUpdate(holder);
        } catch (Exception e1) {
            throw new RuntimeCommandException("Fehler beim Anlegen des Behälters für importierte Objekte.");
        }
        return holder;
    }
    
    private void addPermissions(CnATreeElement element) {
        // We use the name of the currently
        // logged in user as a role which has read and write permissions for
        // the new Organization.
        HashSet<Permission> auditPerms = new HashSet<Permission>();
        auditPerms.add(Permission.createPermission(element, getAuthService().getUsername(), true, true));
        element.setPermissions(auditPerms);
    }
    
    /**
	 * Checks if the username in a {@link Configuration} is unique in the database.
	 * Throws {@link UsernameExistsRuntimeException} if username is not available.
	 * If username is not set or null no exception is thrown
	 * 
	 * @param element a {@link Configuration}
	 * @throws UsernameExistsRuntimeException if username is not available
	 */
	private void checkUsername(String username) throws UsernameExistsRuntimeException {	
		if(username!=null) {
			DetachedCriteria criteria = DetachedCriteria.forClass(Property.class);
			criteria.add(Restrictions.eq("propertyType", Configuration.PROP_USERNAME));
			criteria.add(Restrictions.like("propertyValue", username));
			IBaseDao<Property, Integer> dao = getDaoFactory().getDAO(Property.TYPE_ID);
			List<Property>resultList = dao.findByCriteria(criteria);
			if(resultList!=null && !resultList.isEmpty()) {
				if (getLog().isDebugEnabled()) {
					getLog().debug("Username exists: " + username);
				}
				throw new UsernameExistsRuntimeException(username,"Username already exists: " + username);
			}
		}
	}
	
	@Override
	public int getChangeType() {
		return ChangeLogEntry.TYPE_INSERT;
	}

	@Override
	public List<CnATreeElement> getChangedElements() {
		return savedPersonList;
	}

	@Override
	public String getStationId() {
		return stationId;
	}

	public Set<PersonInfo> getPersonSet() {
		return personSet;
	}

	public void setPersonSet(Set<PersonInfo> personSet) {
		this.personSet = personSet;
	}

	public CnATreeElement getImportRootObject() {
		return importRootObject;
	}

	public void setImportRootObject(CnATreeElement importRootObject) {
		this.importRootObject = importRootObject;
	}

	@Override
	public IAuthService getAuthService() {
		return this.authService;
	}

	@Override
	public void setAuthService(IAuthService service) {
		this.authService = service;	
	}

}
