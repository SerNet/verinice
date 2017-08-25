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
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TimeZone;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIInput;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.event.ValueChangeEvent;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.primefaces.event.SelectEvent;

import sernet.gs.service.GSServiceException;
import sernet.gs.service.RetrieveInfo;
import sernet.gs.service.TimeFormatter;
import sernet.gs.web.SecurityException;
import sernet.gs.web.Util;
import sernet.hui.common.VeriniceContext;
import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.EntityType;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.hui.common.connect.PropertyGroup;
import sernet.hui.common.connect.PropertyOption;
import sernet.hui.common.connect.PropertyType;
import sernet.hui.common.multiselectionlist.IMLPropertyOption;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.interfaces.IConfigurationService;
import sernet.verinice.interfaces.bpm.ITask;
import sernet.verinice.interfaces.bpm.ITaskService;
import sernet.verinice.model.bsi.MassnahmenUmsetzung;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.configuration.Configuration;
import sernet.verinice.service.commands.LoadCurrentUserConfiguration;
import sernet.verinice.service.commands.LoadElementByUuid;
import sernet.verinice.service.commands.SaveElement;
import sernet.verinice.service.parser.GSScraperUtil;

/**
 * JSF managed bean which provides data for the element editor in the web
 * application. Main purpose for this this bean is template editor.xhtml.
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
@ManagedBean(name = "edit")
@SessionScoped
public class EditBean {

    private static final Logger LOG = Logger.getLogger(EditBean.class);

    public static final String BOUNDLE_NAME = "sernet.verinice.web.EditMessages";
    public static final String TAG_WEB = "Web";
    public static final String TAG_ALL = "ALL-TAGS-VISIBLE";
    private static final String SUBMIT = "submit";
    private static final String NAME_SUFFIX = "_name";

    private CnATreeElement element;
    private EntityType entityType;
    private String typeId;
    private String uuid;
    private String title;
    private List<HuiProperty> propertyList;
    private List<sernet.verinice.web.PropertyGroup> groupList;
    private List<String> noLabelTypeList = new LinkedList<>();
    private Set<String> roles = null;
    private List<IActionHandler> actionHandler;
    private List<IChangeListener> changeListener;
    private boolean generalOpen = true;
    private boolean groupOpen = false;
    private boolean attachmentOpen = true;
    private boolean saveButtonHidden = true;
    private List<String> visibleTags = Arrays.asList(TAG_ALL);
    private Set<String> visiblePropertyIds = new HashSet<>();
    private String saveMessage = null;
    private MassnahmenUmsetzung massnahmenUmsetzung;
    private ITask task;
    private Map<String, String> changedElementProperties = new HashMap<>();

    @ManagedProperty("#{link}")
    private LinkBean linkBean;

    @ManagedProperty("#{attachment}")
    private AttachmentBean attachmentBean;

    public void init() {
        init(null);
    }

    public void init(ITask task) {
        long start = 0;
        if (LOG.isDebugEnabled()) {
            start = System.currentTimeMillis();
            LOG.debug("init() called ..."); //$NON-NLS-1$
        }
        try {
            doInit(task);
        } catch (CommandException t) {
            LOG.error("Error while initialization. ", t);
            Util.addError("massagePanel", Util.getMessage(BOUNDLE_NAME, "init.failed"));
        }
        if (LOG.isDebugEnabled()) {
            long duration = System.currentTimeMillis() - start;
            LOG.debug("init() finished in: " + TimeFormatter.getHumanRedableTime(duration)); //$NON-NLS-1$
        }
    }

    private void doInit(ITask task) throws CommandException {
        RetrieveInfo ri = RetrieveInfo.getPropertyInstance();
        ri.setPermissions(true);
        LoadElementByUuid<CnATreeElement> command = new LoadElementByUuid<>(getTypeId(), getUuid(), ri);
        command = getCommandService().executeCommand(command);
        setElement(command.getElement());

        this.task = task;
        resetChangedElementProperties();

        checkMassnahmenUmsetzung();

        if (getElement() != null) {
            doInitElement();
        } else {
            // (sometimes) his is not an error, GSM workflow tasks doesn't have
            // an element
            LOG.info("Element not found, type: " + getTypeId() + ", uuid: " + getUuid());
        }

    }

    private void resetChangedElementProperties() {
        changedElementProperties = new HashMap<>();
    }

    protected void doInitElement() {
        Entity entity = getElement().getEntity();
        setEntityType(getHuiService().getEntityType(getTypeId()));

        if (isTaskEditorContext()) {
            loadChangedElementPropertiesFromTask();
        }

        getLinkBean().setElement(getElement());
        getLinkBean().setEntityType(getEntityType());
        getLinkBean().setTypeId(getTypeId());
        getLinkBean().reset();

        getAttachmentBean().setElement(getElement());
        getAttachmentBean().init();

        doInitPropertyGroups(entity);
        doInitProperties(entity);
    }

    protected void doInitPropertyGroups(Entity entity) {
        groupList = new ArrayList<>();
        List<PropertyGroup> groupListHui = entityType.getPropertyGroups();
        for (PropertyGroup groupHui : groupListHui) {
            if (isVisible(groupHui)) {
                sernet.verinice.web.PropertyGroup group = new sernet.verinice.web.PropertyGroup(groupHui.getId(), groupHui.getName());
                List<PropertyType> typeListHui = groupHui.getPropertyTypes();
                List<HuiProperty> listOfGroup = createPropertyList(entity, typeListHui);
                group.setPropertyList(listOfGroup);
                if (!listOfGroup.isEmpty()) {
                    groupList.add(group);
                }
            }
        }
    }

    protected void doInitProperties(Entity entity) {
        List<PropertyType> typeList = entityType.getPropertyTypesSorted();
        propertyList = createPropertyList(entity, typeList);
    }

    protected List<HuiProperty> createPropertyList(Entity entity, List<PropertyType> typeListHui) {
        List<HuiProperty> listOfGroup = new ArrayList<>();
        for (PropertyType huiType : typeListHui) {
            if (isVisible(huiType)) {
                String id = huiType.getId();
                String value = entity.getRawPropertyValue(id);
                HuiProperty prop = new HuiProperty(entity, huiType, id, value);
                if (getNoLabelTypeList().contains(id)) {
                    prop.setShowLabel(false);
                }
                listOfGroup.add(prop);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("prop: " + id + " (" + huiType.getInputName() + ") - " + value);
                }
            }
        }
        return listOfGroup;
    }

    private void checkMassnahmenUmsetzung() {
        if (getElement() instanceof MassnahmenUmsetzung) {
            setMassnahmenUmsetzung((MassnahmenUmsetzung) element);
        } else {
            setMassnahmenUmsetzung(null);
        }
    }

    private void loadChangedElementPropertiesFromTask() {
        Map<String, String> changedProperties = (Map<String, String>) getTaskService().loadChangedElementProperties(task.getId());
        for (Entry<String, String> entry : changedProperties.entrySet()) {
            element.setPropertyValue(entry.getKey(), entry.getValue());
        }

        setTitle(element.getTitle() + Util.getMessage(BOUNDLE_NAME, "change.request"));
        LOG.info("Loaded changes for element properties from task."); //$NON-NLS-1$
    }

    private boolean isVisible(PropertyType huiType) {
        if (getVisiblePropertyIds() != null && !getVisiblePropertyIds().isEmpty()) {
            return isVisibleType(huiType);
        } else {
            return isVisibleByTags(huiType);
        }
    }

    private boolean isVisibleType(PropertyType type) {
        return type.isVisible() && getVisiblePropertyIds().contains(type.getId());
    }

    private boolean isVisibleByTags(PropertyType type) {
        if (!type.isVisible()) {
            return false;
        }
        Set<String> tagSet = getTagSet(type.getTags());
        return isVisible(tagSet);
    }

    private boolean isVisible(PropertyGroup groupHui) {
        boolean visible = isVisible(getTagSet(groupHui.getTags()));
        if (!visible) {
            for (PropertyType type : groupHui.getPropertyTypes()) {
                if (isVisible(type)) {
                    visible = true;
                    break;
                }
            }
        }
        return visible;
    }

    private Set<String> getTagSet(String allTags) {
        Set<String> tagSet = new HashSet<>();
        StringTokenizer st = new StringTokenizer(allTags, ",");
        while (st.hasMoreTokens()) {
            tagSet.add(st.nextToken());
        }
        return tagSet;
    }

    /**
     * Returns true if tagSet contains one of the visible tags for this bean
     * instance.
     * 
     * @param tagSet
     *            A set of tags
     * @return true if tagSet contains one of the visible tags
     */
    private boolean isVisible(Set<String> tagSet) {
        boolean visible = getVisibleTags().contains(TAG_ALL);
        if (tagSet != null) {
            for (String tag : getVisibleTags()) {
                if (tagSet.contains(tag)) {
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
        // nothing to do
    }

    public void save() {
        LOG.debug("save called...");
        try {
            if (getElement() != null) {
                if (isTaskEditorContext()) {
                    updateTaskWithChangedElementProperties();
                    Util.addInfo(SUBMIT, Util.getMessage(TaskBean.BOUNDLE_NAME, "taskUpdate"));
                    LOG.info("Skipped save cnAElement."); //$NON-NLS-1$
                } else {
                    doSave();
                }
            } else {
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

    private boolean isTaskEditorContext() {
        return task != null && task.isWithAReleaseProcess();
    }

    private void updateTaskWithChangedElementProperties() {
        if (!changedElementProperties.isEmpty()) {
            getTaskService().updateChangedElementProperties(task.getId(), changedElementProperties);
            changedElementProperties.clear();
            LOG.info("Updated task: saved changes in element properties."); //$NON-NLS-1$
        }
    }

    private void doSave() throws CommandException {
        if (!writeEnabled()) {
            throw new SecurityException("write is not allowed");
        }
        Entity entity = getElement().getEntity();
        for (HuiProperty property : getPropertyList()) {
            entity.setSimpleValue(property.getType(), property.getValue());
            if (LOG.isDebugEnabled()) {
                LOG.debug("Property: " + property.getType().getId() + " set to: " + property.getValue());
            }
        }
        for (sernet.verinice.web.PropertyGroup group : getGroupList()) {
            for (HuiProperty property : group.getPropertyList()) {
                entity.setSimpleValue(property.getType(), property.getValue());
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Property: " + property.getType().getId() + " set to: " + property.getValue());
                }
            }
        }
        SaveElement<CnATreeElement> command = new SaveElement<>(getElement());
        command = getCommandService().executeCommand(command);
        setElement(command.getElement());
        for (IChangeListener listener : getChangeListener()) {
            listener.elementChanged(getElement());
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Element saved, uuid: " + getUuid());
        }
        Util.addInfo(SUBMIT, getSaveMessage());
    }

    private String getSaveMessage() {
        if (saveMessage == null) {
            return Util.getMessage(EditBean.BOUNDLE_NAME, "saved");
        } else {
            return saveMessage;
        }
    }

    public void setSaveMessage(String message) {
        this.saveMessage = message;
    }

    public void clear() {
        if (groupList != null) {
            groupList.clear();
        }
        if (propertyList != null) {
            propertyList.clear();
        }
        uuid = null;
        typeId = null;
        title = null;
        element = null;
        if (getLinkBean() != null) {
            getLinkBean().clear();
        }
        if (noLabelTypeList != null) {
            noLabelTypeList.clear();
        }
        clearActionHandler();
    }

    public String getAction() {
        return null;
    }

    public void setAction(String s) {
        // nothing to do
    }

    public boolean writeEnabled() {
        boolean enabled = false;
        if (getElement() != null) {
            // causes NoClassDefFoundError:
            // org/eclipse/ui/plugin/AbstractUIPlugin
            // FIXME: fix this dependency to eclipse related classes.
            enabled = getConfigurationService().isWriteAllowed(getElement());
        }
        return enabled;
    }

    /**
     * Check if write is allowed by using the saved element as context and
     * delegating to {@link #isWriteAllowed(CnATreeElement)}.
     * 
     * @return true if write is allowed for the element.
     */
    public boolean isWriteAllowed() {
        if (element == null) {
            return false;
        }

        return getConfigurationService().isWriteAllowed(element);
    }

    // TODO: impl reference
    // TODO: impl multiselect
    public void onChange(ValueChangeEvent event) {
        if (isTaskEditorContext()) {
            trackChangedValuesForReleaseProcess(event);
        }
    }

    private void trackChangedValuesForReleaseProcess(ValueChangeEvent event) {
        String key = (String) ((UIInput) event.getComponent()).getAttributes().get("key");
        String newValue = handleBooleanValue(event.getNewValue());

        if (StringUtils.isNotEmpty(key)) {

            PropertyType propertyType = HUITypeFactory.getInstance().getPropertyType(getElement().getEntityType().getId(), key);

            if (propertyType.isSingleSelect()) {
                newValue = getSingleSelectOptionId(newValue, propertyType);
            } else if (propertyType.isNumericSelect()) {
                Entity entity = getElement().getEntity();
                newValue = entity.getPropertyValue(key);
            }

            changedElementProperties.put(key, newValue);
        }
        if (key.contains(NAME_SUFFIX)) {
            setTitle(newValue + Util.getMessage(BOUNDLE_NAME, "change.request"));
        }
    }

    private boolean isNotTaskEditorContext() {
        return !isTaskEditorContext();
    }

    public void onChangeNumericSelection(AjaxBehaviorEvent valueChangeEvent) {
        if (task != null) {
            String key = (String) ((UIInput) valueChangeEvent.getComponent()).getAttributes().get("key");
            @SuppressWarnings("unchecked")
            HuiProperty huiProperty = (HuiProperty) ((UIInput) valueChangeEvent.getComponent()).getAttributes().get("huiProperty");

            changedElementProperties.put(key, huiProperty.getValue());
        }
    }

    /**
     * Returns a string representation of boolean, so we can store the value
     * properly in our database.
     */
    private String handleBooleanValue(Object newValue) {
        if (newValue instanceof Boolean) {
            if ((Boolean) newValue) {
                return "1";
            } else {
                return "0";
            }
        }

        return (String) newValue;
    }

    private String getSingleSelectOptionId(String newValue, PropertyType propertyType) {
        String optionId = null;
        if (!Messages.getString(PropertyOption.SINGLESELECTDUMMYVALUE).equals(newValue)) {
            for (IMLPropertyOption option : propertyType.getOptions()) {
                if (option.getName().equals(newValue)) {
                    optionId = option.getId();
                    break;
                }
            }
        }
        return optionId;
    }

    public void onDateSelect(SelectEvent event) {
        String key = (String) ((UIInput) event.getComponent()).getAttributes().get("key");
        String newValue = String.valueOf(((Date) event.getObject()).getTime());

        if (StringUtils.isNotEmpty(key) && StringUtils.isNotEmpty(newValue)) {
            changedElementProperties.put(key, newValue);
        }
    }

    public void onURLValueChange(ValueChangeEvent event) {
        Map<String, Object> attributes = ((UIInput) event.getComponent()).getAttributes();
        String key = (String) attributes.get("key");
        String url = (String) event.getNewValue();
        String label = (String) attributes.get("label");
        changeURL(key, url, label);
    }

    public void onURLTextChange(ValueChangeEvent event) {
        Map<String, Object> attributes = ((UIInput) event.getComponent()).getAttributes();
        String key = (String) attributes.get("key");
        String url = (String) attributes.get("url");
        String label = (String) event.getNewValue();
        changeURL(key, url, label);
    }

    private void changeURL(String key, String url, String label) {
        if (StringUtils.isNotEmpty(key) && StringUtils.isNotEmpty(url) && StringUtils.isNotEmpty(label)) {
            StringBuilder sb = new StringBuilder();
            sb.append("<a href=\"").append(url).append("\">").append(label).append("</a>");
            changedElementProperties.put(key, sb.toString());
        }
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
     * @param attachmentBean
     *            the attachmentBean to set
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

    public List<HuiProperty> getLabelPropertyList() {
        List<HuiProperty> emptyList = Collections.emptyList();
        List<HuiProperty> list = getPropertyList();
        return list != null ? list : emptyList;
    }

    public boolean isAttachmentEnabled() {
        // TODO dm implement rights management in web frontend
        return true;
    }

    public boolean isNewAttachmentEnabled() {
        // TODO dm implement rights management in web frontend
        return true;
    }

    public List<HuiProperty> getPropertyList() {
        if (propertyList == null) {
            propertyList = Collections.emptyList();
        }
        moveURLPropertyToEndOfList();
        return propertyList;
    }

    private void moveURLPropertyToEndOfList() {
        HuiProperty docProp = null;
        for (int i = 0; i < propertyList.size(); i++) {
            if (propertyList.get(i).getIsURL()) {
                docProp = propertyList.get(i);
                break;
            }
        }
        if (docProp != null) {
            Collections.swap(propertyList, propertyList.indexOf(docProp), propertyList.size() - 1);
        }
    }

    public void setPropertyList(List<HuiProperty> properties) {
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
        if (this.actionHandler == null) {
            this.actionHandler = new LinkedList<>();
        }
        this.actionHandler.add(newActionHandler);
    }

    public void clearActionHandler() {
        if (getActionHandler() != null) {
            getActionHandler().clear();
        }
    }

    public List<IChangeListener> getChangeListener() {
        if (this.changeListener == null) {
            this.changeListener = new LinkedList<>();
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
        if (isSaveButtonHidden()) {
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

    private ITaskService getTaskService() {
        return (ITaskService) VeriniceContext.get(VeriniceContext.TASK_SERVICE);
    }

    private IConfigurationService getConfigurationService() {
        return (IConfigurationService) VeriniceContext.get(VeriniceContext.CONFIGURATION_SERVICE);
    }

    public MassnahmenUmsetzung getMassnahmenUmsetzung() {
        return massnahmenUmsetzung;
    }

    public void setMassnahmenUmsetzung(MassnahmenUmsetzung massnahmenUmsetzung) {
        this.massnahmenUmsetzung = massnahmenUmsetzung;
    }

    public String getMassnahmeHtml() {
        final MassnahmenUmsetzung massnahme = getMassnahmenUmsetzung();
        String text = null;
        if (massnahme != null) {
            try {
                text = GSScraperUtil.getInstanceWeb().getModel().getMassnahmeHtml(massnahme.getUrl(), massnahme.getStand());
            } catch (GSServiceException e) {
                LOG.error("Error while loading massnahme description.", e);
                Util.addError("submit", Util.getMessage("todo.load.failed"));
            }
        }
        if (text != null) {
            int start = text.indexOf("<div id=\"content\">");
            int end = text.lastIndexOf("</body>");
            if (start == -1 || end == -1) {
                LOG.error("Can not find content of control description: " + text);
                text = "";
            } else {
                text = text.substring(start, end);
            }
        } else {
            text = "";
        }
        return text;
    }
}
