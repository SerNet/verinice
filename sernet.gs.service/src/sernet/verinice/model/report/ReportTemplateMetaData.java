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

import org.eclipse.jdt.annotation.NonNull;

import sernet.gs.service.NumericStringComparator;
import sernet.verinice.interfaces.IReportTemplateService.OutputFormat;

public class ReportTemplateMetaData implements Serializable, Comparable<ReportTemplateMetaData> {

    private static final long serialVersionUID = 1208760677224489421L;

    private static final NumericStringComparator NSC = new NumericStringComparator();

    public static final String[] CONTEXTS = { "ISM-ISO", "ISM-ISA", "ISM-DS", "ITGS", "ITGS-DS", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
            "ITGS-alt", Messages.ReportTemplateMetaDataUnspecified }; //$NON-NLS-1$

    public static final String REPORT_LOCAL_DECORATOR = "(L)"; //$NON-NLS-1$
    public static final String REPORT_SERVER_DECORATOR = "(S)"; //$NON-NLS-1$

    private OutputFormat[] outputFormat;

    private String outputname;

    private String context;

    private boolean isServer;

    /**
     * Can this report run with multiple root objects.
     */
    private boolean multipleRootObjects;

    private FileMetaData fileMetaData;

    public ReportTemplateMetaData(@NonNull FileMetaData fileMetadata, String outputname,
            OutputFormat[] outputFormats, boolean isServer, boolean multipleRootObjects,
            String context) {

        this.fileMetaData = fileMetadata;
        this.outputname = outputname;
        setOutputFormats(outputFormats);
        this.isServer = isServer;
        this.multipleRootObjects = multipleRootObjects;
        this.context = context;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        int prime = 31;
        int result = fileMetaData.hashCode();
        result = prime * result + Arrays.hashCode(outputFormat);
        result = prime * result + ((outputname == null) ? 0 : outputname.hashCode());
        result = prime * result + Boolean.valueOf(multipleRootObjects).hashCode();
        result = prime * result + getContext().hashCode();
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ReportTemplateMetaData other = (ReportTemplateMetaData) obj;
        if (fileMetaData == null) {
            if (other.fileMetaData != null) {
                return false;
            }
        } else if (!fileMetaData.equals(other.fileMetaData)) {
            return false;
        }
        if (!Arrays.equals(outputFormat, other.outputFormat)) {
            return false;
        }
        if (outputname == null) {
            if (other.outputname != null) {
                return false;
            }
        } else if (!outputname.equals(other.outputname)) {
            return false;
        }
        if (multipleRootObjects != other.multipleRootObjects) {
            return false;
        }

        return true;
    }

    public String getFilename() {
        return fileMetaData.getFilename();
    }

    public OutputFormat[] getOutputFormats() {
        return outputFormat;
    }

    public void setOutputFormats(OutputFormat[] outputFormats) {
        this.outputFormat = (outputFormats != null) ? outputFormats.clone() : null;
    }

    public String getDecoratedOutputname() {
        String name;
        if (isServer()) {
            name = (ReportTemplateMetaData.REPORT_SERVER_DECORATOR + " " + getOutputname()); //$NON-NLS-1$
        } else {
            name = (ReportTemplateMetaData.REPORT_LOCAL_DECORATOR + " " + getOutputname()); //$NON-NLS-1$
        }
        return name;
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
    public int compareTo(ReportTemplateMetaData other) {
        return NSC.compare(getDecoratedOutputname(), other.getDecoratedOutputname());
    }

    public boolean isMultipleRootObjects() {
        return multipleRootObjects;
    }

    public void setMultipleRootObject(boolean multipleRootObjects) {
        this.multipleRootObjects = multipleRootObjects;
    }

    public @NonNull String getContext() {
        if (context == null) {
            return ""; //$NON-NLS-1$
        }
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }
}
