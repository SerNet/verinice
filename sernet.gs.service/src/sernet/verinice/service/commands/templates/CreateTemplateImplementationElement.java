/*******************************************************************************  
 * Copyright (c) 2017 Viktor Schmidt.  
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
 *     Viktor Schmidt <vschmidt[at]ckc[dot]de> - initial API and implementation  
 ******************************************************************************/
package sernet.verinice.service.commands.templates;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.log4j.Logger;

import sernet.gs.service.RetrieveInfo;
import sernet.gs.service.RuntimeCommandException;
import sernet.verinice.interfaces.ChangeLoggingCommand;
import sernet.verinice.interfaces.IAuthAwareCommand;
import sernet.verinice.interfaces.IAuthService;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.IChangeLoggingCommand;
import sernet.verinice.model.bsi.ITVerbund;
import sernet.verinice.model.common.ChangeLogEntry;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.Permission;
import sernet.verinice.model.iso27k.Audit;
import sernet.verinice.model.iso27k.Organization;
import sernet.verinice.service.commands.CnATypeMapper;

/**
 * Create and save new template implementation element of Class<T> to the
 * database using its class to lookup the DAO from the factory.
 * 
 * @author Viktor Schmidt <vschmidt[at]ckc[dot]de>
 */
@SuppressWarnings("serial")
public class CreateTemplateImplementationElement<T extends CnATreeElement> extends ChangeLoggingCommand implements IChangeLoggingCommand, IAuthAwareCommand {

    private transient Logger log = Logger.getLogger(CreateTemplateImplementationElement.class);

    protected CnATreeElement container;
    private Class<T> clazz;
    private String typeId;
    private String title;
    protected T element;
    private String stationId;

    private transient IAuthService authService;
    private transient IBaseDao<T, Serializable> dao;
    private transient IBaseDao<CnATreeElement, Serializable> containerDAO;

    private boolean skipReload;
    protected boolean createChildren;
    protected boolean inheritAuditPermissions = false;

    public CreateTemplateImplementationElement(CnATreeElement container, Class<T> clazz, String title, boolean skipReload, boolean createChildren) {
        this.container = container;
        this.clazz = clazz;
        this.title = title;
        this.stationId = ChangeLogEntry.STATION_ID;
        this.skipReload = skipReload;
        this.createChildren = createChildren;
    }

    @Override
    public void execute() {
        try {
            if (clazz == null) {
                clazz = CnATypeMapper.getClassFromTypeId(typeId);
            }

            if (!skipReload && !getContainerDAO().contains(container)) {
                getContainerDAO().reload(container, container.getDbId());
            }
            element = createInstance();
            if (authService.isPermissionHandlingNeeded()) {
                element = addPermissions(element);
            }
            // Template implementations do copy the entity of template element
            // {@see CopyTemplateCommand.saveCopy}
            element.setEntity(null);
            element = saveElement();
        } catch (Exception e) {
            getLogger().error("Error while creating element", e);
            throw new RuntimeCommandException(e);
        }
    }

    protected T saveElement() {
        element = getDao().merge(element, false);
        container.addChild(element);
        element.setParentAndScope(container);

        return element;
    }

    protected T createInstance() throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        T instance = clazz.getConstructor(CnATreeElement.class).newInstance(container);

        if (title != null) {
            // override the default title
            instance.setTitel(title);
        }
        return instance;
    }

    protected T addPermissions(/* not final */ T pElement) {
        // By default, inherit permissions from parent element but ITVerbund
        // instances cannot do this, as its parents (BSIModel) is not visible
        // and has no permissions. Therefore we use the name of the currently
        // logged in user as a role which has read and write permissions for
        // the new ITVerbund.
        if (pElement instanceof ITVerbund || pElement instanceof Organization) {
            addPermissionsForScope(pElement);
        } else if (pElement instanceof Audit && isInheritAuditPermissions()) {
            addPermissionsForAudit((Audit) pElement);
        } else {
            RetrieveInfo ri = new RetrieveInfo();
            ri.setPermissions(true);
            CnATreeElement elementPerm = getContainerDAO().retrieve(container.getDbId(), ri);
            pElement.setPermissions(Permission.clonePermissionSet(pElement, elementPerm.getPermissions()));
        }
        return pElement;
    }

    protected void addPermissionsForScope(/* not final */ T pElement) {
        HashSet<Permission> newperms = new HashSet<Permission>();
        newperms.add(Permission.createPermission(pElement, authService.getUsername(), true, true));
        pElement.setPermissions(newperms);
        for (CnATreeElement child : pElement.getChildren()) {
            addPermissionsForScope((T) child);
        }
    }

    protected void addPermissionsForAudit(/* not final */ Audit audit) {
        HashSet<Permission> newperms = new HashSet<Permission>();
        RetrieveInfo ri = new RetrieveInfo();
        ri.setPermissions(true);
        CnATreeElement containerWithPerm = getContainerDAO().retrieve(container.getDbId(), ri);
        newperms.addAll(Permission.clonePermissionSet(audit, containerWithPerm.getPermissions()));
        audit.setPermissions(newperms);
        for (CnATreeElement child : audit.getChildren()) {
            addPermissions((T) child);
        }
    }

    public T getCreatedElement() {
        return element;
    }

    /*
     * @see sernet.gs.ui.rcp.main.service.commands.IChangeLoggingCommand#
     * getChangeType()
     */
    @Override
    public int getChangeType() {
        return ChangeLogEntry.TYPE_INSERT;
    }

    /*
     * @see
     * sernet.gs.ui.rcp.main.service.commands.IChangeLoggingCommand#getStationId
     * ()
     */
    @Override
    public String getStationId() {
        return stationId;
    }

    public String getTypeId() {
        return typeId;
    }

    public void setTypeId(String typeId) {
        this.typeId = typeId;
    }

    /*
     * @see sernet.gs.ui.rcp.main.service.commands.IChangeLoggingCommand#
     * getChangedElements()
     */
    @Override
    public List<CnATreeElement> getChangedElements() {
        ArrayList<CnATreeElement> result = new ArrayList<CnATreeElement>(1);
        result.add(element);
        return result;
    }

    @Override
    public IAuthService getAuthService() {
        return authService;
    }

    @Override
    public void setAuthService(IAuthService service) {
        this.authService = service;
    }

    public IBaseDao<T, Serializable> getDao() {
        if (dao == null) {
            dao = getDaoFactory().getDAOforTypedElement(element);
        }
        return dao;
    }

    public IBaseDao<CnATreeElement, Serializable> getContainerDAO() {
        if (containerDAO == null) {
            containerDAO = getDaoFactory().getDAOforTypedElement(container);
        }
        return containerDAO;
    }

    /**
     * @return the inheritAuditPermissions
     */
    public boolean isInheritAuditPermissions() {
        return inheritAuditPermissions;
    }

    /**
     * @param inheritAuditPermissions
     *            the inheritAuditPermissions to set
     */
    public void setInheritAuditPermissions(boolean inheritAuditPermissions) {
        this.inheritAuditPermissions = inheritAuditPermissions;
    }

    private Logger getLogger() {
        if (log == null) {
            log = Logger.getLogger(CreateTemplateImplementationElement.class);

        }
        return log;
    }

}
