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
package sernet.snutils;

/**
 * @author asernet.deprack
 * 
 * @version $Id: AssertException.java,v 1.1 2005/11/14 10:17:52 aprack Exp $
 */
public class AssertException extends Exception {

	public AssertException(String message) {
		super(message);
		
	}
}


/*
 * $Log: AssertException.java,v $
 * Revision 1.1  2005/11/14 10:17:52  aprack
 * - new project for common tasks
 *
 * Revision 1.3  2005/03/01 19:21:40  aprack
 * - some work on telephone menu
 * - bug fix: changed adr not saved when clicking on differnt item
 *
 * Revision 1.2  2004/08/04 15:50:30  aprack
 * - save and delete for customer and contact
 * - started moving methods from gui class to helper
 * - assert exception separated from snkdb exception
 * - items no longer move to bottom of tree when edited
 * - form fields now properly reset when aborting changes
 * - output sorted by id
 *
 * Revision 1.1  2004/05/25 14:33:31  aprack
 * - gui features: new customer, load customer,
 *   update tree display, select item
 * - switched to eclipse 3.0 (full commit)
 *
 */
