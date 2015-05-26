/*******************************************************************************
 * Copyright (c) 2010 Daniel Murygin.
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
package sernet.verinice.samt.service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import sernet.gs.service.CsvFile;
import sernet.gs.service.RuntimeCommandException;
import sernet.hui.common.connect.HitroUtil;
import sernet.verinice.interfaces.ChangeLoggingCommand;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.IAuthAwareCommand;
import sernet.verinice.interfaces.IAuthService;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.IChangeLoggingCommand;
import sernet.verinice.interfaces.iso27k.IItem;
import sernet.verinice.model.common.ChangeLogEntry;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.Permission;
import sernet.verinice.model.iso27k.Audit;
import sernet.verinice.model.iso27k.AuditGroup;
import sernet.verinice.model.iso27k.ControlGroup;
import sernet.verinice.model.iso27k.ISO27KModel;
import sernet.verinice.model.iso27k.Organization;
import sernet.verinice.model.samt.SamtTopic;
import sernet.verinice.service.commands.CreateElement;
import sernet.verinice.service.iso27k.ImportCatalog;
import sernet.verinice.service.iso27k.ItemControlTransformer;

/**
 * Creates a new self-assessment (SAMT).
 * A SAMT is an ISO-27000 organization with controls  
 * divided into several groups.
 * 
 * Controls are imported from a CSV-file while creating the SAMT.
 * See wiki for more information about the CSV file:
 * http://www.verinice.org/priv/mediawiki-1.6.12/index.php/Control_Import
 * 
 * @author Daniel Murygin <dm@sernet.de>
 */
public class CreateSelfAssessment extends ChangeLoggingCommand implements IChangeLoggingCommand, IAuthAwareCommand {

    private transient Logger log = Logger.getLogger(CreateSelfAssessment.class);

    public Logger getLog() {
        if (log == null) {
            log = Logger.getLogger(CreateSelfAssessment.class);
        }
        return log;
    }

    private CsvFile csvFile;
    private String titleOrganization;
    private String title;
    private ISO27KModel model;
    private Organization organization;
    private AuditGroup auditGroup;
    private Audit isaAudit;
    private String stationId;
    private List<CnATreeElement> changedElements;

    private transient IAuthService authService;

    public CreateSelfAssessment(ISO27KModel model, String titleOrganization, String title) {
        super();
        this.titleOrganization = titleOrganization;
        this.title = title;
        this.model = model;
        this.stationId = ChangeLogEntry.STATION_ID;
    }

    /**
     * @param parent
     * @param titelOrganization
     * @param titel
     */
    public CreateSelfAssessment(AuditGroup auditGroup, String titleOrganization, String title) {
        super();
        this.titleOrganization = titleOrganization;
        this.title = title;
        this.auditGroup = auditGroup;
        this.stationId = ChangeLogEntry.STATION_ID;
    }

    /*
     * (non-Javadoc)
     * 
     * @see sernet.gs.ui.rcp.main.service.commands.ICommand#execute()
     */
    @Override
    public void execute() {
        try {
            changedElements = new ArrayList<CnATreeElement>();
            if(auditGroup==null) { 
                organization = saveNewOrganisation(model, titleOrganization);                           
                changedElements.add(organization);
                
                // Create the audit add it to organization
                auditGroup = getAuditGroup(organization);
                changedElements.add(auditGroup);
            }
            
            isaAudit = new Audit(auditGroup, true);
            if (title != null) {
                isaAudit.setTitel(title);
            }
            addPermissions(isaAudit);
            auditGroup.addChild(isaAudit);
            IBaseDao<Audit, Serializable> auditDao = getDaoFactory().getDAO(Audit.class);
            auditDao.saveOrUpdate(isaAudit);
            
            changedElements.add(isaAudit);
            changedElements.addAll(isaAudit.getChildren());
            
            // read the control items from the csv file
            Collection<IItem> itemCollection = getItemCollection();
            ControlGroup controlGroup = getControlGroup(isaAudit);
            addPermissions(controlGroup);
            
            // convert catalog items to self assessment topics (class: SamtTopic)
            importCatalogItems(controlGroup, itemCollection);
            IBaseDao<Organization, Serializable> orgDao = getDaoFactory().getDAO(Organization.class);
            
            if(organization!=null) {
                orgDao.saveOrUpdate(organization);
            } else {
                IBaseDao<Audit, Serializable> dao = getDaoFactory().getDAO(Audit.class);
                dao.saveOrUpdate(isaAudit);
                organization = findOrganization(isaAudit);
                organization = orgDao.findById(organization.getDbId());
            }
            
            // Link all controls of audit to audit
            IBaseDao<CnALink, Serializable> daoLink = getDaoFactory().getDAO(CnALink.class);
            
            auditDao.flush();
            
            CnALink link = new CnALink(organization,isaAudit,"rel_org_audit",null);
            organization.addLinkDown(link);
            isaAudit.addLinkUp(link);
            daoLink.saveOrUpdate(link);
            
            Set<CnATreeElement> isaCategories = controlGroup.getChildren();
            for (CnATreeElement categorie : isaCategories) {
                link = new CnALink(isaAudit,categorie,"rel_audit_control",null);
                isaAudit.addLinkDown(link);
                categorie.addLinkUp(link);
                daoLink.saveOrUpdate(link);
            }
        } catch (CommandException e) {
            getLog().error("Error while creating self assesment", e); //$NON-NLS-1$
            throw new RuntimeCommandException("Error while creating self assesment: " + e.getMessage()); //$NON-NLS-1$
        } catch (IOException e){
            getLog().error("I-/O-Error while creating self assesment", e); //$NON-NLS-1$
            throw new RuntimeCommandException("I-/O-Error while creating self assesment: " + e.getMessage()); //$NON-NLS-1$
        }
    }
    
    public Organization saveNewOrganisation(CnATreeElement container, String title) throws CommandException {
        String title0 = (title != null) ? title : null;
        if(title0==null) {
            title0 = HitroUtil.getInstance().getTypeFactory().getMessage(Organization.TYPE_ID);   
        }
        CreateElement<Organization> saveCommand = new CreateElement<Organization>(container, Organization.class, title0, false, true);
        saveCommand = getCommandService().executeCommand(saveCommand);
        Organization child = saveCommand.getNewElement();
        container.addChild(child);
        child.setParentAndScope(container);
        return child;
    }

    /**
     * @param isaAudit2
     * @return
     */
    private Organization findOrganization(CnATreeElement element) {
        CnATreeElement parent = element.getParent();
        if(parent instanceof Organization) {
            return (Organization) parent;
        } else if(parent!=null) {
            return findOrganization(parent);          
        } else {
            return null;
        }
    }

    private void addPermissions(CnATreeElement element) {
        // We use the name of the currently
        // logged in user as a role which has read and write permissions for
        // the new Organization.
        HashSet<Permission> auditPerms = new HashSet<Permission>();
        auditPerms.add(Permission.createPermission(element, authService.getUsername(), true, true));
        element.setPermissions(auditPerms);
    }

    private void importCatalogItems(CnATreeElement group, Collection<IItem> itemCollection) {
        for (Iterator<IItem> iterator = itemCollection.iterator(); iterator.hasNext();) {
            IItem item = iterator.next();
            CnATreeElement element = null;
            if (item.getItems() != null && item.getItems().size() > 0) {
                // create a group
                element = ItemControlTransformer.transformToGroup(item, new ControlGroup());
                element.setParentAndScope(group);
                importCatalogItems(element, item.getItems());
            } else {
                // create an element
                element = ItemControlTransformer.transformGeneric(item, new SamtTopic());
                element.setParentAndScope(group);
            }
            addPermissions(element);             
            group.addChild(element);
            changedElements.add(element);
        }
    }

    /**
     * Imports the control items from a the CSV file
     * 
     * @return A collection of control items
     * @throws FileNotFoundException
     *             if CSV file can not be found
     * @throws IOException
     *             if reading of the CSV file fails
     * @throws CommandException
     *             if executing of CSV import command fails
     */
    private Collection<IItem> getItemCollection() throws IOException, CommandException {
        // if csvFile was not passed to this command as parameter, read it here
        if (csvFile == null) {
            // read the CSV file which contains the self assessment controls
            String relativePath = "resources/add/real/path/to/samt-catalog.csv"; //$NON-NLS-1$
            InputStream is = this.getClass().getClassLoader().getResourceAsStream(relativePath);
            if (is == null) {
                throw new FileNotFoundException("Relative path: " + relativePath + " not found by ..getClassLoader().getResource(..)"); //$NON-NLS-1$ //$NON-NLS-2$
            }
            csvFile = new CsvFile(is);
        }
        ImportCatalog catalogCommand = new ImportCatalog(csvFile.getFileContent());
        catalogCommand = getCommandService().executeCommand(catalogCommand);
        return catalogCommand.getCatalog().getRoot().getItems();
    }

    /**
     * @param selfAssessment2
     * @return
     */
    private AuditGroup getAuditGroup(CnATreeElement selfAssessment) {
        AuditGroup auditGroup0 = null;
        Set<CnATreeElement> elementSet = selfAssessment.getChildren();
        for (Iterator<CnATreeElement> iterator = elementSet.iterator(); iterator.hasNext();) {
            CnATreeElement element = iterator.next();
            if (element instanceof AuditGroup) {
                auditGroup0 = (AuditGroup) element;
                break;
            }
        }
        return auditGroup0;
    }
    
    /**
     * @param selfAssessment2
     * @return
     */
    private ControlGroup getControlGroup(CnATreeElement selfAssessment) {
        ControlGroup controlGroup = null;
        Set<CnATreeElement> elementSet = selfAssessment.getChildren();
        for (Iterator<CnATreeElement> iterator = elementSet.iterator(); iterator.hasNext();) {
            CnATreeElement element = iterator.next();
            if (element instanceof ControlGroup) {
                controlGroup = (ControlGroup) element;
                break;
            }
        }
        return controlGroup;
    }

    public ISO27KModel getModel() {
        return model;
    }

    public Organization getOrganization() {
        return organization;
    }
    
    public AuditGroup getAuditGroup() {
        return auditGroup;
    }
    
    public Audit getIsaAudit() {
        return isaAudit;
    }

    public void setCsvFile(CsvFile csvFile) {
        this.csvFile = csvFile;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * sernet.gs.ui.rcp.main.service.commands.IChangeLoggingCommand#getChangeType
     * ()
     */
    @Override
    public int getChangeType() {
        return ChangeLogEntry.TYPE_INSERT;
    }

    /*
     * (non-Javadoc)
     * 
     * @seesernet.gs.ui.rcp.main.service.commands.IChangeLoggingCommand#
     * getChangedElements()
     */
    @Override
    public List<CnATreeElement> getChangedElements() {
        return changedElements;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * sernet.gs.ui.rcp.main.service.commands.IChangeLoggingCommand#getStationId
     * ()
     */
    @Override
    public String getStationId() {
        return stationId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * sernet.gs.ui.rcp.main.service.commands.IAuthAwareCommand#getAuthService()
     */
    @Override
    public IAuthService getAuthService() {
        return authService;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * sernet.gs.ui.rcp.main.service.commands.IAuthAwareCommand#setAuthService
     * (sernet.gs.ui.rcp.main.service.IAuthService)
     */
    @Override
    public void setAuthService(IAuthService service) {
        this.authService = service;
    }

}
