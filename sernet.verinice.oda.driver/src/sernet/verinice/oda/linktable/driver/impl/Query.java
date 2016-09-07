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
import java.util.LinkedList;
import java.util.List;

import org.eclipse.datatools.connectivity.oda.IParameterMetaData;
import org.eclipse.datatools.connectivity.oda.IQuery;
import org.eclipse.datatools.connectivity.oda.IResultSet;
import org.eclipse.datatools.connectivity.oda.IResultSetMetaData;
import org.eclipse.datatools.connectivity.oda.OdaException;
import org.eclipse.datatools.connectivity.oda.SortSpec;
import org.eclipse.datatools.connectivity.oda.spec.QuerySpecification;

import sernet.verinice.service.linktable.ColumnPathParser;
import sernet.verinice.service.linktable.ILinkTableConfiguration;
import sernet.verinice.service.linktable.LinkTableService;
import sernet.verinice.service.linktable.vlt.VeriniceLinkTable;
import sernet.verinice.service.linktable.vlt.VeriniceLinkTableIO;

/**
 * A query to build a BIRT data set for a link table
 *
 * @author Daniel Murygin <dm{a}sernet{dot}de>
 */
public class Query implements IQuery {

    public static final String ODA_DATA_SET_ID = "verinice.oda.linktable.driver.dataSet.id";  //$NON-NLS-1$

    private static final int DEFAULT_MAX_ROWS = 1000;
    
    private String vlt = null;
    
    private List<String> columnList;
    
    private IResultSetMetaData resultSetMetaData;
    
    private Integer[] scopeIds;
    
    /**
     * @param rootElementIds
     */
    public Query(Integer[] rootElementIds) {
        columnList = new LinkedList<String>();
        this.scopeIds = rootElementIds;
    }

    /* (non-Javadoc)
     * @see org.eclipse.datatools.connectivity.oda.IQuery#cancel()
     */
    @Override
    public void cancel() throws OdaException {
        // nothing to do
    }

    /* (non-Javadoc)
     * @see org.eclipse.datatools.connectivity.oda.IQuery#clearInParameters()
     */
    @Override
    public void clearInParameters() throws OdaException {
        // nothing to do
    }

    /* (non-Javadoc)
     * @see org.eclipse.datatools.connectivity.oda.IQuery#close()
     */
    @Override
    public void close() throws OdaException {
        this.vlt = null;  
    }

    /* (non-Javadoc)
     * @see org.eclipse.datatools.connectivity.oda.IQuery#executeQuery()
     */
    @Override
    public IResultSet executeQuery() throws OdaException {
        return new LinkTableResultSet(createTable(), resultSetMetaData);
    }
    
    private List<List<String>> createTable() {
        LinkTableService linkTableService = new LinkTableService();
        List<List<String>> table = linkTableService.createTable(createLinkTableConfiguration());
        // Remove the heading line of the table 
        table.remove(0);
        return table;
    }

    public ILinkTableConfiguration createLinkTableConfiguration() {
        VeriniceLinkTable vltFile = VeriniceLinkTableIO.readContent(vlt);
        ILinkTableConfiguration linkTableConfiguration = VeriniceLinkTableIO.createLinkTableConfiguration(vltFile);
        if(scopeIds==null || scopeIds.length == 0) {
            linkTableConfiguration.removeAllScopeIds();
        } else {
            for (Integer scopeId : scopeIds) {
                linkTableConfiguration.addScopeId(scopeId);
            }
        }
        return linkTableConfiguration;
    }

    /* (non-Javadoc)
     * @see org.eclipse.datatools.connectivity.oda.IQuery#findInParameter(java.lang.String)
     */
    @Override
    public int findInParameter(String arg0) throws OdaException {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see org.eclipse.datatools.connectivity.oda.IQuery#getEffectiveQueryText()
     */
    @Override
    public String getEffectiveQueryText() {
        return vlt;
    }

    /* (non-Javadoc)
     * @see org.eclipse.datatools.connectivity.oda.IQuery#getMaxRows()
     */
    @Override
    public int getMaxRows() throws OdaException {   
        return DEFAULT_MAX_ROWS;
    }

    /* (non-Javadoc)
     * @see org.eclipse.datatools.connectivity.oda.IQuery#getMetaData()
     */
    @Override
    public IResultSetMetaData getMetaData() throws OdaException {
        return resultSetMetaData;
    }

    /* (non-Javadoc)
     * @see org.eclipse.datatools.connectivity.oda.IQuery#getParameterMetaData()
     */
    @Override
    public IParameterMetaData getParameterMetaData() throws OdaException {
        
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.datatools.connectivity.oda.IQuery#getSortSpec()
     */
    @Override
    public SortSpec getSortSpec() throws OdaException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.datatools.connectivity.oda.IQuery#getSpecification()
     */
    @Override
    public QuerySpecification getSpecification() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.datatools.connectivity.oda.IQuery#prepare(java.lang.String)
     */
    @Override
    public void prepare(String queryText) throws OdaException {    
        this.vlt = queryText;
        columnList = getColumnList(this.vlt);
        resultSetMetaData = new ResultSetMetaData(columnList.toArray(new String[columnList.size()]));
    }

    public static List<String> getColumnList(String queryText) {
        List<String> columnList = new LinkedList<String>();
        VeriniceLinkTable vltFile = VeriniceLinkTableIO.readContent(queryText);
        ILinkTableConfiguration linkTableConfiguration = VeriniceLinkTableIO.createLinkTableConfiguration(vltFile);
        for (String columnPath : linkTableConfiguration.getColumnPaths()) {
            columnList.add(ColumnPathParser.extractAlias(columnPath));
        }
        return columnList;
    }

    @Override
    public void setAppContext(Object arg0) throws OdaException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setBigDecimal(String arg0, BigDecimal arg1) throws OdaException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setBigDecimal(int arg0, BigDecimal arg1) throws OdaException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setBoolean(String arg0, boolean arg1) throws OdaException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setBoolean(int arg0, boolean arg1) throws OdaException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setDate(String arg0, Date arg1) throws OdaException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setDate(int arg0, Date arg1) throws OdaException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setDouble(String arg0, double arg1) throws OdaException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setDouble(int arg0, double arg1) throws OdaException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setInt(String arg0, int arg1) throws OdaException {
        throw new UnsupportedOperationException();
    }

    public void setInt(int arg0, int arg1) throws OdaException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setMaxRows(int arg0) throws OdaException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setNull(String arg0) throws OdaException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setNull(int arg0) throws OdaException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setObject(String arg0, Object arg1) throws OdaException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setObject(int arg0, Object arg1) throws OdaException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setProperty(String arg0, String arg1) throws OdaException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setSortSpec(SortSpec arg0) throws OdaException {
        throw new UnsupportedOperationException();
        
    }

    @Override
    public void setSpecification(QuerySpecification arg0)
            throws OdaException, UnsupportedOperationException {
        throw new UnsupportedOperationException();       
    }

    @Override
    public void setString(String arg0, String arg1) throws OdaException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setString(int arg0, String arg1) throws OdaException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setTime(String arg0, Time arg1) throws OdaException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setTime(int arg0, Time arg1) throws OdaException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setTimestamp(String arg0, Timestamp arg1) throws OdaException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setTimestamp(int arg0, Timestamp arg1) throws OdaException {
        throw new UnsupportedOperationException();
    }
}
