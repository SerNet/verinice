/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak[at]sernet[dot]de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *     You should have received a copy of the GNU Lesser General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Alexander Koderman <ak[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.service.taskcommands;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import sernet.gs.ui.rcp.main.bsi.model.Konsolidator;
import sernet.verinice.interfaces.ChangeLoggingCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.IChangeLoggingCommand;
import sernet.verinice.model.bsi.BausteinUmsetzung;
import sernet.verinice.model.common.ChangeLogEntry;
import sernet.verinice.model.common.CnATreeElement;

public class KonsolidatorCommand extends ChangeLoggingCommand implements IChangeLoggingCommand {

	private List<BausteinUmsetzung> selectedElements;
	private BausteinUmsetzung source;
    private String stationId;
    private List<CnATreeElement> changedElements;
    
    
	public KonsolidatorCommand(List<BausteinUmsetzung> selectedElements,
			BausteinUmsetzung source) {
		this.selectedElements = selectedElements;
		this.source = source;
        this.stationId = ChangeLogEntry.STATION_ID;
        
	}

	public void execute() {
		IBaseDao<BausteinUmsetzung, Serializable> dao = getDaoFactory().getDAO(BausteinUmsetzung.class);
		dao.reload(source, source.getDbId());
		
		changedElements = new LinkedList<CnATreeElement>();
		// for every target:
		for (BausteinUmsetzung target: selectedElements) {
			// do not copy source onto itself:
			if (source.equals(target)){
				continue;
			}
			dao.reload(target, target.getDbId());
			// set values:
			Konsolidator.konsolidiereBaustein(source, target);
			changedElements.add(target);
			changedElements.addAll(Konsolidator.konsolidiereMassnahmen(source, target));
		}
		
		// remove elements to make object smaller for transport back to client
		selectedElements = null;
		source = null;
	}

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IChangeLoggingCommand#getStationId()
     */
    @Override
    public String getStationId() {
        return stationId;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IChangeLoggingCommand#getChangedElements()
     */
    @Override
    public List<CnATreeElement> getChangedElements() {
        return changedElements;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IChangeLoggingCommand#getChangeType()
     */
    @Override
    public int getChangeType() {
        return ChangeLogEntry.TYPE_UPDATE;
    }


	
	

}
