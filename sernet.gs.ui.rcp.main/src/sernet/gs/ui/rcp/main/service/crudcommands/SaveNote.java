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

import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.Property;
import sernet.hui.common.connect.PropertyList;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.bsi.Addition;

public class SaveNote extends GenericCommand {

	private transient Logger log = Logger.getLogger(SaveNote.class);
	
	public Logger getLog() {
		if(log==null) {
			log = Logger.getLogger(LoadNotes.class);
		}
		return log;
	}
	
	Addition addition;

	public SaveNote(Addition note) {
		super();
		this.addition = note;
	}

	public void execute() {
		if (getLog().isDebugEnabled()) {
			getLog().debug("executing...");
		}
		try {
			if(getAddition()!=null) {
				IBaseDao<Addition, Serializable> dao = getDaoFactory().getDAO(Addition.class);
				dao.saveOrUpdate(getAddition());
				if (getLog().isDebugEnabled()) {
					getLog().debug("addition saved, id: " + getAddition().getDbId());
				}
				Entity entity = getAddition().getEntity();
				if(entity!=null) {
					for (PropertyList pl : entity.getTypedPropertyLists().values()) {
						for (Property p : pl.getProperties()) {
							p.setParent(entity);
						}
					}
				}
			}
		} catch (RuntimeException e) {
			log.error("Error while executing", e);
			throw e;
		}
	}
	
	public Addition getAddition() {
		return addition;
	}

	public void setAddition(Addition note) {
		this.addition = note;
	}
}
