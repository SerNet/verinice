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
package sernet.gs.ui.rcp.main.service;

import java.io.File;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.BeanFactory;

import sernet.gs.ui.rcp.main.CnAWorkspace;
import sernet.springclient.SpringClientPlugin;

public abstract class ServiceFactory {

	private static final String AUTH_SERVICE = "authService";
	private static final String COMMAND_SERVICE = "commandService";
	
	private static final String BEAN_REF_FACTORY = "beanRefFactory.xml";
	private static final String CONTEXT_LOCAL = "ctxHibernate";
	private static final String CONTEXT_REMOTE = "ctxRemote";

	public static final int LOCAL = 0;
	public static final int REMOTE = 1;

	private static int locality = LOCAL;

	public static void setService(int locality) {
		ServiceFactory.locality = locality;
	}

	public static boolean isUsingRemoteService() {
		return locality == REMOTE;
	}

	public static void openCommandService() {
		if (locality == LOCAL)
			openLocalServiceFactory();
		else
			openRemoteServiceFactory();
	}

	public static void closeCommandService() {
		Logger.getLogger(ServiceFactory.class).debug("Closing bean factory.");
		SpringClientPlugin.getDefault().closeBeanFactory();
	}

	public static ICommandService lookupCommandService() {
		
		return (ICommandService) SpringClientPlugin.getDefault()
				.getBeanFactory().getBean(COMMAND_SERVICE);
	}
	
	public static IAuthService lookupAuthService() {
		IAuthService authService = (IAuthService) SpringClientPlugin.getDefault()
			.getBeanFactory().getBean(AUTH_SERVICE);
		return authService;
	}

	private static void openRemoteServiceFactory() {
		String path = "file://" + CnAWorkspace.getInstance().getConfDir()
				+ File.separator + BEAN_REF_FACTORY;
		Logger.getLogger(ServiceFactory.class).debug(
				"Creating remote bean factory from " + path);
		SpringClientPlugin.getDefault().openBeanFactory(path, CONTEXT_REMOTE);
	}

	private static void openLocalServiceFactory() {
		String path = "file://" + CnAWorkspace.getInstance().getConfDir()
				+ File.separator + BEAN_REF_FACTORY;
		Logger.getLogger(ServiceFactory.class).debug(
				"Creating local bean factory from " + path);
		SpringClientPlugin.getDefault().openBeanFactory(path, CONTEXT_LOCAL);
	}

}
