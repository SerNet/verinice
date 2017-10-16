/*******************************************************************************
 * Copyright (c) 2015 Sebastian Hagedorn <sh@sernet.de>.
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
 *     Sebastian Hagedorn <sh@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.service.commands.migration;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

import sernet.hui.common.connect.HUITypeFactory;
import sernet.hui.common.connect.PropertyType;
import sernet.verinice.model.bsi.Attachment;

/**
 * checks all attachment entites for the existenz of filesize property, adds if needed
 */
@SuppressWarnings("serial")
public class MigrateDbTo1_01D extends DbMigration {
    
    private static final String ORACLE_TEMP_TABLE_NAME = "TEMP_FILESIZE_TABLE";
    
    private transient Logger log;

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.ICommand#execute()
     */
    @Override
    public void execute() {
        long callBackStartTime = System.currentTimeMillis();
        try {
            List<Object[]> idToSizeList = getIdSizeList();
            if (getLog().isDebugEnabled()) {
                getLog().debug("Time for executing callback:\t" + String.valueOf(((System.currentTimeMillis() - callBackStartTime) / 1000)) + "seconds");
            }
            addFileSizeToAttachments(idToSizeList);
            if (isOracle()) {
                removeTempTable();
            }
        } catch (Exception e) {
            handleError(e, "Something went wrong");
        }

        if (getLog().isDebugEnabled()) {
            getLog().debug("Time for updating all entities:\t" + String.valueOf(((System.currentTimeMillis() - callBackStartTime) / 1000)) + "seconds");
        }

        super.updateVersion();

    }

    /**
     * removes table with name ORACLE_TEMP_TABLE_NAME 
     * (created if HIBERNATE_DIALECT_ORACLE is used by hibernate)
     */
    private void removeTempTable(){
        getDaoFactory().getDAO(Attachment.class).executeCallback(new HibernateCallback() {
            
            @Override
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                session.createSQLQuery(getDeleteTempTableSQL()).executeUpdate();
                return null;
            }
        });
    }

    /**
     * @param idToSizeList
     */
    private void addFileSizeToAttachments(List<Object[]> idToSizeList) {
        for(Object[] element : idToSizeList){
            if(getLog().isDebugEnabled()){
                getLog().debug("Updating Attachment (" + idToSizeList.indexOf(element) + "/" + idToSizeList.size() + ")");
            }
            int dbid = 0;
            int filesize = -1;
            if(element[0] instanceof Integer){
                dbid = ((Integer)element[0]).intValue();
            } else if (element[0] instanceof BigDecimal){
                dbid = ((BigDecimal)element[0]).intValue();
            }
            if(element[1] instanceof Integer){
                filesize = ((Integer)element[1]).intValue();
            } else if(element[1] instanceof BigDecimal){
                filesize = ((BigDecimal)element[1]).intValue();
            }
            if(dbid != 0 && filesize >= 0){
                getDaoFactory().getDAO(Attachment.class).saveOrUpdate(updateAttachment(dbid, filesize));
            }
        }
    }

    /**
     * writes filesize property to attachment (given by dbid) entity  
     * property is created if not existant, overriden otherwise
     * @param dbid
     * @param filesize
     * @return
     */
    private Attachment updateAttachment(int dbid, int filesize) {
        Attachment a = getDaoFactory().getDAO(Attachment.class).findById(dbid);
        if(a.getFileSize() == null){
            PropertyType newType = HUITypeFactory.getInstance()
                    .getEntityType(a.getEntity().getEntityType())
                    .getPropertyType(Attachment.PROP_SIZE);
            a.getEntity().createNewProperty(newType, String.valueOf(filesize));
        }
        return a;
    }

    /**
     * returns a list of Object[] that contains key value pairs: {dbid, filesize}
     * @return
     */
    @SuppressWarnings({"unchecked"})
    private List<Object[]> getIdSizeList() {
        return (List<Object[]>)getDaoFactory().getDAO(Attachment.class).executeCallback(new HibernateCallback() {
            
            @Override
            public Object doInHibernate(Session session) throws HibernateException, SQLException {

                if(getLog().isDebugEnabled()){
                    getLog().debug("Configured Hibernate Dialect:\t" + getHibernateDialect());
                }
                String sql = determineDialectSpecificQueryText(getHibernateDialect());
                return getFilesizeDataFromDB(session, sql);
            }

            /**
             * @param session
             * @param sql
             * @return
             * @throws SQLException
             */
            private Object getFilesizeDataFromDB(Session session, String sql) throws SQLException {
                if(! sql.isEmpty()){
                    if(!isOracle()){
                        return session.createSQLQuery(sql).list();
                    } else {
                        return handleOracleDB(session, sql);
                    }
                } else {
                    getLog().warn("Unsupported dialect (" + getHibernateDialect() + ") configured.\nPlease use one of the supported (Oracle, Postgresql or Derby)");
                    return new ArrayList<Object[]>(0);
                }
            }

            /**
             * @param session
             * @param sql
             * @return
             * @throws SQLException
             */
            private Object handleOracleDB(Session session, String sql) throws SQLException {
                session.createSQLQuery(sql).executeUpdate();
                Object retVal = null;
                long start_time = System.currentTimeMillis();
                final long MAX_DURATION = 180000; // wait max 3 mins before canceling 
                while(retVal == null && ((System.currentTimeMillis() - start_time) < MAX_DURATION) ){
                    try{
                        retVal = session.createSQLQuery("select dbid, dbms_lob.getlength(obj) from " + ORACLE_TEMP_TABLE_NAME).list(); 
                    } catch (Exception e){
                        // do nothing
                        getLog().warn("table not created yet, trying again", e);
                    }
                }
                if(System.currentTimeMillis() - start_time > MAX_DURATION){
                    throw new SQLException("Oracle DB takes to long to answer");
                }
                return retVal;
            }
        });
    }

    /**
     * gets filesize of filedata selecting query dependent on used database dialect
     * @param dialect
     * @return
     */
    private String determineDialectSpecificQueryText(String dialect) {
        if (isPostgres()) {
            return "select dbid, BIT_LENGTH(filedata) from note";
        } else if (isOracle()) {
            return "create table " + ORACLE_TEMP_TABLE_NAME + " as select dbid, to_lob(filedata) obj from note";
        } else if (isDerby()) {
            return "select dbid, length(filedata) from note";
        } else {
            return "";
        }
    }
    
    private String getDeleteTempTableSQL() {
        return "drop table " + ORACLE_TEMP_TABLE_NAME;
    }
    
    /* (non-Javadoc)
     * @see sernet.gs.ui.rcp.main.service.migrationcommands.DbMigration#getVersion()
     */
    @Override
    public double getVersion() {
        return 1.01D;
    }

    private void handleError(Exception ex, String message) {
        getLog().error(message, ex);
        throw new RuntimeException(message);
    }

    private Logger getLog() {
        if (log == null)
            log = Logger.getLogger(MigrateDbTo1_01D.class);
        return log;
    }
}
