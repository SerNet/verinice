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

import org.apache.commons.codec.digest.DigestUtils;
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

    private ReportDepositService() {
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * sernet.verinice.interfaces.report.IReportService#getOutputFormat(java
     * .lang.String)
     */
    public IOutputFormat getOutputFormat(OutputFormat formatLabel) {
        switch(formatLabel){
            case PDF: return new PDFOutputFormat();
            case HTML: return new HTMLOutputFormat();
            case ODS: return new ODSOutputFormat();
            case ODT: return new ODTOutputFormat();
            case XLS: return new ExcelOutputFormat();
            case DOC: return new WordOutputFormat();
            default: return null;
        }

    }

    public IOutputFormat[] getOutputFormats(OutputFormat[] formatLabel) {
        List<IOutputFormat> list = new ArrayList<IOutputFormat>(formatLabel.length);
        for (OutputFormat s : formatLabel) {
            IOutputFormat format = getOutputFormat(s);
            if (format != null) {
                list.add(format);
            } else {
                LOG.warn("Report output format:\t" + s + " not available in verinice");
            }
        }
        return list.toArray(new IOutputFormat[list.size()]);
    }

    public ReportTemplateMetaData[] getReportTemplates(String[] rptDesignFiles) throws IOException, ReportMetaDataException, PropertyFileExistsException {
        List<ReportTemplateMetaData> list = new ArrayList<ReportTemplateMetaData>(rptDesignFiles.length);
        for (String designFilePath : rptDesignFiles) {
            list.add(getMetaData(new File(designFilePath)));
        }
        return list.toArray(new ReportTemplateMetaData[rptDesignFiles.length]);
    }

    public ReportTemplateMetaData[] getServerReportTemplates() throws IOException, ReportMetaDataException, PropertyFileExistsException {
        return getReportTemplates(getServerRptDesigns());
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

    public ReportTemplateMetaData getMetaData(File rptDesign) throws IOException, ReportMetaDataException, PropertyFileExistsException {
        Properties props = null;
        if (checkReportMetaDataFile(rptDesign)) {
            props = parseAndExtendMetaData(rptDesign);
        } else {
            props = createDefaultProperties(rptDesign.getPath(), rptDesign.getName());
        }
        return createReportMetaData(props);
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
        String filename = metadata.getFilename();
        filename = filename.substring(0, filename.lastIndexOf(IReportDepositService.EXTENSION_SEPARATOR_CHAR) + 1);
        filename = filename + IReportDepositService.PROPERTIES_FILE_EXTENSION;
        File propFile = new File(filename);
        if (propFile.exists()) {
            FileUtils.deleteQuietly(propFile);
        }
        File rptFile = new File(metadata.getFilename());
        if (rptFile.exists()) {
            FileUtils.deleteQuietly(rptFile);
        }
    }

    private ReportTemplateMetaData createReportMetaData(Properties props) throws IOException {
        String outputformatsString = props.getProperty(IReportDepositService.PROPERTIES_OUTPUTFORMATS);
        StringTokenizer tokenizer = new StringTokenizer(outputformatsString, ",");
        ArrayList<OutputFormat> formats = new ArrayList<OutputFormat>(tokenizer.countTokens());
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken().trim();
            formats.add(OutputFormat.valueOf(token.toUpperCase()));
        }

        String fileName = props.getProperty(IReportDepositService.PROPERTIES_FILENAME);
        String outputName = props.getProperty(IReportDepositService.PROPERTIES_OUTPUTNAME);
        OutputFormat[] outputFormats = formats.toArray(new OutputFormat[formats.size()]);
        String md5CheckSum = getChecksum(fileName);

        return new ReportTemplateMetaData(fileName, outputName, outputFormats, md5CheckSum);
    }

    private String getChecksum(String fileName) throws IOException {
        String filePath = getReportDeposit().getFile().getPath() + File.separatorChar + fileName;
        FileInputStream fis = new FileInputStream(new File(filePath));
        return DigestUtils.md5Hex(fis);
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
            props.setProperty(PROPERTIES_OUTPUTFORMATS, StringUtils.join(OutputFormat.values(), ","));
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
        props.put(PROPERTIES_OUTPUTFORMATS, StringUtils.join(OutputFormat.values(), ','));
        return props;
    }

    public Resource getReportDeposit() {
        return reportDeposit;
    }

    public void setReportDeposit(Resource reportDeposit) {
        this.reportDeposit = reportDeposit;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.interfaces.IReportDepositService#getDepositLocation()
     */
    @Override
    public String getDepositLocation() throws IOException {
        if(getReportDeposit() != null){
            return getReportDeposit().getFile().getAbsolutePath();
        }
        return "";
    }
    
    @Override
    public void updateInServerDeposit(ReportTemplateMetaData metadata) throws IOException{
        if(checkReportMetaDataFile(new File(metadata.getFilename()))){
            File propFile = getPropertiesFile(metadata.getFilename());
            Properties props = parseAndExtendMetaData(propFile);
            props.setProperty(PROPERTIES_OUTPUTFORMATS, StringUtils.join(metadata.getOutputFormats(), ','));
            props.setProperty(PROPERTIES_OUTPUTNAME, metadata.getOutputname());
            writePropertiesFile(props, metadata.getFilename(), "");

        } else {
            writePropertiesFile(convertToProperties(metadata), metadata.getFilename(), "Default Properties for verinice-" + "Report " + metadata.getOutputname() + "\nauto-generated content");
        }
    }
    
    private void writePropertiesFile(Properties properties, String name, String comment) throws IOException{
        String newFilePath = getReportDeposit().getFile().getPath() + File.separatorChar + name;
        FileOutputStream fos = new FileOutputStream(newFilePath);
        properties.store(fos, comment);
        fos.close();
    }
    
    private Properties convertToProperties(ReportTemplateMetaData metaData){
        Properties props = new Properties();
        props.setProperty(PROPERTIES_OUTPUTFORMATS, StringUtils.join(metaData.getOutputFormats(), ','));
        props.setProperty(PROPERTIES_OUTPUTNAME, metaData.getOutputname());
        props.setProperty(PROPERTIES_FILENAME, metaData.getFilename());
        return props;
    }
    
    private ReportTemplateMetaData convertToReportTemplateMetadata(Properties props){
//        return new ReportTemplateMetaData(props.getProperty(IReportDepositService.PROPERTIES_FILENAME),
//                props.getProperty(IReportDepositService.PROPERTIES_OUTPUTNAME),
//                props.getProperty(IReportDepositService.PROPERTIES_OUTPUTFORMATS));
        return null;
    }

}
