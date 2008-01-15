package sernet.gs.ui.rcp.main.reports;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

import sernet.gs.ui.rcp.main.CnAWorkspace;

public abstract class Report {

	protected static Properties reportProperties;

	public Report() {
		if (reportProperties == null) {
    		try {
    			reportProperties = new Properties();
    			File config = new File(CnAWorkspace.getInstance().getConfDir() + File.separator
    					+ IBSIReport.PROPERTY_FILE);
    			FileInputStream is = new FileInputStream(config);
	    		if (is != null) {
	    			reportProperties.load(is);
	    			is.close();
	    		} else {
	    			Logger.getLogger(this.getClass())
	    				.error("Konnte Report Default-Felder nicht laden.");
	    		}
			} catch (IOException e) {
				Logger.getLogger(
						this.getClass()).error("Konnte Report Default-Felder nicht laden.", e);
			} finally {
			}
    	}
	}

	/**
	 * Check if list of default columns for export contains the given column.
	 * 
	 */
	public boolean isDefaultColumn(String property_id) {
		String prop = reportProperties.getProperty(getClass().getSimpleName());
		if (prop == null)
			return false;
		return (prop.indexOf(property_id) > -1 );
	}

}