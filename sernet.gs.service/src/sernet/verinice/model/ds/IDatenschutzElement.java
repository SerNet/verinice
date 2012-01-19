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
package sernet.verinice.model.ds;

/**
 * Marker interface for Datenschutz specific classes.
 * 
 * @author koderman[at]sernet[dot]de
 *
 */
public interface IDatenschutzElement {
	
	
	// use new "_link" types instead:
	@Deprecated
	String P_ABTEILUNG_OLD = "vs_abteilung";
	@Deprecated
	String P_FACHLICHVERANTWORTLICHER_OLD = "vs_fachl";
	@Deprecated
	String P_ITVERANTWORTLICHER_OLD = "vs_it";
	
	

}
