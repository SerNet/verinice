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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TimeZone;

import org.apache.log4j.Logger;

import sernet.gs.common.ApplicationRoles;
import sernet.gs.service.RetrieveInfo;
import sernet.gs.service.TimeFormatter;
import sernet.gs.ui.rcp.main.service.AuthenticationHelper;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadCurrentUserConfiguration;
import sernet.gs.web.SecurityException;
import sernet.gs.web.Util;
import sernet.hui.common.VeriniceContext;
import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.EntityType;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.hui.common.connect.PropertyGroup;
import sernet.hui.common.connect.PropertyType;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.Permission;
import sernet.verinice.model.common.configuration.Configuration;
import sernet.verinice.service.commands.LoadElementByUuid;
import sernet.verinice.service.commands.SaveElement;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 * 
 */
public class EditBean {
    
    private static final Logger LOG = Logger.getLogger(EditBean.class);
    
    public static final String BOUNDLE_NAME = "sernet.verinice.web.EditMessages";

    public static final String TAG_WEB = "Web";
    
    public static final String TAG_ALL = "ALL-TAGS-VISIBLE";
    
    private static final String SUBMIT = "submit";
    
    private LinkBean linkBean;
    
    private AttachmentBean attachmentBean;
    
    private CnATreeElement element;
    
    private EntityType entityType;
    
    private String typeId;
    
    private String uuid;
    
    private String title;
    
    private List<HuiProperty<String, String>> propertyList;
    
    private List<sernet.verinice.web.PropertyGroup> groupList;
    
    private List<String> noLabelTypeList = new LinkedList<String>();
    
    private Set<String> roles = null;
    
    private List<IActionHandler> actionHandler;
    
    private List<IChangeListener> changeListener;
    
    private boolean generalOpen = true;
    
    private boolean groupOpen = false;
    
    private boolean linkCollapsed = true;
    
    private boolean attachmentOpen = true;
    
    private boolean saveButtonHidden = false;
    
    private List<String> visibleTags = Arrays.asList(TAG_ALL);
    
    private Set<String> visiblePropertyIds = new HashSet<String>(); 
    
    private String saveMessage = null;
       
    public void init() {
        long start = 0;
        if (LOG.isDebugEnabled()) {
            start = System.currentTimeMillis();
            LOG.debug("init() called ..."); //$NON-NLS-1$
        }
        try {
            doInit();
        } catch(CommandException t) {
            LOG.error("Error while initialization. ", t);
            Util.addError( "massagePanel", Util.getMessage(BOUNDLE_NAME,"init.failed"));
        }
        if (LOG.isDebugEnabled()) {
            long duration = System.currentTimeMillis() - start;
            LOG.debug("init() finished in: " + TimeFormatter.getHumanRedableTime(duration)); //$NON-NLS-1$
        }
    }
    
    private void doInit() throws CommandException {
        RetrieveInfo ri = RetrieveInfo.getPropertyInstance();
        ri.setPermissions(true);
        LoadElementByUuid<CnATreeElement> command = new LoadElementByUuid<CnATreeElement>(getTypeId(),getUuid(),ri);        
        command = getCommandService().executeCommand(command);    
        setElement(command.getElement());
        
        if(getElement()!=null) {
            Entity entity = getElement().getEntity();           
            setEntityType(getHuiService().getEntityType(getTypeId())); 
            
            getLinkBean().setElement(getElement());
            getLinkBean().setEntityType(getEntityType());
            getLinkBean().setTypeId(getTypeId());
            getLinkBean().reset();
            if(!(getLinkCollapsed())) {
                getLinkBean().init();
            } 
            
            getAttachmentBean().setElement(getElement());
            getAttachmentBean().init();
            
            groupList = new ArrayList<sernet.verinice.web.PropertyGroup>();
            List<PropertyGroup> groupListHui = entityType.getPropertyGroups();     
            for (PropertyGroup groupHui : groupListHui) {
                if(isVisible(groupHui)) {
                    sernet.verinice.web.PropertyGroup group = new sernet.verinice.web.PropertyGroup(groupHui.getId(), groupHui.getName());
                    List<PropertyType> typeListHui = groupHui.getPropertyTypes();
                    List<HuiProperty<String, String>> listOfGroup = new ArrayList<HuiProperty<String,String>>();
                    for (PropertyType huiType : typeListHui) {
                        if(isVisible(huiType)) {
                            String id = huiType.getId();
                            String value = entity.getValue(id);
                            HuiProperty<String, String> prop = new HuiProperty<String, String>(huiType, id, value);
                            if(getNoLabelTypeList().contains(id)) {
                                prop.setShowLabel(false);
                            }
                            listOfGroup.add(prop);
                            if (LOG.isDebugEnabled()) {
                                LOG.debug("prop: " + id + " (" + huiType.getInputName() + ") - " + value);
                            }
                        }
                    }
                    group.setPropertyList(listOfGroup);
                    groupList.add(group);
                }
            }              
            propertyList = new ArrayList<HuiProperty<String,String>>();
            List<PropertyType> typeList = entityType.getPropertyTypes();        
            for (PropertyType propertyType : typeList) {
                if(isVisible(propertyType)) {
                    String id = propertyType.getId();
                    String value = entity.getValue(id);
                    HuiProperty<String, String> prop = new HuiProperty<String, String>(propertyType, id, value);
                    if(getNoLabelTypeList().contains(id)) {
                        prop.setShowLabel(false);
                    }
                    propertyList.add(prop);
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("prop: " + id + " (" + propertyType.getInputName() + ") - " + value);
                    }
                }
            }   
        } else {
            // (sometimes) his is not an error, GSM workflow tasks doesn't have an element
            LOG.info("Element not found, type: " + getTypeId() + ", uuid: " + getUuid());
        }
        
    }

    private boolean isVisible(PropertyType huiType) {
        if(getVisiblePropertyIds()!=null && !getVisiblePropertyIds().isEmpty()) {
            return isVisibleType(huiType);
        } else {
            return isVisible(getTagSet(huiType.getTags()));
        }
    }

    private boolean isVisibleType(PropertyType type) {
        return type.isVisible() && getVisiblePropertyIds().contains(type.getId());
    }

    /**
     * 
     * 
     * @param groupHui
     * @return
     */
    private boolean isVisible(PropertyGroup groupHui) {
        boolean visible = isVisible(getTagSet(groupHui.getTags()));
        if(!visible) {
            for (PropertyType type : groupHui.getPropertyTypes()) {
                if(isVisible(type)) {
                    visible = true;
                    break;
                }
            }
        }
        return visible;
    }

    private Set<String> getTagSet(String allTags) {
        Set<String> tagSet = new HashSet<String>();
        StringTokenizer st = new StringTokenizer(allTags,",");
        while(st.hasMoreTokens()) {
            tagSet.add(st.nextToken());
        }
        return tagSet;
    }

    /**
     * Returns true if tagSet contains one of the visible tags for this bean instance. 
     * 
     * @param tagSet A set of tags
     * @return true if tagSet contains one of the visible tags
     */
    private boolean isVisible(Set<String> tagSet) {
        boolean visible = getVisibleTags().contains(TAG_ALL);
        if(tagSet!=null) {
            for (String tag : getVisibleTags()) {
                if(tagSet.contains(tag)) {
                    visible = true;
                    break;
                }
            }
        }
        return visible;
    }
    
    public String getSave() {
        return null;
    }
    
    public void setSave(String save) {
    }

    public void save() {
        LOG.debug("save called...");
        try {
            if(getElement()!=null) {
                doSave();                  
            }
            else {
                LOG.warn("Control is null. Can not save.");
            }
        } catch (SecurityException e) {
            LOG.error("Saving not allowed, uuid: " + getUuid(), e);
            Util.addError(SUBMIT, Util.getMessage(BOUNDLE_NAME, "save.forbidden"));
        } catch (sernet.gs.service.SecurityException e) {
            LOG.error("Saving not allowed, uuid: " + getUuid(), e);
            Util.addError(SUBMIT, Util.getMessage(BOUNDLE_NAME, "save.forbidden"));
        } catch (Exception e) {
            LOG.error("Error while saving element, uuid: " + getUuid(), e);
            Util.addError(SUBMIT, Util.getMessage(BOUNDLE_NAME, "save.failed"));
        }
    }

    private void doSave() throws CommandException {
        if (!writeEnabled()) {
            throw new SecurityException("write is not allowed" );
        }
        Entity entity = getElement().getEntity();    
        for (HuiProperty<String, String> property : getPropertyList()) {
            entity.setSimpleValue(property.getType(), property.getValue());
            if (LOG.isDebugEnabled()) {
                LOG.debug("Property: " + property.getType().getId() + " set to: " + property.getValue());
            }
        }
        for (sernet.verinice.web.PropertyGroup group : getGroupList()) {
            for (HuiProperty<String, String> property : group.getPropertyList()) {
                entity.setSimpleValue(property.getType(), property.getValue());
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Property: " + property.getType().getId() + " set to: " + property.getValue());
                }
            }
        }
        SaveElement<CnATreeElement> command = new SaveElement<CnATreeElement>(getElement());                           
        command = getCommandService().executeCommand(command);
        setElement(command.getElement());
        for (IChangeListener listener : getChangeListener()) {
            listener.elementChanged(getElement());
        }
        if(LOG.isDebugEnabled()) {
            LOG.debug("Element saved, uuid: " + getUuid());
        }   
        Util.addInfo(SUBMIT, getSaveMessage());
    }

    private String getSaveMessage() {
        if(saveMessage==null) {
            return Util.getMessage(EditBean.BOUNDLE_NAME, "saved");
        } else {
            return saveMessage;
        }
    }
    
    public void setSaveMessage( String message ) {
        this.saveMessage = message;
    }
    
    public void clear() {
        if(groupList!=null) {
            groupList.clear();
        }
        if(propertyList!=null) {
            propertyList.clear();
        }
        uuid = null;
        typeId = null;
        title = null;
        element = null;
        if(getLinkBean()!=null) {
            getLinkBean().clear();
        }
        if(noLabelTypeList!=null) {
            noLabelTypeList.clear();
        }
        clearActionHandler();
    }
    
    public String getAction() { return null; };
    
    public void setAction(String s) {
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
            Set<String> userRoles = getRoles();
            for (Permission p : cte.getPermissions()) {
                if (p!=null && p.isWriteAllowed() && userRoles.contains(p.getRole())) {
                    return true;
                }
            }
        } catch (SecurityException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Write is not allowed", e);
            }
            return false;
        } catch (sernet.gs.service.SecurityException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Write is not allowed", e);
            }
            return false;
        } catch(RuntimeException re) {
            LOG.error("Error while checking write permissions", re);
            throw re;
        } catch(CommandException t) {
            LOG.error("Error while checking write permissions", t);
            throw new RuntimeException("Error while checking write permissions", t);
        }
        return false;
    }
    
    public Set<String> getRoles() throws CommandException {
        if (roles == null) {        
            roles = loadRoles();
        } 
        return roles;
    }
    
    public Set<String> loadRoles() throws CommandException {
        LoadCurrentUserConfiguration lcuc = new LoadCurrentUserConfiguration();       
        lcuc = getCommandService().executeCommand(lcuc);            
        Configuration c = lcuc.getConfiguration();
        // No configuration for the current user (anymore?). Then nothing is
        // writable.
        if (c == null) {
            return Collections.emptySet();
        }
        return c.getRoles();
    }
    
    public void addNoLabelType(String id) {
        noLabelTypeList.add(id);
    }
    
    public LinkBean getLinkBean() {
        return linkBean;
    }

    public void setLinkBean(LinkBean linkBean) {
        this.linkBean = linkBean;
    }

    /**
     * @return the attachmentBean
     */
    public AttachmentBean getAttachmentBean() {
        return attachmentBean;
    }

    /**
     * @param attachmentBean the attachmentBean to set
     */
    public void setAttachmentBean(AttachmentBean attachmentBean) {
        this.attachmentBean = attachmentBean;
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

    public EntityType getEntityType() {
        return entityType;
    }

    public void setEntityType(EntityType entityType) {
        this.entityType = entityType;
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
    
    public List<HuiProperty<String, String>> getLabelPropertyList() {
        List<HuiProperty<String, String>> labelList = Collections.emptyList();
        List<HuiProperty<String, String>> list = getPropertyList();
        if(list!=null) {
            labelList = new LinkedList<HuiProperty<String,String>>();
            for (HuiProperty<String, String> property : getPropertyList()) {
                if(property.isShowLabel()) {
                    labelList.add(property);
                }
            }  
        }
        return labelList;
    }
    public List<HuiProperty<String, String>> getNoLabelPropertyList() {
        List<HuiProperty<String, String>> noLabelList = Collections.emptyList();
        List<HuiProperty<String, String>> list = getPropertyList();
        if(list!=null) {
            noLabelList = new LinkedList<HuiProperty<String,String>>();
            for (HuiProperty<String, String> property : getPropertyList()) {
                if(!property.isShowLabel()) {
                    noLabelList.add(property);
                }
            }  
        }
        return noLabelList;
    }
    
    public boolean isAttachmentEnabled() {
        // TODO dm implement rights management in web frontend
        return true;
    }
    
    public boolean isNewAttachmentEnabled() {
        // TODO dm implement rights management in web frontend
        return true;
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

    public List<String> getNoLabelTypeList() {
        return noLabelTypeList;
    }

    public List<IActionHandler> getActionHandler() {
        return actionHandler;
    }

    public void setActionHandler(List<IActionHandler> actionHandlerList) {
        this.actionHandler = actionHandlerList;
    }
    
    public void addActionHandler(IActionHandler newActionHandler) {
        if(this.actionHandler==null) {
            this.actionHandler = new LinkedList<IActionHandler>();
        }
        this.actionHandler.add(newActionHandler);
    }
    
    public void clearActionHandler() {
        if(getActionHandler()!=null) {
            getActionHandler().clear();
        }
    }
    
    public List<IChangeListener> getChangeListener() {
        if(this.changeListener==null) {
            this.changeListener = new LinkedList<IChangeListener>();
        }
        return changeListener;
    }

    public void setChangeListener(List<IChangeListener> changeListener) {
        this.changeListener = changeListener;
    }
    
    public void addChangeListener(IChangeListener changeListener) {
        getChangeListener().add(changeListener);
    }
    
    public void clearChangeListener() {
        getChangeListener().clear();
    }

    public List<String> getVisibleTags() {
        return visibleTags;
    }

    public void setVisibleTags(List<String> visibleTags) {
        this.visibleTags = visibleTags;
    }

    public Set<String> getVisiblePropertyIds() {
        return visiblePropertyIds;
    }

    public void setVisiblePropertyIds(Set<String> visiblePropertyIds) {
        this.visiblePropertyIds = visiblePropertyIds;
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

    public boolean getLinkCollapsed() {
        return linkCollapsed;
    }

    public void setLinkCollapsed(boolean linkCollapsed) {
        this.linkCollapsed = linkCollapsed;
    }
    
    public boolean isAttachmentOpen() {
        return attachmentOpen;
    }

    public void setAttachmentOpen(boolean open) {
        this.attachmentOpen = open;
    }

    public boolean isSaveButtonHidden() {
        return saveButtonHidden;
    }

    public void setSaveButtonHidden(boolean saveButtonHidden) {
        this.saveButtonHidden = saveButtonHidden;
    }
    
    public String getSaveButtonClass() {
        if(isSaveButtonHidden()) {
            return "saveButtonHidden";
        } else {
            return "saveButton";
        }
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
