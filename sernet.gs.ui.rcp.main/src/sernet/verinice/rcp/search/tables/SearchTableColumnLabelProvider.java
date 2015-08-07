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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.elasticsearch.search.SearchService;

import sernet.verinice.interfaces.search.ISearchService;
import sernet.verinice.model.search.Occurence;
import sernet.verinice.model.search.VeriniceQuery;
import sernet.verinice.model.search.VeriniceSearchResultRow;
import sernet.verinice.rcp.search.column.IColumn;
import sernet.verinice.rcp.search.column.IconColumn;
import sernet.verinice.rcp.search.column.OccurenceColumn;
import sernet.verinice.rcp.search.column.PropertyTypeColumn;
import sernet.verinice.rcp.search.column.ScopeColumn;
import sernet.verinice.rcp.search.column.TitleColumn;

/**
 * Makes a decision, which type of {@link IColumn} has to be rendered and marks
 * substring matches of columns with type {@link PropertyTypeColumn}.
 *
 *
 * Note: The class {@link Occurence} provides highlighted fields, but they are
 * not used at all, since the whole field is always completly wrapped by the
 * elastic search hightlighter.
 *
 *
 * @see SearchService
 * @see Occurence
 *
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 */
public class SearchTableColumnLabelProvider extends StyledCellLabelProvider {

    Color yellow = Display.getCurrent().getSystemColor(SWT.COLOR_YELLOW);
    Color white = Display.getCurrent().getSystemColor(SWT.COLOR_WHITE);
    Color red = Display.getCurrent().getSystemColor(SWT.COLOR_RED);

    private IColumn column;

    public SearchTableColumnLabelProvider(IColumn column) {
        super();
        this.column = column;
    }

    @Override
    public void update(ViewerCell cell) {

        Object element = cell.getElement();

        if (element instanceof VeriniceSearchResultRow) {
            VeriniceSearchResultRow row = (VeriniceSearchResultRow) element;

            if (column instanceof IconColumn) {
                cell.setImage(getImage(row));
                cell.setText("");
            }

            else if (column instanceof TitleColumn) {
                cell.setText(row.getValueFromResultString(TitleColumn.TITLE_PROPERTY_NAME));
                markMatches(cell);
            }

            else if (column instanceof ScopeColumn) {
                cell.setText(row.getValueFromResultString(ISearchService.ES_FIELD_SCOPE_TITLE));
            }

            else if (column instanceof OccurenceColumn) {
                cell.setText(formatOccurences(row));
            }

            else if (column instanceof PropertyTypeColumn) {
                cell.setText(row.getValueFromResultString(column.getId()));
                markMatches(cell);
            }

        } else {
            throw new RuntimeException("unknown column class type");
        }
    }

    private String formatOccurences(VeriniceSearchResultRow row) {
        return StringUtils.join(row.getOccurence().getColumnNamesWithoutTitle(), "\n");
    }

    private Image getImage(VeriniceSearchResultRow row) {
        return TableImageProvider.getImage(row);
    }

    private void markMatches(ViewerCell cell) {

        VeriniceSearchResultRow row = (VeriniceSearchResultRow) cell.getElement();
        Occurence occurences = row.getOccurence();
        VeriniceQuery query = row.getParent().getParent().getVeriniceQuery();
        List<StyleRange> styleRanges = new ArrayList<StyleRange>(0);

        if (query.isQueryEmpty()){
            return;
        }

        if (column instanceof PropertyTypeColumn) {
            for (String fragment : occurences.getMatches(column.getId())) {
                createStyleRanges(cell, fragment, styleRanges);
            }
        }

        if (column instanceof TitleColumn) {
            createStyleRanges(cell, query.getQuery(), styleRanges);
        }

        cell.setStyleRanges(styleRanges.toArray(new StyleRange[styleRanges.size()]));
    }

    private void createStyleRanges(ViewerCell cell, String query, List<StyleRange> styleRanges) {
        int index = 0;
        for (;;) {

            index = cell.getText().toLowerCase().indexOf(query.toLowerCase(), index);

            if (index == -1) {
                break;
            } else {
                styleRanges.add(new StyleRange(index, query.length(), red, cell.getBackground()));
                index++;
            }
        }
    }
}
