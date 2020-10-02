/*******************************************************************************
 * Copyright (c) 2018 Daniel Murygin.
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
 ******************************************************************************/
package sernet.gs.service;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Test class for sernet.gs.service.StringUtil
 */
public class StringUtilTest {

    /**
     * Test method for {@link sernet.gs.service.StringUtil#replaceEmptyStringByNull(java.lang.String)}.
     */
    @Test
    public void testReplaceEmptyStringByNull() {
        testReplaceEmptyStringByNull("", null);
        testReplaceEmptyStringByNull(null, null);
        String loremIpsum = "Lorem ipsum dolor sit amet, consectetur adipisici elit, sed eiusmod tempor incidunt ut labore et dolore magna aliqua.";
        testReplaceEmptyStringByNull(loremIpsum, loremIpsum);
    }

    private void testReplaceEmptyStringByNull(String input, String expected) {
        assertEquals(expected, StringUtil.replaceEmptyStringByNull(input));
    }

}
