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
import java.util.Map;

import sernet.verinice.interfaces.IReportDepositService.OutputFormat;

/**
 *
 */
public class ReportTemplateMetaData implements Serializable {

    private static final long serialVersionUID = 201410011436L;

    private String filename;

    private OutputFormat[] outputFormat;

    private String outputname;

    private String md5CheckSumm;

    private boolean isServer;

    public ReportTemplateMetaData(String filename, String outputname, OutputFormat[] outputFormats) {
        this.filename = filename;
        this.outputname = outputname;
        this.outputFormat = outputFormats;
    }

    public ReportTemplateMetaData(String filename, String outputname, OutputFormat[] outputFormats, boolean isServer, String md5CheckSum) {
        this(filename, outputname, outputFormats);
        this.isServer = isServer;
        this.setMd5CheckSumm(md5CheckSum);
    }

    public String getFilename() {
        return filename;
    }

    public OutputFormat[] getOutputFormats() {
        return outputFormat;
    }

    public String getOutputname() {
        return outputname;
    }

    private String getMd5CheckSumm() {
        return md5CheckSumm;
    }

    private void setMd5CheckSumm(String md5CheckSumm) {
        this.md5CheckSumm = md5CheckSumm;
    }

    public boolean isServer() {
        return isServer;
    }

    public void setServer(boolean isServer) {
        this.isServer = isServer;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (!(obj instanceof ReportTemplateMetaData))
            return false;

        ReportTemplateMetaData other = (ReportTemplateMetaData) obj;

        if (md5CheckSumm != null && other.md5CheckSumm != null) {
            return md5CheckSumm.equals(md5CheckSumm);
        }

        if (filename != null) {
            return filename.equals(other.filename);
        }

        return false;
    }

    @Override
    public int hashCode() {
        int hash = 1;
        hash = hash * 17 + ((md5CheckSumm != null) ? md5CheckSumm.hashCode() : 0);
        hash = hash * 31 + ((filename != null) ? filename.hashCode() : 0);
        return hash;
    }
}
