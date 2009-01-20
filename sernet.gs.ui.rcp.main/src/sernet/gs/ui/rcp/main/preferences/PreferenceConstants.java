package sernet.gs.ui.rcp.main.preferences;

import java.io.File;

/**
 * Constant definitions for plug-in preferences
 */
public class PreferenceConstants {

	// which method to use to access the BSI catalogues:
	public static final String GSACCESS = "cna_gsaccess"; //$NON-NLS-1$
	public static final String GSACCESS_DIR = "cna_gsaccess_url"; //$NON-NLS-1$
	public static final String GSACCESS_ZIP = "cna_gsaccess_zip"; //$NON-NLS-1$
	
	// locatio of ZIP-file or URL:
	public static final String BSIZIPFILE = "cna_bsizipfile"; //$NON-NLS-1$
	public static final String DSZIPFILE = "cna_dszipfile"; //$NON-NLS-1$
	public static final String BSIDIR = "cna_bsiurl"; //$NON-NLS-1$
	
	public static final String OODIR = "cna_oodir"; //$NON-NLS-1$
	public static final String OOTEMPLATE = "cna_ootemplate"; //$NON-NLS-1$
	public static final String OOTEMPLATE_TEXT = "cna_oodoctemplate"; //$NON-NLS-1$
	
	public static final String FIRSTSTART= "cna_derbywarning"; //$NON-NLS-1$
	public static final String ERRORPOPUPS = "cna_errorpopups"; //$NON-NLS-1$
	public static final String INPUTHINTS = "cna_inputhelperhints";

	public static final String DB_DRIVER = "cna_driver"; //$NON-NLS-1$
	public static final String DB_DRIVER_DERBY = "org.apache.derby.jdbc.EmbeddedDriver"; //$NON-NLS-1$
	public static final String DB_DRIVER_POSTGRES = "org.postgresql.Driver"; //$NON-NLS-1$
	public static final String DB_DRIVER_MYSQL = "org.gjt.mm.mysql.Driver"; //$NON-NLS-1$
	
	// Verinice DB:
	public static final String DB_USER = "cna_dbuser"; //$NON-NLS-1$
	public static final String DB_PASS = "cna_dbpass"; //$NON-NLS-1$
	
	public static final String DB_URL = "cna_dburl"; //$NON-NLS-1$
	public static final String DB_URL_DERBY = "jdbc:derby:%s/verinicedb;create=true"; //$NON-NLS-1$
	public static final String DB_URL_POSTGRES = "jdbc:postgresql://127.0.0.1:5432/verinicedb"; //$NON-NLS-1$
	public static final String DB_URL_MYSQL = "jdbc:mysql://127.0.0.1:3306/verinicedb"; //$NON-NLS-1$

	public static final String DB_DIALECT = "cna_dbdialect"; //$NON-NLS-1$
	public static final String DB_DIALECT_derby = "org.hibernate.dialect.DerbyDialect"; //$NON-NLS-1$
	public static final String DB_DIALECT_postgres= "org.hibernate.dialect.PostgreSQLDialect"; //$NON-NLS-1$
	public static final String DB_DIALECT_mysql = "org.hibernate.dialect.MySQLInnoDBDialect"; //$NON-NLS-1$
	
	
	//gstool db for import:
	public static final String GS_DB_USER = "gs_cna_dbuser"; //$NON-NLS-1$
	public static final String GS_DB_USER_DEFAULT = "sa"; //$NON-NLS-1$

	public static final String GS_DB_PASS = "gs_cna_dbpass"; //$NON-NLS-1$

	public static final String GS_DB_URL = "gs_cna_dburl"; //$NON-NLS-1$
	
	public static final String GS_DB_DRIVER_JTDS = "net.sourceforge.jtds.jdbc.Driver"; //$NON-NLS-1$
	public static final String GS_DB_DRIVER_ODBC = "sun.jdbc.odbc.JdbcOdbcDriver"; //$NON-NLS-1$

	public static final String GS_DB_DIALECT_JTDS = "org.hibernate.dialect.SQLServerDialect"; //$NON-NLS-1$
	public static final String GS_DB_DIALECT_ODBC = "sernet.gs.ui.rcp.gsimport.AccessDialect"; //$NON-NLS-1$

	// gstool uses port 1135, not 1433:
	public static final String GS_DB_URL_LOCALHOST = "jdbc:jtds:sqlserver://localhost:1135/BSIDB_V45"; //$NON-NLS-1$
	
	public static final String GSTOOL_FILE = "gs_cna_attachfile";
	public static final String GS_DB_ATTACHDB = "gs_cna_attachdb";
	
	public static final String GSTOOL_RESTOREDB_FILE = "gs_cna_restoredb_file";
	public static final String GS_DB_RESTOREDB_NAME = "gs_cna_restoredb_name";
	public static final String GS_DB_RESTOREDB_TODIR = "gs_cna_restoredb_todir";
	
	public static final String OPERATION_MODE = "gs_cna_operationmode";
	public static final String OPERATION_MODE_STANDALONE = "gs_cna_operationmode_standalone";
	public static final String OPERATION_MODE_WITHSERVER = "gs_cna_operationmode_withserver";
	
	public static final String VNSERVER_URI = "gs_cna_vnserver_uri";
	public static final String VNSERVER_URI_DEFAULT = "http://veriniceserver.private:2010/veriniceserver";
	public static final String VNSERVER_USER = "gs_cna_serveruser";
	public static final String VNSERVER_PASS = "gs_cna_serverpass";
	
	
	
	
}
