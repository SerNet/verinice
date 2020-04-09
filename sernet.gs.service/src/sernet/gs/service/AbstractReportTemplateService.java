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
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jfree.util.Log;

import sernet.verinice.interfaces.IReportDepositService;
import sernet.verinice.interfaces.IReportTemplateService;
import sernet.verinice.interfaces.ReportTemplateServiceException;
import sernet.verinice.interfaces.report.IOutputFormat;
import sernet.verinice.model.report.ExcelOutputFormat;
import sernet.verinice.model.report.FileMetaData;
import sernet.verinice.model.report.HTMLOutputFormat;
import sernet.verinice.model.report.ODSOutputFormat;
import sernet.verinice.model.report.ODTOutputFormat;
import sernet.verinice.model.report.PDFOutputFormat;
import sernet.verinice.model.report.PropertyFileExistsException;
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
public abstract class AbstractReportTemplateService implements IReportTemplateService {

    private static final Logger logger = Logger.getLogger(AbstractReportTemplateService.class);
    private static final Pattern PROPERTIES_FILENAME_LOCALE_PATTERN = Pattern
            .compile("_([a-z]{2})\\.properties$");

    protected abstract boolean isHandeledByReportDeposit();

    protected abstract String getTemplateDirectory();

    protected void handleException(String msg, Exception ex) throws ReportTemplateServiceException {
        logger.error(msg, ex);
        throw new ReportTemplateServiceException(ex);
    }

    protected Properties parseAndExtendMetaData(File rptDesign, Locale locale) throws IOException {
        File propFile = PropertiesFileUtil.getPropertiesFile(rptDesign, locale);
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
            props.setProperty(PROPERTIES_OUTPUTFORMATS,
                    StringUtils.join(OutputFormat.values(), ","));
            changed = true;
        }
        if (!(props.containsKey(PROPERTIES_OUTPUTNAME))) {
            props.setProperty(PROPERTIES_OUTPUTNAME, FilenameUtils.removeExtension(fileName));
            changed = true;
        }

        if (changed) {
            OutputStream out = new FileOutputStream(propFile.getAbsoluteFile());
            props.store(out, String.format("Metadata for the report deposit %s",
                    FilenameUtils.getName(rptDesign.getPath())));
        }

        return props;
    }

    public void parseAndExtendMetaData(String[] rptDesignFiles, Locale locale) throws IOException {
        for (String rptDesignFile : rptDesignFiles) {
            parseAndExtendMetaData(new File(rptDesignFile), locale);
        }
    }

    private Properties createDefaultProperties(File path, String name, Locale locale)
            throws IOException, PropertyFileExistsException {
        File propFile = PropertiesFileUtil.getPropertiesFile(path, locale);
        if (propFile.exists()) {
            throw new PropertyFileExistsException();
        } else {
            Properties props = getDefaultProperties(name);
            FileOutputStream fos = new FileOutputStream(propFile);

            props.store(fos, "Default Properties for verinice-" + "Report " + name
                    + "\nauto-generated content");
            fos.close();
            return props;
        }

    }

    private Properties getDefaultProperties(String name) {
        Properties props = new Properties();
        props.setProperty(PROPERTIES_FILENAME, name);
        props.setProperty(PROPERTIES_OUTPUTNAME, FilenameUtils.removeExtension(name));
        props.setProperty(PROPERTIES_OUTPUTFORMATS, StringUtils.join(OutputFormat.values(), ','));
        return props;
    }

    private ReportTemplateMetaData createReportMetaData(Properties props) throws IOException {
        String outputformatsString = props
                .getProperty(IReportDepositService.PROPERTIES_OUTPUTFORMATS);
        StringTokenizer tokenizer = new StringTokenizer(outputformatsString, ",");
        ArrayList<OutputFormat> formats = new ArrayList<>(tokenizer.countTokens());
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken().trim();
            formats.add(OutputFormat.valueOf(token.toUpperCase()));
        }

        String fileName = props.getProperty(IReportDepositService.PROPERTIES_FILENAME);
        String md5CheckSum = getCheckSum(fileName);
        FileMetaData fileMetaData = new FileMetaData(fileName, md5CheckSum);
        String outputName = props.getProperty(IReportDepositService.PROPERTIES_OUTPUTNAME);
        String context = props.getProperty(IReportDepositService.PROPERTIES_CONTEXT);
        OutputFormat[] outputFormats = formats.toArray(new OutputFormat[formats.size()]);
        boolean multipleRootObjects = Boolean.parseBoolean(
                props.getProperty(IReportDepositService.PROPERTIES_MULTIPLE_ROOT_OBJECTS, "false"));

        return new ReportTemplateMetaData(fileMetaData, outputName, outputFormats,
                isHandeledByReportDeposit(), multipleRootObjects, context);
    }

    protected Map<String, byte[]> getPropertiesFiles(String fileName) throws IOException {
        Map<String, byte[]> propertiesFiles = new TreeMap<>();
        Iterator<File> iter = listPropertiesFiles(fileName);
        while (iter.hasNext()) {
            File f = iter.next();
            propertiesFiles.put(f.getName(), FileUtils.readFileToByteArray(f.getAbsoluteFile()));
        }

        return propertiesFiles;
    }

    @SuppressWarnings("unchecked")
    private Iterator<File> listPropertiesFiles(String fileName) {
        String baseName = FilenameUtils.removeExtension(fileName);
        IOFileFilter filter = new RegexFileFilter(baseName + "\\_?.*\\.properties",
                IOCase.INSENSITIVE);
        return FileUtils.iterateFiles(new File(getTemplateDirectory()), filter, null);
    }

    private String getCheckSum(String fileName) throws IOException {
        String filePath;
        if (!fileName.contains(getTemplateDirectory())) {
            filePath = getTemplateDirectory() + File.separatorChar + fileName;
        } else {
            filePath = fileName;
        }
        return DigestUtils.md5Hex(FileUtils.readFileToByteArray(new File(filePath)));
    }

    @Override
    public Set<ReportTemplateMetaData> getReportTemplates(Locale locale)
            throws ReportTemplateServiceException {
        Set<ReportTemplateMetaData> set = new HashSet<>();

        for (String designFilePath : getReportTemplateFileNames()) {
            set.add(toMeta(new File(designFilePath), locale));
        }
        return set;
    }

    @SuppressWarnings({ "unchecked" })
    public String[] getReportTemplateFileNames() {
        List<String> list = new ArrayList<>();
        IOFileFilter filter = new SuffixFileFilter("rptdesign", IOCase.INSENSITIVE);
        Iterator<File> iter = FileUtils.iterateFiles(new File(getTemplateDirectory()), filter,
                null);
        while (iter.hasNext()) {
            list.add(iter.next().getAbsolutePath());
        }
        return list.toArray(new String[list.size()]);
    }

    @Override
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

    @Override
    public IOutputFormat[] getOutputFormats(OutputFormat[] formatLabel) {
        List<IOutputFormat> list = new ArrayList<>(formatLabel.length);
        for (OutputFormat s : formatLabel) {
            IOutputFormat format = getOutputFormat(s);
            if (format != null) {
                list.add(format);
            } else {
                logger.warn("Report output format:\t" + s + " not available in verinice");
            }
        }
        return list.toArray(new IOutputFormat[list.size()]);
    }

    @Override
    public byte[] readResource(String filename) throws ReportTemplateServiceException {
        try {
            String filePath = getTemplateDirectory() + File.separatorChar + filename;
            return FileUtils.readFileToByteArray(new File(filePath));
        } catch (IOException ex) {
            handleException("error while fetching file", ex);
        }

        return null;
    }

    @Override
    public Set<FileMetaData> getAllResources() {
        return toMeta(getAllResourceFilenames());
    }

    @Override
    public Set<FileMetaData> getAllResources(Locale locale) {
        return toMeta(getAllResourceFilenames().filter(filename -> {
            Matcher matcher = PROPERTIES_FILENAME_LOCALE_PATTERN.matcher(filename);
            if (matcher.find()) {
                return matcher.group(1).equals(locale.getLanguage());
            }
            return true;
        }));
    }

    private ReportTemplateMetaData toMeta(File rptDesign, Locale locale)
            throws ReportTemplateServiceException {
        try {
            Properties props;
            File propertiesFile = PropertiesFileUtil.getPropertiesFile(rptDesign, locale);
            if (propertiesFile.exists()) {
                props = parseAndExtendMetaData(rptDesign, locale);
            } else {
                Map<String, byte[]> propertiesFilesAllLocales = getPropertiesFiles(
                        rptDesign.getName());
                if (!propertiesFilesAllLocales.isEmpty()) {
                    Entry<String, byte[]> firstEntry = propertiesFilesAllLocales.entrySet()
                            .iterator().next();
                    FileUtils.writeByteArrayToFile(propertiesFile, firstEntry.getValue());
                    props = parseAndExtendMetaData(rptDesign, locale);
                } else {
                    props = createDefaultProperties(rptDesign, rptDesign.getName(), locale);
                }
            }

            return createReportMetaData(props);
        } catch (IOException | PropertyFileExistsException ex) {
            handleException("error while fetching/generating metadata", ex);
        }

        return null;
    }

    private Set<FileMetaData> toMeta(Stream<String> filenames) {
        Set<FileMetaData> metas = new HashSet<>();
        filenames.forEach(filename -> {
            try {
                metas.add(new FileMetaData(filename, getCheckSum(filename)));
            } catch (IOException ex) {
                Log.error("Failed creating meta data for file " + filename, ex);
            }
        });
        return metas;
    }

    private Stream<String> getAllResourceFilenames() {
        Stream.Builder<String> filenames = Stream.builder();
        IOFileFilter filter = new SuffixFileFilter(
                new String[] { "rptdesign", "rptlibrary", "properties", "js", "css" },
                IOCase.INSENSITIVE);
        Iterator<File> iter = FileUtils.iterateFiles(new File(getTemplateDirectory()), filter,
                null);
        while (iter.hasNext()) {
            filenames.add(iter.next().getName());
        }
        return filenames.build();
    }
}
