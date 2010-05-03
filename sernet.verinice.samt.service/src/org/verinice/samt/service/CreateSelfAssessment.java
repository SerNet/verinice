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
package org.verinice.samt.service;

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

import sernet.gs.ui.rcp.main.common.model.ChangeLogEntry;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.common.model.Permission;
import sernet.gs.ui.rcp.main.connect.IBaseDao;
import sernet.gs.ui.rcp.main.service.IAuthService;
import sernet.gs.ui.rcp.main.service.commands.CommandException;
import sernet.gs.ui.rcp.main.service.commands.GenericCommand;
import sernet.gs.ui.rcp.main.service.commands.IAuthAwareCommand;
import sernet.gs.ui.rcp.main.service.commands.IChangeLoggingCommand;
import sernet.gs.ui.rcp.main.service.commands.RuntimeCommandException;
import sernet.verinice.iso27k.model.ControlGroup;
import sernet.verinice.iso27k.model.ISO27KModel;
import sernet.verinice.iso27k.model.Organization;
import sernet.verinice.iso27k.service.IItem;
import sernet.verinice.iso27k.service.ItemControlTransformer;
import sernet.verinice.iso27k.service.commands.CsvFile;
import sernet.verinice.iso27k.service.commands.ImportCatalog;

/**
 * @author Daniel Murygin <dm@sernet.de> // TODO dm: Externalize Strings
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
    private String title;
    private ISO27KModel model;
    private Organization selfAssessment;
    private String stationId;

    private transient IAuthService authService;

    public CreateSelfAssessment(ISO27KModel model, String title) {
        super();
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
            if (title != null) {
                selfAssessment.setTitel(title);
            }
            model.addChild(selfAssessment);

            // We use the name of the currently
            // logged in user as a role which has read and write permissions for
            // the new Organization.
            HashSet<Permission> newperms = new HashSet<Permission>();
            newperms.add(Permission.createPermission(selfAssessment, authService.getUsername(), true, true));
            selfAssessment.setPermissions(newperms);

            // read the control items from a the csv file
            Collection<IItem> itemCollection = getItemCollection();
            ControlGroup controlGroup = getControlGroup(selfAssessment);
            importCatalogItems(controlGroup, itemCollection);

            IBaseDao<Organization, Serializable> dao = getDaoFactory().getDAO(Organization.class);
            dao.saveOrUpdate(selfAssessment);
        } catch (Exception e) {
            getLog().error("Error while creating self assesment", e);
            throw new RuntimeCommandException("Error while creating self assesment: " + e.getMessage());
        }
    }

    private void importCatalogItems(CnATreeElement group, Collection<IItem> itemCollection) {
        for (Iterator<IItem> iterator = itemCollection.iterator(); iterator.hasNext();) {
            IItem item = iterator.next();
            CnATreeElement element = null;
            if (item.getItems() != null && item.getItems().size() > 0) {
                // create a group
                element = ItemControlTransformer.transformToGroup(item);
                importCatalogItems(element, item.getItems());
            } else {
                // create an element
                element = ItemControlTransformer.transform(item);
            }
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
            String relativePath = "resources/add/real/path/to/samt-catalog.csv";
            InputStream is = this.getClass().getClassLoader().getResourceAsStream(relativePath);
            if (is == null) {
                throw new FileNotFoundException("Relative path: " + relativePath + " not found by ..getClassLoader().getResource(..)");
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
    private ControlGroup getControlGroup(CnATreeElement selfAssessment) {
        ControlGroup controlGroup = null;
        Set<CnATreeElement> elementSet = selfAssessment.getChildren();
        for (Iterator iterator = elementSet.iterator(); iterator.hasNext();) {
            CnATreeElement element = (CnATreeElement) iterator.next();
            if (element instanceof ControlGroup) {
                controlGroup = (ControlGroup) element;
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
