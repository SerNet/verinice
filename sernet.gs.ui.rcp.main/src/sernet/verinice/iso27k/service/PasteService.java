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
package sernet.verinice.iso27k.service;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.iso27k.rcp.RcpProgressObserver;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.IISO27kGroup;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 *
 */
public abstract class PasteService implements IProgressTask {
    
    /**
     * @author Daniel Murygin <dm[at]sernet[dot]de>
     *
     */
    public interface IPostProcessor {
        
        void process(Map<String, String> sourceDestMap);
        
    }

    protected CnATreeElement selectedGroup;
    
    protected List<CnATreeElement> elements;
    
    protected int numberOfElements;
    
    private List<IPostProcessor> postProcessorList;
     
    protected IProgressObserver progressObserver;
    
    private ICommandService commandService;
    
    /* (non-Javadoc)
     * @see sernet.verinice.iso27k.service.IProgressTask#getNumberOfElements()
     */
    @Override
    public int getNumberOfElements() {
        return numberOfElements;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.iso27k.service.IProgressTask#setProgressObserver(sernet.verinice.iso27k.rcp.RcpProgressObserver)
     */
    @Override
    public void setProgressObserver(RcpProgressObserver rcpProgressObserver) {
        this.progressObserver = rcpProgressObserver;     
    }
    
    protected List<CnATreeElement> createInsertList(List<CnATreeElement> elementDragList) {
        List<CnATreeElement> tempList = new ArrayList<CnATreeElement>();
        List<CnATreeElement> insertList = new ArrayList<CnATreeElement>();
        int depth = 0;
        int removed = 0;
        for (CnATreeElement item : elementDragList) {
            createInsertList(item,tempList,insertList, depth, removed);
        }
        this.numberOfElements = tempList.size() - removed;
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
                element = Retriever.checkRetrieveChildren(element);
                for (CnATreeElement child : element.getChildren()) {
                    createInsertList(child,tempList,insertList,depth,removed);
                }
            }
        } else {
            insertList.remove(element);
            removed++;
        }
    }
    
    protected List<IPostProcessor> getPostProcessorList() {
        if(postProcessorList==null) {
            postProcessorList = new LinkedList<IPostProcessor>();
        }
        return postProcessorList;
    }
    
    public void addPostProcessor(IPostProcessor task) {
        if(postProcessorList==null) {
            postProcessorList = new LinkedList<IPostProcessor>();
        }
        postProcessorList.add(task);
    }
    
    public ICommandService getCommandService() {
        if (commandService == null) {
            commandService = createCommandServive();
        }
        return commandService;
    }

    private ICommandService createCommandServive() {
        return ServiceFactory.lookupCommandService();
    }

    /**
     * @return
     */
    public CnATreeElement getGroup() {
        return selectedGroup;
    }

}
