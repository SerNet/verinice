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
 ******************************************************************************/
package sernet.verinice.rcp.search;

import java.util.SortedSet;

import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.PropertyType;

/**
 * Provides modifying access to search view table columns.
 *
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 *
 */
interface ColumnStore {

    /**
     * Returns a set of columns which should be rendered by the search view
     * table.
     *
     */
    public SortedSet<PropertyType> getColumns();

    /**
     * Set the visible columns back to default.
     */
    public void restoreDefault();

    /**
     * Returns all possible columns for an {@link Entity} minus the columns
     * which are returned by {{@link #getColumns()}. Possible Columns are
     * defined in the SNCA.xml.
     *
     */
    public SortedSet<PropertyType> getNotVisible();

    /**
     * Configures visibility of column.
     */
    public void setVisible(PropertyType column, boolean visible);

}
