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

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;
import org.eclipse.datatools.connectivity.oda.IParameterMetaData;
import org.eclipse.datatools.connectivity.oda.IQuery;
import org.eclipse.datatools.connectivity.oda.IResultSet;
import org.eclipse.datatools.connectivity.oda.IResultSetMetaData;
import org.eclipse.datatools.connectivity.oda.OdaException;
import org.eclipse.datatools.connectivity.oda.SortSpec;
import org.eclipse.datatools.connectivity.oda.spec.QuerySpecification;

import bsh.EvalError;
import bsh.Interpreter;
import bsh.TargetError;
import sernet.hui.common.VeriniceContext;
import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.hui.common.connect.HuiTypeFactoryException;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.ICommand;
import sernet.verinice.interfaces.oda.IVeriniceOdaDriver;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.oda.driver.Activator;
import sernet.verinice.security.report.ReportClassLoader;
import sernet.verinice.security.report.ReportSecurityException;



public class Query implements IQuery
{
    public static final String ODA_DATA_SOURCE_ID = "verinice.oda.driver.dataSource.id";  //$NON-NLS-1$
    public static final String ODA_DATA_SET_ID = "verinice.oda.driver.dataSet.id";  //$NON-NLS-1$
    
	private Logger log = Logger.getLogger(Query.class);
	
	private int maxRows;
    private String queryText;
    
    
    private Integer vnRootElement;
    
    private Integer[] vnRootElements;
    
    private Interpreter setupInterpreter, interpreter;
    
    private Map<String, String> properties = new HashMap<String, String>();
    
    private Map<String, Object> inParameterValues = new HashMap<String, Object>();
    
    private Object result;
    
    private String[] columns, inParameters;
    
    public static final String PROP_SETUP_QUERY_TEXT = "setupQueryText";
    
    Query(Integer[] rootElementIds){
        if(rootElementIds!=null && rootElementIds.length == 1 ) {
            vnRootElement = rootElementIds[0]; 
            vnRootElements = null;
        } else {
        	vnRootElement = -1;
        	vnRootElements = (rootElementIds != null) ? rootElementIds.clone() : null;
        }
    	init();
    }
    
    Query(Integer rootElementId) {
        vnRootElement = rootElementId; 
        vnRootElements = null;
        init();
    }

    private void init() {
        IVeriniceOdaDriver odaDriver = Activator.getDefault().getOdaDriver();  	
    	ReportClassLoader securedClassLoader = new ReportClassLoader(Query.class.getClassLoader());	

    	try {
    	    // "Setup" BSH environment:
    		setupInterpreter = new Interpreter();
    		setupInterpreter.setClassLoader(securedClassLoader);
    		
    		setupInterpreter.set("__columns", null);
    		setupInterpreter.eval("columns(c) { __columns = c; }");
			
    		setupInterpreter.set("__inParameters", null);
    		setupInterpreter.eval("inParameters(ip) { __inParameters = ip; }");
    		setupInterpreter.set("helper", new Helper());

    		// BSH environment:
    		interpreter = new Interpreter();
    		interpreter.setClassLoader(securedClassLoader);
    		
    		
			interpreter.set("_inpv", inParameterValues);
			interpreter.eval(
					"inpv(s) {" +
					" v = _inpv.get(s);" +
					" return (v == null) ? \"input parameter value \" + s + \" does not exist.\" : v;" +
					"}");

			interpreter.set("_vars", odaDriver.getScriptVariables());
			interpreter.eval(
					"vars(s) {" +
					" v = _vars.get(s);" +
					" return (v == null) ? s + \" does not exist.\" : v;" +
					"}");
			
    		interpreter.set("helper", new Helper());
    		interpreter.eval("gpt(entityType) { return helper.getAllPropertyTypes(entityType); }");
    		interpreter.set("properties", properties);
    		

    		
    		
		} catch (EvalError e) {
		    log.error("Error while evaluating a BeanShell script for an ODA query.", e);
			throw new RuntimeException("Error while evaluating a BeanShell script for an ODA query.", e);
		} catch (RuntimeException e) {
            log.error("RuntimeException while creating an ODA query.", e);
            throw e;
        } catch (Exception e) {
		    log.error("Exception while creating an ODA query.", e);
            throw new RuntimeException("Error while evaluating a BeanShell script for an ODA query.", e);
        }
    }
    
    /**
     * A class with utility methods which are supposed to be used by report scripts.
     * 
     * @author Robert Schuster <r.schuster@tarent.de>
     *
     */
    public class Helper {
        
    	public ICommand execute(ICommand c)
    	{
    		try
    		{
    			return Activator.getDefault().getCommandService().executeCommand(c);
    		} catch (CommandException e)
    		{
    		    log.error("Query Helper: running a command failed.", e);
    			throw new IllegalStateException("Running the command failed.", e);
    		}
    	}
    	
        /**
         * A variant of 'retrieveEntityValues' which does not specify the type of the properties. (Defaults
         * to 'getSimpleValue'.)
         * 
         * @param typeId
         * @param propertyNames
         * @return
         */
        public List<List<Object>> retrieveEntityValues(String typeId, String[] propertyNames)
        {
        	return retrieveEntityValues(typeId, propertyNames, new Class[0]);
        }
        
        /**
         * Retrieves a list containing the values of the propertytypes of the specified entitytype.
         * 
         * The data is returned in a way that it can directly be used for BIRT tables (list of property value lists) 
         * 
         * By specifying the class of the result the retrieval code will use {@link Entity#getInt(String)} (for
         * <code>Integer.class</code>) or {@link Entity#getSimpleValue(String)} (for <code>String.class</code>).
         * 
         * @param typeId
         * @param propertyNames
         * @param classes
         * @return
         */
        public List<List<Object>> retrieveEntityValues(String typeId, String[] propertyNames, Class<?>[] classes)
        {
    		LoadEntityValues command = new LoadEntityValues(typeId, propertyNames, classes );

    			try {
    				command = Activator.getDefault().getCommandService().executeCommand(command);
    			} catch (CommandException e) {
    				return Collections.emptyList();
    			}
    		
    		return command.getResult();
        }

        public String[] getAllPropertyTypes(String entityTypeId) {
            return getAllPropertyTypes(entityTypeId, false);
        }
        
        public String[] getAllPropertyTypes(String entityTypeId, boolean withId) {
            HUITypeFactory htf = (HUITypeFactory) VeriniceContext.get(VeriniceContext.HUI_TYPE_FACTORY);
            String[] props = htf.getEntityType(entityTypeId).getAllPropertyTypeIDsIncludingGroups();
            if (withId) {
                String[] arr = new String[props.length+1];
                System.arraycopy(props, 0, arr, 0, props.length);
                arr[props.length]  = "dbid";
                props = arr;
            }
            return props;
        }
      
        public List<List<Object>> map(List<CnATreeElement> input, String[] props)
        {
        	return map(input, props, new Class<?>[0]);
        }
        
        /**
         * Takes an existing list of {@link CnATreeElement} instances and converts them into a list
         * of string values (this can be used as the input for BIRT tables).
         * 
         * @param input
         * @param props
         * @param classes
         * @param addDbId optionally add database ID of the element at the end of each result
         * @return
         */
        public List<List<Object>> map(List<CnATreeElement> input, String[] props, Class<?>[] classes, boolean addDbId)
        {
            if (input == null || input.size()==0){
                return new ArrayList<List<Object>>();
            }
       
           	MapEntityValues cmd = new MapEntityValues(input.get(0).getEntityType().getId(), reduceToIDs(input), props, classes, addDbId);
        	cmd = (MapEntityValues) execute(cmd);
        	return cmd.getResult();
        }
        
        public List<List<Object>> map(List<CnATreeElement> input, String[] props, Class<?>[] classes, boolean addDbId, boolean mapNumericalOptionValues){
            if (input == null || input.size()==0){
                return new ArrayList<List<Object>>();
            }

            MapEntityValues cmd = new MapEntityValues(input.get(0).getEntityType().getId(), reduceToIDs(input), props, classes, addDbId, mapNumericalOptionValues);
            cmd = (MapEntityValues) execute(cmd);
            return cmd.getResult();
            
        }
        
        /**
         * Takes an existing list of {@link CnATreeElement} instances and converts them into a list
         * of string values (this can be used as the input for BIRT tables).
         * 
         * @param elementList
         * @param propertyIdArray
         * @param classes
         * @param addDbId optionally add database ID of the element at the end of each result
         * @return
         */
        public List<List<Object>> mapfast(List<CnATreeElement> elementList, String[] propertyIdArray) {
            if (elementList == null || elementList.size()==0) {
                return new ArrayList<List<Object>>();
            }
            List<List<Object>> result = new ArrayList<List<Object>>(elementList.size());           
            for (CnATreeElement element : elementList) {              
                List<Object> row = LoadEntityValues.convertValuesToList(element.getEntity(), propertyIdArray);
                if (log.isDebugEnabled()) {
                    log.debug("Adding dbid: " + element.getDbId() + " to " + element.getTitle());
                }
                // add db-id
                row.add(element.getDbId().toString());
                result.add(row);
            }
            return result;
        }
        
        /**
         * @param input
         * @return
         */
        private List<Integer> reduceToIDs(List<CnATreeElement> input) {
            List<Integer> result_0 = new ArrayList<Integer>();
            for (CnATreeElement elmt : input) {
                result_0.add(elmt.getDbId());
            }
            return result_0;
        }

        public List<List<Object>> map(List<CnATreeElement> input, String[] props, boolean withDbId){
            return map(input, props, new Class<?>[0], withDbId);
        }

        public List<List<Object>> map(List<CnATreeElement> input, String[] props, Class<?>[] classes){
            return map(input, props, classes, false);
        }
        
        public List<List<Object>> map(List<CnATreeElement> input, String[] props, boolean withDbId, boolean mapNumericalValues){
            return map(input, props, new Class<?>[0], withDbId, mapNumericalValues);
        }
        
        public Integer getRoot() {
       		return vnRootElement;
        }
        
        public Integer[] getRootElements(){
            if (vnRootElements == null || vnRootElements.length <= 0) {
                return new Integer[] { getRoot() };
            }
        	return vnRootElements;
        }

        /**
         * Takes a {@link BufferedImage} instance and turns it into a byte array which can be used
         * by BIRT's dynamic images.
         * 
         * <p>Note: If a dataset should contain only a single image it *MUST* be wrapped
         * using {@link #wrapeSingleImageResult}.</p>
         * 
         * @param im
         * @return
         * @throws IOException
         */
        public byte[] createImageResult(BufferedImage im) throws IOException
        {
        	ByteArrayOutputStream bos = new ByteArrayOutputStream();
        	ImageIO.write(im, "png", bos);
        	return bos.toByteArray();
        }
        
        public Object arraycopy(Object sourceArray, int sourcePosition, Object destinationArray, int destinationPosition, int length){
            System.arraycopy(sourceArray, sourcePosition, destinationArray, destinationPosition, length);
            return destinationArray;
        }
        
    }
	
	@Override
    public void prepare( String queryText ) throws OdaException
	{
        this.queryText = queryText;
	}
	
	@Override
    public void setAppContext( Object context ) throws OdaException
	{
	    // do nothing; assumes no support for pass-through context
	}

	@Override
    public void close() throws OdaException
	{
        queryText = null;
        result = null;
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#getMetaData()
	 */
	@Override
    public IResultSetMetaData getMetaData() throws OdaException
	{
		return new ResultSetMetaData(runQuery(), columns);
	}
	
	private void runSetupQuery() throws OdaException
	{
		try {
			String setupQueryText = properties.get(PROP_SETUP_QUERY_TEXT);
			if (setupQueryText == null){
				return;
			}
			setupInterpreter.eval(setupQueryText);
			Object cols = setupInterpreter.get("__columns");
			if (cols instanceof String[]){
				columns = (String[]) cols;
			} else {
				columns = null;
			}
			Object inp = setupInterpreter.get("__inParameters");
			if (inp instanceof String[]){
				inParameters = (String[]) inp;
			} else {
				inParameters = null;
			}
		} catch (EvalError e) {
			log.error("Error evaluating the setup query: ", e);
			
			if(e instanceof TargetError){
			    TargetError targetError = (TargetError) e;
			    if(targetError.getTarget() instanceof HuiTypeFactoryException){
			        throw new IllegalStateException("verinice server is not available.", targetError.getTarget());
			    }
			}

			throw new IllegalStateException("Unable to execute setup query: " + e.getErrorText());
		}
	}
	
    private Object runQuery() throws OdaException
	{
	    
		runSetupQuery();
		
		try {
			result = interpreter.eval(queryText);
			
		} catch (EvalError e) {
			result = "Exception while executing query: ";
			if(e.getMessage() != null){
			    result = result + e.getMessage();
			} 
			if(e.getScriptStackTrace() != null){
			    result = result + "\n\n"+  e.getScriptStackTrace();
			}
			if(e.getCause()!=null) {
				result = result + ", " + e.getCause().getMessage();
			}
			log.error("Error evaluating the query: " + queryText + "\n\n" + result, e);

			Throwable t = ((TargetError)e).getTarget();
			if(t instanceof ReportSecurityException){
			    throw (ReportSecurityException)t;
			}

		}
		
		return result;
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#executeQuery()
	 */
	@Override
    public IResultSet executeQuery() throws OdaException
	{
		ResultSet resultSet = new ResultSet(runQuery(), columns);
		resultSet.setMaxRows( getMaxRows() );
		return resultSet;
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#setProperty(java.lang.String, java.lang.String)
	 */
	@Override
    public void setProperty( String name, String value ) throws OdaException
	{
		properties.put(name, value);
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#setMaxRows(int)
	 */
	@Override
    public void setMaxRows( int max ) throws OdaException
	{
	    maxRows = max;
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#getMaxRows()
	 */
	@Override
    public int getMaxRows() throws OdaException
	{
		return maxRows;
	}
	
	private void setValue(int parameterId, Object value) throws OdaException
	{
    	runSetupQuery();
    	if (inParameters != null
    			&& inParameters.length >= parameterId)
    	{
    		inParameterValues.put(inParameters[parameterId-1], value);
    	}
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#clearInParameters()
	 */
	@Override
    public void clearInParameters() throws OdaException
	{
		inParameterValues.clear();
	}

	@Override
    public void setInt( String parameterName, int value ) throws OdaException
	{
		inParameterValues.put(parameterName, value);
	}

	@Override
    public void setInt( int parameterId, int value ) throws OdaException
	{
		setValue(parameterId, value);
	}

	@Override
    public void setDouble( String parameterName, double value ) throws OdaException
	{
		inParameterValues.put(parameterName, value);
	}

	@Override
    public void setDouble( int parameterId, double value ) throws OdaException
	{
		setValue(parameterId, value);
	}

	@Override
    public void setBigDecimal( String parameterName, BigDecimal value ) throws OdaException
	{
		inParameterValues.put(parameterName, value);
	}

	@Override
    public void setBigDecimal( int parameterId, BigDecimal value ) throws OdaException
	{
		setValue(parameterId, value);
	}

	@Override
    public void setString( String parameterName, String value ) throws OdaException
	{
		inParameterValues.put(parameterName, value);
	}

	@Override
    public void setString( int parameterId, String value ) throws OdaException
	{
		setValue(parameterId, value);
	}

	@Override
    public void setDate( String parameterName, Date value ) throws OdaException
	{
		inParameterValues.put(parameterName, value);
	}

	@Override
    public void setDate( int parameterId, Date value ) throws OdaException
	{
		setValue(parameterId, value);
	}

	@Override
    public void setTime( String parameterName, Time value ) throws OdaException
	{
		inParameterValues.put(parameterName, value);
	}

	@Override
    public void setTime( int parameterId, Time value ) throws OdaException
	{
		setValue(parameterId, value);
	}

	@Override
    public void setTimestamp( String parameterName, Timestamp value ) throws OdaException
	{
		inParameterValues.put(parameterName, value);
	}

	@Override
    public void setTimestamp( int parameterId, Timestamp value ) throws OdaException
	{
		setValue(parameterId, value);
	}

    @Override
    public void setBoolean( String parameterName, boolean value )
            throws OdaException
    {
		inParameterValues.put(parameterName, value);
    }

    @Override
    public void setBoolean( int parameterId, boolean value )
            throws OdaException
    {
		setValue(parameterId, value);
    }

    @Override
    public void setObject( String parameterName, Object value )
            throws OdaException
    {
		inParameterValues.put(parameterName, value);
    }
    
    @Override
    public void setObject( int parameterId, Object value ) throws OdaException
    {
		setValue(parameterId, value);
    }
    
    @Override
    public void setNull( String parameterName ) throws OdaException
    {
		inParameterValues.put(parameterName, null);
    }

    @Override
    public void setNull( int parameterId ) throws OdaException
    {
		setValue(parameterId, null);
    }

	@Override
    public int findInParameter( String parameterName ) throws OdaException
	{
		for (int i = 0; i < inParameters.length; i++)
		{
			if (inParameters[i].equals(parameterName)){
				return i;
			}
		}
		
		return -1;
	}

	@Override
    public IParameterMetaData getParameterMetaData() throws OdaException
	{
		runSetupQuery();
		return new ParameterMetaData(inParameters);
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#setSortSpec(org.eclipse.datatools.connectivity.oda.SortSpec)
	 */
	@Override
    public void setSortSpec( SortSpec sortBy ) throws OdaException
	{
		// only applies to sorting, assumes not supported
        throw new UnsupportedOperationException();
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IQuery#getSortSpec()
	 */
	@Override
    public SortSpec getSortSpec() throws OdaException
	{
		// only applies to sorting
		return null;
	}

    /* (non-Javadoc)
     * @see org.eclipse.datatools.connectivity.oda.IQuery#setSpecification(org.eclipse.datatools.connectivity.oda.spec.QuerySpecification)
     */
    @Override
    @SuppressWarnings("restriction")
    public void setSpecification( QuerySpecification querySpec )
            throws OdaException
    {
        // assumes no support
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see org.eclipse.datatools.connectivity.oda.IQuery#getSpecification()
     */
    @Override
    @SuppressWarnings("restriction")
    public QuerySpecification getSpecification()
    {
        // assumes no support
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.datatools.connectivity.oda.IQuery#getEffectiveQueryText()
     */
    @Override
    public String getEffectiveQueryText()
    {
        return queryText;
    }

    /* (non-Javadoc)
     * @see org.eclipse.datatools.connectivity.oda.IQuery#cancel()
     */
    @Override
    public void cancel() throws OdaException
    {
    	result = null;
    }
    
    private Set<String> getImportsFromQuery(String query){
        Set<String> imports = new HashSet<String>();
        StringTokenizer tokenizer = new StringTokenizer(query, ";");
        while(tokenizer.hasMoreTokens()){
            String token = tokenizer.nextToken();
            if(token.trim().startsWith("import")){
                String importPath = token.substring(token.lastIndexOf(" "));
                imports.add(importPath.trim());
            }
        }
        return imports;
    }
    
}
