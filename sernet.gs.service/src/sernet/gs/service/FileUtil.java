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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

/**
 * @author Daniel <dm[at]sernet[dot]de>
 *
 */
public class FileUtil {
	
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
	 * Usage:
	 * Charset charsetFrom = Charset.forName("UTF-8");
     * Charset charsetTo = Charset.forName("ISO-8859-15");
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

}
