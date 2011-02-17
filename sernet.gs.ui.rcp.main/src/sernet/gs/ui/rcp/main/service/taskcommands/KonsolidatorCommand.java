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
import java.util.List;

import sernet.gs.ui.rcp.main.bsi.model.Konsolidator;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.bsi.BausteinUmsetzung;

public class KonsolidatorCommand extends GenericCommand {

	private List<BausteinUmsetzung> selectedElements;
	private BausteinUmsetzung source;

	public KonsolidatorCommand(List<BausteinUmsetzung> selectedElements,
			BausteinUmsetzung source) {
		this.selectedElements = selectedElements;
		this.source = source;
	}

	public void execute() {
		IBaseDao<BausteinUmsetzung, Serializable> dao = getDaoFactory().getDAO(BausteinUmsetzung.class);
		dao.reload(source, source.getDbId());
		
		// for every target:
		for (BausteinUmsetzung target: selectedElements) {
			// do not copy source onto itself:
			if (source.equals(target))
				continue;
			
			dao.reload(target, target.getDbId());
			// set values:
			Konsolidator.konsolidiereBaustein(source, target);
			Konsolidator.konsolidiereMassnahmen(source, target);
		}
		
		// remove elements to make object smaller for transport back to client
		selectedElements = null;
		source = null;
	}


	
	

}
