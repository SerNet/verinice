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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import sernet.gs.ui.rcp.main.service.crudcommands.LoadCnAElementByEntityId;
import sernet.hui.common.connect.Entity;
import sernet.verinice.interfaces.ChangeLoggingCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.interfaces.IChangeLoggingCommand;
import sernet.verinice.model.common.ChangeLogEntry;
import sernet.verinice.model.common.CnATreeElement;

public class BulkEditUpdate extends ChangeLoggingCommand  implements IChangeLoggingCommand {

	private Class<? extends CnATreeElement> clazz;
	private List<Integer> dbIDs;
	private Entity dialogEntity;
	
	private transient Set<Entity> changedEntities;
	private String stationId;

	public BulkEditUpdate(Class<? extends CnATreeElement> clazz, List<Integer> dbIDs, Entity dialogEntity) {
		this.clazz=clazz;
		this.dbIDs = dbIDs;
		this.dialogEntity = dialogEntity;
		
		this.stationId = ChangeLogEntry.STATION_ID;
	}

	public void execute() {
		changedEntities = new HashSet<Entity>(dbIDs.size());
		IBaseDao<? extends CnATreeElement, Serializable> dao = getDaoFactory().getDAO(clazz);
		for (Integer id : dbIDs) {
			CnATreeElement found = dao.findById(id);
			Entity editEntity = found.getEntity();
			editEntity.copyEntity(dialogEntity);
			changedEntities.add(editEntity);
		}
	}

	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.service.commands.IChangeLoggingCommand#getChangeType()
	 */
	public int getChangeType() {
		return ChangeLogEntry.TYPE_UPDATE;
	}

	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.service.commands.IChangeLoggingCommand#getChangedElements()
	 */
	public List<CnATreeElement> getChangedElements() {
		List<CnATreeElement> changedElements = new ArrayList<CnATreeElement>(changedEntities.size());
		try {
			for (Entity entity : changedEntities) {
				LoadCnAElementByEntityId command = new LoadCnAElementByEntityId(entity.getDbId());
				command = getCommandService().executeCommand(command);
				changedElements.addAll(command.getElements());
			}
		} catch (Exception e) {
			Logger.getLogger(this.getClass()).error("Fehler beim Laden geänderter Elemente aus Transaktionslog.", e);
		}
		return changedElements;
	}

	/* (non-Javadoc)
	 * @see sernet.gs.ui.rcp.main.service.commands.IChangeLoggingCommand#getStationId()
	 */
	public String getStationId() {
		return stationId;
	}

}
