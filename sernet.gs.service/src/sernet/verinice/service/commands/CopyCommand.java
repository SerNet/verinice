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
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.IPostProcessor;
import sernet.verinice.model.bsi.BausteinUmsetzung;
import sernet.verinice.model.bsi.risikoanalyse.GefaehrdungsUmsetzung;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.IISO27kGroup;

/**
 * Copies a list of elements with all children to a group.
 * Element types in BLACKLIST are ignored.
 * 
 * CopyCommand uses command SaveElement to save element copies.
 * SaveElement is a IChangeLoggingCommand and logs all changes from
 * CopyCommand
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
@SuppressWarnings("serial")
public class CopyCommand extends GenericCommand {

    private transient Logger log = Logger.getLogger(CopyCommand.class);

    public Logger getLog() {
        if (log == null) {
            log = Logger.getLogger(CopyCommand.class);
        }
        return log;
    }
    
    public static final List<String> copy_blacklist;
    public static final List<String> cut_blacklist;
    
    static {
    	copy_blacklist = Arrays.asList("riskanalysis","gefaehrdungsumsetzung"); //$NON-NLS-1$
        cut_blacklist = Arrays.asList("riskanalysis","gefaehrdungsumsetzung"); //$NON-NLS-1$
    }
    
    private String uuidGroup;
    
    private transient CnATreeElement selectedGroup;
    
    private List<String> uuidList;
    
    
    private List<IPostProcessor> postProcessorList;
    
    private int number = 0;
    
    private transient IBaseDao<CnATreeElement, Serializable> dao;
    
    private List<String> newElements;
      

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
            List<CnATreeElement> copyElements;
            newElements = new ArrayList<String>(0);
            number = 0;
            copyElements = createInsertList(uuidList);
            selectedGroup = getDao().findByUuid(uuidGroup, RetrieveInfo.getChildrenInstance().setParent(true).setProperties(true));       
            Map<String, String> sourceDestMap = new Hashtable<String, String>();
            for (CnATreeElement element : copyElements) {     
                CnATreeElement newElement = copy(selectedGroup, element, sourceDestMap);
                if(newElement != null && newElement.getUuid() != null){
                    newElements.add(newElement.getUuid());         
                }
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

    private CnATreeElement copy(CnATreeElement group, CnATreeElement element, Map<String, String> sourceDestMap) throws CommandException {
        CnATreeElement elementCopy = element;
        if(element!=null 
            && element.getTypeId()!=null 
            && !copy_blacklist.contains(element.getTypeId()) 
            && group.canContain(element)) {
            elementCopy = saveCopy(group, element);     
            number++;
            sourceDestMap.put(element.getUuid(), elementCopy.getUuid());
            if(element.getChildren()!=null) {
                for (CnATreeElement child : element.getChildren()) {
                    copy(elementCopy,child,sourceDestMap);
                }
            }
        } else if(element!=null) {
            getLog().warn("Can not copy element with pk: " + element.getDbId() + " to group with pk: " + selectedGroup.getDbId()); //$NON-NLS-1$ //$NON-NLS-2$
        } else {
            getLog().warn("Can not copy element. Element is null");
        }
        return elementCopy;
    }

    private CnATreeElement saveCopy(CnATreeElement toGroup, CnATreeElement copyElement) throws CommandException {
        copyElement = getDao().initializeAndUnproxy(copyElement);
        CnATreeElement newElement = saveNew(toGroup, copyElement);
        if(newElement.getEntity()!=null) {
            newElement.getEntity().copyEntity(copyElement.getEntity());
            if(toGroup.getChildren()!=null && toGroup.getChildren().size()>0) {
               if (newElement instanceof GefaehrdungsUmsetzung){
                    String title = newElement.getTitle();
                    String copyGefaehrdungtitle = ((GefaehrdungsUmsetzung)newElement).getText();
                    Set<CnATreeElement> siblings = toGroup.getChildren();
                    siblings.remove(newElement);
                    newElement.setTitel(getUniqueTitle(title, copyGefaehrdungtitle, siblings, 0));
                }else {
                String title = newElement.getTitle();
                Set<CnATreeElement> siblings = toGroup.getChildren();
                siblings.remove(newElement);
                newElement.setTitel(getUniqueTitle(title, title, siblings, 0));
            }
        }
    }
        SaveElement<CnATreeElement> saveCommand = new SaveElement<CnATreeElement>(newElement);
        saveCommand = getCommandService().executeCommand(saveCommand);
        newElement = (CnATreeElement) saveCommand.getElement();
        newElement.setParentAndScope(toGroup);
        if (getLog().isDebugEnabled()) {
            getLog().debug("Copy created: " + newElement.getTitle()); //$NON-NLS-1$
        }
        newElement.setChildren(new HashSet<CnATreeElement>());
        return newElement;
    }
    
    private CnATreeElement saveNew(CnATreeElement container, CnATreeElement element) throws CommandException {
        String title = HitroUtil.getInstance().getTypeFactory().getMessage(element.getTypeId());   
        CreateElement<CnATreeElement> saveCommand = new CreateElement<CnATreeElement>(container, (Class<CnATreeElement>) element.getClass(), title, true, false);
        saveCommand.setInheritAuditPermissions(true);
        saveCommand = getCommandService().executeCommand(saveCommand);
        CnATreeElement child = saveCommand.getNewElement();
        container.addChild(child);
        child.setParentAndScope(container);
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
            int depth_ = depth;
            if(depth_==0) {
                insertList.add(element);
            }
            if((element instanceof IISO27kGroup || element instanceof BausteinUmsetzung) 
               && element.getChildren()!=null) {

                depth_++;
                for (CnATreeElement child : element.getChildren()) {
                    createInsertList(child,tempList,insertList,depth_,removed);
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
        int n_ = n;
        for (CnATreeElement cnATreeElement : siblings) {
            if(cnATreeElement!=null && cnATreeElement.getTitle()!=null && (cnATreeElement.getTitle().equals(copyTitle)) ) {
                n_++;
                return getUniqueTitle(title, getCopyTitle(title, n_), siblings, n_);
            }
        }
        return result;
    }
    
    private String getCopyTitle(String title, int n) {
        return Messages.getString("CopyCommand.0", title, n); //$NON-NLS-1$
    }

    public String getUuidGroup() {
        return uuidGroup;
    }

    public void setUuidGroup(String uuidGroup) {
        this.uuidGroup = uuidGroup;
    }

    public List<String> getUuidList() {
        return uuidList;
    }

    public void setUuidList(List<String> uuidList) {
        this.uuidList = uuidList;
    }

    public List<IPostProcessor> getPostProcessorList() {
        return postProcessorList;
    }

    public void addPostProcessor(IPostProcessor task) {
        if(postProcessorList==null) {
            postProcessorList = new LinkedList<IPostProcessor>();
        }
        postProcessorList.add(task);
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    private IBaseDao<CnATreeElement, Serializable> getDao() {
        if(dao==null) {
            dao = getDaoFactory().getDAO(CnATreeElement.class);
        }
        return dao;
    }

    public List<String> getNewElements() {
        return newElements;
    }

}
