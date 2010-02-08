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
package sernet.verinice.iso27k.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.service.ICommandService;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.crudcommands.SaveElement;
import sernet.verinice.iso27k.model.Group;
import sernet.verinice.iso27k.model.IISO27kGroup;

/**
 * A CutService is a job, which moves a list of elements from one to another Element-{@link Group}.
 * The progress of the job can be monitored by a {@link IProgressObserver}.
 * 
 * @author Daniel Murygin <dm@sernet.de>
 */
public class CutService {
	
	private final Logger log = Logger.getLogger(CutService.class);
	
	private ICommandService commandService;
	
	private IProgressObserver progressObserver;
	
	private CnATreeElement selectedGroup;
	
	private int numberOfElements;
	
	private int numberProcessed;

	private List<CnATreeElement> elements;
	
	public int getNumberOfElements() {
		return numberOfElements;
	}
	
	/**
	 * Creates a new CutService
	 * 
	 * @param progressObserver used to monitor the job process
	 * @param group an element group, elements are moved to this group
	 * @param elementList a list of elements
	 */
	@SuppressWarnings("unchecked")
	public CutService(IProgressObserver progressObserver, CnATreeElement group, List<CnATreeElement> elementList) {
		this.progressObserver = progressObserver;
		this.selectedGroup = group;
		this.elements = elementList;	
	}

	/**
	 * Starts the execution of the moving job.
	 */
	public void run()  {
		try {	
			Activator.inheritVeriniceContextState();
			this.numberOfElements = 0;
			List<CnATreeElement> elementList = createInsertList(elements);
			StringBuilder sb = new StringBuilder();
			sb.append("Moving ").append(numberOfElements).append(" elements.");
			progressObserver.beginTask(sb.toString(), numberOfElements);
			numberProcessed = 0;
			
			for (CnATreeElement element : elementList) {
				CnATreeElement elementCopy = move(progressObserver, selectedGroup, element);
				CnAElementFactory.getModel(elementCopy).childAdded(selectedGroup, elementCopy);
				CnAElementFactory.getModel(elementCopy).databaseChildAdded(elementCopy);
			}		
		} catch (Exception e) {
			log.error("Error while copying element", e);
			throw new RuntimeException("Error while copying element", e);
		} finally {
			progressObserver.done();
		}
	}
	
	/**
	 * @param monitor
	 * @param group 
	 * @param element
	 * @throws Exception 
	 */
	@SuppressWarnings("unchecked")
	private CnATreeElement move(IProgressObserver monitor, CnATreeElement group, CnATreeElement element) throws Exception {
		if(monitor.isCanceled()) {
			log.warn("Copying canceled. " + numberProcessed + " of " + numberOfElements + " elements copied.");
			return null;
		}
		monitor.setTaskName(getText(numberOfElements,numberProcessed,element.getTitle()));
		CnATreeElement parentOld = element.getParent();
		parentOld.removeChild(element);
		element.setParent(group);
		group.addChild(element);
		
		// save old parent
		SaveElement saveOldParentCommand = new SaveElement(parentOld);
		saveOldParentCommand = getCommandService().executeCommand(saveOldParentCommand);
		parentOld = (CnATreeElement) saveOldParentCommand.getElement();
		
		CnAElementFactory.getModel(parentOld).childRemoved(parentOld, element);
		CnAElementFactory.getModel(parentOld).databaseChildRemoved(element);
		
		// save element
		SaveElement saveElementCommand = new SaveElement(element);
		saveOldParentCommand = getCommandService().executeCommand(saveElementCommand);
		element = (CnATreeElement) saveElementCommand.getElement();
		
		element.setParent(group);
		
		CnAElementFactory.getModel(element).childAdded(group, element);
		CnAElementFactory.getModel(element).databaseChildAdded(element);
		
		monitor.processed(1);
		numberProcessed++;
		return element;
	}

	private List<CnATreeElement> createInsertList(List<CnATreeElement> elementDragList) {
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

	/**
	 * @param n
	 * @param i
	 * @param title
	 */
	private String getText(int n, int i, String title) {
		StringBuilder sb = new StringBuilder();
		sb.append(i).append(" of ").append(n).append(" elements moved.");
		sb.append(" Moving element: ").append(title);
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
