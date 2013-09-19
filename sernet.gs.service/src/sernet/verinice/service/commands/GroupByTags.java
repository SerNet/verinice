/*******************************************************************************
 * Copyright (c) 2012 Daniel Murygin.
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
package sernet.verinice.service.commands;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import sernet.gs.service.RetrieveInfo;
import sernet.gs.service.RuntimeCommandException;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Asset;
import sernet.verinice.model.iso27k.Audit;
import sernet.verinice.model.iso27k.IISO27kGroup;

/**
 * This command groups child elements of a group.
 * 
 * If a child has a tag a group with the same name as the tag is created
 * and the element is moved to this group.
 * 
 * If there is already a group with the name of tag this group is used.
 * 
 * If a child has multiple tags the first tag is used after sorting the tags
 * lexicographically.
 * 
 * This command does not extend ChangeLoggingCommand because it uses 
 * other command to change data, which are ChangeLoggingCommands.
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class GroupByTags extends GenericCommand { 

    private transient Logger log = Logger.getLogger(GroupByTags.class);

    public Logger getLog() {
        if (log == null) {
            log = Logger.getLogger(GroupByTags.class);
        }
        return log;
    }

    private String groupUuid;

    private Set<String> tags = new HashSet<String>();

    private transient IBaseDao<CnATreeElement, Serializable> elementDao;

    private transient Map<String, CnATreeElement> existingChildrenGroups;
    
    private transient Map<String, List<String>> existingChildren;

    public GroupByTags(String uuid, Set<String> tags) {
        this.groupUuid = uuid;
        this.tags = tags;
    }

    /*
     * (non-Javadoc)
     * 
     * @see sernet.verinice.service.commands.LoadElementByUuid#execute()
     */
    @Override
    public void execute() {
        try {
            existingChildrenGroups = new Hashtable<String, CnATreeElement>();
            existingChildren = new Hashtable<String, List<String>>();
            if(this.groupUuid==null) {
                return;
            }
            if(this.tags==null || this.tags.isEmpty()) {
                return;
            }
            RetrieveInfo ri = new RetrieveInfo();
            ri.setChildren(true);
            ri.setChildrenProperties(true);
            CnATreeElement group = getElementDao().findByUuid(this.groupUuid, ri);
            cacheExistingChildren(group);           
            createChildrenGroups(group);
            moveChildrenToGroups(group);
        } catch (RuntimeException e) {
            getLog().error("Error while grouping by tags", e);
            throw e;
        } catch (Exception e) {
            getLog().error("Error while grouping by tags", e);
            throw new RuntimeCommandException("Error while grouping by tags", e);
        }
    }


    private void moveChildrenToGroups(CnATreeElement group) throws CommandException {
        for (String tag : existingChildren.keySet()) {
            CnATreeElement targetGroup = existingChildrenGroups.get(tag);
            CutCommand cutCommand = new CutCommand(targetGroup.getUuid(), existingChildren.get(tag));
            cutCommand = getCommandService().executeCommand(cutCommand);
        }       
    }

    private void cacheExistingChildren(CnATreeElement group) {
        for (CnATreeElement child : group.getChildren()) {
            if (isGroup(child)) {
                existingChildrenGroups.put(child.getTitle(), child);
            } else {
                List<String> childTagList = new ArrayList<String>(LoadTagsOfGroupElements.getTagList(child));
                childTagList.addAll(LoadTagsOfGroupElements.getGsmTagList(child));
                childTagList.addAll(LoadTagsOfGroupElements.getGsmIsmTagList(child));            
                Collections.sort(childTagList);
                for (String childTag : childTagList) {
                    if(tags.contains(childTag)) {
                        addChild(childTag, child);
                    }
                }
            }
        }
    }

    /**
     * @param child
     * @return
     */
    private boolean isGroup(CnATreeElement child) {
        return child instanceof IISO27kGroup &&
               !(child instanceof Asset) &&
               !(child instanceof Audit);
    }

    private void addChild(String childTag, CnATreeElement child) {
        List<String> children = existingChildren.get(childTag);
        if(children==null) {
            children = new LinkedList<String>();
        }
        children.add(child.getUuid());
        existingChildren.put(childTag, children);
        
    }

    private void createChildrenGroups(CnATreeElement group) throws CommandException {
        for (String tag : tags) {
            CnATreeElement childGroup = existingChildrenGroups.get(tag);
            if (childGroup == null) {
                childGroup = createGroup(group, tag);
                existingChildrenGroups.put(tag, childGroup);
            }
        }
    }

    @SuppressWarnings("rawtypes")
    private CnATreeElement createGroup(CnATreeElement parent, String name) throws CommandException {
        CreateElement saveCommand = new CreateElement(parent, parent.getTypeId(), name);
        saveCommand = getCommandService().executeCommand(saveCommand);
        return saveCommand.getNewElement();
    }

    public Set<String> getTags() {
        return tags;
    }

    public IBaseDao<CnATreeElement, Serializable> getElementDao() {
        if (elementDao == null) {
            elementDao = getDaoFactory().getDAO(CnATreeElement.class);
        }
        return elementDao;
    }

}
