/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak@sernet.de>.
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
 *     Alexander Koderman <ak@sernet.de> - initial API and implementation
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

import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.bsi.model.BSIModel;
import sernet.gs.ui.rcp.main.bsi.model.DocumentLink;
import sernet.gs.ui.rcp.main.bsi.model.DocumentLinkRoot;
import sernet.gs.ui.rcp.main.bsi.model.DocumentReference;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.connect.IBaseDao;
import sernet.gs.ui.rcp.main.service.ICommandService;
import sernet.gs.ui.rcp.main.service.commands.CommandException;
import sernet.gs.ui.rcp.main.service.commands.GenericCommand;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadCnAElementByEntityId;
import sernet.hui.swt.widgets.URL.URLUtil;

/**
 * Retrieves the properties which are URLs as a {@link DocumentLinkRoot}
 * structure.
 * 
 * <p>The command works much faster when it is not neccessary to retrieve
 * the {@link CnATreeElement} instances which use a particular link. This
 * behavior is of interest when one only needs a list of used links and
 * is not interested in who uses them.</p>
 */
public class FindURLs extends GenericCommand {

	private static final long serialVersionUID = 9207422070204886804L;

	private static final Logger log = Logger.getLogger(FindURLs.class);

	private HibernateCallback hcb = new FindURLsCallback();

	private HibernateCallback hcb2 = new FindURLsCallbackWithCnATreeElement();

	private Set<String> allIDs;
	private DocumentLinkRoot root = new DocumentLinkRoot();

	private boolean prepareElementRefs;

	public FindURLs(Set<String> allIDs, boolean prepareElementRefs) {
		this.allIDs = allIDs;
		this.prepareElementRefs = prepareElementRefs;
	}

	@SuppressWarnings("unchecked")
	public void execute() {
		IBaseDao<BSIModel, Serializable> dao = getDaoFactory().getDAO(BSIModel.class);
		
		if (prepareElementRefs)
		{
			/* Requests the links and resolves the CnATreeElement instances which use
			 * them. (This is needed for the {@link DocumentView}).
			 */
			List<Object[]> resultList = (List<Object[]>) dao.findByCallback(hcb2);
			
			ICommandService cs = getCommandService();
			
			for (Object[] result : resultList)
			{
				String rawURL = (String) result[0];
				Integer id = (Integer) result[1];
				
				String name = URLUtil.getName(rawURL);
				String url = URLUtil.getHref(rawURL);
				
				DocumentLink link = root.getDocumentLink(name, url);
				if (link == null) {
					link = new DocumentLink(name, url);
					root.addChild(link);
				}

				LoadCnAElementByEntityId command = new LoadCnAElementByEntityId(id);
				try {
					command = cs.executeCommand(command);
				} catch (CommandException e) {
					ExceptionUtil.log(e, "Fehler beim Laden der URLs.");
				}
				
				CnATreeElement element = (CnATreeElement) command.getElements().get(0);
				
				DocumentReference reference = new DocumentReference(element);
				element.getTitel();
	
				link.addChild(reference);
			}
		}
		else
		{
			/* Requests only the links and skip resolving the CnATreeElement instances
			 * which use them. (This is needed for the combo box in the editor for the
			 * links.
			 */
			List<String> rawURLs = (List<String>) dao.findByCallback(hcb);

			for (String rawURL : rawURLs)
			{
				String name = URLUtil.getName(rawURL);
				String url = URLUtil.getHref(rawURL);
				
				DocumentLink link = root.getDocumentLink(name, url);
				if (link == null) {
					link = new DocumentLink(name, url);
					root.addChild(link);
				}
			}
			
		}
	}

	public Set<String> getAllIDs() {
		return allIDs;
	}

	public DocumentLinkRoot getUrls() {
		return root;
	}

	private class FindURLsCallback implements HibernateCallback, Serializable {
		private static final long serialVersionUID = 4738281794545102652L;

		@SuppressWarnings("unchecked")
		public Object doInHibernate(Session session) throws HibernateException,
				SQLException {
			List<String> list = new ArrayList<String>();

			/**
			 * Retrieves only the property elements which are URLs.
			 */
			for (String type : allIDs) {
				List<String> result = (List<String>) session.createSQLQuery(
						"select distinct propertyValue "
							+ "from properties "
							+ "where propertytype = :type "
							+ "and propertyvalue != ''")
						.addScalar("propertyvalue", Hibernate.STRING)
						.setString("type", type).list();

				list.addAll(result);
			}

			return list;
		}

	}

	private class FindURLsCallbackWithCnATreeElement implements
			HibernateCallback, Serializable {
		private static final long serialVersionUID = 4738281794545102652L;

		@SuppressWarnings("unchecked")
		public Object doInHibernate(Session session) throws HibernateException,
				SQLException {
			List<Object[]> list = new ArrayList<Object[]>();

			/**
			 * Retrieves the property elements which are URLs along with
			 * the CnATreeElement id that uses it.
			 */
			 Query query = session.createSQLQuery(
					"select p.propertyValue, e.dbid "
							+ "from properties p, entity e "
							+ "where p.propertytype in (:types) "
							+ "and p.propertyvalue != '' "
							+ "and p.parent = e.dbid ")
					.addScalar("propertyvalue", Hibernate.STRING)
					.addScalar("dbid", Hibernate.INTEGER)
					.setParameterList("types", allIDs, Hibernate.STRING);
							
			 log.debug("created statement: " + query.getQueryString());

			return (List<Object[]>) query.list();
		}

	}

}
