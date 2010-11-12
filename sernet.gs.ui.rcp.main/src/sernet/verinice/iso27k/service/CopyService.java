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

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import sernet.gs.service.RetrieveInfo;
import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.common.model.BuildInput;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.service.crudcommands.SaveElement;
import sernet.verinice.model.bsi.IBSIModelListener;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Audit;
import sernet.verinice.model.iso27k.Group;
import sernet.verinice.model.iso27k.Organization;

/**
 * A CopyService is a job, which
 * copies a list of elements to an Element-{@link Group}.
 * The progress of the copy process can be monitored by a {@link IProgressObserver}.
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class CopyService extends PasteService implements IProgressTask {
	
	private final Logger log = Logger.getLogger(CopyService.class);
	
	public static List<String> BLACKLIST;
	
	static {
		BLACKLIST = Arrays.asList("riskanalysis","bstumsetzung","mnums"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
	
	private int numberProcessed;

	private List<CnATreeElement> elements;
	
	private List<CnATreeElement> copyElements;
	
	boolean doFullReload = false;
	
	/**
     * Creates a new CopyService
     * 
     * @param progressObserver used to monitor the job process
     * @param group an element group, elements are copied to this group
     * @param elementList a list of elements
     */
    @SuppressWarnings("unchecked")
    public CopyService(CnATreeElement group, List<CnATreeElement> elementList) {
        progressObserver = new DummyProgressObserver();
        this.selectedGroup = group;
        this.elements = elementList;    
        this.doFullReload = (this.elements!=null && this.elements.size()>9); 
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
		this.doFullReload = (this.elements!=null && this.elements.size()>9);
	}

	/* (non-Javadoc)
     * @see sernet.verinice.iso27k.service.IProgressTask#run()
     */
	public void run()  {
		try {	
			Activator.inheritVeriniceContextState();
			this.numberOfElements = 0;
			copyElements = createInsertList(elements);
			progressObserver.beginTask(Messages.getString("CopyService.1",numberOfElements), numberOfElements);
			this.doFullReload = numberOfElements>9;
			Map<String, String> sourceDestMap = new Hashtable<String, String>();
            numberProcessed = 0;
            selectedGroup = Retriever.retrieveElement(selectedGroup, RetrieveInfo.getChildrenInstance().setParent(true).setProperties(true));		
            for (CnATreeElement element : copyElements) {	    
				CnATreeElement elementCopy = copy(progressObserver, selectedGroup, element, sourceDestMap);
				if(!doFullReload) {
				    CnAElementFactory.getModel(elementCopy).childAdded(selectedGroup, elementCopy);
				    CnAElementFactory.getModel(elementCopy).databaseChildAdded(elementCopy);
				}
			}
			for (IPostProcessor postProcessor : getPostProcessorList()) {
			    postProcessor.process(sourceDestMap);
            }
			if(doFullReload) {
			    CnAElementFactory.getInstance().reloadModelFromDatabase();
			}
		} catch (Exception e) {
			log.error("Error while copying element", e); //$NON-NLS-1$
			throw new RuntimeException("Error while copying element", e); //$NON-NLS-1$
		} finally {
			progressObserver.done();
		}
	}
	
	/**
	 * @param monitor
	 * @param group 
	 * @param element
	 * @param sourceDestMap 
	 * @throws Exception 
	 */
	@SuppressWarnings("unchecked")
	private CnATreeElement copy(IProgressObserver monitor, CnATreeElement group, CnATreeElement element, Map<String, String> sourceDestMap) throws Exception {
		if(monitor.isCanceled()) {
			log.warn("Copying canceled. " + numberProcessed + " of " + numberOfElements + " elements copied."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			return null;
		}
		CnATreeElement elementCopy = element;
		if(element!=null 
			&& element.getTypeId()!=null 
			&& !BLACKLIST.contains(element.getTypeId()) 
			&& group.canContain(element)) {
			element = Retriever.retrieveElement(element, RetrieveInfo.getPropertyChildrenInstance());
			monitor.setTaskName(getText(numberOfElements,numberProcessed,element.getTitle()));
			elementCopy = saveCopy(group, element);
			sourceDestMap.put(element.getUuid(), elementCopy.getUuid());
			monitor.processed(1);
			numberProcessed++;
			if(element.getChildren()!=null) {
				for (CnATreeElement child : element.getChildren()) {
					copy(monitor,elementCopy,child,sourceDestMap);
				}
			}
		} else {
			log.warn("Can not copy element with pk: " + element.getDbId() + " to group with pk: " + selectedGroup.getDbId()); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return elementCopy;
	}

	/**
	 * @param sourceDestMap 
	 * @param element 
	 * @param element
	 * @return
	 * @throws Exception 
	 */
	private CnATreeElement saveCopy(CnATreeElement toGroup, CnATreeElement copyElement) throws Exception {
		CnATreeElement newElement = null;
		if(Organization.TYPE_ID.equals(copyElement.getTypeId())) {
		    newElement = CnAElementFactory.getInstance().saveNewOrganisation(toGroup, false, false);
		} else if(Audit.TYPE_ID.equals(copyElement.getTypeId())) {
            newElement = CnAElementFactory.getInstance().saveNewAudit(toGroup, false, false);
        } else {
		    newElement = CnAElementFactory.getInstance().saveNew(toGroup, copyElement.getTypeId(), new BuildInput<Boolean>(Boolean.FALSE), false);
		}
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
		if (log.isDebugEnabled()) {
			log.debug("Copy created: " + newElement.getTitle()); //$NON-NLS-1$
		}
		// notify all views of change:
		if(!doFullReload) {
		    CnAElementFactory.getModel(newElement).childChanged(toGroup, newElement);
		    CnAElementFactory.getModel(newElement).refreshAllListeners(IBSIModelListener.SOURCE_EDITOR);
		}
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
			if(cnATreeElement!=null && cnATreeElement.getTitle()!=null && (cnATreeElement.getTitle().equals(copyTitle)) ) {
				n++;
				return getUniqueTitle(title, getCopyTitle(title, n), siblings, n);
			}
		}
		return result;
	}
	
	private String getCopyTitle(String title, int n) {
		StringBuilder sb = new StringBuilder();
		return sb.append(title).append(" ").append(Messages.getString("CopyService.3", n)).toString();
	}

	/**
	 * @param n
	 * @param i
	 * @param title
	 */
	private String getText(int n, int i, String title) {
        return Messages.getString("CopyService.2", i, n, title);
	}

    protected List<CnATreeElement> getCopyElements() {
        return copyElements;
    }
	
}
