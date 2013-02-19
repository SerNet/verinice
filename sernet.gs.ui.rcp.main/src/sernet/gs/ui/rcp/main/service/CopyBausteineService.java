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
package sernet.gs.ui.rcp.main.service;

import java.util.List;

import org.apache.log4j.Logger;

import sernet.gs.model.Baustein;
import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.bsi.dnd.Messages;
import sernet.gs.ui.rcp.main.common.model.BuildInput;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.verinice.iso27k.service.IProgressObserver;
import sernet.verinice.model.bsi.BausteinUmsetzung;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Group;

/**
 * A CopyBausteineService is a job, which
 * copies a list of elements to an Element-{@link Group}.
 * The progress of the copy process can be monitored by a {@link IProgressObserver}.
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class CopyBausteineService {
	
	private final Logger log = Logger.getLogger(CopyBausteineService.class);
	
	private IProgressObserver progressObserver;
	
	private List<CnATreeElement> selectedTargetList;
	
	private int numberOfElements;
	
	private int numberProcessed;

	private List<Baustein> elements;
	
	public int getNumberOfElements() {
		return numberOfElements;
	}
	
	/**
	 * Creates a new CopyBausteineService
	 * 
	 * @param progressObserver used to monitor the job process
	 * @param targets an element group, elements are copied to this group
	 * @param elementList a list of elements
	 */
	@SuppressWarnings("unchecked")
	public CopyBausteineService(IProgressObserver progressObserver, List<CnATreeElement> targets, List<Baustein> elementList) {
		this.progressObserver = progressObserver;
		this.selectedTargetList = targets;
		this.elements = elementList;	
	}

	/**
	 * Starts the execution of the copy job.
	 */
	public void run()  {
		try {	
			Activator.inheritVeriniceContextState();
			this.numberOfElements = selectedTargetList.size() * this.elements.size();
			StringBuilder sb = new StringBuilder();
			sb.append("Copying ").append(numberOfElements).append(" elements.");
			progressObserver.beginTask(sb.toString(), numberOfElements);
			numberProcessed = 0;
			
			CnATreeElement saveNew = null;
			for (CnATreeElement target : selectedTargetList) {
				saveNew = pasteBausteine(this.elements, target);
			}
			
			// notifying for one child is sufficient to update the views:
			CnAElementFactory.getLoadedModel().databaseChildAdded(saveNew);
					
		} catch (Exception e) {
			log.error("Error while copying element", e);
			throw new RuntimeException("Error while copying element", e);
		} finally {
			progressObserver.done();
		}
	}
	
	private CnATreeElement pasteBausteine(List<Baustein> elements, CnATreeElement target) {
		CnATreeElement saveNew = null;
		for (Baustein baustein : elements) {		
			if (target.canContain(baustein)) {
				try {
					StringBuilder sb = new StringBuilder();
					sb.append("Copy ").append(baustein.getTitel());
					sb.append(" (").append(numberProcessed).append("/").append(numberOfElements).append(")");
					progressObserver.setTaskName(sb.toString());
					saveNew = CnAElementFactory.getInstance().saveNew(target,
							BausteinUmsetzung.TYPE_ID,
							new BuildInput<Baustein>(baustein),
							false /* do not notify single elements*/);
				} catch (Exception e) {
					log.error(Messages.getString("PasteBsiModelViewAction.5"), e); //$NON-NLS-1$
				}	
			}	
			progressObserver.processed(1);
			numberProcessed++;
		}
		return saveNew;
	}
	
}
