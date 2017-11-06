package sernet.verinice.service.commands.migration;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

import sernet.verinice.model.bsi.Attachment;

/**
 * This migration shall increase the length of the PROPERTYVALUE column for Derby.
 * In postgreSQL and Oracle the scheme already allows entries with 400,000 chars.
 *
 * Thought in derby it is not possible to alter columns types, otherwise the
 * following error occurs:
 *
 *     Error: Invalid type specified for column 'PROPERTYVALUE'. The type of a column may not be changed.
 *
 * Therefore we need to create a new column, copy the value etc. see
 *
 *     https://db.apache.org/derby/docs/10.9/ref/rrefsqljrenamecolumnstatement.html
 *
 * In addition we don't limit the length of the CLOB in this migration since
 * hibernate doesn't on creation of new schemes. If wee would add the same
 * limit as in postgre here, hibernate wouldn't for freshly create database
 * schemes and we would have inconsistent scheme for updated clients and fresh
 * installations.
 * 
 * @author Alexander Ben Nasrallah <an[at]sernet[dot]de>
 */
@SuppressWarnings("serial")
public class MigrateDbTo1_04D extends DbMigration {

    private transient Logger log;

    private static final List<String> DERBY_SQL_LIST;
    static {
        DERBY_SQL_LIST = new ArrayList<String>();
        DERBY_SQL_LIST.add("ALTER TABLE PROPERTIES ADD COLUMN PROPERTYVALUE_AS_CLOB CLOB");
        DERBY_SQL_LIST.add("UPDATE PROPERTIES SET PROPERTYVALUE_AS_CLOB = PROPERTYVALUE");
        DERBY_SQL_LIST.add("ALTER TABLE PROPERTIES DROP COLUMN PROPERTYVALUE");
        DERBY_SQL_LIST.add("RENAME COLUMN PROPERTIES.PROPERTYVALUE_AS_CLOB TO PROPERTYVALUE");
    }

    @Override
    public void execute() {
        getLog().debug("Updating db to Version: " + getVersion());
        // This migration only effects Apache Derby
        if (isDerby()) {
            updateDerby();
        }

        super.updateVersion();
    }

    private void updateDerby() {
        for (String sqlStatement : DERBY_SQL_LIST) {
            executeHibernateCallback(sqlStatement);
        }
    }

    /**
     * Executes given SQL query within the current hibernate session.
     */
    private int executeHibernateCallback(final String sql) {
        return (int) getDaoFactory().getDAO(Attachment.class).executeCallback(new HibernateCallback() {
            @Override
            public Integer doInHibernate(Session session) throws HibernateException, SQLException {
                int result = session.createSQLQuery(sql).executeUpdate();
                if (getLog().isDebugEnabled()) {
                    getLog().debug("Result of session.executeUpdate():\t" + result);
                }
                return result;
            }
        });
    }

    @Override
    public double getVersion() {
        return 1.04D;
    }

    private Logger getLog() {
        if (log == null)
            log = Logger.getLogger(MigrateDbTo1_04D.class);
        return log;
    }
}
