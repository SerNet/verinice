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
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.charset.Charset;

import org.apache.commons.io.FileUtils;


/**
 * @author Daniel <dm[at]sernet[dot]de>
 *
 */
public class CsvFile implements Serializable{
	
    public final static Charset CHARSET_DEFAULT = VeriniceCharset.CHARSET_UTF_8;
	
    String filePath;
	
	byte[] fileContent;
	
	public CsvFile(InputStream is) throws IOException {
	    this(is,CHARSET_DEFAULT);
	}
	
	public CsvFile(InputStream is, Charset charset) throws IOException {
        super();
        this.filePath = "unknown";
        byte[] content = FileUtil.getBytesFromInputstream(is);
        if(!VeriniceCharset.CHARSET_UTF_8.equals(charset)) {
            content = FileUtil.changeEncoding(content, charset, VeriniceCharset.CHARSET_UTF_8);
        }
        setFileContent(content);
    }
	
	public CsvFile(String filePath) throws IOException {
        this(filePath,CHARSET_DEFAULT);
    }
	
	public CsvFile(String filePath, Charset charset) throws IOException {
		super();
		this.filePath = filePath;
		readFile(charset);
	}

	
	public CsvFile(byte[] fileContent) throws IOException {
		super();
		setFileContent(fileContent);
	}

	public void readFile(Charset charset) throws IOException {
		if( getFilePath()!=null) {
			File file = new File(getFilePath());
			byte[] content = FileUtils.readFileToByteArray(file);
			if(!VeriniceCharset.CHARSET_UTF_8.equals(charset)) {
			    content = FileUtil.changeEncoding(content, charset, VeriniceCharset.CHARSET_UTF_8);
			}
			setFileContent(content);
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
