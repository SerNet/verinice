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
package sernet.verinice.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.core.io.Resource;

import sernet.verinice.interfaces.IReportDepositService;
import sernet.verinice.interfaces.report.IOutputFormat;
import sernet.verinice.model.report.ExcelOutputFormat;
import sernet.verinice.model.report.HTMLOutputFormat;
import sernet.verinice.model.report.ODSOutputFormat;
import sernet.verinice.model.report.ODTOutputFormat;
import sernet.verinice.model.report.PDFOutputFormat;
import sernet.verinice.model.report.PropertyFileExistsException;
import sernet.verinice.model.report.ReportMetaDataException;
import sernet.verinice.model.report.ReportTemplateMetaData;
import sernet.verinice.model.report.WordOutputFormat;
import sernet.verinice.service.report.Messages;

/**
 *
 */
public class ReportDepositService implements IReportDepositService {

    private static final Logger LOG = Logger.getLogger(ReportDepositService.class);

    private Resource reportDeposit;

    private static ReportDepositService INSTANCE = new ReportDepositService();

    private ReportDepositService() {
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * sernet.verinice.interfaces.report.IReportService#getOutputFormat(java
     * .lang.String)
     */
    public IOutputFormat getOutputFormat(String formatLabel) {
        if ("pdf".equalsIgnoreCase(formatLabel)) {
            return new PDFOutputFormat();
        } else if ("html".equalsIgnoreCase(formatLabel)) {
            return new HTMLOutputFormat();
        } else if ("doc".equalsIgnoreCase(formatLabel)) {
            return new WordOutputFormat();
        } else if ("xls".equalsIgnoreCase(formatLabel)) {
            return new ExcelOutputFormat();
        } else if ("odt".equalsIgnoreCase(formatLabel)) {
            return new ODTOutputFormat();
        } else if ("ods".equalsIgnoreCase(formatLabel)) {
            return new ODSOutputFormat();
        }
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * sernet.verinice.interfaces.report.IReportService#getOutputFormats(java
     * .lang.String[])
     */
    public IOutputFormat[] getOutputFormats(String[] formatLabel) {
        List<IOutputFormat> list = new ArrayList<IOutputFormat>(formatLabel.length);
        for (String s : formatLabel) {
            IOutputFormat format = getOutputFormat(s);
            if (format != null) {
                list.add(format);
            } else {
                LOG.warn("Report output format:\t" + s + " not available in verinice");
            }
        }
        return list.toArray(new IOutputFormat[list.size()]);
    }

    public ReportTemplateMetaData[] getReportTemplates(String[] rptDesignFiles, boolean isServer) throws IOException, ReportMetaDataException, PropertyFileExistsException {
        List<ReportTemplateMetaData> list = new ArrayList<ReportTemplateMetaData>(rptDesignFiles.length);
        for (String designFilePath : rptDesignFiles) {
            list.add(getMetaData(new File(designFilePath), isServer));
        }
        return list.toArray(new ReportTemplateMetaData[rptDesignFiles.length]);
    }

    public ReportTemplateMetaData[] getServerReportTemplates() throws IOException, ReportMetaDataException, PropertyFileExistsException {
        return getReportTemplates(getServerRptDesigns(), true);
    }

    private String[] getServerRptDesigns() throws IOException {
        List<String> list = new ArrayList<String>(0);
        // // DirFilter = null means no subdirectories
        IOFileFilter filter = new SuffixFileFilter("rptdesign", IOCase.INSENSITIVE);
        Iterator<File> iter = FileUtils.iterateFiles(getReportDeposit().getFile(), filter, null);
        while (iter.hasNext()) {
            list.add(iter.next().getAbsolutePath());
        }
        return list.toArray(new String[list.size()]);
    }

    public ReportTemplateMetaData getMetaData(File rptDesign, boolean isServer) throws IOException, ReportMetaDataException, PropertyFileExistsException {
        Properties props = null;
        if (checkReportMetaDataFile(rptDesign)) {
            props = parseAndExtendMetaData(rptDesign);
        } else {
            props = createDefaultProperties(rptDesign.getPath(), rptDesign.getName());
        }
        return createReportMetaData(props, isServer);
    }

    public void addToServerDeposit(ReportTemplateMetaData metadata, byte[] file) {
        String filename = metadata.getFilename();
        filename = filename.substring(filename.lastIndexOf(File.separatorChar) + 1);
        File serverDepositPath;
        try {
            serverDepositPath = getReportDeposit().getFile();
            String newFilePath = serverDepositPath.getPath() + File.separatorChar + filename;
            FileUtils.writeByteArrayToFile(new File(newFilePath), file);

        } catch (IOException e) {
            LOG.error("Error reading report deposit location on server", e);
        }
    }


    public void removeFromServer(ReportTemplateMetaData metadata) throws IOException {
        FileUtils.deleteQuietly(new File(getReportDeposit().getFile().getPath() + File.separatorChar + metadata.getFilename()));
    }

    private ReportTemplateMetaData createReportMetaData(Properties props, boolean isServer) {
        String outputformatsString = props.getProperty(IReportDepositService.PROPERTIES_OUTPUTFORMATS);
        StringTokenizer tokenizer = new StringTokenizer(outputformatsString, ",");
        ArrayList<String> formats = new ArrayList<String>(tokenizer.countTokens());
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken().trim();
            formats.add(token);
        }
        return new ReportTemplateMetaData(props.getProperty(IReportDepositService.PROPERTIES_FILENAME), props.getProperty(PROPERTIES_OUTPUTNAME), formats.toArray(new String[formats.size()]), isServer);
    }

    private Properties parseAndExtendMetaData(File rptDesign) throws IOException {
        File propFile = getPropertiesFile(rptDesign);
        Properties props = new Properties();
        FileInputStream fis = new FileInputStream(propFile.getAbsoluteFile());
        props.load(fis);
        fis.close();


        if (!(props.containsKey(PROPERTIES_FILENAME))) {
            props.setProperty(PROPERTIES_FILENAME, FilenameUtils.getName(rptDesign.getPath()));
        }
        if (!(props.containsKey(PROPERTIES_OUTPUTFORMATS))) {
            props.setProperty(PROPERTIES_OUTPUTFORMATS, StringUtils.join(OutputFormats.values(), ","));
        }
        if (!(props.containsKey(PROPERTIES_OUTPUTNAME))) {
            props.setProperty(PROPERTIES_OUTPUTNAME, Messages.PROPERTIES_DEFAULT_OUTPUT_NAME);
        }

        OutputStream out = new FileOutputStream(propFile);
        props.store(out, "Metadata for the report deposit");

        return props;
    }

    private File getPropertiesFile(File rptDesign) {
        String path = rptDesign.getPath();
        return getPropertiesFile(path);
    }

    private File getPropertiesFile(String path) {
        path = path.substring(0, path.lastIndexOf(IReportDepositService.EXTENSION_SEPARATOR_CHAR));
        File propFile = new File(path + IReportDepositService.EXTENSION_SEPARATOR_CHAR + IReportDepositService.PROPERTIES_FILE_EXTENSION);
        return propFile;
    }

    private boolean checkReportMetaDataFile(File rptDesign) {
        File propertiesFile = getPropertiesFile(rptDesign);
        return propertiesFile.exists();
    }

    private Properties createDefaultProperties(String path, String name) throws IOException, PropertyFileExistsException {
        File propFile = getPropertiesFile(path);
        if (propFile.exists()) {
            throw new PropertyFileExistsException();
        } else {
            Properties props = getDefaultProperties();
            FileOutputStream fos = new FileOutputStream(propFile);
            props.setProperty(PROPERTIES_FILENAME, name);
            props.store(fos, "Default Properties for verinice-" + "Report " + name + "\nauto-generated content");
            fos.close();
            return props;
        }

    }

    private Properties getDefaultProperties() {
        Properties props = new Properties();
        props.put(PROPERTIES_OUTPUTNAME, Messages.PROPERTIES_DEFAULT_OUTPUT_NAME);
        props.put(PROPERTIES_OUTPUTFORMATS, StringUtils.join(OutputFormats.values(), ','));
        return props;
    }

    public static ReportDepositService getInstance() {
        return INSTANCE;
    }

    public Resource getReportDeposit() {
        return reportDeposit;
    }

    public void setReportDeposit(Resource reportDeposit) {
        this.reportDeposit = reportDeposit;
    }

}
