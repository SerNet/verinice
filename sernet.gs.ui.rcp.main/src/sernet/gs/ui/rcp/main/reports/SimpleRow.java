package sernet.gs.ui.rcp.main.reports;

import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.office.IOOTableRow;

/**
 * Row for a single String in one column.
 * 
 * @author aprack@sernet.de
 *
 */
public class SimpleRow implements IOOTableRow {


	private String style;
	private String[] rows;
	
	public SimpleRow(String style, String... rows) {
		this.style = style;
		this.rows = rows;
		
	}
	
	public double getCellAsDouble(int column) {
		return 0;
	}

	public String getCellAsString(int column) {
		return rows[column];
	}

	public int getCellType(int column) {
		return IOOTableRow.CELL_TYPE_STRING;
	}

	public int getNumColumns() {
		return rows.length;
	}
	
	public String getRowStyle() {
		return style;
	}

}
