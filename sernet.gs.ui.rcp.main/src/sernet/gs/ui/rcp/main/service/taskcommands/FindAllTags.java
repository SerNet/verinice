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
 *     Robert Schuster <r.schuster@tarent.de> - rewritten to use custom SQL query
 ******************************************************************************/
package sernet.gs.ui.rcp.main.service.taskcommands;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.model.bsi.BSIModel;
import sernet.verinice.model.bsi.TagHelper;

/** Retrieves all the tags used in the model.
 * 
 * <p>The resulting list contains unique, non-empty value.</p> 
 * 
 */
@SuppressWarnings("serial")
public class FindAllTags extends GenericCommand {
	
	private List<String> tags;
	
	private static HibernateCallback hcb = new FindTagsCallback();

	@SuppressWarnings("unchecked")
	public void execute() {
		// TODO rschuster: Sorting and creating distinct value is something the database could do on its
		// own if the data model would allow this. Unfortunately it is not done this way.
		
		List<String> tempTags = (List<String>) getDaoFactory().getDAO(BSIModel.class).findByCallback(hcb);
		
		HashSet<String> uniqueTags = new HashSet<String>();
		for (String elem : tempTags)
		{
			TagHelper.putInTags(uniqueTags, elem);
		}
		
		tags = new ArrayList<String>(uniqueTags);
		Collections.sort(tags);
	}

	public List<String> getTags() {
		return tags;
	}
	
	private static class FindTagsCallback implements HibernateCallback, Serializable
	{

		public Object doInHibernate(Session session) throws HibernateException,
				SQLException {
			/* Retrieves all tags. The resulting entries are non-empty and distinct.
			 * Unfortunately they are still in the CSV format, e.g. "foo, baz, bar"
			 */
			
			// TODO: Implicitly we assume that all propertytypes that denote a tag
			// have the common suffix '_tag'.
			return session
			.createSQLQuery(
					"select propertyValue " +
					"from properties " +
					"where propertytype like '%_tag'")
			.addScalar("propertyValue", Hibernate.STRING)
			.list();
		}
		
	}

	
	
}
