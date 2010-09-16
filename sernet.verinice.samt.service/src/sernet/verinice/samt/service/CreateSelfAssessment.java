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

import java.awt.image.TileObserver;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import sernet.gs.service.CsvFile;
import sernet.gs.service.RuntimeCommandException;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
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
public class CreateSelfAssessment extends GenericCommand implements IChangeLoggingCommand, IAuthAwareCommand {

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
    private Organization selfAssessment;
    private String stationId;

    private transient IAuthService authService;

    public CreateSelfAssessment(ISO27KModel model, String titleOrganization, String title) {
        super();
        this.titleOrganization = titleOrganization;
        this.title = title;
        this.model = model;
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
            selfAssessment = new Organization(model);
            if (titleOrganization != null) {
                selfAssessment.setTitel(titleOrganization);
            }
            model.addChild(selfAssessment);

            // We use the name of the currently
            // logged in user as a role which has read and write permissions for
            // the new Organization.
            HashSet<Permission> newperms = new HashSet<Permission>();
            newperms.add(Permission.createPermission(selfAssessment, authService.getUsername(), true, true));
            selfAssessment.setPermissions(newperms);
            
            // Create the audit add it to organization
            AuditGroup auditGroup = getAuditGroup(selfAssessment);
            Audit audit = new Audit(auditGroup, true);
            if (title != null) {
                audit.setTitel(title);
            }
            HashSet<Permission> auditPerms = new HashSet<Permission>();
            auditPerms.add(Permission.createPermission(audit, authService.getUsername(), true, true));
            audit.setPermissions(auditPerms);
            auditGroup.addChild(audit);
            
            // read the control items from the csv file
            Collection<IItem> itemCollection = getItemCollection();
            ControlGroup controlGroup = getControlGroup(audit);
            
            // We use the name of the currently
            // logged in user as a role which has read and write permissions for
            // the new element.
            HashSet<Permission> permissionSet = new HashSet<Permission>();
            permissionSet.add(Permission.createPermission(controlGroup, authService.getUsername(), true, true));
            controlGroup.setPermissions(permissionSet);
            
            // convert catalog items to self assessment topics (class: SamtTopic)
            importCatalogItems(controlGroup, itemCollection);

            IBaseDao<Organization, Serializable> dao = getDaoFactory().getDAO(Organization.class);
            dao.saveOrUpdate(selfAssessment);
            
            // Link all controls of audit to audit
            IBaseDao<CnALink, Serializable> daoLink = getDaoFactory().getDAO(CnALink.class);
            
            CnALink link = new CnALink(selfAssessment,audit,"rel_org_audit",null);
            selfAssessment.addLinkDown(link);
            audit.addLinkUp(link);
            daoLink.saveOrUpdate(link);
            
            Set<CnATreeElement> isaCategories = controlGroup.getChildren();
            for (CnATreeElement categorie : isaCategories) {
                link = new CnALink(audit,categorie,"rel_audit_control",null);
                audit.addLinkDown(link);
                categorie.addLinkUp(link);
                daoLink.saveOrUpdate(link);
            }
        } catch (Exception e) {
            getLog().error("Error while creating self assesment", e); //$NON-NLS-1$
            throw new RuntimeCommandException("Error while creating self assesment: " + e.getMessage()); //$NON-NLS-1$
        }
    }

    private void importCatalogItems(CnATreeElement group, Collection<IItem> itemCollection) {
        for (Iterator<IItem> iterator = itemCollection.iterator(); iterator.hasNext();) {
            IItem item = iterator.next();
            CnATreeElement element = null;
            if (item.getItems() != null && item.getItems().size() > 0) {
                // create a group
                element = ItemControlTransformer.transformToGroup(item, new ControlGroup());
                importCatalogItems(element, item.getItems());
            } else {
                // create an element
                element = ItemControlTransformer.transformGeneric(item, new SamtTopic());
            }
            
            // We use the name of the currently
            // logged in user as a role which has read and write permissions for
            // the new element.
            HashSet<Permission> permissionSet = new HashSet<Permission>();
            permissionSet.add(Permission.createPermission(element, authService.getUsername(), true, true));
            element.setPermissions(permissionSet);
            
            group.addChild(element);
            element.setParent(group);
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
    private Collection<IItem> getItemCollection() throws FileNotFoundException, IOException, CommandException {
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
        Collection<IItem> itemCollection = catalogCommand.getCatalog().getRoot().getItems();
        return itemCollection;
    }

    /**
     * @param selfAssessment2
     * @return
     */
    private AuditGroup getAuditGroup(CnATreeElement selfAssessment) {
        AuditGroup auditGroup = null;
        Set<CnATreeElement> elementSet = selfAssessment.getChildren();
        for (Iterator<CnATreeElement> iterator = elementSet.iterator(); iterator.hasNext();) {
            CnATreeElement element = iterator.next();
            if (element instanceof AuditGroup) {
                auditGroup = (AuditGroup) element;
                break;
            }
        }
        return auditGroup;
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

    public Organization getSelfAssessment() {
        return selfAssessment;
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
        return Arrays.asList((CnATreeElement) selfAssessment);
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
