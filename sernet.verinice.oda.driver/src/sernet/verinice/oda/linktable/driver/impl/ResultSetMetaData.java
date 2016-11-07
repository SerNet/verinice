/***************************************************************************
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
 ***************************************************************************/
package sernet.verinice.oda.linktable.driver.impl;

import java.lang.reflect.Array;
import java.util.Collection;

import org.eclipse.datatools.connectivity.oda.IResultSetMetaData;
import org.eclipse.datatools.connectivity.oda.OdaException;

import sernet.verinice.oda.driver.impl.Driver;

/**
 * Meta data for a BIRT ODA result set based on a link table
 *
 * @author Daniel Murygin <dm{a}sernet{dot}de>
 */
public class ResultSetMetaData implements IResultSetMetaData {
    private int columnCount;
    private int rowCount;

    private String[] columns;

    public ResultSetMetaData(String[] columns) {
        this.columns = (columns != null) ? columns.clone() : null;
        this.columnCount = (columns != null) ? columns.length : 0;
    }

    @Override
    public int getColumnCount() throws OdaException {
        return columnCount;
    }

    @Override
    public String getColumnName(int index) throws OdaException {
        if (columns != null) {
            return columns[index - 1];
        } else {
            return "Column" + index;
        }
    }

    int getRowCount() {
        return rowCount;
    }

    @Override
    public String getColumnLabel(int index) throws OdaException {
        return getColumnName(index); // default
    }

    @Override
    public int getColumnType(int index) throws OdaException {
        return java.sql.Types.JAVA_OBJECT;
    }

    @Override
    public String getColumnTypeName(int index) throws OdaException {
        int nativeTypeCode = getColumnType(index);
        return Driver.getNativeDataTypeName(nativeTypeCode, Query.ODA_DATA_SET_ID);
    }

    @Override
    public int getColumnDisplayLength(int index) throws OdaException {
        return 8;
    }

    @Override
    public int getPrecision(int index) throws OdaException {
        return -1;
    }

    @Override
    public int getScale(int index) throws OdaException {
        return -1;
    }

    @Override
    public int isNullable(int index) throws OdaException {
        return IResultSetMetaData.columnNullableUnknown;
    }

    interface Accessor {
        Object get(Object result, int row, int column);
    }

    static class SingleValueAccessor implements Accessor {
        @Override
        public Object get(Object result, int row, int column) {
            return result;
        }
    }

    static class OneDimensionalArrayAccessor implements Accessor {
        @Override
        public Object get(Object result, int row, int column) {
            return Array.get(result, row);
        }
    }

    static class MultiDimensionalArrayAccessor implements Accessor {
        @Override
        public Object get(Object result, int row, int column) {
            return Array.get(Array.get(result, row), column);
        }
    }

    static class OneDimensionalCollectionAccessor implements Accessor {
        @Override
        public Object get(Object result, int row, int column) {
            return ((Collection) result).toArray()[row];
        }
    }

    static class MultiDimensionalCollectionAccessor implements Accessor {
        @Override
        public Object get(Object result, int row, int column) {
            return ((Collection) ((Collection) result).toArray()[row]).toArray()[column];
        }
    }
}
