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
 *     Sebastian Hagedorn <sh@sernet.de> - additions for report logging
 ******************************************************************************/
package sernet.verinice.oda.driver;

import java.util.Hashtable;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.service.url.URLConstants;
import org.osgi.service.url.URLStreamHandlerService;
import org.osgi.util.tracker.ServiceTracker;

import sernet.gs.service.ReportTemplateDirectoryService;
import sernet.hui.common.VeriniceContext;
import sernet.springclient.SpringClientPlugin;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.interfaces.ILogPathService;
import sernet.verinice.interfaces.IMain;
import sernet.verinice.interfaces.IReportLocalTemplateDirectoryService;
import sernet.verinice.oda.driver.impl.VeriniceURLStreamHandlerService;
import sernet.verinice.oda.driver.preferences.PreferenceConstants;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "sernet.verinice.oda.driver";

	// The shared instance
	private static Activator plugin;

	private VeriniceURLStreamHandlerService urlStreamHandlerService = new VeriniceURLStreamHandlerService();

	private ServiceTracker mainTracker;

	private ServiceTracker commandServiceTracker;

	private ServiceTracker logPathTracker;

	private IReportLocalTemplateDirectoryService templateDirService;

	private static final String WORK_OBJECTS = "workObjects";

	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
	 */
	public void start(final BundleContext context) throws Exception {
		super.start(context);
		plugin = this;

		Hashtable<String, String[]> properties = new Hashtable<String, String[]>();
		properties.put(URLConstants.URL_HANDLER_PROTOCOL, new String[] { "verinice" });

		context.registerService(URLStreamHandlerService.class.getName(), urlStreamHandlerService, properties);

		templateDirService = new ReportTemplateDirectoryService();
		context.registerService(IReportLocalTemplateDirectoryService.class.getName(), templateDirService, null);

		mainTracker = new ServiceTracker(context, IMain.class.getName(), null);
		mainTracker.open();

		commandServiceTracker = new ServiceTracker(context, ICommandService.class.getName(), null);
		commandServiceTracker.open();

		logPathTracker = new ServiceTracker(context, ILogPathService.class.getName(), null);
		logPathTracker.open();

	}

	@Override
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		plugin = null;
		mainTracker.close();
		commandServiceTracker.close();
		logPathTracker.close();
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	public VeriniceURLStreamHandlerService getURLStreamHandlerService() {
		return urlStreamHandlerService;
	}

	public IMain getMain() {
		return (IMain) mainTracker.getService();
	}

	public ICommandService getCommandService() {
		inheritVeriniceContext();
		if (getPreferenceStore().getBoolean(PreferenceConstants.REPORT_USE_CACHE)) {

			return (ICommandService) VeriniceContext.get(VeriniceContext.COMMAND_CACHE_SERVICE);
		} else {
			return (ICommandService) VeriniceContext.get(VeriniceContext.COMMAND_SERVICE);
		}
	}

	private void inheritVeriniceContext() {
		VeriniceContext.State state = (VeriniceContext.State) SpringClientPlugin.getDefault().getBeanFactory()
				.getBean(WORK_OBJECTS);
		VeriniceContext.setState(state);
	}

	public ILogPathService getLogPathService() {
		return (ILogPathService) logPathTracker.getService();
	}

	public IReportLocalTemplateDirectoryService getIReportTemplateDirectoryService() {
		return templateDirService;
	}
}
