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
import java.util.List;

import org.apache.log4j.Logger;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.bsi.dnd.DNDItems;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.service.ICommandService;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.commands.CommandException;
import sernet.gs.ui.rcp.main.service.crudcommands.SaveElement;
import sernet.verinice.iso27k.model.Control;
import sernet.verinice.iso27k.model.ControlGroup;
import sernet.verinice.iso27k.model.Group;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class ControlTransformService {
	
	private final Logger log = Logger.getLogger(ControlTransformService.class);
	
	private ICommandService commandService;
	
	private IProgressObserver progressObserver;
	
	@SuppressWarnings("unchecked")
	private Group selectedGroup;
	
	private int numberOfControls;
	
	private int numberProcessed;
	
	public int getNumberOfControls() {
		return numberOfControls;
	}
	
	/**
	 * @param progressObserver
	 * @param selectedGroup
	 */
	public ControlTransformService(IProgressObserver progressObserver, Group selectedGroup) {
		this.progressObserver = progressObserver;
		this.selectedGroup = selectedGroup;
	}

	public void run()  {
		try {	
			Activator.inheritVeriniceContextState();
			this.numberOfControls = 0;
			List<IItem> itemList = createInsertList(DNDItems.getItems());
			StringBuilder sb = new StringBuilder();
			sb.append("Transforming ").append(numberOfControls).append(" catalog items to controls.");
			progressObserver.beginTask(sb.toString(), numberOfControls);
			numberProcessed = 0;
			for (IItem item : itemList) {				
				insertItem(progressObserver, selectedGroup, item);
			}		
		} catch (Exception e) {
			log.error("Error while transforming to control", e);
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
			log.warn("Transforming canceled. " + numberProcessed + " of " + numberOfControls + " items transformed.");
			return;
		}
		SaveElement command = null;
		CnATreeElement element = null;
		if(item.getItems()!=null && item.getItems().size()>0) {
			// create a group
			element = ItemControlTransformer.transformToGroup(item);
			monitor.setTaskName(getText(numberOfControls,numberProcessed,element.getTitle()));
			group.addChild(element);
			element.setParent(group);
			command = new SaveElement<ControlGroup>((ControlGroup) element);
			
		} else {
			// create a control
			element = ItemControlTransformer.transform(item);
			monitor.setTaskName(getText(numberOfControls,numberProcessed,element.getTitle()));
			group.addChild(element);
			element.setParent(group);
			command = new SaveElement<Control>((Control) element);
		}

		try {
			command = getCommandService().executeCommand(command);
		} catch (CommandException e) {
			log.error("Error while inserting control", e);
			throw new RuntimeException("Error while inserting control", e);
		}
		
		
		element = (CnATreeElement) command.getElement();
		element.setParent(group);
		CnAElementFactory.getModel(element).childAdded(group, element);
		CnAElementFactory.getModel(element).databaseChildAdded(element);
		monitor.processed(1);
		numberProcessed++;
		if(item.getItems()!=null) {
			for (IItem child : item.getItems()) {
				insertItem(monitor,(Group) element,child);
			}
		}
	}

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
		StringBuilder sb = new StringBuilder();
		sb.append(i).append(" of ").append(n).append(" items transformed. ");
		sb.append("Transforming item: ").append(title);
		return sb.toString();
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
