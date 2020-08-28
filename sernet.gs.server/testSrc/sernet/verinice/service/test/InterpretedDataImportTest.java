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

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.junit.Assert;
import org.junit.Test;
import org.junit.matchers.StringContains;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import sernet.verinice.interfaces.CommandException;
import sernet.verinice.model.bp.SecurityLevel;
import sernet.verinice.model.bp.elements.Safeguard;
import sernet.verinice.service.commands.SyncCommand;
import sernet.verinice.service.commands.SyncParameter;
import sernet.verinice.service.commands.SyncParameterException;

@TransactionConfiguration(transactionManager = "txManager", defaultRollback = true)
@Transactional
public class InterpretedDataImportTest extends CommandServiceProvider {

    @Test
    public void importDataWithInvalidImplementationStatus()
            throws IOException, SyncParameterException {
        try (InputStream is = InterpretedDataImportTest.class.getResourceAsStream(
                "Informationsverbund-VN-2752-invalid-implementation-status.vna")) {
            byte[] bytes = IOUtils.toByteArray(is);
            SyncParameter parameter = new SyncParameter(true, false, false, false);
            SyncCommand syncCommand = new SyncCommand(parameter, bytes);
            try {
                commandService.executeCommand(syncCommand);
                Assert.fail("Import should have failed with an exception");
            } catch (CommandException e) {
                Throwable unwrapped = syncCommand.getErrorCause().getCause();
                Assert.assertThat(unwrapped.getMessage(), StringContains.containsString(
                        "Invalid value found for option property bp_requirement_implementation_status"));
            }
        }
    }

    @Test
    public void importDataWithInvalidProceeding() throws IOException, SyncParameterException {
        try (InputStream is = InterpretedDataImportTest.class
                .getResourceAsStream("Informationsverbund-VN-2752-invalid-proceeding.vna")) {
            byte[] bytes = IOUtils.toByteArray(is);
            SyncParameter parameter = new SyncParameter(true, false, false, false);
            SyncCommand syncCommand = new SyncCommand(parameter, bytes);
            try {
                commandService.executeCommand(syncCommand);
                Assert.fail("Import should have failed with an exception");
            } catch (CommandException e) {
                Throwable unwrapped = syncCommand.getErrorCause().getCause();
                Assert.assertThat(unwrapped.getMessage(), StringContains.containsString(
                        "Invalid value found for option property bp_itnetwork_qualifier"));
            }
        }
    }

    @Test
    public void importDataWithInvalidSecurityLevel() throws IOException, SyncParameterException {
        try (InputStream is = InterpretedDataImportTest.class
                .getResourceAsStream("Informationsverbund-VN-2752-invalid-security-level.vna")) {
            byte[] bytes = IOUtils.toByteArray(is);
            SyncParameter parameter = new SyncParameter(true, false, false, false);
            SyncCommand syncCommand = new SyncCommand(parameter, bytes);
            try {
                commandService.executeCommand(syncCommand);
                Assert.fail("Import should have failed with an exception");
            } catch (CommandException e) {
                Throwable unwrapped = syncCommand.getErrorCause().getCause();
                Assert.assertThat(unwrapped.getMessage(), StringContains.containsString(
                        "Invalid value found for option property bp_safeguard_qualifier"));
            }
        }
    }

    @Test
    public void importDataWithTranslatedSecurityLevel()
            throws IOException, SyncParameterException, CommandException {
        try (InputStream is = InterpretedDataImportTest.class
                .getResourceAsStream("Informationsverbund-VN-2752-translated-security-level.vna")) {
            byte[] bytes = IOUtils.toByteArray(is);
            SyncParameter parameter = new SyncParameter(true, false, false, false);
            SyncCommand syncCommand = new SyncCommand(parameter, bytes);
            commandService.executeCommand(syncCommand);
            List<Safeguard> safeguards = elementDao
                    .findByCriteria(DetachedCriteria.forClass(Safeguard.class));
            assertEquals(1, safeguards.size());
            assertEquals(SecurityLevel.HIGH, safeguards.get(0).getSecurityLevel());
        }
    }

}