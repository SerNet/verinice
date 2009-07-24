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
package sernet.gs.ui.rcp.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.Preferences.IPropertyChangeListener;
import org.eclipse.core.runtime.Preferences.PropertyChangeEvent;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.update.internal.core.UpdateCore;

import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;
import sernet.gs.ui.rcp.main.service.ServiceFactory;

/**
 * Prepare the workspace directory for the application. Created needed files
 * etc.
 * 
 * @author akoderman@sernet.de
 * 
 */
public class CnAWorkspace {
	private static final String OFFICEDIR = "office";

	public static final String LINE_SEP = System.getProperty("line.separator");

	private static String workDir;

	// copy binary data using 100K buffer:
	static final int BUFF_SIZE = 100000;

	static final byte[] buffer = new byte[BUFF_SIZE];

	/**
	 * Version number to check against version file.
	 * When changing this, also change version number in skeleton file "conf/configuration.version"
	 */
	public static final Object CONFIG_CURRENT_VERSION = "0.8.1";

	protected static final String VERINICEDB = "verinicedb";

	protected static final String TEMPIMPORTDB = "tempGstoolImportDb";

	private static final String POLICY_FILE = "updatePolicyURL";

	private static final Object LOCAL_UPDATE_SITE_URL = "/Verinice-Update-Site";

	private static CnAWorkspace instance;
	

	private final IPropertyChangeListener prefChangeListener = new IPropertyChangeListener() {
		private boolean modechangeWarning = true;

		public void propertyChange(PropertyChangeEvent event) {
			if ((event.getProperty().equals(PreferenceConstants.GS_DB_URL)
					|| event.getProperty().equals(PreferenceConstants.GS_DB_USER) 
					|| event.getProperty().equals(PreferenceConstants.GS_DB_PASS))) {
				
				Preferences prefs = Activator.getDefault().getPluginPreferences();
				try {
					String dbUrl = prefs.getString(PreferenceConstants.GS_DB_URL);
					
					createGstoolImportDatabaseConfig(dbUrl,
							prefs.getString(PreferenceConstants.GS_DB_USER), 
							prefs.getString(PreferenceConstants.GS_DB_PASS));
					
				} catch (Exception e) {
					ExceptionUtil.log(e, "Fehler beim Schreiben der Konfiguration für GSTool-Import.");
				}
			}
			
			if (event.getProperty().equals(PreferenceConstants.OPERATION_MODE)
					|| event.getProperty().equals(PreferenceConstants.VNSERVER_URI)) {
				try {
					if (event.getNewValue().equals(PreferenceConstants.OPERATION_MODE_STANDALONE))
						ServiceFactory.setService(ServiceFactory.LOCAL);
					else if (event.getNewValue().equals(PreferenceConstants.OPERATION_MODE_WITHSERVER))
						ServiceFactory.setService(ServiceFactory.REMOTE);
					
					createSpringConfig();
					if (!modechangeWarning ) {
						modechangeWarning = false;
						MessageDialog.openWarning(Display.getCurrent().getActiveShell(), "Neustart erforderlich", 
								"Wechsel des Betriebsmodus oder Änderungen an der Serververbindung erfordern " +
								"einen Neustart. Sie müssen Verinice " +
						"jetzt beenden und neu starten.");
					}
				} catch (Exception e) {
					ExceptionUtil.log(e, "Fehler beim Schreiben der Konfiguration für " +
							"Datenbankzugriff (Spring).");
				}
			}
		}
	};

	private File confDir;
	
	private CnAWorkspace() {
		Activator.getDefault().getPluginPreferences()
		.addPropertyChangeListener(this.prefChangeListener);
	}

	public String createTempImportDbUrl() {
		String tmpDerbyUrl = PreferenceConstants.DB_URL_DERBY.replace("%s",
				CnAWorkspace.getInstance().getWorkdir().replaceAll("\\\\", "/") );
		return tmpDerbyUrl.replace(VERINICEDB, TEMPIMPORTDB);
	}
	
	public String getTempImportDbDirName() {
		return CnAWorkspace.getInstance().getWorkdir() + File.separator + TEMPIMPORTDB;
	}

	public static CnAWorkspace getInstance() {
		if (instance == null)
			instance = new CnAWorkspace();
		return instance;
	}

	private static HashMap<String, String> settings;

	/**
	 * Initialize new workspace folder with config and other files that are
	 * distributed with the application.
	 * 
	 * 
	 */
	public void prepare(boolean force) {
		prepareWorkDir();

		if (!force
				&& confDir.exists() 
				&& confDir.isDirectory()) {
			File confFile = new File(confDir, "configuration.version");
			if (confFile.exists()) {
				Properties props = new Properties();
				FileInputStream fis;
				try {
					fis = new FileInputStream(confFile);
					props.load(fis);

					if (props.get("version").equals(CONFIG_CURRENT_VERSION)) {
						Logger.getLogger(CnAWorkspace.class).debug(
								"Arbeitsverzeichnis bereits vorhanden, wird nicht neu erzeugt: "
										+ confDir.getAbsolutePath());
						return;
					}
				} catch (Exception e) {
					Logger.getLogger(this.getClass()).debug(e);
				}
			}

		}
		
		CnAWorkspace instance = new CnAWorkspace();
		try {
			instance.createConfDir();
			instance.createHtmlDir();
			instance.createOfficeDir();
			instance.createDatabaseConfig();
			instance.createSpringConfig();
		} catch (Exception e) {
			ExceptionUtil.log(e,
					"Fehler beim Anlegen des Arbeitsverzeichnisses: "
					+ confDir.getAbsolutePath());
		}


	}

	public void prepareWorkDir() {
		URL url = Platform.getInstanceLocation().getURL();
		String path = url.getPath().replaceAll("/", "\\" + File.separator);
		workDir = (new File(path)).getAbsolutePath();		
		confDir = new File(url.getPath() + File.separator + "conf");
		
		if (ServiceFactory.isUsingRemoteService()) {
			try {
				createPolicyFile(Activator.getDefault().getPluginPreferences());
			} catch (MalformedURLException e) {
				Logger.getLogger(this.getClass()).error("Konnte Update-Policy File nicht erzeugen.", e);
			} catch (IOException e) {
				Logger.getLogger(this.getClass()).error("Konnte Update-Policy File nicht erzeugen.", e);
			}
		}
		else {
			removePolicyFile();
		}
		
	}

	private void createSpringConfig() throws NullPointerException, IOException {
		
		// create application context xml for direct database access:
		settings = new HashMap<String, String>(1);
		String cfgFileURL = (new File(getConfDir() + File.separator + "hibernate.cfg.xml")).toURI().toURL().toString();
		settings.put("hibernatecfg", cfgFileURL);
		createTextFile("conf" + File.separator + "skel_applicationContextHibernate.xml",
				getConfDir(), 
				"applicationContextHibernate.xml",
				settings);
		
		// create context for remote service:
		Preferences prefs = Activator.getDefault().getPluginPreferences();
		settings = new HashMap<String, String>(1);
		settings.put("veriniceserver", prefs.getString(PreferenceConstants.VNSERVER_URI));
		createTextFile("conf" + File.separator + "skel_applicationContextRemoteService.xml",
				getConfDir(), 
				"applicationContextRemoteService.xml",
				settings);
		

		// create bean ref factory xml:
		settings = new HashMap<String, String>(2);
		
		String appCtxHibernate = (new File(getConfDir() + File.separator + "applicationContextHibernate.xml"))
			.toURI().toURL().toString();
		settings.put("applicationContextHibernate", appCtxHibernate );
		
		
		String appCtxRemote = (new File(getConfDir() + File.separator + "applicationContextRemoteService.xml"))
		.toURI().toURL().toString();
		settings.put("applicationContextRemote", appCtxRemote);
		
		if (ServiceFactory.isUsingRemoteService()) {
			Logger.getLogger(this.getClass()).debug("Creating bean ref for remote service.");
			
			createTextFile("conf" + File.separator + "skel_beanRefFactory-Remote.xml", 
					getConfDir(),		
					"beanRefFactory.xml",
					settings);
			
			createPolicyFile(prefs);
			
		}
		else {
			Logger.getLogger(this.getClass()).debug("Creating bean ref for local hibernate access.");

			createTextFile("conf" + File.separator + "skel_beanRefFactory-Hibernate.xml", 
					getConfDir(),		
					"beanRefFactory.xml",
					settings);
			
			removePolicyFile();
		}
	}

	private void removePolicyFile() {
		// remove policy file / path to policy file. thereby setting update site to default:
		removeFile(getConfDir(), "policy.xml");
		UpdateCore.getPlugin().getPluginPreferences().setValue("updatePolicyURL", "" );
	}

	private void createPolicyFile(Preferences prefs) throws IOException,
			MalformedURLException {
		// create update policy file to set update site to local verinice server:
		settings = new HashMap<String, String>(1);
		settings.put("updatesiteurl", createUpdateSiteUrl(prefs.getString(PreferenceConstants.VNSERVER_URI)));
		createTextFile("conf" + File.separator + "skel_policy.xml",
				getConfDir(), 
				"policy.xml",
				settings);
		
		// set path to policy.xml with changed update site (on local server):
		File policyFile = new File(getConfDir() + File.separator + "policy.xml");
		UpdateCore.getPlugin().getPluginPreferences().setValue("updatePolicyURL", 
				policyFile.toURI().toURL().toString() );
	}

	/**
	 * @param dir
	 * @param string
	 */
	private void removeFile(String dir, String name) {
		File fileToDelete = new File(dir + File.separator + name);
		boolean success = fileToDelete.delete();
		if (success)
			Logger.getLogger(this.getClass()).debug(name + " was successfully deleted.");
		else
			Logger.getLogger(this.getClass()).debug(name + " was NOT deleted.");
	}

	/**
	 * @param string
	 * @return
	 */
	private String createUpdateSiteUrl(String serverUrl) {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(serverUrl);
		stringBuilder.append(LOCAL_UPDATE_SITE_URL);
		return stringBuilder.toString();
	}

	public String getWorkdir() {
		return workDir;
	}

	public String getConfDir() {
		return workDir + File.separator + "conf";
	}

	private void createConfDir() throws NullPointerException, IOException {
		URL url = Platform.getInstanceLocation().getURL();
		File confDir = new File(url.getPath() + File.separator + "conf");
		confDir.mkdirs();

		createTextFile("conf" + File.separator + "hitro.xsd", workDir);
		createTextFile("conf" + File.separator + "SNCA.xml", workDir);
		createTextFile("conf" + File.separator + "reports.properties_skeleton",
				workDir, "conf" + File.separator + "reports.properties");
		createTextFile("conf" + File.separator + "configuration.version",
				workDir);
	}

	public void createReportTempFile() {
		URL url = Platform.getInstanceLocation().getURL();
		File officeDir = new File(url.getPath() + File.separator + OFFICEDIR);

	}

	private void createOfficeDir() throws NullPointerException, IOException {
		URL url = Platform.getInstanceLocation().getURL();
		File officeDir = new File(url.getPath() + File.separator + OFFICEDIR);
		officeDir.mkdirs();

		createBinaryFile(OFFICEDIR + File.separator + "report.ods", workDir);
		createBinaryFile(OFFICEDIR + File.separator + "report.odt", workDir);
		createBinaryFile(OFFICEDIR + File.separator + "sernet.png", workDir);

	}

	private void createHtmlDir() throws NullPointerException, IOException {
		URL url = Platform.getInstanceLocation().getURL();
		File htmlDir = new File(url.getPath() + File.separator + "html");
		htmlDir.mkdirs();

		createTextFile("html" + File.separator + "screen.css", workDir);
		createTextFile("html" + File.separator + "about.html", workDir);
		createBinaryFile("splash.bmp", workDir + File.separator + "html");
	}

	/**
	 * Copy resource from classpath (i.e. inside JAR file) to local filesystem.
	 * 
	 * @param infile
	 * @param toDir
	 * @throws IOException
	 */
	private void createBinaryFile(String infile, String toDir)
			throws IOException {

		backupFile(toDir, infile);

		String infileResource = infile.replace('\\', '/');
		InputStream in = getClass().getClassLoader().getResourceAsStream(
				infileResource);
		OutputStream out = null;
		try {
			out = new FileOutputStream(toDir + File.separator + infile);
			while (true) {
				synchronized (buffer) {
					int amountRead = in.read(buffer);
					if (amountRead == -1) {
						break;
					}
					out.write(buffer, 0, amountRead);
				}
			}
		} finally {
			if (in != null) {
				in.close();
			}
			if (out != null) {
				out.close();
			}
		}
	}

	public void copyFile(String infileName, File outfile) throws IOException {
		FileInputStream in = new FileInputStream((new File(infileName)));
		OutputStream out = null;
		try {
			out = new FileOutputStream(outfile);
			while (true) {
				synchronized (buffer) {
					int amountRead = in.read(buffer);
					if (amountRead == -1) {
						break;
					}
					out.write(buffer, 0, amountRead);
				}
			}
		} finally {
			if (in != null) {
				in.close();
			}
			if (out != null) {
				out.close();
			}
		}

	}

	/**
	 * Create a hibernate dataase config from a skeleton file, filling in the
	 * given values for user, password etc.
	 * 
	 * @param url
	 * @param user
	 * @param pass
	 * @param driver
	 * @param dialect
	 * @throws NullPointerException
	 * @throws IOException
	 */
	public void createDatabaseConfig(String url, String user, String pass,
			String driver, String dialect) throws NullPointerException,
			IOException {
		settings = new HashMap<String, String>(5);
		settings.put("url", url);
		settings.put("user", user);
		settings.put("pass", pass);
		settings.put("driver", driver);
		settings.put("dialect", dialect);
		
		if (driver.indexOf("derby")>-1) 
			// use optimzed derby config:
			createTextFile("conf" + File.separator + "skel_hibernate_derby.cfg.xml",
					workDir, "conf" + File.separator + "hibernate.cfg.xml",
					settings);
		else		
			createTextFile("conf" + File.separator + "skel_hibernate.cfg.xml",
				workDir, "conf" + File.separator + "hibernate.cfg.xml",
				settings);
	}

	public void createGstoolImportDatabaseConfig(String url, String user,
			String pass) throws NullPointerException, IOException {
		settings = new HashMap<String, String>(5);
		settings.put("url", url.replace("\\", "\\\\"));
		settings.put("user", user);
		settings.put("pass", pass);
		
		// import from .mdb file over odbc bridge goes into temporary derby db first:
		if (url.indexOf("odbc")>-1) {
			// change db url to temporary DB when importing from mdb file
			String dbUrl = createTempImportDbUrl();
			settings.put("url", dbUrl);
			settings.put("driver", PreferenceConstants.DB_DRIVER_DERBY);
			settings.put("dialect", PreferenceConstants.DB_DIALECT_derby);
		}
		else {
			// direct import from ms sql server or desktop engine:
			settings.put("driver", PreferenceConstants.GS_DB_DRIVER_JTDS);
			settings.put("dialect", PreferenceConstants.GS_DB_DIALECT_JTDS);
		}
		
		createTextFile("conf" + File.separator
				+ "skel_hibernate-vampire.cfg.xml", workDir, "conf"
				+ File.separator + "hibernate-vampire.cfg.xml", settings);
	}
	
	public void createGstoolImportDatabaseConfig() throws NullPointerException, IOException {
		Preferences prefs = Activator.getDefault().getPluginPreferences();
		createGstoolImportDatabaseConfig(prefs
				.getString(PreferenceConstants.GS_DB_URL), prefs
				.getString(PreferenceConstants.GS_DB_USER), prefs
				.getString(PreferenceConstants.GS_DB_PASS));
	}

	private void createTextFile(String infile, String toDir)
			throws NullPointerException, IOException {
		createTextFile(infile, toDir, infile, null);
	}

	private void createTextFile(String infile, String toDir, String outfile)
			throws NullPointerException, IOException {
		createTextFile(infile, toDir, outfile, null);
	}

	/**
	 * Create a text file in the local file system from a resource (i.e. inside
	 * a JAR file distributed with the application).
	 * 
	 * Adapt line-feeds to local settings and optionally replace variables with
	 * values given in a hashmap.
	 * 
	 * @param infile
	 * @param toDir
	 * @param outfile
	 * @param variables
	 * @throws NullPointerException
	 * @throws IOException
	 */
	private void createTextFile(String infile, String toDir, String outfile,
			Map<String, String> variables) throws NullPointerException,
			IOException {

		String infileResource = infile.replace('\\', '/');
		InputStream is = getClass().getClassLoader().getResourceAsStream(
				infileResource);
		InputStreamReader inRead = new InputStreamReader(is);
		BufferedReader bufRead = new BufferedReader(inRead);
		StringBuffer skelFile = new StringBuffer();

		// write from skel file, replacing newline characters to system
		// specific:
		String line;
		Pattern var = Pattern.compile("\\{(.*)\\}");
		while ((line = bufRead.readLine()) != null) {
			line = line.replaceFirst("\n", LINE_SEP);
			if (variables != null) {
				Matcher match = var.matcher(line);
				if (match.find()) {
					line = match.replaceFirst(variables.get(match.group(1)));
				}
			}
			skelFile.append(line + LINE_SEP);
		}
		bufRead.close();
		inRead.close();
		is.close();

		backupFile(toDir, outfile);
		FileOutputStream fout = new FileOutputStream(toDir + File.separator
				+ outfile, false);
		OutputStreamWriter outWrite = new OutputStreamWriter(fout);
		outWrite.write(skelFile.toString());
		outWrite.close();
		fout.close();
	}

	private void backupFile(String dir, String filepath) throws IOException {
		File file = new File(dir + File.separator + filepath);
		if (file.exists()) {
			File outfile = new File(dir + File.separator + filepath + ".bak");
			copyFile(file.getAbsolutePath(), outfile);
		}
	}

	public synchronized boolean isDatabaseConfigUpToDate() {
		Preferences prefs = Activator.getDefault().getPluginPreferences();
		boolean result = false;
		if (settings != null) {
			result = settings.get("url").equals(
					prefs.getString(PreferenceConstants.DB_URL))
					&& settings.get("user").equals(
							prefs.getString(PreferenceConstants.DB_USER))
					&& settings.get("pass").equals(
							prefs.getString(PreferenceConstants.DB_PASS))
					&& settings.get("driver").equals(
							prefs.getString(PreferenceConstants.DB_DRIVER))
					&& settings.get("dialect").equals(
							prefs.getString(PreferenceConstants.DB_DIALECT));

			String s1 = prefs.getString(PreferenceConstants.DB_URL);
			String s2 = prefs.getString(PreferenceConstants.DB_PASS);
			String s3 = prefs.getString(PreferenceConstants.DB_DRIVER);
			String s4 = prefs.getString(PreferenceConstants.DB_DIALECT);
			String s5 = prefs.getString(PreferenceConstants.DB_USER);
		}
		return result;
	}

	public synchronized void createDatabaseConfig()
			throws NullPointerException, IOException {
		
		Preferences prefs = Activator.getDefault().getPluginPreferences();
		createDatabaseConfig(prefs.getString(PreferenceConstants.DB_URL), prefs
				.getString(PreferenceConstants.DB_USER), prefs
				.getString(PreferenceConstants.DB_PASS), prefs
				.getString(PreferenceConstants.DB_DRIVER), prefs
				.getString(PreferenceConstants.DB_DIALECT));

		createGstoolImportDatabaseConfig(prefs
				.getString(PreferenceConstants.GS_DB_URL), prefs
				.getString(PreferenceConstants.GS_DB_USER), prefs
				.getString(PreferenceConstants.GS_DB_PASS));
	}

	public void prepare() {
		prepare(false);
	}

}
