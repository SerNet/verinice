package sernet.verinice.service.commands.migration;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

import sernet.verinice.model.bsi.Attachment;
import sernet.verinice.model.bsi.risikoanalyse.OwnGefaehrdung;
import sernet.verinice.model.bsi.risikoanalyse.RisikoMassnahme;

/**
 * migrates verinice db to version 1.02D. Ensures that field description of {@link RisikoMassnahme}
 * and field beschreibung of {@link OwnGefaehrdung} are mapped to maximum values as done in their
 * hibernate mappings also, if the database is existant already and changed hibernatemappings
 * do not have any influence on the size of the columns.
 * In case of Oracle and Derby db, a temporary column with the correct (new) size will be created
 * which gets all the content of the existant column. after that the old column will be removed
 * and the temporary column gets the name of the old column set. this needs to be done, since both 
 * databases do not allow to change size of existant (not empty, in case of Oracle) columns.
 * @author Sebastian Hagedorn <sh[at]sernet[dot]de>
 */
@SuppressWarnings("serial")
public class MigrateDbTo1_02D extends DbMigration {

    private transient Logger log;

    private static final List<String> DERBY_SQL_LIST;
    private static final List<String> ORACLE_SQL_LIST;

    private static final String MASSNAHME_TMP_COLUMN_NAME = "TMP_DESC_MASS";

    static {
        DERBY_SQL_LIST = new ArrayList<String>();
        DERBY_SQL_LIST.add("ALTER TABLE RISIKOMASSNAHME ADD COLUMN " + MASSNAHME_TMP_COLUMN_NAME + " VARCHAR (32672)");
        DERBY_SQL_LIST.add("UPDATE RISIKOMASSNAHME SET " +  MASSNAHME_TMP_COLUMN_NAME + "=DESCRIPTION");
        DERBY_SQL_LIST.add("ALTER TABLE RISIKOMASSNAHME DROP COLUMN DESCRIPTION");
        DERBY_SQL_LIST.add("RENAME COLUMN RISIKOMASSNAHME." + MASSNAHME_TMP_COLUMN_NAME +" TO DESCRIPTION");
    }
    
    // NCLOBs in Oracle are sized max length 4000 automatically
    // since property.propertyvalue is mapped the same we are using it here to
    static {
        ORACLE_SQL_LIST = new ArrayList<String>();
        ORACLE_SQL_LIST.add("ALTER TABLE RISIKOMASSNAHME add " + MASSNAHME_TMP_COLUMN_NAME + " NCLOB");
        ORACLE_SQL_LIST.add("UPDATE RISIKOMASSNAHME SET " + MASSNAHME_TMP_COLUMN_NAME + "=DESCRIPTION");
        ORACLE_SQL_LIST.add("ALTER TABLE RISIKOMASSNAHME DROP COLUMN DESCRIPTION");
        ORACLE_SQL_LIST.add("ALTER TABLE RISIKOMASSNAHME RENAME COLUMN " + MASSNAHME_TMP_COLUMN_NAME + " TO DESCRIPTION");
    }
    
    private static final String POSTGRE_SQL = "ALTER TABLE risikomassnahme ALTER COLUMN description TYPE varchar(400000);";

    @Override
    public void execute() {

        alterDatabases();

        super.updateVersion();

    }

    private void alterDatabases() {
        String dialect = getHibernateDialect();
        if (getLog().isDebugEnabled()) {
            getLog().debug("Updating db to Version: " + getVersion() + " using dialect: " + dialect);
        }
        if (HIBERNATE_DIALECT_DERBY.equals(dialect)) {
            alterDerby();
        } else if (HIBERNATE_DIALECT_POSTGRSQL.equals(dialect)) {
            alterPostgres();
        } else if (HIBERNATE_DIALECT_ORACLE.equals(dialect)) {
            alterOracle();
        } else {
            getLog().error("configured Hibernate Dialect is not supported by this migration. please contact support and ask for customized help to migrate to db 1.02D");
        }

    }

    private void alterDerby() {
        for (String sqlStatement : DERBY_SQL_LIST) {
            executeHibernateCallback(sqlStatement);
        }
    }

    private void alterPostgres() {
        executeHibernateCallback(POSTGRE_SQL);

    }

    private void alterOracle() {
        for (String sqlStatement : ORACLE_SQL_LIST) {
            executeHibernateCallback(sqlStatement);
        }
    }

    /**
     * executes given sql query within the current hibernate session
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
        return 1.02D;
    }

    private Logger getLog() {
        if (log == null)
            log = Logger.getLogger(MigrateDbTo1_02D.class);
        return log;
    }
}
