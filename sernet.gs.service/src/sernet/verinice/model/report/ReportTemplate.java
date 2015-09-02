/*******************************************************************************
 * Copyright (c) 2014 Benjamin Weißenfels.
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
 *     Benjamin Weißenfels <bw[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.model.report;

import java.io.Serializable;
import java.util.Map;

/**
 * Represents a rptdesignFile file in memory and also contains the properties
 * files.
 *
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 *
 */

public class ReportTemplate implements Serializable {

    private static final long serialVersionUID = -3027918113110500864L;

    private ReportTemplateMetaData metaData;

    private byte[] rptdesignFile;

    private Map<String, byte[]> propertiesFiles;

    public ReportTemplate(ReportTemplateMetaData metaData, byte[] rptdesignFile, Map<String, byte[]> propertiesFiles) {
        this.metaData = metaData;
        setRptdesignFile(rptdesignFile);
        this.setPropertiesFiles(propertiesFiles);
    }

    public ReportTemplateMetaData getMetaData() {
        return metaData;
    }

    public void setMetaData(ReportTemplateMetaData metaData) {
        this.metaData = metaData;
    }

    public byte[] getRptdesignFile() {
        return rptdesignFile;
    }

    public void setRptdesignFile(byte[] rptdesign) {
        this.rptdesignFile = (rptdesign!=null) ? rptdesign.clone() : null;
    }

    public Map<String, byte[]> getPropertiesFiles() {
        return propertiesFiles;
    }

    public void setPropertiesFiles(Map<String, byte[]> propertiesFiles) {
        this.propertiesFiles = propertiesFiles;
    }

}
