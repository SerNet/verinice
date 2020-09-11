/*******************************************************************************
 * Copyright (c) 2019 Jochen Kemnade.
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
package sernet.verinice.service.test;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.matchers.StringContains;

import sernet.verinice.interfaces.CommandException;
import sernet.verinice.service.commands.SyncCommand;
import sernet.verinice.service.commands.SyncParameter;
import sernet.verinice.service.commands.SyncParameterException;
import sernet.verinice.service.sync.VeriniceArchiveNotValidException;

public class ImportPathTraversalTest extends AbstractModernizedBaseProtection {

    @Test
    public void path_traversal_in_attachment_filename()
            throws SyncParameterException, IOException, CommandException {
        try (InputStream is = ImportPathTraversalTest.class
                .getResourceAsStream("Informationsverbund_VN-2795.vna")) {
            byte[] bytes = IOUtils.toByteArray(is);
            SyncParameter parameter = new SyncParameter(true, false, false, false);
            SyncCommand syncCommand = new SyncCommand(parameter, bytes);
            try {
                commandService.executeCommand(syncCommand);
                Assert.fail("Import should have failed with an exception");
            } catch (VeriniceArchiveNotValidException e) {
                Throwable unwrapped = syncCommand.getErrorCause();
                Assert.assertThat(unwrapped.getMessage(),
                        StringContains.containsString("VNA file is corrupt"));
            }
        }
    }

}
