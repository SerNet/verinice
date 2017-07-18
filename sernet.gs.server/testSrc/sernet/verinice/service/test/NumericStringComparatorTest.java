/*******************************************************************************
 * Copyright (c) 2016 Daniel Murygin <dm{a}sernet{dot}de>.
 *
 * This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     Daniel Murygin <dm{a}sernet{dot}de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.service.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import sernet.gs.service.NumericStringComparator;

/**
 * Test for class NumericStringComparator.
 * 
 * From the Javadoc of java.util.Comparator:
 * "Returns a negative integer,
 * zero, or a positive integer as the first argument is less than, equal
 * to, or greater than the second."
 * 
 * -1: first is less than second
 *  0: first is equal second
 *  1: first is greater than second
 *  
 *  English translation for "deutsche Umlaute" is "German umlaut": http://www.dict.cc/?s=umlaut
 *
 * @author Daniel Murygin <dm{a}sernet{dot}de>
 */
public class NumericStringComparatorTest {

    private static final NumericStringComparator NSC = new NumericStringComparator();
    
    private static final int FIRST_IS_LESS_THAN_SECOND = -1;
    
    @Test
    public void testCompareGermanUmlautO() {
        final String startsWithUmlautO = "öffi";
        final String startsWithP = "Peter";
        final String startsWithO = "Oben";
        isFirstLessThanSecond(startsWithUmlautO, startsWithP);
        isFirstLessThanSecond(startsWithO, startsWithUmlautO);
    }
    
    @Test
    public void testCompareGermanUmlautA() {      
        final String startsWithUmlautA = "Ähem";
        final String startsWithB = "Beer";
        final String startsWithA = "Adele";
        isFirstLessThanSecond(startsWithUmlautA, startsWithB);
        isFirstLessThanSecond(startsWithA, startsWithUmlautA);
    }
     
    @Test
    public void testCompareGermanUmlautU() {       
        final String startsWithUmlautU = "über";
        final String startsWithV = "verinice";
        final  String startsWithU = "Uber";
        isFirstLessThanSecond(startsWithUmlautU, startsWithV);
        isFirstLessThanSecond(startsWithU, startsWithUmlautU);
    }
    
    @Test
    public void testCompareMueller() {       
        final String mueller = "Mueller";
        final String muellerWithUmlautU = "Müller";
        isFirstLessThanSecond(mueller, muellerWithUmlautU);
    }
    
    /**
     * See https://de.wikipedia.org/wiki/Alphabetische_Sortierung#Deutschland
     * to learn what DIN5007 Var.1 means.
     */
    @Test
    public void testCompareDIN5007Var1() {
        final String goebel = "Göbel";
        final String goethe = "Goethe";
        final String goetheWithUmlautO = "Göthe";
        final String goldmann = "Goldmann";
        final String goetz = "Götz";
        isFirstLessThanSecond(goebel, goethe);
        isFirstLessThanSecond(goldmann, goetheWithUmlautO);
        isFirstLessThanSecond(goethe, goldmann);
        isFirstLessThanSecond(goetheWithUmlautO, goetz);
    }
       
    @Test
    public void testCompareGermanUmlautS() {
        final String startsWithUmlautS = "ßuper";
        final String startsWithT = "Tor";
        final String startsWithS = "Salat";
        isFirstLessThanSecond(startsWithUmlautS, startsWithT);
        isFirstLessThanSecond(startsWithS,  startsWithUmlautS);
    }
    
    @Test
    public void testStringStartsWithNumber() {
        final String STARTS_with_1_0 = "1.0 Topic A";
        final String STARTS_with_1_1 = "1.1 Topic B";
        isFirstLessThanSecond(STARTS_with_1_0, STARTS_with_1_1);
        
        final String STARTS_with_9_0 = "9.0 Lorem";
        final String STARTS_with_10_0 = "10.0 Ipsum";
        isFirstLessThanSecond(STARTS_with_9_0, STARTS_with_10_0);
    }
    
    @Test
    public void testStringWithNumberAndUmlauts() {
        final String STARTS_with_1_0 = "1.0 Göbel";
        final String STARTS_with_1_1 = "1.0 Goethe";
        isFirstLessThanSecond(STARTS_with_1_0, STARTS_with_1_1);
        
        final String STARTS_with_9_0 = "9.0 Lorem";
        final String STARTS_with_10_0 = "10.0 Ipsum";
        isFirstLessThanSecond(STARTS_with_9_0, STARTS_with_10_0);
    }
    
    @Test
    public void testNumbers() {
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
        int result = NSC.compare(a, b);
        assertEquals(a + " is greater than " + b + " (" + result + ")", true, result>0);
        result = NSC.compare(b, a);
        assertEquals(b + " is less than " + a + " (" + result + ")", true, result<0);
    }
    
    private void aLtB(String a, String b) {
        int result = NSC.compare(a, b);
        assertEquals(a + " is less than " + b + " (" + result + ")", true, result<0);
        result = NSC.compare(b, a);
        assertEquals(b + " is graeter than " + a + " (" + result + ")", true, result>0);
    }
    
    private void aEqB(String a, String b) {
        int result = NSC.compare(a, b);
        assertEquals(a + " is equal " + b + " (" + result + ")", true, result==0);
        result = NSC.compare(b, a);
        assertEquals(a + " is equal " + b + " (" + result + ")", true, result==0);
    }

    protected void isFirstLessThanSecond(String startsWithUmlautO, String startsWithZ) {
        assertTrue(startsWithUmlautO + " is not less than " + startsWithZ, 
                FIRST_IS_LESS_THAN_SECOND >= NSC.compare(startsWithUmlautO, startsWithZ));
    }

}
