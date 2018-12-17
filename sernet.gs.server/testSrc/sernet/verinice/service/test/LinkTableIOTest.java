/*******************************************************************************
 * Copyright (c) 2018 Urs Zeidler.
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
 *     Urs Zeidler uz[at]sernet.de - initial API and implementation
 ******************************************************************************/
package sernet.verinice.service.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Test;

import sernet.verinice.service.linktable.ILinkTableConfiguration;
import sernet.verinice.service.linktable.LinkTableConfiguration;
import sernet.verinice.service.linktable.vlt.VeriniceLinkTableIO;

/**
 * Test the serialization of the flag 'followLinksOutsideOfScope'.
 * 
 * @author uz[at]sernet.de
 *
 */
public class LinkTableIOTest {
    
    @Test
    public void testBasicIO_Flag_Set() throws IOException {
        ILinkTableConfiguration configuration = new LinkTableConfiguration.Builder().setFollowLinksOutsideOfScope(true).build();
        Path tempFile = Files.createTempFile("test1", ".vlt");
        String fullPath = tempFile.toAbsolutePath().toString();
        VeriniceLinkTableIO.write(configuration, fullPath);
        
        ILinkTableConfiguration configuration2 = VeriniceLinkTableIO.readLinkTableConfiguration(fullPath);
        assertTrue(configuration2.followLinksOutsideOfScope());
        assertEquals(configuration, configuration2);
    }

    @Test
    public void testBasicIO() throws IOException {
        ILinkTableConfiguration configuration = new LinkTableConfiguration.Builder().build();
        Path tempFile = Files.createTempFile("test1", ".vlt");
        String fullPath = tempFile.toAbsolutePath().toString();
        VeriniceLinkTableIO.write(configuration, fullPath);
        
        ILinkTableConfiguration configuration2 = VeriniceLinkTableIO.readLinkTableConfiguration(fullPath);
        assertFalse(configuration2.followLinksOutsideOfScope());
        assertEquals(configuration, configuration2);
    }

}
