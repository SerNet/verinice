/*******************************************************************************
 * Copyright (c) 2014 benjamin.
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
 *     benjamin <bw[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.service;

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
import org.apache.log4j.Logger;

import sernet.verinice.interfaces.IReportDepositService;
import sernet.verinice.interfaces.IReportTemplateService;
import sernet.verinice.interfaces.ReportTemplateServiceException;
import sernet.verinice.interfaces.report.IOutputFormat;
import sernet.verinice.model.report.ExcelOutputFormat;
import sernet.verinice.model.report.HTMLOutputFormat;
import sernet.verinice.model.report.ODSOutputFormat;
import sernet.verinice.model.report.ODTOutputFormat;
import sernet.verinice.model.report.PDFOutputFormat;
import sernet.verinice.model.report.PropertyFileExistsException;
import sernet.verinice.model.report.ReportTemplate;
import sernet.verinice.model.report.ReportTemplateMetaData;
import sernet.verinice.model.report.WordOutputFormat;

/**
 * Provides report template metadata and report templates.
 *
 * <p>
 * The implementation is file based, thus it can run on server side and on
 * client side. The user of this class must provide the report directory path
 * and if this directory is handled by the {@link IReportDepositService}.
 * </p>
 *
 * <p>
 * As a side effect, this implementation creates default properties for all
 * rptdesign files without a properties file automatically, if they don't exist.
 * <p>
 *
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 *
 */
abstract public class AbstractReportTemplateService implements IReportTemplateService {

    private final Logger LOG = Logger.getLogger(AbstractReportTemplateService.class);

    abstract public boolean isHandeledByReportDeposit();

    abstract public String getTemplateDirectory();

    public ReportTemplateMetaData getMetaData(File rptDesign, String locale) throws ReportTemplateServiceException {
        try {
            Properties props = null;
            if (checkReportMetaDataFile(rptDesign, locale)) {
                props = parseAndExtendMetaData(rptDesign, locale);
            } else {
                props = createDefaultProperties(rptDesign.getPath(), rptDesign.getName(), locale);
            }
            return createReportMetaData(props);
        } catch (IOException ex) {
            handleException("error while fetching/generating metadata", ex);
        } catch (PropertyFileExistsException ex) {
            handleException("error while fetching/generating metadata", ex);
        }

        return null;
    }

    protected void handleException(String msg, Exception ex) throws ReportTemplateServiceException {
        LOG.error(msg, ex);
        throw new ReportTemplateServiceException(ex);
    }

    public boolean checkReportMetaDataFile(File rptDesign, String locale) {
        File propertiesFile = getPropertiesFile(rptDesign, locale);
        return propertiesFile.exists();
    }

    protected Properties parseAndExtendMetaData(File rptDesign, String locale) throws IOException {
        File propFile = getPropertiesFile(rptDesign, locale);
        Properties props = new Properties();
        FileInputStream fis = new FileInputStream(propFile.getAbsoluteFile());
        props.load(fis);
        fis.close();

        String fileName = FilenameUtils.getName(rptDesign.getPath());
        boolean changed = false;
        if (!(props.containsKey(PROPERTIES_FILENAME))) {
            props.setProperty(PROPERTIES_FILENAME, fileName);
            changed = true;
        }
        if (!(props.containsKey(PROPERTIES_OUTPUTFORMATS))) {
            props.setProperty(PROPERTIES_OUTPUTFORMATS, StringUtils.join(OutputFormat.values(), ","));
            changed = true;
        }
        if (!(props.containsKey(PROPERTIES_OUTPUTNAME))) {
            props.setProperty(PROPERTIES_OUTPUTNAME, removeSuffix(fileName));
            changed = true;
        }

        if (changed) {
            OutputStream out = new FileOutputStream(propFile.getAbsoluteFile());
            props.store(out, String.format("Metadata for the report deposit %s", FilenameUtils.getName(rptDesign.getPath())));
        }

        return props;
    }

    private String removeSuffix(String fileName) {
        return fileName.substring(0, fileName.lastIndexOf(IReportDepositService.EXTENSION_SEPARATOR_CHAR));
    }

    public void parseAndExtendMetaData(String[] rptDesignFiles, String locale) throws IOException {
        for (String rptDesignFile : rptDesignFiles) {
            parseAndExtendMetaData(new File(rptDesignFile), locale);
        }
    }

    private File getPropertiesFile(File rptDesign, String locale) {
        String path = rptDesign.getPath();
        return getPropertiesFile(path, locale);
    }

    protected File getPropertiesFile(String path, String locale) {

        locale = sanitizeLocale(locale, path);
        path = removeSuffix(path);
        String templateDir = getTemplateDirectory();
        if (!templateDir.endsWith(String.valueOf(File.separatorChar))) {
            templateDir = templateDir + File.separatorChar;
        }
        if (!path.contains(templateDir)) {
            path = templateDir + templateDir;
        }
        File propFile = new File(path + locale + IReportDepositService.EXTENSION_SEPARATOR_CHAR + IReportDepositService.PROPERTIES_FILE_EXTENSION);
        return propFile;
    }

    /**
     * Generates a properties file suffix. It does not take into account
     * regions, so the string is empty, when it is the default english or the
     * default is already in the properties path.
     *
     * <p>Examples</p>
     *
     * sanitizeLocale(de_DE, "path/report_de.properties") -> ""
     * sanitizeLocale(de, "path/report_de.properties") -> ""
     * sanitizeLocale(DE, "path/report_de.properties") -> ""
     * sanitizeLocale(DE, "path/report.properties") -> "_de"
     * sanitizeLocale(en_UK, "path/report.properties") -> ""
     *
     * As we do not deal with dialects like en_UK here, we just take the
     * leftside locale (e.g. "en")
     *
     *
     */
    private String sanitizeLocale(String locale, String path) {

        if (locale.length() > 2 && locale.contains(String.valueOf('_'))) {
            locale = locale.substring(0, locale.indexOf(String.valueOf('_')));
        }

        if (!"".equals(locale)) {
            if ("en".equals(locale.toLowerCase()) || path.contains("_" + locale + IReportDepositService.EXTENSION_SEPARATOR_CHAR + IReportDepositService.PROPERTIES_FILE_EXTENSION)) {
                locale = "";
            } else {
                locale = "_" + locale.toLowerCase();
            }
        }

        return locale;
    }

    private Properties createDefaultProperties(String path, String name, String locale) throws IOException, PropertyFileExistsException {
        File propFile = getPropertiesFile(path, locale);
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
        props.setProperty(PROPERTIES_OUTPUTNAME, removeSuffix(name));
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
        boolean multipleRootObjects = Boolean.parseBoolean(props.getProperty(
                IReportDepositService.PROPERTIES_MULTIPLE_ROOT_OBJECTS, "false"));
        return new ReportTemplateMetaData(fileName, outputName, outputFormats, isHandeledByReportDeposit(),
                md5CheckSums, multipleRootObjects);
    }

    protected Map<String, byte[]> getPropertiesFiles(String fileName) throws IOException {
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
        String baseName = removeSuffix(fileName);
        IOFileFilter filter = new RegexFileFilter(baseName + "\\_?.*\\.properties", IOCase.INSENSITIVE);
        Iterator<File> iter = FileUtils.iterateFiles(new File(getTemplateDirectory()), filter, null);
        return iter;
    }

    private String[] getCheckSums(String fileName) throws IOException {
        String filePath;
        if (!fileName.contains(getTemplateDirectory())) {
            filePath = getTemplateDirectory() + File.separatorChar + fileName;
        } else {
            filePath = fileName;
        }
        Iterator<File> iter = listPropertiesFiles(fileName);

        List<String> md5CheckSums = new ArrayList<String>();
        md5CheckSums.add(DigestUtils.md5Hex(FileUtils.readFileToByteArray(new File(filePath))));

        while (iter.hasNext()) {
            File f = iter.next();
            md5CheckSums.add(DigestUtils.md5Hex(FileUtils.readFileToByteArray(f)));
        }

        return md5CheckSums.toArray(new String[md5CheckSums.size()]);
    }

    public Set<ReportTemplateMetaData> getReportTemplates(String[] rptDesignFiles, String locale) throws ReportTemplateServiceException {
        Set<ReportTemplateMetaData> set = new HashSet<ReportTemplateMetaData>();

        for (String designFilePath : rptDesignFiles) {
            set.add(getMetaData(new File(designFilePath), locale));
        }
        return set;
    }

    public Set<ReportTemplateMetaData> getReportTemplates(String locale) throws ReportTemplateServiceException {
        return getReportTemplates(getReportTemplateFileNames(), locale);
    }

    @SuppressWarnings({ "unchecked" })
    public String[] getReportTemplateFileNames() {
        List<String> list = new ArrayList<String>();
        IOFileFilter filter = new SuffixFileFilter("rptdesign", IOCase.INSENSITIVE);
        Iterator<File> iter = FileUtils.iterateFiles(new File(getTemplateDirectory()), filter, null);
        while (iter.hasNext()) {
            list.add(iter.next().getAbsolutePath());
        }
        return list.toArray(new String[list.size()]);
    }

    public IOutputFormat getOutputFormat(OutputFormat formatLabel) {
        switch (formatLabel) {
        case PDF:
            return new PDFOutputFormat();
        case HTML:
            return new HTMLOutputFormat();
        case ODS:
            return new ODSOutputFormat();
        case ODT:
            return new ODTOutputFormat();
        case XLS:
            return new ExcelOutputFormat();
        case DOC:
            return new WordOutputFormat();
        default:
            return null;
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

    @Override
    public ReportTemplate getReportTemplate(ReportTemplateMetaData metadata, String locale) throws ReportTemplateServiceException {
        try {

            String filePath = getTemplateDirectory() + File.separatorChar + metadata.getFilename();
            byte[] rptdesign = FileUtils.readFileToByteArray(new File(filePath));

            Map<String, byte[]> propertiesFile = getPropertiesFiles(metadata.getFilename());

            return new ReportTemplate(metadata, rptdesign, propertiesFile);
        } catch (IOException ex) {
            handleException("error while fetching template", ex);
        }

        return null;
    }

    @Override
    public Set<ReportTemplateMetaData> getServerReportTemplates(String locale) throws ReportTemplateServiceException {
        return getReportTemplateMetaData(getServerRptDesigns(), locale);
    }

    @Override
    public Set<ReportTemplateMetaData> getReportTemplateMetaData(String[] rptDesignFiles, String locale) throws ReportTemplateServiceException {
        return getReportTemplates(rptDesignFiles, locale);
    }

    @SuppressWarnings("unchecked")
    private String[] getServerRptDesigns() throws ReportTemplateServiceException {
        List<String> list = new ArrayList<String>(0);
        // // DirFilter = null means no subdirectories
        IOFileFilter filter = new SuffixFileFilter("rptdesign", IOCase.INSENSITIVE);
        Iterator<File> iter = FileUtils.iterateFiles(new File(getTemplateDirectory()), filter, null);
        while (iter.hasNext()) {
            list.add(iter.next().getAbsolutePath());
        }
        return list.toArray(new String[list.size()]);
    }

}
