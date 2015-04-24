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
package sernet.verinice.rcp.search.tables;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.graphics.Image;

import sernet.verinice.model.search.VeriniceSearchResultRow;
import sernet.verinice.rcp.search.column.IColumn;
import sernet.verinice.rcp.search.column.IconColumn;
import sernet.verinice.rcp.search.column.ScopeColumn;
import sernet.verinice.rcp.search.column.TitleColumn;

/**
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 */
public class SearchTableColumnLabelProvider extends ColumnLabelProvider {

    private IColumn column;

    public SearchTableColumnLabelProvider(IColumn column) {
        super();
        this.column = column;
    }

    @Override
    public String getText(Object element) {
        if (element instanceof VeriniceSearchResultRow) {
            VeriniceSearchResultRow row = (VeriniceSearchResultRow) element;

            if (column instanceof TitleColumn)
                return row.getValueFromResultString(TitleColumn.TITLE_PROPERTY_NAME);

            if (column instanceof ScopeColumn) {
                return row.getValueFromResultString(ScopeColumn.SCOPE_PROPERTY_NAME);
            }

            return row.getValueFromResultString(column.getId());

        } else {
            throw new RuntimeException("you holded it wrong");
        }
    }

    @Override
    public Image getImage(Object element) {

        if (column instanceof IconColumn) {
            if (element instanceof VeriniceSearchResultRow) {
                VeriniceSearchResultRow row = (VeriniceSearchResultRow) element;
                return TableImageProvider.getImagePath(row);
            }
        }

        return null;
    }

}
