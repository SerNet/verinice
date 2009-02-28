package sernet.gs.ui.rcp.main.reports;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Properties;

import org.apache.log4j.Logger;

import sernet.gs.ui.rcp.main.CnAWorkspace;
import sernet.gs.ui.rcp.main.bsi.model.BSIModel;

public abstract class Report implements Serializable {

	private BSIModel model;
	protected Properties reportProperties;

	public Report(Properties reportProperties) {
		this.reportProperties = reportProperties;
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

	public BSIModel getModel() {
		return this.model;
	}
	
	public void setModel(BSIModel model) {
		this.model = model;
	}

}