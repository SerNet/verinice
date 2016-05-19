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

import static org.junit.Assert.*;

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
        String startsWithUmlautO = "öffi";
        String startsWithP = "Peter";
        String startsWithO = "Oben";
        isFirstLessThanSecond(startsWithUmlautO, startsWithP);
        isFirstLessThanSecond(startsWithO, startsWithUmlautO);
    }
    
    @Test
    public void testCompareGermanUmlautA() {      
        String startsWithUmlautA = "Ähem";
        String startsWithB = "Beer";
        String startsWithA = "Adele";
        isFirstLessThanSecond(startsWithUmlautA, startsWithB);
        isFirstLessThanSecond(startsWithA, startsWithUmlautA);
    }
     
    @Test
    public void testCompareGermanUmlautU() {       
        String startsWithUmlautU = "über";
        String startsWithV = "verinice";
        String startsWithU = "Uber";
        isFirstLessThanSecond(startsWithUmlautU, startsWithV);
        isFirstLessThanSecond(startsWithU, startsWithUmlautU);
    }
    
    @Test
    public void testCompareMueller() {       
        String mueller = "Mueller";
        String muellerWithUmlautU = "Müller";
        isFirstLessThanSecond(mueller, muellerWithUmlautU);
    }
    
    /**
     * See https://de.wikipedia.org/wiki/Alphabetische_Sortierung#Deutschland
     * to learn what DIN5007 Var.1 means.
     */
    @Test
    public void testCompareDIN5007Var1() {
        isFirstLessThanSecond("Göbel", "Goethe");
        isFirstLessThanSecond("Goldmann", "Göthe");
        isFirstLessThanSecond("Goethe", "Goldmann");
        isFirstLessThanSecond("Göthe", "Götz");
    }
       
    @Test
    public void testCompareGermanUmlautS() {
        String startsWithUmlautS = "ßuper";
        String startsWithT = "Tor";
        String startsWithS = "Salat";
        isFirstLessThanSecond(startsWithUmlautS, startsWithT);
        isFirstLessThanSecond(startsWithS,  startsWithUmlautS);
    }
    
    @Test
    public void testStringStartsWithNumber() {
        String STARTS_with_1_0 = "1.0 Topic A";
        String STARTS_with_1_1 = "1.1 Topic B";
        isFirstLessThanSecond(STARTS_with_1_0, STARTS_with_1_1);
        
        String STARTS_with_9_0 = "9.0 Lorem";
        String STARTS_with_10_0 = "10.0 Ipsum";
        isFirstLessThanSecond(STARTS_with_9_0, STARTS_with_10_0);
    }
    
    @Test
    public void testStringWithNumberAndUmlauts() {
        String STARTS_with_1_0 = "1.0 Göbel";
        String STARTS_with_1_1 = "1.0 Goethe";
        isFirstLessThanSecond(STARTS_with_1_0, STARTS_with_1_1);
        
        String STARTS_with_9_0 = "9.0 Lorem";
        String STARTS_with_10_0 = "10.0 Ipsum";
        isFirstLessThanSecond(STARTS_with_9_0, STARTS_with_10_0);
    }

    protected void isFirstLessThanSecond(String startsWithUmlautO, String startsWithZ) {
        assertTrue(startsWithUmlautO + " is not less than " + startsWithZ, 
                FIRST_IS_LESS_THAN_SECOND >= NSC.compare(startsWithUmlautO, startsWithZ));
    }

}
