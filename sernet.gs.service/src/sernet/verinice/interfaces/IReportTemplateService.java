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

import java.io.File;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;

import sernet.gs.service.AbstractReportTemplateService;
import sernet.verinice.interfaces.report.IOutputFormat;
import sernet.verinice.model.report.ReportTemplate;
import sernet.verinice.model.report.ReportTemplateMetaData;

/**
 * Provides an interfaces for report metadata and retrieving full template
 * together with their report property files. A full report template is
 * represented {@link ReportTemplate}.
 *
 * <p>
 * There exists in abstract Implementation which is files system based:
 * {@link AbstractReportTemplateService}
 * </p>
 *
 * @see AbstractReportTemplateService
 *
 *
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 *
 */
public interface IReportTemplateService {

    public enum OutputFormat {
        PDF, HTML, XLS, ODS, ODT, DOC
    }

    public static final String PROPERTIES_FILE_EXTENSION = "properties";
    public static final char EXTENSION_SEPARATOR_CHAR = FilenameUtils.EXTENSION_SEPARATOR;

    public static final String PROPERTIES_FILENAME = "filename";
    public static final String PROPERTIES_OUTPUTFORMATS = "outputformats";
    public static final String PROPERTIES_OUTPUTNAME = "outputname";
    public static final String PROPERTIES_MULTIPLE_ROOT_OBJECTS = "multiple_root_objects";
    
    public static final String REPORT_DEPOSIT_CLIENT_LOCAL = "report_templates_local";
    public static final String REPORT_DEPOSIT_CLIENT_REMOTE = "report_templates_remote";

    IOutputFormat getOutputFormat(OutputFormat format);

    IOutputFormat[] getOutputFormats(OutputFormat[] format);

    public ReportTemplate getReportTemplate(ReportTemplateMetaData metadata, String locale) throws ReportTemplateServiceException;

    public Set<ReportTemplateMetaData> getServerReportTemplates(String locale) throws ReportTemplateServiceException;

    public Set<ReportTemplateMetaData> getReportTemplateMetaData(String[] rptDesignFiles, String locale) throws ReportTemplateServiceException;

    public Set<ReportTemplateMetaData> getReportTemplates(String[] rptDesignFiles, String locale) throws ReportTemplateServiceException;

    public Set<ReportTemplateMetaData> getReportTemplates(String locale) throws ReportTemplateServiceException;

    public Iterator<File> listPropertiesFiles(String fileName);

    @Deprecated
    ReportTemplateMetaData getMetaData(File rptDesign, String locale) throws ReportTemplateServiceException;
}
