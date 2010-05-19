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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import org.eclipse.core.runtime.Platform;
import org.eclipse.datatools.connectivity.oda.IConnection;
import org.eclipse.datatools.connectivity.oda.IDataSetMetaData;
import org.eclipse.datatools.connectivity.oda.IQuery;
import org.eclipse.datatools.connectivity.oda.OdaException;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

import sernet.hui.common.connect.HUITypeFactory;
import sernet.verinice.oda.driver.Activator;

import com.ibm.icu.util.ULocale;

/**
 * Implementation class of IConnection for an ODA runtime driver.
 */
public class Connection implements IConnection
{
    private boolean isOpen = false;
    
    private HUITypeFactory huiTypeFactory;
    
    private static final String MAIN_SYMBOLIC_NAME = "sernet.gs.ui.rcp.main"; //$NON-NLS-1$
    
    private Bundle mainBundle;

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IConnection#open(java.util.Properties)
	 */
	public void open( Properties connProperties ) throws OdaException
	{
		try {
			mainBundle = Platform.getBundle(MAIN_SYMBOLIC_NAME);
			
			// When running the driver from inside the designer we expect that the
			// main bundle is not running yet and only in that case we publish
			// a service that can be accessed from the main bundle then.
			if (mainBundle.getState() == Bundle.INSTALLED
					|| mainBundle.getState() == Bundle.RESOLVED)
			{
				BundleContext ctx = Activator.getDefault().getBundle().getBundleContext();
				String uri = connProperties.getProperty("serverURI");
				ctx.registerService(IVeriniceOdaDriver.class.getName(), new VeriniceOdaDriver(uri), null);
			}
			
			mainBundle.start();
		} catch (BundleException e) {
			throw (OdaException) new OdaException("Could not start").initCause(e);
		}
		
		/*
		URL url;
		try {
			// TODO: When nicht angegeben, dann nimm vorkonfigurierte Umgebung
			// HuiTypefactory.getXXX()
			url = new URL(connProperties.getProperty("sncaUrl"));
			huiTypeFactory = HUITypeFactory.createInstance(url);
		} catch (MalformedURLException e) {
			throw new OdaException("URL was invalid.");
		} catch (DBException e) {
			throw new OdaException("URL was invalid.");
		}
		*/
		
	    isOpen = true;        
 	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IConnection#setAppContext(java.lang.Object)
	 */
	public void setAppContext( Object context ) throws OdaException
	{
	    // do nothing; assumes no support for pass-through context
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IConnection#close()
	 */
	public void close() throws OdaException
	{
		try {
			mainBundle.stop();
		} catch (BundleException e) {
			throw new OdaException("Problem stopping main application bundle: " + e);
		}
		huiTypeFactory = null;
	    isOpen = false;
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IConnection#isOpen()
	 */
	public boolean isOpen() throws OdaException
	{
		return isOpen;
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IConnection#getMetaData(java.lang.String)
	 */
	public IDataSetMetaData getMetaData( String dataSetType ) throws OdaException
	{
	    // assumes that this driver supports only one type of data set,
        // ignores the specified dataSetType
		return new DataSetMetaData( this );
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IConnection#newQuery(java.lang.String)
	 */
	public IQuery newQuery( String dataSetType ) throws OdaException
	{
        // assumes that this driver supports only one type of data set,
        // ignores the specified dataSetType
		return new Query(huiTypeFactory);
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IConnection#getMaxQueries()
	 */
	public int getMaxQueries() throws OdaException
	{
		return 0;	// no limit
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IConnection#commit()
	 */
	public void commit() throws OdaException
	{
	    // do nothing; assumes no transaction support needed
	}

	/*
	 * @see org.eclipse.datatools.connectivity.oda.IConnection#rollback()
	 */
	public void rollback() throws OdaException
	{
        // do nothing; assumes no transaction support needed
	}

    /* (non-Javadoc)
     * @see org.eclipse.datatools.connectivity.oda.IConnection#setLocale(com.ibm.icu.util.ULocale)
     */
    public void setLocale( ULocale locale ) throws OdaException
    {
        // do nothing; assumes no locale support
    }
    
}
