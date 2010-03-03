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
package sernet.gs.ui.rcp.main.service.migrationcommands;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.bsi.model.BSIModel;
import sernet.gs.ui.rcp.main.common.model.HydratorUtil;
import sernet.gs.ui.rcp.main.connect.IBaseDao;
import sernet.gs.ui.rcp.main.service.commands.GenericCommand;
import sernet.hui.common.connect.Entity;

/**
 * @author koderman[at]sernet[dot]de
 * @version $Rev$ $LastChangedDate$ $LastChangedBy$
 * 
 */
public class GetDbVersionID extends GenericCommand {

	private Double version = null;

	public Double getVersion() {
		return version;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see sernet.gs.ui.rcp.main.service.commands.ICommand#execute()
	 */
	public void execute() {

		IBaseDao<BSIModel, Serializable> dao = getDaoFactory().getDAO(
				BSIModel.class);

		try {
			List<Double> idIterator = (List<Double>) dao
					.findByCallback(new FindVersionCallback());
			if (idIterator == null) {
				return;
			}
			
			for (Double double1 : idIterator) {
				version = double1;
			}

		} catch (HibernateException he) {
			ExceptionUtil
					.log(he.getCause(), "Error during database migration.");
		}
	}

	private static class FindVersionCallback implements HibernateCallback,
			Serializable {

		public Object doInHibernate(Session session) throws HibernateException,
				SQLException {
			try {
				Query query = session
				.createSQLQuery("select elmt.dbversion from bsimodel elmt where elmt.dbversion is not null");
				return query.list();
			} catch (Exception e) {
				return null;
			}
		}

	}

}
