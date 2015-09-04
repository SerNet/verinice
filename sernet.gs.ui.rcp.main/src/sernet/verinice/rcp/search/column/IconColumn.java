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

import sernet.verinice.rcp.search.Messages;

/**
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 */
public class IconColumn extends AbstractColumn {

    public static final int DEFAULT_WIDTH = 32;

    public IconColumn(ColumnStore columnStore) {
        super(columnStore);
    }

    private final static String ICON_COLUMN_TITLE = Messages.IconColumn_0;

    public final static String ICON_PROPERTY_NAME = "icon-path";



    /* (non-Javadoc)
     * @see sernet.verinice.rcp.search.column.IColumn#getTitle()
     */
    @Override
    public String getTitle() {
        return ICON_COLUMN_TITLE;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.rcp.search.column.AbstractColumn#getId()
     */
    @Override
    public String getId() {
        return ICON_PROPERTY_NAME;
    }

    @Override
    public int getRank(){
        return -5;
    }
}
