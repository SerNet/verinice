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
 ******************************************************************************/
package sernet.gs.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Adapts schema for DB updates for cases where Hibernate hbm2ddl does not work on its own.
 * This class is used as a bean in spring context and called just before the Hibernate session factory bean is instantiated.
 * 
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ $LastChangedBy$
 * 
 */

// TODO akoderman remove this and integrate LiquidBase changelog instead

public class SchemaCreator implements InitializingBean {
	private DataSource dataSource;
	
	private static String SQL_GETDBVERSION_PRE_096 	= "select  dbversion from bsimodel";
	private static String SQL_GETDBVERSION_POST_096 = "select dbversion from cnatreeelement";
	
	private static String SQL_Ver_095_096 = "sernet/gs/server/hibernate/update-095-096.sql";

	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public void afterPropertiesSet() throws Exception {
		double dbVersion = determineDbVersion();
		
		if (dbVersion == -1D) {
			Logger.getLogger(this.getClass()).debug("No database version defined, no database created yet?");
			return;
		}

		try {
			if (dbVersion == 0.95D) {
				updateDbVersion(SQL_Ver_095_096);
			}
		} catch (Exception e) {
			throw new RuntimeException("Konnte Datenbank-Schema nicht aktualisieren.", e);
		}
		
	}

	/**
	 * @param d
	 * @throws IOException 
	 */
	private void updateDbVersion(String sqlFile) throws IOException {
		InputStream stream = getClass().getClassLoader().getResourceAsStream(sqlFile);

		InputStreamReader read = new InputStreamReader(stream, "iso-8859-1"); //$NON-NLS-1$
		BufferedReader buffRead = new BufferedReader(read);
		String query;

		while ((query = buffRead.readLine()) != null) {
			if (query.matches("^$") || query.matches("^--"))
				continue;

			if (Logger.getLogger(this.getClass()).isDebugEnabled())
				Logger.getLogger(this.getClass()).debug("Executing query: " + query);
			JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
			jdbcTemplate.execute(query);
		}
	}
	// FIXME akoderman remove / add remaining constraints 

	/**
	 * @return
	 */
	private double determineDbVersion() {
		Double dbVersion = -1D;
		try {
			JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
			dbVersion = (Double) jdbcTemplate.queryForObject(SQL_GETDBVERSION_PRE_096, Double.class);
		} catch (Exception e) {
			// do nothing (i.e. table may have been removed, see below)
		}
		
		try {
			// try again for new db schema:
			if (dbVersion == -1D) {
				JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
				dbVersion = (Double) jdbcTemplate.queryForObject(SQL_GETDBVERSION_POST_096, Double.class);
			}
		} catch (Exception e) {
			// do nothing, i.e. db may not exist at all yet, will be initialized by hibernate hbm2ddl
		}
		
		return dbVersion;
	}


}
