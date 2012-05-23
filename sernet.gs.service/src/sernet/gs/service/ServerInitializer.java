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
package sernet.gs.service;

import org.apache.log4j.Logger;

import sernet.hui.common.VeriniceContext;
import sernet.verinice.interfaces.IHibernateCommandService;
/**
 * Initialize environemnt on Verinice server on startup.
 * 
 * @author koderman[at]sernet[dot]de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public class ServerInitializer {
	
	private final Logger log = Logger.getLogger(ServerInitializer.class);

	
	private static VeriniceContext.State state;
	
	private IHibernateCommandService hibernateCommandService;
	
	
	
	/**
	 * Initializes the current thread with the VeriniceContext.State
	 * of the client application.
	 * 
	 * <p>Calling this method is needed when the Activator was run on a
	 * different thread then the Application class.</p>
	 */
	public static void inheritVeriniceContextState()
	{
		VeriniceContext.setState(state);
	}

	public void initialize() {
		Logger.getLogger(this.getClass()).debug("Initializing server context...");
		// After this we can use the getInstance() methods from HitroUtil and
		// GSScraperUtil
		VeriniceContext.setState(state);
		
		// The work objects in the HibernateCommandService can only be set
		// at this point because otherwise we would have a circular dependency
		// in the Spring configuration (= commandService needs workObjects
		// and vice versa)
		hibernateCommandService.setWorkObjects(state);	
		
	}

	public void setWorkObjects(VeriniceContext.State workObjects) {
		ServerInitializer.state = workObjects;
	}

	public VeriniceContext.State getWorkObjects() {
		return state;
	}

	public void setHibernateCommandService(IHibernateCommandService hibernateCommandService) {
		this.hibernateCommandService = hibernateCommandService;
	}

	public IHibernateCommandService getHibernateCommandService() {
		return hibernateCommandService;
	}

}
