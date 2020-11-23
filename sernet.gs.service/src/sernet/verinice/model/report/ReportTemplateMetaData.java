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
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;

import sernet.gs.service.NumericStringComparator;
import sernet.verinice.interfaces.IReportTemplateService.OutputFormat;
import sernet.verinice.model.common.CnATreeElement;

public class ReportTemplateMetaData implements Serializable, Comparable<ReportTemplateMetaData> {

    private static final long serialVersionUID = 1208760677224489421L;

    private static final NumericStringComparator NSC = new NumericStringComparator();

    public static final String REPORT_LOCAL_DECORATOR = "(L)"; //$NON-NLS-1$
    public static final String REPORT_SERVER_DECORATOR = "(S)"; //$NON-NLS-1$

    private OutputFormat[] outputFormat;

    private String outputname;

    private @NonNull ReportContext context;

    private boolean isServer;

    /**
     * Can this report run with multiple root objects.
     */
    private boolean multipleRootObjects;

    private FileMetaData fileMetaData;

    public ReportTemplateMetaData(@NonNull FileMetaData fileMetadata, String outputname,
            OutputFormat[] outputFormats, boolean isServer, boolean multipleRootObjects,
            @NonNull ReportContext context) {

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
        if (context != other.context) {
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

    public @NonNull ReportContext getContext() {
        return context;
    }

    public void setContext(@NonNull ReportContext context) {
        this.context = context;
    }

    public enum ReportContext {
        ISM_ISO, ISM_ISA, ISM_DS, ITGS, ITGS_DS, ITGS_ALT, UNSPECIFIED;

        public static List<ReportContext> getValidContexts(@NonNull CnATreeElement element) {
            if (element.isOrganization()) {
                return Arrays.asList(ReportContext.ISM_DS, ReportContext.ISM_ISA,
                        ReportContext.ISM_ISO, ReportContext.UNSPECIFIED);
            } else if (element.isItNetwork()) {
                return Arrays.asList(ReportContext.ITGS, ReportContext.ITGS_DS,
                        ReportContext.UNSPECIFIED);
            } else if (element.isItVerbund()) {
                return Arrays.asList(ReportContext.ITGS_ALT, ReportContext.UNSPECIFIED);
            }
            return Arrays.asList(ReportContext.UNSPECIFIED);
        }

        public static ReportContext fromString(String context) {
            try {
                return ReportContext.valueOf(context.toUpperCase().replace('-', '_'));
            } catch (Exception ignored) {
            }
            return ReportContext.UNSPECIFIED;
        }

        public String prettyString() {
            return name().replace('_', '-').replace("ALT", "alt");
        }
    }
}
