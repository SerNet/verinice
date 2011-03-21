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
package sernet.gs.ui.rcp.main.preferences;


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
	public static final String INPUTHINTS = "cna_inputhelperhints"; //$NON-NLS-1$
	public static final String INFO_CONTROLS_ADDED = "info_controls_added"; //$NON-NLS-1$
	public static final String INFO_ELEMENTS_COPIED = "info_elements_copied"; //$NON-NLS-1$
	public static final String INFO_ELEMENTS_CUT = "info_elements_cut";  //$NON-NLS-1$
	public static final String INFO_IMPORT_LDAP = "info_import_ldap"; //$NON-NLS-1$
    public static final String INFO_PROCESSES_STARTED = "info_processes_started"; //$NON-NLS-1$

	public static final String DB_DRIVER = "cna_driver"; //$NON-NLS-1$
	public static final String DB_DRIVER_DERBY = "org.apache.derby.jdbc.EmbeddedDriver"; //$NON-NLS-1$
	public static final String DB_DRIVER_POSTGRES = "org.postgresql.Driver"; //$NON-NLS-1$
	public static final String DB_DRIVER_MYSQL = "org.gjt.mm.mysql.Driver"; //$NON-NLS-1$
	public static final String DB_DRIVER_ORACLE = "oracle.jdbc.OracleDriver"; //$NON-NLS-1$
	
	// Verinice DB:
	public static final String DB_USER = "cna_dbuser"; //$NON-NLS-1$
	public static final String DB_PASS = "cna_dbpass"; //$NON-NLS-1$
	
	public static final String DB_URL = "cna_dburl"; //$NON-NLS-1$
	public static final String DB_URL_DERBY = "jdbc:derby:%s/verinicedb;create=true"; //$NON-NLS-1$
	public static final String DB_URL_POSTGRES = "jdbc:postgresql://127.0.0.1:5432/verinicedb"; //$NON-NLS-1$
	public static final String DB_URL_MYSQL = "jdbc:mysql://127.0.0.1:3306/verinicedb"; //$NON-NLS-1$

	public static final String DB_DIALECT = "cna_dbdialect"; //$NON-NLS-1$
	public static final String DB_DIALECT_derby = "sernet.verinice.hibernate.ByteArrayDerbyDialect"; //$NON-NLS-1$
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
	public static final String OPERATION_MODE_INTERNAL_SERVER = "gs_cna_operationmode_standalone";
	public static final String OPERATION_MODE_REMOTE_SERVER = "gs_cna_operationmode_withserver";
	
	public static final String VNSERVER_URI = "gs_cna_vnserver_uri";
	public static final String VNSERVER_URI_INTERNAL = "http://localhost:8800";
	public static final String VNSERVER_URI_DEFAULT = "http://localhost:8080/veriniceserver";
	public static final String VNSERVER_USER = "gs_cna_serveruser";
	public static final String VNSERVER_PASS = "gs_cna_serverpass";
	
	// additional gui preferences
	public static final String SWITCH_PERSPECTIVE = "switch_perspective";
	public static final String DONT_ASK_BEFORE_SWITCH_PERSPECTIVE = "switch_perspective_dont_ask";
	
	
	// catalog import preferences
	public static final String CHARSET_CATALOG = "org.verinice.iso27k.rcp.charset";
    
	// tags to filter HitroUI Editor fields / strict filter mode setting:
	public static final String HUI_TAGS = "hui_tags";
    public static final String HUI_TAGS_STRICT = "hui_tags_strict";
    
	/**
	 * Returns the DONT_ASK_BEFORE_SWITCH_PERSPECTIVE preference name for a view class.
	 * 
	 * @param clazz a view class
	 * @return DONT_ASK_BEFORE_SWITCH_PERSPECTIVE preference name
	 */
	public static String getDontAskBeforeSwitch(Class clazz) {
		return new StringBuilder(clazz.getName()).append("_").append(PreferenceConstants.DONT_ASK_BEFORE_SWITCH_PERSPECTIVE).toString();		
	}
	
	/**
	 * Returns the SWITCH_PERSPECTIVE preference name for a view class.
	 * 
	 * @param clazz a view class
	 * @return DONT_ASK_BEFORE_SWITCH_PERSPECTIVE preference name
	 */
	public static String getSwitch(Class clazz) {
		return new StringBuilder(clazz.getName()).append("_").append(PreferenceConstants.SWITCH_PERSPECTIVE).toString();		
	}
	
	
}
