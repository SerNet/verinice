/*******************************************************************************
 * Copyright (c) 2015 Daniel Murygin.
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.rcp.search;

import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang.NotImplementedException;

import sernet.hui.common.connect.PropertyType;

/**
 * This implementation of {@link IColumnStore} is based on two {@link TreeSet}s
 * for visible and invisible columns.
 * 
 * A column (or {@link PropertyType}) is either in set visibleColumns or
 * invisibleColumns.
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */

public class ColumnStore implements IColumnStore {

    private SortedSet<PropertyType> visibleColumns;
    private SortedSet<PropertyType> invisibleColumns;

    public ColumnStore() {
        super();
        visibleColumns = new TreeSet<PropertyType>();
        invisibleColumns = new TreeSet<PropertyType>();
    }

    public ColumnStore(String entityTypeid) {
        this();
    }

    /*
     * (non-Javadoc)
     *
     * @see sernet.verinice.rcp.search.IColumnStore#getColumns()
     */
    @Override
    public SortedSet<PropertyType> getColumns() {
        return visibleColumns;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * sernet.verinice.rcp.search.IColumnStore#addColumn(sernet.hui.common.connect
     * .PropertyType)
     */
    @Override
    public void addColumn(PropertyType column) {
        visibleColumns.add(column);
        invisibleColumns.remove(column);
    }

    public void addInvisibleColumn(PropertyType column) {
        visibleColumns.remove(column);
        invisibleColumns.add(column);
    }

    /*
     * (non-Javadoc)
     *
     * @see sernet.verinice.rcp.search.IColumnStore#restoreDefault()
     */
    @Override
    public void restoreDefault() {
        throw new UnsupportedOperationException();
    }

    /*
     * (non-Javadoc)
     *
     * @see sernet.verinice.rcp.search.IColumnStore#getNotVisible()
     */
    @Override
    public SortedSet<PropertyType> getInvisible() {
        return invisibleColumns;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * sernet.verinice.rcp.search.IColumnStore#setVisible(sernet.hui.common.
     * connect.PropertyType, boolean)
     */
    @Override
    public void setVisible(PropertyType column, boolean visible) {
        if (visible) {
            addColumn(column);
        } else {
            addInvisibleColumn(column);
        }
    }

    public SortedSet<PropertyType> getAllColumns() {
        SortedSet<PropertyType> allColumns = getColumns();
        allColumns.addAll(getInvisible());
        return allColumns;
    }
}
