/*******************************************************************************
 * Copyright (c) 2014 Benjamin Weißenfels.
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
 *     Benjamin Weißenfels <bw[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.logging;

import java.io.File;

import org.apache.commons.io.*;


/**
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 *
 */
public class WindowsLogDirectory implements LogDirectoryProvider {

    private String filePath;
    
    public WindowsLogDirectory(String filePath) {
        this.filePath = filePath;
    }
   
    @Override
    public String getLogDirectory() {
        String directory = FilenameUtils.getFullPath(FilenameUtils.separatorsToSystem(filePath));
        return removeInvalidPrefix(directory);
    }
    
    private String removeInvalidPrefix(String directory){
        if (directory.startsWith("file:" + File.separator) ){
            return directory.substring(6);
        }
        
        return directory;
    }

}
