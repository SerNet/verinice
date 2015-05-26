/*******************************************************************************
 * Copyright (c) 2014 Benjamin Weißenfels.
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
 *     Benjamin Weißenfels <bw[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.service.test.helper.util;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.jgrapht.alg.BellmanFordShortestPath;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 * 
 */
public class BooleanCombinator {

    private Logger log = Logger.getLogger(BooleanCombinator.class);

    private int upperBound;

    private int exponent;

    // for junit
    public BooleanCombinator() {
    };

    public BooleanCombinator(int powerOfTwo) {
        this.exponent = powerOfTwo;
        this.upperBound = 2 ^ powerOfTwo;
    }

    private String getBinaryString(int x) {
        StringBuilder binaryNumber = new StringBuilder();

        if (x == 0) {
            return "0";
        }

        while (x > 0) {
            binaryNumber.insert(0, String.valueOf(x % 2));
            x = x / 2;
        }

        return binaryNumber.toString();
    }

    private List<String> createBinaryStringRepresentationList(int length) {

        List<String> binaryStrings = new LinkedList<String>();
        for (int i = 0; i < length; i++) {
            binaryStrings.add(getBinaryString(i));
        }

        return fillUpWithZero(binaryStrings, length);
    }

    private List<String> fillUpWithZero(List<String> binaryStrings, int length) {
        for (int i = 0; i < binaryStrings.size(); i++) {
            int y = (int) exponent - binaryStrings.get(i).length();

            for (; y > 0; y--) {
                String binaryNumber = binaryStrings.get(i);
                binaryStrings.remove(i);
                binaryStrings.add(i, "0" + binaryNumber);

            }

            if (log.isDebugEnabled())
                log.debug("fill up " + binaryStrings.get(i));
        }

        return binaryStrings;
    }

    private List<boolean[]> stringToBooleanRepresentation(List<String> binaryStrings) {

        List<boolean[]> binaryBooleanRepresentation = new ArrayList<boolean[]>(0);

        for (String binaryNumber : binaryStrings) {

            boolean[] binaryBoolean = new boolean[exponent];

            for (int i = 0; i < exponent; i++) {
                if (binaryNumber.toCharArray()[i] == '0') {
                    binaryBoolean[i] = false;
                } else {
                    binaryBoolean[i] = true;
                }
            }

            binaryBooleanRepresentation.add(binaryBoolean);
        }

        return binaryBooleanRepresentation;
    }

    public List<boolean[]> getBooleanList() {
        List<String> binarList = createBinaryStringRepresentationList(exponent);
        return stringToBooleanRepresentation(binarList);
    }

    @Test
    public void testBinaryStringLength() {

        List<boolean[]> booleanArrayList = new BooleanCombinator(3).getBooleanList();
        
        for (boolean [] binaryNumber : booleanArrayList)
        {
            Assert.assertEquals(3, binaryNumber.length);
        }
    }

    @Test
    public void testBinaryString() {
        Assert.assertEquals("110", getBinaryString(6));
        Assert.assertEquals("111", getBinaryString(7));
        Assert.assertEquals("1000", getBinaryString(8));
        Assert.assertEquals("1", getBinaryString(1));
        Assert.assertEquals("0", getBinaryString(0));
    }
}
