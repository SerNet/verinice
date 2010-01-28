/*******************************************************************************
 * Copyright (c) 2010 Daniel Murygin <dm@sernet.de>.
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Daniel Murygin <dm@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.iso27k.rcp;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;

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
import sernet.verinice.iso27k.service.IItem;
import sernet.verinice.iso27k.service.ItemControlTransformer;

/**
 * Operation with transforms items from the {@link CatalogView}
 * to {@link Control}s. Created controls are added to a {@link ControlGroup}.
 * 
 * Operation is executed as task in a {@link IProgressMonitor}
 * 
 * @author Daniel Murygin <dm@sernet.de>
 */
public class ControlTransformOperation implements IRunnableWithProgress {

	private static final Logger LOG = Logger.getLogger(ControlTransformOperation.class);
	
	private ICommandService commandService;
	
	@SuppressWarnings("unchecked")
	private Group selectedGroup;
	
	private int numberOfControls;
	
	public int getNumberOfControls() {
		return numberOfControls;
	}


	@SuppressWarnings("unchecked")
	public ControlTransformOperation(Group selectedGroup) {
		this.selectedGroup = selectedGroup;	
	}
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.actions.WorkspaceModifyOperation#execute(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void run(IProgressMonitor monitor)  {
		try {	
			Activator.inheritVeriniceContextState();
			this.numberOfControls = 0;
			List<IItem> itemList = createInsertList(DNDItems.getItems());
			StringBuilder sb = new StringBuilder();
			sb.append("Transforming ").append(numberOfControls).append(" catalog items to controls.");
			monitor.beginTask(sb.toString(), numberOfControls);
			int i = 0;
			for (IItem item : itemList) {				
				insertItem(monitor, selectedGroup, item, i);
			}		
		} catch (Exception e) {
			LOG.error("Error while transforming to control", e);
		} finally {
			monitor.done();
		}

	}
	
	

	/**
	 * @param monitor
	 * @param group 
	 * @param item
	 */
	private void insertItem(IProgressMonitor monitor, Group group, IItem item, int i) {
		if(monitor.isCanceled()) {
			LOG.warn("Transforming canceled. " + i + " of " + numberOfControls + " items transformed.");
			return;
		}
		SaveElement command = null;
		CnATreeElement element = null;
		if(item.getItems()!=null && item.getItems().size()>0) {
			// create a group
			element = ItemControlTransformer.transformToGroup(item);
			monitor.setTaskName(getText(numberOfControls,i,element.getTitle()));
			group.addChild(element);
			element.setParent(group);
			command = new SaveElement<ControlGroup>((ControlGroup) element);
			
		} else {
			// create a control
			element = ItemControlTransformer.transform(item);
			monitor.setTaskName(getText(numberOfControls,i,element.getTitle()));
			group.addChild(element);
			element.setParent(group);
			command = new SaveElement<Control>((Control) element);
		}

		try {
			command = getCommandService().executeCommand(command);
		} catch (CommandException e) {
			LOG.error("Error while inserting control", e);
			throw new RuntimeException("Error while inserting control", e);
		}
		
		
		element = (CnATreeElement) command.getElement();
		element.setParent(group);
		CnAElementFactory.getModel(element).childAdded(group, element);
		CnAElementFactory.getModel(element).databaseChildAdded(element);
		monitor.worked(i++);
		i++;
		if(item.getItems()!=null) {
			for (IItem child : item.getItems()) {
				insertItem(monitor,(Group) element,child,i);
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
