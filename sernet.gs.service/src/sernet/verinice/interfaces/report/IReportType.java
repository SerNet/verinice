/*******************************************************************************
 * Copyright (c) 2010 Robert Schuster <r.schuster@tarent.de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *     You should have received a copy of the GNU Lesser General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Robert Schuster <r.schuster@tarent.de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.interfaces.report;

import sernet.verinice.model.report.ReportTemplateMetaData;

public interface IReportType {

    // reports only applicable to organizations / it networks
    public final static String USE_CASE_ID_GENERAL_REPORT = "use_case_report_general";
    // reports applicable to both of the cases above
    public final static String USE_CASE_ID_ALWAYS_REPORT = "use_case_report_always";

    public static final String USER_REPORT_ID = "user"; //$NON-NLS-1$

    /**
     * @return an application usable id
     */
    String getId();

    /**
     * @return a human-readable name for this report type
     */
    String getLabel();

    /**
     * @return a human-readable description of this report type
     */
    String getDescription();

    /**
     * @return possible output formats
     */
    IOutputFormat[] getOutputFormats();

    void createReport(IReportOptions reportOptions);

    void createReport(ReportTemplateMetaData metadata);

    /**
     * Return the selected report file or empty string. Reports that do not
     * support file selection should return null.
     * 
     * @return file String or null if not supported (i.e. internal reports
     *         contained in JAR file).
     */
    String getReportFile();

    void setReportFile(String file);

    /**
     * Some Reports should only be used with a particular type of root element
     * (e.g. csr only with audits) this id should determine which report belongs
     * to which type of action
     * 
     * @return The use case ID
     */
    String getUseCaseID();

}
