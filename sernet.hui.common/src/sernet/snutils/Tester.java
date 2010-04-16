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

import org.apache.log4j.Logger;


/**
 * @author prack
 * 
 * @version $Id: Tester.java,v 1.2 2006/01/03 17:01:03 aprack Exp $
 */
public class Tester {

	public static void assertTrue(String message, boolean statement) throws AssertException {
		if (! statement) {
			Logger.getLogger(Tester.class).error("Assertion failed:" + message); //$NON-NLS-1$
			throw new AssertException(message);
		}
	}
	
	public static void assertEquals(String message, String string1, String string2) 
	throws AssertException {
		if (! string1.equals(string2)) {
			Logger.getLogger(Tester.class)
			.error("Assertion failed, not equal: " + string1 + " : " + string2); //$NON-NLS-1$ //$NON-NLS-2$
			throw new AssertException(message);
		}
	}
	
}


/*
 * $Log: Tester.java,v $
 * Revision 1.2  2006/01/03 17:01:03  aprack
 * *** empty log message ***
 *
 * Revision 1.1  2005/11/14 10:17:52  aprack
 * - new project for common tasks
 *
 * Revision 1.2  2005/04/13 14:53:41  aprack
 * - imports
 * - number format for phone item
 *
 * Revision 1.1  2004/05/25 14:33:31  aprack
 * - gui features: new customer, load customer,
 *   update tree display, select item
 * - switched to eclipse 3.0 (full commit)
 *
 */
