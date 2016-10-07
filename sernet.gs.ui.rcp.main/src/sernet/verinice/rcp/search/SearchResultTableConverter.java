/*******************************************************************************
 * Copyright (c) 2016 Daniel Murygin.
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

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import sernet.verinice.model.search.VeriniceSearchResultRow;
import sernet.verinice.model.search.VeriniceSearchResultTable;
import sernet.verinice.rcp.search.column.ColumnStore;
import sernet.verinice.rcp.search.column.IColumn;
import sernet.verinice.rcp.search.column.IColumnStore;
import sernet.verinice.rcp.search.column.IconColumn;

/**
 * Converts a {@link VeriniceSearchResultTable} to a simple table.
 * Converts only columns which are defined in {@link IColumnStore} parameter.
 *
 * Do not create instances of this class, use public static methods.
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public abstract class SearchResultTableConverter  {

    private static final List<String> COLUMN_BLACKLIST;
    static {
        COLUMN_BLACKLIST = new LinkedList<>();
        COLUMN_BLACKLIST.add(IconColumn.ICON_PROPERTY_NAME);
    }

    private SearchResultTableConverter() {
        // Do not create instances, use public static methods
    }

    public static List<String[]> convertTable(VeriniceSearchResultTable result, IColumnStore columnStore) {
        IColumnStore converterColumnStore = columnStore;
        if(converterColumnStore==null) {
            converterColumnStore = ColumnStore.createColumnStore(result);
        }
        List<String[]> table = new LinkedList<>();
        disableBlacklistedColumns(converterColumnStore);
        table.add(exportHeaderRow(converterColumnStore));
        Set<VeriniceSearchResultRow> rows = result.getAllResults();
        if(rows==null || rows.isEmpty()) {
            return table;
        }
        for (VeriniceSearchResultRow row : rows) {
            table.add(exportRow(row, converterColumnStore));
        }
        if(result.getHits()>=result.getLimit()) {
            table.add(exportLimitHintRow(result.getLimit()));
        }
        return table;
    }

    private static String[] exportHeaderRow(IColumnStore columnStore) {
        Iterator<IColumn> columns = columnStore.getColumns().iterator();
        String[] csvColumns = new String[columnStore.getColumns().size()];
        for (int i = 0; i < csvColumns.length; i++) {
            IColumn column = columns.next();
            csvColumns[i] = column.getTitle();
        }
        return csvColumns;
    }

    private static String[] exportRow(VeriniceSearchResultRow row, IColumnStore columnStore) {
        Set<IColumn> columns = columnStore.getColumns();
        String[] rowArray = new String[columns.size()];
        int i=0;
        for (IColumn column : columns ) {
            rowArray[i] = row.getValueFromResultString(column.getId());
            i++;
        }
        return rowArray;
    }

    private static String[] exportLimitHintRow(int limit) {
        return new String[]{MessagesCsvExport.getString("CsvExport_1", limit)}; //$NON-NLS-1$

    }

    private static void disableBlacklistedColumns(IColumnStore columnStore) {
        Set<IColumn> visibleColumns = new HashSet<>(columnStore.getColumns());
        for (IColumn column : visibleColumns) {
            if(COLUMN_BLACKLIST.contains(column.getId())) {
                columnStore.setVisible(column, false);
            }
        }
    }

}
