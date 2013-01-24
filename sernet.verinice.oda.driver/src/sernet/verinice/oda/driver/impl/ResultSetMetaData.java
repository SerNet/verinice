/*******************************************************************************
 * Copyright (c) 2010 Robert Schuster <r.schuster@tarent.de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *     You should have received a copy of the GNU Lesser General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Robert Schuster <r.schuster@tarent.de> - initial API and implementation
 ******************************************************************************/

package sernet.verinice.oda.driver.impl;

import java.lang.reflect.Array;
import java.util.Collection;

import org.eclipse.datatools.connectivity.oda.IResultSetMetaData;
import org.eclipse.datatools.connectivity.oda.OdaException;

public class ResultSetMetaData implements IResultSetMetaData {
	private int columnCount;
	private int rowCount;

	private final Accessor accessor;

	private Object result;
	
	private String[] columns;

	ResultSetMetaData(Object result, String[] columns) {
		this.result = result;
		this.columns = (columns != null) ? columns.clone() : null;

		Class<? extends Accessor> accessorClass = initValues(result, columns);
		if(accessorClass.equals(MultiDimensionalArrayAccessor.class)){
		    accessor = new MultiDimensionalArrayAccessor();
		} else if(accessorClass.equals(OneDimensionalArrayAccessor.class)){
		    accessor = new OneDimensionalArrayAccessor();
		} else if(accessorClass.equals(MultiDimensionalCollectionAccessor.class)){
		    accessor = new MultiDimensionalCollectionAccessor();
		} else if(accessorClass.equals(OneDimensionalCollectionAccessor.class)){
		    accessor = new OneDimensionalCollectionAccessor();
		} else {
		    accessor = new SingleValueAccessor();
		}
	}

    private Class<? extends Accessor> initValues(Object result, String[] columns) {
        Class<? extends Accessor> clazz = null;
        Class<?> rowClass = result.getClass();
		if (rowClass.isArray() && rowClass.getComponentType() != Byte.TYPE) {
			Object firstElement = (Array.getLength(result) > 0 ? Array.get(result, 0) : null);
			Class<?> columnClass = (firstElement != null ? firstElement.getClass() : null);
			if (columnClass != null && columnClass.isArray()) {
				// 2-dimensional array (at least)
				// first dimension: row
				// second dimension: column
				columnCount = (columns != null) ? columns.length : Array.getLength(firstElement);
				rowCount = Array.getLength(result);
				clazz = MultiDimensionalArrayAccessor.class;
			} else {
				// one-dimensional array -> elements are rows
				columnCount = (columns != null) ? columns.length : 1;
				rowCount = Array.getLength(result);
				clazz = OneDimensionalArrayAccessor.class;
			}
		} else if (Collection.class.isAssignableFrom(rowClass)) {
			
			Object[] arr = ((Collection) result).toArray();
			
			Object firstElement = (arr.length > 0 ? arr[0] : null);
			Class<?> columnClass = (firstElement != null ? firstElement.getClass() : null);
			if (columnClass != null && Collection.class.isAssignableFrom(columnClass)) {
				// 2-dimensional collection (at least)
				// first dimension: row
				// second dimension: column
				columnCount = (columns != null) ? columns.length : ((Collection) firstElement).size();
				rowCount = ((Collection) result).size();
				clazz = MultiDimensionalCollectionAccessor.class;
			} else {
				// one-dimensional collection -> elements are rows
				columnCount = (columns != null) ? columns.length : 1;
				rowCount = ((Collection) result).size();
				clazz = OneDimensionalCollectionAccessor.class;
			}
		} else {
			// single value
			columnCount = (columns != null) ? columns.length : 1;
			rowCount = 1;
			clazz = SingleValueAccessor.class;
		}

		// If a data set is actually empty we need to pass -1 as the rowcount otherwise BIRT
		// thinks the dataset is endless.
		if (rowCount == 0){
			rowCount = -1;
		}
		return clazz;
    }

	Object getValue(int row, int column) {
		try
		{
			return accessor.get(result, row - 1, column - 1);
		} catch (ArrayIndexOutOfBoundsException aiiobe)
		{
			return null;
		}
		
	}

	/*
	 * @see
	 * org.eclipse.datatools.connectivity.oda.IResultSetMetaData#getColumnCount
	 * ()
	 */
	public int getColumnCount() throws OdaException {
		return columnCount;
	}

	/*
	 * @see
	 * org.eclipse.datatools.connectivity.oda.IResultSetMetaData#getColumnName
	 * (int)
	 */
	public String getColumnName(int index) throws OdaException {
		if (columns != null)
		{
			return columns[index - 1];
		}
		else
		{
			return "Column" + index;
		}
	}

	int getRowCount() {
		return rowCount;
	}

	/*
	 * @see
	 * org.eclipse.datatools.connectivity.oda.IResultSetMetaData#getColumnLabel
	 * (int)
	 */
	public String getColumnLabel(int index) throws OdaException {
		return getColumnName(index); // default
	}

	/*
	 * @see
	 * org.eclipse.datatools.connectivity.oda.IResultSetMetaData#getColumnType
	 * (int)
	 */
	public int getColumnType(int index) throws OdaException {
		return java.sql.Types.JAVA_OBJECT;
	}

	/*
	 * @see
	 * org.eclipse.datatools.connectivity.oda.IResultSetMetaData#getColumnTypeName
	 * (int)
	 */
	public String getColumnTypeName(int index) throws OdaException {
		int nativeTypeCode = getColumnType(index);
		return Driver.getNativeDataTypeName(nativeTypeCode);
	}

	/*
	 * @seeorg.eclipse.datatools.connectivity.oda.IResultSetMetaData#
	 * getColumnDisplayLength(int)
	 */
	public int getColumnDisplayLength(int index) throws OdaException {
		// TODO replace with data source specific implementation

		// hard-coded for demo purpose
		return 8;
	}

	/*
	 * @see
	 * org.eclipse.datatools.connectivity.oda.IResultSetMetaData#getPrecision
	 * (int)
	 */
	public int getPrecision(int index) throws OdaException {
		// TODO Auto-generated method stub
		return -1;
	}

	/*
	 * @see
	 * org.eclipse.datatools.connectivity.oda.IResultSetMetaData#getScale(int)
	 */
	public int getScale(int index) throws OdaException {
		// TODO Auto-generated method stub
		return -1;
	}

	/*
	 * @see
	 * org.eclipse.datatools.connectivity.oda.IResultSetMetaData#isNullable(int)
	 */
	public int isNullable(int index) throws OdaException {
		// TODO Auto-generated method stub
		return IResultSetMetaData.columnNullableUnknown;
	}

	interface Accessor {
		Object get(Object result, int row, int column);
	}

	static class SingleValueAccessor implements Accessor {
		public Object get(Object result, int row, int column) {
			return result;
		}
	}

	static class OneDimensionalArrayAccessor implements Accessor {
		public Object get(Object result, int row, int column) {
			return Array.get(result, row);
		}
	}

	static class MultiDimensionalArrayAccessor implements Accessor {
		public Object get(Object result, int row, int column) {
			return Array.get(Array.get(result, row), column);
		}
	}

	static class OneDimensionalCollectionAccessor implements Accessor {
		public Object get(Object result, int row, int column) {
			return ((Collection) result).toArray()[row];
		}
	}

	static class MultiDimensionalCollectionAccessor implements Accessor {
		public Object get(Object result, int row, int column) {
			return ((Collection) ((Collection) result).toArray()[row])
					.toArray()[column];
		}
	}
}
