/*******************************************************************************
 * Copyright (c) 2009 Daniel Murygin <dm[at]sernet[dot]de>.
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
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.service.crudcommands;

import java.io.Serializable;

import org.apache.log4j.Logger;

import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.bsi.Addition;
import sernet.verinice.model.bsi.Note;

public class DeleteNote extends GenericCommand {

private transient Logger log = Logger.getLogger(DeleteNote.class);
	
	public Logger getLog() {
		if(log==null) {
			log = Logger.getLogger(LoadNotes.class);
		}
		return log;
	}
	
	Addition note;

	public DeleteNote(Addition note) {
		super();
		this.note = note;
	}

	public void execute() {
		if (getLog().isDebugEnabled()) {
			getLog().debug("executing...");
		}
		if(getNote()!=null) {
			IBaseDao<Addition, Serializable> dao = getDaoFactory().getDAO(Addition.class);
			dao.delete(getNote());
			if (getLog().isDebugEnabled()) {
				getLog().debug("Addition deleted, id: " + getNote().getDbId());
			}
		}
	}
	
	public Addition getNote() {
		return note;
	}

	public void setNote(Note note) {
		this.note = note;
	}

}
