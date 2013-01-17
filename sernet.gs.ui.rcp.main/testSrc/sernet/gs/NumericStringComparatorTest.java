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
package sernet.gs;

import junit.framework.TestCase;
import sernet.gs.service.NumericStringComparator;


/**
 * @author Daniel <dm[at]sernet[dot]de>
 *
 */
public class NumericStringComparatorTest extends TestCase{
	
	NumericStringComparator comparator = new NumericStringComparator();

	public void testCompare() {
		String a = "2";
		String b = "2.1";
		aLtB(a, b);
		
		a = "5";
		b = "2.1";
		aGtB(a, b);
		
		a = "1";
		b = "2.1";
		aLtB(a, b);
		
		a = "2";
		b = "2.a";
		aLtB(a, b);
		
		a = "A";
		b = "2";
		aGtB(a, b);
		
		a = "A";
		b = "2.1";
		aGtB(a, b);
		
		
		a = "A";
		b = "A.1";
		aLtB(a, b);
		
		a = "2.1";
		b = "2.2";
		aLtB(a, b);
		
		a = "2.1";
		b = "2.1";
		aEqB(a, b);
		
		a = "3.b.4";
		b = "3.c.4";
		aLtB(a, b);
		
		a = "2.4.a";
		b = "2.4.b";
		aLtB(a, b);
		
		a = "10.4876.B";
		b = "10.4876.B";
		aEqB(a, b);
		
		a = "10b";
		b = "10a";
		aGtB(a, b);
		
		a = "2.01";
		b = "2.1";
		aGtB(a, b);
		
		a = "2.01";
		b = "2.6";
		aLtB(a, b);
		
		a = "234.0002.1";
		b = "234.2.1";
		aGtB(a, b);
		
		a = "2.01";
		b = "2.11";
		aLtB(a, b);
		
	}
	
	private void aGtB(String a, String b) {
		int result = comparator.compare(a, b);
		assertEquals(a + " is greater than " + b + " (" + result + ")", true, result>0);
		result = comparator.compare(b, a);
		assertEquals(b + " is less than " + a + " (" + result + ")", true, result<0);
	}
	
	private void aLtB(String a, String b) {
		int result = comparator.compare(a, b);
		assertEquals(a + " is less than " + b + " (" + result + ")", true, result<0);
		result = comparator.compare(b, a);
		assertEquals(b + " is graeter than " + a + " (" + result + ")", true, result>0);
	}
	
	private void aEqB(String a, String b) {
		int result = comparator.compare(a, b);
		assertEquals(a + " is equal " + b + " (" + result + ")", true, result==0);
		result = comparator.compare(b, a);
		assertEquals(a + " is equal " + b + " (" + result + ")", true, result==0);
	}
	
}
