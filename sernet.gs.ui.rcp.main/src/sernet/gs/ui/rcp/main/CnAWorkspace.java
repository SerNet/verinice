package sernet.gs.ui.rcp.main;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Preferences;

import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;
import sun.misc.Regexp;

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

	private static CnAWorkspace instance;

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
	public void prepare() {
		URL url = Platform.getInstanceLocation().getURL();
		workDir = (new File(url.getPath())).getAbsolutePath();

		File confDir = new File(url.getPath() + File.separator + "conf");
		if (confDir.exists() && confDir.isDirectory()) {
			Logger.getLogger(CnAWorkspace.class).debug(
					"Arbeitsverzeichnis bereits vorhanden, wird nicht neu erzeugt: "
							+ confDir.getAbsolutePath());
			return;
		}

		CnAWorkspace instance = new CnAWorkspace();
		try {
			instance.createConfDir();
			instance.createHtmlDir();
			instance.createOfficeDir();
			instance.createDatabaseConfig();
		} catch (Exception e) {
			ExceptionUtil.log(e,
					"Fehler beim Anlegen des Arbeitsverzeichnisses: "
							+ confDir.getAbsolutePath());
		}

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
	 * Copy resource from claspath (i.e. inside JAR file) to local filesystem.
	 * 
	 * @param infile
	 * @param toDir
	 * @throws IOException
	 */
	private void createBinaryFile(String infile, String toDir)
			throws IOException {
		String infileResource = infile.replace('\\', '/');
		InputStream in = getClass().getClassLoader()
				.getResourceAsStream(infileResource);
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
		createTextFile("conf" + File.separator + "skel_hibernate.cfg.xml",
				workDir,
				"conf" + File.separator + "hibernate.cfg.xml",
				settings);
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
		InputStream is = getClass().getClassLoader()
				.getResourceAsStream(infileResource);
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

		FileOutputStream fout = new FileOutputStream(toDir + File.separator
				+ outfile, false);
		OutputStreamWriter outWrite = new OutputStreamWriter(fout);
		outWrite.write(skelFile.toString());
		outWrite.close();
		fout.close();
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

	public synchronized void createDatabaseConfig() throws NullPointerException, IOException {
		Preferences prefs = Activator.getDefault().getPluginPreferences();
		createDatabaseConfig(prefs.getString(PreferenceConstants.DB_URL), prefs
				.getString(PreferenceConstants.DB_USER), prefs
				.getString(PreferenceConstants.DB_PASS), prefs
				.getString(PreferenceConstants.DB_DRIVER), prefs
				.getString(PreferenceConstants.DB_DIALECT));
	}

}
