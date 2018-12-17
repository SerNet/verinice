package sernet.verinice.service.ldap;

import java.io.Serializable;
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
import sernet.verinice.interfaces.IAuthAwareCommand;
import sernet.verinice.interfaces.IAuthService;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.IChangeLoggingCommand;
import sernet.verinice.interfaces.IRightsService;
import sernet.verinice.model.bp.elements.BpModel;
import sernet.verinice.model.bp.groups.ImportBpGroup;
import sernet.verinice.model.bsi.BSIModel;
import sernet.verinice.model.bsi.ImportBsiGroup;
import sernet.verinice.model.bsi.Person;
import sernet.verinice.model.common.ChangeLogEntry;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.Domain;
import sernet.verinice.model.common.Permission;
import sernet.verinice.model.common.configuration.Configuration;
import sernet.verinice.model.iso27k.ISO27KModel;
import sernet.verinice.model.iso27k.ImportIsoGroup;
import sernet.verinice.model.iso27k.PersonIso;
import sernet.verinice.service.commands.CnATypeMapper;
import sernet.verinice.service.commands.CreateConfiguration;
import sernet.verinice.service.commands.SaveConfiguration;
import sernet.verinice.service.commands.UsernameExistsRuntimeException;
import sernet.verinice.service.iso27k.LoadImportObjectsHolder;
import sernet.verinice.service.model.LoadModel;

public class SaveLdapUser extends ChangeLoggingCommand
        implements IChangeLoggingCommand, IAuthAwareCommand {

    private static final Logger log = Logger.getLogger(SaveLdapUser.class);

    private String stationId;

    private transient IAuthService authService;

    private Set<PersonInfo> personSet;

    private List<CnATreeElement> savedPersonList;

    private CnATreeElement importRootObject;

    private Map<Domain, CnATreeElement> containerMap = new HashMap<>(3);

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

        if (personSet == null) {
            return;
        }
        savedPersonList = new ArrayList<>();
        for (PersonInfo personInfo : getPersonSet()) {
            // check username
            checkUsername(personInfo.getLoginName());
            // create person
            CnATreeElement person = createPerson(personInfo);
            // create configuration for person
            CreateConfiguration createConfiguration = new CreateConfiguration(person);
            try {
                createConfiguration = getCommandService().executeCommand(createConfiguration);
            } catch (CommandException e) {
                log.error(
                        "Error while creating configuration for user: " + personInfo.getLoginName(),
                        e);
            }
            // save username in person
            Configuration configuration = createConfiguration.getConfiguration();
            setUserAndEMail(personInfo, person, configuration);
            SaveConfiguration<Configuration> saveConfiguration = new SaveConfiguration<>(
                    configuration, false);
            try {
                getCommandService().executeCommand(saveConfiguration);
            } catch (CommandException e) {
                log.error("Error while saving username in configuration for user: "
                        + personInfo.getLoginName(), e);
            }
        }

    }

    private static void setUserAndEMail(PersonInfo personInfo, CnATreeElement person,
            Configuration configuration) {
        configuration.setUser(personInfo.getLoginName());
        String email = "";
        if (person != null) {
            if (person instanceof Person) {
                email = ((Person) person).getEntity().getPropertyValue(Person.P_EMAIL);
            } else if (person instanceof PersonIso) {
                email = ((PersonIso) person).getEmail();
            }
            if (email != null && !email.isEmpty()) {
                configuration.setNotificationEmail(email);
            }
        }
    }

    private CnATreeElement createPerson(PersonInfo personInfo) {
        CnATreeElement person = personInfo.getPerson();
        Domain domain = CnATypeMapper.getDomainFromTypeId(person.getTypeId());
        CnATreeElement parent = loadContainer(person.getClass(), domain);
        person.setParentAndScope(parent);
        if (authService.isPermissionHandlingNeeded()) {
            person.setPermissions(Permission.clonePermissionSet(person, parent.getPermissions()));
        }
        setImportRootObject(parent);

        IBaseDao<CnATreeElement, Serializable> dao = getDaoFactory().getDAO(CnATreeElement.class);

        person = dao.merge(person);
        dao.flush();
        savedPersonList.add(person);
        return person;
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
     * 
     * @param clazz
     * 
     * @return
     */
    private CnATreeElement loadContainer(Class<? extends CnATreeElement> clazz, Domain domain) {
        // Create the importRootObject if it does not exist yet
        // and set the 'importRootObject' variable.
        CnATreeElement container = containerMap.get(domain);
        if (container == null) {
            LoadImportObjectsHolder cmdLoadContainer = new LoadImportObjectsHolder(clazz);
            try {

                cmdLoadContainer = getCommandService().executeCommand(cmdLoadContainer);
            } catch (CommandException e) {
                log.error("Error while loading container", e);
                throw new RuntimeCommandException("Error while loading container", e);
            }
            container = cmdLoadContainer.getHolder();
            if (container == null) {
                container = createContainer(domain);
            }
            // load the parent
            container.getParent().getTitle();
            containerMap.put(domain, container);
        }
        return container;
    }

    private CnATreeElement createContainer(Domain domain) {
        switch (domain) {
        case BASE_PROTECTION_OLD:
            return createBsiContainer();
        case ISM:
            return createIsoContainer();
        case BASE_PROTECTION:
            return createITBPContainer();
        default:
            throw new IllegalArgumentException("Unsupported domain " + domain);
        }
    }

    private CnATreeElement createBsiContainer() {
        BSIModel model = loadModel(BSIModel.class);
        try {
            ImportBsiGroup holder = new ImportBsiGroup(model);
            addDefaultPermissions(holder);
            getDaoFactory().getDAO(ImportBsiGroup.class).saveOrUpdate(holder);
            return holder;
        } catch (Exception e1) {
            throw new RuntimeCommandException(
                    "Fehler beim Anlegen des Behaelters für importierte Objekte.");
        }
    }

    private CnATreeElement createIsoContainer() {
        ISO27KModel model = loadModel(ISO27KModel.class);
        try {
            ImportIsoGroup holder = new ImportIsoGroup(model);
            addDefaultPermissions(holder);
            getDaoFactory().getDAO(ImportIsoGroup.class).saveOrUpdate(holder);
            return holder;
        } catch (Exception e1) {
            throw new RuntimeCommandException(
                    "Fehler beim Anlegen des Behälters für importierte Objekte.");
        }

    }

    private CnATreeElement createITBPContainer() {
        BpModel model = loadModel(BpModel.class);
        try {
            ImportBpGroup holder = new ImportBpGroup(model);
            addDefaultPermissions(holder);
            getDaoFactory().getDAO(ImportBpGroup.class).saveOrUpdate(holder);
            return holder;
        } catch (Exception e1) {
            throw new RuntimeCommandException(
                    "Fehler beim Anlegen des Behälters für importierte Objekte.");
        }
    }

    private <T extends CnATreeElement> T loadModel(Class<T> modelClass) {
        LoadModel<T> cmdLoadModel = new LoadModel<>(modelClass);
        try {
            cmdLoadModel = getCommandService().executeCommand(cmdLoadModel);
        } catch (CommandException e) {
            log.error("Error while loading model", e);
            throw new RuntimeCommandException("Error while loading model", e);
        }
        return cmdLoadModel.getModel();
    }

    private void addDefaultPermissions(/* not final */ CnATreeElement element) {
        String userName = authService.getUsername();
        addPermissions(element, userName);
        addPermissions(element, IRightsService.USERDEFAULTGROUPNAME);

    }

    private static void addPermissions(CnATreeElement element, String userName) {
        Set<Permission> permission = element.getPermissions();
        if (permission == null) {
            permission = new HashSet<>();
        }
        permission.add(Permission.createPermission(element, userName, true, true));
        element.setPermissions(permission);
    }

    /**
     * Checks if the username in a {@link Configuration} is unique in the
     * database. Throws {@link UsernameExistsRuntimeException} if username is
     * not available. If username is not set or null no exception is thrown
     * 
     * @param element
     *            a {@link Configuration}
     * @throws UsernameExistsRuntimeException
     *             if username is not available
     */
    private void checkUsername(String username) throws UsernameExistsRuntimeException {
        if (username == null) {
            return;
        }
        DetachedCriteria criteria = DetachedCriteria.forClass(Property.class);
        criteria.add(Restrictions.eq("propertyType", Configuration.PROP_USERNAME));
        criteria.add(Restrictions.like("propertyValue", username));
        IBaseDao<Property, Integer> dao = getDaoFactory().getDAO(Property.TYPE_ID);
        List<Property> resultList = dao.findByCriteria(criteria);

        if (resultList == null || resultList.isEmpty()) {
            return;
        }

        for (Property property : resultList) {
            // check again to exclude name which start with the same
            // characters
            if (username.equals(property.getPropertyValue())) {
                if (log.isDebugEnabled()) {
                    log.debug("Username exists: " + username);
                }
                throw new UsernameExistsRuntimeException(username,
                        "Username already exists: " + username);
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
