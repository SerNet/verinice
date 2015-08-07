/*******************************************************************************
 * Copyright (c) 2015 Benjamin Weißenfels.
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
 *     Benjamin Weißenfels <bw[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.rcp.search.column;

import sernet.verinice.model.search.VeriniceSearchResultRow;
import sernet.verinice.rcp.search.tables.SearchResultsTableViewer;

/**
 * Maps the column headers from {@link SearchResultsTableViewer} to this
 * interface.
 *
 *
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 */
public interface IColumn {

    static final int DEFAULT_WIDTH = 200;

    /**
     * The id can be used to retrieve an value from
     * {@link VeriniceSearchResultRow#getValueFromResultString(String)}.
     */
    public String getId();

    /**
     * Returns an human readable title. Not implemented by every subclass.
     *
     */
    public String getTitle();

    public void setVisible(boolean visible);

    public boolean isVisible();

    /**
     * Sets the order of the column.
     *
     */
    public int getRank();

    public boolean isMultiselect();

    public boolean isSingleSelect();

    public boolean isNumericSelect();

    public boolean isBooleanSelect();

    public boolean isEnum();

    public boolean isLine();

    public boolean isReference();

    public boolean isCnaLinkReference();

    public boolean isText();

    public boolean isDate();

    void setWidth(int width);

    public int getWidth();
}