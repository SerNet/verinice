/*******************************************************************************
 * Copyright (c) 2009 Daniel Murygin <dm[at]sernet[dot]de>.
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
 *     Daniel <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.web;


/**
 * @author Daniel <dm[at]sernet[dot]de>
 *
 */
public class ExceptionHandler {

	/**
	 * @param e
	 */
	public static void handle(Throwable e) {
		if(e instanceof sernet.gs.web.SecurityException) {
			Util.addError("submit", Util.getMessage("todo.save.forbidden"));
		} else if(e.getCause()!=null && e.getCause() instanceof sernet.gs.service.SecurityException) {
			Util.addError("submit", Util.getMessage("todo.save.forbidden"));	
		} else {
			Util.addError("submit", Util.getMessage("todo.save.failed"));
		}
	}

}
