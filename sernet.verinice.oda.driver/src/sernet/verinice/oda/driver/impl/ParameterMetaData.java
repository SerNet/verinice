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

import org.eclipse.datatools.connectivity.oda.IParameterMetaData;
import org.eclipse.datatools.connectivity.oda.OdaException;

public class ParameterMetaData implements IParameterMetaData 
{
	String[] inParameters;
	
	ParameterMetaData(String[] inParameters)
	{
		this.inParameters = (inParameters == null ? new String[0] : inParameters);
	}

	/* 
	 * @see org.eclipse.datatools.connectivity.oda.IParameterMetaData#getParameterCount()
	 */
	public int getParameterCount() throws OdaException 
	{
        return inParameters.length;
	}

    /*
	 * @see org.eclipse.datatools.connectivity.oda.IParameterMetaData#getParameterMode(int)
	 */
	public int getParameterMode( int param ) throws OdaException 
	{
		return IParameterMetaData.parameterModeIn;
	}

    public String getParameterName( int param ) throws OdaException
    {
        return inParameters[param-1];
    }

	/* 
	 * @see org.eclipse.datatools.connectivity.oda.IParameterMetaData#getParameterType(int)
	 */
	public int getParameterType( int param ) throws OdaException 
	{
        return java.sql.Types.VARCHAR;
	}

	public String getParameterTypeName( int param ) throws OdaException 
	{
        int nativeTypeCode = getParameterType( param );
        return Driver.getNativeDataTypeName( nativeTypeCode );
	}

	public int getPrecision( int param ) throws OdaException 
	{
        // TODO Auto-generated method stub
		return -1;
	}

	public int getScale( int param ) throws OdaException 
	{
        // TODO Auto-generated method stub
		return -1;
	}

	public int isNullable( int param ) throws OdaException 
	{
		return IParameterMetaData.parameterNullableUnknown;
	}

}
