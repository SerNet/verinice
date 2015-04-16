/*******************************************************************************
 * Copyright (c) 2015 benjamin.
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
 *     benjamin <bw[at]sernet[dot]de> - initial API and implementation
 *     Daniel Murygin <dm[at]sernet[dot]de> - Additional improvements
 ******************************************************************************/
package sernet.verinice.rcp.search;

import java.util.SortedSet;

import sernet.hui.common.connect.PropertyType;

/**
 * Stores a set of visible and invisible columns / {@link PropertyType}s.
 * Is used by {@link SearchView} to modify access to table columns.
 *
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public interface IColumnStore {

    /**
     * Returns a set of columns 
     * which should be visible in the search view table.
     */
    public SortedSet<PropertyType> getColumns();

    /**
     * Returns all possible columns for an {@link Entity} minus the columns
     * which are returned by {{@link #getColumns()}. Possible Columns are
     * defined in the SNCA.xml.
     */
    public SortedSet<PropertyType> getInvisible();

    
    /**
     * Adds a column. The column is visible by default.
     */
    public void addColumn(PropertyType column);

    /**
     * Configures visibility of column.
     */
    public void setVisible(PropertyType column, boolean visible);
    
    /**
     * Set the visibility columns back to default.
     */
    public void restoreDefault();

}
