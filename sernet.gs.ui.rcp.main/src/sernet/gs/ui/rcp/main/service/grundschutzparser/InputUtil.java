/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak[at]sernet[dot]de>.
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
 *     Alexander Koderman <ak[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.service.grundschutzparser;

import java.io.IOException;
import java.io.InputStream;

public abstract class InputUtil {

    public static String streamToString(InputStream in, String charset) throws IOException {
        final int byteArraySize = 4096;
        StringBuffer out = new StringBuffer();
        byte[] b = new byte[byteArraySize];
        if(in!=null) {
            for (int n; (n = in.read(b)) != -1;) {
                out.append(new String(b, 0, n, charset));
            }
        } else {
            out.append("");
        }
        return out.toString();
    }

}
