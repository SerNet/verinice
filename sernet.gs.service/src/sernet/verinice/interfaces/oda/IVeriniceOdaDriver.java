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
 ******************************************************************************/
package sernet.verinice.interfaces.oda;

import java.util.Map;

public interface IVeriniceOdaDriver {

	/** Name of the property which stores the root element's id. The id specifies the database id of the
	 * object that is being used for a report.
	 */
	public static final String ROOT_ELEMENT_ID_NAME = "rootElementId";
	
	/**
	 * Registers an image provider for a certain image name.
	 * 
	 * <p>The provider instance should be a generic and long-living object,
	 * registered as early as possible and stay available throughout the runtime
	 * of the application.</p>
	 * 
	 * @param name
	 * @param imageProvider
	 */
	void setImageProvider(String name, IImageProvider imageProvider);

	/**
	 * Unregisters an image provider.
	 * 
	 * @param name
	 */
	void removeImageProvider(String name);

	/**
	 * Makes the given map of keys and values available to the scripting environment
	 * of a report.
	 * 
	 * <p>The use of this way of providing information to a report is discouraged as it
	 * prevents the standalone use of the report from the report designer. If possible the
	 * report should retrieve the information on its own (if neccessary with the help of custom
	 * commands).</p>
	 * 
	 * @param Map
	 */
	void setScriptVariables(Map<String, Object> Map);
	
	Map<String, Object> getScriptVariables();
	
}
