/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak@sernet.de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details.
 *     You should have received a copy of the GNU General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Alexander Koderman <ak@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.springclient;

import org.apache.log4j.Logger;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.access.BeanFactoryLocator;
import org.springframework.beans.factory.access.BeanFactoryReference;
import org.springframework.beans.factory.access.SingletonBeanFactoryLocator;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

/**
 * The main plugin class to be used in the desktop.
 */
public class SpringClientPlugin extends AbstractUIPlugin {
	private BeanFactory beanFactory;

	private BeanFactoryReference beanFactoryReference;
	
	//The shared instance.
	private static SpringClientPlugin plugin;
	
	/**
	 * The constructor.
	 */
	public SpringClientPlugin() {
		plugin = this;
	}

	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		plugin = null;
	}

	/**
	 * Returns the shared instance.
	 */
	public static SpringClientPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path.
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return AbstractUIPlugin.imageDescriptorFromPlugin("SpringClient", path);
	}
	
	public synchronized void closeBeanFactory() {
		if (beanFactoryReference != null) {
//			try {
//			Scheduler scheduler = (Scheduler) beanFactory.getBean("quartzSchedulerFactory");
//				scheduler.shutdown(false);
//			} catch (Exception e) {
//				Logger.getLogger(this.getClass()).error(e);
//			}
			AbstractApplicationContext ctx = (AbstractApplicationContext) beanFactory;
			ctx.close();
			beanFactoryReference.release();
			beanFactory = null;
		}
	}
	
	public synchronized BeanFactory getBeanFactory() {
		return beanFactory;
	}

	public synchronized void openBeanFactory(String beanFactoryUrl, String context) {
		if (beanFactory == null) {
			
			BeanFactoryLocator beanFactoryLocator = SingletonBeanFactoryLocator.getInstance(beanFactoryUrl);
			beanFactoryReference = beanFactoryLocator.useBeanFactory(context);
			beanFactory = beanFactoryReference.getFactory();

		}
	}	
}
