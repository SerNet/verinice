/*******************************************************************************
 * Copyright (c) 2010 Daniel Murygin <dm@sernet.de>.
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
 * You should have received a copy of the GNU General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Daniel Murygin <dm@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.iso27k.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.bsi.model.IBSIModelListener;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.connect.RetrieveInfo;
import sernet.gs.ui.rcp.main.service.ICommandService;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.crudcommands.SaveElement;
import sernet.verinice.iso27k.model.Group;
import sernet.verinice.iso27k.model.IISO27kGroup;

/**
 * A CopyService is a job, which
 * copies a list of elements to an Element-{@link Group}.
 * The progress of the copy process can be monitored by a {@link IProgressObserver}.
 * 
 * @author Daniel Murygin <dm@sernet.de>
 */
public class CopyService {
	
	private final Logger log = Logger.getLogger(CopyService.class);
	
	public static List<String> BLACKLIST;
	
	static {
		BLACKLIST = Arrays.asList("riskanalysis","bstumsetzung","mnums");
	}
	
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
	 * Creates a new CopyService
	 * 
	 * @param progressObserver used to monitor the job process
	 * @param group an element group, elements are copied to this group
	 * @param elementList a list of elements
	 */
	@SuppressWarnings("unchecked")
	public CopyService(IProgressObserver progressObserver, CnATreeElement group, List<CnATreeElement> elementList) {
		this.progressObserver = progressObserver;
		this.selectedGroup = group;
		this.elements = elementList;	
	}

	/**
	 * Starts the execution of the copy job.
	 */
	public void run()  {
		try {	
			Activator.inheritVeriniceContextState();
			this.numberOfElements = 0;
			List<CnATreeElement> elementList = createInsertList(elements);
			StringBuilder sb = new StringBuilder();
			sb.append("Copying ").append(numberOfElements).append(" elements.");
			progressObserver.beginTask(sb.toString(), numberOfElements);
			numberProcessed = 0;
			
			for (CnATreeElement element : elementList) {			
				CnATreeElement elementCopy = insertCopy(progressObserver, selectedGroup, element);
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
	private CnATreeElement insertCopy(IProgressObserver monitor, CnATreeElement group, CnATreeElement element) throws Exception {
		if(monitor.isCanceled()) {
			log.warn("Copying canceled. " + numberProcessed + " of " + numberOfElements + " elements copied.");
			return null;
		}
		CnATreeElement elementCopy = element;
		if(element!=null 
			&& element.getTypeId()!=null 
			&& !BLACKLIST.contains(element.getTypeId()) 
			&& selectedGroup.canContain(element)) {
			element = Retriever.retrieveElement(element, RetrieveInfo.getPropertyChildrenInstance());
			monitor.setTaskName(getText(numberOfElements,numberProcessed,element.getTitle()));
			elementCopy = copyElement(group, element);
			monitor.processed(1);
			numberProcessed++;
			if(element.getChildren()!=null) {
				for (CnATreeElement child : element.getChildren()) {
					insertCopy(monitor,elementCopy,child);
				}
			}
		} else {
			log.warn("Can not copy element with pk: " + element.getDbId() + " to group with pk: " + selectedGroup.getDbId());
		}
		return elementCopy;
	}

	/**
	 * @param element 
	 * @param element
	 * @return
	 * @throws Exception 
	 */
	private CnATreeElement copyElement(CnATreeElement toGroup, CnATreeElement copyElement) throws Exception {
		CnATreeElement newElement = CnAElementFactory.getInstance().saveNew(toGroup, copyElement.getTypeId(), null, false);
		newElement.getEntity().copyEntity(copyElement.getEntity());
		if(toGroup.getChildren()!=null && toGroup.getChildren().size()>0) {
			String title = newElement.getTitle();
			newElement.setTitel(getUniqueTitle(title, title, toGroup.getChildren(), 0));
		}
		SaveElement<CnATreeElement> saveCommand = new SaveElement<CnATreeElement>(newElement);
		saveCommand = getCommandService().executeCommand(saveCommand);
		newElement = (CnATreeElement) saveCommand.getElement();
		newElement.setParent(toGroup);
		if (log.isDebugEnabled()) {
			log.debug("Copy created: " + newElement.getTitle());
		}
		// notify all views of change:
		CnAElementFactory.getModel(newElement).childChanged(toGroup, newElement);
		CnAElementFactory.getModel(newElement).refreshAllListeners(IBSIModelListener.SOURCE_EDITOR);
		newElement.setChildren(new HashSet<CnATreeElement>());
		return newElement;
	}

	/**
	 * @param title
	 * @param siblings
	 * @return
	 */
	private String getUniqueTitle(String title, String copyTitle, Set<CnATreeElement> siblings, int n) {
		String result = copyTitle;
		for (CnATreeElement cnATreeElement : siblings) {
			cnATreeElement = Retriever.retrieveElement(cnATreeElement,RetrieveInfo.getPropertyInstance());
			if(cnATreeElement.getTitle()!=null && (cnATreeElement.getTitle().equals(copyTitle)) ) {
				n++;
				return getUniqueTitle(title, getCopyTitle(title, n), siblings, n);
			}
		}
		return result;
	}
	
	private String getCopyTitle(String title, int n) {
		StringBuilder sb = new StringBuilder();
		return sb.append(title).append(" (Copy ").append(n).append(")").toString();
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
		sb.append(i).append(" of ").append(n).append(" elements copied.");
		sb.append(" Copying element: ").append(title);
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
