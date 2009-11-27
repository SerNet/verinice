/*******************************************************************************
 * Copyright (c) 2009 Daniel Murygin <dm@sernet.de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details.
 *     You should have received a copy of the GNU General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Daniel Murygin <dm@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.service.crudcommands;

import java.io.Serializable;

import org.apache.log4j.Logger;

import sernet.gs.ui.rcp.main.bsi.model.Note;
import sernet.gs.ui.rcp.main.connect.IBaseDao;
import sernet.gs.ui.rcp.main.service.commands.GenericCommand;
import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.Property;
import sernet.hui.common.connect.PropertyList;

public class SaveNote extends GenericCommand {

	private transient Logger log = Logger.getLogger(SaveNote.class);
	
	public Logger getLog() {
		if(log==null) {
			log = Logger.getLogger(LoadNotes.class);
		}
		return log;
	}
	
	Note note;

	public SaveNote(Note note) {
		super();
		this.note = note;
	}

	public void execute() {
		if (getLog().isDebugEnabled()) {
			getLog().debug("executing...");
		}
		if(getNote()!=null) {
			IBaseDao<Note, Serializable> dao = getDaoFactory().getDAO(Note.class);
			dao.saveOrUpdate(getNote());
			if (getLog().isDebugEnabled()) {
				getLog().debug("note saved, id: " + getNote().getDbId());
			}
			Entity entity = getNote().getEntity();
			if(entity!=null) {
				for (PropertyList pl : entity.getTypedPropertyLists().values()) {
					for (Property p : pl.getProperties()) {
						p.setParent(entity);
					}
				}
			}
		}
	}
	
	public Note getNote() {
		return note;
	}

	public void setNote(Note note) {
		this.note = note;
	}
}
