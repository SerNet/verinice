/*******************************************************************************
 * Copyright (c) 2009 Daniel Murygin <dm[at]sernet[dot]de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *     You should have received a copy of the GNU Lesser General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Daniel <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.service;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.security.MessageDigest;

import org.apache.log4j.Logger;

/**
 * @author Daniel <dm[at]sernet[dot]de>
 * 
 */
public final class FileUtil {

    private FileUtil() {
        super();
    }

    public static byte[] getBytesFromInputstream(InputStream is) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[16384];
        while ((nRead = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        buffer.flush();
        return buffer.toByteArray();
    }

    /**
     * Usage: Charset charsetFrom = Charset.forName("UTF-8"); Charset charsetTo
     * = Charset.forName("ISO-8859-15");
     * 
     * 
     * @param charsetFrom
     * @param charsetTo
     * @return
     */
    public static byte[] changeEncoding(byte[] byteArray, Charset charsetFrom, Charset charsetTo) {
        ByteBuffer inputBuffer = ByteBuffer.wrap(byteArray);
        // decode charsetFrom
        CharBuffer data = charsetFrom.decode(inputBuffer);
        // encode charsetTo
        ByteBuffer outputBuffer = charsetTo.encode(data);
        return outputBuffer.array();
    }

    public static void writeStringToFile(String content, String filename) throws IOException {
        BufferedWriter writer = null;
        writer = new BufferedWriter(new FileWriter(filename));
        writer.write(content);

        if (writer != null) {
            writer.close();
        }
    }

    private static byte[] createChecksum(String filename) throws Exception {
        InputStream fis = new FileInputStream(filename);

        byte[] buffer = new byte[1024];
        MessageDigest complete = MessageDigest.getInstance("MD5");
        int numRead;

        do {
            numRead = fis.read(buffer);
            if (numRead > 0) {
                complete.update(buffer, 0, numRead);
            }
        } while (numRead != -1);

        fis.close();
        return complete.digest();
    }

    // see this How-to for a faster way to convert
    // a byte array to a HEX string
    public static String getMD5Checksum(String filename) throws Exception {
        byte[] b = createChecksum(filename);
        String result = "";

        for (int i = 0; i < b.length; i++) {
            result += Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
        }
        return result;
    }
    
    public static byte[] getFileData(File f){
        try {
            return Files.readAllBytes(f.toPath());

        } catch (IOException e) {
            throw new RuntimeException("Error while reading file data",e);
        }     
    }
    
    public static boolean deleteDirectory(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    deleteDirectory(files[i]);
                } else {
                    files[i].delete();
                }
            }
        }
        return (path.delete());
    }
    
    public static String getFolderFromPath(String path) {
        String returnPath = null;
        if(path!=null && path.indexOf(File.separatorChar)!=-1) {
            returnPath = path.substring(0, path.lastIndexOf(File.separatorChar)+1);
        }
        return returnPath;
    }

}
