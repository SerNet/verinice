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

import sernet.gs.service.PermissionException;
import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.verinice.interfaces.ElementChange;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Group;
import sernet.verinice.service.commands.CutCommand;

/**
 * A CutService is a job, which moves a list of elements from one to another Element-{@link Group}.
 * The progress of the job can be monitored by a {@link IProgressObserver}.
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
@SuppressWarnings("restriction")
public class CutService extends PasteService implements IProgressTask {
	
	private final Logger log = Logger.getLogger(CutService.class);
	
	private List<ElementChange> elementChanges;
	
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
	 * 
	 * @see sernet.verinice.iso27k.service.IProgressTask#run()
	 */
	public void run()  {
		try {	
			Activator.inheritVeriniceContextState();
			checkPermissions(this.elements);
			List<String> uuidList = new ArrayList<String>(this.elements.size());
            for (CnATreeElement element : this.elements) {
                uuidList.add(element.getUuid());
            }
            numberOfElements = uuidList.size();
            progressObserver.beginTask(Messages.getString("CutService.1",numberOfElements), numberOfElements);  
            CutCommand cc = new CutCommand(this.selectedGroup.getUuid(), uuidList, getPostProcessorList());
            cc = getCommandService().executeCommand(cc);
            numberOfElements = cc.getNumber();
            progressObserver.setTaskName(Messages.getString("CutService.3"));
            CnAElementFactory.getInstance().reloadModelFromDatabase();
            elementChanges = cc.getChanges();
		} catch (PermissionException e) {
			if (log.isDebugEnabled()) {
				log.debug(e);
			}
			throw e;
		} catch (RuntimeException e) {
			log.error("RuntimeException while copying element", e);
			throw e;
		} catch (Exception e) {
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
				throw new PermissionException(Messages.getString("CutService.4") + getTitle(element));
			}
		}
		return ok;
	}

	private String getTitle(CnATreeElement element) {
		String title = "unknown";
		try {
			title = element.getTitle();
		} catch(Exception t) {
			log.error("Error while reading title.", t);
		}
		return title;
	}

    public List<ElementChange> getElementChanges() {
        return elementChanges;
    }


}
