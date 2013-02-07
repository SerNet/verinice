/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak[at]sernet[dot]de>.
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
 *     Alexander Koderman <ak[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.service;

import java.net.MalformedURLException;

import org.apache.log4j.Logger;

import sernet.hui.common.VeriniceContext;
import sernet.springclient.SpringClientPlugin;
import sernet.verinice.interfaces.IAuthService;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.interfaces.bpm.IGsmService;
import sernet.verinice.interfaces.bpm.IIndividualService;
import sernet.verinice.interfaces.bpm.IIsaControlFlowService;
import sernet.verinice.interfaces.bpm.IIsaQmService;
import sernet.verinice.interfaces.bpm.IProcessServiceIsa;
import sernet.verinice.interfaces.bpm.ITaskService;
import sernet.verinice.interfaces.validation.IValidationService;

@SuppressWarnings("restriction")
public abstract class ServiceFactory {
	
	private static final String WORK_OBJECTS = "workObjects";
	private static Boolean permissionHandlingNeeded = null;

	public static void openCommandService() throws MalformedURLException {
		SpringClientPlugin.getDefault().openBeanFactory();
	}

	public static void closeCommandService() {
		Logger.getLogger(ServiceFactory.class).debug("Closing bean factory.");
		SpringClientPlugin.getDefault().closeBeanFactory();
	}

	/** Retrieves the application's ICommandService instance.
	 * 
	 * <p>The method works on the server as well as the client.</p>
	 * 
	 * <p>Note: Usage of this method is discouraged. If the class that needs
	 * the command service is managed by Spring declare a property and
	 * let the instance being injected by the IoC container.</p> 
	 * 
	 * @return
	 */
	public static ICommandService lookupCommandService() {
		return (ICommandService) VeriniceContext.get(VeriniceContext.COMMAND_SERVICE);
	}
	
	/** Retrieves the client's IAuthService instance.
	 * 
	 * <p>The method works only on the client.</p>
	 * 
	 * <p>Note: Usage of this method is discouraged. If the class that needs
	 * the auth service is managed by Spring declare a property and
	 * let the instance being injected by the IoC container.</p> 
	 * 
	 * @return
	 */
	public static IAuthService lookupAuthService() {
		// Cache result of permission handling config for the configured service.
		// Otherwise, the server was queried on every call to determine this:
		
		IAuthService authService = (IAuthService) VeriniceContext.get(VeriniceContext.AUTH_SERVICE);
		
		if (permissionHandlingNeeded == null){
			permissionHandlingNeeded = authService.isPermissionHandlingNeeded();
		}
		return authService;
	}
	
	/** Retrieves the application's ITaskService instance.
     * 
     * <p>The method works on the server as well as the client.</p>
     * 
     * <p>Note: Usage of this method is discouraged. If the class that needs
     * the command service is managed by Spring declare a property and
     * let the instance being injected by the IoC container.</p> 
     * 
     * @return
     */
    public static ITaskService lookupTaskService() {
        return (ITaskService) VeriniceContext.get(VeriniceContext.TASK_SERVICE);
    }
    
    /** Retrieves the application's IProcessServiceIsa instance.
     * 
     * <p>The method works on the server as well as the client.</p>
     * 
     * <p>Note: Usage of this method is discouraged. If the class that needs
     * the command service is managed by Spring declare a property and
     * let the instance being injected by the IoC container.</p> 
     * 
     * @return
     */
    public static IProcessServiceIsa lookupProcessServiceIsa() {
        return (IProcessServiceIsa) VeriniceContext.get(VeriniceContext.PROCESS_SERVICE_ISA);
    }
    
    public static IIsaControlFlowService lookupIsaControlFlowService() {
        return (IIsaControlFlowService) VeriniceContext.get(VeriniceContext.ISA_CONTROL_FLOW_SERVICE);
    }
    
    public static IIsaQmService lookupQmService() {
        return (IIsaQmService) VeriniceContext.get(VeriniceContext.ISA_QM_SERVICE);
    }
    
    public static IIndividualService lookupIndividualService() {
        return (IIndividualService) VeriniceContext.get(VeriniceContext.INDIVIDUAL_SERVICE);
    }
    
    public static IGsmService lookupGsmService() {
        return (IGsmService) VeriniceContext.get(VeriniceContext.GSM_SERVICE);
    }
	
	public static boolean isPermissionHandlingNeeded() {
		if (permissionHandlingNeeded == null){
			return true; /* return true, just to be safe */
		}
		return permissionHandlingNeeded;
	}
	
	public static IValidationService lookupValidationService(){
	    return (IValidationService)VeriniceContext.get(VeriniceContext.VALIDATION_SERVICE);
	}
	
	/**
	 * Retrieves the work objects that have been configured for the client.
	 * 
	 * <p>This method must only be used from the applications' <code>Activator</code>
	 * in order to retrieve the initial values for the {@link VeriniceContext} class.</p>
	 */
	public static VeriniceContext.State getClientWorkObjects()
	{
		return (VeriniceContext.State) SpringClientPlugin.getDefault()
				.getBeanFactory()
				.getBean(WORK_OBJECTS);
	}
	
}
