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
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Locale;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.core.io.Resource;

import sernet.gs.service.AbstractReportTemplateService;
import sernet.gs.service.PropertiesFileUtil;
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
    public void add(ReportTemplateMetaData metadata, byte[] file, Locale locale)
            throws ReportDepositException {
        try {
            lock.writeLock().lock();
            File reportFileToStore = new File(reportDeposit.getFile(),
                    FilenameUtils.getName(metadata.getFilename()));
            FileUtils.writeByteArrayToFile(reportFileToStore, file);
            writePropertiesFile(convertToProperties(metadata),
                    PropertiesFileUtil.getPropertiesFile(reportFileToStore, locale), "");
        } catch (IOException ex) {
            LOG.error("problems while adding report", ex);
            throw new ReportDepositException(ex);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void remove(ReportTemplateMetaData metadata, Locale locale)
            throws ReportDepositException {
        try {
            lock.writeLock().lock();
            File propertiesFilename = PropertiesFileUtil
                    .getPropertiesFile(new File(metadata.getFilename()), locale);
            File depositDir = reportDeposit.getFile();

            File propFile = new File(depositDir, propertiesFilename.getPath());
            deleteFile(propFile);

            File rptFile = new File(depositDir, metadata.getFilename());
            deleteFile(rptFile);

        } catch (IOException ex) {
            throw new ReportDepositException(ex);
        } finally {
            lock.writeLock().unlock();
        }
    }

    private void deleteFile(File rptFile) throws IOException {
        try {
            lock.writeLock().lock();
            if (rptFile.exists()) {
                Files.delete(rptFile.toPath());
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    public Resource getReportDeposit() {
        return reportDeposit;
    }

    public void setReportDeposit(Resource reportDeposit) {
        this.reportDeposit = reportDeposit;
    }

    @Override
    public void update(ReportTemplateMetaData oldMetadata, byte[] file,
            ReportTemplateMetaData newMetadata, Locale locale) throws ReportDepositException {
        try {
            lock.writeLock().lock();
            remove(oldMetadata, locale);
            add(newMetadata, file, locale);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void update(ReportTemplateMetaData metadata, Locale locale)
            throws ReportDepositException {
        try {
            lock.writeLock().lock();
            updateSafe(metadata, locale);
        } catch (IOException ex) {
            throw new ReportDepositException(ex);
        } finally {
            lock.writeLock().unlock();
        }
    }

    private void updateSafe(ReportTemplateMetaData metadata, Locale locale) throws IOException {
        File propertiesFile = PropertiesFileUtil.getPropertiesFile(
                new File(getTemplateDirectory(), metadata.getFilename()), locale);
        if (propertiesFile.exists()) {

            Properties props = parseAndExtendMetaData(propertiesFile, locale);
            props.setProperty(PROPERTIES_OUTPUTFORMATS,
                    StringUtils.join(metadata.getOutputFormats(), ','));
            props.setProperty(PROPERTIES_OUTPUTNAME, metadata.getOutputname());
            props.setProperty(PROPERTIES_MULTIPLE_ROOT_OBJECTS,
                    Boolean.toString(metadata.isMultipleRootObjects()));
            props.setProperty(PROPERTIES_CONTEXT, metadata.getContext().prettyString());
            writePropertiesFile(props, propertiesFile, "");
        } else {
            writePropertiesFile(convertToProperties(metadata),
                    PropertiesFileUtil.getPropertiesFile(new File(metadata.getFilename()), locale),
                    "Default Properties for verinice-" + "Report " + metadata.getOutputname()
                            + "\nauto-generated content");
        }
    }

    private void writePropertiesFile(Properties properties, File propFile, String comment)
            throws IOException {
        File file = new File(getTemplateDirectory(), propFile.getName());
        if (LOG.isDebugEnabled()) {
            LOG.debug("writing properties for " + properties.getProperty(PROPERTIES_FILENAME)
                    + " to " + file);
        }
        try (OutputStream fos = Files.newOutputStream(file.toPath())) {
            properties.store(fos, comment);
        }
    }

    private Properties convertToProperties(ReportTemplateMetaData metaData) {
        Properties props = new Properties();
        props.setProperty(PROPERTIES_OUTPUTFORMATS,
                StringUtils.join(metaData.getOutputFormats(), ','));
        props.setProperty(PROPERTIES_OUTPUTNAME, metaData.getOutputname());
        props.setProperty(PROPERTIES_FILENAME, metaData.getFilename());
        props.setProperty(PROPERTIES_MULTIPLE_ROOT_OBJECTS,
                Boolean.toString(metaData.isMultipleRootObjects()));
        props.setProperty(PROPERTIES_CONTEXT, metaData.getContext().prettyString());
        return props;
    }

    @Override
    protected boolean isHandeledByReportDeposit() {
        return true;
    }

    @Override
    protected File getTemplateDirectory() {
        try {
            return reportDeposit.getFile();
        } catch (IOException ex) {
            LOG.error("error while locating report template directory", ex);
            throw new RuntimeException(ex);
        }
    }
}
