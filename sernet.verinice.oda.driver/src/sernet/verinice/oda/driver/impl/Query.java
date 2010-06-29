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

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.eclipse.datatools.connectivity.oda.IParameterMetaData;
import org.eclipse.datatools.connectivity.oda.IQuery;
import org.eclipse.datatools.connectivity.oda.IResultSet;
import org.eclipse.datatools.connectivity.oda.IResultSetMetaData;
import org.eclipse.datatools.connectivity.oda.OdaException;
import org.eclipse.datatools.connectivity.oda.SortSpec;
import org.eclipse.datatools.connectivity.oda.spec.QuerySpecification;

import sernet.hui.common.connect.EntityType;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.hui.common.connect.PropertyGroup;
import sernet.hui.common.connect.PropertyType;
import sernet.verinice.interfaces.oda.IVeriniceOdaDriver;
import sernet.verinice.oda.driver.Activator;
import bsh.EvalError;
import bsh.Interpreter;

public class Query implements IQuery
{
	private int maxRows;
    private String queryText;
    
    private Interpreter interpreter;
    
    private HashMap<String, String> properties = new HashMap<String, String>();
    
    private HashSet<String> inParameters = new HashSet<String>();
    
    private Object result;
    
    private String[] columns;
    
    Query(HUITypeFactory huiTypeFactory)
    {
    	IVeriniceOdaDriver odaDriver = Activator.getDefault().getOdaDriver();
    	
    	try {
    		interpreter = new Interpreter();
			interpreter.set("_vars", odaDriver.getScriptVariables());
			interpreter.eval(
					"vars(s) {" +
					" v = _vars.get(s);" +
					" return (v == null) ? s + \" does not exist.\" : v;" +
					"}");
    		interpreter.set("helper", this);
    		interpreter.eval("gpt(entityType) { return helper.getAllPropertyTypes(entityType); }");
    		interpreter.set("htf", huiTypeFactory);
			interpreter.set("properties", properties);
			interpreter.set("__columns", null);
			interpreter.eval("columns(c) { __columns = c; }");

		} catch (EvalError e) {
			new RuntimeException("Unable to set BSH variable 'properties'.", e);
		}
    }
    
    public String[] getAllPropertyTypes(EntityType et)
    {
    	List<String> result = new ArrayList<String>();
    	for (PropertyType pt : et.getPropertyTypes())
    	{
    		result.add(pt.getName());
    	}
    	
    	for (PropertyGroup pg : et.getPropertyGroups())
    	{
    		for (PropertyType pt : pg.getPropertyTypes())
    		{
    			result.add(pt.getName());
    		}
    	}
    	
    	return result.toArray(new String[result.size()]);
    }
    
    List<List<Object>> map(List<Object> input, String[] props)
    {
    	List<List<Object>> result = new ArrayList<List<Object>>();
    	
    	for (Object o : input)
    	{
    		// TODO: Use anonymous classes
    	}
    	
    	return result;
    }
	
	public void prepare( String queryText ) throws OdaException
	{
        this.queryText = queryText;
	}
	
	public void setAppContext( Object context ) throws OdaException
	{
	    // do nothing; assumes no support for pass-through context
	}

	public void close() throws OdaException
	{
        queryText = null;
        result = null;
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#getMetaData()
	 */
	public IResultSetMetaData getMetaData() throws OdaException
	{
		return new ResultSetMetaData(createResult(), columns);
	}
	
	private Object createResult() throws OdaException
	{
		if (result == null)
		{
			try {
				result = interpreter.eval(queryText);
				Object cols = interpreter.get("__columns");
				if (cols instanceof String[])
					columns = (String[]) cols;
				else
					columns = null;
			} catch (EvalError e) {
				throw (OdaException) new OdaException("Unable to execute query.").initCause(e);
			}
		}
		
		return result;
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#executeQuery()
	 */
	public IResultSet executeQuery() throws OdaException
	{
		ResultSet resultSet = new ResultSet(createResult(), columns);
		resultSet.setMaxRows( getMaxRows() );
		return resultSet;
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#setProperty(java.lang.String, java.lang.String)
	 */
	public void setProperty( String name, String value ) throws OdaException
	{
		properties.put(name, value);
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#setMaxRows(int)
	 */
	public void setMaxRows( int max ) throws OdaException
	{
	    maxRows = max;
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#getMaxRows()
	 */
	public int getMaxRows() throws OdaException
	{
		return maxRows;
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#clearInParameters()
	 */
	public void clearInParameters() throws OdaException
	{
		for (String ip : inParameters)
		{
			try {
				interpreter.unset(ip);
			} catch (EvalError e) {
				throw new RuntimeException("Unable to unset BSH variable '" + ip + "'.", e);
			}
		}
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#setInt(java.lang.String, int)
	 */
	public void setInt( String parameterName, int value ) throws OdaException
	{
		inParameters.add(parameterName);
		try {
			interpreter.set(parameterName, value);
		} catch (EvalError e) {
			throw new RuntimeException("Unable to set BSH variable '" + parameterName + "'.", e);
		}
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#setInt(int, int)
	 */
	public void setInt( int parameterId, int value ) throws OdaException
	{
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#setDouble(java.lang.String, double)
	 */
	public void setDouble( String parameterName, double value ) throws OdaException
	{
		inParameters.add(parameterName);
		try {
			interpreter.set(parameterName, value);
		} catch (EvalError e) {
			throw new RuntimeException("Unable to set BSH variable '" + parameterName + "'.", e);
		}
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#setDouble(int, double)
	 */
	public void setDouble( int parameterId, double value ) throws OdaException
	{
        // TODO Auto-generated method stub
		// only applies to input parameter
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#setBigDecimal(java.lang.String, java.math.BigDecimal)
	 */
	public void setBigDecimal( String parameterName, BigDecimal value ) throws OdaException
	{
		inParameters.add(parameterName);
		try {
			interpreter.set(parameterName, value);
		} catch (EvalError e) {
			throw new RuntimeException("Unable to set BSH variable '" + parameterName + "'.", e);
		}
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#setBigDecimal(int, java.math.BigDecimal)
	 */
	public void setBigDecimal( int parameterId, BigDecimal value ) throws OdaException
	{
        // TODO Auto-generated method stub
		// only applies to input parameter
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#setString(java.lang.String, java.lang.String)
	 */
	public void setString( String parameterName, String value ) throws OdaException
	{
		inParameters.add(parameterName);
		try {
			interpreter.set(parameterName, value);
		} catch (EvalError e) {
			throw new RuntimeException("Unable to set BSH variable '" + parameterName + "'.", e);
		}
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#setString(int, java.lang.String)
	 */
	public void setString( int parameterId, String value ) throws OdaException
	{
        // TODO Auto-generated method stub
		// only applies to input parameter
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#setDate(java.lang.String, java.sql.Date)
	 */
	public void setDate( String parameterName, Date value ) throws OdaException
	{
		inParameters.add(parameterName);
		try {
			interpreter.set(parameterName, value);
		} catch (EvalError e) {
			throw new RuntimeException("Unable to set BSH variable '" + parameterName + "'.", e);
		}
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#setDate(int, java.sql.Date)
	 */
	public void setDate( int parameterId, Date value ) throws OdaException
	{
        // TODO Auto-generated method stub
		// only applies to input parameter
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#setTime(java.lang.String, java.sql.Time)
	 */
	public void setTime( String parameterName, Time value ) throws OdaException
	{
		inParameters.add(parameterName);
		try {
			interpreter.set(parameterName, value);
		} catch (EvalError e) {
			throw new RuntimeException("Unable to set BSH variable '" + parameterName + "'.", e);
		}
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#setTime(int, java.sql.Time)
	 */
	public void setTime( int parameterId, Time value ) throws OdaException
	{
        // TODO Auto-generated method stub
		// only applies to input parameter
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#setTimestamp(java.lang.String, java.sql.Timestamp)
	 */
	public void setTimestamp( String parameterName, Timestamp value ) throws OdaException
	{
		inParameters.add(parameterName);
		try {
			interpreter.set(parameterName, value);
		} catch (EvalError e) {
			throw new RuntimeException("Unable to set BSH variable '" + parameterName + "'.", e);
		}
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#setTimestamp(int, java.sql.Timestamp)
	 */
	public void setTimestamp( int parameterId, Timestamp value ) throws OdaException
	{
        // TODO Auto-generated method stub
		// only applies to input parameter
	}

    /* (non-Javadoc)
     * @see org.eclipse.datatools.connectivity.oda.IQuery#setBoolean(java.lang.String, boolean)
     */
    public void setBoolean( String parameterName, boolean value )
            throws OdaException
    {
		inParameters.add(parameterName);
		try {
			interpreter.set(parameterName, value);
		} catch (EvalError e) {
			throw new RuntimeException("Unable to set BSH variable '" + parameterName + "'.", e);
		}
    }

    /* (non-Javadoc)
     * @see org.eclipse.datatools.connectivity.oda.IQuery#setBoolean(int, boolean)
     */
    public void setBoolean( int parameterId, boolean value )
            throws OdaException
    {
        // TODO Auto-generated method stub       
        // only applies to input parameter
    }

    /* (non-Javadoc)
     * @see org.eclipse.datatools.connectivity.oda.IQuery#setObject(java.lang.String, java.lang.Object)
     */
    public void setObject( String parameterName, Object value )
            throws OdaException
    {
		inParameters.add(parameterName);
		try {
			interpreter.set(parameterName, value);
		} catch (EvalError e) {
			throw new RuntimeException("Unable to set BSH variable '" + parameterName + "'.", e);
		}
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.datatools.connectivity.oda.IQuery#setObject(int, java.lang.Object)
     */
    public void setObject( int parameterId, Object value ) throws OdaException
    {
        // TODO Auto-generated method stub
        // only applies to input parameter
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.datatools.connectivity.oda.IQuery#setNull(java.lang.String)
     */
    public void setNull( String parameterName ) throws OdaException
    {
		inParameters.add(parameterName);
		try {
			interpreter.set(parameterName, null);
		} catch (EvalError e) {
			throw new RuntimeException("Unable to set BSH variable '" + parameterName + "'.", e);
		}
    }

    /* (non-Javadoc)
     * @see org.eclipse.datatools.connectivity.oda.IQuery#setNull(int)
     */
    public void setNull( int parameterId ) throws OdaException
    {
        // TODO Auto-generated method stub
        // only applies to input parameter
    }

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#findInParameter(java.lang.String)
	 */
	public int findInParameter( String parameterName ) throws OdaException
	{
        // TODO Auto-generated method stub
		// only applies to named input parameter
		return 0;
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#getParameterMetaData()
	 */
	public IParameterMetaData getParameterMetaData() throws OdaException
	{
		return new ParameterMetaData();
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#setSortSpec(org.eclipse.datatools.connectivity.oda.SortSpec)
	 */
	public void setSortSpec( SortSpec sortBy ) throws OdaException
	{
		// only applies to sorting, assumes not supported
        throw new UnsupportedOperationException();
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#getSortSpec()
	 */
	public SortSpec getSortSpec() throws OdaException
	{
		// only applies to sorting
		return null;
	}

    /* (non-Javadoc)
     * @see org.eclipse.datatools.connectivity.oda.IQuery#setSpecification(org.eclipse.datatools.connectivity.oda.spec.QuerySpecification)
     */
    @SuppressWarnings("restriction")
    public void setSpecification( QuerySpecification querySpec )
            throws OdaException, UnsupportedOperationException
    {
        // assumes no support
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see org.eclipse.datatools.connectivity.oda.IQuery#getSpecification()
     */
    @SuppressWarnings("restriction")
    public QuerySpecification getSpecification()
    {
        // assumes no support
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.datatools.connectivity.oda.IQuery#getEffectiveQueryText()
     */
    public String getEffectiveQueryText()
    {
        return queryText;
    }

    /* (non-Javadoc)
     * @see org.eclipse.datatools.connectivity.oda.IQuery#cancel()
     */
    public void cancel() throws OdaException, UnsupportedOperationException
    {
    	result = null;
    }
    
}
