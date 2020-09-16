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
import org.junit.Test;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import sernet.verinice.interfaces.CommandException;
import sernet.verinice.service.commands.SyncCommand;
import sernet.verinice.service.commands.SyncParameter;
import sernet.verinice.service.commands.SyncParameterException;

@TransactionConfiguration(transactionManager = "txManager")
@Transactional
public class ImportPerformanceTest extends CommandServiceProvider {

    @Test
    public void importModplast() throws IOException, CommandException, SyncParameterException {
        try (InputStream is = ImportPerformanceTest.class.getResourceAsStream("modplast-1.1.vna")) {
            byte[] bytes = IOUtils.toByteArray(is);
            SyncParameter parameter = new SyncParameter(true, false, false, false);
            SyncCommand syncCommand = new SyncCommand(parameter, bytes);
            commandService.executeCommand(syncCommand);

        }
    }

}