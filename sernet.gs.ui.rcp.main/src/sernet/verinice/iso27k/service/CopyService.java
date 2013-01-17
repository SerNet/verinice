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
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Group;
import sernet.verinice.service.commands.CopyCommand;

/**
 * A CopyService is a job, which
 * copies a list of elements to an Element-{@link Group}.
 * The progress of the copy process can be monitored by a {@link IProgressObserver}.
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class CopyService extends PasteService implements IProgressTask {
	
	private final Logger log = Logger.getLogger(CopyService.class);
	
	
    
	private List<CnATreeElement> elements;
	
	private List<String> newElements;
	
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

	/* (non-Javadoc)
     * @see sernet.verinice.iso27k.service.IProgressTask#run()
     */
	public void run()  {
		try {	
			Activator.inheritVeriniceContextState();
		    List<String> uuidList = new ArrayList<String>(this.elements.size());
			for (CnATreeElement element : this.elements) {
			    uuidList.add(element.getUuid());
				}
			numberOfElements = uuidList.size();
			// -1 means unknown runtime
			progressObserver.beginTask(Messages.getString("CopyService.1",numberOfElements), -1);         //$NON-NLS-1$
			CopyCommand cc = new CopyCommand(this.selectedGroup.getUuid(), uuidList, getPostProcessorList());
			cc = getCommandService().executeCommand(cc);
			numberOfElements = cc.getNumber();
			progressObserver.setTaskName(Messages.getString("CopyService.4")); //$NON-NLS-1$
			CnAElementFactory.getInstance().reloadModelFromDatabase();
			newElements = cc.getNewElements();
		} catch (Exception e) {
			log.error("Error while copying element", e); //$NON-NLS-1$
			throw new RuntimeException("Error while copying element", e); //$NON-NLS-1$
		} finally {
			progressObserver.done();
		}
	}

    public List<String> getNewElements() {
        return newElements;
    }
	
}
