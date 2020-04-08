/*******************************************************************************
 * Copyright (c) 2014 benjamin.
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
 *     benjamin <bw[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.interfaces;

import java.util.Locale;
import java.util.Set;

import sernet.gs.service.AbstractReportTemplateService;
import sernet.verinice.interfaces.report.IOutputFormat;
import sernet.verinice.model.report.FileMetaData;
import sernet.verinice.model.report.ReportTemplateMetaData;

/**
 * Provides metadata & content of report templates and other report resources.
 *
 * <p>
 * There is an abstract Implementation which is file system based:
 * {@link AbstractReportTemplateService}
 * </p>
 *
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 *
 */
public interface IReportTemplateService {

    public enum OutputFormat {
        PDF, HTML, XLS, ODS, ODT, DOC
    }

    public static final String PROPERTIES_FILE_EXTENSION = "properties";

    public static final String PROPERTIES_FILENAME = "filename";
    public static final String PROPERTIES_OUTPUTFORMATS = "outputformats";
    public static final String PROPERTIES_OUTPUTNAME = "outputname";
    public static final String PROPERTIES_MULTIPLE_ROOT_OBJECTS = "multiple_root_objects";
    public static final String PROPERTIES_CONTEXT = "context";

    public static final String REPORT_DEPOSIT_CLIENT_LOCAL = "report_templates_local";
    public static final String REPORT_DEPOSIT_CLIENT_REMOTE = "report_templates_remote";

    IOutputFormat getOutputFormat(OutputFormat format);

    IOutputFormat[] getOutputFormats(OutputFormat[] format);

    /**
     * Gets all report template resources (designs, libs, etc.).
     */
    public Set<FileMetaData> getAllResources() throws ReportTemplateServiceException;

    /**
     * Gets all report template resources (designs, libs, etc.) for given locale
     * (excludes all files specific to other locales).
     */
    public Set<FileMetaData> getAllResources(Locale locale) throws ReportTemplateServiceException;

    public Set<ReportTemplateMetaData> getReportTemplates(Locale locale)
            throws ReportTemplateServiceException;

    public byte[] readResource(String filename) throws ReportTemplateServiceException;
}
