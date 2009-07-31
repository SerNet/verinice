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

import java.net.MalformedURLException;

import org.apache.log4j.Logger;

import sernet.gs.ui.rcp.main.CnAWorkspace;
import sernet.springclient.SpringClientPlugin;

public abstract class ServiceFactory {

	private static final String AUTH_SERVICE = "authService";
	private static final String COMMAND_SERVICE = "commandService";

	public static final int LOCAL = 0;
	public static final int REMOTE = 1;

	private static int locality = LOCAL;

	public static void setService(int locality) {
		ServiceFactory.locality = locality;
	}

	public static boolean isUsingRemoteService() {
		return locality == REMOTE;
	}

	public static void openCommandService() throws MalformedURLException {
		SpringClientPlugin.getDefault().openBeanFactory(CnAWorkspace.getInstance().getApplicationContextLocation());
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
}
