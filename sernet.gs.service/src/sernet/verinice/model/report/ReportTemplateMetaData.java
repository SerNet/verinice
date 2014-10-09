/*******************************************************************************
 * Copyright (c) 2014 Sebastian Hagedorn <sh@sernet.de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details.
 *     You should have received a copy of the GNU General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Sebastian Hagedorn <sh@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.model.report;

import java.io.Serializable;
import java.util.Arrays;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.lang.ArrayUtils;

import sernet.gs.service.NumericStringComparator;
import sernet.verinice.interfaces.IReportDepositService.OutputFormat;

public class ReportTemplateMetaData implements Serializable, Comparable<ReportTemplateMetaData> {

    private static final long serialVersionUID = 201410011436L;

    private String filename;

    private OutputFormat[] outputFormat;

    private String outputname;

    /**
     * contains checksums from the rptdesign file and also from all propertie
     * files
     **/
    private String[] md5CheckSums;

    private boolean isServer;

    public ReportTemplateMetaData(String filename, String outputname, OutputFormat[] outputFormats, boolean isServer, String[] md5CheckSums) {
        this.filename = filename;
        this.outputname = outputname;
        this.outputFormat = outputFormats;
        this.isServer = isServer;
        this.md5CheckSums = md5CheckSums;
    }

    public String getFilename() {
        return filename;
    }

    public OutputFormat[] getOutputFormats() {
        return outputFormat;
    }
    
    public void setOutputFormats(OutputFormat[] outputFormats) {
        this.outputFormat = outputFormats;
    }


    public String getOutputname() {
        return outputname;
    }
    
    public void setOutputname(String outputname) {
        this.outputname = outputname;
    }

    public boolean isServer() {
        return isServer;
    }

    public void setServer(boolean isServer) {
        this.isServer = isServer;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ReportTemplateMetaData other = (ReportTemplateMetaData) obj;
        if (filename == null) {
            if (other.filename != null)
                return false;
        } else if (!filename.equals(other.filename))
            return false;
        if (!Arrays.equals(md5CheckSums, other.md5CheckSums))
            return false;
        if (!Arrays.equals(outputFormat, other.outputFormat))
            return false;
        if (outputname == null) {
            if (other.outputname != null)
                return false;
        } else if (!outputname.equals(other.outputname))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((filename == null) ? 0 : filename.hashCode());
        result = prime * result + Arrays.hashCode(md5CheckSums);
        result = prime * result + Arrays.hashCode(outputFormat);
        result = prime * result + ((outputname == null) ? 0 : outputname.hashCode());
        return result;
    }

    @Override
    public int compareTo(ReportTemplateMetaData other) {
        return new NumericStringComparator().compare(filename, other.filename);
    }
}
