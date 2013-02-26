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
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.model.bsi;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;

import org.apache.commons.io.FileUtils;

import sernet.hui.common.connect.ITypedElement;

/**
 * File meta-data is loaded and saved by {@link Attachment}.
 * 
 * @author Daniel <dm[at]sernet[dot]de>
 */
@SuppressWarnings("serial")
public class AttachmentFile implements Serializable, ITypedElement {

	private Integer dbId;
	

	private byte[] fileData;


    public static final String TYPE_ID = "attachmentfile";
	
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
		return (fileData != null) ? fileData.clone() : null;
	}

	public void setFileData(byte[] fileData) {
		this.fileData = (fileData != null) ? fileData.clone() : null;
	}
	

	public void writeFileData(String path) throws IOException {
		FileOutputStream fos = new FileOutputStream(path);
		fos.write(getFileData());
		fos.close(); 
	}
	
	public void readFileData(String path) throws IOException {
		if(getFileData()==null && path!=null) {
			File file = new File(path);
			setFileData(FileUtils.readFileToByteArray(file));
		}
	}

    /* (non-Javadoc)
     * @see sernet.hui.common.connect.ITypedElement#getTypeId()
     */
    public String getTypeId() {
        return TYPE_ID;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((dbId == null) ? 0 : dbId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj){
            return true;
        }
        if (obj == null){
            return false;
        }
        if (getClass() != obj.getClass()){
            return false;
        }
        AttachmentFile other = (AttachmentFile) obj;
        if (dbId == null) {
            if (other.dbId != null){
                return false;
            }
        } else if (!dbId.equals(other.dbId)){
            return false;
        }
        return true;
    }
    
    
	
}
