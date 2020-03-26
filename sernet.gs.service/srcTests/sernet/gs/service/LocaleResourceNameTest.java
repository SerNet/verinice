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

import org.junit.Test;

public class LocaleResourceNameTest {

    @Test
    public void testSanitizeLocale() {
        assertEquals("", AbstractReportTemplateService.sanitizeLocale("en", "report.properties"));
        assertEquals("", AbstractReportTemplateService.sanitizeLocale("en_UK", "report.properties"));
        assertEquals("", AbstractReportTemplateService.sanitizeLocale("de", "report_de.properties"));
        assertEquals("", AbstractReportTemplateService.sanitizeLocale("de_DE", "report_de.properties"));
        assertEquals("_de", AbstractReportTemplateService.sanitizeLocale("de", "report.properties"));
        assertEquals("_de", AbstractReportTemplateService.sanitizeLocale("DE", "report.properties"));
        assertEquals("_de", AbstractReportTemplateService.sanitizeLocale("de_DE", "report.properties"));
    }
}
