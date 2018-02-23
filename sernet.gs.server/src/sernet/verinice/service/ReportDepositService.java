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
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.core.io.Resource;

import sernet.gs.service.AbstractReportTemplateService;
import sernet.verinice.interfaces.IReportDepositService;
import sernet.verinice.interfaces.ReportDepositException;
import sernet.verinice.model.report.ReportTemplateMetaData;

public class ReportDepositService extends AbstractReportTemplateService
        implements IReportDepositService {

    private static final Logger LOG = Logger.getLogger(ReportDepositService.class);

    private Resource reportDeposit;

    private ReportDepositService() {
    }

    @Override
    public void add(ReportTemplateMetaData metadata, byte[] file, String locale)
            throws ReportDepositException {
        try {
            if ("en".equalsIgnoreCase(locale)) {
                locale = "";
            } else {
                locale = "_" + locale.toLowerCase();
            }
            String filename = metadata.getFilename();
            filename = filename.substring(filename.lastIndexOf(File.separatorChar) + 1);
            File serverDepositPath;
            serverDepositPath = getReportDeposit().getFile();
            String newFilePath = serverDepositPath.getPath() + File.separatorChar + filename;
            FileUtils.writeByteArrayToFile(new File(newFilePath), file);
            writePropertiesFile(convertToProperties(metadata),
                    new File(ensurePropertiesExtension(newFilePath, locale)), "");
        } catch (IOException ex) {
            LOG.error("problems while adding report", ex);
            throw new ReportDepositException(ex);
        }
    }

    @Override
    public void remove(ReportTemplateMetaData metadata, String locale)
            throws ReportDepositException {
        try {
            if ("en".equalsIgnoreCase(locale)) {
                locale = "";
            } else {
                locale = "_" + locale.toLowerCase();
            }
            String filename = metadata.getFilename();
            filename = filename.substring(0,
                    filename.lastIndexOf(IReportDepositService.EXTENSION_SEPARATOR_CHAR));
            filename = filename + locale + IReportDepositService.EXTENSION_SEPARATOR_CHAR
                    + IReportDepositService.PROPERTIES_FILE_EXTENSION;
            File depositDir = getReportDeposit().getFile();

            File propFile = new File(depositDir, filename);
            deleteFile(propFile);

            File rptFile = new File(depositDir, metadata.getFilename());
            deleteFile(rptFile);

        } catch (IOException ex) {
            throw new ReportDepositException(ex);
        }
    }

    private void deleteFile(File rptFile) throws IOException {
        if (rptFile.exists()) {
            Files.delete(rptFile.toPath());
        }
    }

    public Resource getReportDeposit() {
        return reportDeposit;
    }

    public void setReportDeposit(Resource reportDeposit) {
        this.reportDeposit = reportDeposit;
    }

    @Override
    public String getDepositLocation() throws ReportDepositException {
        try {
            if (getReportDeposit() != null) {
                String location = getReportDeposit().getFile().getAbsolutePath();
                if (!(location.endsWith(String.valueOf(File.separatorChar)))) {
                    location = location + File.separatorChar;
                }
                return location;
            }
            return "";
        } catch (IOException ex) {
            throw new ReportDepositException(ex);
        }
    }

    @Override
    public void update(ReportTemplateMetaData metadata, String locale)
            throws ReportDepositException {
        try {
            updateSafe(metadata, locale);
        } catch (IOException ex) {
            throw new ReportDepositException(ex);
        }
    }

    private void updateSafe(ReportTemplateMetaData metadata, String locale)
            throws IOException, ReportDepositException {
        String filename = metadata.getFilename();
        if (filename.contains(String.valueOf(File.separatorChar))) {
            filename = filename.substring(filename.lastIndexOf(File.separatorChar) + 1);
        }
        if (checkReportMetaDataFile(new File(getDepositLocation() + metadata.getFilename()),
                locale)) {
            File propFile = getPropertiesFile(getDepositLocation() + metadata.getFilename(),
                    locale);
            Properties props = parseAndExtendMetaData(propFile, locale);
            props.setProperty(PROPERTIES_OUTPUTFORMATS,
                    StringUtils.join(metadata.getOutputFormats(), ','));
            props.setProperty(PROPERTIES_OUTPUTNAME, metadata.getOutputname());
            props.setProperty(PROPERTIES_MULTIPLE_ROOT_OBJECTS,
                    Boolean.toString(metadata.isMultipleRootObjects()));
            writePropertiesFile(props, propFile, "");
        } else {
            writePropertiesFile(convertToProperties(metadata),
                    getPropertiesFile(metadata.getFilename(), locale),
                    "Default Properties for verinice-" + "Report " + metadata.getOutputname()
                            + "\nauto-generated content");
        }
    }

    private void writePropertiesFile(Properties properties, File propFile, String comment)
            throws IOException {
        String path = getTemplateDirectory() + propFile.getName();
        if (LOG.isDebugEnabled()) {
            LOG.debug("writing properties for " + properties.getProperty(PROPERTIES_FILENAME)
                    + " to " + path);
        }
        FileOutputStream fos = new FileOutputStream(path);
        properties.store(fos, comment);
        fos.close();
    }

    private Properties convertToProperties(ReportTemplateMetaData metaData) {
        Properties props = new Properties();
        props.setProperty(PROPERTIES_OUTPUTFORMATS,
                StringUtils.join(metaData.getOutputFormats(), ','));
        props.setProperty(PROPERTIES_OUTPUTNAME, metaData.getOutputname());
        props.setProperty(PROPERTIES_FILENAME, metaData.getFilename());
        props.setProperty(PROPERTIES_MULTIPLE_ROOT_OBJECTS,
                Boolean.toString(metaData.isMultipleRootObjects()));
        return props;
    }

    private String ensurePropertiesExtension(String filename, String locale) {
        if (filename.contains(String.valueOf('.'))) {
            filename = filename.substring(0, filename.lastIndexOf('.'));
        }
        String oldLocale = locale;
        locale = removeUnderscores(locale);
        if (LOG.isDebugEnabled()) {
            LOG.debug("changed:\t" + oldLocale + "\tto:\t" + locale);
        }
        if (locale != null && !(locale.equals("")) && !filename.endsWith(locale)) {
            if (!filename.endsWith("_")) {
                filename = filename + "_";
            }
            filename = filename + locale;

        }
        return filename + IReportDepositService.EXTENSION_SEPARATOR_CHAR
                + IReportDepositService.PROPERTIES_FILE_EXTENSION;
    }

    private String removeUnderscores(String locale) {
        if (locale.startsWith("_")) {
            locale = removeUnderscores(locale.substring(1));
        }
        if (locale.endsWith("_")) {
            locale = removeUnderscores(locale.substring(0, locale.length() - 1));
        }

        return locale;
    }

    @Override
    public boolean isHandeledByReportDeposit() {
        return true;
    }

    @Override
    public String getTemplateDirectory() {
        try {
            return getDepositLocation();
        } catch (ReportDepositException ex) {
            LOG.error("error while locating report template directory", ex);
            throw new RuntimeException(ex);
        }
    }
}
