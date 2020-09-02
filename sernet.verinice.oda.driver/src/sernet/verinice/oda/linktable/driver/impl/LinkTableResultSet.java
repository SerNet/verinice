/*******************************************************************************
 * Copyright (c) 2016 Daniel Murygin <dm{a}sernet{dot}de>.
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
 *     Daniel Murygin <dm{a}sernet{dot}de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.oda.linktable.driver.impl;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.datatools.connectivity.oda.IBlob;
import org.eclipse.datatools.connectivity.oda.IClob;
import org.eclipse.datatools.connectivity.oda.IResultSet;
import org.eclipse.datatools.connectivity.oda.IResultSetMetaData;
import org.eclipse.datatools.connectivity.oda.OdaException;

/**
 * A BIRT ODA result set for a link table 
 *
 * @author Daniel Murygin <dm{a}sernet{dot}de>
 */
@SuppressWarnings("restriction")
public class LinkTableResultSet implements IResultSet {
    
    private static final Logger LOG = Logger.getLogger(LinkTableResultSet.class);
    
    private int maxRows = 0;
    private int currentRowId;

    private List<List<String>> linkTable;
    private IResultSetMetaData metaData;

    public LinkTableResultSet(List<List<String>> linkTable, IResultSetMetaData metaData) {
        this.linkTable = linkTable;
        this.metaData = metaData;
        if(linkTable!=null) {
            this.maxRows = linkTable.size();
        }
    }

    /*
     * @see org.eclipse.datatools.connectivity.oda.IResultSet#getMetaData()
     */
    @Override
    public IResultSetMetaData getMetaData() throws OdaException {
        return metaData;
    }

    /*
     * @see org.eclipse.datatools.connectivity.oda.IResultSet#setMaxRows(int)
     */
    @Override
    public void setMaxRows(int maxRows) throws OdaException {
        this.maxRows = maxRows;
    }

    /**
     * Returns the maximum number of rows that can be fetched from this result
     * set.
     * 
     * @return the maximum number of rows to fetch.
     */
    protected int getMaxRows() {
        return maxRows;
    }

    /*
     * @see org.eclipse.datatools.connectivity.oda.IResultSet#next()
     */
    @Override
    public boolean next() throws OdaException {
        if (currentRowId < maxRows) {
            currentRowId++;
            return true;
        }
        return false;
    }

    @Override
    public void close() throws OdaException {
        currentRowId = 0; // reset row counter
    }

    @Override
    public int getRow() throws OdaException {
        return currentRowId;
    }
    
    @Override
    public Object getObject(int index) throws OdaException { 
        try {
            return linkTable.get(currentRowId-1).get(index-1);
        } catch (Exception e){
            LOG.error("No value found for row: " + currentRowId + " and column: " + index, e);
            return null;
        }
    }

    @Override
    public String getString(int index) throws OdaException {
        Object r = getObject(index);

        return (r == null) ? "null" : r.toString();
    }

    @Override
    public String getString(String columnName) throws OdaException {
        return getString(findColumn(columnName));
    }

    @Override
    public int getInt(int index) throws OdaException {
        return getRow();
    }

    @Override
    public int getInt(String columnName) throws OdaException {
        return getInt(findColumn(columnName));
    }

    @Override
    public double getDouble(int index) throws OdaException {
        throw new UnsupportedOperationException();
    }

    @Override
    public double getDouble(String columnName) throws OdaException {
        return getDouble(findColumn(columnName));
    }

    @Override
    public BigDecimal getBigDecimal(int index) throws OdaException {
        throw new UnsupportedOperationException();
    }

    @Override
    public BigDecimal getBigDecimal(String columnName) throws OdaException {
        return getBigDecimal(findColumn(columnName));
    }

    @Override
    public Date getDate(int index) throws OdaException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Date getDate(String columnName) throws OdaException {
        return getDate(findColumn(columnName));
    }

    @Override
    public Time getTime(int index) throws OdaException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Time getTime(String columnName) throws OdaException {
        return getTime(findColumn(columnName));
    }

    @Override
    public Timestamp getTimestamp(int index) throws OdaException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Timestamp getTimestamp(String columnName) throws OdaException {
        return getTimestamp(findColumn(columnName));
    }

    @Override
    public IBlob getBlob(int index) throws OdaException {
        throw new UnsupportedOperationException();
    }

    @Override
    public IBlob getBlob(String columnName) throws OdaException {
        return getBlob(findColumn(columnName));
    }

    @Override
    public IClob getClob(int index) throws OdaException {
        throw new UnsupportedOperationException();
    }

    @Override
    public IClob getClob(String columnName) throws OdaException {
        return getClob(findColumn(columnName));
    }

    @Override
    public boolean getBoolean(int index) throws OdaException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean getBoolean(String columnName) throws OdaException {
        return getBoolean(findColumn(columnName));
    }

    @Override
    public Object getObject(String columnName) throws OdaException {
        return getObject(findColumn(columnName));
    }

    @Override
    public boolean wasNull() throws OdaException {
        return false;
    }

    @Override
    public int findColumn(String columnName) throws OdaException {
        throw new UnsupportedOperationException();
    }
}
