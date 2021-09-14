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

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

/**
 * @author Daniel <dm[at]sernet[dot]de>
 * 
 */
public final class FileUtil {

    private FileUtil() {
        super();
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

    public static String getFolderFromPath(String path) {
        String returnPath = null;
        if (path != null && path.indexOf(File.separatorChar) != -1) {
            returnPath = path.substring(0, path.lastIndexOf(File.separatorChar) + 1);
        }
        return returnPath;
    }

}
