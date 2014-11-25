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
 * This class is used as a "InitializingBean" in spring context 
 * and called just before the Hibernate session factory bean is instantiated.
 * 
 * @author koderman[at]sernet[dot]de
 * @version $Rev$ $LastChangedDate$ $LastChangedBy$
 * 
 */

// TODO akoderman remove this and integrate LiquidBase changelog instead

public class SchemaCreator implements InitializingBean {
	
	private final Logger log = Logger.getLogger(this.getClass());
	
	private static final String SQL_GETDBVERSION_PRE_096     = "select dbversion from bsimodel";
    private static final String SQL_GETDBVERSION_POST_096 = "select dbversion from cnatreeelement where object_type='bsimodel'";
    
    private static final String SQL_VER_095_096 = "sernet/gs/server/hibernate/update-095-096.sql";
	
	private DataSource dataSource;
	
	private IDBUpdate dbUpdate97To98;
    
    private IDBUpdate dbUpdate98To99;

    private static final double TOLERATED_ERROR = .0001;

	/**
	 * Use this mnethod to update database.
	 * Method called just before the Hibernate session factory bean is instantiated.
	 * So you can not use hibernate in this method.
	 * 
	 * Use Springs JdbcTemplate instead:
	 *   JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
     *   jdbcTemplate.execute(query);
     *
     * DataSource is injected by Spring configuration.
	 *  
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	@Override
    public void afterPropertiesSet() throws IOException {
		log.debug("afterPropertiesSet");
		
		double dbVersion = determineDbVersion();
		
		if (Math.abs(dbVersion - -1D) < TOLERATED_ERROR) {
			log.debug("No database version defined, no database created yet?");
			return;
		}

		try {
			if (dbVersion < 0.95D) {
				log.error("Db version is: " + dbVersion + ". Can not upgrade from version below 0.95 directly. Use older version of verinice first (i.e. V 1.0.16) !");
				throw new RuntimeException("Db version is: " + dbVersion + ". Can not upgrade from version below 0.95 directly. Use older version of verinice first (i.e. V 1.0.16) !");
			}
			if (Math.abs(dbVersion - 0.95D) < TOLERATED_ERROR) {
			    if (log.isInfoEnabled()) {
		            log.info("Updating database from version 0.95 to 0.96");
		        }
				updateDbVersion(SQL_VER_095_096);
			}
			if (Math.abs(dbVersion - 0.97D) < TOLERATED_ERROR) {
			    if (log.isInfoEnabled()) {
		            log.info("Updating database from version 0.97 to 0.98");
		        }
			    getDbUpdate97To98().update();
            }
			if (Math.abs(dbVersion - 0.98D) < TOLERATED_ERROR) {
                if (log.isInfoEnabled()) {
                    log.info("Updating database from version 0.98 to 0.99");
                }
                getDbUpdate98To99().update();
            }
		} catch (RuntimeException e) {
		    log.error("Exception while updating database, v < 0.95", e);
		    // dont't throw an exception here to continue server start
		} catch (IOException e){
		    log.error("Exception while updatind database, v == 0.95", e);
		}
		
	}

    /**
	 * @param d
	 * @throws IOException 
	 */
	private void updateDbVersion(String sqlFile) throws IOException {
		InputStream stream = getClass().getClassLoader().getResourceAsStream(sqlFile);

		InputStreamReader read = null;
		BufferedReader buffRead = null;
		
		try {
    		read = new InputStreamReader(stream, "iso-8859-1"); //$NON-NLS-1$
    		buffRead = new BufferedReader(read);
    		String query;
    
    		while ((query = buffRead.readLine()) != null) {
    			if (query.matches("^$") || query.matches("^--")){
    				continue;
    			}
    			// remove ; for Derby
    			if(query.endsWith(";")) {
    				query = query.substring(0, query.length()-1);
    			}
    			if (log.isDebugEnabled()) {
    			    log.debug("Executing query: " + query);
    			}
    			JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
    			jdbcTemplate.execute(query);
    		}
		} finally {
		    if(buffRead!=null){
		        buffRead.close();
		    }
		    if(read!=null) {
		        read.close();
		    }
		}
	}
	// FIXME akoderman remove / add remaining constraints 

	/**
	 * @return
	 */
	private double determineDbVersion() {
		log.debug("determineDbVersion");
		Double dbVersion = -1D;
		try {
			// check for db schema < 0.9.6:
			JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
			dbVersion = (Double) jdbcTemplate.queryForObject(SQL_GETDBVERSION_PRE_096, Double.class);
		} catch (Exception e) {
			//  empty !
			if (log.isDebugEnabled()) {
			    log.debug("Can not determine db-version. Database is new and empty or version is > 0.95.");
				log.debug("stacktrace: ", e);
			}
		}
		
		try {
			// try again for db schema > 0.9.6:
			if (dbVersion == -1D) {
				JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
				dbVersion = (Double) jdbcTemplate.queryForObject(SQL_GETDBVERSION_POST_096, Double.class);
			}
		} catch (Exception e) {
			log.info("Can not determine db-version. Database is new and empty or unknown error occurred.");
			if (log.isDebugEnabled()) {
				log.debug("stacktrace: ", e);
			}
		}
		
		return dbVersion;
	}
	
	public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public IDBUpdate getDbUpdate97To98() {
        return dbUpdate97To98;
    }

    public void setDbUpdate97To98(IDBUpdate dbUpdate97To98) {
        this.dbUpdate97To98 = dbUpdate97To98;
    }

    /**
     * @return the dbUpdate98To99
     */
    public IDBUpdate getDbUpdate98To99() {
        return dbUpdate98To99;
    }

    /**
     * @param dbUpdate98To99 the dbUpdate98To99 to set
     */
    public void setDbUpdate98To99(IDBUpdate dbUpdate98To99) {
        this.dbUpdate98To99 = dbUpdate98To99;
    }


}
