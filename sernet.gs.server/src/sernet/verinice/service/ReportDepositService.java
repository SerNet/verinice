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
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.core.io.Resource;

import sernet.gs.service.AbstractReportTemplateService;
import sernet.verinice.interfaces.IReportDepositService;
import sernet.verinice.model.report.ReportTemplateMetaData;

public class ReportDepositService extends AbstractReportTemplateService implements IReportDepositService {

    private static final Logger LOG = Logger.getLogger(ReportDepositService.class);

    private Resource reportDeposit;

    private ReportDepositService() {
    }

    @Override
    public void addToServerDeposit(ReportTemplateMetaData metadata, byte[] file, String locale) throws IOException {
        if ("en".equals(locale.toLowerCase())) {
            locale = "";
        } else {
            locale = "_" + locale.toLowerCase();
        }
        String filename = metadata.getFilename();
        filename = filename.substring(filename.lastIndexOf(File.separatorChar) + 1);
        File serverDepositPath;
        serverDepositPath = getReportDeposit().getFile();
        String newFilePath = serverDepositPath.getPath() + File.separatorChar + filename;
        String propFilePath = filename.substring(0, filename.lastIndexOf(IReportDepositService.EXTENSION_SEPARATOR_CHAR)) + locale + IReportDepositService.EXTENSION_SEPARATOR_CHAR + IReportDepositService.PROPERTIES_FILE_EXTENSION;
        FileUtils.writeByteArrayToFile(new File(newFilePath), file);
        writePropertiesFile(convertToProperties(metadata), new File(ensurePropertiesExtension(newFilePath, locale)), "");
    }

    @Override
    public void removeFromServer(ReportTemplateMetaData metadata, String locale) throws IOException {
        if ("en".equals(locale.toLowerCase())) {
            locale = "";
        } else {
            locale = "_" + locale.toLowerCase();
        }
        String filename = metadata.getFilename();
        filename = filename.substring(0, filename.lastIndexOf(IReportDepositService.EXTENSION_SEPARATOR_CHAR));
        filename = filename + locale + IReportDepositService.EXTENSION_SEPARATOR_CHAR + IReportDepositService.PROPERTIES_FILE_EXTENSION;
        File depositDir = getReportDeposit().getFile();
        File propFile = new File(depositDir, filename);
        if (propFile.exists()) {
            FileUtils.deleteQuietly(propFile);
        }
        File rptFile = new File(depositDir, metadata.getFilename());
        if (rptFile.exists()) {
            FileUtils.deleteQuietly(rptFile);
        }
    }

    public Resource getReportDeposit() {
        return reportDeposit;
    }

    public void setReportDeposit(Resource reportDeposit) {
        this.reportDeposit = reportDeposit;
    }

    @Override
    public String getDepositLocation() throws IOException {
        if (getReportDeposit() != null) {
            String location = getReportDeposit().getFile().getAbsolutePath();
            if(!(location.endsWith(String.valueOf(File.separatorChar)))){
                location = location + File.separatorChar;
            }
            return location;
        }
        return "";
    }

    @Override
    public void updateInServerDeposit(ReportTemplateMetaData metadata, String locale) throws IOException {
        String filename = metadata.getFilename();
        if (filename.contains(String.valueOf(File.separatorChar))) {
            filename = filename.substring(filename.lastIndexOf(File.separatorChar) + 1);
        }
        if (checkReportMetaDataFile(new File(getDepositLocation() + metadata.getFilename()), locale)) {
            File propFile = getPropertiesFile(getDepositLocation() + metadata.getFilename(), locale);
            Properties props = parseAndExtendMetaData(propFile, locale);
            props.setProperty(PROPERTIES_OUTPUTFORMATS, StringUtils.join(metadata.getOutputFormats(), ','));
            props.setProperty(PROPERTIES_OUTPUTNAME, metadata.getOutputname());
            writePropertiesFile(props, propFile, "");

        } else {
            writePropertiesFile(convertToProperties(metadata), getPropertiesFile(metadata.getFilename(), locale), "Default Properties for verinice-" + "Report " + metadata.getOutputname() + "\nauto-generated content");
        }
    }
    
    @Deprecated
    private String removeWrongPathSeparators(String path){
        if(LOG.isDebugEnabled()){
            LOG.debug("File.separatorChar:\t" + File.separatorChar);
        }
        if(path.contains("/") && !(String.valueOf(File.separatorChar).equals("/"))){
            String oldPath = path;
            path = path.replaceAll("/", Matcher.quoteReplacement(String.valueOf(File.separatorChar)));
            if(LOG.isDebugEnabled()){
                LOG.debug(oldPath + "\t replaced with \t" + path);
            }
        }
        if(path.contains("\\") && !(String.valueOf(File.separatorChar).equals("\\"))){
            String oldPath = path;
            path = path.replaceAll(Pattern.quote("\\"), Matcher.quoteReplacement(String.valueOf(File.separatorChar)));
            if(LOG.isDebugEnabled()){
                LOG.debug(oldPath + "\t replaced with \t" + path);
            }
        }
        return path;
        
    }

    private void writePropertiesFile(Properties properties, File propFile, String comment) throws IOException {
        String path = getTemplateDirectory() + propFile.getName();
        if(LOG.isDebugEnabled()){
            LOG.debug("writing properties for " + properties.getProperty(PROPERTIES_FILENAME) + " to " + path);
        }
        FileOutputStream fos = new FileOutputStream(path);
        properties.store(fos, comment);
        fos.close();
    }

    private Properties convertToProperties(ReportTemplateMetaData metaData) {
        Properties props = new Properties();
        props.setProperty(PROPERTIES_OUTPUTFORMATS, StringUtils.join(metaData.getOutputFormats(), ','));
        props.setProperty(PROPERTIES_OUTPUTNAME, metaData.getOutputname());
        props.setProperty(PROPERTIES_FILENAME, metaData.getFilename());
        return props;
    }

    private String ensurePropertiesExtension(String filename, String locale) {
        if (filename.contains(String.valueOf('.'))) {
            filename = filename.substring(0, filename.lastIndexOf('.'));
        }
        String oldLocale = locale;
        locale = removeUnderscores(locale);
        if(LOG.isDebugEnabled()){
            LOG.debug("changed:\t" + oldLocale + "\tto:\t" + locale);
        }
        if(locale != null && !(locale.equals("")) && !filename.endsWith(locale)){
            if(!filename.endsWith("_")){
                filename = filename + "_";
            }
            filename = filename + locale;
            
        }
        return filename + IReportDepositService.EXTENSION_SEPARATOR_CHAR + IReportDepositService.PROPERTIES_FILE_EXTENSION;
    }
    
    private String removeUnderscores(String locale){
        if(locale.startsWith("_")){
            locale = removeUnderscores(locale.substring(1));
        }
        if(locale.endsWith("_")){
            locale = removeUnderscores(locale.substring(0, locale.length() - 1));
        }
        
        return locale;
    }

    @Override
    public boolean isServerSide() {
        return true;
    }

    @Override
    public String getTemplateDirectory() {
        try {
            return getDepositLocation();
        } catch (IOException ex) {
            LOG.error("error while locating report template directory", ex);
            throw new RuntimeException(ex);
        }
    }
}
