package sernet.gs.ui.rcp.main.reports;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.office.IOOTableRow;
import sernet.snutils.AssertException;

/**
 * Returns the given properties as columns for OpenOffice export.
 * 
 * @author koderman@sernet.de
 *
 */
public class PropertiesRow implements IOOTableRow, ICnaItemRow {

	private CnATreeElement item;
	private List<String> properties;
	private String style;
	

	public PropertiesRow(CnATreeElement item, List<String> properties, String style) {
		this.item = item;
		this.properties = properties;
		this.style = style;
	}
	
	public double getCellAsDouble(int column) {
		return 0;
	}

	public String getCellAsString(int column) {
		return item.getEntity().getSimpleValue(properties.get(column));
	}

	public int getCellType(int column) {
		return IOOTableRow.CELL_TYPE_STRING;
	}

	public int getNumColumns() {
		if (properties == null)
			return 0;
		return properties.size();
	}
	
	public String getRowStyle() {
		return style;
	}

	public CnATreeElement getItem() {
		return item;
	}
	
}