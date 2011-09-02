/*******************************************************************************
 * Copyright (c) 2011 Daniel Murygin.
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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import sernet.gs.service.RetrieveInfo;
import sernet.hui.common.connect.HitroUtil;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.IPostProcessor;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.IISO27kGroup;

/**
 * Copies a list of elements with all children to a group.
 * Element types in BLACKLIST are ignored.
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class CopyCommand extends GenericCommand {

    private transient Logger log = Logger.getLogger(CopyCommand.class);

    public Logger getLog() {
        if (log == null) {
            log = Logger.getLogger(CopyCommand.class);
        }
        return log;
    }
    
    public static List<String> BLACKLIST;
    
    static {
        BLACKLIST = Arrays.asList("riskanalysis","bstumsetzung","mnums"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
    
    String uuidGroup;
    
    CnATreeElement selectedGroup;
    
    List<String> uuidList;
    
    private List<CnATreeElement> copyElements;
    
    private List<IPostProcessor> postProcessorList;
    
    private int number = 0;
    
    private transient IBaseDao<CnATreeElement, Serializable> dao;
      
    /**
     * @param uuidGroup Uuid of an group
     * @param uuidList Uuids of the elements to copy
     */
    public CopyCommand(String uuidGroup, List<String> uuidList) {
        this(uuidGroup,uuidList,new ArrayList<IPostProcessor>());
    }

    /**
     * @param uuid
     * @param uuidList2
     * @param postProcessorList2
     */
    public CopyCommand(String uuidGroup, List<String> uuidList, List<IPostProcessor> postProcessorList) {
        super();
        this.uuidGroup = uuidGroup;
        this.uuidList = uuidList;
        this.postProcessorList = postProcessorList;
    }

    /**
     * Copies the elements from uuidList to group with uuidGroup.
     * Calls recurvise method copy to copy all children of an element.
     * 
     * @see sernet.verinice.interfaces.ICommand#execute()
     */
    @Override
    public void execute() {
        try { 
            number = 0;
            copyElements = createInsertList(uuidList);
            selectedGroup = getDao().findByUuid(uuidGroup, RetrieveInfo.getChildrenInstance().setParent(true).setProperties(true));       
            Map<String, String> sourceDestMap = new Hashtable<String, String>();
            for (CnATreeElement element : copyElements) {             
                copy(selectedGroup, element, sourceDestMap);         
            }
            if(getPostProcessorList()!=null && !getPostProcessorList().isEmpty()) {
                List<String> copyElementUuidList = new ArrayList<String>(copyElements.size());
                for (CnATreeElement element : copyElements) {
                    copyElementUuidList.add(element.getUuid());
                }
                for (IPostProcessor postProcessor : getPostProcessorList()) {
                    postProcessor.process(copyElementUuidList,sourceDestMap);
                }
            }
           
        } catch (Exception e) {
            getLog().error("Error while copying element", e); //$NON-NLS-1$
            throw new RuntimeException("Error while copying element", e); //$NON-NLS-1$
        }
    }

    private CnATreeElement copy(CnATreeElement group, CnATreeElement element, Map<String, String> sourceDestMap) throws Exception {
        CnATreeElement elementCopy = element;
        if(element!=null 
            && element.getTypeId()!=null 
            && !BLACKLIST.contains(element.getTypeId()) 
            && group.canContain(element)) {
            elementCopy = saveCopy(group, element);     
            number++;
            sourceDestMap.put(element.getUuid(), elementCopy.getUuid());
            if(element.getChildren()!=null) {
                for (CnATreeElement child : element.getChildren()) {
                    copy(elementCopy,child,sourceDestMap);
                }
            }
        } else {
            getLog().warn("Can not copy element with pk: " + element.getDbId() + " to group with pk: " + selectedGroup.getDbId()); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return elementCopy;
    }

    private CnATreeElement saveCopy(CnATreeElement toGroup, CnATreeElement copyElement) throws Exception {
        copyElement = getDao().initializeAndUnproxy(copyElement);
        CnATreeElement newElement = saveNew(toGroup, copyElement);
        if(newElement.getEntity()!=null) {
            newElement.getEntity().copyEntity(copyElement.getEntity());
            if(toGroup.getChildren()!=null && toGroup.getChildren().size()>0) {
                String title = newElement.getTitle();
                Set<CnATreeElement> siblings = toGroup.getChildren();
                siblings.remove(newElement);
                newElement.setTitel(getUniqueTitle(title, title, siblings, 0));
            }
        }
        SaveElement<CnATreeElement> saveCommand = new SaveElement<CnATreeElement>(newElement);
        saveCommand = getCommandService().executeCommand(saveCommand);
        newElement = (CnATreeElement) saveCommand.getElement();
        newElement.setParent(toGroup);
        if (getLog().isDebugEnabled()) {
            getLog().debug("Copy created: " + newElement.getTitle()); //$NON-NLS-1$
        }
        newElement.setChildren(new HashSet<CnATreeElement>());
        return newElement;
    }
    
    private CnATreeElement saveNew(CnATreeElement container, CnATreeElement element) throws Exception {
        String title = HitroUtil.getInstance().getTypeFactory().getMessage(element.getTypeId());   
        CreateElement<CnATreeElement> saveCommand = new CreateElement<CnATreeElement>(container, (Class<CnATreeElement>) element.getClass(), title, true, false);
        saveCommand = getCommandService().executeCommand(saveCommand);
        CnATreeElement child = saveCommand.getNewElement();
        container.addChild(child);
        child.setParent(container);
        return child;
    }
    
    /**
     * Creates a list of elements. First all elements are loaded by
     * UUID. A child will be removed from the list if it's parent is already 
     * a member.
     * 
     * @param uuidList A list of element UUID
     * @return List of elements
     */
    protected List<CnATreeElement> createInsertList(List<String> uuidList) {
        List<CnATreeElement> tempList = new ArrayList<CnATreeElement>();
        List<CnATreeElement> insertList = new ArrayList<CnATreeElement>();
        int depth = 0;
        int removed = 0;
        for (String uuid : uuidList) {
            CnATreeElement element = getDao().findByUuid(uuid, RetrieveInfo.getChildrenInstance());
            createInsertList(element,tempList,insertList, depth, removed);
        }
        return insertList;
    }

    private void createInsertList(CnATreeElement element, List<CnATreeElement> tempList, List<CnATreeElement> insertList, int depth, int removed) {
        if(!tempList.contains(element)) {
            tempList.add(element);
            if(depth==0) {
                insertList.add(element);
            }
            if(element instanceof IISO27kGroup && element.getChildren()!=null) {
                depth++;
                for (CnATreeElement child : element.getChildren()) {
                    createInsertList(child,tempList,insertList,depth,removed);
                }
            }
        } else {
            insertList.remove(element);
            removed++;
        }
    }

    /**
     * Returns a unique title compared to tiltles of all siblings siblings
     * 
     * @param Title A title of an element
     * @param siblings Siblings of the element
     * @return A unique title
     */
    private String getUniqueTitle(String title, String copyTitle, Set<CnATreeElement> siblings, int n) {
        String result = copyTitle;
        for (CnATreeElement cnATreeElement : siblings) {
            if(cnATreeElement!=null && cnATreeElement.getTitle()!=null && (cnATreeElement.getTitle().equals(copyTitle)) ) {
                n++;
                return getUniqueTitle(title, getCopyTitle(title, n), siblings, n);
            }
        }
        return result;
    }
    
    private String getCopyTitle(String title, int n) {
        return Messages.getString("CopyCommand.0", title, n); //$NON-NLS-1$
    }

    /**
     * @return the uuidGroup
     */
    public String getUuidGroup() {
        return uuidGroup;
    }

    /**
     * @param uuidGroup the uuidGroup to set
     */
    public void setUuidGroup(String uuidGroup) {
        this.uuidGroup = uuidGroup;
    }

    /**
     * @return the uuidList
     */
    public List<String> getUuidList() {
        return uuidList;
    }

    /**
     * @param uuidList the uuidList to set
     */
    public void setUuidList(List<String> uuidList) {
        this.uuidList = uuidList;
    }

    /**
     * @return the postProcessorList
     */
    public List<IPostProcessor> getPostProcessorList() {
        return postProcessorList;
    }

    public void addPostProcessor(IPostProcessor task) {
        if(postProcessorList==null) {
            postProcessorList = new LinkedList<IPostProcessor>();
        }
        postProcessorList.add(task);
    }

    /**
     * @return the number
     */
    public int getNumber() {
        return number;
    }

    /**
     * @param number the number to set
     */
    public void setNumber(int number) {
        this.number = number;
    }

    private IBaseDao<CnATreeElement, Serializable> getDao() {
        if(dao==null) {
            dao = getDaoFactory().getDAO(CnATreeElement.class);
        }
        return dao;
    }

}
