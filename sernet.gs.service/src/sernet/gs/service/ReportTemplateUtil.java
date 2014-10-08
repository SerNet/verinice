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
package sernet.gs.service;

import static sernet.verinice.interfaces.IReportDepositService.PROPERTIES_FILENAME;
import static sernet.verinice.interfaces.IReportDepositService.PROPERTIES_OUTPUTFORMATS;
import static sernet.verinice.interfaces.IReportDepositService.PROPERTIES_OUTPUTNAME;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.lang.StringUtils;

import sernet.verinice.interfaces.IReportDepositService;
import sernet.verinice.interfaces.IReportDepositService.OutputFormat;
import sernet.verinice.model.report.PropertyFileExistsException;
import sernet.verinice.model.report.ReportMetaDataException;
import sernet.verinice.model.report.ReportTemplateMetaData;

/**
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 *
 */
public class ReportTemplateUtil {

    private String reportTemplateDirectory;

    private boolean isServerSide;

    public ReportTemplateUtil(String reportDepositDirectory) {
        this.reportTemplateDirectory = reportDepositDirectory;
    }

    public ReportTemplateUtil(String reportDepositDirectory, boolean isServerSide) {
        this.reportTemplateDirectory = reportDepositDirectory;
        this.isServerSide = isServerSide;
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

    public boolean checkReportMetaDataFile(File rptDesign) {
        File propertiesFile = getPropertiesFile(rptDesign);
        return propertiesFile.exists();
    }

    public Properties parseAndExtendMetaData(File rptDesign) throws IOException {
        File propFile = getPropertiesFile(rptDesign);
        Properties props = new Properties();
        FileInputStream fis = new FileInputStream(propFile.getAbsoluteFile());
        props.load(fis);
        fis.close();

        boolean changed = false;
        if (!(props.containsKey(PROPERTIES_FILENAME))) {
            props.setProperty(PROPERTIES_FILENAME, FilenameUtils.getName(rptDesign.getPath()));
            changed = true;
        }
        if (!(props.containsKey(PROPERTIES_OUTPUTFORMATS))) {
            props.setProperty(PROPERTIES_OUTPUTFORMATS, StringUtils.join(OutputFormat.values(), ","));
            changed = true;
        }
        if (!(props.containsKey(PROPERTIES_OUTPUTNAME))) {
            props.setProperty(PROPERTIES_OUTPUTNAME, FilenameUtils.getName(rptDesign.getPath()));
            changed = true;
        }

        if (changed) {
            OutputStream out = new FileOutputStream(propFile.getAbsoluteFile());
            props.store(out, String.format("Metadata for the report deposit %s", FilenameUtils.getName(rptDesign.getPath())));
        }

        return props;
    }

    public void parseAndExtendMetaData(String[] rptDesignFiles) throws IOException {
        for (String rptDesignFile : rptDesignFiles) {
            parseAndExtendMetaData(new File(rptDesignFile));
        }
    }

    private File getPropertiesFile(File rptDesign) {
        String path = rptDesign.getPath();
        return getPropertiesFile(path);
    }

    public File getPropertiesFile(String path) {
        path = path.substring(0, path.lastIndexOf(IReportDepositService.EXTENSION_SEPARATOR_CHAR));
        File propFile = new File(path + IReportDepositService.EXTENSION_SEPARATOR_CHAR + IReportDepositService.PROPERTIES_FILE_EXTENSION);
        return propFile;
    }

    private Properties createDefaultProperties(String path, String name) throws IOException, PropertyFileExistsException {
        File propFile = getPropertiesFile(path);
        if (propFile.exists()) {
            throw new PropertyFileExistsException();
        } else {
            Properties props = getDefaultProperties(name);
            FileOutputStream fos = new FileOutputStream(propFile);

            props.store(fos, "Default Properties for verinice-" + "Report " + name + "\nauto-generated content");
            fos.close();
            return props;
        }

    }

    private Properties getDefaultProperties(String name) {
        Properties props = new Properties();
        props.setProperty(PROPERTIES_FILENAME, name);
        props.setProperty(PROPERTIES_OUTPUTNAME, name);
        props.setProperty(PROPERTIES_OUTPUTFORMATS, StringUtils.join(OutputFormat.values(), ','));
        return props;
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
        String[] md5CheckSums = getCheckSums(fileName);

        return new ReportTemplateMetaData(fileName, outputName, outputFormats, isServerSide, md5CheckSums);
    }

    public Map<String, byte[]> getPropertiesFiles(String fileName) throws IOException {
        Map<String, byte[]> propertiesFiles = new TreeMap<String, byte[]>();
        Iterator<File> iter = listPropertiesFiles(fileName);
        while (iter.hasNext()) {
            File f = iter.next();
            propertiesFiles.put(f.getName(), FileUtils.readFileToByteArray(f.getAbsoluteFile()));
        }

        return propertiesFiles;
    }

    @SuppressWarnings("unchecked")
    public Iterator<File> listPropertiesFiles(String fileName) {
        String baseName = fileName.substring(0, fileName.lastIndexOf(IReportDepositService.EXTENSION_SEPARATOR_CHAR));
        IOFileFilter filter = new RegexFileFilter(baseName + "\\_?.*\\.properties", IOCase.INSENSITIVE);
        Iterator<File> iter = FileUtils.iterateFiles(new File(this.reportTemplateDirectory), filter, null);
        return iter;
    }

    private String[] getCheckSums(String fileName) throws IOException {

        String filePath = reportTemplateDirectory + File.separatorChar + fileName;
        Iterator<File> iter = listPropertiesFiles(fileName);

        List<String> md5CheckSums = new ArrayList<String>();
        md5CheckSums.add(DigestUtils.md5Hex(FileUtils.readFileToByteArray(new File(filePath))));

        while (iter.hasNext()) {
            File f = iter.next();
            md5CheckSums.add(DigestUtils.md5Hex(FileUtils.readFileToByteArray(f)));
        }

        return md5CheckSums.toArray(new String[md5CheckSums.size()]);
    }

    public Set<ReportTemplateMetaData> getReportTemplates(String[] rptDesignFiles) throws IOException, ReportMetaDataException, PropertyFileExistsException {
        Set<ReportTemplateMetaData> set = new HashSet<ReportTemplateMetaData>();

        for (String designFilePath : rptDesignFiles) {
            set.add(getMetaData(new File(designFilePath)));
        }
        return set;
    }

    public Set<ReportTemplateMetaData> getReportTemplates() throws IOException, ReportMetaDataException, PropertyFileExistsException {
        return getReportTemplates(getReportTemplateFileNames());
    }

    @SuppressWarnings({ "unchecked" })
    public String[] getReportTemplateFileNames() {
        List<String> list = new ArrayList<String>();
        IOFileFilter filter = new SuffixFileFilter("rptdesign", IOCase.INSENSITIVE);
        Iterator<File> iter = FileUtils.iterateFiles(new File(this.reportTemplateDirectory), filter, null);
        while (iter.hasNext()) {
            list.add(iter.next().getAbsolutePath());
        }
        return list.toArray(new String[list.size()]);
    }
}
