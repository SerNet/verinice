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

/**
 * Service to execute commands. 
 * 
 * The command's state after execution is returned by this method.
 * 
 * This is important if results in the command are expected by the executing code:
 * when remotely executed, the results will only be present in the returned command,
 * not in the parameter!
 * 
 * For local execution both reference the same object, but to keep the transparency between
 * local and remote execution you are required to use the returned object for further processing. 
 * 
 * @see ICommand
 * 
 * @author koderman[at]sernet[dot]de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public interface ICommandService {
	public  <T extends ICommand> T executeCommand(T command) throws CommandException;

	public void discardRoleMap();
}
