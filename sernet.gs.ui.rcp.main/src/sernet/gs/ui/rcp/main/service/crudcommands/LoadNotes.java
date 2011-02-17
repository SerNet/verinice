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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Projection;
import org.hibernate.criterion.Restrictions;

import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.Property;
import sernet.hui.common.connect.PropertyList;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.bsi.Note;

public class LoadNotes extends GenericCommand {

	private transient Logger log = Logger.getLogger(LoadNotes.class);
	
	public Logger getLog() {
		if(log==null) {
			log = Logger.getLogger(LoadNotes.class);
		}
		return log;
	}

	private final NoteSorter sorter = new NoteSorter();

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
			for (Note note : noteList) {
				Entity entity = note.getEntity();
				if(entity!=null) {
					for (PropertyList pl : entity.getTypedPropertyLists().values()) {
						for (Property p : pl.getProperties()) {
							p.setParent(entity);
						}
					}
				}
			}
			Collections.sort(noteList, sorter);
			setNoteList(noteList);
		}
	}

	public void setCnAElementId(Integer cnAElementId) {
		this.cnAElementId = cnAElementId;
	}


	public Integer getCnAElementId() {
		return cnAElementId;
	}
	
	class NoteSorter implements Comparator<Note>, Serializable {

		/* (non-Javadoc)
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		public int compare(Note o1, Note o2) {
			int result = -1;
			if(o1!=null && o1.getTitel()!=null) {
				if(o2==null || o2.getTitel()==null) {
					result = 1;
				} else {
					result = o1.getTitel().compareTo(o2.getTitel());
				}
			} else if(o2==null || o2.getTitel()==null) {
				result = 0;
			}
			return result;
		}
		
	}

}
