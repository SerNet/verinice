/*******************************************************************************
 * Copyright (c) 2011 Daniel Murygin.
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
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.service.sync;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.bind.JAXB;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import de.sernet.sync.risk.Risk;
import sernet.verinice.interfaces.IVeriniceConstants;

/**
 * Class to read the content of a verinice archive.
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class VeriniceArchive extends PureXml implements IVeriniceArchive {

    private static final Logger LOG = Logger.getLogger(VeriniceArchive.class);

    public static final String EXTENSION_VERINICE_ARCHIVE = ".vna"; //$NON-NLS-1$

    public static final String VERINICE_XML = "verinice.xml"; //$NON-NLS-1$
    public static final String RISK_XML = "verinice-risk-analysis.xml"; //$NON-NLS-1$
    public static final String DATA_XSD = "data.xsd"; //$NON-NLS-1$
    public static final String MAPPING_XSD = "mapping.xsd"; //$NON-NLS-1$
    public static final String SYNC_XSD = "sync.xsd"; //$NON-NLS-1$
    public static final String RISK_XSD = "risk.xsd"; //$NON-NLS-1$
    public static final String README_TXT = "readme.txt"; //$NON-NLS-1$

    private Risk riskData = null;

    private String uuid;

    private String tempFileName = null;

    public static final String FILES = "files"; //$NON-NLS-1$

    /**
     * Creates a verinice archive instance out of <code>data</code>.
     * 
     * @param data
     *            data of a verinice archive (zip archive)
     * @throws VeriniceArchiveNotValidException
     *             In case of a missing entry
     */
    public VeriniceArchive(InputStream data) throws VeriniceArchiveNotValidException {
        super();
        uuid = UUID.randomUUID().toString();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Creating new VeriniceArchive...");
        }
        try {
            extractZipEntries(data);
        } catch (VeriniceArchiveNotValidException e) {
            LOG.error("Error while reading verinice archive", e);
            throw e;
        } catch (Exception e) {
            LOG.error("Error while reading verinice archive", e);
            throw new VeriniceArchiveNotValidException(e);
        }
    }

    /**
     * Creates a verinice archive instance out of <code>data</code>.
     * 
     * @param data
     *            data of a verinice archive (zip archive)
     * @throws VeriniceArchiveNotValidException
     *             In case of a missing entry
     */
    public VeriniceArchive(byte[] data) throws VeriniceArchiveNotValidException {
        this(new ByteArrayInputStream(data));
    }

    @Override
    public InputStream getFileData(String fileName) {
        Path fullPath = getFullPath(fileName);
        try {
            return Files.newInputStream(fullPath);
        } catch (IOException e) {
            throw new RuntimeException("Error while loading file data: " + fullPath, e);
        }
    }

    private Path getFullPath(String fileName) {
        Path tmpDir = Paths.get(getTempDirName());
        Path fullPath = tmpDir.resolve(fileName).normalize();
        if (!fullPath.startsWith(tmpDir)) {
            throw new IllegalArgumentException("File " + fileName + " not contained in archive");
        }
        return fullPath;
    }

    /**
     * Extracts all entries of a Zip-Archive
     * 
     * @param zipFileData
     *            Data of a zip archive
     * @throws IOException
     */
    private void extractZipEntries(InputStream zipFileData) throws IOException {
        Path tmpDir = Paths.get(getTempDirName());
        Files.createDirectories(tmpDir);
        // get the zip file content
        try (ZipInputStream zis = new ZipInputStream(zipFileData)) {
            // get the zipped file list entry
            ZipEntry ze = zis.getNextEntry();

            while (ze != null) {
                if (!ze.isDirectory()) {
                    String fileName = ze.getName();
                    Path newPath = tmpDir.resolve(fileName);

                    boolean stillInTempFolder = newPath.normalize().startsWith(tmpDir);
                    if (!stillInTempFolder) {
                        throw new VeriniceArchiveNotValidException(
                                "Path Traversal in VNA detected! Stopping import.");
                    }
                    Files.createDirectories(newPath.getParent());
                    Files.copy(zis, newPath, StandardCopyOption.REPLACE_EXISTING);

                    if (LOG.isDebugEnabled()) {
                        LOG.debug("File unzipped: " + newPath.toAbsolutePath().toString());
                    }
                }
                ze = zis.getNextEntry();
            }
            zis.closeEntry();
        }
    }

    /*
     * @see sernet.verinice.service.sync.IVeriniceArchive#getSyncRiskAnalysis()
     */
    @Override
    public Risk getSyncRiskAnalysis() {
        if (isRiskAnalysis() && riskData == null) {
            riskData = JAXB.unmarshal(getRiskAnalysisXml(), Risk.class);
        }
        return riskData;
    }

    private boolean isRiskAnalysis() {
        return Files.exists(getFullPath(RISK_XML));
    }

    /**
     * Returns file verinice.xml from the archive. If there is no verinice.xml
     * in the archive null is returned.
     * 
     * @return verinice.xml from the archive
     */
    @Override
    public InputStream getVeriniceXml() {
        return getFileData(VERINICE_XML);
    }

    /*
     * @see sernet.verinice.service.sync.IVeriniceArchive#getRiskAnalysisXml()
     */
    @Override
    public InputStream getRiskAnalysisXml() {
        return (isRiskAnalysis()) ? getFileData(RISK_XML) : null;
    }

    /*
     * @see sernet.verinice.service.sync.IVeriniceArchive#clear()
     */
    @Override
    public void clear() {
        try {
            FileUtils.deleteDirectory(new File(getTempDirName()));
        } catch (IOException e) {
            LOG.error("Error while deleting zipfile content.", e);
        }
    }

    public String getUuid() {
        return uuid;
    }

    public String getTempDirName() {
        if (tempFileName == null) {
            tempFileName = createTempFileName(getUuid());
        }
        return tempFileName;
    }

    private static String createTempFileName(String uuid) {
        String tempDir = System.getProperty(IVeriniceConstants.JAVA_IO_TMPDIR);
        StringBuilder sb = new StringBuilder().append(tempDir);
        if (!tempDir.endsWith(String.valueOf(File.separatorChar))) {
            sb.append(File.separatorChar);
        }
        return sb.append(uuid).toString();
    }

}
