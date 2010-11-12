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
package sernet.verinice.interfaces;

@SuppressWarnings("serial")
public abstract class GenericCommand implements ICommand {
	
	
	private transient IDAOFactory daoFactory;
	private transient ICommandService commandService;
	

	public void setCommandService(ICommandService service) {
		this.commandService = service;
	}
	
	public void setDaoFactory(IDAOFactory daoFactory) {
		this.daoFactory = daoFactory;
	}

	public IDAOFactory getDaoFactory() {
		return daoFactory;
	}
	
	public ICommandService getCommandService() {
		return commandService;
	}
	
	public void clear() {
		// default implementation does nothing
	}


}
