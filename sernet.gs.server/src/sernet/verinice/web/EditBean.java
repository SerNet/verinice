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
package sernet.verinice.web;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import org.apache.log4j.Logger;

import sernet.gs.common.ApplicationRoles;
import sernet.gs.service.RetrieveInfo;
import sernet.gs.ui.rcp.main.service.AuthenticationHelper;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadCurrentUserConfiguration;
import sernet.gs.ui.rcp.main.service.crudcommands.SaveElement;
import sernet.gs.web.ExceptionHandler;
import sernet.gs.web.SecurityException;
import sernet.gs.web.Util;
import sernet.hui.common.VeriniceContext;
import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.EntityType;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.hui.common.connect.PropertyGroup;
import sernet.hui.common.connect.PropertyType;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.Permission;
import sernet.verinice.model.common.configuration.Configuration;
import sernet.verinice.service.commands.LoadElementByUuid;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 * 
 */
public class EditBean {
    
    private static final Logger LOG = Logger.getLogger(EditBean.class);
    
    public static final String BOUNDLE_NAME = "sernet.verinice.web.EditMessages";
    
    private CnATreeElement element;
    
    private String typeId;
    
    private String uuid;
    
    private String title;
    
    public List<HuiProperty<String, String>> propertyList;
    
    public List<sernet.verinice.web.PropertyGroup> groupList;
    
    private Set<String> roles = null;
    
    private boolean generalOpen = true;
    
    private boolean groupOpen = false;
       
    public void init() {
        try {
            RetrieveInfo ri = RetrieveInfo.getPropertyInstance();
            LoadElementByUuid<CnATreeElement> command = new LoadElementByUuid<CnATreeElement>(getTypeId(),getUuid(),ri);        
            command = getCommandService().executeCommand(command);
            setElement(command.getElement());
            Entity entity = getElement().getEntity();           
            EntityType entityType = getHuiService().getEntityType(getTypeId());          
            
            groupList = new ArrayList<sernet.verinice.web.PropertyGroup>();
            List<PropertyGroup> groupListHui = entityType.getPropertyGroups();     
            for (PropertyGroup groupHui : groupListHui) {
                sernet.verinice.web.PropertyGroup group = new sernet.verinice.web.PropertyGroup(groupHui.getId(), groupHui.getName());
                List<PropertyType> typeListHui = groupHui.getPropertyTypes();
                List<HuiProperty<String, String>> listOfGroup = new ArrayList<HuiProperty<String,String>>();
                for (PropertyType huiType : typeListHui) {
                    String id = huiType.getId();
                    String value = entity.getValue(id);
                    listOfGroup.add(new HuiProperty<String, String>(huiType, id, value));
                }
                group.setPropertyList(listOfGroup);
                groupList.add(group);
            }
            
            propertyList = new ArrayList<HuiProperty<String,String>>();
            List<PropertyType> typeList = entityType.getPropertyTypes();        
            for (PropertyType propertyType : typeList) {
                String id = propertyType.getId();
                String value = entity.getValue(id);
                propertyList.add(new HuiProperty<String, String>(propertyType, id, value));
                if (LOG.isDebugEnabled()) {
                    LOG.debug("prop: " + id + " (" + propertyType.getInputName() + ") - " + value);
                }
            }          
        } catch(Throwable t) {
            LOG.error("Error while initializatio. ", t);
            Util.addError( "editPanel", Util.getMessage(BOUNDLE_NAME,"init.failed"));
        }
    }
    
    public void save() {
        LOG.debug("save called...");
        try {
            if(getElement()!=null) {
                if (!writeEnabled())
                {
                    throw new SecurityException("write is not allowed" );
                }
                Entity entity = getElement().getEntity();    
                for (HuiProperty<String, String> property : getPropertyList()) {
                    entity.setSimpleValue(property.getType(), property.getValue());
                }
                for (sernet.verinice.web.PropertyGroup group : getGroupList()) {
                    for (HuiProperty<String, String> property : group.getPropertyList()) {
                        entity.setSimpleValue(property.getType(), property.getValue());
                    }
                }
                SaveElement<CnATreeElement> command = new SaveElement<CnATreeElement>(getElement());                           
                command = getCommandService().executeCommand(command);
                setElement(command.getElement());
                if(LOG.isDebugEnabled()) {
                    LOG.debug("Element saved, uuid: " + getUuid());
                }   
                Util.addInfo("submit", Util.getMessage(EditBean.BOUNDLE_NAME, "saved"));                  
            }
            else {
                LOG.warn("Control is null. Can not save.");
            }
        } catch (Exception e) {
            LOG.error("Error while saving element, uuid: " + getUuid(), e);
            ExceptionHandler.handle(e);
        }
    }
    
    public boolean writeEnabled() {
        boolean enabled = false;
        if(getElement()!=null) {
            // causes NoClassDefFoundError: org/eclipse/ui/plugin/AbstractUIPlugin
            // FIXME: fix this dependency to eclipse related classes.
            enabled = isWriteAllowed(getElement());
        }   
        return enabled;
    }
    
    public boolean isWriteAllowed(CnATreeElement cte) {
        // Server implementation of CnAElementHome.isWriteAllowed
        try {
            // Short cut: If no permission handling is needed than all objects are
            // writable.
            if (!ServiceFactory.isPermissionHandlingNeeded()) {
                return true;
            } 
            // Short cut 2: If we are the admin, then everything is writable as
            // well.
            if (AuthenticationHelper.getInstance().currentUserHasRole(new String[] { ApplicationRoles.ROLE_ADMIN })) {
                return true;
            }   
            if (roles == null) {
                LoadCurrentUserConfiguration lcuc = new LoadCurrentUserConfiguration();       
                lcuc = getCommandService().executeCommand(lcuc);            
                Configuration c = lcuc.getConfiguration();
                // No configuration for the current user (anymore?). Then nothing is
                // writable.
                if (c == null) {
                    return false;
                }
                Set<String> roles = c.getRoles();
            }    
            for (Permission p : cte.getPermissions()) {
                if (p.isWriteAllowed() && roles.contains(p.getRole())) {
                    return true;
                }
            }
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
        return false;
    }
    
    public String getTypeId() {
        return typeId;
    }

    public CnATreeElement getElement() {
        return element;
    }

    public void setElement(CnATreeElement element) {
        this.element = element;
    }

    public void setTypeId(String typeId) {
        this.typeId = typeId;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
    
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<HuiProperty<String, String>> getPropertyList() {
        if(propertyList==null) {
            propertyList = Collections.emptyList();
        }
        return propertyList;
    }

    public void setPropertyList(List<HuiProperty<String, String>> properties) {
        this.propertyList = properties;
    }
    
    public List<sernet.verinice.web.PropertyGroup> getGroupList() {
        return groupList;
    }

    public void setGroupList(List<sernet.verinice.web.PropertyGroup> groupList) {
        this.groupList = groupList;
    }

    public boolean isGeneralOpen() {
        return generalOpen;
    }

    public void setGeneralOpen(boolean generalOpen) {
        this.generalOpen = generalOpen;
    }

    public boolean isGroupOpen() {
        return groupOpen;
    }

    public void setGroupOpen(boolean groupOpen) {
        this.groupOpen = groupOpen;
    }

    public TimeZone getTimeZone() {
        return TimeZone.getDefault();
    }

    private HUITypeFactory getHuiService() {
        return (HUITypeFactory) VeriniceContext.get(VeriniceContext.HUI_TYPE_FACTORY);
    }
    
    private ICommandService getCommandService() {
        return (ICommandService) VeriniceContext.get(VeriniceContext.COMMAND_SERVICE);
    }
}
