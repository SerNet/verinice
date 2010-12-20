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

import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import sernet.gs.service.PermissionException;
import sernet.gs.service.RetrieveInfo;
import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.gs.ui.rcp.main.service.crudcommands.SaveElement;
import sernet.gs.ui.rcp.main.service.crudcommands.UpdateElement;
import sernet.verinice.model.common.ChangeLogEntry;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Group;

/**
 * A CutService is a job, which moves a list of elements from one to another Element-{@link Group}.
 * The progress of the job can be monitored by a {@link IProgressObserver}.
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class CutService extends PasteService implements IProgressTask {
	
	private final Logger log = Logger.getLogger(CutService.class);
	
	private int numberProcessed;
	
	private boolean doFullReload = false;
	
	   /**
     * Creates a new CopyService
     * 
     * @param progressObserver used to monitor the job process
     * @param group an element group, elements are copied to this group
     * @param elementList a list of elements
     */
    @SuppressWarnings("unchecked")
    public CutService(CnATreeElement group, List<CnATreeElement> elementList) {
        progressObserver = new DummyProgressObserver();
        this.selectedGroup = group;
        this.elements = elementList;
        doFullReload = (this.elements!=null && this.elements.size()>9);
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
        doFullReload = (this.elements!=null && this.elements.size()>9);	
	}

	/**
	 * Starts the execution of the moving job.
	 * 
	 * @see sernet.verinice.iso27k.service.IProgressTask#run()
	 */
	public void run()  {
		try {	
			Activator.inheritVeriniceContextState();
			this.numberOfElements = 0;
			List<CnATreeElement> elementList = createInsertList(elements);		
			progressObserver.beginTask(Messages.getString("CutService.1",numberOfElements), numberOfElements);
			numberProcessed = 0;
			Map<String, String> sourceDestMap = new Hashtable<String, String>();
			checkPermissions(elementList);
			for (CnATreeElement element : elementList) {
				CnATreeElement movedElement = move(progressObserver, selectedGroup, element);
				// cut: source and dest is the same
				sourceDestMap.put(movedElement.getUuid(),movedElement.getUuid());
			}
            for (IPostProcessor postProcessor : getPostProcessorList()) {
                postProcessor.process(sourceDestMap);
            }	
            if(doFullReload) {
                CnAElementFactory.getInstance().reloadModelFromDatabase();
            }        
		} catch (PermissionException e) {
			if (log.isDebugEnabled()) {
				log.debug(e);
			}
			throw e;
		} catch (RuntimeException e) {
			log.error("RuntimeException while copying element", e);
			throw e;
		} catch (Throwable e) {
			log.error("Error while copying element", e);
			throw new RuntimeException("Error while copying element", e);
		} finally {
			progressObserver.done();
		}
	}
	
	private boolean checkPermissions(List<CnATreeElement> elementList) {
		boolean ok = true;
		for (CnATreeElement element : elementList) {
			if(!CnAElementHome.getInstance().isDeleteAllowed(element)) {
				ok = false;
				// FIXME: externalize Strings
				// this message must be multi lingual			
				throw new PermissionException("No permission to move elment: " + getTitle(element));
			}
		}
		return ok;
	}

	private String getTitle(CnATreeElement element) {
		String title = "unknown";
		try {
			title = element.getTitle();
		} catch(Throwable t) {
			log.error("Error while reading title.", t);
		}
		return title;
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
		CnATreeElement elementOld = Retriever.retrieveElement(element,new RetrieveInfo().setParent(true).setProperties(true));
		CnATreeElement parentOld = elementOld.getParent();
		parentOld = Retriever.retrieveElement(parentOld,RetrieveInfo.getChildrenInstance().setParent(true));
		parentOld.removeChild(element);
		
		// save old parent
		UpdateElement command = new UpdateElement(parentOld, true, ChangeLogEntry.STATION_ID);
		command = getCommandService().executeCommand(command);
		parentOld = (CnATreeElement) command.getElement();
		  
		element.setParent(group);
        group.addChild(element);
		
		// save element
		SaveElement saveElementCommand = new SaveElement(element);
		saveElementCommand = getCommandService().executeCommand(saveElementCommand);
		CnATreeElement savedElement = (CnATreeElement) saveElementCommand.getElement();
		
		if(!doFullReload) {
    		CnAElementFactory.getModel(parentOld).childRemoved(parentOld, elementOld);
            CnAElementFactory.getModel(elementOld).databaseChildRemoved(elementOld);
    		CnAElementFactory.getModel(savedElement).childAdded(group, savedElement);
    		CnAElementFactory.getModel(savedElement).databaseChildAdded(savedElement);
		}
		
		monitor.processed(1);
		numberProcessed++;
		return savedElement;
	}


	/**
	 * @param n
	 * @param i
	 * @param title
	 */
	private String getText(int n, int i, String title) {
		return Messages.getString("CutService.2", i, n, title);
	}

}
