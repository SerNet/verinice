/*******************************************************************************
 * Copyright (c) 2009 Daniel Murygin <dm@sernet.de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *     You should have received a copy of the GNU General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Daniel Murygin <dm@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.bsi.model;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;

import sernet.verinice.iso27k.service.FileUtil;

/**
 * File meta-data is loaded and saved by {@link Attachment}.
 * 
 * @author Daniel <dm@sernet.de>
 */
@SuppressWarnings("serial")
public class AttachmentFile implements Serializable{

	Integer dbId;
	

	private byte[] fileData;
	
	public AttachmentFile() {
		super();
	}
	
	public Integer getDbId() {
		return dbId;
	}

	public void setDbId(Integer dbId) {
		this.dbId = dbId;
	}
	
	
	public byte[] getFileData() {
		return fileData;
	}

	public void setFileData(byte[] fileData) {
		this.fileData = fileData;
	}
	

	public void writeFileData(String path) throws IOException {
		FileOutputStream fos = new FileOutputStream(path);
		fos.write(getFileData());
		fos.close(); 
	}
	
	public void readFileData(String path) throws IOException {
		if(getFileData()==null && path!=null) {
			File file = new File(path);
			setFileData(FileUtil.getBytesFromFile(file));
		}
	}
	
}
