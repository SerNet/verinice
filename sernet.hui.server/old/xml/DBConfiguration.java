package sernet.hui.server.connect.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class DBConfiguration {

	private static String PROPS_FILE = "/home/aprack/config/etc/db.properties";
	
	private Properties props;

	public DBConfiguration() throws IOException {
		File f = new File(PROPS_FILE);
		FileInputStream fis = new FileInputStream(f);
		props = new Properties();
		props.load(fis);
		fis.close();
	}

	public String getEnvDir() {
		return props.getProperty("envdir");
	}

	public String getContainer() {
		return props.getProperty("container");
	}

}
