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
import java.util.Calendar;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import sernet.gs.service.PermissionException;
import sernet.gs.service.RetrieveInfo;
import sernet.verinice.interfaces.ChangeLoggingCommand;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.ElementChange;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.IChangeLoggingCommand;
import sernet.verinice.interfaces.IPostProcessor;
import sernet.verinice.model.bsi.ITVerbund;
import sernet.verinice.model.common.ChangeLogEntry;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.IISO27kGroup;
import sernet.verinice.model.iso27k.Organization;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 *
 */
public class CutCommand extends ChangeLoggingCommand implements IChangeLoggingCommand {
 
    private transient Logger log = Logger.getLogger(CutCommand.class);
    
    public Logger getLog() {
        if (log == null) {
            log = Logger.getLogger(CutCommand.class);
        }
        return log;
    }
    
    private String uuidGroup;
    
    private CnATreeElement selectedGroup;
    
    private List<String> uuidList;
    
    private int number = 0;
    
    private List<IPostProcessor> postProcessorList;
    
    private transient IBaseDao<CnATreeElement, Serializable> dao;
    
    // used on server side only !
    private transient Set<ElementChange> elementChanges;
    
    private String stationId;
    
    /**
     * returns a list of classes that can contain persons or personIsos as children
     * @return
     */
    private static List<String> getPersonContainingTypeIDs(){
        ArrayList<String> list = new ArrayList<String>();
        list.add(sernet.verinice.model.bsi.Person.TYPE_ID);
        list.add(sernet.verinice.model.bsi.PersonenKategorie.TYPE_ID);
        list.add(sernet.verinice.model.iso27k.Audit.TYPE_ID);
        list.add(sernet.verinice.model.iso27k.PersonGroup.TYPE_ID);
        list.add(sernet.verinice.model.iso27k.PersonIso.TYPE_ID);
        return list;
    }
    
    /**
     * @param uuidGroup
     * @param uuidList
     */
    public CutCommand(String uuidGroup, List<String> uuidList) {
        this(uuidGroup,uuidList,new ArrayList<IPostProcessor>());
    }

    /**
     * @param uuid
     * @param uuidList2
     * @param postProcessorList2
     */
    public CutCommand(String uuidGroup, List<String> uuidList, List<IPostProcessor> postProcessorList) {
        super();
        this.uuidGroup = uuidGroup;
        this.uuidList = uuidList;
        this.postProcessorList = postProcessorList;
        this.stationId = ChangeLogEntry.STATION_ID;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICommand#execute()
     */
    @Override
    public void execute() {
        try {   
            this.number = 0;
            elementChanges = new HashSet<ElementChange>();
            List<CnATreeElement> elementList = createInsertList(uuidList); 
            selectedGroup = getDao().findByUuid(uuidGroup, RetrieveInfo.getChildrenInstance().setParent(true).setProperties(true));       
            Map<String, String> sourceDestMap = new Hashtable<String, String>();
            boolean isPersonMoved = false;
            for (CnATreeElement element : elementList) {
                CnATreeElement movedElement = move(selectedGroup, element);
                // cut: source and dest is the same
                sourceDestMap.put(movedElement.getUuid(),movedElement.getUuid());
                for(String s : getPersonContainingTypeIDs()){
                    if(selectedGroup.getTypeId().equals(s)){
                        isPersonMoved = true;
                        break;
                    }
                }
            }       
            if(isPersonMoved){
                getCommandService().discardUserData();
            }         
            
            // set scope id of all elements and it's subtrees
            for (CnATreeElement element : elementList) {
                if(selectedGroup.getScopeId() != null){
                    UpdateScopeId updateScopeId = new UpdateScopeId(element.getDbId(), selectedGroup.getScopeId());
                    updateScopeId = getCommandService().executeCommand(updateScopeId);
                } else if(!(selectedGroup instanceof Organization) && !(selectedGroup instanceof ITVerbund)) {
                        getLog().warn("cut&paste target has no scopeID");
                }
            }
            
            
            if(getPostProcessorList()!=null && !getPostProcessorList().isEmpty()) {
                List<String> copyElementUuidList = new ArrayList<String>(elementList.size());
                for (CnATreeElement element : elementList) {
                    copyElementUuidList.add(element.getUuid());
                }
                for (IPostProcessor postProcessor : getPostProcessorList()) {
                    postProcessor.process(copyElementUuidList,sourceDestMap);
                }
            }
             
                   
        } catch (PermissionException e) {
            if (getLog().isDebugEnabled()) {
                getLog().debug(e);
            }
            throw e;
        } catch (RuntimeException e) {
            getLog().error("RuntimeException while copying element", e);
            throw e;
        } catch (Exception e) {
            getLog().error("Error while copying element", e);
            throw new RuntimeException("Error while copying element", e);
        }
        
    }
    
    private CnATreeElement move(CnATreeElement group, CnATreeElement element) throws CommandException  {
        CnATreeElement parentOld = element.getParent();
        parentOld.removeChild(element);
        
        // save old parent
        UpdateElement command = new UpdateElement(parentOld, true, ChangeLogEntry.STATION_ID);
        command.setLogChanges(false);
        getCommandService().executeCommand(command);
        ElementChange delete = new ElementChange(element, ChangeLogEntry.TYPE_DELETE);
        elementChanges.add(delete);
        
        element.setParentAndScope(group);
        
        group.addChild(element);
        
        // save element
        SaveElement saveElementCommand = new SaveElement(element);
        saveElementCommand.setLogChanges(false);
        saveElementCommand = getCommandService().executeCommand(saveElementCommand);
        CnATreeElement savedElement = (CnATreeElement) saveElementCommand.getElement();
        ElementChange insert = new ElementChange(savedElement, ChangeLogEntry.TYPE_INSERT);
        if(insert.getTime().equals(delete.getTime())) {
            Calendar plus1Second = Calendar.getInstance();
            plus1Second.add(Calendar.SECOND, 1);       
            insert.setTime(plus1Second.getTime());
        }
        elementChanges.add(insert);
        
        number++;
        return savedElement;
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
            CnATreeElement element = getDao().findByUuid(uuid, RetrieveInfo.getChildrenInstance().setParent(true));
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
    
    private IBaseDao<CnATreeElement, Serializable> getDao() {
        if(dao==null) {
            dao = getDaoFactory().getDAO(CnATreeElement.class);
        }
        return dao;
    }
    
    @Override
    public void clear() {
        // changedElements are used on server side only !
        if(elementChanges!=null) {
            elementChanges.clear();
        }
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IChangeLoggingCommand#getStationId()
     */
    @Override
    public String getStationId() {
        return this.stationId;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IChangeLoggingCommand#getChangedElements()
     */
    @Override
    public List<ElementChange> getChanges() {
        ArrayList<ElementChange> changes = new ArrayList<ElementChange>(0);
        if(elementChanges != null && elementChanges.size() > 0){
            changes.addAll(elementChanges);
        }
        return changes;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IChangeLoggingCommand#getChangeType()
     */
    @Override
    public int getChangeType() {
        return ChangeLogEntry.TYPE_UPDATE;
    }

}
