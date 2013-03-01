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
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import sernet.gs.service.RetrieveInfo;
import sernet.gs.service.TimeFormatter;
import sernet.gs.web.ExceptionHandler;
import sernet.gs.web.Util;
import sernet.hui.common.VeriniceContext;
import sernet.hui.common.connect.EntityType;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.hui.common.connect.HuiRelation;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.interfaces.iso27k.ILink;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.service.commands.CreateLink;
import sernet.verinice.service.commands.LoadElementByTypeId;
import sernet.verinice.service.commands.LoadElementByUuid;
import sernet.verinice.service.commands.RemoveLink;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 *
 */
public class LinkBean {
    
    private static final Logger LOG = Logger.getLogger(LinkBean.class);

    private CnATreeElement element;
    
    private String typeId;
    
    private EntityType entityType;

    private List<ILink> linkList = new ArrayList<ILink>();
    
    private ILink selectedLink;
    
    private List<String> linkTypeList = new ArrayList<String>();
    private Map<String, HuiRelation> huiRelationMap = new Hashtable<String, HuiRelation>();
    
    private String selectedLinkType;
    
    private List<String> linkTargetNameList;
    
    private List<? extends CnATreeElement> linkTargetList;
    
    private String selectedLinkTargetName;
    
    private boolean deleteVisible = false;
    
    private boolean loading = true;
    
    public void reset() {
        loading = true;
        linkList = new ArrayList<ILink>();
        linkTypeList = new ArrayList<String>();
        huiRelationMap = new Hashtable<String, HuiRelation>();
    }
    
    public void init() {
        long start = 0;
        if (LOG.isDebugEnabled()) {
            start = System.currentTimeMillis();
            LOG.debug("init() called ..."); //$NON-NLS-1$
        }
        try {
            if(loading) {
                doInit();
            }
        } catch (Exception e) {
            LOG.error("Error while loading links.", e);
        } 
        if (LOG.isDebugEnabled()) {
            long duration = System.currentTimeMillis() - start;
            LOG.debug("init() finished in: " + TimeFormatter.getHumanRedableTime(duration)); //$NON-NLS-1$
        }
    }
    
    private void doInit() throws CommandException {
        CnATreeElement element = getElement();
        if(element==null) {
            // (sometimes) his is not an error, GSM workflow tasks doesn't have an element
            if (LOG.isInfoEnabled()) {
                LOG.info("Element is null. Can not init link bean.");
            }
            return;
        }
        RetrieveInfo ri = new RetrieveInfo();
        ri.setLinksDownProperties(true);
        ri.setLinksUpProperties(true);
        LoadElementByUuid<CnATreeElement> command = new LoadElementByUuid<CnATreeElement>(getTypeId(),getElement().getUuid(),ri);        
        command = getCommandService().executeCommand(command);    
        setElement(command.getElement());
        linkList = new ArrayList<ILink>();
        for (CnALink link : getElement().getLinksDown()) {
            linkList.add(map(link));
        }
        for (CnALink link : getElement().getLinksUp()) {
            linkList.add(map(link, true));
        }
        setSelectedLink(null);
        linkTypeList = new ArrayList<String>();
        huiRelationMap = new Hashtable<String, HuiRelation>();
        Set<HuiRelation> huiRelationSet = entityType.getPossibleRelations();
        for (HuiRelation huiRelation : huiRelationSet) {
            String label = getLinkTypeLabel(huiRelation);
            linkTypeList.add(label);
            huiRelationMap.put(label, huiRelation);
        }
        loading = false;
    }
    
    private String getLinkTypeLabel(HuiRelation huiRelation) {
        StringBuilder sb = new StringBuilder();
        if(getTypeId().equals(huiRelation.getFrom())) {
            sb.append(huiRelation.getName());
        } else {
            sb.append(huiRelation.getReversename());
        }
        sb.append(" (");
        sb.append(getHuiService().getMessage(huiRelation.getTo()));
        sb.append(")");
        return sb.toString();
    }
    
    public String getLoadLinkTargets() {
        return null;
    }
    
    public void loadLinkTargets(String s) {
    }
    
    /**
     * Loads the link targets, after a link type is selected.
     * See tasks.xhtml
     */
    public void loadLinkTargets() {
        String targetTypeId = null;
        try {
            if(getSelectedLinkType()!=null) {             
                Set<HuiRelation> huiRelationSet = getEntityType().getPossibleRelations();
                for (HuiRelation huiRelation : huiRelationSet) {
                    if(getSelectedLinkType().equals(getLinkTypeLabel(huiRelation))) {
                        targetTypeId = huiRelation.getTo();
                        break;
                    }
                }
                if (LOG.isDebugEnabled()) {
                    LOG.debug("loadLinkTargets(), targetTypeId: " + targetTypeId);
                }
                if(targetTypeId!=null) {
                    LoadElementByTypeId command = new LoadElementByTypeId(targetTypeId,RetrieveInfo.getPropertyInstance());
                    command = getCommandService().executeCommand(command);
                    setLinkTargetList(command.getElementList());
                    setLinkTargetNameList(new ArrayList<String>(linkTargetList.size()));
                    for (CnATreeElement linkTarget : linkTargetList) {
                        linkTargetNameList.add(linkTarget.getTitle());
                    }
                    Collections.sort(linkTargetNameList);
                }
            }
        } catch (Exception t) {
            LOG.error("Error while loading link targets, targetTypeId: " + targetTypeId, t);
            ExceptionHandler.handle(t);
        }
    }
    
    public String getAddLink() {
        addLink();
        return null;
    }
    
    public void setAddLink(String s) {
        addLink();
    }
    
    public void addLink() {
        try {
            if(getSelectedLinkType()!=null && getSelectedLinkTargetName()!=null) {
                CnATreeElement target = null;
                for (CnATreeElement linkTarget : getLinkTargetList()) {
                    if(linkTarget.getTitle().equals(getSelectedLinkTargetName())) {
                        target = linkTarget;
                        break;
                    }                  
                }
                if(target!=null) {
                    Set<HuiRelation> possibleRelations = getHuiService().getPossibleRelations(getTypeId(), target.getTypeId());
                    // try to link from target to dragged elements first:
                    // use first relation type (user can change it later):
                    if (!possibleRelations.isEmpty()) {
                        createLinkLookupRelations(target);
                    }
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("addLink, type-id: " + typeId);
                    }
                }
            }
        } catch (Exception t) {
            LOG.error("Error while creating link, typeId: " + typeId, t);
            ExceptionHandler.handle(t);
        }
    }

    private void createLinkLookupRelations(CnATreeElement target) throws CommandException {
        Set<HuiRelation> possibleRelations;
        boolean reverse = false;
        HuiRelation selectedRelation = huiRelationMap.get(getSelectedLinkType());
        CnALink link = createLink(getElement(), target, selectedRelation.getId(), "Created by web client");
        if(link==null) {
            // if none found: try reverse direction from dragged element to target (link is always modelled from one side only)
            possibleRelations = getHuiService().getPossibleRelations(target.getTypeId(), getTypeId());
            if ( !possibleRelations.isEmpty()) {
                link = createLink(target, getElement(), selectedRelation.getId(), "Created by web client");
            }
            reverse = true;
        } 
        if(link!=null) {
            LinkInformation linkInformation = map(link,reverse);
            linkInformation.setType(getTypeName(link));
            linkList.add(linkInformation);
            Util.addInfo("addLink", Util.getMessage(EditBean.BOUNDLE_NAME, "linkAdded", new String[] {target.getTitle()})); 
        }
    }
    
    public String getSelectLink() {  
        return null;
    }
    
    public void setSelectLink(String s) {
    }
    
    public void selectLink() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("selectLink() called ...");
        }
    }
    
    public void setShowDeleteLink(String s) {
    }
    
    public String getShowDeleteLink() {
        return null;
    }
    
    public void showDeleteLink() {
        deleteVisible = true;
    }
    
    public String getHideDeleteLink() {
        return null;
    }
    
    public void setHideDeleteLink(String s) {
    }
    
    public void hideDeleteLink() {
        deleteVisible = false;
    }
    
    public String getDeleteLink() {
        deleteLink();
        return null;
    }
    
    public void setDeleteLink() {
        deleteLink();
    }
    
    public void deleteLink() {
        try {
            if(getSelectedLink()!=null) {
                RemoveLink<CnALink> command = new RemoveLink<CnALink>(
                        getSelectedLink().getDependantId(), 
                        getSelectedLink().getDependencyId(), 
                        getSelectedLink().getTypeId());
                command = getCommandService().executeCommand(command);
                getLinkList().remove(getSelectedLink());
                setSelectedLink(null);
                deleteVisible = false;
            }
        } catch (CommandException t) {
            LOG.error("Error while deleting link", t);
            ExceptionHandler.handle(t);
        }
    }

    private String getTypeName(CnALink link) {
        String typeName = null;
        Set<HuiRelation> huiRelationSet = entityType.getPossibleRelations();
        for (HuiRelation huiRelation : huiRelationSet) {
            if(link.getRelationId()!=null && link.getRelationId().equals(huiRelation.getId())) {
                if(getTypeId().equals(huiRelation.getFrom())) {
                    typeName = huiRelation.getName();
                } else {
                    typeName = huiRelation.getReversename();
                }
                break;
            }
        }
        return typeName;
    }
    
    private CnALink createLink(CnATreeElement source, CnATreeElement target, String typeId, String comment) throws CommandException {
        if(LOG.isDebugEnabled()) {
            LOG.debug("Saving new link from " + source + " to " + target + "of type " + typeId); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
        CreateLink command = new CreateLink(source, target, typeId, comment);
        command = getCommandService().executeCommand(command);
        return command.getLink();
    }
    
    private LinkInformation map(CnALink link) {
        return map(link,false);
    }
    
    private LinkInformation map(CnALink link, boolean reverse) {
        LinkInformation linkInformation = new LinkInformation();
        linkInformation.setId(generateId(link));
        if(reverse) {
            linkInformation.setTargetName(link.getDependant().getTitle());
            linkInformation.setTargetUuid(link.getDependant().getUuid());
        } else {
            linkInformation.setTargetName(link.getDependency().getTitle());
            linkInformation.setTargetUuid(link.getDependency().getUuid());
        }
        linkInformation.setType(CnALink.getRelationName(getElement(),link));
        linkInformation.setDependantId(link.getId().getDependantId());
        linkInformation.setDependencyId(link.getId().getDependencyId());
        linkInformation.setTypeId(link.getRelationId());
        return linkInformation;
    }
    
    /**
     * @param link
     * @return
     */
    private String generateId(CnALink link) {
        StringBuilder sb = new StringBuilder();
        if(link.getDependant()!=null) {
            sb.append(link.getDependant().getId()).append("-");
        }
        if(link.getDependency()!=null) {
            sb.append(link.getDependency().getId()).append("-");
        }
        if(link.getTypeId()!=null) {
            sb.append(link.getRelationId());
        }
        return sb.toString();
    }

    public void clear() {
        setTypeId(null);
        setElement(null);
        if(getLinkList()!=null) {
            getLinkList().clear();
        }
        if(getLinkTargetList()!=null) {
            getLinkTargetList().clear();
        }
        if(getLinkTargetNameList()!=null) {
            getLinkTargetNameList().clear();
        }
        if(getLinkTypeList()!=null) {
            getLinkTypeList().clear();
        }
    }
    
    public CnATreeElement getElement() {
        return element;
    }

    public void setElement(CnATreeElement element) {
        this.element = element;
    }

    public String getTypeId() {
        return typeId;
    }

    public void setTypeId(String typeId) {
        this.typeId = typeId;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public void setEntityType(EntityType entityType) {
        this.entityType = entityType;
    }

    public List<ILink> getLinkList() {
        return linkList;
    }

    public void setLinkList(List<ILink> linkList) {
        this.linkList = linkList;
    }

    public ILink getSelectedLink() {
        return selectedLink;
    }

    public void setSelectedLink(ILink selectedLink) {
        this.selectedLink = selectedLink;
    }

    public List<String> getLinkTypeList() {
        return linkTypeList;
    }

    public String getSelectedLinkType() {
        return selectedLinkType;
    }

    public void setSelectedLinkType(String selectedLinkType) {
        this.selectedLinkType = selectedLinkType;
    }

    public void setLinkTypeList(List<String> linkTargetList) {
        this.linkTypeList = linkTargetList;
    }

    public List<String> getLinkTargetNameList() {
        return linkTargetNameList;
    }

    public void setLinkTargetNameList(List<String> linkTargetList) {
        this.linkTargetNameList = linkTargetList;
    }

    public List<? extends CnATreeElement> getLinkTargetList() {
        return linkTargetList;
    }

    public void setLinkTargetList(List<? extends CnATreeElement> linkTargetList) {
        this.linkTargetList = linkTargetList;
    }

    public String getSelectedLinkTargetName() {
        return selectedLinkTargetName;
    }

    public void setSelectedLinkTargetName(String selectedLinkTarget) {
        this.selectedLinkTargetName = selectedLinkTarget;
    }
    
    public boolean getDeleteVisible() {
        return deleteVisible;
    }

    public void setDeleteVisible(boolean deleteVisible) {
        this.deleteVisible = deleteVisible;
    }

    public boolean getLoading() {
        return loading;
    }

    public void setLoading(boolean loading) {
        this.loading = loading;
    }

    private HUITypeFactory getHuiService() {
        return (HUITypeFactory) VeriniceContext.get(VeriniceContext.HUI_TYPE_FACTORY);
    }
    
    private ICommandService getCommandService() {
        return (ICommandService) VeriniceContext.get(VeriniceContext.COMMAND_SERVICE);
    }
}
