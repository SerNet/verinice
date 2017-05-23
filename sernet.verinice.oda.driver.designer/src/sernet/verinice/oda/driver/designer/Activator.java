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
package sernet.verinice.oda.driver.designer;

import org.apache.log4j.Logger;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.service.auth.KerberosStatusService;
import sernet.verinice.service.model.IObjectModelService;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

    private final Logger log = Logger.getLogger(Activator.class);

    
	// The plug-in ID
	public static final String PLUGIN_ID = "sernet.verinice.oda.driver.designer";

	// The shared instance
	private static Activator plugin;

    private ServiceTracker<ICommandService, ICommandService> commandServiceTracker;


    private ServiceTracker<IObjectModelService, IObjectModelService> objectModelServiceTracker;
	
	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		if (log.isInfoEnabled()) {
            final Bundle bundle = context.getBundle();
            log.info("Starting bundle " + bundle.getSymbolicName() + " " + bundle.getVersion());
        }
//		ServiceFactory.getInstance().openBeanFactory(context);
		
		
		
		  commandServiceTracker = new ServiceTracker<ICommandService,ICommandService>(context, ICommandService.class.getName(), null);
		  if(commandServiceTracker != null){
		      commandServiceTracker.open();
		  }
		  
          objectModelServiceTracker = new ServiceTracker<IObjectModelService,IObjectModelService>(context, IObjectModelService.class.getName(), null);
          if(objectModelServiceTracker != null){
              objectModelServiceTracker.open();
          }
		  
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);

        if(commandServiceTracker != null){
            commandServiceTracker.close();
        }
        if(objectModelServiceTracker != null){
            objectModelServiceTracker.close();
        }

	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

    public ICommandService getCommandService() {
        return commandServiceTracker.getService();
    }

    public IObjectModelService getObjectModelService() {
        return objectModelServiceTracker.getService();
    }
}
