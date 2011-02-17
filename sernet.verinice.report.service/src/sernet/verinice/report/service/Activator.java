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
package sernet.verinice.report.service;

import org.apache.log4j.Logger;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.interfaces.oda.IVeriniceOdaDriver;

public class Activator implements BundleActivator {
	
    private final Logger log = Logger.getLogger(Activator.class);
    
    // The shared instance
    private static Activator plugin;
    
    private ServiceTracker commandServiceTracker;
    
    private ServiceTracker odaDriverTracker;
	
	public void start(BundleContext context) throws Exception {
		plugin = this;

		if (log.isInfoEnabled()) {
            final Bundle bundle = context.getBundle();
            log.info("Starting bundle " + bundle.getSymbolicName() + " " + bundle.getVersion());
        }
		
		// Reach ICommandService implementation via service tracker since the instance
		// is provided via Spring (and should not be instantiated by OSGi)
		commandServiceTracker = new ServiceTracker(context, ICommandService.class.getName(), null);
		commandServiceTracker.open();
		
		odaDriverTracker = new ServiceTracker(context, IVeriniceOdaDriver.class.getName(), null);
		odaDriverTracker.open();
	}

	public void stop(BundleContext context) throws Exception {
		commandServiceTracker.close();
		odaDriverTracker.close();
	}
	
	public static Activator getDefault()
	{
		return plugin;
	}

	public ICommandService getCommandService()
	{
		return (ICommandService) commandServiceTracker.getService();
	}
	
	public IVeriniceOdaDriver getOdaDriver()
	{
		return (IVeriniceOdaDriver) odaDriverTracker.getService();
	}
	
}
