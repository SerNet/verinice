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
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Projection;
import org.hibernate.criterion.Restrictions;

import sernet.gs.ui.rcp.main.bsi.model.Note;
import sernet.gs.ui.rcp.main.connect.IBaseDao;
import sernet.gs.ui.rcp.main.service.commands.GenericCommand;

public class LoadNotes extends GenericCommand {

	private transient Logger log = Logger.getLogger(LoadNotes.class);
	
	public Logger getLog() {
		if(log==null) {
			log = Logger.getLogger(LoadNotes.class);
		}
		return log;
	}


	private Integer cnAElementId;
	
	private List<Note> noteList;
	
	public List<Note> getNoteList() {
		return noteList;
	}

	public void setNoteList(List<Note> noteList) {
		this.noteList = noteList;
	}

	public LoadNotes(Integer cnAElementId) {
		super();
		this.cnAElementId = cnAElementId;
	}

	public void execute() {
		if (getLog().isDebugEnabled()) {
			getLog().debug("executing, id is: " + getCnAElementId() + "...");
		}
		if(getCnAElementId()!=null) {
			IBaseDao<Note, Serializable> dao = getDaoFactory().getDAO(Note.class);
			DetachedCriteria crit = DetachedCriteria.forClass(Note.class);
			crit.add(Restrictions.eq("cnATreeElementId", getCnAElementId()));
			crit.setFetchMode("entity", FetchMode.JOIN);
			crit.setFetchMode("entity.typedPropertyLists", FetchMode.JOIN);
			crit.setFetchMode("entity.typedPropertyLists.properties", FetchMode.JOIN);
			crit.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
			List<Note> noteList = dao.findByCriteria(crit);
			if (getLog().isDebugEnabled()) {
				getLog().debug("number of notes found: " + noteList.size());
			}
			setNoteList(noteList);
		}
	}

	public void setCnAElementId(Integer cnAElementId) {
		this.cnAElementId = cnAElementId;
	}


	public Integer getCnAElementId() {
		return cnAElementId;
	}

}
