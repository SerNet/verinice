package sernet.gs.ui.rcp.main.preferences;

/**
 * Constant definitions for plug-in preferences
 */
public class PreferenceConstants {

	// which method to use to access the BSI catalogues:
	public static final String GSACCESS = "cna_gsaccess";
	public static final String GSACCESS_DIR = "cna_gsaccess_url";
	public static final String GSACCESS_ZIP = "cna_gsaccess_zip";
	
	// locatio of ZIP-file or URL:
	public static final String BSIZIPFILE = "cna_bsizipfile";
	public static final String BSIDIR = "cna_bsiurl";
	
	public static final String OODIR = "cna_oodir";
	public static final String OOTEMPLATE = "cna_ootemplate";
	public static final String OOTEMPLATE_TEXT = "cna_oodoctemplate";
	
	public static final String FIRSTSTART= "cna_derbywarning";
	public static final String ERRORPOPUPS = "cna_errorpopups";

	public static final String DB_DRIVER = "cna_driver";
	public static final String DB_DRIVER_DERBY = "org.apache.derby.jdbc.EmbeddedDriver";
	public static final String DB_DRIVER_POSTGRES = "org.postgresql.Driver";
	public static final String DB_DRIVER_MYSQL = "org.gjt.mm.mysql.Driver";
	
	public static final String DB_USER = "cna_dbuser";
	public static final String DB_PASS = "cna_dbpass";
	
	public static final String DB_URL = "cna_dburl";
	public static final String DB_URL_DERBY = "jdbc:derby:%s/verinicedb;create=true";
	public static final String DB_URL_POSTGRES = "jdbc:postgresql://127.0.0.1:5432/verinicedb";
	public static final String DB_URL_MYSQL = "jdbc:mysql://127.0.0.1:3306/verinicedb";
	
	public static final String DB_DIALECT = "cna_dbdialect";
	public static final String DB_DIALECT_derby = "org.hibernate.dialect.DerbyDialect";
	public static final String DB_DIALECT_postgres= "org.hibernate.dialect.PostgreSQLDialect";
	public static final String DB_DIALECT_mysql = "org.hibernate.dialect.MySQLInnoDBDialect";
	
	
}
