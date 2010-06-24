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
 *     Robert Schuster <r.schuster@tarent.de> - use custom SQL
 ******************************************************************************/
package sernet.gs.ui.rcp.main.service.migrationcommands;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

import sernet.verinice.interfaces.IBaseDao;
import sernet.verinice.model.bsi.BSIModel;

/**
 * Adds UUID to all required objects.
 * 
 * @author koderman[at]sernet[dot]de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
@SuppressWarnings("serial")
public class MigrateDbTo0_94 extends DbMigration {
	
	private static transient Logger log = Logger.getLogger(MigrateDbTo0_94.class);

    public static Logger getLog() {
        if (log == null) {
            log = Logger.getLogger(MigrateDbTo0_94.class);
        }
        return log;
    }
	
	private String[] tables = {
			"cnatreeelement",
			"finishedriskanalysislists",
			"entity",
			"propertylist",
			"risikomassnahme",
			"gefaehrdung"
	};
	
	@Override
	public double getVersion() {
		return 0.94D;
	}

	@SuppressWarnings("unchecked")
	public void execute() {
		IBaseDao<BSIModel, Serializable> dao = getDaoFactory().getDAO(BSIModel.class);
		
		// Retrieves all rows (ids only) of the tables mentioned in the array and sets the
		// uuid column in each row.
		for (String table : tables)
		{
			log.info("migrating table: " + table);
			
			try
			{
			List<Integer> idIterator = (List<Integer>) dao.findByCallback(new FindIdsCallback(table));
			dao.executeCallback(new CreateUuidCallback(table, idIterator));
			} catch (HibernateException he)
			{
				getLog().error("Error during database migration.", he);
			}
		}
		
		super.updateVersion();
	}

	private static class FindIdsCallback implements HibernateCallback, Serializable
	{
		private String table;
		
		
		FindIdsCallback(String table)
		{
			this.table = table;
		}

		public Object doInHibernate(Session session) throws HibernateException,
				SQLException {
			
				Query query = session.createSQLQuery(
						"select t.dbid as dbid from " + table +  " t")
						.addScalar("dbid");
				
				getLog().debug("generated query: " + query.getQueryString());

			return query.list();
		}
		
	}

	private static class CreateUuidCallback implements HibernateCallback, Serializable
	{
		private String table;
		
		private List<Integer> idIterator;
		
		CreateUuidCallback(String table, List<Integer> idIterator)
		{
			this.table = table;
			this.idIterator = idIterator;
		}

		public Object doInHibernate(Session session) throws HibernateException,
				SQLException {
			Connection connection = session.connection();
			connection.setAutoCommit(false);
			
			int i = 0;
			final int size = idIterator.size();
			Iterator<Integer> iterator = idIterator.iterator();
			while(iterator.hasNext())
			{
				Integer id = iterator.next();
				String uuidString = UUID.randomUUID().toString();
				
				/*
				 * We would like to use Hibernate SQL query mechanism but
				 * this does not work due to a bug in Hibernate caused by
				 * a memory leak.
				 * see http://opensource.atlassian.com/projects/hibernate/browse/HHH-2470

				Query query = session.createSQLQuery(
						"update " + table + " "
						+ "set uuid = :uuid "
						+ "where dbid = :id ")
						.setString("uuid", UUID.randomUUID().toString())
						.setInteger("id", id);
				*/
				
				// Create statements manually and commit every 10k rows. 
				Statement stmt = connection.createStatement();
				String sql = "update " + table
							+ " set uuid = '" + uuidString + "' "
							+ "where dbid = " + id;
				stmt.executeUpdate(sql);
				
				iterator.remove();

				// speed up debug logging:
				++i;
				if (log.isDebugEnabled() && (i % 500 == 0))
				{
					log.debug("migrating table [" + table + "] - processed elements: " + i + "/" + size);
				}
				
				if (i % 10000 == 0)
				{
					connection.commit();
				}
				
				/* Not used because of Hibernate bug. See above.
				
				query.executeUpdate();
				*/
			}
			
			// Commits after creating the final rows.
			connection.commit();
		
			return null;
		}
		
	}

}
