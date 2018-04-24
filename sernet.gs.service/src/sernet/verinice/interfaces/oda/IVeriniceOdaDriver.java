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

public interface IVeriniceOdaDriver {

	/** Name of the property which stores the root element's id. The id specifies the database id of the
	 * object that is being used for a report.
	 */
	public static final String ROOT_ELEMENT_ID_NAME = "rootElementId";
	
	/** Name of the property which stores the root element's ids. The ids specify the database ids of the
	 * objects that are being used for a report that compares some organziations.
	 */
	public static final String ROOT_ELEMENT_IDS_NAME = "rootElementIds";

	boolean getReportLoggingState();

	String getLogLvl();

	String getLogFile();

	String getLocalReportLocation();

	boolean isSandboxEnabled();
}
