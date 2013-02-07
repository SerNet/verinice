/*******************************************************************************
 * Copyright (c) 2010 Alexander Koderman <ak@sernet.de>.
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
package sernet.gs.ui.rcp.main.reports;

import java.util.ArrayList;
import java.util.List;

import sernet.gs.ui.rcp.office.IOOTableRow;
import sernet.verinice.model.common.CnATreeElement;

/**
 * Row that fills all vlues on creation, avoiding proxy problems when transporting the data between
 * client and server.
 * 
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public class PrefilledRow implements IOOTableRow, ICnaItemRow {

    private IOOTableRow row;
    List<Object> columns;

    public PrefilledRow(ControlMaturityRow originalRow) {
        this.row = originalRow;
        columns = new ArrayList<Object>();
        for (int colIdx = 0; colIdx < row.getNumColumns(); colIdx++) {
            columns.add(getCellByType(row, colIdx));
        }
    }
    
    public PrefilledRow(ControlGroupMaturityRow row) {
        this.row = row;
        columns = new ArrayList<Object>();
        for (int colIdx = 0; colIdx < row.getNumColumns(); colIdx++) {
            columns.add(getCellByType(row, colIdx));
        }
    }
    
    
    private Object getCellByType(IOOTableRow row, int col) {
        if (row.getCellType(col) == IOOTableRow.CELL_TYPE_STRING){
            return row.getCellAsString(col);
        }
        if (row.getCellType(col) == IOOTableRow.CELL_TYPE_DOUBLE){
            return row.getCellAsDouble(col);
        }
        
        return "";
    }
    
    /* (non-Javadoc)
     * @see sernet.gs.ui.rcp.office.IOOTableRow#getCellAsDouble(int)
     */
    public double getCellAsDouble(int column) {
        return (Double) columns.get(column);
    }

    /* (non-Javadoc)
     * @see sernet.gs.ui.rcp.office.IOOTableRow#getCellAsString(int)
     */
    public String getCellAsString(int column) {
        return (String) columns.get(column);
    }

    /* (non-Javadoc)
     * @see sernet.gs.ui.rcp.office.IOOTableRow#getCellType(int)
     */
    public int getCellType(int column) {
        return row.getCellType(column);
    }

    /* (non-Javadoc)
     * @see sernet.gs.ui.rcp.office.IOOTableRow#getNumColumns()
     */
    public int getNumColumns() {
        return row.getNumColumns();
    }

    /* (non-Javadoc)
     * @see sernet.gs.ui.rcp.office.IOOTableRow#getRowStyle()
     */
    public String getRowStyle() {
        return row.getRowStyle();
    }

    /* (non-Javadoc)
     * @see sernet.gs.ui.rcp.main.reports.ICnaItemRow#getItem()
     */
    public CnATreeElement getItem() {
        return null;
    }

}
