package sernet.gs.ui.rcp.main.reports;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.office.IOOTableRow;

/**
 * Interface for different BSI report types that will be exported to OpenOffice.
 * 
 * @author koderman@sernet.de
 *
 */
public interface IBSIReport {
	
	public static String PROPERTY_FILE = "reports.properties";
	
	/**
	 * Get all items to include in the report as flat list,
	 * regardless of category.
	 * 
	 * @return
	 */
	public ArrayList<CnATreeElement> getItems();

	
	/**
	 * Get the title for this report. 
	 */
	public String getTitle();
	
	/**
	 * Get the complete report as data source for OpenOffice Calc export.
	 * 
	 * @param shownPropertyTypes
	 * @return
	 */
	public ArrayList<IOOTableRow> getReport(PropertySelection shownPropertyTypes);
		
	/**
	 * Which columns should be included in this report?
	 * 
	 * @param property_id
	 * @return
	 */
	public boolean isDefaultColumn(String property_id);


	
}
