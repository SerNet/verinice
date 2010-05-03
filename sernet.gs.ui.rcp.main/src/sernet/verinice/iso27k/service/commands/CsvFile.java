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
package sernet.verinice.iso27k.service.commands;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.net.URL;

import sernet.verinice.iso27k.service.FileUtil;

/**
 * @author Daniel <dm[at]sernet[dot]de>
 *
 */
public class CsvFile implements Serializable{
	
	String filePath;
	
	byte[] fileContent;
	
	public CsvFile(InputStream is) throws IOException {
        super();
        this.filePath = "unknown";
        setFileContent(FileUtil.getBytesFromInputstream(is));
    }
	
	public CsvFile(String filePath) throws IOException {
		super();
		this.filePath = filePath;
		readFile();
	}
	
	public CsvFile(URL url) throws IOException  {
	    super();
	    // url to file: http://weblogs.java.net/blog/2007/04/25/how-convert-javaneturl-javaiofile
    	File f;
    	try {
    	  f = new File(url.toURI());
    	} catch(Exception e) {
    	  f = new File(url.getPath());
    	}
    	this.filePath = f.getAbsolutePath();
    	readFile();
	}

	
	public CsvFile(byte[] fileContent) throws IOException {
		super();
		setFileContent(fileContent);
	}

	public void readFile() throws IOException {
		if( getFilePath()!=null) {
			File file = new File(getFilePath());
			setFileContent(FileUtil.getBytesFromFile(file));
		}
	}
	
	public byte[] getFileContent() {
		return fileContent;
	}

	public void setFileContent(byte[] fileContent) {
		this.fileContent = fileContent;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String getFilePath() {
		return filePath;
	}
}
