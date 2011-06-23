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

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

/**
 * Class to read the content of a verinice archive.
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de> 
 */
public class VeriniceArchive {

    private static final Logger LOG = Logger.getLogger(VeriniceArchive.class);

    public static final String EXTENSION_VERINICE_ARCHIVE = ".vna"; //$NON-NLS-1$
    
    public static final String VERINICE_XML = "verinice.xml"; //$NON-NLS-1$
    public static final String DATA_XSD = "data.xsd"; //$NON-NLS-1$
    public static final String MAPPING_XSD = "mapping.xsd"; //$NON-NLS-1$
    public static final String SYNC_XSD = "sync.xsd"; //$NON-NLS-1$
    public static final String README_TXT = "readme.txt"; //$NON-NLS-1$
    
    public static final String[] ALL_STATIC_FILES = new String[]{
        VERINICE_XML,
        DATA_XSD,
        MAPPING_XSD,
        SYNC_XSD,
        README_TXT,
    };
    
    static {
        Arrays.sort(ALL_STATIC_FILES);
    }
    
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
        if (LOG.isDebugEnabled()) {
            LOG.debug("Creating new VeriniceArchive...");
        }
        try {
            contentMap = extractZipEntries(data, Arrays.asList(ALL_STATIC_FILES));           
            checkArchive();
        } catch (VeriniceArchiveNotValidException e) {
            LOG.error("Error while reading verinice archive", e);
            throw e;
        } catch (Exception e) {
            LOG.error("Error while reading verinice archive", e);
            throw new VeriniceArchiveNotValidException(e);
        }
    }
    
    /**
     * Extracts all entries in <code>entryList</code> from <code>zipFileData</code>.
     * Returns the files in a map, key is the path, value is the file data.
     * 
     * All entries of the zip archive which are not in <code>entryList</code> are not extracted and not returned
     * but the path is stored in the returned map as a key with no value.
     * 
     * @param zipFileData Data of a zip archive
     * @param entryList A list of zip archive entry pathes 
     * @return Data of selected files 
     * @throws IOException
     */
    public static Map<String, byte[]> extractZipEntries(byte[] zipFileData, List<String> entryList) throws IOException {
        Map<String, byte[]> result = new HashMap<String, byte[]>(entryList.size());
        String tempFileName = createTempFileName();
        File tempFile = new File(tempFileName);
        try {
            ZipEntry entry;

            // write byte array to a temp file
            // because there is no other way to create a ZipFile
            FileUtils.writeByteArrayToFile(tempFile, zipFileData);
            if (LOG.isDebugEnabled()) {
                LOG.debug("VeriniceArchive saved to temp file: " + tempFileName);
            }
            ZipFile zipfile = new ZipFile(tempFile);

            Enumeration<? extends ZipEntry> e = zipfile.entries();

            while (e.hasMoreElements()) {
                entry = e.nextElement();
                String name = entry.getName();
                
                if(entryList.contains(name)) {
                    result.put(name, getEntryAsByteArray(zipfile, entry));
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Entry extracted: " + name);
                    }                
                } else  {         
                    // store the name to extract content later
                    result.put(name, null);
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Entry NOT extracted, name stored: " + name );
                    }
                }
            }
            return result;
        } finally {
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
                if (LOG.isDebugEnabled()) {
                    LOG.debug("VeriniceArchive temp file deleted: tempFileName");
                }
            }
        }
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
    public byte[] getVeriniceXml() {
        return contentMap.get(VERINICE_XML);
    }

    /**
     * Returns true if all necessary entries exist
     * 
     * @throws VeriniceArchiveNotValidException In case of a missing entry
     */
    public void checkArchive() throws VeriniceArchiveNotValidException {
        if(contentMap.get(VERINICE_XML) == null) {
            throw new VeriniceArchiveNotValidException("File missing in verinice archive: " + VERINICE_XML);
        }
        
    }

    private static byte[] getEntryAsByteArray(ZipFile zipfile, ZipEntry entry) throws IOException {
        BufferedInputStream is;
        is = new BufferedInputStream(zipfile.getInputStream(entry));
        int count;
        byte dataBuffer[] = new byte[BUFFER];
        ByteArrayOutputStream dest = new ByteArrayOutputStream(BUFFER);
        while ((count = is.read(dataBuffer, 0, BUFFER)) != -1) {
            dest.write(dataBuffer, 0, count);
        }
        dest.flush();
        byte[] zipEntryData = dest.toByteArray();
        is.close();
        return zipEntryData;
    }

    /**
     * 
     */
    private static String createTempFileName() {
        String tempDir = System.getProperty("java.io.tmpdir");
        StringBuilder sb = new StringBuilder().append(tempDir);
        if(!tempDir.endsWith(String.valueOf(File.separatorChar))) {
            sb.append(File.separatorChar);
        }
        return sb.append(UUID.randomUUID().toString()).append(VeriniceArchive.EXTENSION_VERINICE_ARCHIVE).toString();
    }

    

}
