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
package sernet.hui.common.multiselectionlist;



/**
 * an option to display in the multi selection list
 * @author prack
 */
public interface IMLPropertyOption {

	/**
	 * the display name of the option (i.e. "Brown")
	 * @return
	 */
	String getName();

	/**
	 * the internal id of the option (i.e. "colour3")
	 * @return
	 */
	String getId();
	
	/**
	 * Optional Context Menu to be displayed on right click.
	 * 
	 * @return swt.Listener interface that is called on MenuDetect Events.
	 */
	IContextMenuListener getContextMenuListener();

	/**
	 * Callback Handler to allow the option to notify its checkbox when the display name changes or similar.
	 * @param checkBoxHandler
	 */
	void setCheckboxHandler(ICheckBoxHandler checkBoxHandler);
	
	ICheckBoxHandler getCheckboxHandler();

}
