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
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
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
    
    public static final String[] ALL_STATIC_FILES = new String[]{
        VERINICE_XML,
        RISK_XML,
        DATA_XSD,
        MAPPING_XSD,
        SYNC_XSD,
        RISK_XSD,
        README_TXT,
    };
    
    static {
        Arrays.sort(ALL_STATIC_FILES);
    }
    
    private Risk riskData = null;
    
    private String uuid;
    
    private String tempFileName = null;
    
    public static final String FILES = "files"; //$NON-NLS-1$

    static final int BUFFER = 2048;
    
    private Map<String, byte[]> contentMap;

    /**
     * Creates a verinice archive instance out of <code>data</code>. 
     * 
     * @param data data of a verinice archive (zip archive)
     * @throws VeriniceArchiveNotValidException In case of a missing entry
     */
    public VeriniceArchive(byte[] data) throws VeriniceArchiveNotValidException {
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
    
    @Override
    public byte[] getFileData(String fileName) {
        String fullPath = getFullPath(fileName);
        try {
            return FileUtils.readFileToByteArray(new File(fullPath));
        } catch (Exception e) {
            LOG.error("Error while loading file data: " + fullPath, e);
            return null;
        }
    }

    private String getFullPath(String fileName) {
        StringBuilder sb = new StringBuilder();       
        sb.append(getTempDirName()).append(File.separator).append(fileName);
        String fullPath = sb.toString();
        return fullPath;
    }
    
    /**
     * Extracts all entries of a Zip-Archive
     * 
     * @param zipFileData Data of a zip archive
     * @throws IOException
     */
    public void extractZipEntries(byte[] zipFileData) throws IOException {
        byte[] buffer = new byte[1024];;        
        new File(getTempDirName()).mkdirs();
        // get the zip file content
        ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipFileData));
        // get the zipped file list entry
        ZipEntry ze = zis.getNextEntry();
        
        while (ze != null) {
            if(!ze.isDirectory()) {              
                String fileName = ze.getName();
                File newFile = new File(getTempDirName() + File.separator + fileName);
                new File(newFile.getParent()).mkdirs();

                FileOutputStream fos = new FileOutputStream(newFile);

                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }

                fos.close();

                if (LOG.isDebugEnabled()) {
                    LOG.debug("File unzipped: " + newFile.getAbsoluteFile());
                }
            }
            ze = zis.getNextEntry();
        }
        zis.closeEntry();
        zis.close();      
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.service.sync.IVeriniceArchive#getSyncRiskAnalysis()
     */
    @Override
    public Risk getSyncRiskAnalysis() {
        if(isRiskAnalysis() && riskData==null) {
            riskData = JAXB.unmarshal(new ByteArrayInputStream(getRiskAnalysisXml()), Risk.class);  
        }
        return riskData;
    }


    private boolean isRiskAnalysis() {
        return new File(getFullPath(RISK_XML)).exists();
    }

    /**
     * Returns all entry names of the archive.
     * 
     * @return all entry names
     */
    public Set<String> getEntrySet() {
        return contentMap.keySet();
    }
    
    /**
     * Returns file verinice.xml from the archive.
     * If there is no verinice.xml in the archive null is returned.
     * 
     * @return verinice.xml from the archive
     */
    @Override
    public byte[] getVeriniceXml() {
        return getFileData(VERINICE_XML);
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.service.sync.IVeriniceArchive#getRiskAnalysisXml()
     */
    @Override
    public byte[] getRiskAnalysisXml() {
        return (isRiskAnalysis()) ? getFileData(RISK_XML) : null;
    }

    /**
     * Returns true if all necessary entries exist
     * 
     * @throws VeriniceArchiveNotValidException In case of a missing entry
     */
    public void checkArchive() {
        if(contentMap.get(VERINICE_XML) == null) {
            throw new VeriniceArchiveNotValidException("File missing in verinice archive: " + VERINICE_XML);
        }
        
    }
    
    /* (non-Javadoc)
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
        if(tempFileName==null) {
            tempFileName = createTempFileName(getUuid());
        }
        return tempFileName;
    }

    private static String createTempFileName(String uuid) {
        String tempDir = System.getProperty(IVeriniceConstants.JAVA_IO_TMPDIR);
        StringBuilder sb = new StringBuilder().append(tempDir);
        if(!tempDir.endsWith(String.valueOf(File.separatorChar))) {
            sb.append(File.separatorChar);
        }
        return sb.append(uuid).toString();
    }

}
