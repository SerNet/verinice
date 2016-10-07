/*******************************************************************************
 * Copyright (c) 2016 Sebastian Hagedorn.
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
 *     Sebastian Hagedorn <sh[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.security.report;

import java.net.URL;

import sernet.verinice.interfaces.report.IReportOptions;
import sernet.verinice.model.report.ReportTemplateMetaData;

/**
 * wrap informations needed for executing / generating a report 
 * in a secure context ( sandbox)
 * @author Sebastian Hagedorn <sh[at]sernet[dot]de>
 */
public class ReportSecurityContext {
    
    private IReportOptions reportOptions;
    private URL rptDesignUrl;
    private String logFileLocation;
    private ReportTemplateMetaData metaData;

    /**
     * @param reportOptions
     * @param rptDesignUrl
     * @param logFileLocation
     */
    public ReportSecurityContext(IReportOptions reportOptions, URL rptDesignUrl,
            String logFileLocation, ReportTemplateMetaData reportMetaData) {
        this.reportOptions = reportOptions;
        this.rptDesignUrl = rptDesignUrl;
        this.logFileLocation = logFileLocation;
        this.metaData = reportMetaData;
    }
    public IReportOptions getReportOptions() {
        return reportOptions;
    }
    public URL getRptDesignUrl() {
        return rptDesignUrl;
    }
    public String getLogFileLocation() {
        return logFileLocation;
    }
    
    public ReportTemplateMetaData getTemplateMetaData(){
        return this.metaData;
    }

}
