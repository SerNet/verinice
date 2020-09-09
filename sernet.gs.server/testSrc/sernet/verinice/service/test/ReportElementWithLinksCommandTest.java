/*******************************************************************************
 * Copyright (c) 2019 Daniel Murygin
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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import sernet.verinice.interfaces.CommandException;
import sernet.verinice.service.commands.crud.LoadReportElementWithLinks;

/**
 * Test class for command ReportElementWithLinks
 */
@TransactionConfiguration(transactionManager = "txManager")
@Transactional
@SuppressWarnings("restriction")
public class ReportElementWithLinksCommandTest extends HibernateInstanceOfInterfaceTest {

    private static final Logger log = Logger.getLogger(ReportElementWithLinksCommandTest.class);

    /**
     * Checks if the result of the command contains all abbreviations of the
     * linked assets of a process.
     */
    @Test
    public void testAbbreviation() throws CommandException {
        beforeTestInstanceOfLinks();
        LoadReportElementWithLinks command = new LoadReportElementWithLinks(null,
                process.getDbId());
        command = commandService.executeCommand(command);
        List<List<String>> result = command.getResult();
        if (log.isDebugEnabled()) {
            logResult(result);
        }
        assertNotNull(result);
        assertEquals(2, result.size());
        List<String> abbreviationList = getAbbreviationList(result);
        assertEquals(2, abbreviationList.size());
        assertTrue(abbreviationList.contains(asset1.getAbbreviation()));
        assertTrue(abbreviationList.contains(asset2.getAbbreviation()));
    }

    public void beforeTestCommandLoadReportElementWithLinks() throws CommandException {
        createOrganizationWithLinkedProcessAndAssets();
    }

    private List<String> getAbbreviationList(List<List<String>> result) {
        return result.stream().map(row -> row.get(1)).collect(Collectors.toList());
    }

    public void logResult(List<List<String>> result) {
        for (List<String> row : result) {
            StringBuilder sb = new StringBuilder();
            row.stream().forEach(column -> sb.append(column).append(" | "));
            log.debug(sb.toString());
        }
    }
}
