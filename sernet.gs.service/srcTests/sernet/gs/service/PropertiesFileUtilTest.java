/*******************************************************************************
 * Copyright (c) 2020 Alexander Ben Nasrallah.
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

import java.io.File;
import java.util.Locale;

import org.junit.Test;

public class PropertiesFileUtilTest {

    @Test
    public void testGetPropertiesFileName() {
        assertEquals(new File("/tmp/test.properties"), PropertiesFileUtil
                .getPropertiesFile(new File("/tmp/test.rtpdesign"), Locale.ENGLISH));
        assertEquals(new File("/tmp/test_de.properties"), PropertiesFileUtil
                .getPropertiesFile(new File("/tmp/test_de.rtpdesign"), Locale.ENGLISH));

        assertEquals(new File("/tmp/test_de.properties"), PropertiesFileUtil
                .getPropertiesFile(new File("/tmp/test.rtpdesign"), Locale.GERMANY));
        assertEquals(new File("/tmp/test_de_de.properties"), PropertiesFileUtil
                .getPropertiesFile(new File("/tmp/test_de.rtpdesign"), Locale.GERMANY));

        assertEquals(new File("/tmp/test__de.properties"), PropertiesFileUtil
                .getPropertiesFile(new File("/tmp/test_.rtpdesign"), Locale.GERMANY));
        assertEquals(new File("/tmp/test_de__de.properties"), PropertiesFileUtil
                .getPropertiesFile(new File("/tmp/test_de_.rtpdesign"), Locale.GERMANY));
    }
}
