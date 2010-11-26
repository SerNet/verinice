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
 *     Robert Schuster <r.schuster@tarent.de> - use custom SQL for query
 ******************************************************************************/
package sernet.gs.ui.rcp.main.service.taskcommands;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

import sernet.hui.common.connect.HuiUrl;
import sernet.hui.swt.widgets.URL.URLUtil;
import sernet.verinice.interfaces.GenericCommand;
import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.bsi.BSIModel;

/**
 * Retrieves the properties which are URLs as a list of {@link HuiUrl}
 * instances.
 */
public class FindHuiUrls extends GenericCommand {

	private static final long serialVersionUID = 3230058749744891441L;

	private static final Logger log = Logger.getLogger(FindHuiUrls.class);

	private HibernateCallback hcb = new FindURLsCallback();

	private Set<String> allIDs;

	private List<HuiUrl> list = new ArrayList<HuiUrl>();

	public FindHuiUrls(Set<String> allIDs) {
		this.allIDs = allIDs;
	}

	@SuppressWarnings("unchecked")
	public void execute() {
		IBaseDao<BSIModel, Serializable> dao = getDaoFactory().getDAO(
				BSIModel.class);

		/*
		 * Requests only the links and skip resolving the CnATreeElement
		 * instances which use them. (This is needed for the combo box in the
		 * editor for the document links.
		 */
		List<String> rawURLs = (List<String>) dao.findByCallback(hcb);

		for (String rawURL : rawURLs) {
			String name = URLUtil.getName(rawURL);
			String url = URLUtil.getHref(rawURL);

			list.add(new HuiUrl(name, url));
		}
	}

	public List<HuiUrl> getList() {
		return list;
	}

	@SuppressWarnings("serial")
	private class FindURLsCallback implements HibernateCallback, Serializable {

		@SuppressWarnings("unchecked")
		public Object doInHibernate(Session session) throws HibernateException,
				SQLException {

			/**
			 * Retrieves only the property elements which are URLs and
			 * have a non-enmpty value.
			 */
			Query query = session.createSQLQuery(
					"select distinct propertyValue "
					+ "from properties "
					+ "where propertytype in (:types) "
					+ "and propertyvalue != ''")
					.addScalar("propertyvalue", Hibernate.STRING)
					.setParameterList("types", allIDs, Hibernate.STRING);

			if (log.isDebugEnabled())
				log.debug("created statement: " + query.getQueryString());

			return (List<String>) query.list();
		}

	}

}
