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

import sernet.verinice.rcp.search.SearchView;

/**
 * Constant definitions for plug-in preferences
 */
public class PreferenceConstants {

    // which method to use to access the BSI catalogues:
    public static final String GSACCESS = "cna_gsaccess"; //$NON-NLS-1$
    public static final String GSACCESS_DIR = "cna_gsaccess_url"; //$NON-NLS-1$
    public static final String GSACCESS_ZIP = "cna_gsaccess_zip"; //$NON-NLS-1$

    // location of ZIP-file or URL:
    public static final String BSIZIPFILE = "cna_bsizipfile"; //$NON-NLS-1$
    public static final String DSZIPFILE = "cna_dszipfile"; //$NON-NLS-1$
    public static final String BSIDIR = "cna_bsiurl"; //$NON-NLS-1$

    public static final String FIRSTSTART = "cna_derbywarning"; //$NON-NLS-1$
    public static final String ERRORPOPUPS = "cna_errorpopups"; //$NON-NLS-1$
    public static final String INPUTHINTS = "cna_inputhelperhints"; //$NON-NLS-1$
    public static final String INFO_CONTROLS_ADDED = "info_controls_added"; //$NON-NLS-1$
    public static final String INFO_CONTROLS_TRANSFORMED_TO_MODERNIZED_GS = "info_controls_transformed_to_modernized_gs"; //$NON-NLS-1$
    public static final String INFO_ELEMENTS_COPIED = "info_elements_copied"; //$NON-NLS-1$
    public static final String INFO_ELEMENTS_CUT = "info_elements_cut"; //$NON-NLS-1$
    public static final String INFO_IMPORT_LDAP = "info_import_ldap"; //$NON-NLS-1$v
    public static final String INFO_PROCESSES_STARTED = "info_processes_started"; //$NON-NLS-1$
    public static final String INFO_PROCESS_VALIDATE = "info_processes_validate"; //$NON-NLS-1$
    public static final String INFO_STATUS_DERIVED = "info_status_derived"; //$NON-NLS-1$
    public static final String INFO_SEARCH_ELEMENT_NOT_FOUND = "search_element_not_found"; //$NON-NLS-1$
    public static final String INFO_BP_MODELING_CONFIRMATION = "info_bp_modeling_confirmation"; //$NON-NLS-1$
    public static final String SHOW_ALIEN_DECORATOR = "show_alien_decorator"; //$NON-NLS-1$
    public static final String SHOW_DBID_DECORATOR = "show_dbid_decorator"; //$NON-NLS-1$
    public static final String SHOW_GSMISM_DECORATOR = "show_gsmism_decorator"; //$NON-NLS-1$
    public static final String SHOW_RISK_ANALYSIS_DECORATOR = "show_risk_analysis_decorator"; //$NON-NLS-1$
    public static final String USE_AUTOMATIC_VALIDATION = "use_automatic_validation"; //$NON-NLS-1$
    public static final String SHOW_REPORT_VALIDATION_WARNING = "show_report_validation_warning"; //$NON-NLS-1$
    public static final String USE_VALIDATION_GUI_HINTS = "use_validation_gui_hints"; //$NON-NLS-1$
    public static final String SHOW_UPDATE_NEWS_DIALOG = "show_update_news_dialog"; //$NON-NLS-1$

    public static final String DB_DRIVER = "cna_driver"; //$NON-NLS-1$
    public static final String DB_DRIVER_DERBY = "org.apache.derby.jdbc.EmbeddedDriver"; //$NON-NLS-1$
    public static final String DB_DRIVER_POSTGRES = "org.postgresql.Driver"; //$NON-NLS-1$
    public static final String DB_DRIVER_ORACLE = "oracle.jdbc.OracleDriver"; //$NON-NLS-1$

    // Verinice DB:
    public static final String DB_USER = "cna_dbuser"; //$NON-NLS-1$
    public static final String DB_PASS = "cna_dbpass"; //$NON-NLS-1$

    public static final String DB_URL = "cna_dburl"; //$NON-NLS-1$
    public static final String DB_URL_DERBY = "jdbc:derby:%s/verinicedb;create=true"; //$NON-NLS-1$
    public static final String DB_URL_POSTGRES = "jdbc:postgresql://127.0.0.1:5432/verinicedb"; //$NON-NLS-1$

    public static final String DB_DIALECT = "cna_dbdialect"; //$NON-NLS-1$
    public static final String DB_DIALECT_DERBY = "sernet.verinice.hibernate.ByteArrayDerbyDialect"; //$NON-NLS-1$
    public static final String DB_DIALECT_POSTGRE = "org.hibernate.dialect.PostgreSQLDialect"; //$NON-NLS-1$

    // gstool db for import:
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
    public static final String VNSERVER_URI_INTERNAL_PORT= "8080";
    public static final String VNSERVER_HOST_INTERNAL ="localhost";
    public static final String VNSERVER_SCHEME_INTERNAL ="http";
    public static final String VNSERVER_URI_DEFAULT = "http://localhost:8080/veriniceserver";
    public static final String VNSERVER_USER = "gs_cna_serveruser";
    public static final String VNSERVER_PASS = "gs_cna_serverpass";

    // all things crypto
    public static final String CRYPTO_VERINICE_SSL_SECURITY_ENABLED = "gs_cna_verinice_ssl_security_enabled";
    public static final String CRYPTO_PKCS11_LIBRARY_PATH = "gs_cna_pkcs11_library_path";
    public static final String CRYPTO_TRUSTSTORE_FILE = "gs_cna_truststore_file";
    public static final String CRYPTO_TRUSTSTORE_PASSWORD = "javax.net.ssl.trustStorePassword";
    public static final String CRYPTO_TRUSTSTORE_SOURCE = "gs_cna_truststore_source";
    public static final String CRYPTO_TRUSTSTORE_SOURCE_FROM_FILE = "file";
    public static final String CRYPTO_TRUSTSTORE_SOURCE_FROM_PKCS11_LIBRARY = "pkcs11";
    public static final String CRYPTO_KEYSTORE_FILE = "gs_cna_keystore_file";
    public static final String CRYPTO_KEYSTORE_PASSWORD = "javax.net.ssl.keyStorePassword";
    public static final String CRYPTO_KEYSTORE_SOURCE = "gs_cna_keystore_source";
    public static final String CRYPTO_KEYSTORE_SOURCE_NONE = "none";
    public static final String CRYPTO_KEYSTORE_SOURCE_FROM_FILE = "file";
    public static final String CRYPTO_KEYSTORE_SOURCE_FROM_PKCS11_LIBRARY = "pkcs11";
    public static final String CRYPTO_PKCS11_LIBRARY_ENABLED = "gs_cna_pkcs11_library_enabled";
    public static final String CRYPTO_SERVER_AUTHENTICATION_VIA_CERTIFICATE_ENABLED = "gs_cna_server_authentication_via_certificate_enabled";
    public static final String CRYPTO_PKCS11_CERTIFICATE_ALIAS = "gs_cna_pkcs11_certificate_alias";

    public static final String DEFAULT_FOLDER_IMPORT = "default_folder_import";
    public static final String DEFAULT_FOLDER_EXPORT = "default_folder_export";
    public static final String DEFAULT_FOLDER_REPORT = "default_folder_report";
    public static final String DEFAULT_FOLDER_CSV_EXPORT = "default_folder_csv_export";
    public static final String DEFAULT_FOLDER_VLT = "default_folder_vlt_export";
    public static final String DEFAULT_TEMPLATE_FOLDER_REPORT = "default_template_folder_report";
    public static final String DEFAULT_FOLDER_ADDFILE = "default_folder_addfile";
    public static final String DEFAULT_FOLDER_DIALOG = "default_folder_dialog";
    // additional gui preferences
    public static final String SWITCH_PERSPECTIVE = "switch_perspective";
    public static final String DONT_ASK_BEFORE_SWITCH_PERSPECTIVE = "switch_perspective_dont_ask";
    public static final String LINK_TO_EDITOR = "link_to_editor";
    public static final String CUT_INHERIT_PERMISSIONS = "cut_inherit_permissions";
    public static final String COPY_ATTACHMENTS_WITH_OBJECTS = "copy_attachments_with_objects";
    public static final String ENABLE_RELEASE_PROCESS = "enable_release_process"; //$NON-NLS-1$

    // catalog import preferences
    public static final String CHARSET_CATALOG = "org.verinice.iso27k.rcp.charset";

    // tags to filter HitroUI Editor fields / strict filter mode setting:
    public static final String HUI_TAGS = "hui_tags";
    public static final String HUI_TAGS_ALL = "hui_tags_all";
    public static final String HUI_TAGS_STRICT = "hui_tags_strict";
    public static final String SHOW_LINK_MAKER_IN_EDITOR = "link_maker_in_editor";

    public static final String THUMBNAIL_SIZE = "thumbnail_size";

    public static final String RESTART = "is_restart";

    public static final String REPORT_LOCAL_TEMPLATE_DIRECTORY = "local_report_template_dir";

    public static final String INHERIT_SPECIAL_GROUP_ICON = "inherit_special_group_icon";

    /**
     * {@link SearchView} contains a table with many columns. Sometimes it is
     * better to sort them alphabetically instead of following the snca.xml
     */
    public static final String SEARCH_SORT_COLUMN_EDITOR_PREFERENCES = "sort_column_prefs";
    public static final String SEARCH_SORT_COLUMN_BY_SNCA = "snca";
    public static final String SEARCH_SORT_COLUMN_BY_ALPHABET = "lexicography";

    public static final String SEARCH_CSV_EXPORT_ENCODING = "search_csv_export_encoding";
    public static final String SEARCH_CSV_EXPORT_SEPERATOR = "search_csv_export_seperator";
    public static final String SEARCH_INDEX_ON_STARTUP = "search_index_on_startup";
    public static final String SEARCH_DISABLE = "search_disable";
    
    public static final String EXPORT_RISK_ANALYSIS = "export_risk_analysis";
    
    public static final String STANDALONE_UPDATENEWS_URL = 
            "standalone_updatenews_url";

    /**
     * Returns the DONT_ASK_BEFORE_SWITCH_PERSPECTIVE preference name for a view
     * class.
     *
     * @param clazz
     *            a view class
     * @return DONT_ASK_BEFORE_SWITCH_PERSPECTIVE preference name
     */
    public static String getDontAskBeforeSwitch(@SuppressWarnings("rawtypes") final Class clazz) {
        return new StringBuilder(clazz.getName()).append("_").append(PreferenceConstants.DONT_ASK_BEFORE_SWITCH_PERSPECTIVE).toString();
    }

    /**
     * Returns the SWITCH_PERSPECTIVE preference name for a view class.
     *
     * @param clazz
     *            a view class
     * @return DONT_ASK_BEFORE_SWITCH_PERSPECTIVE preference name
     */
    @SuppressWarnings("rawtypes")
    public static String getSwitch(final Class clazz) {
        return new StringBuilder(clazz.getName()).append("_").append(PreferenceConstants.SWITCH_PERSPECTIVE).toString();
    }

}
