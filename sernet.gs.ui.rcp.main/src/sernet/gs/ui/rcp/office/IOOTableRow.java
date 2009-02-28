package sernet.gs.ui.rcp.office;

import java.io.Serializable;

public interface IOOTableRow extends Serializable {
	public static final int CELL_TYPE_STRING = 0;
	public static final int CELL_TYPE_DOUBLE = 1;
	
	// row styles, names must match style definition in OpenOffice template:
	public static final String ROW_STYLE_ELEMENT = "Element";
	public static final String ROW_STYLE_SUBHEADER = "ElementHeader";
	public static final String ROW_STYLE_HEADER = "Kategorie";
	
	public String getCellAsString(int column);
	public double getCellAsDouble(int column);
	public int getCellType(int column);
	public int getNumColumns();
	public String getRowStyle();
}
