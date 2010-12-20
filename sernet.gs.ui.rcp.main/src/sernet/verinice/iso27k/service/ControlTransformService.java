/*******************************************************************************
 * Copyright (c) 2010 Daniel Murygin <dm[at]sernet[dot]de>.
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
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.iso27k.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.log4j.Logger;

import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.crudcommands.SaveElement;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.IAuthService;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.interfaces.iso27k.IItem;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.Permission;
import sernet.verinice.model.iso27k.Group;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class ControlTransformService {
	
	private final Logger log = Logger.getLogger(ControlTransformService.class);
	
	private ICommandService commandService;

	private IAuthService authService;

	private IProgressObserver progressObserver;
	
	private IModelUpdater modelUpdater;
	
	private List itemList;
	
	@SuppressWarnings("unchecked")
	private Group selectedGroup;
	
	private int numberOfControls;
	
	private int numberProcessed;
	
	private boolean doFullRefresh = false;
	
	public int getNumberOfControls() {
		return numberOfControls;
	}
	
	/**
	 * @param progressObserver
	 * @param selectedGroup
	 */
	public ControlTransformService(IProgressObserver progressObserver, IModelUpdater modelUpdater, Group selectedGroup) {
		this.progressObserver = progressObserver;
		this.modelUpdater = modelUpdater;
		this.selectedGroup = selectedGroup;
	}

	/**
     * @param progressObserver2
     * @param selectedGroup2
     * @param items
     */
    public ControlTransformService(
    		IProgressObserver progressObserver, 
    		IModelUpdater modelUpdater, 
    		Group selectedGroup, 
    		List items) {
        this(progressObserver, modelUpdater, selectedGroup);
        this.itemList = items;
    }

    public void run()  {
		try {	
			this.numberOfControls = 0;
			List<IItem> itemList = createInsertList(getItemList());
			progressObserver.beginTask(Messages.getString("ControlTransformService.1", numberOfControls), numberOfControls); //$NON-NLS-1$
			doFullRefresh = numberOfControls > 9;
			numberProcessed = 0;
			for (IItem item : itemList) {				
				insertItem(progressObserver, selectedGroup, item);
			}	
			if(doFullRefresh) {
			    modelUpdater.reload();
			}
		} catch (Exception e) {
			log.error("Error while transforming to control", e); //$NON-NLS-1$
		} finally {
			progressObserver.done();
		}
	}
	
	/**
	 * @param monitor
	 * @param group 
	 * @param item
	 */
	@SuppressWarnings("unchecked")
	private void insertItem(IProgressObserver monitor, Group group, IItem item) {
		if(monitor.isCanceled()) {
			log.warn("Transforming canceled. " + numberProcessed + " of " + numberOfControls + " items transformed."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			return;
		}
		SaveElement command = null;
		CnATreeElement element = null;
		if(item.getItems()!=null && item.getItems().size()>0) {
			// create a group
			element = GenericItemTransformer.transformToGroup(item);
			monitor.setTaskName(getText(numberOfControls,numberProcessed,element.getTitle()));
			if (group.canContain(element)) {
				group.addChild(element);
				element.setParent(group);
				if (log.isDebugEnabled()) {
               	 log.debug("Creating control group,  UUID: " + element.getUuid() + ", title: " + element.getTitle()); //$NON-NLS-1$ //$NON-NLS-2$
           	 	}	
			    
			}
		} else {
			// create a control
			element = GenericItemTransformer.transform(item);
			monitor.setTaskName(getText(numberOfControls,numberProcessed,element.getTitle()));
			
			if (group.canContain(element)) {
				group.addChild(element);
				element.setParent(group);
				if (log.isDebugEnabled()) {
			    	log.debug("Creating control,  UUID: " + element.getUuid() + ", title: " + element.getTitle());    //$NON-NLS-1$ //$NON-NLS-2$
			
				}
            }
		}

		try {
		    HashSet<Permission> newperms = new HashSet<Permission>();
	        newperms.add(Permission.createPermission(element, getAuthService().getUsername(), true, true));
	        element.setPermissions(newperms);
		    command = new SaveElement( element);
			command = getCommandService().executeCommand(command);
		} catch (CommandException e) {
			log.error("Error while inserting control", e); //$NON-NLS-1$
			throw new RuntimeException("Error while inserting control", e); //$NON-NLS-1$
		}
		
		
		element = (CnATreeElement) command.getElement();
		element.setParent(group);
		if(!doFullRefresh) {
		    modelUpdater.childAdded(group, element);
		}
		monitor.processed(1);
		numberProcessed++;
		if(item.getItems()!=null) {
			for (IItem child : item.getItems()) {
				insertItem(monitor,(Group) element,child);
			}
		}
	}

	/**
	 * createInsertList removes redundant items from itemDragList
	 * 
	 * @param itemDragList i list with items
	 * @return A list with all non redundant items from itemDragList
	 */
	private List<IItem> createInsertList(List<IItem> itemDragList) {
		List<IItem> tempList = new ArrayList<IItem>();
		List<IItem> insertList = new ArrayList<IItem>();
		int depth = 0;
		int removed = 0;
		for (IItem item : itemDragList) {
			createInsertList(item,tempList,insertList, depth, removed);
		}
		this.numberOfControls = tempList.size() - removed;
		return insertList;
	}

	/**
	 * Recursive helper method for createInsertList
	 * 
	 * @param item
	 * @param tempList
	 * @param insertList
	 * @param depth
	 * @param removed
	 */
	private void createInsertList(final IItem item, List<IItem> tempList, List<IItem> insertList, int depth, int removed) {
		if(!tempList.contains(item)) {
			tempList.add(item);
			if(depth==0) {
				insertList.add(item);
			}
			if(item.getItems()!=null) {
				depth++;
				for (IItem child : item.getItems()) {
					createInsertList(child,tempList,insertList,depth,removed);
				}
			}
		} else {
			insertList.remove(item);
			removed++;
		}
	}

	/**
	 * @param n
	 * @param i
	 * @param title
	 */
	private String getText(int n, int i, String title) {
		return Messages.getString("ControlTransformService.2", i, n, title); //$NON-NLS-1$
	}


	private List getItemList() {
        return itemList;
    }

    private void setItemList(List itemList) {
        this.itemList = itemList;
    }
    
    public IAuthService getAuthService() {
        if (authService == null) {
            authService = createAuthService();
        }
        return authService;
    }

    private IAuthService createAuthService() {
        return ServiceFactory.lookupAuthService();
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
}
