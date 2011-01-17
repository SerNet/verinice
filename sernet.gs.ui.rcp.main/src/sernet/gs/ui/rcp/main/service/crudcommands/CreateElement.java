/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak[at]sernet[dot]de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *     You should have received a copy of the GNU Lesser General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Alexander Koderman <ak[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.service.crudcommands;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import sernet.gs.service.RuntimeCommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IAuthAwareCommand;
import sernet.verinice.interfaces.IAuthService;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.IChangeLoggingCommand;
import sernet.verinice.model.bsi.ITVerbund;
import sernet.verinice.model.common.ChangeLogEntry;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.Permission;
import sernet.verinice.model.iso27k.Organization;

/**
 * Create and save new element of clazz clazz to the database using its class to
 * lookup the DAO from the factory.
 * 
 * @author koderman[at]sernet[dot]de
 * @version $Rev$ $LastChangedDate$ $LastChangedBy$
 * 
 * @param <T>
 */
@SuppressWarnings("serial")
public class CreateElement<T extends CnATreeElement> extends GenericCommand implements IChangeLoggingCommand, IAuthAwareCommand {

    transient private Logger log = Logger.getLogger(CreateElement.class);

    private CnATreeElement container;
    private Class<T> clazz;
    // may be null
    private String title;
    protected T child;
    private String stationId;

    private transient IAuthService authService;

    private boolean skipReload;
    
    protected boolean createChildren;

    /**
     * @param container2
     * @param clazz
     * @param typeId
     */
    public CreateElement(CnATreeElement container, Class<T> clazz, String title, boolean skipReload, boolean createChildren) {
        this.container = container;
        this.clazz = clazz;
        this.title = title;
        this.stationId = ChangeLogEntry.STATION_ID;
        this.skipReload = skipReload;
        this.createChildren = createChildren;
    }

    public CreateElement(CnATreeElement container, Class<T> type, String title) {
        this(container, type, title, false, true);
    }

    public CreateElement(CnATreeElement container, Class<T> type) {
        this(container, type, null, false, true);
    }

    public CreateElement(CnATreeElement container, Class<T> type, boolean skipReload) {
        this(container, type, null, skipReload, true);
    }
    
    public CreateElement(CnATreeElement container, Class<T> type, boolean skipReload, boolean createChildren) {
        this(container, type, null, skipReload, createChildren);
    }

    public void execute() {
        IBaseDao<T, Serializable> dao = (IBaseDao<T, Serializable>) getDaoFactory().getDAO(clazz);
        IBaseDao<Object, Serializable> containerDAO = getDaoFactory().getDAOforTypedElement(container);

        try {
            if (!skipReload)
                containerDAO.reload(container, container.getDbId());

            // get constructor with parent-parameter and create new object:
            if(clazz.equals(Organization.class)) {
                child = clazz.getConstructor(CnATreeElement.class,boolean.class).newInstance(container,createChildren);
            } else {
                child = clazz.getConstructor(CnATreeElement.class).newInstance(container);
            }
            if (title != null) {
                // override the default title
                child.setTitel(title);
            }

            if (authService.isPermissionHandlingNeeded()) {
                addPermissions();
            }

            child = dao.merge(child, false);
            container.addChild(child);
            child.setParent(container);

            // initialize UUID, used to find container in display in views:
            container.getUuid();
        } catch (Exception e) {
            getLogger().error("Error while creating element", e);
            throw new RuntimeCommandException(e);
        }
    }

    private void addPermissions() {
        // By default, inherit permissions from parent element but ITVerbund
        // instances cannot do this, as its parents (BSIModel) is not visible
        // and has no permissions. Therefore we use the name of the currently
        // logged in user as a role which has read and write permissions for
        // the new ITVerbund.
        if (child instanceof ITVerbund || child instanceof Organization) {
            addPermissions(child);           
        } else {
            child.setPermissions(Permission.clonePermissionSet(child, container.getPermissions()));
        }
    }
    
    private void addPermissions(/*not final*/ CnATreeElement element) {
        HashSet<Permission> newperms = new HashSet<Permission>();
        newperms.add(Permission.createPermission(element, authService.getUsername(), true, true));
        element.setPermissions(newperms);
        for (CnATreeElement child : element.getChildren()) {
            addPermissions(child);
        }
    }

    public T getNewElement() {
        return child;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * sernet.gs.ui.rcp.main.service.commands.IChangeLoggingCommand#getChangeType
     * ()
     */
    public int getChangeType() {
        return ChangeLogEntry.TYPE_INSERT;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * sernet.gs.ui.rcp.main.service.commands.IChangeLoggingCommand#getStationId
     * ()
     */
    public String getStationId() {
        return stationId;
    }

    /*
     * (non-Javadoc)
     * 
     * @seesernet.gs.ui.rcp.main.service.commands.IChangeLoggingCommand#
     * getChangedElements()
     */
    public List<CnATreeElement> getChangedElements() {
        ArrayList<CnATreeElement> result = new ArrayList<CnATreeElement>(1);
        result.add(child);
        return result;
    }

    public IAuthService getAuthService() {
        return authService;
    }

    public void setAuthService(IAuthService service) {
        this.authService = service;
    }

    private Logger getLogger() {
        if (log == null) {
            log = Logger.getLogger(CreateElement.class);

        }
        return log;
    }

}
